/**
 *  Midnight Mars Browser - http://midnightmarsbrowser.blogspot.com
 *  Copyright (c) 2005-2007 by Michael R. Howard
 *
 *  Midnight Mars Browser is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 * 
 *  Midnight Mars Browser is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Midnight Mars Browser; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 */

package midnightmarsbrowser.application;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.NoRouteToHostException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Vector;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import midnightmarsbrowser.application.UpdateParams;
import midnightmarsbrowser.editors.MMBEditorInput;
import midnightmarsbrowser.editors.PanExportImageEntry;
import midnightmarsbrowser.editors.UpdateViewerEditor;
import midnightmarsbrowser.editors.ViewerEditor;
import midnightmarsbrowser.metadata.ImageMetadata;
import midnightmarsbrowser.metadata.ImageMetadataEntry;
import midnightmarsbrowser.metadata.ImageStretchMetadata;
import midnightmarsbrowser.metadata.ImageStretchMetadataEntry;
import midnightmarsbrowser.metadata.LocationDescriptionMetadata;
import midnightmarsbrowser.metadata.LocationMetadata;
import midnightmarsbrowser.metadata.MetadataSlicerDicer;
import midnightmarsbrowser.metadata.ObservationMetadata;
import midnightmarsbrowser.model.ImageEntry;
import midnightmarsbrowser.model.MerUtils;
import midnightmarsbrowser.model.ViewerSettings;
import midnightmarsbrowser.util.PDSIMG;

public class UpdateTask implements Runnable, IUpdateTask {

	private static final String baseJplUrl = "http://marsrovers.jpl.nasa.gov/gallery/all/";

	private static final String baseJplCachePathString = "marsrovers.jpl.nasa.gov"
			+ File.separator + "gallery" + File.separator + "all";

	private static final String baseExplUrl = "http://nasa.exploratorium.edu/mars/";

	private static final String lyleIndexUrl = "http://www.lyle.org/~markoff/index.mmb";

	private static final String lyleCacheIndexPathString = "www.lyle.org"
			+ File.separator + "~markoff" + File.separator + "index.mmb";

	private static final String baseLyleUrl = "http://www.lyle.org/~markoff/mmb/";

	private static final String mmbHomePage = "http://midnightmarsbrowser.blogspot.com";

	private static final int maxEmptySolsForJplDownload = 30;

	private static final long minCheckTimeMillis = 60000L * 5; // min x minutes

	// between
	// metadataa
	// checks

	MMBWorkspace workspace;
	
	UpdateParams params;

	IWorkbenchWindow window;
	

	static final int updateThreadSleepTime = 100;

	private boolean terminate;

	private Date latestDownloadedExplTime = null;	
	
	private HashSet updatedRawImageFilenames = null;

	private HashSet updatedRawImageSols = null;

	private HashSet updatedGeneratedImageFilenames = null;

	private HashSet updatedGeneratedImageSols = null;
	
	private HashSet updatedMetadataImageFilenames = new HashSet();

	private HashSet updatedMetadataImageSols = new HashSet();	
	
	boolean running = false;
	
	boolean completed = false;
	
	boolean updateViewerOpened = false;
	
	int numImagesDownloaded = 0;

	int numImagesCopied = 0;

	int numImagesSkipped = 0;

	int numImagesDeleted = 0;

	int totalNumImagesCopied = 0;

	int totalNumImagesSkipped = 0;

	int totalNumImagesDeleted = 0;

	int numAnaglyphsGenerated = 0;

	int numFalseColorGenerated = 0;

	int numErrors = 0;

	int numWarnings = 0;

	int numNewImages = 0;

	int numNewImagesExpl = 0;

	int numNewImagesJPL = 0;	
	
	public UpdateTask(UpdateParams params, MMBWorkspace workspace, IWorkbenchWindow window) {		
		this.params = params;
		this.workspace = workspace;
		this.window = window;
	}

	public void run() {
		running = true;
		
//		Update.updateError = false;
//		MyPreferences prefs = MyPreferences.instance();
//		MyPreferences.instance().autoUpdateLastTimeMillis = System.currentTimeMillis();
//		MyPreferences.write();
//		attachViewFrame();
//		if (!params.autoUpdate) {
//			LogFrame.getInstance().show();
//		}

		try {
			if (params.mode == UpdateParams.MODE_CHECK) {
				logln("Checking for new images "+new Date().toString());
				numNewImagesExpl = 0;
				numNewImagesJPL = 0;
				if (params.autoUpdate) {
					// find from the check if we actually want to update, individually
					params.updateOpportunity = false;
					params.updateSpirit = false;
				}
				if (params.checkExpl) {
					try {
						doCheckExploratorium("1");
						doCheckExploratorium("2");
					}
					catch (UpdateInterrupt e) {
						throw e;
					}
					catch (Throwable e) {
						e.printStackTrace();
						logln("ERROR checking Exploratorium: "+e.toString());
						numErrors++;
						//StringWriter writer = new StringWriter();
						//e.printStackTrace(new PrintWriter(writer));
						//LogFrame.getInstance().logln(writer.toString());
					}
				}
				if ((numNewImagesExpl == 0) && (params.checkJPL)) {
					try {
						doCheckJpl("1");
						doCheckJpl("2");
					}
					catch (UpdateInterrupt e) {
						throw e;
					}
					catch (Throwable e) {
						logln("ERROR checking JPL: "+e.toString());
						numErrors++;
						//StringWriter writer = new StringWriter();
						//e.printStackTrace(new PrintWriter(writer));
						//LogFrame.getInstance().logln(writer.toString());
					}
				}
				boolean newMetadata = false;
				try {
					newMetadata = checkMetadata();
				}
				catch (UpdateInterrupt e) {
					throw e;
				}
				catch (Throwable e) {
					logln("ERROR checking for metadata: "+e.toString());
					numErrors++;
					StringWriter writer = new StringWriter();
					e.printStackTrace(new PrintWriter(writer));
					logln(writer.toString());
				}
//				MyPreferences.instance().lastMetadataCheckTimeMillis = System.currentTimeMillis();
//				MyPreferences.write();
				logln("Check complete "+new Date().toString());
				params.downloadMode = UpdateParams.DOWNLOAD_NONE;
				params.updateMetadata = false;
				if (numNewImagesExpl > 0) {
					params.mode = UpdateParams.MODE_UPDATE;
					params.downloadMode = UpdateParams.DOWNLOAD_FROM_EXPL;
					logln("There are "+numNewImagesExpl+" new images at Exploratorium.");
				}
				else if (numNewImagesJPL > 0) {
					params.mode = UpdateParams.MODE_UPDATE;
					params.downloadMode = UpdateParams.DOWNLOAD_LATEST_FROM_JPL;
					logln("There are "+numNewImagesJPL+" new images at JPL.");
				}
				else {
					logln("There are no new images.");
				}
				if (newMetadata) {
					params.mode = UpdateParams.MODE_UPDATE;
					params.updateMetadata = true;
					logln("There is new metadata at "+mmbHomePage+".");
				}
				
				if ((!params.autoUpdate) && (params.mode == UpdateParams.MODE_UPDATE)) {
					if (params.downloadMode != UpdateParams.DOWNLOAD_NONE) {
						Runnable runnable = new Runnable() {
							public void run() {
								boolean okay = MessageDialog.openConfirm(window.getShell(), "New Images Available", "New images are available. Would you like to download them now?");
								if (!okay) {
									// do not update
									params.mode = UpdateParams.MODE_CHECK;	
								}
							}
						};
						window.getShell().getDisplay().syncExec(runnable);						
					}
					else if (params.updateMetadata) {
						Runnable runnable = new Runnable() {
							public void run() {
								boolean okay = MessageDialog.openConfirm(window.getShell(), "New Metadata Available", "There is new metadata available. Would you like to download it now?");
								if (!okay) {
									// do not update
									params.mode = UpdateParams.MODE_CHECK;	
								}
							}
						};
						window.getShell().getDisplay().syncExec(runnable);						
					}
				}				
			}
			
			if (params.mode == UpdateParams.MODE_UPDATE) {
				if (params.updateMetadata) {
					updateMetadata();
				}

				logln("Beginning image update "+new Date().toString());
				boolean newOppyImages = false;
				boolean newSpiritImages = false;
				if (params.updateOpportunity || params.forceGenerateOppy) {
					doStartUpdate();
				}
				if (params.updateOpportunity) {
					doUpdate("1");
				}
				if (params.updateOpportunity || params.forceGenerateOppy) {
					doGenerate("1");
					HashSet newSet = new HashSet();
					newSet.addAll(updatedRawImageFilenames);
					newSet.addAll(updatedGeneratedImageFilenames);
					if (!newSet.isEmpty()) {
						workspace.setMostRecentUpdateFilenames(MerUtils.ROVERCODE_OPPORTUNITY, newSet);
						newOppyImages = true;
					}
				}
				if (params.updateSpirit || params.forceGenerateSpirit) {
					doStartUpdate();
				}
				if (params.updateSpirit) {
					doUpdate("2");
				}
				if (params.updateSpirit || params.forceGenerateSpirit) {
					doGenerate("2");
					HashSet newSet = new HashSet();
					newSet.addAll(updatedRawImageFilenames);
					newSet.addAll(updatedGeneratedImageFilenames);
					if (!newSet.isEmpty()) {
						workspace.setMostRecentUpdateFilenames(MerUtils.ROVERCODE_SPIRIT, newSet);
						newSpiritImages = true;
					}
				}
				if (params.updateDownloadStartDate) {
					if (latestDownloadedExplTime != null) {
						workspace.setExplLastDownloadTime(MerUtils.explHour(latestDownloadedExplTime));
					}
				}
				window.getShell().getDisplay().syncExec(new Runnable() {
					public void run() {
						UpdateViewerEditor.closeUpdateViewer();
					}});
				
				if (newSpiritImages) {
					logln("Creating slideshow of new Spirit images");
					window.getShell().getDisplay().asyncExec(new Runnable() {
						public void run() {
							try {
								ViewerSettings viewerSettings = new ViewerSettings(MerUtils.ROVERCODE_SPIRIT);
								viewerSettings.limitToSet = true;
								//viewerSettings.anaglyph = false;	// the anaglyphs just irritate me in the update slideshows, to be honest
								IWorkbenchPage page = window.getActivePage();
								MMBEditorInput input = new MMBEditorInput(viewerSettings);
								page.openEditor(input, ViewerEditor.ID);
							} catch (PartInitException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					});
				}
				if (newOppyImages) {
					logln("Creating slideshow of new Opportunity images");
					window.getShell().getDisplay().asyncExec(new Runnable() {
						public void run() {
							try {
								ViewerSettings viewerSettings = new ViewerSettings(MerUtils.ROVERCODE_OPPORTUNITY);
								viewerSettings.limitToSet = true;
								//viewerSettings.anaglyph = false;	// the anaglyphs just irritate me in the update slideshows, to be honest
								IWorkbenchPage page = window.getActivePage();
								MMBEditorInput input = new MMBEditorInput(viewerSettings);
								page.openEditor(input, ViewerEditor.ID);
							} catch (PartInitException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}							
						}
					});
				}
			}
			/*
			if (params.mode == UpdateParams.MODE_DEVELOPMENT1) {
				doSyncImg();
			}
			if (params.mode == UpdateParams.MODE_DEVELOPMENT2) {
				// doGenPanFiles();
				newGenPanFiles();
			}
			if (params.mode == UpdateParams.MODE_IMPORT) {
				doImport();
			}
			else if (params.mode == UpdateParams.MODE_EXPORT) {
				doExport();
			}
			else if (params.mode == UpdateParams.MODE_DELETE) {
				doDelete();
			}
			else if (params.mode == UpdateParams.MODE_EXPORT_MOVIE) {
				doExportMovieFrames();
			}
*/			
			else if (params.mode == UpdateParams.MODE_EXPORT_PAN_IMAGES) {
				doExportPanImagesForPTGui();
			}
			else if (params.mode == UpdateParams.MODE_BUILD_METADATA) {
				MetadataSlicerDicer builder = new MetadataSlicerDicer(workspace, this);
				builder.sliceAndDice();
			}
			else if (params.mode == UpdateParams.MODE_BUILD_METADATA_1X) {
				MetadataSlicerDicer builder = new MetadataSlicerDicer(workspace, this);
				builder.sliceAndDice1x();
			}
			else if (params.mode == UpdateParams.MODE_UPDATE_PHOENIX_JPG) {
				doUpdatePhoenixImages();
			}
			
			completed = true;
		} catch (UnknownHostException e) {
			logln(e.toString());
			logln("Could not connect to web: UnknownHostException");
//			if (!params.autoUpdate) {
//				JOptionPane.showMessageDialog(MainFrame.getInstance().mainFrame,
//						"Could not connect to web: UnknownHostException", "Update Error",
//						JOptionPane.ERROR_MESSAGE);
//			}
//			Update.updateError = true;
		} catch (NoRouteToHostException e) {
			logln(e.toString());
			logln("Could not connect to web: NoRouteToHostException");
//			if (!params.autoUpdate) {
//				JOptionPane.showMessageDialog(MainFrame.getInstance().mainFrame,
//						"Could not connect to web: NoRouteToHostException", "Update Error",
//						JOptionPane.ERROR_MESSAGE);
//			}
//			Update.updateError = true;
		} catch (UpdateInterrupt e) {
			logln("\nUpdate terminated "+new Date().toString());
		} catch (Throwable e) {
			e.printStackTrace();
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			logln(writer.toString());
//			if (!params.autoUpdate) {
//				JOptionPane.showMessageDialog(MainFrame.getInstance().mainFrame, e.toString(), "Update Error",
//						JOptionPane.ERROR_MESSAGE);
//			}
//			Update.updateError = true;
		}
		finally {
			try {
//				detachViewFrame();
				if (workspace.getImageIndex().isDirty()) {
					log("Writing image indexes... ");				
					workspace.getImageIndex().writeImageIndexes();
				}
//				ImageUtils.shutdown();
				logln("Done.");
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

//		Update.updateInstance = null;
//		updateThread = null;
		running = false;
		
		if (completed) {
			if (params.mode == UpdateParams.MODE_UPDATE) {
				logln("Update complete "+new Date().toString());
				if (params.downloadMode != UpdateParams.DOWNLOAD_LATEST_FROM_JPL) {
					log(" " + this.numImagesDownloaded
							+ " images downloaded ");
				}
//				if (params.generateFA || params.generateRA || params.generateNA || params.generatePA)  {
//					LogFrame.getInstance().log(" " + this.numAnaglyphsGenerated
//							+ " anaglyphs generated ");
//				}
//				if (params.generatePC) {
//					LogFrame.getInstance().log(" " + this.numFalseColorGenerated
//							+ " false-color generated ");
//				}
				log(" " + this.numErrors + " errors ");
				logln(".");
			}
		}
		logln("\n");		
	}
	
	public void stop() {
		terminate = true;
	}
	
	private void updateMetadata() throws IOException {
		logln("Updating metadata.");
		
		File downloadDir = workspace.getMetadataDownloadedDir();
		if (!downloadDir.isDirectory()) {
			logln("Creating directory "+downloadDir.getAbsolutePath());
			downloadDir.mkdirs();
		}
		
		File homepageFile = new File(workspace.getMetadataDownloadedDir(), "homepage.htm");
		boolean newMetadata = false;		
		try {
			log("Downloading "+mmbHomePage+"...");
			URL homepageUrl = new URL(mmbHomePage);
			StringBuffer buf = new StringBuffer();
			readTextFileFromURL(homepageUrl.toString(), buf);
			// save file
			// updateFileFromURLNoModTime no longer works because Blogspot changed
			//updatedHomepage = updateFileFromURLNoModTime(homepageFile, homepageUrl, false, -1, -1);
			try {
				debug("saving... ");
				FileWriter fileOut = new FileWriter(homepageFile);
				fileOut.write(buf.toString());
				fileOut.flush();
				fileOut.close();
				log("downloaded. ");
			}
			catch (Exception e) {
				numErrors++;
				e.printStackTrace();
				throw e;
			}
			logln();
		}
		catch (Exception e) {
			String msg = e.getMessage();
			if (msg == null)
				msg = e.toString();
			logln(msg);
		}
		
		// update homepage
		//boolean updatedHomepage = false;
		// Read local homepage file
		String metadataUrlString = "http://homepage.mac.com/michaelhoward/MidnightMarsBrowser/metadata";
		String latestMMBVersion = null;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(homepageFile));
			String inputLine;
			while ((inputLine = reader.readLine()) != null) {
				metadataUrlString = readMetadataParamFromLine(inputLine, "MMBMetaData", metadataUrlString);
				latestMMBVersion = readMetadataParamFromLine(inputLine, "MMBLatestVersion", latestMMBVersion);
			}
			if (metadataUrlString == null) {
				logln("Error reading "+homepageFile+": did not find expected tag.");
				// continue on with default
			}
		}
		catch (Exception e) {
			String msg = e.getMessage();
			if (msg == null)
				msg = e.toString();
			logln("Error reading local homepage file: "+msg);
			// continue on with default hard-coded metadata address
		}
		finally {
			if (reader != null) {
				reader.close();
				reader = null;
			}
		}

		if (!metadataUrlString.endsWith("/")) {
			metadataUrlString += "/";
		}
		
		newMetadata = updateMetadata(metadataUrlString, ImageMetadata.NAME, newMetadata);
		newMetadata = updateMetadata(metadataUrlString, ImageStretchMetadata.NAME, newMetadata);
		newMetadata = updateMetadata(metadataUrlString, LocationMetadata.NAME, newMetadata);
		newMetadata = updateMetadata(metadataUrlString, LocationDescriptionMetadata.NAME, newMetadata);
		newMetadata = updateMetadata(metadataUrlString, ObservationMetadata.NAME, newMetadata);
	
		if (newMetadata) {
			logln("Loading new metadata.");
			Runnable runnable = new Runnable() {
				public void run() {
					try {
						workspace.readMetadata();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};
			window.getShell().getDisplay().syncExec(runnable);
		}
		logln("Done updating metadata.");
	}
	
	private boolean updateMetadata(String metadataUrlString, String filename, boolean newMetadata) {	
		newMetadata = updateMetadataFile(metadataUrlString, filename+"_archive.csv", newMetadata);
		newMetadata = updateMetadataFile(metadataUrlString, filename+"_update.csv", newMetadata);
		return newMetadata;
	}
		
	private boolean updateMetadataFile(String metadataUrlString, String filename, boolean newMetadata) {
		File localPackageFile = new File(workspace.getMetadataDownloadedDir(), filename);		
		HttpURLConnection connection = null;
		BufferedReader reader = null;
//		File currentFile = null;
//		long currentLastModTime = -1;
//		boolean currentFileNew = false;
//		BufferedWriter currentWriter = null;
		BufferedWriter localPackageWriter = null;
		try {
			log("Updating metadata file "+filename+"... ");
			URL url = new URL(metadataUrlString+filename);

			connection = (HttpURLConnection) url.openConnection();
			connection.connect();
			long remotePackageLastModified = connection.getLastModified();
			if (remotePackageLastModified <= 0) {
				logln("file not found.");
				return newMetadata;
			}
			
			if ((localPackageFile.exists()) && 
					(localPackageFile.lastModified()/1000 == remotePackageLastModified/1000)) {
				logln("same last modified time.");
			}
			else {
				log("downloading new version...");
				int lineCount = 0;
				newMetadata = true;
				reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				localPackageWriter = new BufferedWriter(new FileWriter(localPackageFile));
				String inputLine;
				while ((inputLine = reader.readLine()) != null) {					
					checkForTerminate();
					localPackageWriter.write(inputLine);
					localPackageWriter.write("\n");
					lineCount++;
					if ((lineCount % 1000) == 0) {
						log(".");
					}
				}
				if (localPackageWriter != null) {
					localPackageWriter.flush();
					localPackageWriter.close();
					localPackageWriter = null;
					localPackageFile.setLastModified(remotePackageLastModified);
				}
				logln();
			}
		}
		catch (UpdateInterrupt e) {
			// TODO would be smart to do a safe download method
			try {
				if (localPackageWriter != null) {
					localPackageWriter.flush();
					localPackageWriter.close();
					localPackageWriter = null;
				}
				if ((localPackageFile != null) && (localPackageFile.exists())) {
					localPackageFile.delete();
				}				
			}
			catch (Exception e2) {
				e2.printStackTrace();
			}
			throw e;
		}
		catch (Throwable e) {
			e.printStackTrace(); // TODO handle this better
			numErrors++;
			logln("Error reading metadata package: "+e.toString());
			try {
				if (localPackageWriter != null) {
					localPackageWriter.flush();
					localPackageWriter.close();
					localPackageWriter = null;
				}
				if ((localPackageFile != null) && (localPackageFile.exists())) {
					localPackageFile.delete();
				}
			}
			catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					logln("Error closing reader: "+e.toString());
				}
				reader = null;
			}
			if (connection != null) {
				connection.disconnect();
				connection = null;
			}
		}		
		return newMetadata;	
	}	
	
	private boolean checkMetadata() throws Exception {
		log("Checking new metadata flag... ");
		boolean newMetadata = false;
		// if local homepage file doesn't exist then we should update metadata
		File homepageFile = new File(workspace.getMetadataDir(), "downloaded"+File.separator+"homepage.htm");
		if (!homepageFile.exists()) {
			log("metadata update needed.");
			return true;
		}
		// Read local homepage file to get metadata url
		String metadataUrlString = "http://homepage.mac.com/michaelhoward/MidnightMarsBrowser/metadata";
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(homepageFile));
			String inputLine;
			while ((inputLine = reader.readLine()) != null) {
				metadataUrlString = readMetadataParamFromLine(inputLine, "MMBMetaData", metadataUrlString);
			}
			if (metadataUrlString == null) {
				logln("Error reading "+homepageFile+": did not find expected tag.");
				// continue on with default
			}
		}
		catch (Exception e) {
			String msg = e.getMessage();
			if (msg == null)
				msg = e.toString();
			logln("Error reading local homepage file: "+msg);
			// continue on with default hard-coded metadata address
		}
		finally {
			if (reader != null) {
				reader.close();
				reader = null;
			}
		}
		if (!metadataUrlString.endsWith("/")) {
			metadataUrlString += "/";
		}
		// see if the latest.txt file has been updated
		File latestFile = new File(workspace.getMetadataDownloadedDir(), "latest2.txt");
		boolean updatedLatest = false;
		try {
			URL latestUrl = new URL(metadataUrlString+"latest2.txt");
			updatedLatest = updateFileFromURL(latestFile, latestUrl, false, false);
			if (updatedLatest) {
				logln(" New metadata found.");
				newMetadata = true;
			}
			else {
				logln();
			}
		}
		catch (Exception e) {
			String msg = e.getMessage();
			if (msg == null)
				msg = e.toString();
			logln(msg);
		}
		
		return newMetadata;
	}
	
