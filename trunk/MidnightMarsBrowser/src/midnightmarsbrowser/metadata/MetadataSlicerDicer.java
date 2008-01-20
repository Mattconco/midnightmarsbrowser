package midnightmarsbrowser.metadata;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;
import java.util.Vector;

import midnightmarsbrowser.application.IUpdateTask;
import midnightmarsbrowser.application.MMBWorkspace;
import midnightmarsbrowser.application.UpdateTask;
import midnightmarsbrowser.model.LocationCounter;
import midnightmarsbrowser.model.MerUtils;

public class MetadataSlicerDicer {
	MMBWorkspace workspace;
	
	IUpdateTask task;
	
	public MetadataSlicerDicer(MMBWorkspace workspace, IUpdateTask task) {
		this.workspace = workspace;
		this.task = task;
	}
	
	public void sliceAndDice() throws Exception {
		processPancamTrackingHeaderLevel();
		processPancamTrackingObservations();
		stageMetadata();		
	}
	
	public void sliceAndDice1x() throws Exception {
		// build legacy 1.x metadata
		task.logln("Generating legacy 1.x pans");
		oldGenPanFiles();
		task.logln("Staging legacy 1.x metadata");
		buildOldMetadataPackages();		
	}
	
	public void processPancamTrackingHeaderLevel() throws Exception {
		ImageMetadata imageMetadata = new ImageMetadata();
		LocationMetadata locationMetadata = new LocationMetadata();
				
		File[] files = workspace.getMetadataDir().listFiles();
		for (int n=0; n<files.length; n++) {
			File file = files[n];
			if ((file.isFile()) && (file.getName().startsWith("HeaderLevelInfo"))
					&& (file.getName().endsWith(".html") || file.getName().endsWith(".htm"))) {
				task.logln(" - reading "+file.getName());
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String line = reader.readLine();
				// first read everything up to "<pre>"
				while (line != null) {
					int preIndex = line.indexOf("<pre>");
					if (preIndex >= 0) {
						line = line.substring(preIndex+5);
						break;
					}
					else {
						line = reader.readLine();
					}
				}				
				// now read lines until line starts with "</pre>"
				while (line != null) {
					task.checkForTerminate();
					if (line.startsWith("</pre>")) {
						break;
					}
					String[] params = line.split("::");
					if (params.length < 13) {
						throw new Exception("Error reading line \""+line+"\"");
					}
					
					int fullPathLength = params[0].length();
					String filename = params[0].substring(fullPathLength-27-4, fullPathLength-4);

					String productType = MerUtils.productTypeFromFilename(filename);
					
					ImageMetadataEntry image = new ImageMetadataEntry(filename);			
					image.inst_az_rover = Double.parseDouble(params[1]);
					image.inst_el_rover = Double.parseDouble(params[2]);
					image.first_line = Integer.parseInt(params[3]);
					image.first_line_sample = Integer.parseInt(params[4]);
					image.n_lines = Integer.parseInt(params[5]);
					image.n_line_samples = Integer.parseInt(params[6]);
					image.pixel_averaging_height = Integer.parseInt(params[7]);
					image.pixel_averaging_width = Integer.parseInt(params[8]);
					
					if (productType.equalsIgnoreCase("EFF") || productType.equalsIgnoreCase("ESF") 
							|| productType.equalsIgnoreCase("EDN")) {					
						imageMetadata.addEntry(image);
					}

					double site_rover_origin_rotation_quaternian_a = Double.parseDouble(params[9]);
					double site_rover_origin_rotation_quaternian_b = Double.parseDouble(params[10]);
					double site_rover_origin_rotation_quaternian_c = Double.parseDouble(params[11]);
					double site_rover_origin_rotation_quaternian_d = Double.parseDouble(params[12]);
					boolean has_site_rover_origin_offset_vector = false;
					double site_rover_origin_offset_vector_a = 0.0;
					double site_rover_origin_offset_vector_b = 0.0;
					double site_rover_origin_offset_vector_c = 0.0;
					
					if (params.length >= 16) {
						has_site_rover_origin_offset_vector = true;
						site_rover_origin_offset_vector_a = Double.parseDouble(params[13]);
						site_rover_origin_offset_vector_b = Double.parseDouble(params[14]);
						site_rover_origin_offset_vector_c = Double.parseDouble(params[15]);
					}
					
					if (params.length >= 18) {
						image.rmc_site = Integer.parseInt(params[16]);
						image.rmc_drive = Integer.parseInt(params[17]);						
					}
					
					if (params.length >= 19) {
						// TODO reduce object allocation
						image.obsID = params[18];
					}
					
					String roverCode = MerUtils.roverCodeFromFilename(filename);
					String siteDrive = MerUtils.siteDriveFromFilename(filename);
					LocationCounter location = new LocationCounter(siteDrive);
					if ((image.rmc_site >= 0) && (image.rmc_drive >= 0)) {
						location.site = image.rmc_site;
						location.drive = image.rmc_drive;
					}
					
					LocationMetadataEntry site = locationMetadata.getEntry(roverCode, location);
					if (site == null) {
						site = new LocationMetadataEntry(roverCode, location.site, location.drive);
						locationMetadata.putEntry(site);
						site.rover_origin_rotation_quaternian_a = site_rover_origin_rotation_quaternian_a;
						site.rover_origin_rotation_quaternian_b = site_rover_origin_rotation_quaternian_b;
						site.rover_origin_rotation_quaternian_c = site_rover_origin_rotation_quaternian_c;
						site.rover_origin_rotation_quaternian_d = site_rover_origin_rotation_quaternian_d;
						site.has_site_rover_origin_offset_vector = has_site_rover_origin_offset_vector;
						site.rover_origin_offset_vector_a = site_rover_origin_offset_vector_a;
						site.rover_origin_offset_vector_b = site_rover_origin_offset_vector_b;
						site.rover_origin_offset_vector_c = site_rover_origin_offset_vector_c;
					}
					else {
						// TODO probably want to check for different values and deal with them somehow
					}
					// TODO deal with start sol, end sol
					
					line = reader.readLine();
				}
			}
		}
				
		locationMetadata.write(workspace);
		imageMetadata.write(workspace);		
		workspace.readMetadata();
	}
	
