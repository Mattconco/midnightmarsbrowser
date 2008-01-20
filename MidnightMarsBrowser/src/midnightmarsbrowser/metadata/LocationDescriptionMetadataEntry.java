package midnightmarsbrowser.metadata;

import midnightmarsbrowser.model.MerUtils;

public class LocationDescriptionMetadataEntry {
	String roverCode;	// (index)
	String site;
	String drive;
	String siteDrive;	// (index)
	public String description;
	
	LocationDescriptionMetadataEntry(String roverCode, String site, String drive) {
		this.roverCode = roverCode;
		this.site = site;
		this.drive = drive;
		this.siteDrive = MerUtils.zeroPad(site, 2) + MerUtils.zeroPad(drive, 2);;		
	}
	
	public boolean equals(Object obj) {
		if ((obj == null) || (!(obj instanceof LocationDescriptionMetadataEntry)))
			return false;
		LocationDescriptionMetadataEntry entry = (LocationDescriptionMetadataEntry) obj;
 		return ((roverCode.equals(entry.roverCode)) 
 				&& (site.equals(entry.site))
 				&& (drive.equals(entry.drive))
 				&& (siteDrive.equals(entry.siteDrive))
 				&& (description.equals(entry.description))
 				);
	}
}
