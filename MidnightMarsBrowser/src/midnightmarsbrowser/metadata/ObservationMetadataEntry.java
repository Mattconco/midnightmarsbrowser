package midnightmarsbrowser.metadata;

public class ObservationMetadataEntry {

	public ObservationMetadataEntry(String obsID) {
		this.obsID = obsID;
	}
	
	public String obsID;
	public String description;
	
	public boolean equals(Object obj) {
		if ((obj == null) || (!(obj instanceof ObservationMetadataEntry)))
			return false;
		ObservationMetadataEntry entry = (ObservationMetadataEntry) obj;
 		return (obsID.equals(entry.obsID) 
 				&& description.equals(entry.description)
 				);
	}
}
