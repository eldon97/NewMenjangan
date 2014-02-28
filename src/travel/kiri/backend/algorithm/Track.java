package travel.kiri.backend.algorithm;

import java.util.List;

/**
 * Represents a single public transport track. In oldmenjangan: worker.cc/Track
 * @author PascalAlfadian
 *
 */
public class Track {
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
	 * @param fullyQualifiedTrackId The fully qualified track id, i.e. trackTypeId.trackTypeId
	 */
	public Track(String fullyQualifiedTrackId) {
		// FIXME implement this!
	}
}
