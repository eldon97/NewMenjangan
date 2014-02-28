package travel.kiri.backend.algorithm;

import java.util.List;

/**
 * Merepresentasikan sebuah penghitung jarak terdekat dengan Dijkstra.
 * Referensi dari dijsktra.cc
 * @author PascalAlfadian
 *
 */
public class Dijkstra {
	/**
	 * Class constructor. All allocations should go here.
	 * @param graph the list of nodes, specifying the graph
	 * @param startNode reference to the starting node
	 * @param finishNode reference to the finish node
	 * @param computeMemorySize whether to compute the memory size required for this instance or not.
	 */
	Dijkstra(List<GraphNode> graph, GraphNode startNode, GraphNode finishNode, boolean computeMemorySize) {
		// FIXME implement this
	}
	
	/**
	 * Run the Dijkstra algorithm, based on the input. After the function
	 * is run, {@link Dijkstra#getDistance(GraphNode)} will retrieve distance of each node from start
	 * node and {@link Dijkstra#getParent(GraphNode)} will retrieve the parent of each node.
	 * Complexity: O(|E| + |V| log |V|)
	 * @return the distance from source, or {@link Double#POSITIVE_INFINITY} if no path was found.
	 */
	double runAlgorithm() {
		// FIXME implement this
		return 0;
	}
	
	/**
	 * Retrieves the parent of a particular node.
	 * @param node the node to check.
	 * @return the parent of requested node, or null if it has no parent.
	 */
	GraphNode getParent(GraphNode node) {
		// FIXME implement this
		return null;
	}

	/**
	 * Retrieves the distance of a particular node.
	 * @param node the node to check.
	 * @return the distance of this node from the starting node.
	 */
	double getDistance(GraphNode node) {
		// FIXME implement this
		return 0;
	}
}
