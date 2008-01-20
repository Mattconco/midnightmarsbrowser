package midnightmarsbrowser.editors;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import midnightmarsbrowser.model.ImageEntry;
import midnightmarsbrowser.model.MerUtils;

class PanImageEntry {
	ImageEntry imageListEntry;
	float projectedZ;
	boolean loaded;
	char camera;
	int	textureNumber = -1;
	boolean draw = true;
//	ByteBuffer loadImageBuffer = null;
//	int loadImageWidth = 0;
//	int loadImageHeight = 0;
	ArrayList loadImageBuffer = null;
	ArrayList loadImageWidth = null;
	ArrayList loadImageHeight = null;
	
	PanImageEntry(ImageEntry imageListEntry) {
		this.imageListEntry = imageListEntry;
		this.camera = MerUtils.cameraFromFilename(imageListEntry.getFilename());
	}

}
