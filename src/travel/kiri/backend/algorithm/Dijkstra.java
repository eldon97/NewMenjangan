package travel.kiri.backend.algorithm;

import java.util.List;
import java.util.Set;

/**
 * Merepresentasikan sebuah penghitung jarak terdekat dengan Dijkstra.
 * Referensi dari dijsktra.cc
 * @author PascalAlfadian
 *
 */
public class Dijkstra {
	
	public static final int DIJKSTRA_NULLNODE = -1;
	
	List<GraphNode> graph;
	int startNode, finishNode;
	
	NodeInfo[] nodeInfoLinks;
	NodeInfo[] nodesMinHeap;
	int heapsize;
	int numOfNodes;
	
	int memorySize;

	final double multiplierWalking;
	final double penaltyTransfer;
	
	
	/**
	 * Class constructor. All allocations should go here.
	 * @param graph the list of nodes, specifying the graph
	 * @param startNode reference to the starting node
	 * @param finishNode reference to the finish node
	 */
	public Dijkstra(Graph graph, int startNode, int finishNode, double multiplierWalking, double penaltyTransfer) {
		this.graph=graph;
		this.startNode=startNode;
		this.finishNode=finishNode;
		this.numOfNodes=graph.size();
		this.nodesMinHeap = new NodeInfo[numOfNodes];
		this.nodeInfoLinks = new NodeInfo[numOfNodes];
		this.multiplierWalking = multiplierWalking;
		this.penaltyTransfer = penaltyTransfer;
		
		for(int i=0;i<numOfNodes;i++)
		{
			nodeInfoLinks[i]=new NodeInfo();
		}
		
	}
	
	/**
	 * Run the Dijkstra algorithm, based on the input. After the function
	 * is run, {@link Dijkstra#getDistance(GraphNode)} will retrieve distance of each node from start
	 * node and {@link Dijkstra#getParent(GraphNode)} will retrieve the parent of each node.
	 * Complexity: O(|E| + |V| log |V|)
	 * @param trackTypeIdBlacklist Set of track type ids blacklisted on navigation, or null to allow everything
	 * @return the distance from source, or {@link Double#POSITIVE_INFINITY} if no path was found.
	 */
	public double runAlgorithm(Set<String> trackTypeIdBlacklist) {
		
		heapsize = 0;
		for(int i=0;i<numOfNodes;i++)
		{
			NodeInfo ni = nodeInfoLinks[i];
			ni.baseIndex=i;
			ni.heapIndex=i;
			ni.distance=Double.POSITIVE_INFINITY;
			ni.parent=DIJKSTRA_NULLNODE;
			nodesMinHeap[heapsize++] = ni;
		}
		nodeInfoLinks[startNode].distance = 0;
		//heapify!
		
		for(int i=heapsize/2-1; i>=0; i--)
		{
			heapPercolateDown(i);
		}
		
		NodeInfo currentNode;
		
		do
		{
			currentNode = heapDeleteMin();
			if(currentNode == null || currentNode.baseIndex == finishNode)
			{
				break;
			}
			
			UnrolledLinkedList<GraphEdge> edges = graph.get(currentNode.baseIndex).edges;
			
			for(GraphEdge edge : edges)
			{
				double weight = calculateWeight(currentNode, edge);
				if(currentNode.distance + weight < nodeInfoLinks[edge.node].distance && (trackTypeIdBlacklist == null || graph.get(edge.node).track == null || !trackTypeIdBlacklist.contains(graph.get(edge.node).track.trackTypeId)))
				{
					nodeInfoLinks[edge.node].distance = currentNode.distance+weight;
					nodeInfoLinks[edge.node].parent = currentNode.baseIndex;
					heapPercolateUp(nodeInfoLinks[edge.node].heapIndex);
				}
			}
			
		}while(currentNode.distance!=Double.POSITIVE_INFINITY);
				
		return nodeInfoLinks[finishNode].distance;
	}
	
