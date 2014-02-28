package travel.kiri.backend.algorithm;

/**
 * Representing a graph node. Reference: worker.cc/GraphNode
 * @author PascalAlfadian
 *
 */
public class GraphNode {
	/**
	 * Location (lat/lon) of this node.
	 */
	LatLon location;
	
	/**
	 * List of out-edges from this node.
	 */
	FastLinkedList<GraphEdge> edges;
	
	/**
	 * A reverse-reference to the track information.
	 */
	Track track;
	
	/**
	 * Determines whether this is a transfer node (whether a person can
	 * alight or board the public transport from this node).
	 */
	boolean isTransferNode;
	

	/**
	 * Create new instance of the graph node.
	 * @param location The latitude/longitude.
	 * @param track the reverse-reference to the track information.
	 */
	public GraphNode(LatLon location, Track track) {
		// FIXME implement this.
	}
	
}
