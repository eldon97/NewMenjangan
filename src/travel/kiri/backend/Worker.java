package travel.kiri.backend;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Scanner;

import travel.kiri.backend.algorithm.Dijkstra;
import travel.kiri.backend.algorithm.Graph;
import travel.kiri.backend.algorithm.GraphNode;
import travel.kiri.backend.algorithm.LatLon;
import travel.kiri.backend.algorithm.Track;
import edu.wlu.cs.levy.CG.KDTree;
import edu.wlu.cs.levy.CG.KeyDuplicateException;
import edu.wlu.cs.levy.CG.KeySizeException;

/**
 * The class responsible for processing routing requests.
 * 
 * @author PascalAlfadian
 * 
 */
public class Worker {

	public Double globalMaximumWalkingDistance;
	public Double global_maximum_transfer_distance;
	public Double globalMultiplierWalking;
	public Double globalPenaltyTransfer;
	
	public int numberOfRequests;
	// in millis, needs to be converted to seconds later
	public long totalProcessTime;

	List<Track> tracks;
	Graph nodes;

	public Worker(String homeDirectory) throws FileNotFoundException, IOException {
		tracks = new ArrayList<Track>();
		nodes = new Graph();
		readConfiguration(homeDirectory + "/" + Main.MJNSERVE_PROPERTIES);
		Main.globalLogger.info("Configuration were read successfully");
		readGraph(homeDirectory + "/" + Main.TRACKS_CONF);
		Main.globalLogger.info("Tracks were read successfully");
		linkAngkots();
		cleanUpMemory();
		Main.globalLogger.info("Tracks were linked successfully");
	}

	/**
	 * Cleans up memory used during precomputation.
	 */
	private void cleanUpMemory() {
		for (GraphNode node: nodes) {
			node.getEdges().cleanUpMemory();
		}
		System.gc();
	}

	private void readConfiguration(String filename)
			throws FileNotFoundException, IOException {
		Properties properties = new Properties();
		properties.load(new FileInputStream(filename));
		globalMaximumWalkingDistance = new Double(
				properties.getProperty("maximum_walking_distance"));
		global_maximum_transfer_distance = new Double(
				properties.getProperty("maximum_transfer_distance"));
		globalMultiplierWalking = new Double(
				properties.getProperty("multiplier_walking"));
		globalPenaltyTransfer = new Double(
				properties.getProperty("penalty_transfer"));
	}

	/**
	 * Show the summary of internal track information to the log file. Useful
	 * for debugging.
	 * 
	 * @return
	 */
	String printTracksInfo() {
		StringBuilder sb = new StringBuilder();

		sb.append("Loaded tracks information\n");
		sb.append(tracks.size() + " tracks.\n");
		sb.append(nodes.size() + " nodes.\n");
		int transferNodes = 0;
		int edgesCount = 0;
		for (GraphNode node : nodes) {
			if (node.isTransferNode()) {
				transferNodes++;
			}
			edgesCount += node.getEdges().size();
		}
		sb.append(transferNodes + " transfer nodes.\n");
		sb.append(edgesCount + " edges.\n");

		sb.append("Maximum walking = " + globalMaximumWalkingDistance + "\n");
		sb.append("Maximum transfer = " + global_maximum_transfer_distance
				+ "\n");
		sb.append("Walking multiplier = " + globalMultiplierWalking + "\n");
		sb.append("Transfer penalty = " + globalPenaltyTransfer + "\n");
		sb.append("Log Level = " + Main.globalLogger.getLevel() + "\n");

		return sb.toString();
	}