	public void processPancamTrackingObservations() throws Exception {
		ObservationMetadata observationMetadata = new ObservationMetadata();
				
		File[] files = workspace.getMetadataDir().listFiles();
		for (int n=0; n<files.length; n++) {
			File file = files[n];
			if ((file.isFile()) && (file.getName().startsWith("ObservationInfo"))
					&& (file.getName().endsWith(".html") || file.getName().endsWith(".htm"))) {
				task.logln(" - reading "+file.getName());
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String line = reader.readLine();
				// first read everything up to "<pre>"
				while (line != null) {
					int preIndex = line.indexOf("<pre>");
					if (preIndex >= 0) {
						line = line.substring(preIndex+5);
						break;
					}
					else {
						line = reader.readLine();
					}
				}				
				// now read lines until line starts with "</pre>"
				while (line != null) {
					task.checkForTerminate();
					if (line.startsWith("</pre>")) {
						break;
					}
					String[] params = line.split("::");
					if (params.length < 1) {
						throw new Exception("Error reading line \""+line+"\"");
					}										
					ObservationMetadataEntry entry = new ObservationMetadataEntry(params[0]);			
					if (params.length > 1) {
						entry.description = params[1];
					}
					else {
						entry.description = "";
					}
					observationMetadata.addEntry(entry);
					line = reader.readLine();
				}
			}
		}
				
		observationMetadata.write(workspace);
		workspace.readMetadata();
	}
	
	void stageMetadata() throws IOException {
		stageMetadata(workspace.getImageMetadata());
		stageMetadata(workspace.getObservationMetadata());
		stageMetadata(workspace.getLocationMetadata());
		stageMetadata(workspace.getLocationDescriptionMetadata());
		stageMetadata(workspace.getImageStretchMetadata());
		// update the lastMod time on any local latest2.txt file (for 2.0 metadata)
		File latestFile = new File(workspace.getMetadataStageDir(), "latest2.txt");
		if (latestFile.exists()) {
			latestFile.setLastModified(System.currentTimeMillis());
		}
		else {
			latestFile.createNewFile();
		}		
	}
	
	private void stageMetadata(Metadata metadata) {
		task.logln("Staging "+metadata.metadataName);
		try {
			metadata.stage(workspace);
		} catch (IOException e) {
			task.logln("Exception: "+e.toString());
		}		
	}
	
