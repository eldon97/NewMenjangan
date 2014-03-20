package travel.kiri.backend.algorithm;

/**
 * Representing a latitude/longitude position. In oldmenjangan: none
 * @author PascalAlfadian
 *
 */
public class LatLon {
	/**
	 * Earth radius, in km.
	 */
	private static final double EARTH_RADIUS = 6371.0;
	
	public final double lat;
	public final double lon;
	
	/**
	 * Creates a new lat/lon instance
	 * @param lat latitude
	 * @param lon longitude
	 */
	public LatLon(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
	}
	
	public String toString()
	{
		return lat+" "+lon;
	}
	
	/**
	 * Computes the distance of this lat/lon to another one.
	 * @param target the target lat/lon
	 * @return distance, in km.
	 */
	public double distanceTo(LatLon target) {
		double lat1 = this.lat, lon1 = this.lon;
		double lat2 = target.lat, lon2 = target.lon;
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2)
				* Math.cos(lat1) * Math.cos(lat2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return EARTH_RADIUS * c;
	}
}
