package travel.kiri.backend.puller;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.geojson.Feature;
import org.geojson.LngLatAlt;
import org.geojson.MultiLineString;

import travel.kiri.backend.Main;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DataPuller {
	public static final double EARTH_RADIUS = 6371.0;
	public static final Double MAX_DISTANCE = 0.1;
	public static final String ANGKOTWEBID_URL = "https://angkot.web.id/route/transportation/%s.json";
	public static final String DEFAULT_PENALTY = "0.05";
	public static final double MAX_LINK_DISTANCE = 0.5;

	public void pull(File sqlPropertiesFile, PrintStream output)
			throws IOException, SQLException {
		Properties sqlProperties = new Properties();
		sqlProperties.load(new FileReader(sqlPropertiesFile));
		Connection connection = null;
		connection = DriverManager.getConnection(String.format(
				"jdbc:mysql://%s/%s?user=%s&password=%s",
				sqlProperties.get("host"), sqlProperties.get("database"),
				sqlProperties.get("user"), sqlProperties.get("password")));
		Statement statement = connection.createStatement();
		ResultSet result = statement
				.executeQuery("SELECT trackTypeId, trackId, AsText(geodata), pathloop, penalty, transferNodes, internalInfo FROM tracks ORDER BY trackId");

		while (result.next()) {
			if (result.getString(7) != null
					&& result.getString(7).startsWith("angkotwebid:")) {
				String[] fields = result.getString(7).split(":");
				AngkotWebIdResult awiResult = formatTrackFromAngkotWebId(
						fields[1], result.getString(1), result.getString(2));
				if (awiResult != null) {
					output.println(awiResult.getTrackInConfFormat());
					Statement updateStatement = connection.createStatement();
					String sql = String
							.format("UPDATE tracks SET internalInfo='%s', geodata=%s WHERE trackTypeId='%s' AND trackId='%s'",
									fields[0] + ':' + fields[1]
											+ ':' + awiResult.lastUpdate,
									awiResult.getTrackInMySQLFormat(),
									result.getString(1),
									result.getString(2));
					updateStatement
							.execute(sql);
				}
			} else if (result.getString(3) != null) {
				AngkotWebIdResult awiResult = formatTrack(result.getString(1), result
						.getString(2), lineStringToLngLatArray(result
						.getString(3)), result.getString(4).equals("1") ? true
						: false, result.getString(5), result.getString(6), 0);
				output.println(awiResult.getTrackInConfFormat());
			} else {
				throw new DataPullerException("Route not found everywhere for "
						+ result.getString(1) + "." + result.getString(2));
			}
		}

		result.close();
		statement.close();
		connection.close();
	}

	private static LngLatAlt[] lineStringToLngLatArray(String wktText) {
		wktText = wktText.replace("LINESTRING(", "").replace(")", "");
		String[] textCoordinates = wktText.split(",");
		LngLatAlt[] coordinates = new LngLatAlt[textCoordinates.length];
		for (int i = 0; i < textCoordinates.length; i++) {
			String[] textLonlat = textCoordinates[i].split(" ");
			LngLatAlt coordinate = new LngLatAlt(
					Double.parseDouble(textLonlat[0]),
					Double.parseDouble(textLonlat[1]));
			coordinates[i] = coordinate;
		}
		return coordinates;
	}

	private static double computeDistance(LngLatAlt p1, LngLatAlt p2) {
		double lat1 = p1.getLatitude(), lon1 = p1.getLongitude();
		double lat2 = p2.getLatitude(), lon2 = p2.getLongitude();
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2)
				* Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return EARTH_RADIUS * c;
	}

	private AngkotWebIdResult formatTrack(String trackTypeId, String trackId,
			LngLatAlt[] geodata, boolean isPathLoop, String penalty,
			String transferNodesStr, int lastUpdate) {

		// Setup track info
		LngLatAlt[] tracks = geodata;
		int[][] transitNodes;
		if (transferNodesStr == null || transferNodesStr.length() == 0) {
			transitNodes = new int[][] { { 0, tracks.length - 1 } };
		} else {
			String[] transitNodesString = transferNodesStr.split(",");
			transitNodes = new int[transitNodesString.length][2];
			for (int i = 0; i < transitNodes.length; i++) {
				String[] numbers = transitNodesString[i].split("-");
				transitNodes[i][0] = Integer.parseInt(numbers[0]);
				transitNodes[i][1] = Integer
						.parseInt(numbers.length > 1 ? numbers[1] : numbers[0]);
			}
		}

		List<LngLatAlt> trackString = new ArrayList<LngLatAlt>();

		int insertedNodes = 0;
		int[] transferNodesOffset = new int[tracks.length];
		LngLatAlt previousPoint = null;

		// Print tracks
		for (int i = 0; i < tracks.length; i++) {
			LngLatAlt currentPoint = tracks[i];
			if (i > 0) {
				boolean inTransitNode = false;
				for (int j = 0; j < transitNodes.length; j++) {
					if (i >= transitNodes[j][0] && i <= transitNodes[j][1]) {
						inTransitNode = true;
					}
				}
				// then, check if we have to add virtual nodes
				double distance;
				if (MAX_DISTANCE != null
						&& (distance = computeDistance(currentPoint,
								previousPoint)) > MAX_DISTANCE && inTransitNode) {
					int extraNodes = (int) Math.ceil(distance / MAX_DISTANCE) - 1;
					for (int j = 1; j <= extraNodes; j++) {
						double lat = previousPoint.getLatitude()
								+ j
								* (currentPoint.getLatitude() - previousPoint
										.getLatitude()) / extraNodes;
						double lng = previousPoint.getLongitude()
								+ j
								* (currentPoint.getLongitude() - previousPoint
										.getLongitude()) / extraNodes;
						LngLatAlt extraPoint = new LngLatAlt(lng, lat);
						trackString.add(extraPoint);
					}
					insertedNodes += extraNodes;
				}
			}
			transferNodesOffset[i] = insertedNodes;
			trackString.add(currentPoint);
			previousPoint = currentPoint;
		}

		for (int i = 0; i < transitNodes.length; i++) {
			// Adjust with offset
			for (int j = 0; j < 2; j++) {
				transitNodes[i][j] += transferNodesOffset[transitNodes[i][j]];
			}
		}
		StringBuilder finalTextConf = new StringBuilder();
		StringBuilder finalTextMySQL = new StringBuilder("GeomFromText('LineString(");
		finalTextConf.append(trackTypeId + "." + trackId + "\t");
		finalTextConf.append(penalty + "\t");
		finalTextConf.append(trackString.size() + "\t");
		for (int i = 0; i < trackString.size(); i++) {
			if (i > 0) {
				finalTextConf.append(" ");
				finalTextMySQL.append(",");
			}
			finalTextConf.append(String.format("%.6f %.6f", trackString.get(i)
					.getLatitude(), trackString.get(i).getLongitude()));
			finalTextMySQL.append(String.format("%.6f %.6f", trackString.get(i)
					.getLongitude(), trackString.get(i).getLatitude()));
		}
		finalTextConf.append("\t");
		finalTextConf.append((isPathLoop ? "1" : "0") + "\t");
		for (int i = 0; i < transitNodes.length; i++) {
			if (i > 0) {
				finalTextConf.append(",");
			}
			if (transitNodes[i][0] == transitNodes[i][1]) {
				finalTextConf.append(transitNodes[i][0]);
			} else {
				finalTextConf.append(String.format("%d-%d", transitNodes[i][0],
						transitNodes[i][1]));
			}
		}
		finalTextMySQL.append(")')");
		return new AngkotWebIdResult(lastUpdate, finalTextConf.toString(), finalTextMySQL.toString());
	}

	private AngkotWebIdResult formatTrackFromAngkotWebId(String angkotId,
			String trackTypeId, String trackId) throws IOException {
		URL url = new URL(String.format(ANGKOTWEBID_URL, angkotId));
		Main.globalLogger.info("Fetching " + trackTypeId + "." + trackId
				+ " from " + url + "...");
		JsonFactory factory = new JsonFactory();
		JsonParser parser = factory.createParser(url);

		int lastUpdate = -1;
		while (!parser.isClosed()) {
			JsonToken token = parser.nextToken();
			if (token == null) {
				break;
			}
			if (JsonToken.FIELD_NAME.equals(token)
					&& "updated".equals(parser.getCurrentName())) {
				parser.nextValue();
				lastUpdate = parser.getValueAsInt();
			} else if (JsonToken.FIELD_NAME.equals(token)
					&& "geojson".equals(parser.getCurrentName())) {
				parser.nextBooleanValue();
				Feature feature = new ObjectMapper().readValue(parser,
						Feature.class);
				List<List<LngLatAlt>> coordinates = ((MultiLineString) feature
						.getGeometry()).getCoordinates();
				List<LngLatAlt> finalCoordinates;
				boolean isPathLoop;
				if (coordinates.size() == 0) {
					Main.globalLogger.warning(String.format(
							"%s.%s/%s has zero routes, will be ignored.",
							trackTypeId, trackId, angkotId));
					return null;
				}
				if (coordinates.size() == 1) {
					finalCoordinates = coordinates.get(0);
					isPathLoop = computeDistance(finalCoordinates.get(0),
							finalCoordinates.get(finalCoordinates.size() - 1)) < MAX_LINK_DISTANCE;
				} else if (coordinates.size() == 2) {
					List<LngLatAlt> c1 = coordinates.get(0);
					List<LngLatAlt> c2 = coordinates.get(1);
					if (computeDistance(c1.get(c1.size() - 1), c2.get(0)) < MAX_LINK_DISTANCE
							&& computeDistance(c1.get(0), c2.get(c2.size() - 1)) < MAX_LINK_DISTANCE) {
						finalCoordinates = c1;
						finalCoordinates.addAll(c2);
						isPathLoop = true;
					} else if (computeDistance(c1.get(0), c2.get(0)) < MAX_LINK_DISTANCE
							&& computeDistance(c1.get(c1.size() - 1),
									c2.get(c2.size() - 1)) < MAX_LINK_DISTANCE) {
						finalCoordinates = c1;
						for (int j = c2.size() - 1; j >= 0; j--) {
							finalCoordinates.add(c2.get(j));
						}
						isPathLoop = true;
					} else {
						throw new DataPullerException(
								String.format(
										"Does not support linking tracks that far away: %s.%s/%s ",
										trackTypeId, trackId, angkotId));
					}
				} else {
					Main.globalLogger
							.warning(String
									.format("Does not support tracks with %d routes: %s.%s/%s ",
											coordinates.size(), trackTypeId,
											trackId, angkotId));
					return null;
				}
				AngkotWebIdResult result = formatTrack(trackTypeId, trackId,
						finalCoordinates.toArray(new LngLatAlt[0]), isPathLoop,
						DEFAULT_PENALTY, null, lastUpdate);
				return result;
			}
		}
		Main.globalLogger.warning("Doesn't have GeoJSON info: " + angkotId);
		return null;
	}

	public static class AngkotWebIdResult {
		private final int lastUpdate;
		private final String trackInConfFormat;
		private final String trackInMySQLFormat;

		public AngkotWebIdResult(int lastUpdate, String trackInConfFormat,
				String trackInMySQLFormat) {
			super();
			this.lastUpdate = lastUpdate;
			this.trackInConfFormat = trackInConfFormat;
			this.trackInMySQLFormat = trackInMySQLFormat;
		}

		public int getLastUpdate() {
			return lastUpdate;
		}

		public String getTrackInConfFormat() {
			return trackInConfFormat;
		}

		public String getTrackInMySQLFormat() {
			return trackInMySQLFormat;
		}

	}
}
