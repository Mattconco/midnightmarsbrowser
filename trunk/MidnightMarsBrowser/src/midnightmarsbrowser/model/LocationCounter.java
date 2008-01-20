package midnightmarsbrowser.model;

import java.util.Arrays;

/**
 * And object that holds values for the rover motion counter (RMC).
 * @author michaelhoward
 *
 */
public class LocationCounter implements Comparable {
	public int site = -1;
	public int drive = -1;
	public String siteDriveCode = null;
	
	public LocationCounter(int site, int drive) {
		this.site = site;
		this.drive = drive;
		siteDriveCode = "????";
	}
	
	public LocationCounter(String siteDriveCode) {
		this.siteDriveCode = siteDriveCode;
		setFrom(siteDriveCode);
	}
	
	public LocationCounter(String siteCode, String driveCode) {
		setFrom(siteCode, driveCode);
	}
	
	public int hashCode() {		
		return (site << 11) + drive;
	}

	public boolean equals(Object arg0) {
		LocationCounter counter = (LocationCounter) arg0;
		return (counter.site == this.site) && (counter.drive == this.drive);
	}
	
	public int compareTo(Object arg0) {
		LocationCounter counter = (LocationCounter) arg0;
		int diff = counter.site - this.site;
		if (diff == 0) {
			diff = counter.drive - this.drive;
		}
		return -diff;
	}

	public void setFrom(LocationCounter counter) {
		this.site = counter.site;
		this.drive = counter.drive;
	}
	
	public void setFrom(String siteDriveString) {
		this.site = valueFromString(siteDriveString.substring(0,2));
		this.drive = valueFromString(siteDriveString.substring(2,4));
	}
	
	public void setFrom(String siteString, String driveString) {
		this.site = valueFromString(siteString);
		this.drive = valueFromString(driveString);
	}

	public static int valueFromString(String str) {
		try {
			return Integer.parseInt(str);
		}
		catch (NumberFormatException e) {
			int val0 = valFromChar(str.charAt(0));
			int val1 = valFromChar(str.charAt(1));
			if (val0 < 0) {
				return -1;
			}
			else if (val0 < 10) {
				return val0*10+val1;
			}
			else {
				return (val0-10)*36 + val1 + 100;
			}
		}
	}
	
	private static int valFromChar(char ch) {
		if ((ch >= '0') && (ch <= '9')) {
			return ch-'0';
		}
		else if ((ch >= 'a') && (ch <= 'z')) {
			return ch - 'a' + 10;
		}
		else if ((ch >= 'A') && (ch <= 'Z')) {
			return ch - 'A' + 10;
		}
		else {
			return -1;
		}
	}
	
	/**
	 * Calculate drive counter offset from location1 to location2, given an array of all location counters 
	 * and assuming that for all sites before location2.site the last position is equivalent to the first 
	 * position of the next site. 
	 * @param location1
	 * @param location2
	 * @param allLocations
	 * @return
	 */
	public static int driveDiff(LocationCounter location1, LocationCounter location2, LocationCounter[] allLocations) {
		if (location2.site == location1.site) {
			return location2.drive - location1.drive;
		}
		else if (location2.site > location1.site) {
			int totalDrive = 0;
			int driveStart = location1.drive;
			for (int site=location1.site; site<location2.site; site++) {
				int siteLength = siteLength(site, allLocations);
				if (siteLength >= 0) {
					totalDrive = totalDrive + siteLength - driveStart;
				}
				driveStart = 0;
			}
			totalDrive += location2.drive;
			return totalDrive;
		}
		else {
			// location2.site < location1.site
			return -driveDiff(location2, location1, allLocations);
		}
	}
	
	public static LocationCounter driveAdd(LocationCounter location1, int driveAmount, LocationCounter[] allLocations) {
		LocationCounter location2 = new LocationCounter(location1.site, location1.drive);
		if (driveAmount > 0) {
			int site = location1.site;
			int driveStart = location1.drive;
			while (driveAmount > 0) {
				int siteLength = siteLength(site, allLocations);
				if (siteLength < 0) {
					return new LocationCounter(site, driveAmount+driveStart);
				}
				else if (siteLength-driveStart < driveAmount) {
					driveAmount = driveAmount - (siteLength-driveStart);
					site++;
				}
				else {
					return new LocationCounter(site, driveAmount+driveStart);					
				}				
				driveStart = 0;
			}			
		}
		else if (driveAmount < 0) {
			int site = location1.site;
			int driveStart = location1.drive;
			if (driveStart >= driveAmount) {
				return new LocationCounter(site, driveStart-driveAmount);
			}
			driveAmount -= driveStart;
			while (driveAmount > 0) {
				if (site > 0) {
					site--;
				}
				else {
					return new LocationCounter(0, 0);
				}
				int siteLength = siteLength(site, allLocations);
				if (siteLength < 0) {
					return new LocationCounter(0, 0);
				}
				if (siteLength >= driveAmount) {
					return new LocationCounter(site, siteLength-driveAmount);
				}
				else {
					driveAmount -= siteLength;
				}				
			}
		}
		return location2;
	}
	
	public static int siteLength(int site, LocationCounter[] allLocations) {
		int siteEndIndex = Arrays.binarySearch(allLocations, new LocationCounter(site+1, 0));
		if (siteEndIndex > 0) {
			siteEndIndex--;
		}
		else if (siteEndIndex < 0) {
			siteEndIndex = (-siteEndIndex - 1) - 1;
		}
		if (siteEndIndex >= 0 && siteEndIndex < allLocations.length && allLocations[siteEndIndex].site == site) {
			return allLocations[siteEndIndex].drive;
		}
		else {
			return -1;
		}
	}
	
}
