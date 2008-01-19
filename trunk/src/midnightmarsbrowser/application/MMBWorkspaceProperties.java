package midnightmarsbrowser.application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.TimeZone;

import midnightmarsbrowser.model.MerUtils;

public class MMBWorkspaceProperties {
	public static final String IMG_DIR = "img";
	
	public static final String RAW_IMAGES = "rawImages";
	
	public static final String GENERATED_IMAGES = "generatedImages";
	
	public static final String METADATA = "metadata";
	
	public static final String PROPERTIES_FILENAME = "mmbworkspace.properties";
		
	private File workspaceDir;
	
	private File propertiesFile;
	
	private File imgDir;
	
	private File rawImageDir;
	
	private File generatedImageDir;
	
	private File metadataDir;
	
	private File metadataLocalDir;
	
	private File metadataDownloadedDir;
	
	private File metadataStageDir;
	
	private File screenshotDir;
	
	private Date explLastDownloadTime = null;
	
	private String lastPanExportDir = null;
	
	private boolean adjustAnaglyphRawImageBrightness = true;
	
	private boolean adjustFalseColorRawImageBrightness = true;

	private String[] defaultGeneratePancamLeftFalseColorTypes = new String[] { "L4L5L6","L4L5L7", 
			"L3L5L6", "L3L5L7", 
			"L2L5L6", "L2L5L7",
			"L2L4L6", "L2L4L7" };

	public String[] generatePancamLeftFalseColorTypes = defaultGeneratePancamLeftFalseColorTypes;

	public String[] generatePancamRightFalseColorTypes = new String[] {"R7R5R1",
			"R7R6R1", "R7R2R1", "R7R5R3", "R7R5R3"};// ?
	
	public String[] generatePancamBackupFalseColorTypes = new String[] { "L2L5L5" };

	private String[] defaultGeneratePancamAnaglyphTypes = new String[] { "L7R1", "L2R2" };
	
	public String[] generatePancamAnaglyphTypes = defaultGeneratePancamAnaglyphTypes;
	
	// has non-sundial, don't add: P213, P214
	private String[] defaultSundialCmdSeqNbrs = 
		new String[] {"P209","P210","P211","P212","P280","P281","P282","P283","P284","P285"};
	
	public String[] sundialCmdSeqNbrs = defaultSundialCmdSeqNbrs;
	
	public int generatePancamLeftFalseColorTypesMax = 1;

	public int generatePancamRightFalseColorTypesMax = 0;

	public int generatePancamAnaglyphTypesMax = 1;
	
	public int generatePancamBackupFalseColorTypesMax = 0;
	
	public double generateAnaglyphShiftPercentHazcam = 0.035; // changed from 0.075 2/25/05; // changed from 0.1 on 12/28/04

	public double generateAnaglyphShiftPercentNavcam = 0.06; // changed from 0.12 2/25/05

	public double generateAnaglyphShiftPercentPancam = 0;	// change from 0.05 2/25/05	
	
	public int generateAnaglyphTrimHPixels = 1;
	
	public int generateAnaglyphTrimHHazcam = 0;

	public int generateAnaglyphTrimHNavcam = 1;

	public int generateAnaglyphTrimHPancam = 1;
	
	public int generateFalseColorTrimHPixels = 1;
	
	
	public static Date getDefaultExplStartDate() {
		Calendar newCal = new GregorianCalendar(TimeZone.getTimeZone("PST"));
		newCal.add(Calendar.HOUR_OF_DAY, -24);
		newCal.set(Calendar.MINUTE, 0);
		newCal.set(Calendar.SECOND, 0);
		newCal.set(Calendar.MILLISECOND, 0);
		return newCal.getTime();
	}

	
	public MMBWorkspaceProperties(String pathString) {
		this.workspaceDir = new File(pathString);
		this.propertiesFile = new File(workspaceDir, PROPERTIES_FILENAME);
		this.imgDir = new File(workspaceDir, IMG_DIR);
		this.rawImageDir = new File(workspaceDir, RAW_IMAGES);
		this.generatedImageDir = new File(workspaceDir, GENERATED_IMAGES);
		this.metadataDir = new File(workspaceDir, METADATA);
		this.metadataLocalDir = new File(metadataDir, "local");
		this.metadataDownloadedDir = new File(metadataDir, "downloaded");
		this.metadataStageDir = new File(metadataDir, "stage");
		this.screenshotDir = new File(workspaceDir, "screenshots");
	}
	
