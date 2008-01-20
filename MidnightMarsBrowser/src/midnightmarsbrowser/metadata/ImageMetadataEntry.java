package midnightmarsbrowser.metadata;

import midnightmarsbrowser.model.MerUtils;

public class ImageMetadataEntry implements Comparable {

	public ImageMetadataEntry(String filename) {
		this.filename = filename;
		this.roverCode = MerUtils.roverCodeFromFilename(filename);
		this.roverClock = MerUtils.roverClockIntegerFromFilename(filename);
	}
	
	public String filename;	// primary key
	public String roverCode;	// computed; alternate key
	public Integer roverClock;		// computed; alternate key
	public double inst_az_rover;
	public double inst_el_rover;		
	public int first_line;
	public int first_line_sample;
	public int n_lines;
	public int n_line_samples;
	public int pixel_averaging_height;
	public int pixel_averaging_width;	
	public int rmc_site = -1;
	public int rmc_drive = -1;
	public String obsID;	// key back into the Observation table
	
	public boolean equals(Object obj) {
		if ((obj == null) || (!(obj instanceof ImageMetadataEntry)))
			return false;
		ImageMetadataEntry entry = (ImageMetadataEntry) obj;
 		return ((filename.equals(entry.filename)) 
 				&& (roverCode.equals(entry.roverCode))
 				&& (roverClock.equals(entry.roverClock))
 				&& (inst_az_rover == entry.inst_az_rover)
 				&& (inst_el_rover == entry.inst_el_rover)
 				&& (first_line == entry.first_line)
 				&& (first_line_sample == entry.first_line_sample)
 				&& (n_lines == entry.n_lines)
 				&& (n_line_samples == entry.n_line_samples)
 				&& (pixel_averaging_height == entry.pixel_averaging_height)
 				&& (pixel_averaging_width == entry.pixel_averaging_width)
 				&& (rmc_site == entry.rmc_site)
 				&& (rmc_drive == entry.rmc_drive)
 				&& (obsID == null ? entry.obsID==null : obsID.equals(entry.obsID))
 				);
	}
	
	public int compareTo(Object arg0) {
		ImageMetadataEntry entry = (ImageMetadataEntry) arg0;
		return filename.compareTo(entry.filename);
	}
}