	/**
	 * Compute the shortest path between the start point and the end point.
	 * 
	 * @param start
	 *            the start location to route
	 * @param finish
	 *            the finish location to route
	 * @param customMaximumWalkingDistance
	 *            the custome maximum walking distance, or null to use default
	 * @param customMultiplierWalking
	 *            the custom walking multiplier, or null to use default
	 * @param customPenaltyTransfer
	 *            the custom penalty transfer, or null to use default
	 * @return string containing the steps, as specified in Kalapa-Dago
	 *         protocol.
	 */
	public String findRoute(LatLon start, LatLon finish,
			Double customMaximumWalkingDistance,
			Double customMultiplierWalking, Double customPenaltyTransfer) {

		long startTime, endTime;

		Main.globalLogger.fine("Worker started for " + start + " to " + finish);
		
		// thread and stuff
		startTime = System.currentTimeMillis();

		// setting the parameters
		if (customMaximumWalkingDistance == null
				|| customMaximumWalkingDistance == -1) {
			customMaximumWalkingDistance = globalMaximumWalkingDistance;
		}
		if (customMultiplierWalking == null || customMultiplierWalking == -1) {
			customMultiplierWalking = globalMultiplierWalking;
		}
		if (customPenaltyTransfer == null || customPenaltyTransfer == -1) {
			customPenaltyTransfer = globalPenaltyTransfer;
		}

		// create virtual graph

		int vNodesSize = nodes.size() + 2;
		int startNode = nodes.size();
		int endNode = nodes.size() + 1;

		Graph vNodes = new Graph(vNodesSize);
		GraphNode realNode;
		for (int i = 0; i < nodes.size(); i++) {
			realNode = nodes.get(i);
			vNodes.add(new GraphNode(realNode.getLocation(), realNode
					.getTrack()));
			vNodes.get(i).link(realNode);
		}

		// add start and finish node
		vNodes.add(new GraphNode(start, null));
		vNodes.add(new GraphNode(finish, null));

		// Link startNode to other nodes by walking
		for (int i = 0; i < nodes.size(); i++) {
			double distance = start.distanceTo(nodes.get(i).getLocation());
			if (distance <= customMaximumWalkingDistance
					&& nodes.get(i).isTransferNode()) {
				vNodes.get(startNode).push_back(i, (float)distance);
			}
		}

		// Link endNode to other nodes by walking
		for (int i = 0; i < nodes.size(); i++) {
			double distance = finish.distanceTo(nodes.get(i).getLocation());
			if (distance <= customMaximumWalkingDistance
					&& nodes.get(i).isTransferNode()) {
				vNodes.get(i).push_back(endNode, (float)distance);
			}
		}

		{
			double distance = start.distanceTo(finish);
			if (distance <= customMaximumWalkingDistance) {
				vNodes.get(startNode).push_back(endNode,
						(float)(customMultiplierWalking * distance));
				vNodes.get(endNode).push_back(startNode,
						(float)(customMultiplierWalking * distance));
			}
		}

		Dijkstra dijkstra = new Dijkstra(vNodes, startNode, endNode,
				customMultiplierWalking, customPenaltyTransfer);
		dijkstra.runAlgorithm();

		// traversing
		int currentNode = endNode;

		int lastNode, angkotLength = 0;
		double distance = 0;
		StringBuilder line = new StringBuilder();
		List<String> steps = new ArrayList<String>();

		while (dijkstra.getParent(currentNode) != Dijkstra.DIJKSTRA_NULLNODE) {
			lastNode = currentNode;
			currentNode = dijkstra.getParent(currentNode);

			if (lastNode == endNode
					|| currentNode == startNode
					|| !nodes.get(currentNode).getTrack()
							.equals(nodes.get(lastNode).getTrack())) {
				if (angkotLength > 0) {
					Track t = nodes.get(lastNode).getTrack();
					line.insert(0, "/");
					line.insert(0, t.getTrackId());
					line.insert(0, "/");
					line.insert(0, t.getTrackTypeId());

					line.append(String.format(Locale.US, "%.3f", distance));
					line.append("/");
					// places line.append(b)
					line.append("\n");

					steps.add(line.toString());
				}

				distance = (dijkstra.getDistance(lastNode) - dijkstra
						.getDistance(currentNode)) / customMultiplierWalking;
				if (!(lastNode == endNode || currentNode == startNode)) {
					distance -= customPenaltyTransfer;
				}

				line = new StringBuilder("walk/walk/");
				if (currentNode == startNode) {
					line.append("start ");
				} else {
					LatLon location = nodes.get(currentNode).getLocation();
					line.append(String.format(Locale.US, "%.5f,%.5f ",
							location.lat, location.lon));
				}

				if (lastNode == endNode) {
					line.append("finish");
				} else {
					LatLon location = nodes.get(lastNode).getLocation();
					line.append(String.format(Locale.US, "%.5f,%.5f",
							location.lat, location.lon));
				}

				line.append(String.format(Locale.US, "/%.3f/\n", distance));

				steps.add(line.toString());

				if (currentNode != startNode) {
					LatLon location = nodes.get(currentNode).getLocation();
					line = new StringBuilder(String.format(Locale.US,
							"%.5f,%.5f/", location.lat, location.lon));
					distance = 0;
					angkotLength = 1;
				}
			} else {
				// angkot!!
				distance += (dijkstra.getDistance(lastNode) - dijkstra
						.getDistance(currentNode))
						/ nodes.get(currentNode).getTrack().getPenalty();

				LatLon location = nodes.get(currentNode).getLocation();
				line.insert(0, String.format(Locale.US, "%.5f,%.5f ",
						location.lat, location.lon));
				angkotLength++;
			}
		}

		StringBuilder retval = new StringBuilder();
		if (steps.size() == 0) {
			retval.append("none\n");
		} else {
			for (int i = steps.size() - 1; i >= 0; i--) {
				retval.append(steps.get(i));
			}
		}

		endTime = System.currentTimeMillis();

		// logs
		long diff = endTime - startTime;
		numberOfRequests++;
		totalProcessTime+=diff;
		
		Main.globalLogger.fine("Worker ended, elapsed: "+diff+" milliseconds");

		return retval.toString();
	}

