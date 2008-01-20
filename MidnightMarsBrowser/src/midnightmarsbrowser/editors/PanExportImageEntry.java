package midnightmarsbrowser.editors;

import midnightmarsbrowser.model.ImageEntry;

public class PanExportImageEntry {
	public ImageEntry imageListEntry;
	public double yaw;
	public double pitch;
	public double roll;
	public double fov;
	
	PanExportImageEntry(ImageEntry imageListEntry) {
		this.imageListEntry = imageListEntry;
	}
}
