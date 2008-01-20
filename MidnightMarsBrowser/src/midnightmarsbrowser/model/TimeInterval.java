package midnightmarsbrowser.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;

import midnightmarsbrowser.application.MMBWorkspace;
import midnightmarsbrowser.metadata.LocationDescriptionMetadataEntry;
import midnightmarsbrowser.metadata.LocationMetadataEntry;
import midnightmarsbrowser.metadata.ObservationMetadata;

public class TimeInterval {
	TimeIntervalList parent = null;
	
	int startTime = -1;
	
	int endTime = -1;

	int startSol = -1;

	int endSol = -1;
	
	LocationCounter startLocation = null;
	
	LocationCounter endLocation = null;
	
	ImageEntry[] imageList = null;
	
	int numEnabledImages = 0;
	
	String solsString;
	
	String timeString;

	String description;
	
	LocationMetadataEntry locationMetadataEntry = null;

	TimeInterval(TimeIntervalList parent) {
		this.parent = parent;
	}
	
	void finishSetup(ArrayList imageArrayList, MMBWorkspace workspace) {
		imageList = new ImageEntry[imageArrayList.size()];
		imageList = (ImageEntry[]) imageArrayList.toArray(imageList);
		
		if (startSol == endSol) {
			solsString = "" + startSol;
		}
		else {
			solsString = "" + startSol + "-" + endSol;
		}
		
		timeString = "Sol "+startSol;
		if (endSol != startSol) {
			timeString = timeString+"-"+endSol;
		}
		else {
			long timeMillis = MerUtils.timeMillisFromRoverClock(startTime);
			int[] marsTime = new int[4];
			MerUtils.marsTimeFromTimeMillis(getParent().getRoverCode(), timeMillis, marsTime);
			timeString = timeString +" "+MerUtils.zeroPad(marsTime[1], 2) + ":"
					+ MerUtils.zeroPad(marsTime[2], 2) + ":"
					+ MerUtils.zeroPad(marsTime[3], 2);
			if (endTime != startTime) {
				timeMillis = MerUtils.timeMillisFromRoverClock(endTime);
				MerUtils.marsTimeFromTimeMillis(getParent().getRoverCode(), timeMillis, marsTime);
				timeString = timeString +"-"+MerUtils.zeroPad(marsTime[1], 2) + ":"
				+ MerUtils.zeroPad(marsTime[2], 2) + ":"
				+ MerUtils.zeroPad(marsTime[3], 2);						
			}
		}
		
		if (startLocation.equals(endLocation)) {
			LocationDescriptionMetadataEntry ldmEntry = workspace.getLocationDescriptionMetadata().getEntry(parent.roverCode, startLocation.siteDriveCode);
			if (ldmEntry != null) {
				description = ldmEntry.description;
			}
		}
		else {
			description = "multiple locations";			
		}
		locationMetadataEntry = workspace.getLocationMetadata().getEntry(parent.roverCode, startLocation);
		if (locationMetadataEntry == null) {
			System.err.println("Warning: Location "+startLocation.site+" "+startLocation.drive+" "+startLocation.siteDriveCode+" did not have location metadata.");
		}
	}
	
	void countEnabledImages() {
		// image enablement needs to happen before this is called
		numEnabledImages = 0;
		for (int i=0; i<imageList.length; i++) {
			if (imageList[i].enabled) {
				numEnabledImages++;
			}
		}
	}
	
/* This didn't really work out.
	void computeDescription() {
		LinkedHashSet set = new LinkedHashSet();
		String imageDesc = null;
		for (int i=0; i<imageList.length; i++) {
			if (imageDesc == null || !imageDesc.equals(imageList[i].observationDescription)) {
				imageDesc = imageList[i].observationDescription;
				if (imageDesc != null) {
					String[] components = imageDesc.split("[ _()]");
					for (int j=0; j<components.length; j++) {
						String comp = components[j];
						if (comp.length() <= 1)
							continue;
						if (comp.equals("Unexpected") || comp.equals("sequence!!!!"))
							continue;
						if (comp.equals("fs") || comp.equals("commanded") || comp.equals("eg") || comp.equals("sunfind"))
							continue;
						if (comp.equals("front") || comp.equals("rear"))
							continue;
						if (comp.equals("navcam") || comp.equals("pancam") || comp.equals("hazcam"))
							continue;
						if (Character.isDigit(comp.charAt(0)) || Character.isDigit(comp.charAt(1)))
							continue;
						if (comp.equals("stereo") || comp.equals("bpp") || comp.equals("ite") 
								|| comp.equals("tier") || comp.equals("sub") || comp.equals("sundial")
								|| comp.equals("sol") || comp.equals("ext") || comp.equals("scaled")
								|| comp.equals("pan"))
							continue;
						if (comp.equals("image") || comp.equals("cal") || comp.equals("target") 
								|| comp.equals("microscopic"))
							continue;
						set.add(comp);
					}
				}
			}
		}
		description = null;
		Iterator iter = set.iterator();
		while (iter.hasNext()) {
			if (description == null)
				description = (String) iter.next();
			else
				description += " " + (String) iter.next();
		}
	}
*/
	
	public TimeIntervalList getParent() {
		return parent;
	}
	
	public ImageEntry[] getImageList() {
		return imageList;
	}
	
	public int indexOf(ImageEntry image) {
		if (imageList == null)
			return -1;
		for (int n=0; n<imageList.length; n++) {
			if (imageList[n] == image) {
				return n;
			}
		}
		return -1;
	}
	
	public String getDescription() {
		return description;
	}

	
	public int getStartTime() {
		return startTime;
	}

	public int getEndTime() {
		return endTime;
	}

	public int getStartSol() {
		return startSol;
	}
	
	public int getEndSol() {
		return endSol;
	}

	/**
	 * Panoramas are always broken down by location, so startLocation will always be the same as endLocation
	 * - except possibly in the rare cases when the locations really need to be joined (ie, McMurdo Pan).
	 * @return
	 */	
	public LocationCounter getStartLocation() {
		return startLocation;
	}
	
	public LocationCounter getEndLocation() {
		return endLocation;
	}

	/**
	 * locationMetadataEntry will exist for panoramas; need not exist for slideshows, where time intervals can cross locations.
	 */
	public LocationMetadataEntry getLocationMetadataEntry() {
		return locationMetadataEntry;
	}
	
	public String getSolsString() {
		return solsString;
	}	
	
	public String getTimeString() {
		return timeString;
	}

	public int getNumEnabledImages() {
		return numEnabledImages;
	}

	public ImageEntry findImageEntryClosestTo(ImageEntry oldEntry) {
		if (imageList.length == 0) 
			return null;
		if (imageList.length == 1)
			return imageList[0];
		for (int n=0; n<imageList.length-1; n++) {
			ImageEntry entry1 = imageList[n];
			if (oldEntry.roverClock <= entry1.roverClock) {
				return entry1;
			}
			ImageEntry entry2 = imageList[n+1];
			if (oldEntry.roverClock < entry2.roverClock) {
				if (oldEntry.roverClock - entry1.roverClock > entry2.roverClock - oldEntry.roverClock) {
					return entry1;
				}
				else {
					return entry2;
				}
			}
		}
		return imageList[imageList.length-1];
	}
	
}