	/**
	 * Reset the statistic values.
	 */
	void resetStatistics() {
		numberOfRequests = 0;
		totalProcessTime = 0;
	}

	/**
	 * Return the number of requests since last reset.
	 * 
	 * @return the number of requests
	 */
	int getNumberOfRequests() {
		return numberOfRequests;
	}

	/**
	 * Return the total processing time for all requests.
	 * 
	 * @return the total process time.
	 */
	double getTotalProcessTime() {
		return totalProcessTime / 1000.0;
	}

	public boolean readGraph(String filename) {
		try {
			Scanner linescan = new Scanner(new File(filename));
			Scanner scan;
			String line, word;
			while (linescan.hasNextLine()) {
				line = linescan.nextLine();
				// kalau ada karakter dan bukan comment
				if (line.length() > 0 && line.charAt(0) != '#') {
					scan = new Scanner(line);

					// trackid
					word = scan.next();
					Track track = new Track(word);

					// penalty
					word = scan.next();
					track.setPenalty(Double.parseDouble(word));

					// number of nodes
					int numOfNodes = scan.nextInt();

					// foreach nodes
					for (int i = 0; i < numOfNodes; i++) {
						int nodeIndex = nodes.size();
						// parse lat and lon
						float lat = Float.parseFloat(scan.next());
						float lon = Float.parseFloat(scan.next());
						GraphNode node = new GraphNode(new LatLon(lat, lon),
								track);
						track.addNode(node);
						nodes.add(node);

						// connect it with the previous node
						if (i > 0) {
							double distance = node.getLocation().distanceTo(
									track.getNode(i - 1).getLocation());
							nodes.get(nodeIndex - 1).push_back(nodeIndex,
									(float)distance);
						}
					}

					// loop
					int loop = scan.nextInt();

					// if loop
					if (loop > 0) {
						// connect last node to the first
						GraphNode first = track.getNode(0);
						GraphNode last = track.getNode(numOfNodes - 1);

						int firstIndex = nodes.size() - track.getSize();
						int lastIndex = nodes.size() - 1;

						double distance = first.getLocation().distanceTo(
								last.getLocation());
						// pushback edge
						nodes.get(lastIndex).push_back(firstIndex, (float)distance);
					}

					// transferNodes
					word = scan.next();
					String[] tnodes = word.split(",");
					// nodes
					int start, finish;
					for (int i = 0; i < tnodes.length; i++) {
						// if a-b
						if (tnodes[i].indexOf("-") != -1) {
							String[] sf = tnodes[i].split("-");
							start = Integer.parseInt(sf[0]);
							finish = Integer.parseInt(sf[1]);
						} else {
							start = Integer.parseInt(tnodes[i]);
							finish = start;
						}

						for (int j = start; j <= finish; j++) {
							track.getNode(j).setTransferNode(true);
						}
					}

					tracks.add(track);

				}
			}
			linescan.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	void linkAngkots() {
		KDTree<GraphNodeContainer> kd=new KDTree<GraphNodeContainer>(2);
		float minLat=Float.POSITIVE_INFINITY;
		float minLon=Float.POSITIVE_INFINITY;
		float maxLat=Float.NEGATIVE_INFINITY;
		float maxLon=Float.NEGATIVE_INFINITY;
		for(int i=0;i<nodes.size();i++)
		{
			GraphNode n = nodes.get(i);
			double[] key = {n.getLocation().lat, n.getLocation().lon};
			
			if(minLat>n.getLocation().lat)
			{
				minLat=n.getLocation().lat;
			}
			if(maxLat<n.getLocation().lat)
			{
				maxLat=n.getLocation().lat;
			}
			if(minLon>n.getLocation().lon)
			{
				minLon=n.getLocation().lon;
			}
			if(maxLon<n.getLocation().lon)
			{
				maxLon=n.getLocation().lon;
			}
			
			boolean ok = true;
			while(ok)
			{
					try {
						kd.insert(key, new GraphNodeContainer(n,i));
						ok=false;
					} catch (KeySizeException e) {
						e.printStackTrace();
					} catch (KeyDuplicateException e) {
						key[0]+=0.00001;
						key[1]+=0.00001;
					}					
			}
		}
		
		LatLon minLoc = new LatLon(minLat, minLon);

		double latPerKm = (maxLat-minLat)/minLoc.distanceTo(new LatLon(maxLat, minLon));
		double lonPerKm = (maxLon-minLon)/minLoc.distanceTo(new LatLon(minLat, maxLon));
		double threshold = 2;
		for(int i=0;i<nodes.size();i++)
		{
			GraphNode n = nodes.get(i);
			double[] lowk = {n.getLocation().lat-(threshold*latPerKm*global_maximum_transfer_distance), n.getLocation().lon-(threshold*lonPerKm*global_maximum_transfer_distance)};
			double[] uppk = {n.getLocation().lat+(threshold*latPerKm*global_maximum_transfer_distance), n.getLocation().lon+(threshold*lonPerKm*global_maximum_transfer_distance)};
			List<GraphNodeContainer> nearby=null;
			try {
				nearby =  kd.range(lowk, uppk);
			} catch (KeySizeException e) {
				e.printStackTrace();
			}
			
			for(GraphNodeContainer near: nearby)
			{
				// if not in same track and both are transferNode
				if (!(nodes.get(i).getTrack().equals(near.gn.getTrack()))
						&& nodes.get(i).isTransferNode()
						&& near.gn.isTransferNode()) {
					double distance = nodes.get(i).getLocation()
							.distanceTo(near.gn.getLocation());
					if (distance < global_maximum_transfer_distance) {
						nodes.get(i).push_back(near.index, (float)distance);
						near.gn.push_back(i, (float)distance);
					}
				}
			}
		}
	}

	public String toString() {
		String t = "";
		for (Track tr : tracks) {
			t += tr + "\n";
		}
		return t;
	}
	
	private static class GraphNodeContainer
	{
		public GraphNode gn;
		public int index;
		
		public GraphNodeContainer(GraphNode gn, int index)
		{
			this.gn=gn;
			this.index=index;
		}
	}
	
	public String findNearbyTransports(LatLon start, Double customMaximumWalkingDistance)
	{
		if(customMaximumWalkingDistance == null || customMaximumWalkingDistance == -1)
		{
			customMaximumWalkingDistance = globalMaximumWalkingDistance;
		}
		
		StringBuilder res = new StringBuilder();
		
		//for each tracks, check minimum distance
		for(int idx = 0; idx<tracks.size();idx++)
		{
			Track t = tracks.get(idx);	
			int tSize = t.getSize();
			double min=Double.POSITIVE_INFINITY;
			for(int i=0;i<tSize;i++)
			{
				double dist = start.distanceTo(t.getNode(i).getLocation());
				if(dist<min)
				{
					min=dist;
				}
			}
			
			if(min<=customMaximumWalkingDistance)
			{
				res.append(t.getTrackTypeId());
				res.append("/");
				res.append(t.getTrackId());
				res.append("/");
				res.append(String.format(Locale.US, "%.3f", min));
				res.append("\n");
			}
		}
		
		return res.toString();
	}

}
