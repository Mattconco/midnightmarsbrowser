package midnightmarsbrowser.metadata;

public class SiteMetadataEntry implements Comparable {
	String spacecraftId;	// (index)
	public Integer rmc_site = null;
	// the site origin in landing frame coordinates
	public double offset_vector_a;
	public double offset_vector_b;
	public double offset_vector_c;
		
	SiteMetadataEntry(String spacecraftId, int rmc_site) {		
		this.spacecraftId = spacecraftId;
		this.rmc_site = new Integer(rmc_site);
	}
	
	public boolean equals(Object obj) {
		return (this.compareTo(obj) == 0);
	}

	public int compareTo(Object arg0) {
		SiteMetadataEntry that = (SiteMetadataEntry) arg0;		
		return this.rmc_site.intValue() - that.rmc_site.intValue();
	}
	
	/**
	 * Compare all the fields of the entry, as opposed to just those relevant to sorting,
	 * so compareTo can be consistent with equals.
	 * @param obj
	 * @return
	 */
	public boolean extendedEquals(Object obj) {
		if ((obj == null) || (!(obj instanceof SiteMetadataEntry)))
			return false;
		SiteMetadataEntry entry = (SiteMetadataEntry) obj;
 		return ((spacecraftId.equals(entry.spacecraftId)) 
 				&& (rmc_site.intValue() == entry.rmc_site.intValue())
 				&& (offset_vector_a == entry.offset_vector_a)
 				&& (offset_vector_b == entry.offset_vector_b)
 				&& (offset_vector_c == entry.offset_vector_c) 
 				);
	}	
}