	/**
	 * Retrieves the parent of a particular node.
	 * @param node the node to check.
	 * @return the parent of requested node, or null if it has no parent.
	 */
	public int getParent(int node) {
		return nodeInfoLinks[node].parent;
	}

	/**
	 * Retrieves the distance of a particular node.
	 * @param node the node to check.
	 * @return the distance of this node from the starting node.
	 */
	public double getDistance(int node) {
		return nodeInfoLinks[node].distance;
	}
	
	public double calculateWeight(NodeInfo currentNode, GraphEdge edge)
	{		
		Track track1 = graph.get(currentNode.baseIndex).getTrack();
		Track track2 = graph.get(nodeInfoLinks[edge.node].baseIndex).getTrack();

		
		//WALK from start / to finish
		if(track1==null || track2==null)
		{
			return multiplierWalking * edge.weight;
		}
		// PINDAH ANGKOT
		else if(track1!=track2)
		{
			return multiplierWalking * (penaltyTransfer + edge.weight);
		}
		// MASIH DI ANGKOT
		else 
		if(track1.getTrackId().equals(track2.getTrackId()))
		{
			return edge.weight * track1.penalty;
		}
		
		return Double.POSITIVE_INFINITY;
	}
	
	void heapPercolateDown(int index)
	{
		int minIndex;
		boolean ok = true;
		
		while(ok)
		{
			minIndex = index;
			//kalau ada anak kiri DAN kalau distance anak kiri lebih kecil
			if((index*2+1<heapsize) && nodesMinHeap[index*2+1].distance < nodesMinHeap[minIndex].distance)
			{
				minIndex = index*2+1;
			}

			//kalau ada anak kanan DAN kalau distance anak kanan lebih kecil
			if((index*2+2<heapsize) && nodesMinHeap[index*2+2].distance < nodesMinHeap[minIndex].distance)
			{
				minIndex = index*2+2;
			}
			
			//kalau memang bukan yang paling kecil maka swap
			if(ok = (minIndex != index))
			{
				//swap!
				NodeInfo temp = nodesMinHeap[index];
				nodesMinHeap[index] = nodesMinHeap[minIndex];
				nodesMinHeap[minIndex] = temp;
				
				//swap the heap index
				int tmp = nodesMinHeap[index].heapIndex;
				nodesMinHeap[index].heapIndex = nodesMinHeap[minIndex].heapIndex;
				nodesMinHeap[minIndex].heapIndex = tmp;
				
				index = minIndex;
			}
		}
		
		
	}
	
	void heapPercolateUp(int index)
	{
		//selama belum yang paling atas DAN yang di atasnya lebih besar
		while((index-1)/2 >=0 && nodesMinHeap[(index-1)/2].distance > nodesMinHeap[index].distance)
		{
			//swap!
			NodeInfo temp = nodesMinHeap[index];
			nodesMinHeap[index] = nodesMinHeap[(index-1)/2];
			nodesMinHeap[(index-1)/2] = temp;
			
			//swap the heap index
			int tmp = nodesMinHeap[index].heapIndex;
			nodesMinHeap[index].heapIndex = nodesMinHeap[(index-1)/2].heapIndex;
			nodesMinHeap[(index-1)/2].heapIndex = tmp;
			
			index = (index-1)/2;
		}
	}
	
	NodeInfo heapDeleteMin()
	{
		if(heapsize==0)
		{
			return null;
		}
		
		NodeInfo ret = nodesMinHeap[0];
		
		nodesMinHeap[0] = nodesMinHeap[heapsize - 1];
		heapsize--;
		heapPercolateDown(0);
		
		return ret;
	}
	
	String getString(int node)
	{
		return nodeInfoLinks[node].toString();
	}
	
	
	private static class NodeInfo
	{
		int baseIndex;
		int heapIndex;
		double distance;
		int parent;
		
		public NodeInfo()
		{
			
		}
		
		public String toString()
		{
			String t="";
			
			t+="point="+baseIndex+" distance="+distance+ " parent="+parent;
			
			return t;
		}
		
	}
}