	private String readMetadataParamFromLine(String inputLine, String tag, String inVal) {
		if (inVal != null)
			return inVal;
		String param = null;
		int loc = inputLine.indexOf(tag);
		int endLoc = -1;
		if (loc >= 0) {
			loc = inputLine.indexOf(" ", loc);
			loc++;
			while ((loc < inputLine.length()) && (inputLine.charAt(loc) == ' ')) {
				loc++;
			}
			endLoc = inputLine.indexOf(" ", loc);
			if (endLoc > 0) {
				param = inputLine.substring(loc, endLoc).trim();
			}
		}
		return param;
	}
	
	
	public void doCheckExploratorium(String roverCode) throws Exception {
		updatedRawImageFilenames = null;
		updatedRawImageSols = null;
		updatedGeneratedImageFilenames = null;
		updatedGeneratedImageSols = null;

		numNewImages = 0;
		checkLocalRawImagesFromExploratorium(roverCode, params);
		logln("There are "+numNewImages+" new "+MerUtils.displayRoverNameFromRoverCode(roverCode)+" images at Exploratorium.");
		numNewImagesExpl += numNewImages;
		if (numNewImages>0) {
			params.setUpdateImages(roverCode, true);
		}
	}
	
	private void checkLocalRawImagesFromExploratorium(String roverCode,
			UpdateParams params) throws Exception {
		logln("Checking "
				+ MerUtils.displayRoverNameFromRoverCode(roverCode)
				+ " raw images from Exploratorium. Last download time is "
				+ MerUtils.getExplDateFormat().format(params.downloadStartExplDate));

		// This didn't work; expl directory mod times aren't updated correctly.
		//StringBuffer pageCode = new StringBuffer();
		//String urlString = MerUtils.exploratoriumUrlStringFrom(roverCode);
		//LogFrame.getInstance().log("Reading "+urlString+"... ");
		//readTextFileFromURL(urlString, pageCode);
		//LogFrame.getInstance().logln("scanning... ");
		if (params.updateF)
			updateLocalRawImagesFromExploratoriumCameraCode(roverCode, "f");
		if (params.updateR)
			updateLocalRawImagesFromExploratoriumCameraCode(roverCode, "r");
		if (params.updateN)
			updateLocalRawImagesFromExploratoriumCameraCode(roverCode, "n");
		if (params.updateP)
			updateLocalRawImagesFromExploratoriumCameraCode(roverCode, "p");
		if (params.updateM)
			updateLocalRawImagesFromExploratoriumCameraCode(roverCode, "m");
	}
	
	public void doCheckJpl(String roverCode) throws NoRouteToHostException, FileNotFoundException, UnknownHostException, Exception {
		updatedRawImageFilenames = null;
		updatedRawImageSols = null;
		updatedGeneratedImageFilenames = null;
		updatedGeneratedImageSols = null;
		
		numNewImages = 0;
		smartUpdateRawImagesFromJPL(roverCode);
		logln("There are "+numNewImages+" new "+MerUtils.displayRoverNameFromRoverCode(roverCode)+" images at JPL.");
		numNewImagesJPL += numNewImages;
		if (numNewImages>0) {
			params.setUpdateImages(roverCode, true);
		}
	}
	
	
	private void doStartUpdate() {
		updatedRawImageFilenames = new HashSet();
		updatedRawImageSols = new HashSet();
		updatedGeneratedImageFilenames = new HashSet();
		updatedGeneratedImageSols = new HashSet();		
	}
	
	private void doUpdate(String roverCode) throws Exception {
		if (params.downloadMode == UpdateParams.DOWNLOAD_FROM_JPL) {
			updateRawImagesFromJPL(roverCode, params);
		} 
		else if (params.downloadMode == UpdateParams.DOWNLOAD_LATEST_FROM_JPL) {
			smartUpdateRawImagesFromJPL(roverCode);
		} 
		else if (params.downloadMode == UpdateParams.DOWNLOAD_FROM_EXPL) {
			updateLocalRawImagesFromExploratorium(roverCode);
		} 
		else if (params.downloadMode == UpdateParams.DOWNLOAD_FROM_LYLE) {
			updateFromLyle(roverCode, params);
		}
		else if (params.downloadMode == UpdateParams.DOWNLOAD_FROM_PDS_IMG) {
			updateFromPDSImgNode(roverCode, params);
		} 
	}
	
