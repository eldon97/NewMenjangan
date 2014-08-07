package travel.kiri.backend.algorithm;

/**
 * Merepresentasikan sebuah edge pada graph. In oldmenjangan: dijkstra.h/Edge
 * @author PascalAlfadian
 *
 */
public class GraphEdge {
	/**
	 * Determining the node that this edge points.
	 */
	int node;
	
	/**
	 * Determine the weight of this edge.
	 */
	double weight;
	
	/**
	 * Type of transportation in this edge
	 * Used for precomputation
	 *   0: No Precompute <default>
	 *   1: Angkot Transfer
	 *   @obsolete Implement like this first, then see if this can be merged to
	 *   another attribute.
	 */
	
	
	
	public GraphEdge(int node, double weight)
	{
		this.node=node;
		this.weight=weight;
	}
	
	public int getNode()
	{
		return node;
	}
	
	public double getWeight()
	{
		return weight;
	}
}
