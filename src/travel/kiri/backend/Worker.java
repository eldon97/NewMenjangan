package travel.kiri.backend;

import travel.kiri.backend.algorithm.LatLon;

/**
 * The class responsible for processing routing requests.
 * @author PascalAlfadian
 *
 */
public class Worker {
	/**
	 * Show the summary of internal track information to the log file. Useful
	 * for debugging.
	 */
	void printTracksInfo() {
		// FIXME implement this
	}

	/**
	 * Show the internal places information (including the graph) to the log
	 * file. Useful for debugging.
	 */
	void printPlacesInfo() {
		// FIXME implement this
	}
	
	/**
	 * Compute the shortest path between the start point and the end point.
	 * @param start the start location to route
	 * @param finish the finish location to route
	 * @param customMaximumWalkingDistance the custome maximum walking distance, or null to use default
	 * @param customMultiplierWalking the custom walking multiplier, or null to use default
	 * @param customPenaltyTransfer the custom penalty transfer, or null to use default
	 * @return string containing the steps, as specified in Kalapa-Dago protocol.
	 */
	String startComputing(LatLon start, LatLon finish, Double customMaximumWalkingDistance,
			Double customMultiplierWalking, double customPenaltyTransfer) {
		// FIXME implement this
		// TODO rename this method to findRoute
		return null;
	}
	
	/**
	 * Find all public transports nearby the given point.
	 * @param location the location find the nearby public transports.
	 * @param double customMaximumWalkingDistance the custom maximum distance from this point, or null to use default
	 * @return string containing the nearby transport, as specified in Kalapa-Dago protocol.
	 */
	String findNearbyTransports(LatLon location,
			Double customMaximumWalkingDistance) {
		// FIXME implement this
		return null;
	}
	
	/**
	 * Toggle the verbose flag, determining whether extra information is to be printed by the workers.
	 * @return true if after the toggle, verbose is now turned on.
	 */
	boolean toggleVerbose() {
		// FIXME implement this
		return false;
	}

	/**
	 * Reset the statistic values.
	 */
	void resetStatistics() {
		// FIXME implement this
	}

	/**
	 * Return the number of requests since last reset.
	 * @return the number of requests
	 */
	int getNumberOfRequests() {
		// FIXME implement this
		return 0;
	}

	/**
	 * Return the total processing time for all requests.
	 * @return the total process time.
	 */
	double getTotalProcessTime() {
		// FIXME implement this
		return 0;
	}

}
