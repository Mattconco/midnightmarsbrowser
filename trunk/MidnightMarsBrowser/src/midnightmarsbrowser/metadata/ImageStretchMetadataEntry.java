package midnightmarsbrowser.metadata;

public class ImageStretchMetadataEntry {
	/**
	 * filename is the complete filename (including .JPG) since 
	 * this destretching info is only associated with the stretched JPG images
	 */
	String filename;
	public int minVal = 0;
	public int maxVal = 256;
    public int devigA = 0;
    public int devigB = 0;
	
	public ImageStretchMetadataEntry(String filename) {
		this.filename = filename;
	}

	public ImageStretchMetadataEntry(int minVal, int maxVal, int devigA, int devigB) {
		this.minVal = minVal;
		this.maxVal = maxVal;
        this.devigA = devigA;
        this.devigB = devigB;
	}
	
	public boolean equals(Object obj) {
		if ((obj == null) || (!(obj instanceof ImageStretchMetadataEntry)))
			return false;
		ImageStretchMetadataEntry entry = (ImageStretchMetadataEntry) obj;
 		return ((filename.equals(entry.filename)) 
 				&& (minVal == entry.minVal)
 				&& (maxVal == entry.maxVal)
 				&& (devigA == entry.devigA)
 				&& (devigB == entry.devigB)
 				);
	}
}