	protected synchronized void readProperties() {
		Properties properties = new Properties();
		// first see if we have preferences file in correct location
		
		FileInputStream in = null;
		try {
			in = new FileInputStream(propertiesFile);
			properties.load(in);
			in.close();
		} catch (java.io.FileNotFoundException e) {
			in = null;
			System.out.println("Can't find properties file; using defaults.");
		} catch (java.io.IOException e) {
			System.out.println("Can't read properties file; using defaults.");
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (java.io.IOException e) {
				}
				in = null;
			}
		}

		String str;

		explLastDownloadTime = null;
		try {
			str = properties.getProperty("explLastDownloadTime");
			if (str != null) {
				explLastDownloadTime = MerUtils.getExplDateFormat().parse(str);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error reading explLastDownloadTime; using default.");
			explLastDownloadTime = getDefaultExplStartDate();			
		}			
		if (explLastDownloadTime == null) {
			System.out.println("explLastDownloadTime not found; using default.");
			explLastDownloadTime = getDefaultExplStartDate();
		}
		
		lastPanExportDir = properties.getProperty("lastPanExportDir");
	}
	
	private void checkWriteProperties() {
		// TODO later, have a batch property-edit mode;
		// maybe even have checkReadProperties() and re-read file as needed - (careful!)
		writeProperties();
	}
		
	public synchronized void writeProperties() {
		Properties props = new Properties();
		
		if (explLastDownloadTime != null) {
			try {
				String str = MerUtils.getExplDateFormat().format(explLastDownloadTime);
				props.setProperty("explLastDownloadTime", str);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}	
		
		if (lastPanExportDir != null) {
			props.setProperty("lastPanExportDir", lastPanExportDir);
		}
		
		FileOutputStream out = null;
		try {
			// write prefs file
			out = new FileOutputStream(propertiesFile);
			props.store(out, "---MidnightMarsBrowser workspace properties---");
		}
		catch (Exception e) {
			System.out.println("Could not write properties file: "+e.toString());
		}
		finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}		
	}
	
	public File getWorkspaceDir() {
		return this.workspaceDir;
	}

	public File getImgDir() {
		return imgDir;
	}
	
	public File getGeneratedImageDir() {
		return generatedImageDir;
	}

	public File getRawImageDir() {
		return rawImageDir;
	}
	
	public File getMetadataDir() {
		return metadataDir;
	}

	public File getMetadataDownloadedDir() {
		return metadataDownloadedDir;
	}


	public File getMetadataLocalDir() {
		return metadataLocalDir;
	}

	public File getMetadataStageDir() {
		return metadataStageDir;
	}
	
	public File getScreenshotDir() {
		return screenshotDir;
	}


	public Date getExplLastDownloadTime() {
		return explLastDownloadTime;
	}

	public void setExplLastDownloadTime(Date explLastDownloadTime) {
		this.explLastDownloadTime = explLastDownloadTime;
		checkWriteProperties();
	}

	public boolean isAdjustAnaglyphRawImageBrightness() {
		return adjustAnaglyphRawImageBrightness;
	}

	public boolean isAdjustFalseColorRawImageBrightness() {
		return adjustFalseColorRawImageBrightness;
	}


	public String getLastPanExportDir() {
		return lastPanExportDir;
	}

	public void setLastPanExportDir(String lastPanExportDir) {
		this.lastPanExportDir = lastPanExportDir;
		checkWriteProperties();
	}

}
