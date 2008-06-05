package midnightmarsbrowser.application;

import java.io.File;
import java.util.Date;

import midnightmarsbrowser.editors.PanExportImageEntry;
import midnightmarsbrowser.model.MerUtils;

public class UpdateParams {
	public static final int MODE_CHECK = 0;
	public static final int MODE_UPDATE = 1;
	public static final int MODE_EXPORT = 2;
	public static final int MODE_IMPORT = 3;
	public static final int MODE_DELETE = 4;
	public static final int MODE_EXPORT_MOVIE = 5;
	public static final int MODE_DEVELOPMENT1 = 6;
	public static final int MODE_DEVELOPMENT2 = 7;
	public static final int MODE_BUILD_METADATA = 8;
	public static final int MODE_BUILD_METADATA_1X = 9;
	public static final int MODE_EXPORT_PAN_IMAGES = 10;
	public static final int MODE_UPDATE_PHOENIX_JPG = 11;
 	
	public static final int DOWNLOAD_NONE = 0;
	public static final int DOWNLOAD_FROM_JPL = 1;
	public static final int DOWNLOAD_LATEST_FROM_JPL = 2;
	public static final int DOWNLOAD_FROM_EXPL = 3;
	public static final int DOWNLOAD_FROM_LYLE = 4;
	public static final int DOWNLOAD_FROM_PDS_IMG = 5;

	public int mode = MODE_UPDATE;
	public int downloadMode = DOWNLOAD_NONE;
	public boolean autoUpdate = false;
	public boolean checkExpl = true;
	public boolean checkJPL = true;
	public boolean updateOpportunity = true;
	public boolean updateSpirit = true;
	
	public Date downloadStartExplDate = null;
	public boolean downloadEndExplDateFlag = false;
	public Date downloadEndExplDate = null;
	public boolean updateDownloadStartDate = true;
	public boolean updateF = true;
	public boolean updateR = true;
	public boolean updateN = true;
	public boolean updateP = true;
	public boolean updateM = true;
	public boolean fastUpdate = false;
	public boolean generateFA = true;	//MyPreferences.instance().generateFA;
	public boolean generateRA = true; 	//MyPreferences.instance().generateRA;
	public boolean generateNA = true;	//MyPreferences.instance().generateNA;
	public boolean generatePA = true;	//MyPreferences.instance().generatePA;
	public boolean generatePC = true;	//MyPreferences.instance().generatePC;
	public boolean generatePS = true;	// ps isn't really generated, but it can be exported/imported
	//public boolean generateRawImageThumbnails = false;
	public boolean generateImagesAll = false;
	public boolean generateImagesCheckAll = false;
	//public boolean generateThumbnailsOnly = false;	// this is a 'cheat' for regenerating thumbnails fast for pan color editing
	public int startSol = 1;
	public int endSol = 9999;
	public boolean updateMetadata = false;
	
	public String[] productTypes = null;
	public String[] filters = null;

	public boolean forceGenerateOppy = false;
	public boolean forceGenerateSpirit = false;
	
	// import/export/etc parameters
	public PanExportImageEntry[] panExportImageList;
	public File tgtDir = null;
	// movie generation parameters
	public boolean cleanMovieDir = false;
	public boolean showSolNum = false;
	public boolean movieFrameResize = false;
	public int movieFrameSizeH = 0;
	public int movieFrameSizeV = 0;
	public boolean applyImageAdjustments = true;
	public boolean imgMetadataOnly = false;
	public String productCode = null;
	
	void setUpdateImages(String roverCode, boolean val) {
		if (roverCode.equals("1")) {
			updateOpportunity = val;
		}
		else if (roverCode.equals("2")) {
			updateSpirit = val;
		}
	}
	
	public UpdateParams() {
		downloadStartExplDate = MerUtils.explHour(Application.getWorkspace().getExplLastDownloadTime());
	}
}