	private void updateLocalRawImagesFromExploratorium(String roverCode)
			throws Exception {
		logln("Updating " + MerUtils.displayRoverNameFromRoverCode(roverCode)
				+ " raw images from Exploratorium");
		if (params.updateF)
			updateLocalRawImagesFromExploratoriumCameraCode(roverCode, "f");
		if (params.updateR)
			updateLocalRawImagesFromExploratoriumCameraCode(roverCode, "r");
		if (params.updateN)
			updateLocalRawImagesFromExploratoriumCameraCode(roverCode, "n");
		if (params.updateP)
			updateLocalRawImagesFromExploratoriumCameraCode(roverCode, "p");
		if (params.updateM)
			updateLocalRawImagesFromExploratoriumCameraCode(roverCode, "m");
	}

	private void updateLocalRawImagesFromExploratoriumCameraCode(
			String roverCode, String cameraCode) throws Exception {
		StringBuffer pageCode = new StringBuffer();
		String addPath = MerUtils.exploratoriumPathFrom(roverCode, cameraCode);
		// String localPathname = MyPreferences.instance().webcacheDir +
		// File.separator
		// + addPath + File.separator + "index.htm";
		String urlString = baseExplUrl + addPath;
		// File localFile = new File(localPathname);
		log("Reading " + urlString + "... ");
		// Don't make this update local cached file; it's not a real file on
		// Expl, so
		// apparantly there is no last modified time or length to work with.
		readTextFileFromURL(urlString, pageCode);
		logln("scanning... ");

		int index = 0;
		int index2;
		String filename = null;
		// String pageCode = strbuf.toString();
		Date downloadStartExplDateDay = MerUtils
				.explDay(params.downloadStartExplDate);
		while (index >= 0) {
			index = pageCode.indexOf("HREF", index);
			if (index >= 0) {
				index = pageCode.indexOf("\"", index);
				if (index >= 0) {
					index2 = pageCode.indexOf("\"", index + 1);
					if (index2 >= 0) {
						filename = pageCode.substring(index + 1, index2);
						index = index2 + 1;
						if ((filename.length() == 11)
								&& (filename.charAt(4) == '-')
								&& (filename.charAt(7) == '-')) {
							Date testDate = MerUtils.getExplDateFolderFormat()
									.parse(filename);
							if (!testDate.before(downloadStartExplDateDay)) {
								if ((!params.downloadEndExplDateFlag)
										|| (!testDate
												.after(downloadStartExplDateDay))) {
									updateLocalRawFromExpScanFilenames(addPath
											+ filename.substring(0, 11));
								}
							}
						}
					}
				}
			}
		}
	}

	private void updateLocalRawFromExpScanFilenames(String addPath)
			throws Exception {
		// try {
		String urlString = baseExplUrl + addPath;
		log("Reading " + urlString + "... ");
		URL url = new URL(urlString);

		// String localPathname = MyPreferences.instance().webcacheDir +
		// File.separator
		// + addPath + File.separator + "index.htm";
		// File localFile = new File(localPathname);
		StringBuffer pageCode = new StringBuffer();
		// update local cached file
		// Don't make this update local cached file; it's not a real file on
		// Expl, so
		// apparantly there is no last modified time or length to work with.
		readTextFileFromURL(urlString, pageCode);

		log("scanning... ");
		if (params.mode == UpdateParams.MODE_UPDATE) {
			logln("");
		}
		int startNumNewImages = numNewImages;
		int index = 0;
		int index2;
		String filename = null;
		while (index >= 0) {
			index = pageCode.indexOf("HREF", index);
			if (index >= 0) {
				index = pageCode.indexOf("\"", index);
				if (index >= 0) {
					index2 = pageCode.indexOf("\"", index + 1);
					if (index2 >= 0) {
						filename = pageCode.substring(index + 1, index2);
						index = index2 + 1;
						// BUG FIX: encoded filenames may be longer than
						// standard length, because of the encoding (# char, for
						// example)
						if ((filename.endsWith(".JPG"))
								&& (filename.length() >= 27 + 4)) {
							URL newUrl = new URL(url, filename);
							// Scan the mod date
							index = pageCode.indexOf(" ", index);
							if (index >= 0) {
								String dateStr = pageCode
										.substring(index + 1, index + 1
												+ MerUtils.explDateFormatLength);
								Date date = MerUtils.getExplDateFormat().parse(
										dateStr);
								if ((params.downloadStartExplDate == null)
										|| (!date
												.before(params.downloadStartExplDate))) {
									updateLocalRawFromURL(newUrl);
									if (params.mode == UpdateParams.MODE_UPDATE) {
										if ((latestDownloadedExplTime == null)
												|| (date
														.after(latestDownloadedExplTime))) {
											latestDownloadedExplTime = date;
										}
									}
								} else {
									if (params.mode == UpdateParams.MODE_UPDATE) {
										debugln(
														"Skipped "
																+ filename
																+ "; index time "
																+ dateStr
																+ " downloadStartExplDate "
																+ MerUtils
																		.getExplDateFormat()
																		.format(
																				params.downloadStartExplDate));
									}
								}
							} else {
								// TODO do something for real
								System.err.println("Error scanning date ");
							}
						}
					}
				}
			}
		}
		if (params.mode == UpdateParams.MODE_CHECK) {
			int myNumNewImages = numNewImages - startNumNewImages;
			if (myNumNewImages > 0) {
				log(
						"found " + myNumNewImages + " new images.");
			}
			logln("");
		}
	}
	
	public void smartUpdateRawImagesFromJPL(String roverCode) throws NoRouteToHostException, FileNotFoundException, UnknownHostException, Exception {
		// TODO: doesn't currently support camera selection. Don't need that right now for smart update.
		updatedRawImageFilenames = new HashSet();
		updatedRawImageSols = new HashSet();
		String urlString = baseJplUrl
				+ MerUtils.roverNameFromRoverCode(roverCode) + ".html";
//		File localFile = new File(MyPreferences.instance().webcacheDir, 
//				baseJplCachePathString+File.separator+MerUtils.roverNameFromRoverCode(roverCode) + ".html");
		StringBuffer pagebuf = new StringBuffer();
		StringBuffer strbuf = new StringBuffer();
		if (params.mode == UpdateParams.MODE_CHECK) {
			logln("Checking "
					+ MerUtils.displayRoverNameFromRoverCode(roverCode)
					+ " raw images from JPL");
		}
		else {
			logln("Reading "
					+ MerUtils.displayRoverNameFromRoverCode(roverCode)
					+ " raw images from JPL");			
		}
		log("Reading "+urlString+"... ");

		//boolean downloaded = updateFileFromURL(localFile, new URL(urlString), false, false);
		readTextFileFromURL(urlString, pagebuf);
		logln();
		
		//readTextFile(localFile, pagebuf);

		int index = 0;
		int index2;
		int index3;
		String filename = null;
		while (index >= 0) {
			index = pagebuf.indexOf("<option value=", index);
			if (index >= 0) {
				index = pagebuf.indexOf("\"", index);
				if (index >= 0) {
					index2 = pagebuf.indexOf("\"", index + 1);
					if (index2 >= 0) {
						filename = pagebuf.substring(index + 1, index2);
						index = index2 + 1;
						index2 = pagebuf.indexOf("*NEW*", index);
						index3 = pagebuf.indexOf("</option>", index);
						int uPos = filename.indexOf("_");
						int pPos = filename.indexOf(".");
						if ((index2 > 0) && (index2 < index3)
								&& (uPos>0) && (pPos > 0) && (uPos < pPos)) {
							// ex: spirit_m162.html
//							String solString = filename.substring(filename
//							.length() - 8, filename.length() - 5);
							String solString = filename.substring(uPos+2, pPos);							
							int sol = Integer.parseInt(solString);
//							String cameraCode = filename.substring(filename
//									.length() - 9, filename.length() - 8);
							String cameraCode = filename.substring(uPos+1, uPos+2);
							String pageName = MerUtils
									.roverNameFromRoverCode(roverCode)
									+ "_"
									+ cameraCode
									+ solString
									+ "_text.html";
							String urlString2 = baseJplUrl + pageName;
							log("Reading "+urlString2+"... ");
							try {
								//File localFile2 = new File(MyPreferences.instance().webcacheDir, 
								//		baseJplCachePathString+File.separator+pageName);								
								//updateFileFromURL(localFile2, new URL(urlString2), false, false);
								readTextFileFromURL(urlString2, strbuf);
								logln();
								//readTextFile(localFile2, strbuf);
								updateLocalRawScanImageFileNamesJPL(strbuf,
										solString, sol);
							}
							catch (Exception e) {
								String msg = e.getMessage();
								if (msg == null)
									msg = e.toString();								
								logln(msg);								
							}
						}
					}
				}
			}
		}
	}

	public void updateRawImagesFromJPL(String roverCode, UpdateParams params) throws Exception {
		int noSolCount = 0;
		for (int sol = params.startSol; sol <= params.endSol; sol++) {
			boolean solExists = false;
			if (params.updateF)
				solExists = updateRawImagesFromJPLCameraCode(roverCode, sol, "f", solExists);
			if (params.updateR)
				solExists = updateRawImagesFromJPLCameraCode(roverCode, sol, "r", solExists);
			if (params.updateN)
				solExists = updateRawImagesFromJPLCameraCode(roverCode, sol, "n", solExists);
			if (params.updateP)
				solExists = updateRawImagesFromJPLCameraCode(roverCode, sol, "p", solExists);
			if (params.updateM)
				solExists = updateRawImagesFromJPLCameraCode(roverCode, sol, "m", solExists);
			if (solExists) {
				noSolCount = 0;
			} else {
				noSolCount++;
			}
			if (noSolCount > maxEmptySolsForJplDownload) {
				logln("Scanned "+maxEmptySolsForJplDownload+" sols without finding images; terminating.");
				break;
			}
		}
	}
		
	public boolean updateRawImagesFromJPLCameraCode(String roverCode, int sol, String cameraCode, 
			boolean solExists) throws Exception {
		StringBuffer pageCode = new StringBuffer();
		String solString = MerUtils.zeroPad(sol, 3);
		String urlString = baseJplUrl
				+ MerUtils.roverNameFromRoverCode(roverCode) + "_"
				+ cameraCode + solString + "_text.html";
		logln("Checking for " + urlString);
		try {
			readTextFileFromURL(urlString, pageCode, false);
		} catch (FileNotFoundException e) {
			// silently ignore and continue; don't break for crying out loud!
			return solExists;
		}
		logln("Found " + urlString);
		updateLocalRawScanImageFileNamesJPL(pageCode, solString, sol);
		return true;
	}
	
	private void updateLocalRawScanImageFileNamesJPL(StringBuffer pageCode,
			String solString, int sol) throws Exception {
//		boolean done = false;
		int index = 0;
		int index2;
		String fileName = null;
		while (index >= 0) {
			index = pageCode.indexOf("href", index);
			if (index >= 0) {
				index = pageCode.indexOf("\"", index);
				if (index >= 0) {
					index2 = pageCode.indexOf("\"", index + 1);
					if (index2 >= 0) {
						fileName = pageCode.substring(index + 1, index2);
						index = index2 + 1;
						if ((fileName.endsWith(".JPG"))
								&& (fileName.indexOf(solString) > 0)) {
							URL fileUrl = new URL(baseJplUrl + fileName);
							updateLocalRawFromURL(fileUrl);
						}
					}
				}
			}
		}
	}
	
	public void updateFromPDSImgNode(String roverCode, UpdateParams params) throws Exception {
		int noSolCount = 0;
		for (int sol = params.startSol; sol <= params.endSol; sol++) {
			boolean solExists = false;
			if (params.updateF || params.updateR) {
				solExists = updateFromPDS(roverCode, sol, "ho", params, solExists);
			}
			if (params.updateN) {
				solExists = updateFromPDS(roverCode, sol, "no", params, solExists);
			}
			if (params.updateP) {
				solExists = updateFromPDS(roverCode, sol, "po", params, solExists);
				solExists = updateFromPDS(roverCode, sol, "ps", params, solExists);
			}
			if (params.updateM) {
				solExists = updateFromPDS(roverCode, sol, "mo", params, solExists);
				solExists = updateFromPDS(roverCode, sol, "ms", params, solExists);
			}
			if (solExists) {
				noSolCount = 0;
			} else {
				noSolCount++;
			}
			if (noSolCount > maxEmptySolsForJplDownload) {
				logln("Scanned "+maxEmptySolsForJplDownload+" sols without finding images; terminating.");
				break;
			}
		}
	}
	
	private boolean updateFromPDS(String roverCode, int sol, String volumeCode, UpdateParams params,
			boolean solExists) throws Exception {
		solExists = updateFromPDS(roverCode, sol, volumeCode, "edr", params, solExists);
		solExists = updateFromPDS(roverCode, sol, volumeCode, "rdr", params, solExists);
		return solExists;
	}
	
	private boolean updateFromPDS(String roverCode, int sol, String volumeCode, String subdirectory, UpdateParams params,
			boolean solExists) throws Exception {
		StringBuffer pageCode = new StringBuffer();
		String solString = MerUtils.zeroPad(sol, 4);
		String urlString = 
			"http://pdsimg.jpl.nasa.gov/Atlas/MER/mer"+roverCode+volumeCode+"_0xxx/data/sol"+solString+"/"+subdirectory+"/";
		logln("Checking for " + urlString);
		try {
			readTextFileFromURL(urlString, pageCode, false);
		} catch (FileNotFoundException e) {
			// silently ignore and continue; don't break for crying out loud!
			return solExists;
		}
		logln("Found " + urlString);
		updateFromPDSScanImageFileNames(pageCode, urlString, params.productTypes, params.filters);
		return true;
	}

