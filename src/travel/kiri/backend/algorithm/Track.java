package travel.kiri.backend.algorithm;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single public transport track. In oldmenjangan: worker.cc/Track
 * @author PascalAlfadian
 *
 */
public class Track implements MemorySize{
	/**
	 * Type of track ("angkot", "transjakarta", etc...)
	 */
	String trackTypeId;
	
	/**
	 * The track id ("kalapaledeng", "cicaheumciroyom", etc...)
	 */
	String trackId;
	
	/**
	 * The track path, linking to the graph's {@link GraphNode}s. Array data
	 * structure is recommended to ensure O(1) selection.
	 */
	List<GraphNode> trackPath;
	
	/**
	 * Multiplier penalty for this track's edge cost (the higher the less likely
	 * to be selected).
	 */
	double penalty;
	
	/**
	 * Default constructor. Will initialize trackTypeId and trackId while leave the reset empty.
	 * @param fullyQualifiedTrackId The fully qualified track id, i.e. trackTypeId.trackId
	 */
	public Track(String fullyQualifiedTrackId) {
		// FIXME implement this!
		trackPath = new ArrayList<GraphNode>();
		
		String[] str = fullyQualifiedTrackId.split("\\.");
		trackTypeId = str[0];
		trackId = str [1];
	}
	
	public String getTrackId()
	{
		return trackId;
	}
	
	public String getTrackTypeId()
	{
		return trackTypeId;
	}
	
	public void setPenalty(double penalty)
	{
		this.penalty=penalty;
	}
	
	public double getPenalty()
	{
		return penalty;
	}
	
	public String toString()
	{
		String t =  trackTypeId+" . "+trackId+"\nNodes: "+trackPath.size()+"\n";
		
		//for(int i=0;i<trackPath.size();i++)
		{
			//t+=i+" "+trackPath.get(i)+"\n";
		}
		
		return t;
	}
	
	public void addNode(GraphNode node)
	{
		trackPath.add(node);
	}
	
	public GraphNode getNode(int idx)
	{
		return trackPath.get(idx);
	}

	@Override
	public int getMemorySize() {
		// TODO Auto-generated method stub
		return (trackId.length()+trackTypeId.length())*CHAR_SIZE + DOUBLE_SIZE;
	}
	
	public int getSize()
	{
		return trackPath.size();
	}
}
