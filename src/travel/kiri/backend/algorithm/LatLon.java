package travel.kiri.backend.algorithm;

/**
 * Representing a latitude/longitude position. In oldmenjangan: none
 * @author PascalAlfadian
 *
 */
public class LatLon {
	public double latitude;
	public double longitude;
	
	/**
	 * Creates a new lat/lon instance
	 * @param lat latitude
	 * @param lon longitude
	 */
	public LatLon(double lat, double lon) {
		this.latitude = lat;
		this.longitude = lon;
	}
}