	private void updateFromPDSScanImageFileNames(StringBuffer pageCode,
			String urlString, String[] productTypes, String[] filters) throws Exception {
		int index = 0;
		int index2;
		String fileName = null;
		String fileNameUpperCase = null;
		while (index >= 0) {
			index = pageCode.indexOf("href", index);
			if (index >= 0) {
				index = pageCode.indexOf("\"", index);
				if (index >= 0) {
					index2 = pageCode.indexOf("\"", index + 1);
					if (index2 >= 0) {
						fileName = pageCode.substring(index + 1, index2);						
						fileNameUpperCase = fileName.toUpperCase();
						index = index2 + 1;
						if (fileName.length() == 31 && fileNameUpperCase.endsWith(".IMG")) {
							boolean matchesProductType = false;
							boolean matchesFilter = (filters == null);
							String productType = MerUtils.productTypeFromFilename(fileName);
							for (int n=0; productTypes != null && n<productTypes.length; n++) {
								if (productType.equalsIgnoreCase(productTypes[n])) {
									matchesProductType = true;
									break;
								}
							}
							String filter = fileName.substring(23,25).toUpperCase();
							for (int n=0; filters != null && n<filters.length; n++) {
								if (filter.startsWith(filters[n].toUpperCase())) {
									matchesFilter = true;
									break;
								}
							}
							if (matchesProductType && matchesFilter) {
								URL fileUrl = new URL(urlString + fileName);
								updateLocalImgFromURL(fileUrl);
							}
						}
					}
				}
			}
		}
	}

