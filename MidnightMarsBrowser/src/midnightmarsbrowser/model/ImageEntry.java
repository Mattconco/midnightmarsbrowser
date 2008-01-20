package midnightmarsbrowser.model;

import midnightmarsbrowser.metadata.ImageMetadataEntry;

public class ImageEntry implements Comparable {
	TimeInterval parent = null;
	
	int sol;
	int roverClock;
	String solString;
	String filename;
	// TODO make this a File object, or make ImageIndex deal with strings exclusive... one or the other
	String file;
	String imageCategory;
	boolean isIMG;
	int imageClass;
	LocationCounter location;
	ImageMetadataEntry imageMetadataEntry;
	public boolean enabled = true;
	public String observationDescription;
	
	static int CLASS_UNKNOWN = 0;
	static int CLASS_RAWJPG = 1;
	static int CLASS_PDS_UNKNOWN = 2;
	static int CLASS_PDS_EDR = 3;
	static int CLASS_PDS_MRD = 4;
	static int CLASS_PDS_RAD = 5;
	static int CLASS_MMB_ANAGLYPH = 6;
	static int CLASS_MMB_FALSECOLOR = 7;
	static int CLASS_DCCOLOR = 8;
	
	ImageEntry(String filename, String file, String imageCategory) {
		this.filename = filename;
		this.file = file;
		this.imageCategory = imageCategory;
		this.isIMG = filename.endsWith(".IMG");
		String productType = MerUtils.productTypeFromFilename(filename);
		if (filename.endsWith(".IMG")) {
			if (productType.equals("RAD")) {
				imageClass = CLASS_PDS_RAD;
			}
			else if (productType.equals("MRD")) {
				imageClass = CLASS_PDS_MRD;
			}
			else {
				imageClass = CLASS_PDS_EDR;
			}
		}
		else {
			if (imageCategory.length() == 1) {
				imageClass = CLASS_RAWJPG;
			}
			else if (imageCategory.equals(ImageIndex.CATEGORY_PC)) {
				imageClass = CLASS_MMB_FALSECOLOR;
			}
			else if (imageCategory.equals(ImageIndex.CATEGORY_PS)) {
				imageClass = CLASS_DCCOLOR;
			}
			else {
				imageClass = CLASS_MMB_ANAGLYPH;
			}
		}
	}
	
	public TimeInterval getParent() {
		return parent;
	}
	
	public String getFile() {
		return file;
	}

	public String getFilename() {
		return filename;
	}
	
	public boolean isIMG() {
		return isIMG;
	}

	public int getRoverClock() {
		return roverClock;
	}

	public int getSol() {
		return sol;
	}

	public String getSolString() {
		return solString;
	}
		
	public String getImageCategory() {
		return imageCategory;
	}

	public LocationCounter getLocation() {
		return location;
	}
	
	public ImageMetadataEntry getImageMetadataEntry() {
		return imageMetadataEntry;
	}

	public int compareTo(Object o) {
		int val = this.roverClock - ((ImageEntry)o).roverClock;
		if (val != 0) {
			return val;
		}
		val = this.imageCategory.compareTo(((ImageEntry)o).imageCategory);
		if (val != 0) {
			return val;
		}
		val = this.filename.compareTo(((ImageEntry)o).filename);	
		return val;
	}
	
	
}
