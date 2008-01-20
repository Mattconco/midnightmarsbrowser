/**
 *  Midnight Mars Browser - http://midnightmarsbrowser.blogspot.com
 *  Copyright (c) 2005 by Michael R. Howard
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
package midnightmarsbrowser.model;


/**
 * A set of sorted image partial pathnames for a given sol, used by Slideshow.
 *
 * @deprecated
 * @author michaelhoward
 *
 */
/*
public class ImageList {
	
	private ImageListEntry[] entries;
	
	HashMap entriesByLocationMap;
	
	private String roverCode;
	
	private ImageList(String roverCode, ImageListEntry[] entries, HashMap entriesByLocationMap) {
		this.roverCode = roverCode;
		this.entries = entries;
		this.entriesByLocationMap = entriesByLocationMap;
	}
	
	public ImageListEntry getEntry(int index) {
		if ((entries != null) && (index >= 0)) {
			return entries[index];
		}
		return null;
	}
	
	public int getLength() {
		if (entries != null) {
			return entries.length;
		}
		return 0;
	}
	
	public ImageListEntry[] getEntries() {
		return entries;
	}

	public String getRoverCode() {
		return roverCode;
	}
	
	public int indexOf(ImageListEntry entry) {
		return Arrays.binarySearch(entries, entry);
	} 
		
	public ImageListEntry[] getEntriesForLocation(LocationCounter location) {
		ImageListEntry[] locEntries = (ImageListEntry[]) entriesByLocationMap.get(location);
		return locEntries;
	}
	
	public static ImageList findImageList(MMBWorkspace workspace, ViewerSettings settings, HashSet limitImagePathnames) {
		ArrayList entriesList = new ArrayList();
		HashMap byLocationListMap = new HashMap();
		ImageIndex index = workspace.getImageIndex();
		File rawImageDir = workspace.getRawImageDir();
		File generatedImageDir = workspace.getGeneratedImageDir();
		ImageMetadata imageMetadata = workspace.getImageMetadata();
		if ((rawImageDir == null) || (!rawImageDir.exists())) {
			System.out.println("rawImageDir directory does not exist");		
		}
		if ((generatedImageDir == null) || (!generatedImageDir.exists())) {
			System.out.println("generatedImageDir directory does not exist");
		}
		//System.out.println("-- Starting Sol "+sol);
		for (int n = settings.fromSol; n <= settings.toSol; n++) {
			findImageSetSol(entriesList, byLocationListMap, index, rawImageDir, generatedImageDir,
					settings, n, limitImagePathnames, imageMetadata);
		}
		
		// sort image entry list
		ImageListEntry[] entries = new ImageListEntry[entriesList.size()];
		entries = (ImageListEntry []) entriesList.toArray(entries);				
		if (entries.length > 0) {
			Arrays.sort(entries);
		}
		
		// process and sort by-location lists
		HashMap entriesByLocationMap = new HashMap();
		Iterator locIter = byLocationListMap.keySet().iterator();
		while (locIter.hasNext()) {
			LocationCounter location = (LocationCounter) locIter.next();
			ArrayList list = (ArrayList) byLocationListMap.get(location);
			ImageListEntry[] array = new ImageListEntry[list.size()];
			array = (ImageListEntry []) list.toArray(array);
			Arrays.sort(array);
			entriesByLocationMap.put(location, array);
		}
		
		
		// After image list is compiled and sorted, then we can set enabled status based on Pancam filter selection
		if (settings.panorama) {
			Iterator iter = entriesByLocationMap.keySet().iterator();
			while (iter.hasNext()) {
				LocationCounter location = (LocationCounter) iter.next();
				ImageListEntry[] locEntries = (ImageListEntry[]) entriesByLocationMap.get(location);
				for (int i = 0; i < locEntries.length; i++) {
					ImageListEntry entry = locEntries[i];
					// see if there is another enabled entry at the same camera pointing
					ImageListEntry matchingEntry = null;
					ImageListEntry testEntry;
					for (int j=0; j<i; j++) {
						testEntry = locEntries[j];
						if (testEntry.enabled) {
							double dAz = testEntry.getImageMetadataEntry().inst_az_rover - 
								entry.getImageMetadataEntry().inst_az_rover;
							double dEl = testEntry.getImageMetadataEntry().inst_el_rover - 
								entry.getImageMetadataEntry().inst_el_rover;
							if ((Math.abs(dAz) < matchTolerance) && (Math.abs(dEl) < matchTolerance)) {
								matchingEntry = testEntry;
								break;
							}							
						}
					}
					// if there is an existing entry, figure out if the new entry is a better choice -
					// it's better if it's of better image class
					if (matchingEntry != null) {
						String imageCat = entry.getImageCategory();
						String chosenImageCat = matchingEntry.getImageCategory();
						if (	(imageCat.equals("pc") && (chosenImageCat.equals("p"))) 
								|| (imageCat.equals("ps") && ((chosenImageCat.equals("p") || (chosenImageCat.equals("pc")))))
								) {
							matchingEntry.enabled = false;
							entry.enabled = true;
						}
						else {
							entry.enabled = false;
						}
					}
				}
			}			
		}
				
		ImageList newImageList = new ImageList(settings.roverCode, entries, entriesByLocationMap);
		return newImageList;
	}
	
	private static void findImageSetSol(ArrayList entries, HashMap byLocationListMap, 
			ImageIndex index, File rawImageDir, File generatedImageDir,
			ViewerSettings settings, int sol,
			HashSet limitImagePathnames, ImageMetadata imageMetadata) {
		if (rawImageDir!=null) {
			if (settings.left || settings.right) {
				if (settings.n) {
					addToEntries(entries, byLocationListMap, index, rawImageDir, settings, sol, limitImagePathnames, "n", imageMetadata);
				}
				if (settings.p) {
					addToEntries(entries, byLocationListMap, index, rawImageDir, settings, sol, limitImagePathnames, "p", imageMetadata);
				}
				if (!settings.panorama) {
					if (settings.f) {
						addToEntries(entries, byLocationListMap, index, rawImageDir, settings, sol, limitImagePathnames, "f", imageMetadata);
					}
					if (settings.r) {
						addToEntries(entries, byLocationListMap, index, rawImageDir, settings, sol, limitImagePathnames, "r", imageMetadata);
					}
					if (settings.m) {
						addToEntries(entries, byLocationListMap, index, rawImageDir, settings, sol, limitImagePathnames, "m", imageMetadata);
					}
				}
			}
		}
		if (generatedImageDir!=null) {
			if ((!settings.panorama) || (!settings.left && !settings.right)) {
				if (settings.n && settings.anaglyph) {
					addToEntries(entries, byLocationListMap, index, generatedImageDir, settings, sol, limitImagePathnames, "na", imageMetadata);
				}
				if (settings.p && settings.anaglyph) {
					addToEntries(entries, byLocationListMap, index, generatedImageDir, settings, sol, limitImagePathnames, "pa", imageMetadata);
				}
			}
			// TODO: Need to add enabled property of ImageListEntry and disable MMBFalseColor images
			// from the same group or position
			if (settings.p && settings.pancamMMBFalseColor) {
				addToEntries(entries, byLocationListMap, index, generatedImageDir, settings, sol, limitImagePathnames, "pc", imageMetadata);
			}
			if (settings.p && settings.pancamDCCalibratedColor) {
				addToEntries(entries, byLocationListMap, index, generatedImageDir, settings, sol, limitImagePathnames, "ps", imageMetadata);
			}
			if (!settings.panorama) {
				if (settings.f && settings.anaglyph) {
					addToEntries(entries, byLocationListMap, index, generatedImageDir, settings, sol, limitImagePathnames, "fa", imageMetadata);
				}
				if (settings.r && settings.anaglyph) {
					addToEntries(entries, byLocationListMap, index, generatedImageDir, settings, sol, limitImagePathnames, "ra", imageMetadata);
				}
			}
		}
	}

	private static void addToEntries(ArrayList entries, HashMap byLocationListMap, 
			ImageIndex index, File localImageHome, ViewerSettings settings, int sol, 
			HashSet limitImagePathnames, String imageCategory, ImageMetadata imageMetadata) {
		String solString = "" + sol;
		LinkedHashSet filenames = index.getFilenames(localImageHome, settings.roverCode, 
				imageCategory, sol);
		if (filenames != null) {
			String dirString = settings.roverCode + File.separator + imageCategory
			+ File.separator + MerUtils.zeroPad(sol, 3);
			Iterator iter = filenames.iterator();
			while (iter.hasNext()) {
				String testName = ((String) iter.next());
				String uppercaseTestName = testName.toUpperCase();
				try {
					if (imageCategory.length() == 1) {
						// raw image file
						//check for standard filename
						if ((!uppercaseTestName.endsWith(".JPG")) || (uppercaseTestName.length()!=31))
							continue;
					}
					else {
						// generated image
						//check for whatever
						// TODO maybe we want to allow PNG later
						if ((!uppercaseTestName.endsWith(".JPG")) || (uppercaseTestName.endsWith("_TH.JPG"))
								|| (testName.length()<31))
							continue;
					}
					String testProductType = MerUtils.productTypeFromFilename(uppercaseTestName);
					if (testProductType.equals("EFF")) {
						if (!settings.fullFrame) {
							continue;
						}
					}
					else if (testProductType.equals("ESF")) {
						if (!settings.subFrame) {
							continue;
						}
					}
					else if (testProductType.equals("EDN")) {
						if (!settings.downsampled) {
							continue;
						}
					}
					else if (testProductType.equals("RAD")) {
						// selection not supported for RAD (DC calibrated color)
					}
					else {
//						if (!criteria.typeOther) {
//							continue;
//						}
					}
					if (imageCategory.equals("f")
							|| imageCategory.equals("r")) {
						if ((MerUtils.cameraEyeFromFilename(uppercaseTestName)=='L') && (!settings.left))
							continue;
						if ((MerUtils.cameraEyeFromFilename(uppercaseTestName)=='R') && (!settings.right))
							continue;
					}
					else if (imageCategory.equals("n") ) {
						if ((MerUtils.cameraEyeFromFilename(uppercaseTestName)=='L') && (!settings.left))
							continue;
						if (settings.panorama) {
							if ((MerUtils.cameraEyeFromFilename(uppercaseTestName)=='R') && (!settings.right || settings.left))
								continue;
						}
						else {
							if ((MerUtils.cameraEyeFromFilename(uppercaseTestName)=='R') && (!settings.right))
								continue;
						}
					}
					else if (imageCategory.equals("m") && !(settings.left || settings.right)) {
						continue;
					}
					else if (imageCategory.equals("p")) {
						if ((MerUtils.cameraEyeFromFilename(uppercaseTestName)=='L') && (!(settings.pancamRaw && settings.left)))
							continue;
						if (settings.panorama) {
							if ((MerUtils.cameraEyeFromFilename(uppercaseTestName)=='R') && (!(settings.pancamRaw && settings.right && !settings.left)))
								continue;
						}
						else {
							if ((MerUtils.cameraEyeFromFilename(uppercaseTestName)=='R') && (!(settings.pancamRaw && settings.right)))
								continue;
						}
					}
					else if (imageCategory.equals("pc")) {
						if ((MerUtils.cameraEyeFromFilename(uppercaseTestName)=='L') && (!(settings.left)))
							continue;
						if (settings.panorama) {
							if ((MerUtils.cameraEyeFromFilename(uppercaseTestName)=='R') && (!(settings.right)))
								continue;
						}
						else {
							if ((MerUtils.cameraEyeFromFilename(uppercaseTestName)=='R') && (!(settings.right && !settings.left)))
								continue;							
						}
					}
					else if (imageCategory.equals("ps")) {
						if (!settings.left)
							continue;
					}
					
					String cmdSeqNbr = null;
					String siteDriveNum = null;
					try {
						cmdSeqNbr = uppercaseTestName.substring(18, 23);
						siteDriveNum = uppercaseTestName.substring(14,18);
					}
					catch (IndexOutOfBoundsException e) {
						continue;
					}
					
					if (arrayHasAnEntry(settings.includeCmdSeqs)) {
						if (!arrayMatchesStringStart(settings.includeCmdSeqs, cmdSeqNbr)) {
							continue;
						}
					}
					
					if (arrayHasAnEntry(settings.excludeCmdSeqs)) {
						if (arrayMatchesStringStart(settings.excludeCmdSeqs, cmdSeqNbr)) {
							continue;
						}
					}
					
					if (arrayHasAnEntry(settings.includeSiteDriveNumber)) {
						if (!arrayMatchesStringStart(settings.includeSiteDriveNumber, siteDriveNum)) {
							continue;
						}
					}					
					
					if (settings.excludeDriveImages) {
						if ((cmdSeqNbr.startsWith("F")) || (cmdSeqNbr.startsWith("f"))) {
							continue;
						}
					}
					
					if (settings.excludeSundial) {
						if (arrayMatchesStringStart(sundialCmdSeqNbrs,  cmdSeqNbr)) {
							continue;
						}				
					}
					
					if (imageCategory.equals("p")) {
						String pancamFilter = uppercaseTestName.substring(23,25);
						
						if (arrayHasAnEntry(settings.includePancamFilter)) {
							if (!arrayMatchesStringStart(settings.includePancamFilter, pancamFilter)) {
								continue;
							}
						}
						
						if (arrayHasAnEntry(settings.excludePancamFilter)) {
							if (arrayMatchesStringStart(settings.excludePancamFilter, pancamFilter)) {
								continue;
							}
						}
						
						if (settings.excludeSolarFilter) {
							if (arrayMatchesStringStart(solarFilters, pancamFilter)) {
								continue;
							}
						}
					}
					
					LocationCounter location = new LocationCounter(siteDriveNum);
					
					String spacecraftId = MerUtils.roverCodeFromFilename(testName);
					Integer roverClock = MerUtils.roverClockIntegerFromFilename(testName);
					ImageMetadataEntry imageMetadataEntry = null;
					if (settings.panorama) {
						imageMetadataEntry = imageMetadata.getEntry(spacecraftId, roverClock);
						if (imageMetadataEntry == null)
							continue;
						if (imageMetadataEntry.rmc_site >= 0) {
							location.site = imageMetadataEntry.rmc_site;
							location.drive = imageMetadataEntry.rmc_drive;
						}
					}
					
					String pathname = dirString + File.separator + testName;
					if ((limitImagePathnames == null)
							|| (!settings.limitToSet)
							|| (limitImagePathnames.contains(testName))) {
						ImageListEntry newEntry = new ImageListEntry();
						newEntry.filename = testName;
						newEntry.partialPathname = pathname;
						newEntry.sol = sol;
						newEntry.solString = solString;
						newEntry.roverClock = roverClock.intValue();
						newEntry.file = new File(localImageHome, pathname).getAbsolutePath();
						newEntry.imageCategory = imageCategory;
						newEntry.imageMetadataEntry = imageMetadataEntry;
						entries.add(newEntry);
						ArrayList byLocationList = (ArrayList) byLocationListMap.get(location);
						if (byLocationList == null) {
							byLocationList = new ArrayList();
							byLocationListMap.put(location, byLocationList);
						}
						byLocationList.add(newEntry);
					}
				}
				catch (Exception e) {
					// just ignore it if we have some filename that we choke on
				}
			}
		}
	}

	public int findEntryClosestTo(ImageListEntry oldEntry) {
		for (int n=0; n<entries.length; n++) {
			ImageListEntry entry = entries[n];
			if (entry.roverClock == oldEntry.roverClock) {
				if (entry.filename.equals(oldEntry.filename)) {
					return n;
				}
			}
			else if (entry.roverClock > oldEntry.roverClock) {
				int prevClock = 0;
				if (n > 0) {
					prevClock = entries[n-1].roverClock;
				}
				int time1 = oldEntry.roverClock - prevClock;
				int time2 = entry.roverClock - oldEntry.roverClock;
				if (time1 < time2) {
					if (n > 0)
						return n-1;
					else
						return 0;
				}
				else {
					return n;
				}
			}			
		}
		return entries.length - 1;
	}
	
	private static boolean arrayHasAnEntry(String[] array) {
		if (array != null) {
			for (int n=0; n<array.length; n++) {
				String test = array[n];
				if (test.length() > 0) {
					return true;
				}
			}
		}
		return false;
	}	
	
	private static boolean arrayMatchesStringStart(String[] array, String str) {
		if (array != null) {
			for (int n=0; n<array.length; n++) {
				String test = array[n];
				if (test.length() > 0) {
					if (str.startsWith(test)) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
*/
