package travel.kiri.backend.algorithm;

/**
 * Merepresentasikan sebuah edge pada graph. In oldmenjangan: dijkstra.h/Edge
 * @author PascalAlfadian
 *
 */
public class GraphEdge {
	/**
	 * Determining the node that this edge points.
	 * FIXME GraphNode diganti jadi int dl
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
	char type;
	
	
	
	public GraphEdge(int node, double weight, char type)
	{
		this.node=node;
		this.weight=weight;
		this.type = 0;
	}
	
	public int getNode()
	{
		return node;
	}
	
	/*FIXME Multiplier walkingnya dimasukin ke sini*/
	public double getWeight()
	{
		return weight;
	}
	
	public char getType()
	{
		return type;
	}
	
	
	
}