	public void processOldImageBrightnessFiles() {
		File localBrightnessDir = new File(workspace.getMetadataLocalDir(), "brightness");
		// local settings override downloaded settings
		if (localBrightnessDir.isDirectory()) {
			try {
				File[] files = localBrightnessDir.listFiles();
				if (files != null) {
					for (int n = files.length-1; n >= 0; n--) {
						File file = files[n];
						if (file.isFile() && file.getName().endsWith(".mmb")) {
							try {
								readOldBrightnesses(true, file, null, null);
							}
							catch (Exception e) {
								System.err.println("Error reading local brightness file "+file.getAbsolutePath());
								e.printStackTrace();
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Could not read local brightness info metadata: "
						+ e.toString());
			}
		}
		try {
			workspace.getImageStretchMetadata().write(workspace);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * If loading, we are loading the metadata. If not loading, we are just 
	 * reading the file to determine changed filenames and sols.
	 */
	private void readOldBrightnesses(boolean loading, File file,
			HashSet changedFilenames, HashSet changedSols) throws Exception {
		ImageStretchMetadata metadata = workspace.getImageStretchMetadata();
/*		if (!loading) {
			if ((brightnesses == null) || (seqStarts == null)) {
				loadBrightnesses();
			}
		}
		*/
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String inputLine;
		int lastSol = -1;
		while (((inputLine = reader.readLine()) != null) && (inputLine.startsWith("#"))) {
			// absorb any comment lines at start
		}
		if (inputLine==null)
			throw new Exception("File was not in mmbpan format; empty file.");
		StringTokenizer toks = new StringTokenizer(inputLine);
		int numToks = toks.countTokens();
		if ((numToks !=3) || (!toks.nextToken().equalsIgnoreCase("mmbbrightness"))
				|| (!toks.nextToken().equalsIgnoreCase("version"))) {
			throw new Exception("File was not in mmbbrightness format; invalid version.");
		}
		int version = Integer.parseInt(toks.nextToken());
		if (version > 1) {
			throw new Exception("The brightness file requires a later version of Midnight Mars Browser."
					+" Please update to the latest version.");				
		}
		
		while ((inputLine = reader.readLine()) != null) {
			if (inputLine.startsWith("#")) {
				// do nothing for comment line
			}
			else {
				toks = new StringTokenizer(inputLine);
				numToks = toks.countTokens();
				if (toks.hasMoreTokens()) {
					String cmd = toks.nextToken();
					if (cmd.equalsIgnoreCase("stretch")) {
						if (numToks < 4) {
							throw new Exception("Invalid stretch tag (not enough parameters) \""
									+inputLine+"\"");
						}
						String imageName = toks.nextToken();
						String minValString = toks.nextToken();
						int minVal = Integer.parseInt(minValString);
						String maxValString = toks.nextToken();
						int maxVal = Integer.parseInt(maxValString);
                        int devigA = 0;
                        int devigB = 0;
                        if (toks.hasMoreTokens()) {
                            devigA = Integer.parseInt(toks.nextToken());
                        }
                        if (toks.hasMoreTokens()) {
                            devigB = Integer.parseInt(toks.nextToken());
                        }
                        
						if (loading) {
							ImageStretchMetadataEntry param = (ImageStretchMetadataEntry) metadata.getEntry(imageName);
							if (param == null) {
								param = new ImageStretchMetadataEntry(imageName);
								metadata.addEntry(param);
							}
							param.minVal = minVal;
							param.maxVal = maxVal;
                            param.devigA = devigA;
                            param.devigB = devigB;
						}
						else {
							ImageStretchMetadataEntry param = (ImageStretchMetadataEntry) metadata.getEntry(imageName);
							if ((param==null) || (param.minVal != minVal) || (param.maxVal != maxVal)
                                    || (param.devigA != devigA) || (param.devigB != devigB)) {
								if (changedFilenames != null) {
									changedFilenames.add(imageName);
								}
								if (changedSols != null) {
									int sol = MerUtils.solFromFilename(imageName);
									if (sol != lastSol) {
										changedSols.add(new Integer(sol));
										lastSol = sol;
									}
								}
							}
						}
					}
					else if (cmd.equalsIgnoreCase("seqstart")) {
/*						if (numToks < 2) {
							throw new Exception("Invalid seqstart tag (not enough parameters) \""
									+inputLine+"\"");
						}
						String imageName = toks.nextToken();
						if (loading) {
							if (!seqStarts.contains(imageName)) {
								seqStarts.add(imageName);
							}
						}
						else {
							if (!seqStarts.contains(imageName)) {
								if (changedFilenames != null) {
									changedFilenames.add(imageName);
								}
								if (changedSols != null) {
									int sol = MerUtils.solFromFilename(imageName);
									if (sol != lastSol) {
										changedSols.add(new Integer(sol));
										lastSol = sol;
									}
								}
							}
						}
						*/
					}
					else {
						throw new Exception("Unrecognized command in brightness file: "+cmd);
					}
				}
			}
		}
		reader.close();
	}	
	
	
	private final double sameElementTolerance = 1.0;  // now in degrees // TODO refine this value
	private final int minNumberEnabledImages = 2;
	
	/**
	 * Generate the MMB1.x panorama files for people still stuck in the past, man.
	 * Adapted for MMB2.0.
	 */
	private void oldGenPanFiles() {
		// gather data to write to files
		task.logln("Generating Pans");
		HashMap outputPans = new HashMap();
		oldNewGenPanFiles(outputPans, '1');
		oldNewGenPanFiles(outputPans, '2');
		// write files
		task.logln("Writing pan files");
		File localPanFileDirectory =   new File(workspace.getMetadataLocalDir(), "pan");
//		File panFileDirectory = new File(metadataDir, "generated"+File.separator+"pan");
		// Now writing directly to local since I want to focus on MMB2.0
		File panFileDirectory = localPanFileDirectory;
		File downloadedPanFileDirectory = new File(workspace.getMetadataDownloadedDir(), "pan");
		if (!panFileDirectory.exists()) {
			boolean madeDirs = panFileDirectory.mkdirs();
			if (!madeDirs) {
				task.logln("ERROR");
				throw new Error("Could not make directory for " + panFileDirectory.getAbsolutePath());
			}
		}
		Iterator iter = outputPans.keySet().iterator();
		while (iter.hasNext()) {
			String filename = (String) iter.next();
			task.logln(" - writing "+filename);
			Vector elements = (Vector) outputPans.get(filename);
			String roverCode = filename.substring(0,1);
			String locationCode = filename.substring(2,6);
			LocationCounter locationCounter = new LocationCounter(locationCode);
			LocationMetadataEntry locationEntry = workspace.getLocationMetadata().getEntry(roverCode, locationCounter);
//			Quat4d quat = (Quat4d) outputQuats.get(filename);
			File panFile = new File(panFileDirectory, filename);
			try {
				OldPanModel model = OldPanModel.fromElements(elements, null);
				if (model.numEnabledImages >= minNumberEnabledImages) {
					// copy any editable information from a matching downloaded pan file
					// (downloaded instead of local, because might not be working with 
					// files in local)
					model.title = "";
					File downloadedPanFile = new File(downloadedPanFileDirectory, filename);
					if (downloadedPanFile.exists()) {
						OldPanModel localModel = OldPanModel.readPanModelFromFile(downloadedPanFile, true);
						if ((localModel.title != null) && (localModel.title.length()>0)) {
							task.logln("   - copying title \""+localModel.title+"\"");
							model.title = localModel.title;
						}
					}
					// set additional information
					if (locationEntry != null) {
						model.hasRoverQuat = true;
						model.roverQuatX = locationEntry.rover_origin_rotation_quaternian_a;
						model.roverQuatY = locationEntry.rover_origin_rotation_quaternian_b;
						model.roverQuatZ = locationEntry.rover_origin_rotation_quaternian_c;
						model.roverQuatW = locationEntry.rover_origin_rotation_quaternian_d;
					}
					// write file
					File compareFile = null;
					if (true) {
						compareFile = new File(localPanFileDirectory, filename);
					}
					model.writeToFile(panFile, compareFile);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				task.logln("ERROR writing pan file: "+e.toString());			
			}
		}
		
//		headerLevelInfos = null;
	}
	
	private void oldNewGenPanFiles(HashMap outputPans, char roverCode) {
		oldNewGenPanFiles(outputPans, roverCode, 'n');
		oldNewGenPanFiles(outputPans, roverCode, 'p');
	}
	
	private void oldNewGenPanFiles(HashMap outputPans, char roverCode, char cameraCode) {
		for (int sol = 1; sol <= 9999; sol++) {
			task.checkForTerminate();
			String dirString = roverCode + File.separator + cameraCode + File.separator + 
					MerUtils.zeroPad(sol, 3);
			File srcDir = new File(workspace.getRawImageDir(), dirString);
			if (srcDir!=null && srcDir.exists() && srcDir.isDirectory()) {
				File[] files = srcDir.listFiles();
//				numImagesCopied = 0;
//				numImagesSkipped = 0;
				
				if (cameraCode=='n') {
					// navcam
					File anaglyphDir = new File(workspace.getGeneratedImageDir(), roverCode + File.separator + 
							"na" + File.separator + MerUtils.zeroPad(sol, 3));
					File[] anaglyphFiles = null;
					if (anaglyphDir!=null && anaglyphDir.exists() && anaglyphDir.isDirectory()) {
						anaglyphFiles = anaglyphDir.listFiles();
					}
					String lastMatchString = "";
					for (int n=0; n<files.length; n++) {
						File file = files[n];
						String filename = file.getName();
						//LogFrame.getInstance().logln("Scanning img file "+dirString+File.separator+filename+"... ");
						if (file.isFile() && (filename.endsWith(".jpg") || filename.endsWith(".JPG"))
								&& (filename.length()==31)
								&& (!filename.substring(18,19).equalsIgnoreCase("F"))) {
							String matchString = filename.substring(0, 11);
							if (!matchString.equalsIgnoreCase(lastMatchString)) {
								OldPanModelElement element = new OldPanModelElement();
								element.roverCode = roverCode;
								element.cameraCode = cameraCode;
								element.findNavcamImageTypesAndTimecode(file, files, anaglyphFiles);
								oldNewGenPanFiles(outputPans, element, dirString, file);
							}
							else {
								lastMatchString = matchString;
							}
						}
					}
				}
				else if (cameraCode == 'p') {
					// pancam
					Vector sets = UpdateTask.findPancamImageSets(files, workspace);
					File anaglyphDir = new File(workspace.getGeneratedImageDir(), roverCode + File.separator + 
							"pa" + File.separator + MerUtils.zeroPad(sol, 3));
					File[] anaglyphFiles = null;
					if (anaglyphDir!=null && anaglyphDir.exists() && anaglyphDir.isDirectory()) {
						anaglyphFiles = anaglyphDir.listFiles();
					}
					File colorDir = new File(workspace.getGeneratedImageDir(), roverCode + File.separator + 
							"pc" + File.separator + MerUtils.zeroPad(sol, 3));
					File[] colorFiles = null;
					if (colorDir!=null && colorDir.exists() && colorDir.isDirectory()) {
						colorFiles = colorDir.listFiles();
					}
					File dcColorDir = new File(workspace.getGeneratedImageDir(), roverCode + File.separator + 
							"ps" + File.separator + MerUtils.zeroPad(sol, 3));
					File[] dcColorFiles = null;
					if (dcColorDir!=null && dcColorDir.exists() && dcColorDir.isDirectory()) {
						dcColorFiles = dcColorDir.listFiles();
					}
					for (int n=0; n<sets.size(); n++) {
						OldPanModelElement element = new OldPanModelElement();
						element.roverCode = roverCode;
						element.cameraCode = cameraCode;
						LinkedHashMap matchedFiles = (LinkedHashMap) sets.elementAt(n);
//						Iterator iter = matchedFiles.keySet().iterator();
						File file = element.findPancamImageTypesAndTimecodes(matchedFiles, 
								anaglyphFiles, colorFiles, dcColorFiles);
						if (file != null) {
							oldNewGenPanFiles(outputPans, element, dirString, file);
						}
					}
				}
			}
		}
	}
	
	private void oldNewGenPanFiles(HashMap outputPans, OldPanModelElement element, 
			String dirString, File imgFile) {
		String imgFileName = imgFile.getName();
		String name = imgFileName.substring(0, 27);
		
		ImageMetadataEntry imageEntry = workspace.getImageMetadata().getEntry(name);
		
		if (imageEntry != null) {
			task.debugln("Got imageEntry for "+name);
		}
		else {
			task.logln("WARNING no imageEntry for "+dirString+" "+name);			
			return;
		}
				
		element.azimuthDeg = imageEntry.inst_az_rover;
		// TODO why - ?
		element.elevationDeg = - imageEntry.inst_el_rover;
		element.top = imageEntry.first_line - 1;
		element.left = imageEntry.first_line_sample - 1;
		element.height = imageEntry.n_lines * imageEntry.pixel_averaging_height;
		if (element.top + element.height > 1024) {
			task.logln("WARNING element.top + element.height ="+(element.top + element.height));
			element.height = 1024 - element.top;
		}
		element.width = imageEntry.n_line_samples * imageEntry.pixel_averaging_width;
		if (element.left + element.width > 1024) {
			task.logln("WARNING element.left + element.width ="+(element.left + element.width));
			element.width = 1024 - element.left;
		}
		
		// write out to a pan file based on site/drive number
		String siteDriveNum = imgFileName.substring(14,18);
		String panFilename = ""+element.roverCode+"_"+siteDriveNum+".mmb";
		Vector elements = (Vector) outputPans.get(panFilename);
		if (elements == null) {
			elements = new Vector();
			outputPans.put(panFilename, elements);
		}
		if ((element.cameraCode=='p') && (element.hasRight) && (!element.hasLeft) 
				&& (!element.hasColor) && (!element.hasDCColor)) {
			element.enabled = false;
		}
		else {
			OldPanModelElement matchingElement = OldPanModel.hasMatchingElement(elements, element, sameElementTolerance);
			if (matchingElement != null) {
				if ((element.cameraCode=='p') && element.hasDCColor && !matchingElement.hasDCColor) {
					matchingElement.enabled = false;
				}
				else if ((element.cameraCode=='p') && element.hasColor && !matchingElement.hasColor) {
					matchingElement.enabled = false;
				}
				else if ((element.cameraCode=='p') && !element.hasSolarFilter && matchingElement.hasSolarFilter) {
					matchingElement.enabled = false;
				}
				else {
					element.enabled = false;
				}
			}
		}
		elements.add(element);
	}
	
	private void buildOldMetadataPackages() throws Exception {
		buildOldMetadataPackages("solInfo");
		buildOldMetadataPackages("pan");
		buildOldMetadataPackages("brightness");		
	}
	
	private void buildOldMetadataPackages(String dirName) throws Exception {
		task.logln("Getting old "+dirName+" file last mod times and building package...");
		File localDir = new File(workspace.getMetadataLocalDir(), dirName);
		if (!localDir.exists()) {
			localDir.mkdirs();
		}
		File[] localFiles = localDir.listFiles();
		File packageFile = new File(workspace.getMetadataStageDir(), dirName+"_metadata_new.mmb");
		File archivePackageFile = new File(workspace.getMetadataStageDir(), dirName+"_metadata_archive.mmb");
		long archiveLastMod = 0;
		if (archivePackageFile.exists())
			archiveLastMod = archivePackageFile.lastModified();
		BufferedWriter packageWriter = new BufferedWriter(new FileWriter(packageFile));
		try {
			// first write the index
			packageWriter.write("mmbmetafile "+dirName+"/index\n");
			for (int n = 0; n < localFiles.length; n++) {
				File file = localFiles[n];
				if (isOldMetadataFile(dirName, file)) {
					packageWriter.write(file.getName()+"\n");
				}
			}
			// then write the files
			for (int n = 0; n < localFiles.length; n++) {
				File file = localFiles[n];
				if (isOldMetadataFile(dirName, file)) {
					if (file.lastModified() > archiveLastMod) {
						// get the latest download version to retrieve its last mod time, which we put in the index
						task.checkForTerminate();
						task.logln(" - indexing "+file.getAbsolutePath()+" ");
						long lastModTime = file.lastModified();
						packageWriter.write("mmbmetafile "+dirName+"/"+file.getName()+";"+lastModTime+"\n");
						BufferedReader reader = new BufferedReader(new FileReader(file));
						String line = null;
						while ((line=reader.readLine()) != null) {
							packageWriter.write(line+"\n");
						}
					}
				}
			}
		}
		finally {
			if (packageWriter != null)
				packageWriter.close();
		}
		// update the lastMod time on any local latest.txt file
		File latestFile = new File(workspace.getMetadataStageDir(), "latest.txt");
		if (latestFile.exists()) {
			latestFile.setLastModified(System.currentTimeMillis());
		}
		else {
			latestFile.createNewFile();
		}
	}
	
	private boolean isOldMetadataFile(String dirName, File file) {
		boolean indexIt = false;
		if (dirName.equals("pan")) {
			if (file.isFile() && file.getName().endsWith(".mmb")) {
				indexIt = true;
			}
		}
		else if (dirName.equals("solInfo")) {
			if (file.isFile() && file.getName().endsWith(".htm")) {
				indexIt = true;
			}
		}
		else if (dirName.equals("brightness")) {
			if (file.isFile() && file.getName().endsWith(".mmb")) {
				indexIt = true;
			}
		}
		return indexIt;
	}
	
}
