package travel.kiri.backend.algorithm;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Basically, this class stores a graph, i.e. list of {@link GraphNode}s. However
 * we create a custom class to allow range searching. An initial implementation may
 * use a simple array list, but later should be improved to contain k-d tree as well
 * for fast range searching.
 * @author PascalAlfadian
 *
 */
public class Graph extends ArrayList<GraphNode>{
	/*
	public void addNode(GraphNode node) {
		// FIXME implement this
	}

	public Iterator<GraphNode> getNodeIterator() {
		// FIXME implement this
		return null;
	}*/
	
	/**
	 * Searches for all nodes within a specified range
	 * @param center center of the circle
	 * @param distance radius of the circle
	 * @return all nodes within the circle.
	 */
	public Collection<GraphNode> rangeSearch(LatLon center, double distance) {
		// FIXME implement this!
		
		ArrayList<GraphNode> list = new ArrayList<GraphNode>();
		
		for(GraphNode node : this)
		{
			if(center.distanceTo(node.location)<=distance)
			{
				list.add(node);
			}
		}
		
		return list;	
	}
}