	public void updateFromLyle(String roverCode, UpdateParams params) throws Exception {
		logln("Updating "
				+ MerUtils.displayRoverNameFromRoverCode(roverCode)
				+ " calibrated color images from lyle.org.");
		log("Reading "+lyleCacheIndexPathString+"... ");
		StringBuffer indexBuffer = new StringBuffer();
		readTextFileFromURL(lyleIndexUrl, indexBuffer);
		logln();
		
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new StringReader(indexBuffer.toString()));
			String inputLine;
			while ((inputLine = reader.readLine()) != null) {
				inputLine = inputLine.trim();
				String[] strs = inputLine.split(";");
				if (strs.length != 2) {
					throw new Exception("Invalid indexv2 line; expecting two parameters. line: "+inputLine);
				}
				String srcPathname = strs[0];
				long srcLastMod = Long.parseLong(strs[1]);
				if (srcPathname.length() > 0) {
					int lastSlash = srcPathname.lastIndexOf("/");
					String filename = srcPathname.substring(lastSlash + 1);
					//try {
					//	filename = URLDecoder.decode(filename, "UTF-8");
					//}
					//catch (Exception e) {
					//	System.err.println("Error decoding filename: "+e.toString());
					//}
					String srcRoverCode = MerUtils.roverCodeFromFilename(filename);
					int sol = MerUtils.solFromFilename(filename);
					if ((srcRoverCode.equals(roverCode)) && (sol >= params.startSol) && (sol <= params.endSol)) {	
						String localPathname = MerUtils.localPathnameFrom(roverCode,
								"ps", sol, filename);
						File dstFile = new File(workspace.getGeneratedImageDir(), localPathname);
						log("Updating "+localPathname+"... ");
						if (srcLastMod/1000 == dstFile.lastModified()/1000) {
							logln("skipped; same last modified time in index.");
						}
						else if (srcLastMod/1000 > dstFile.lastModified()/1000) {
							URL srcUrl = new URL(baseLyleUrl + srcPathname);
							try {
								updateLocalRawFromURL(srcUrl, "ps");
								// Set local file to last mod time from index, so we can use the last mod time in the index for future comparison.
								// No; don't do this; it doesn't work well.
								//dstFile.setLastModified(srcLastMod);
								logln();
							}
							catch (InvalidDownloadException e) {
								logln("ERROR Invalid download: "+e.toString());
							}
						}
						else {
							logln("skipped; local copy has later last modified time in index.");
						}
					}
				}
			}
		} finally {
			if (reader != null) {
				reader.close();
				reader = null;
			}
		}
	}	
	
	private void doGenerate(String roverCode) throws Exception {
		if (params.downloadMode != UpdateParams.DOWNLOAD_FROM_LYLE) {
			if (params.generateFA || params.generateRA || params.generateNA 
					|| params.generatePA || params.generatePC /*|| params.generateRawImageThumbnails*/) {
				generateImages(roverCode, params.startSol, params.endSol);
			}
		}
	}
	
	private void generateImages(String roverCode, int startSol, int endSol)
			throws Exception {
		logln("Generating " + MerUtils.displayRoverNameFromRoverCode(roverCode)
				+ " images");
		for (int n = startSol; n <= endSol; n++) {
			Integer solN = new Integer(n);
			if (params.generateImagesCheckAll
					|| params.generateImagesAll
					|| (updatedMetadataImageSols.contains(solN))
					|| ((updatedRawImageSols != null) && updatedRawImageSols
							.contains(solN))) {
				generateImages(roverCode, n);
			}
		}
		debugln(
				"...Done generating "
						+ MerUtils.displayRoverNameFromRoverCode(roverCode)
						+ " images");
//		ImageUtils.shutdown();
	}

	private void generateImages(String roverCode, int sol) throws Exception {
		// by specifying null instead of "R" for camera eye in generateRawImages
		// for f, n, and r, it should pick up either left or right if only one
		// is
		// available. This might result in some duplicate postings if one is
		// modified but the other is posted, but it's better for now not to
		// miss the left images that are getting posted.
		Integer solInteger = new Integer(sol);
		removeDeprecatedFilenames(roverCode, "fa", solInteger);
		removeDeprecatedFilenames(roverCode, "ra", solInteger);
		removeDeprecatedFilenames(roverCode, "na", solInteger);
		removeDeprecatedFilenames(roverCode, "pa", solInteger);
		removeDeprecatedFilenames(roverCode, "pc", solInteger);
		if (params.generateFA) {
			generateAnaglyphs(roverCode, "f", "fa", solInteger,
					workspace.generateAnaglyphShiftPercentHazcam, workspace.generateAnaglyphTrimHHazcam);
		}
		if (params.generateRA) {
			generateAnaglyphs(roverCode, "r", "ra", solInteger,
					workspace.generateAnaglyphShiftPercentHazcam, workspace.generateAnaglyphTrimHHazcam);
		}
		if (params.generateNA) {
			generateAnaglyphs(roverCode, "n", "na", solInteger,
					workspace.generateAnaglyphShiftPercentNavcam, workspace.generateAnaglyphTrimHNavcam);
		}
		// pancam anaglyphs handled by generateFalseColor now
		//generateAnaglyphs(roverCode, "p", "pa", sol, shiftPercent,
		// trimH);
		if ((params.generatePA) || (params.generatePC)) {
			generateFalseColor(roverCode, sol);
		}
	}
	
	void removeDeprecatedFilenames(String roverCode, String dstCode, Integer solInteger) {
		int sol = solInteger.intValue();
		String tgtDirString = roverCode + File.separator + dstCode
				+ File.separator + MerUtils.zeroPad(sol, 3);
		File dstDir = new File(workspace.getGeneratedImageDir(), tgtDirString);
		if (!dstDir.exists())
			return;
		File[] files = dstDir.listFiles();
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			String filename = file.getName();
			if (filename.endsWith("_th.jpg")) {
				if (file.delete()) {
					logln("Deleting obsolete generated file " + file.getAbsolutePath() + "... ");
				}				
			}
			if (filename.endsWith(".jpg")) {
				if (filename.length()>17) {
					String temp = filename.substring(14,17);
					if ((temp.equals("_fc")) || (temp.equals("_an"))) {
						if (file.delete()) {
							logln("Deleting obsolete generated file " + file.getAbsolutePath() + "... ");
							workspace.getImageIndex().removeFilenameFromImageIndex(workspace.getGeneratedImageDir(), 
									roverCode, dstCode, solInteger, file.getName());
						}
					}
				}
			}
		}
	}
	
	public void generateAnaglyphs(String roverCode, String cameraCode,
			String dstCode, Integer solInteger, double shiftPercent, int trimH)
			throws Exception {
		int sol = solInteger.intValue();
		String srcDirString = roverCode + File.separator + cameraCode
				+ File.separator + MerUtils.zeroPad(sol, 3);
		String tgtDirString = roverCode + File.separator + dstCode
				+ File.separator + MerUtils.zeroPad(sol, 3);
		File srcDir = null;
		srcDir = new File(workspace.getRawImageDir(), srcDirString);
		if (!srcDir.exists())
			return;
		logln("Processing directory " + srcDir.getAbsolutePath());
		File[] files = srcDir.listFiles();
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			String fileName = file.getName();
			if ((fileName.endsWith(".JPG") && (fileName.length() == 31))
					&& (fileName.charAt(23) == 'L') 
					&& (!MerUtils.isOtherProductTypeFilename(fileName))
					&& (!(fileName.substring(16,18).equals("__"))) ) {
				String beginRightFileName = fileName.substring(0, 23) + "R";
				for (int j = 0; j < files.length; j++) {
					String jFileName = files[j].getName();
					if ((jFileName.endsWith(".JPG"))
							&& (jFileName.length() == 31)
							&& (jFileName.substring(0, beginRightFileName
									.length()).equals(beginRightFileName))) {
						String type = fileName.substring(23,25) + jFileName.substring(23,25);
						generateAnaglyph(roverCode, dstCode, solInteger, 
								file, files[j],
								tgtDirString, shiftPercent, trimH, type);
						Thread.yield();
					}
				}
			}
		}
		debugln("...Done processing directory "
				+ srcDir.getAbsolutePath());
	}

	/**
	 * type can be null, otherwise we use it in creating the tgt filename
	 */
	private void generateAnaglyph(String roverCode, String dstCameraCode, Integer solInteger, 
			File leftFile, File rightFile, String tgtDirString, double shiftPercent,
			int trimH, String type) throws Exception {		
		String fileName = leftFile.getName();
		String rightFileName = rightFile.getName();
		String targetName = fileName.substring(0, 23);
		String compressedType = MerUtils.compressFalseColorType(type).toLowerCase();
		String pathname = tgtDirString + File.separator + targetName
				+ compressedType + ".jpg";
		int sol = solInteger.intValue();
		File targetFile = null;
		targetFile = new File(workspace.getGeneratedImageDir(), pathname);
		if (((updatedRawImageFilenames != null) && ((updatedRawImageFilenames
				.contains(fileName)) || (updatedRawImageFilenames
				.contains(rightFileName))))
				|| (params.generateImagesAll) || (!targetFile.exists())
				|| (updatedMetadataImageFilenames.contains(fileName))
		//|| (!targetFileBr.exists())
		//|| (!targetFileTh.exists())
		) {
			//new File(this.generatedImageDir, tgtDirString).mkdirs();
			targetFile.getParentFile().mkdirs();
			try {
				checkForTerminate();
				logln("   Making anaglyph " + targetFile.getName());
				if ((MerUtils.productTypeFromFilename(fileName).equals("ESF"))
						&& (MerUtils.productTypeFromFilename(rightFileName).equals("ESF"))) {
					// For subframe images, don't trim
					trimH = 0;
				}
				debugln("    Reading leftFile "
						+ leftFile.getAbsolutePath() + "... ");
				ImageData leftImageData = new ImageData(leftFile.getAbsolutePath());
				
				int leftStretchMin = 0;
				int leftStretchMax = 256;
                int leftDevigA = 0;
                int leftDevigB = 0;
				if (workspace.isAdjustAnaglyphRawImageBrightness()) {
// TODO REINSTATE
//					ImageStretchParam param = MetaData.getImageStretchParam(leftFile.getName());
//					if (param != null) {
//						leftStretchMin = param.minVal;
//						leftStretchMax = param.maxVal;
//                        leftDevigA = param.devigA;
//                        leftDevigB = param.devigB;
//					}
				}
				checkForTerminate();
				debugln("    Reading rightFile "
						+ rightFile.getAbsolutePath() + "... ");
				ImageData rightImageData = new ImageData(rightFile.getAbsolutePath());
				// If right stretch parameter does not exist, do same stretch as left.
				int rightStretchMin = leftStretchMin;
				int rightStretchMax = leftStretchMax;
                int rightDevigA = leftDevigA;
                int rightDevigB = leftDevigB;
				if (workspace.isAdjustAnaglyphRawImageBrightness()) {
//					ImageStretchParam param = MetaData.getImageStretchParam(rightFile.getName());
//					if (param != null) {
//						rightStretchMin = param.minVal;
//						rightStretchMax = param.maxVal;
//                       rightDevigA = param.devigA;
//                        rightDevigB = param.devigB;
//					}
				}
				checkForTerminate();
				debugln("    Making anaglyph... ");
				ImageData anaImageData = ImageUtils.makeAnaglyphImage(
							leftImageData, leftStretchMin, leftStretchMax, leftDevigA, leftDevigB,
							rightImageData, rightStretchMin, rightStretchMax, rightDevigA, rightDevigB,
							shiftPercent, trimH);
				checkForTerminate();
				debugln("    Saving anaglyph "
						+ targetFile.getAbsolutePath() + "... ");
				
				ImageLoader imageLoader = new ImageLoader();
				imageLoader.data = new ImageData[] {anaImageData};
				imageLoader.save(targetFile.getAbsolutePath(), SWT.IMAGE_JPEG);

				setViewImage(anaImageData, "Update in Progress: "
						+ MerUtils.displayRoverNameFromRoverCode(MerUtils
								.roverCodeFromFilename(targetFile.getName()))
						+ " Sol " + sol + " " + targetFile.getName(),
						MerUtils.roverCodeFromFilename(targetFile.getName()),
						sol,	 targetFile.getName());
				this.updatedGeneratedImageFilenames.add(targetFile.getName());
				this.updatedGeneratedImageSols.add(new Integer(sol));
				workspace.getImageIndex().addFileToImageIndex(workspace.getGeneratedImageDir(), pathname);
				// done
				numAnaglyphsGenerated++;
				debugln("    Done.");
				checkForTerminate();
			} catch (Exception e) {
				logln("--- Failed: " + e.toString());
				numErrors++;
			}
		}
/*		
		if (!thumbFile.exists()) {
			checkForTerminate();
			try {
				LogFrame.getInstance().logln("   Making thumbnail " +tgtDirString+File.separator
						+ thumbFile.getName());
				BufferedImage image = ImageIO.read(targetFile);
				makeThumbnail(thumbFile, image);			
			}
			catch (Exception e) {
				LogFrame.getInstance().logln("--- Failed: " + e.toString());
				numErrors++;
			}
		}
*/
	}
	
	public void generateFalseColor(String roverCode, int sol) throws Exception {
		checkForTerminate();
		Integer solInteger = new Integer(sol);
		String srcDirString = roverCode + File.separator + "p" + File.separator
				+ MerUtils.zeroPad(sol, 3);
		String tgtDirString = roverCode + File.separator + "pc"
				+ File.separator + MerUtils.zeroPad(sol, 3);
		String anaTgtDirString = roverCode + File.separator + "pa"
				+ File.separator + MerUtils.zeroPad(sol, 3);
		File srcDir = null;
		srcDir = new File(workspace.getRawImageDir(), srcDirString);
		if (!srcDir.exists())
			return;
		logln("Processing directory " + srcDir.getAbsolutePath());

		//File targetDir = new File(this.generatedImageDir, tgtDirString);
		File[] files = srcDir.listFiles();
		// match patterns
		Vector sets = UpdateTask.findPancamImageSets(files, workspace);
		
		for (int setn=0; setn<sets.size(); setn++) {
			HashMap matchedFiles = (HashMap) sets.elementAt(setn);
			// got a sequence, see if we want to make a color picture
			if (params.generatePC) {
				int numColorLeft = 0;
				int numColorRight = 0;
				// left
				for (int n = 0; n < workspace.generatePancamLeftFalseColorTypes.length; n++) {
					String falseColorType = workspace.generatePancamLeftFalseColorTypes[n];
					File redFile = (File) matchedFiles.get(falseColorType
							.substring(0, 2));
					File greenFile = (File) matchedFiles.get(falseColorType
							.substring(2, 4));
					File blueFile = (File) matchedFiles.get(falseColorType
							.substring(4, 6));
					if ((redFile != null) && (greenFile != null)
							&& (blueFile != null)) {
						numColorLeft++;
						boolean delete = (numColorLeft > workspace.generatePancamLeftFalseColorTypesMax);
						generateFalseColor(roverCode, "pc", solInteger, falseColorType, 
								redFile, greenFile, blueFile, tgtDirString, delete);
						Thread.yield();			
					}
				}
				// right
				for (int n = 0; (n < workspace.generatePancamRightFalseColorTypes.length); n++) {
					String falseColorType = workspace.generatePancamRightFalseColorTypes[n];
					File redFile = (File) matchedFiles.get(falseColorType
							.substring(0, 2));
					File greenFile = (File) matchedFiles.get(falseColorType
							.substring(2, 4));
					File blueFile = (File) matchedFiles.get(falseColorType
							.substring(4, 6));
					if ((redFile != null) && (greenFile != null)
							&& (blueFile != null)) {
						numColorRight++;
						boolean delete = numColorRight > workspace.generatePancamRightFalseColorTypesMax;
						generateFalseColor(roverCode, "pc", solInteger, falseColorType, 
								redFile, greenFile, blueFile, tgtDirString, delete);
						Thread.yield(); 
					}
				}
				// backup
				if ((numColorLeft == 0) && (numColorRight == 0)) {
					//                System.out.println("No image output for sequence
					// "+pattern);
					//            	if none found, check backup possibilities
					for (int n = 0; (n < workspace.generatePancamBackupFalseColorTypes.length); n++) {
						String falseColorType = workspace.generatePancamBackupFalseColorTypes[n];
						File redFile = (File) matchedFiles.get(falseColorType
								.substring(0, 2));
						File greenFile = (File) matchedFiles.get(falseColorType
								.substring(2, 4));
						File blueFile = (File) matchedFiles.get(falseColorType
								.substring(4, 6));
						if ((redFile != null) && (greenFile != null)
								&& (blueFile != null)) {
							numColorLeft++;
							boolean delete = (numColorLeft > workspace.generatePancamBackupFalseColorTypesMax);
							generateFalseColor(roverCode, "pc", solInteger, falseColorType,
									redFile, greenFile, blueFile, tgtDirString, delete);
							Thread.yield();
						}
					}
				}
			}
			// see if we want to make an anaglyph from the sequence
			if (params.generatePA) {
				int numAna = 0;
				for (int n = 0; (n < workspace.generatePancamAnaglyphTypes.length)
						&& (numAna < workspace.generatePancamAnaglyphTypesMax); n++) {
					String anaType = workspace.generatePancamAnaglyphTypes[n];
					File leftFile = (File) matchedFiles.get(anaType.substring(
							0, 2));
					File rightFile = (File) matchedFiles.get(anaType.substring(
							2, 4));
					if ((leftFile != null) && (rightFile != null)) {
						numAna++;
						generateAnaglyph(roverCode, "pa", solInteger,
								leftFile, rightFile,
								anaTgtDirString,
								workspace.generateAnaglyphShiftPercentPancam,
								workspace.generateAnaglyphTrimHPancam, anaType);
						Thread.yield();
					}
				}
			}
		}
		debugln("...Done processing directory " + srcDir.getAbsolutePath());
	}

	/**
	 * Break down a list of pancam image files into sets based on a heuristic.
	 * Also a manual seqstart command can be put in a metadata file to force a 
	 * sequence to start on a particular file.
	 * Returns a Vector of LinkedHashMaps, the LinkedHashMaps are indexed by eye/filter
	 * and contains the file objects.
	 * This is changed to include .img because we scan those in pan generation.
	 */
	public static Vector findPancamImageSets(File[] files, MMBWorkspace workspace) {
		Vector sets = new Vector();
		int index = 0;
		LinkedHashMap matchedFiles = null;
		String lastSiteCmdSeq = null;
		String newSiteCmdSeq = null;
		String newEyeFilter = null;
		while (index < files.length) {
			matchedFiles = new LinkedHashMap();
			lastSiteCmdSeq = null;
			while (index < files.length) {
				String filename = files[index].getName();
				//				LogFrame.getInstance().debugln(" Checking index "+index+" file "+filename);
				if (filename.length() == 31) {
					String ext = filename.substring(27, 31);
					if (ext.equalsIgnoreCase(".jpg") || ext.equalsIgnoreCase(".png")
							|| ext.equalsIgnoreCase(".img")) {
						// BUG FIX: ignore thumbnail images, since they may
						// duplicate 'regular' images and mess us up.
						// Later maybe consider the more general problem of
						// duplicate files, different product types.
						// Ideally, we need a "take best image" approach. However,
						// this fix will probably suffice for now,
						// unless the rover teams change the way they operate.
						//String productType = MerUtils
						//		.productTypeFromFilename(filename);
						if (MerUtils.isOtherProductTypeFilename(filename)) {
							index++;
							continue;
						}
						// new patch: ignore "__" files - should be ##
						if (filename.substring(16,18).equals("__")) {
							index++;
							continue;
						}
						newSiteCmdSeq = filename.substring(14, 23);
						if ((lastSiteCmdSeq != null)
								&& (!lastSiteCmdSeq.equals(newSiteCmdSeq)))
							break;
						newEyeFilter = filename.substring(23, 25);
						if (matchedFiles.containsKey(newEyeFilter))
							break;
						if ((!matchedFiles.isEmpty()) && (workspace.hasSeqStart(filename))) {
							break;
						}
						matchedFiles.put(newEyeFilter, files[index]);
						lastSiteCmdSeq = newSiteCmdSeq;
					}
				}
				index++;
			}
			if (!matchedFiles.isEmpty()) {
				sets.add(matchedFiles);
			}
		}
		return sets;
	}
	
	public void generateFalseColor(String roverCode, String tgtCameraCode, Integer solInteger, 
			String type, File redFile,
			File greenFile, File blueFile, String targetDirString,
			boolean delete)
			throws Exception {
		checkForTerminate();		
		String rootName = redFile.getName().substring(0, 23);
		String pathname = targetDirString + File.separator + rootName
				+ MerUtils.compressFalseColorType(type).toLowerCase();
		int sol = solInteger.intValue();
		File targetFile = null;
		File thumbFile = null;
		targetFile = new File(workspace.getGeneratedImageDir(), pathname + ".jpg");
		if (delete) {
			if (targetFile.isFile()) {
				targetFile.delete();
			}
			if ((thumbFile != null) && (thumbFile.isFile())) {
				thumbFile.delete();
			}
			debugln("   Cleaning up "+targetFile.getName());
			//this.updatedGeneratedImageFilenames.add(targetFile.getName());
			//this.updatedGeneratedImageSols.add(new Integer(sol));
			workspace.getImageIndex().removeFilenameFromImageIndex(workspace.getGeneratedImageDir(), roverCode, tgtCameraCode, 
					solInteger, targetFile.getName());
		}
		else if (((updatedRawImageFilenames != null) && ((updatedRawImageFilenames
				.contains(redFile.getName()))
				|| (updatedRawImageFilenames.contains(greenFile.getName())) || (updatedRawImageFilenames
				.contains(blueFile.getName()))))
				|| (params.generateImagesAll)
				|| (!targetFile.exists())
				|| (updatedMetadataImageFilenames.contains(redFile.getName()))
				|| (updatedMetadataImageFilenames.contains(greenFile.getName()))
				|| (updatedMetadataImageFilenames.contains(blueFile.getName()))
				) {
			//new File(this.generatedImageDir, targetDirString).mkdirs();
			targetFile.getParentFile().mkdirs();
			try {
				checkForTerminate();
				logln("   Making false color image "
						+ targetFile.getName());
				int trimH = workspace.generateFalseColorTrimHPixels;
				if ((MerUtils.productTypeFromFilename(redFile.getName()).equals("ESF"))
						&& (MerUtils.productTypeFromFilename(greenFile.getName()).equals("ESF"))
						&& (MerUtils.productTypeFromFilename(blueFile.getName()).equals("ESF"))) {
					// For subframe images, don't trim
					trimH = 0;
				}
				debugln("     Reading redFile " + redFile.getAbsolutePath() + "... ");
				ImageData redImageData = new ImageData(redFile.getAbsolutePath());
				int redStretchMin = 0;
				int redStretchMax = 256;
                int redDevigA = 0;
                int redDevigB = 0;
// TODO REINSTATE                
//				if (MyPreferences.instance().adjustFalseColorRawImageBrightness) {
//					ImageStretchParam param = MetaData.getImageStretchParam(redFile.getName());
//					if (param != null) {
//						redStretchMin = param.minVal;
//						redStretchMax = param.maxVal;
//                      redDevigA = param.devigA;
//                        redDevigB = param.devigB;
//					}
//				}
				checkForTerminate();
				debugln("     Reading greenFile " + greenFile.getAbsolutePath() + "... ");
				ImageData greenImageData = new ImageData(greenFile.getAbsolutePath());
				int greenStretchMin = 0;
				int greenStretchMax = 256;
                int greenDevigA = 0;
                int greenDevigB = 0;
//				if (MyPreferences.instance().adjustFalseColorRawImageBrightness) {
//					ImageStretchParam param = MetaData.getImageStretchParam(greenFile.getName());
//					if (param != null) {
//						greenStretchMin = param.minVal;
//						greenStretchMax = param.maxVal;
//                        greenDevigA = param.devigA;
//                        greenDevigB = param.devigB;
//					}
//				}
				checkForTerminate();
				debugln("     Reading blueFile " + blueFile.getAbsolutePath() + "... ");
				ImageData blueImageData = new ImageData(blueFile.getAbsolutePath());
				int blueStretchMin = 0;
				int blueStretchMax = 256;
                int blueDevigA = 0;
                int blueDevigB = 0;
//				if (MyPreferences.instance().adjustFalseColorRawImageBrightness) {
//					ImageStretchParam param = MetaData.getImageStretchParam(blueFile.getName());
//					if (param != null) {
//						blueStretchMin = param.minVal;
//						blueStretchMax = param.maxVal;
//                        blueDevigA = param.devigA;
//                        blueDevigB = param.devigB;
//					}
//				}
				checkForTerminate();
				debugln("     Making color... ");
				ImageData colorImageData = ImageUtils.makeColorImage(
							redImageData, redStretchMin, redStretchMax, redDevigA, redDevigB,
							greenImageData, greenStretchMin, greenStretchMax, greenDevigA, greenDevigB,
							blueImageData, blueStretchMin, blueStretchMax, blueDevigA, blueDevigB,
							trimH);
				checkForTerminate();
				debugln("     Saving color " + targetFile.getAbsolutePath() + "... ");
				
				ImageLoader imageLoader = new ImageLoader();
				imageLoader.data = new ImageData[] {colorImageData};
				imageLoader.save(targetFile.getAbsolutePath(), SWT.IMAGE_JPEG);
				
				setViewImage(colorImageData, "Update in Progress: "
						+ MerUtils.displayRoverNameFromRoverCode(MerUtils
								.roverCodeFromFilename(targetFile.getName()))
						+ " Sol " + sol + " " + targetFile.getName(),
						MerUtils.roverCodeFromFilename(targetFile.getName()),
						sol, targetFile.getName());
				
				this.updatedGeneratedImageFilenames.add(targetFile.getName());
				this.updatedGeneratedImageSols.add(new Integer(sol));
				workspace.getImageIndex().addFileToImageIndex(workspace.getGeneratedImageDir(), pathname + ".jpg");
				// done
				numFalseColorGenerated++;
				debugln("     Done.");
				checkForTerminate();
			} catch (Exception e) {
				logln("--- Failed: " + e.toString());
				numErrors++;
			}
		}
	}
	
	private void readTextFile(File file, StringBuffer strbuf)
			throws FileNotFoundException, UnknownHostException,
			NoRouteToHostException, Exception {
		checkForTerminate();
		strbuf.delete(0, strbuf.length());
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(file));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				strbuf.append(inputLine);
				strbuf.append("\n");
			}
		} catch (FileNotFoundException e) {
			logln(e.toString() + "; terminating.");
			throw e;
		} catch (Exception e) {
			throw e;
		} finally {
			if (in != null) {
				in.close();
				in = null;
			}
		}
		checkForTerminate();
	}

	private void readTextFileFromURL(String urlString, StringBuffer strbuf)
			throws FileNotFoundException, UnknownHostException,
			NoRouteToHostException, Exception {
		readTextFileFromURL(urlString, strbuf, true);
	}

	private void readTextFileFromURL(String urlString, StringBuffer strbuf,
			boolean reportFileNotFound) throws FileNotFoundException,
			UnknownHostException, NoRouteToHostException, Exception {
		boolean keepTrying = true;
		int tryNumber = 1;
		strbuf.delete(0, strbuf.length());
		URL url = new URL(urlString);
		BufferedReader in = null;
		while (keepTrying) {
			checkForTerminate();
			try {
				in = new BufferedReader(new InputStreamReader(url.openStream()));
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					checkForTerminate();
					strbuf.append(inputLine);
					// TODO just added this - is it safe
					strbuf.append("\n");		
				}
				keepTrying = false;
			} catch (FileNotFoundException e) {
				if (reportFileNotFound) {
					logln(e.toString() + "; terminating.");
				}
				throw e;
			} catch (UnknownHostException e) {
				logln(e.toString() + "; terminating.");
				throw e;
			} catch (NoRouteToHostException e) {
				logln(e.toString() + "; terminating.");
				throw e;
			} catch (Exception e) {
				if (tryNumber < 4) {
					logln(e.toString() + "; retrying...");
					tryNumber++;
				} else {
					logln("Tried " + tryNumber + " times; terminating.");
					keepTrying = false;
					numErrors++;
					throw e;
				}
			} finally {
				if (in != null) {
					in.close();
					in = null;
				}
			}
		}
		Thread.sleep(UpdateTask.updateThreadSleepTime);
	}
	
	void updateLocalRawFromURL(URL srcUrl) throws InvalidDownloadException, Exception {
		updateLocalRawFromURL(srcUrl, null);
	}
	
	/**
	 * 
	 * @param srcUrl
	 * @param dstCameraCode this is a bit of a hack, if it is null we get the camera code 
	 * as normal from the filename, if it is set we use this dest camera code and if it is
	 * longer than one char then we write to the generated images dir... 
	 * this is temporary for the support of DC pancams... TODO fix this...
	 * @throws InvalidDownloadException
	 * @throws Exception
	 */
	void updateLocalRawFromURL(URL srcUrl, String dstCameraCode) throws InvalidDownloadException, Exception {
		int lastSlash = srcUrl.getFile().lastIndexOf("/");
		String filename = srcUrl.getFile().substring(lastSlash + 1);
		try {
			filename = URLDecoder.decode(filename, "UTF-8");
		}
		catch (Exception e) {
			System.err.println("Error decoding filename: "+e.toString());
		}
		String roverCode = MerUtils.roverCodeFromFilename(filename);
		if (dstCameraCode==null) {
			dstCameraCode = MerUtils.cameraCodeFromFilename(filename);
		}
		int sol = MerUtils.solFromFilename(filename);
		String dstFilename = filename;
		if (filename.length() < 18) {
			debugln("ERROR "+filename+" is too short");
			return;
		}
		if (filename.substring(16,18).equals("__")) {
			dstFilename = filename.substring(0,16)+"##"+filename.substring(18);
		}		
		String localPathname = MerUtils.localPathnameFrom(roverCode,
				dstCameraCode, sol, dstFilename);
		File imageDir = null;
		if (dstCameraCode.length() == 1) { 
			imageDir = workspace.getRawImageDir();
		}
		else {
			imageDir = workspace.getGeneratedImageDir();
		}
		File saveFile = new File(imageDir, localPathname);

		// TODO TEST to see if this is faster
//		if (saveFile.exists()) {
//			logFrame.logln("file already downloaded; not checking last modified time.");
//			return;
//		}
		boolean exists = saveFile.exists();
		if (!exists) {
			numNewImages++;
		}
		if (params.mode == UpdateParams.MODE_CHECK) {
			return;
		}
		if (params.fastUpdate && exists) {
			debugln(localPathname+"skipped; file already exists locally (fast update). ");
			return;
		}
		
		log(localPathname);
		log(" ");
		
		try {
			boolean downloaded = updateFileFromURL(saveFile, srcUrl, true, true);
			
			if (downloaded) {
				debug("adding to image index... ");
				// add file to image index
				workspace.getImageIndex().addFileToImageIndex(imageDir, localPathname);
		
				//updatedSols.add(new Integer(sol));
				if (dstCameraCode.length() == 1) {
					updatedRawImageFilenames.add(saveFile.getName());
					this.updatedRawImageSols.add(new Integer(sol));
				}
				else {
					this.updatedGeneratedImageFilenames.add(saveFile.getName());
					this.updatedGeneratedImageSols.add(new Integer(sol));					
				}
		
				addDownloadedUrl(filename, srcUrl.toString());
		
				// show the file
				ImageData imageData = new ImageData(saveFile.getAbsolutePath());
				setViewImage(imageData, "Update in Progress: "
						+ MerUtils.displayRoverNameFromRoverCode(roverCode) + " Sol " + sol + " "
						+ filename, roverCode, sol, filename);
								
				numImagesDownloaded++;
			}
			logln();
		}
		catch (InvalidDownloadException e) {
			logln("ERROR Invalid Download: "+e.getMessage());
		}
		catch (Exception e) {
			// 6/17/2005: we'll just report errors downloading individual files,
			// instead of terminating the update. Errors downloading 
			// index pages are still fatal.
			logln("ERROR: "+e.getMessage());			
		}
	}
	
	void doUpdatePhoenixImages() {
		final String rootURL = "http://phoenix.lpl.arizona.edu/images/gallery/";		
		boolean done = false;
		int index = 300;		
		int missCount = 0;
		final int maxMissCount = 20;
		int imagesDownloaded = 0;
		int maxContentSize = -1;
		
		File imageDir = new File(workspace.getWorkspaceDir(), "Phoenix"+File.separator+"jpg");
		if (!imageDir.exists()) {
			imageDir.mkdirs();
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HHmm", Locale.US);
		String dateStr = dateFormat.format(new Date());
		String inboxDirStr = "inbox"+File.separator+dateStr;
		File inboxDir = new File(workspace.getWorkspaceDir(), "Phoenix"+File.separator+inboxDirStr);
		
		File renamedDir = new File(workspace.getWorkspaceDir(), "Phoenix"+File.separator+"renamed");
		if (!renamedDir.exists()) {
			renamedDir.mkdirs();
		}
		
		if (params.fastUpdate) {
			logln("Fast Scanning for new Phoenix images at "+rootURL);
			logln("Images larger than 500K will be ignored.");
			maxContentSize = 1024*500;			
			index = 300;
			try {
				String [] existingFiles = imageDir.list();
				for (int i=0; i<existingFiles.length; i++) {
					String fn = existingFiles[i];
					if (fn.startsWith("lg_") && fn.endsWith(".jpg")) {
						int val = Integer.valueOf(fn.substring(3, fn.length()-4)).intValue();
						if (val > index)
							index = val;
					}
				}
			}
			catch (Exception e) {			
			}
		}
		else {
			logln("Full Scan for new Phoenix images at "+rootURL);
		}
		
		logln("All downloaded images are stored in directory "+imageDir+" (do not touch these images)");
		logln("Images are also renamed and stored in directory "+renamedDir+" (these images may be discarded if desired)");
		logln("Any new images during this update will also be renamed and copied to directory "+inboxDir+ " (these images may be discarded if desired)");
		
		while (!done) {
			try {
				index++;
				String filename = "lg_"+index+".jpg";
				String downloadURL = rootURL+filename;
				File saveFile = new File(imageDir, filename);
				log(filename);
				log(" ");
				boolean downloaded = updateFileFromURL(saveFile, new URL(downloadURL), true, true, maxContentSize);
				if (downloaded) {
					if (!inboxDir.exists()) {
						inboxDir.mkdirs();
					}
					PhoenixMetadataEntry metadata = getPhoenixMetadataFromJpg(saveFile, index);
					if (metadata != null && metadata.productId != null & metadata.productId.length() > 0) {
						String newFilename = metadata.productId+".jpg";
						log(" Copying to renamed/"+newFilename);
						File renamedFile = new File(renamedDir, newFilename);
						copyFile(saveFile, renamedFile);
						log(".  Copying to "+inboxDirStr+File.separator+newFilename);
						File inboxFile = new File(inboxDir, newFilename);
						copyFile(saveFile, inboxFile);
					}
					else {
						log(" Copying to "+inboxDirStr+File.separator+filename);
						File inboxFile = new File(inboxDir, filename);
						copyFile(saveFile, inboxFile);
					}
					imagesDownloaded++;
				}
				missCount = 0;
				logln();
			}
			catch (InvalidDownloadException e) {
				logln(e.getMessage());
				missCount++;
				if (missCount > maxMissCount) {
					logln("Scanned for "+missCount+" consecutive images without finding any; terminating.");
					done = true;
				}
			}
			catch (Exception e) {
				logln("ERROR: "+e.getMessage());			
				done = true;
			}
		}

		logln(""+imagesDownloaded+" images downloaded.");
		
		indexPhoenixMetadata(imageDir, index+1);
	}
		
	class PhoenixMetadataEntry implements Comparable {
		String productId;
		int sourceImageNumber = 0;
		String frameId = "";
		String frameType = "";
		String instrumentName = "";
		String localTrueSolarTime = "";
		String releaseId = "";
		String solarLongitude = "";
		String filterName = "";
		String exposureDuration = "";
		String instrumentAzimuth = "";
		String instrumentElevation = "";
		String planetDayNumber = "";
		int fileNumber = 0;
		
		public int compareTo(Object arg0) {
			PhoenixMetadataEntry entry = (PhoenixMetadataEntry) arg0;
			return productId.compareTo(entry.productId);
		}		
	}
	
	void indexPhoenixMetadata(File imageDir, int lastFileNumber) {
		logln("Scanning for metadata in JPG files in "+imageDir.getAbsolutePath());
		// Go through files in order; this was the easiest way
		HashMap metadata = new HashMap();
		for (int n=0; n<lastFileNumber; n++) {
			File file = new File(imageDir, "lg_"+n+".jpg");
			if (file.isFile()) {
				PhoenixMetadataEntry entry = getPhoenixMetadataFromJpg(file, n);
				if (entry != null && entry.productId != null) {
					metadata.put(entry.productId, entry);					
				}
			}
		}
		PhoenixMetadataEntry[] array = new PhoenixMetadataEntry[metadata.size()];
		array = (PhoenixMetadataEntry[]) metadata.values().toArray(array);
		Arrays.sort(array);
		
		File csvFile = new File(workspace.getWorkspaceDir(), "Phoenix"+File.separator+"metadata.csv");
		logln("Writing metadata to file "+csvFile);
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile));
			String NL = System.getProperty("line.separator");
			writer.write("PRODUCT_ID, source image number, PLANET_DAY_NUMBER, LOCAL_TRUE_SOLAR_TIME, INSTRUMENT_AZIMUTH, INSTRUMENT_ELEVATION, FILTER_NAME, FRAME_ID, FRAME_TYPE, INSTRUMENT_NAME, RELEASE_ID, SOLAR_LONGITUDE, EXPOSURE_DURATION");			
			writer.write(NL);
			for (int n=0; n<array.length; n++) {
				PhoenixMetadataEntry entry = array[n];
				writer.write(entry.productId);
				writer.write(",");
				writer.write(""+entry.sourceImageNumber);
				writer.write(",");
				writer.write(entry.planetDayNumber);
				writer.write(",");
				writer.write(entry.localTrueSolarTime);
				writer.write(",");
				writer.write(entry.instrumentAzimuth);
				writer.write(",");
				writer.write(entry.instrumentElevation);
				writer.write(",");
				writer.write(entry.filterName);
				writer.write(",");
				writer.write(entry.frameId);
				writer.write(",");
				writer.write(entry.frameType);
				writer.write(",");
				writer.write(entry.instrumentName);
				writer.write(",");
				writer.write(entry.releaseId);
				writer.write(",");
				writer.write(entry.solarLongitude);
				writer.write(",");
				writer.write(entry.exposureDuration);
				writer.write(NL);
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	PhoenixMetadataEntry getPhoenixMetadataFromJpg(File file, int imageNumber) {
		int fileLength = (int) file.length();
		byte[] bytes = new byte[fileLength];
		try {
			FileInputStream fis = new FileInputStream(file);
			fis.read(bytes);
			fis.close();
			int pos = 20;
			if (unsignedByteToInt(bytes[pos++]) != 0xFF || unsignedByteToInt(bytes[pos++]) != 0xFE) {
				debugln("File did not have expected header (1): "+file.getAbsolutePath());
				return null;
			}
			int segLength = unsignedByteToInt(bytes[pos]) * 256 + unsignedByteToInt(bytes[pos+1]);
			pos=pos+segLength;
			if (unsignedByteToInt(bytes[pos++]) != 0xFF || unsignedByteToInt(bytes[pos++]) != 0xFE) {
				debugln("File did not have expected header (2): "+file.getAbsolutePath());
				return null;
			}					
			segLength = unsignedByteToInt(bytes[pos]) * 256 + unsignedByteToInt(bytes[pos+1]);					
			String commentString = new String(bytes, pos+2, segLength-2);
			if (!commentString.startsWith("PRODUCT_ID")) {
				logln("File comment did not start with PRODUCT_ID: "+file.getAbsolutePath());
				return null;
			}
			BufferedReader reader = new BufferedReader(new StringReader(commentString.toString()));
			String inputLine;
			PhoenixMetadataEntry entry = new PhoenixMetadataEntry();
			entry.sourceImageNumber = imageNumber;
			while ((inputLine = reader.readLine()) != null) {
				if (inputLine.startsWith("PRODUCT_ID")) {
					entry.productId = phoenixMetadataStringValueFromLine(inputLine);
				}
				else if (inputLine.startsWith("FRAME_ID")) {
					entry.frameId = phoenixMetadataStringValueFromLine(inputLine);
				}
				else if (inputLine.startsWith("FRAME_TYPE")) {
					entry.frameType = phoenixMetadataStringValueFromLine(inputLine);
				}
				else if (inputLine.startsWith("INSTRUMENT_NAME")) {
					entry.instrumentName = phoenixMetadataStringValueFromLine(inputLine);
				}
				else if (inputLine.startsWith("LOCAL_TRUE_SOLAR_TIME")) {
					entry.localTrueSolarTime = phoenixMetadataStringValueFromLine(inputLine);
				}
				else if (inputLine.startsWith("RELEASE_ID")) {
					entry.releaseId = phoenixMetadataStringValueFromLine(inputLine);
				}
				else if (inputLine.startsWith("SOLAR_LONGITUDE")) {
					entry.solarLongitude = phoenixMetadataStringValueFromLine(inputLine);
				}
				else if (inputLine.startsWith("FILTER_NAME")) {
					entry.filterName = phoenixMetadataStringValueFromLine(inputLine);
				}
				else if (inputLine.startsWith("EXPOSURE_DURATION")) {
					entry.exposureDuration = phoenixMetadataStringValueFromLine(inputLine);
				}
				else if (inputLine.startsWith("INSTRUMENT_AZIMUTH")) {
					entry.instrumentAzimuth = phoenixMetadataStringValueFromLine(inputLine);
				}
				else if (inputLine.startsWith("INSTRUMENT_ELEVATION")) {
					entry.instrumentElevation = phoenixMetadataStringValueFromLine(inputLine);
				}
				else if (inputLine.startsWith("PLANET_DAY_NUMBER")) {
					entry.planetDayNumber = phoenixMetadataStringValueFromLine(inputLine);
				}
			}
			return entry;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return null;
	}
	
	static String phoenixMetadataStringValueFromLine(String line) {
		String value = null;
		int i = line.indexOf("=");
		if (i >= 0) {
			value = line.substring(i+1).trim();
			if (value.startsWith("\"") || value.endsWith("\"")) {
				value = value.substring(1, value.length()-1);
			}
		}
		return value;
	}
	
	static int unsignedByteToInt(byte b) {
		return ((int)b) & 0x00FF;
	}
	
	/**
	 * 
	 */
	void updateLocalImgFromURL(URL srcUrl) throws InvalidDownloadException, Exception {
		int lastSlash = srcUrl.getFile().lastIndexOf("/");
		String filename = srcUrl.getFile().substring(lastSlash + 1);
		try {
			filename = URLDecoder.decode(filename, "UTF-8");
		}
		catch (Exception e) {
			System.err.println("Error decoding filename: "+e.toString());
		}
		String roverCode = MerUtils.roverCodeFromFilename(filename);
		String dstCameraCode = MerUtils.cameraCodeFromFilename(filename);
		int sol = MerUtils.solFromFilename(filename);
		String dstFilename = filename;
		if (filename.length() < 18) {
			debugln("ERROR "+filename+" is too short");
			return;
		}
		if (filename.substring(16,18).equals("__")) {
			dstFilename = filename.substring(0,16)+"##"+filename.substring(18);
		}		
		String localPathname = MerUtils.localPathnameFrom(roverCode,
				dstCameraCode, sol, dstFilename);
		File imageDir = null;
		imageDir = workspace.getImgDir();
		File saveFile = new File(imageDir, localPathname);

		boolean exists = saveFile.exists();
		if (!exists) {
			numNewImages++;
		}
		if (params.mode == UpdateParams.MODE_CHECK) {
			return;
		}
		if (params.fastUpdate && exists) {
			debugln(localPathname+"skipped; file already exists locally (fast update). ");
			return;
		}
		
		log(localPathname);
		log(" ");
		
		try {
			boolean downloaded = updateFileFromURL(saveFile, srcUrl, true, true);
			
			if (downloaded) {
				debug("adding to image index... ");
				// add file to image index
				workspace.getImageIndex().addFileToImageIndex(imageDir, localPathname);
		
				//updatedSols.add(new Integer(sol));
				if (dstCameraCode.length() == 1) {
					updatedRawImageFilenames.add(saveFile.getName());
					this.updatedRawImageSols.add(new Integer(sol));
				}
				else {
					this.updatedGeneratedImageFilenames.add(saveFile.getName());
					this.updatedGeneratedImageSols.add(new Integer(sol));					
				}
		
				addDownloadedUrl(filename, srcUrl.toString());
		
				// show the file
				// TODO need some common code
				PDSIMG img = PDSIMG.readIMGFile(saveFile.getAbsolutePath(), 200, 5000);
				byte[] imageBytes = img.getImageByteArray();
				PaletteData palette = new PaletteData(0xFF , 0xFF , 0xFF);
				ImageData imageData = new ImageData(img.getLineSamples(),img.getLines(),8,palette,1,imageBytes);				
				
				setViewImage(imageData, "Update in Progress: "
						+ MerUtils.displayRoverNameFromRoverCode(roverCode) + " Sol " + sol + " "
						+ filename, roverCode, sol, filename);

				numImagesDownloaded++;
			}
			logln();
		}
		catch (InvalidDownloadException e) {
			logln("ERROR Invalid Download: "+e.getMessage());
		}
		catch (Exception e) {
			// 6/17/2005: we'll just report errors downloading individual files,
			// instead of terminating the update. Errors downloading 
			// index pages are still fatal.
			logln("ERROR: "+e.getMessage());			
		}
	}
	
	private void addDownloadedUrl(String filename, String string) {
		// TODO Auto-generated method stub
		
	}

	boolean updateFileFromURL(File saveFile, URL srcUrl, boolean skipSameSize, boolean skipLaterMod) throws InvalidDownloadException, Exception {
		return updateFileFromURL(saveFile, srcUrl, skipSameSize, skipLaterMod, -1);
	}	
	
	boolean updateFileFromURL(File saveFile, URL srcUrl, boolean skipSameSize, boolean skipLaterMod, int maxLength) throws InvalidDownloadException, Exception {
		boolean keepTrying = true;
		int tryNumber = 1;
		byte[] byteArray = null;
		long lastModified = 0;
		BufferedInputStream bis = null;
		HttpURLConnection connection = null;

		//System.out.println("updateFileFromURL "+saveFile.getName());
		while (keepTrying) {
			checkForTerminate();
			try {
				connection = (HttpURLConnection) srcUrl.openConnection();
				connection.connect();
				lastModified = connection.getLastModified();
				if (lastModified <= 0) {
					this.numErrors++;
					throw new InvalidDownloadException("Not found (last modified time not available).");
				}
				//System.out.println("remote lastModified = "+lastModified);

				int contentLength = connection.getContentLength();
				if (contentLength <= 0) {
					numErrors++;
					throw new InvalidDownloadException("Not found (content length not available: "+contentLength+")");
				}
				
				if (maxLength > 0 && contentLength > maxLength) {
					log("skipped: content length exceeds maximum for fast download; use full download to get this file.");
					return false;
				}
				
				File saveFileDirectory = saveFile.getParentFile();
				if (!saveFileDirectory.exists()) {
					boolean madeDirs = saveFileDirectory.mkdirs();
					if (!madeDirs) {
						logln("ERROR");
						numErrors++;
						throw new Error("Could not make directory for " + saveFileDirectory.getAbsolutePath());
					}
				}

				if (saveFile.exists()) {					
					long localLastModified = saveFile.lastModified();
					//System.out.println("local lastModified = "+localLastModified);
					long localFileLength = saveFile.length();
										
					if ((localLastModified/1000) == (lastModified/1000)) {
						//if (showUnupdatedFiles)
						//	showFile(saveFile);
						log("skipped; same last modified time.");
						return false;
					} 
					if ((skipSameSize) && ((int)localFileLength == contentLength)) {
						log("skipped; local copy has same file size.");
						return false;
					}
					if ((skipLaterMod) && ((localLastModified/1000) > (lastModified/1000))) {
						//if (showUnupdatedFiles)
						//	showFile(saveFile);
						log("skipped; local copy has later last modified time.");
						debugln("\n   local time = "
								+ new Date(localLastModified)
								+ "; remote time = " + new Date(lastModified));
						return false;
					}
					log("downloading new version... ");
				} else {
					log("downloading... ");
				}

				byteArray = new byte[contentLength];
				try {
					bis = new BufferedInputStream(connection.getInputStream());
				}
				catch (IOException e) {
					numErrors++;
					throw new InvalidDownloadException("Could not open stream; IOException.");	
				}
				boolean done = false;
				int position = 0;
				debug("reading... ");
				while (!done) {
					checkForTerminate();
					//LogFrame.getInstance().debug("r");
					int lengthRead = bis.read(byteArray, position,
							contentLength - position);
					if (lengthRead < 0)
						done = true;
					else {
						if (lengthRead <= 0)
							debugln("lengthRead="+lengthRead+" ");
						position += lengthRead;
						if (position == contentLength) {
							done = true;
						}
					}
				}
				debug("done reading... ");
				if (position != contentLength) {
					numErrors++;
					throw new InvalidDownloadException("Read length " + position
							+ "did not equal content length " + contentLength);
				}
				keepTrying = false;
			} catch (InvalidDownloadException e) {
				throw e;
			} catch (Exception e) {
				if (tryNumber < 4) {
					logln("Exception: " + e + "; retrying...");
					tryNumber++;
				} else {
					keepTrying = false;
					logln(e.toString());
					logln("Tried " + tryNumber
							+ " times; terminating.");
					e.printStackTrace();
					numErrors++;
					throw e;
				}
			} finally {
				if (connection != null) {
					connection.disconnect();
					connection = null;
				}
				if (bis != null) {
					debug("closing... ");				
					bis.close();
					bis = null;
				}
			}
		}

		// save file
		try {
			debug("saving... ");
			FileOutputStream fileOut = new FileOutputStream(saveFile);
			fileOut.write(byteArray);
			fileOut.flush();
			fileOut.close();
			saveFile.setLastModified(lastModified);
			log("downloaded. ");
		}
		catch (Exception e) {
			numErrors++;
			e.printStackTrace();
			throw e;	
		}
		Thread.sleep(UpdateTask.updateThreadSleepTime);
		return true;
	}

	/**
	 * Similar to updateFileFromURL, but just checks to see if a newer file is out there.
	 */
	boolean checkFileFromURL(File saveFile, URL srcUrl, boolean checkFileSize) throws InvalidDownloadException, Exception {
		boolean keepTrying = true;
		int tryNumber = 1;
		long lastModified = 0;
		HttpURLConnection connection = null;

		while (keepTrying) {
			checkForTerminate();
			try {
				connection = (HttpURLConnection) srcUrl.openConnection();
				connection.connect();
				lastModified = connection.getLastModified();
				if (lastModified <= 0) {
					this.numErrors++;
					throw new InvalidDownloadException("Last modified time not available.");
				}

				int contentLength = connection.getContentLength();
				if (contentLength <= 0) {
					numErrors++;
					throw new InvalidDownloadException("Content length not available ("+contentLength+").");	
				}
				
				if (saveFile.exists()) {
					long localLastModified = saveFile.lastModified();
					long localFileLength = saveFile.length();
					if (localLastModified == lastModified) {
						//if (showUnupdatedFiles)
						//	showFile(saveFile);
						log("skipped; same last modified time.");
						return false;
					} 
					if ((checkFileSize) && ((int)localFileLength == contentLength)) {
						log("skipped; local copy has same file size.");
						return false;
					}
					if (localLastModified > lastModified) {
						//if (showUnupdatedFiles)
						//	showFile(saveFile);
						log("skipped; local copy has later last modified time.");
						debugln("\n   local time = "
								+ new Date(localLastModified)
								+ "; remote time = " + new Date(lastModified));
						return false;
					}
					log("newer version exists. ");
				} else {
					log("newer version exists. ");
				}
				keepTrying = false;
			} catch (InvalidDownloadException e) {
				throw e;
			} catch (Exception e) {
				if (tryNumber < 4) {
					logln("Exception: " + e + "; retrying...");
					tryNumber++;
				} else {
					keepTrying = false;
					logln(e.toString());
					logln("Tried " + tryNumber
							+ " times; terminating.");
					e.printStackTrace();
					numErrors++;
					throw e;
				}
			}
			finally {
				if (connection != null) {
					connection.disconnect();
					connection = null;
				}
			}
		}
		Thread.sleep(UpdateTask.updateThreadSleepTime);
		return true;
	}	
	
	
	/**
	 * This just reads the file from the URL in binary mode, without checking mod time.
	 * Also allows us to specify maximum download length, so we can download just the 
	 * headers of IMG files (and save lots of disk space and time).
	 */
	boolean updateFileFromURLNoModTime(File saveFile, URL srcUrl, boolean checkFileSize,
			int maxLength, int minLength) throws InvalidDownloadException, Exception {
		boolean keepTrying = true;
		int tryNumber = 1;
		byte[] byteArray = null;
		BufferedInputStream bis = null;
		HttpURLConnection connection = null;

		while (keepTrying) {
			checkForTerminate();
			try {
				connection = (HttpURLConnection) srcUrl.openConnection();
				connection.connect();

				int contentLength = connection.getContentLength();
				if (contentLength <= 0) {
					numErrors++;
					throw new InvalidDownloadException("Content length not available ("+contentLength+")");	
				}
				
				if (contentLength < minLength) {
					numErrors++;
					throw new InvalidDownloadException("Content length is "+contentLength+"; probable invalid file.");
				}
				
				if ((maxLength > 0) && (contentLength > maxLength)) {
					contentLength = maxLength; 
				}
				
				File saveFileDirectory = saveFile.getParentFile();
				if (!saveFileDirectory.exists()) {
					boolean madeDirs = saveFileDirectory.mkdirs();
					if (!madeDirs) {
						logln("ERROR");
						numErrors++;
						throw new Error("Could not make directory for " + saveFileDirectory.getAbsolutePath());
					}
				}

				if (saveFile.exists()) {
					long localFileLength = saveFile.length();
					if ((checkFileSize) && ((int)localFileLength == contentLength)) {
						log("skipped; local copy has same file size.");
						return false;
					}
					log("downloading version with different file size... ");
				} else {
					log("downloading... ");
				}

				byteArray = new byte[contentLength];
				try {
					bis = new BufferedInputStream(connection.getInputStream());
				}
				catch (IOException e) {
					numErrors++;
					throw new InvalidDownloadException("Could not open stream; IOException.");	
				}
				boolean done = false;
				int position = 0;
				debug("reading... ");
				while (!done) {
					checkForTerminate();
					//LogFrame.getInstance().debug("r");
					int lengthRead = bis.read(byteArray, position,
							contentLength - position);
					if (lengthRead < 0)
						done = true;
					else {
						if (lengthRead <= 0)
							debugln("lengthRead="+lengthRead+" ");
						position += lengthRead;
						if (position == contentLength) {
							done = true;
						}
					}
				}
				debug("done reading... ");
				if (position != contentLength) {
					numErrors++;
					throw new InvalidDownloadException("Read length " + position
							+ "did not equal content length " + contentLength);
				}
				keepTrying = false;
			} catch (InvalidDownloadException e) {
				throw e;
			} catch (Exception e) {
				if (tryNumber < 4) {
					logln("Exception: " + e + "; retrying...");
					tryNumber++;
				} else {
					keepTrying = false;
					logln(e.toString());
					logln("Tried " + tryNumber
							+ " times; terminating.");
					e.printStackTrace();
					numErrors++;
					throw e;
				}
			} finally {
				if (connection != null) {
					connection.disconnect();
					connection = null;
				}
				if (bis != null) {
					debug("closing... ");				
					bis.close();
					bis = null;
				}
			}
		}

		// save file
		try {
			debug("saving... ");
			FileOutputStream fileOut = new FileOutputStream(saveFile);
			fileOut.write(byteArray);
			fileOut.flush();
			fileOut.close();
			log("downloaded. ");
		}
		catch (Exception e) {
			numErrors++;
			e.printStackTrace();
			throw e;	
		}
		Thread.sleep(UpdateTask.updateThreadSleepTime);
		return true;		
	}
	
	private void doExportPanImagesForPTGui() {
		logln(new Date().toString());
		
		if (!params.tgtDir.exists()) {
			params.tgtDir.mkdirs();
		}
		if (params.cleanMovieDir) {
			logln("Cleaning up "+params.tgtDir.getAbsolutePath());
			if (!cleanMovieDir(params.tgtDir)) {
				logln("Export movie frames terminated; could not clean directory.");
				return;
			}
		}
		logln("Exporting pan images to "+params.tgtDir.getAbsolutePath());
		
//		int[] array = new int[1024 * 1024];
		
		logln("Exporting PTGui project file");
		File projectFile = new File(params.tgtDir, "mmb.pts");
		FileWriter out = null;
		
		PanExportImageEntry[] panExportImageList = params.panExportImageList;
		
		boolean hasNavcam = false;
		boolean hasPancam = false;
		for (int n=0; n<panExportImageList.length; n++) {
			PanExportImageEntry entry = panExportImageList[n];
			if ((entry.imageListEntry.getFile() != null)
					&& entry.imageListEntry.enabled) {
				if (entry.imageListEntry.getImageCategory().startsWith("n")) {
					hasNavcam = true;
				}
				else if (entry.imageListEntry.getImageCategory().startsWith("p")) {
					hasPancam = true;
				}
			}
		}
		
		if ((!hasNavcam) && (!hasPancam)) {
			logln("ERROR no images");
			numErrors++;
		}
		if ((hasNavcam) && (hasPancam)) {
			logln("ERROR cannot generate PTGui project for both Navcam and Pancam images; exporting only Navcam.");
			numErrors++;
			hasPancam = false;
		}

		try {
			String NL = "\r\n";
			out = new FileWriter(projectFile);
			out.write("# ptGui project file"+NL);
			out.write(NL);
			out.write("#-fileversion 11"+NL);
			out.write("#-vfov 180"+NL);
			out.write(NL);
			out.write("# Panorama settings:"+NL);
			out.write("p w2828 h1414 f2 v360 u20 n\"JPEG g0 q90\""+NL);
			out.write("m g1 i0 f0");
			out.write(NL);			
			out.write("# input images:"+NL);
			out.write("#-dummyimage"+NL);
			out.write("# The following line contains a 'dummy image' containing some global parameters for the project"+NL);
//			if (hasNavcam) {
//				out.write("o w1 h1 y0 r0 p0 v45.5 a0 b0 c0 f0 d0 e0 g0 t0"+NL);
//			}
//			else {
//				out.write("o w1 h1 y0 r0 p0 v16.0 a0 b0 c0 f0 d0 e0 g0 t0"+NL);
//			}
			
			// TODO find field of view of correct image type
			out.write("o w1 h1 y0 r0 p0 v"+panExportImageList[0].fov+" a0 b0 c0 f0 d0 e0 g0 t0"+NL);
			
			for (int n=0; n<panExportImageList.length; n++) {
				PanExportImageEntry entry = panExportImageList[n];
				ImageEntry imageListEntry = entry.imageListEntry;
				if ((imageListEntry.getFile() != null) 
						&& imageListEntry.enabled) {
//					File srcFile = element.imagePathnameFile();
					String filename = null;
					if (params.applyImageAdjustments && (imageListEntry.getImageCategory().length()==1) ) {
						logln("Reading image "+imageListEntry.getFile()+"... ");
						try {
							checkForTerminate();														
					        ImageData imageData = new ImageData(imageListEntry.getFile());
							logln("   adjusting... ");
					        ImageStretchMetadataEntry stretch = workspace.getImageStretchMetadata().getEntry(imageListEntry.getFilename());
							if (stretch != null) {
								ImageUtils.destretchGrayscaleImageData(imageData, stretch.minVal, stretch.maxVal, stretch.devigA, stretch.devigB);
							}
							filename = imageListEntry.getFilename();
							// TODO PNG support not working in Eclipse...
							//filename = filename.substring(0, filename.length()-4) + ".png";
							File dstFile = new File(params.tgtDir, filename);
							checkForTerminate();
							log("   saving "+dstFile.getAbsolutePath()+"... ");
							ImageLoader imageLoader = new ImageLoader();
							imageLoader.data = new ImageData[] {imageData};
							//imageLoader.save(dstFile.getAbsolutePath(), SWT.IMAGE_PNG);							
							imageLoader.save(dstFile.getAbsolutePath(), SWT.IMAGE_JPEG);							
							numImagesCopied++;
							totalNumImagesCopied++;
							logln("done.");
						}
						catch (Exception e) {
							logln("ERROR "+e.toString());
							numErrors++;
							numImagesSkipped++;
							totalNumImagesSkipped++;
						}
					}
					else {
						logln("Copying "+imageListEntry.getFile());
						filename = imageListEntry.getFilename();
						File dstFile = new File(params.tgtDir, filename);
						try {
							checkForTerminate();
							copyFile(new File(imageListEntry.getFile()), dstFile);
							numImagesCopied++;
							totalNumImagesCopied++;
						}
						catch (Exception e) {
							logln("ERROR "+e.toString());
							numErrors++;
							numImagesSkipped++;
							totalNumImagesSkipped++;
						}
					}
					
					// Can't support subframe images for PTGui - or at least don't know how to at the moment
					if (imageListEntry.getImageMetadataEntry().n_lines != 1024)
						continue;
					
					if ((imageListEntry.getImageCategory().startsWith("p")) && (!hasPancam))
						continue;
				
					String line = "#-imgfile "+imageListEntry.getImageMetadataEntry().n_line_samples+" "+imageListEntry.getImageMetadataEntry().n_lines+" \""+filename+"\""+NL;
					out.write(line);
					
					line = "o f0 y"+(entry.yaw) 
						+" r"+(entry.roll)
						+" p"+(entry.pitch)
						+" v=0"
						+" a=0 b=0 c=0 d=0 e=0 g=0 t=0"
						+NL;
					out.write(line);
				}
			}

/*			
			// create stamp image
			if (panText != null) {
				try {
					StringTokenizer toks = new StringTokenizer(panText, "\n\r");
					int numToks = toks.countTokens();
					BufferedImage image = new BufferedImage(1024, 1024, BufferedImage.TYPE_INT_RGB);				
					Graphics2D graphics = image.createGraphics();
					Font font = new Font("SansSerif", Font.BOLD, 15);
					graphics.setFont(font);
					FontMetrics metrics = graphics.getFontMetrics();
					int fontHeight = metrics.getHeight();
					int charWidth = metrics.charWidth('w');
					int posX = image.getWidth() / 2 - 15 * charWidth;
					if (posX < 0)
						posX = 0;
					int posY = image.getHeight() / 2 - numToks/2*fontHeight;					
					while (toks.hasMoreTokens()) {
						String tok = toks.nextToken();
						graphics.drawString(tok, posX, posY);
						posY += fontHeight;
					}
					// compass
					font = new Font("SansSerif", Font.BOLD, 20);
					graphics.setFont(font);
					metrics = graphics.getFontMetrics();
					fontHeight = metrics.getHeight();
					charWidth = metrics.charWidth('w');
					int rad = 450;
					graphics.drawString("N", 512-charWidth/2, 512+rad);
					graphics.drawString("S", 512-charWidth/2, 512-rad);
					graphics.drawString("E", 512+rad, 512-fontHeight/2);
					graphics.drawString("W", 512-rad, 512-fontHeight/2);
					
					ImageIO.write(image, "png", new File(params.tgtDir, "stamp.png"));
					
					String line = "#-imgfile 1024 1024 \"stamp.png\""+NL;
					out.write(line);
					line = "o f0 y0 r0 p90 v=0"
						+" a=0 b=0 c=0 d=0 e=0 g=0 t=0"
						+NL;
					out.write(line);
				} catch (Exception e) {
					e.printStackTrace();
					System.err.println("Error painting caption: " + e.toString());
				}
			}
*/			
			logln("Total "+totalNumImagesCopied+" images exported; "+numErrors+" errors.");
		}
		catch (Exception e) {
			e.printStackTrace();
			logln("ERROR "+e.toString());
			numErrors++;
		}
		finally {
			try {
				out.close();
			} catch (IOException e) {
				System.err.println(e.toString());
			}
		}
		
/* 	
# input images:
#-dummyimage
# The following line contains a 'dummy image' containing some global parameters for the project
o w1 h1 y0 r0 p0 v34.9999805996076 a0 b0 c0 f0 d0 e0 g0 t0
#-imgfile 1000 1000 "auto_01.jpg"
o f0 y0 r0 p0 v=0 a=0 b=0 c=0 d=0 e=0 g=0 t=0
*/
		
		logln(new Date().toString());
		logln("Done exporting pan images.");	
	}
		
	/**
	 * Clean up all files in the directory with names in the format number.jpg, .JPG, .png, etc.
	 * Returns true if directory successfully cleaned, false if a file could not be deleted.
	 * @param dir
	 * @return
	 */
	private boolean cleanMovieDir(File dir) {
		File[] files = dir.listFiles();
		for (int n=0; n<files.length; n++) {
			File file = files[n];
			if (file.isFile()) {
				String filename = file.getName();
				if (filename.endsWith(".jpg") || filename.endsWith(".JPG")
						|| filename.endsWith(".png") || filename.endsWith(".PNG")) {
					try {
						//	String timecodeString = filename.substring(0, filename.length()-4);
						//	int timecode = Integer.parseInt(timecodeString);
						boolean deleted = file.delete();
						if (!deleted) {
							logln("ERROR could not delete file "+file.getAbsolutePath());
							return false;
						}
					}
					catch (Exception e) {						
					}
				}
			}
		}
		return true;
	}
	
	private void copyFile(File srcFile, File dstFile) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(srcFile));
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dstFile));
	    byte[] buf = new byte[1024];
	    int i = 0;
	    while((i=bis.read(buf))!=-1) {
	    		bos.write(buf, 0, i);
	    }
		bis.close();
		bos.flush();
		bos.close();
		dstFile.setLastModified(srcFile.lastModified());
	}
	
	
	public void checkForTerminate() {
		if (terminate) {
			throw new UpdateInterrupt();
		}
	}
	
	private void setViewImage(ImageData imageData, String title, String roverCode, 
			int sol, String pathname) {
		if (!updateViewerOpened) {
			updateViewerOpened = true;
			window.getShell().getDisplay().syncExec(new Runnable() {
				public void run() {
					UpdateViewerEditor.openUpdateViewer(window);
				}});
		}
		Application.getUpdateViewerService().newUpdateImage(imageData, title, roverCode, sol, pathname);
	}

	public void logln(String line) {
		log(line);
		log("\n");
	}
	
	public void logln() {
		log("\n");
	}
	
	public void debug(String line) {
		// TODO ?
	}
	
	public void debugln(String line) {
		// TODO ?
		//logln(line);
	}	

	public void log(String str) {
		Application.getUpdateConsoleService().write(str);
	}

}
