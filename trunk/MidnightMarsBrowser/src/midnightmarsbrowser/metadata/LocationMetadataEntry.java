package midnightmarsbrowser.metadata;

import midnightmarsbrowser.model.LocationCounter;

public class LocationMetadataEntry implements Comparable {
	String roverCode;	// (index)
	public LocationCounter location; // (index)
	int	startSol;
	int endSol;
	public double rover_origin_rotation_quaternian_a;
	public double rover_origin_rotation_quaternian_b;
	public double rover_origin_rotation_quaternian_c;
	public double rover_origin_rotation_quaternian_d;
	public boolean has_site_rover_origin_offset_vector = false;
	public double rover_origin_offset_vector_a;
	public double rover_origin_offset_vector_b;
	public double rover_origin_offset_vector_c;
		
	public LocationMetadataEntry(String roverCode, int rmc_site, int rmc_drive) {
		this.roverCode = roverCode;
		//String siteDrive = MerUtils.zeroPad(site, 2) + MerUtils.zeroPad(drive, 2);
		this.location = new LocationCounter(rmc_site, rmc_drive);
		this.location.site = rmc_site;
		this.location.drive = rmc_drive;
	}
	
	/**
	 * Compare all the fields of the entry, as opposed to just those relevant to sorting,
	 * so compareTo can be consistent with equals.
	 * @param obj
	 * @return
	 */
	public boolean extendedEquals(Object obj) {
		if ((obj == null) || (!(obj instanceof LocationMetadataEntry)))
			return false;
		LocationMetadataEntry entry = (LocationMetadataEntry) obj;
 		return ((roverCode.equals(entry.roverCode)) 
 				&& (location.equals(entry.location)) 				
 				&& (startSol == entry.startSol)
 				&& (endSol == entry.endSol)
 				&& (rover_origin_rotation_quaternian_a == entry.rover_origin_rotation_quaternian_a)
 				&& (rover_origin_rotation_quaternian_b == entry.rover_origin_rotation_quaternian_b)
 				&& (rover_origin_rotation_quaternian_c == entry.rover_origin_rotation_quaternian_c)
 				&& (rover_origin_rotation_quaternian_d == entry.rover_origin_rotation_quaternian_d)
 				&& (has_site_rover_origin_offset_vector == entry.has_site_rover_origin_offset_vector)
 				&& (rover_origin_offset_vector_a == entry.rover_origin_offset_vector_a)
 				&& (rover_origin_offset_vector_b == entry.rover_origin_offset_vector_b)
 				&& (rover_origin_offset_vector_c == entry.rover_origin_offset_vector_c) 
 				);
	}
	
	public boolean equals(Object obj) {
		return (this.compareTo(obj) == 0);
	}

	public int compareTo(Object arg0) {
		LocationMetadataEntry entry = (LocationMetadataEntry) arg0;
		return location.compareTo(entry.location);
	}	
}
