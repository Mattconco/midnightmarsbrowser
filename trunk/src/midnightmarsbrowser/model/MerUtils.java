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

import java.util.*;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 */

public class MerUtils {
	
	public static final char SPACECRAFT_ID_CHAR_SPIRIT = '2';
	public static final char SPACECRAFT_ID_CHAR_OPPORTUNITY = '1';
	public static final String ROVERCODE_SPIRIT = "2";
	public static final String ROVERCODE_OPPORTUNITY = "1";
	
	public static final String CAMERACODE_F = "F";
	public static final String CAMERACODE_M = "M";
	public static final String CAMERACODE_N = "N";
	public static final String CAMERACODE_P = "P";
	public static final String CAMERACODE_R = "R";
	
	public static final int explDateFormatLength = 17;
	private static Calendar explCalendar = new GregorianCalendar(TimeZone.getTimeZone("PST"), Locale.US);
	private static DateFormat explDateFolderFormat = null;
	private static DateFormat explDateFormat = null;
	
	public static String roverCodeStringFromChar(char roverCode) {
		if (roverCode == '1') {
			return ROVERCODE_OPPORTUNITY;
		}
		else if (roverCode == '2') {
			return ROVERCODE_SPIRIT;
		}
		else {
			return ""+roverCode;
		}
	}
		
	public static DateFormat getExplDateFolderFormat() {
		if (explDateFolderFormat == null) {
			explDateFolderFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
			explDateFolderFormat.setTimeZone(TimeZone.getTimeZone("PST"));			
		}
		return explDateFolderFormat;
	}
	
	public static DateFormat getExplDateFormat() {
		if (explDateFormat == null) {
			explDateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.US);
			explDateFormat.setTimeZone(TimeZone.getTimeZone("PST"));			
		}
		return explDateFormat;
	}
	
	public static Date explDay(Date date) {
		explCalendar.setTime(date);
		explCalendar.set(Calendar.HOUR_OF_DAY, 0);
		explCalendar.set(Calendar.MINUTE, 0);
		explCalendar.set(Calendar.SECOND, 0);
		explCalendar.set(Calendar.MILLISECOND, 0);
		return explCalendar.getTime();
	}
	
	public static Date explHour(Date date) {
		explCalendar.setTime(date);
		explCalendar.set(Calendar.MINUTE, 0);
		explCalendar.set(Calendar.SECOND, 0);
		explCalendar.set(Calendar.MILLISECOND, 0);
		return explCalendar.getTime();
	}
	
	
	/**
	 * Convert, for example, "L2L5L6" to compressed form "L256"
	 *Stereo pairs with filter 0 are compressed to nothing, ie "L0R0" -> "" - not anymore
	 */
	public static String compressFalseColorType(String type) {
		String outType = type;
		if (type == null) {
			outType = "";
		}
		else if (type.length()==6) {
			if ((type.charAt(0) == type.charAt(2)) && (type.charAt(0) == type.charAt(4))) {
				outType = type.substring(0,1) + type.substring(1,2) +
					type.substring(3,4) + type.substring(5,6);
			}
		}
		else if (type.length() == 4) {
//			if ((type.charAt(1)=='0') && (type.charAt(3)=='0')) {
//				outType = "";
//			}
		}
		return outType;
	}

    public static String zeroPad(int number, int digits) {
        String str = Integer.toString(number);
        while (str.length() < digits)
            str = "0" + str;
        return str;
    }
    
    public static String zeroPad(String string, int digits) {
        while (string.length() < digits)
            string = "0" + string;
        return string;
    }

    /**
     * Get the UTC time that was the start of Sol 1 for the specified rover.
     * If null is passed, return the UTC start time of spacecraft clock
     * (Jan 1, 2000 11:58:56 UTC).
     * Spirit Sol 1 started Jan 3, 2004 13:36:16 UTC more or less
     * Opportunity Sol 1 started Jan 24, 2004 15:09:00 UTC
     */
    private static Calendar sol1StartCalendar(String roverCode) {
//        TimeZone timeZone = TimeZone.getTimeZone("UTC");
    	// TODO UTC was throwing an exception - not sure if GMT is better
    	TimeZone timeZone = TimeZone.getTimeZone("GMT");    	
        GregorianCalendar cal = new GregorianCalendar(timeZone);
        cal.clear();
        if (roverCode == null) {
            cal.set(2000,0,1,11,58,56);
        }
        else if (roverCode.equals("1")) {
            // Opportunity Sol 1 start time
            cal.set(2004,0,24,15,9,0);
        }
        else if (roverCode.equals("2")) {
            // Spirit Sol 1 start time
            cal.set(2004,0,3,13,36,16);
        }
        return cal;
    }

    private static int[] sol1StartRoverClocks = null;

    private static int sol1StartRoverClock(String roverCode) {
        if (sol1StartRoverClocks == null) {
            sol1StartRoverClocks = new int[2];
            for (int n=0; n<2; n++) {
                Calendar cal = sol1StartCalendar(Integer.toString(n+1));
                Calendar calStart = sol1StartCalendar(null);
                long time = cal.getTimeInMillis();
                long startTime = calStart.getTimeInMillis();
                sol1StartRoverClocks[n] = (int) ( (time - startTime) / 1000);
            }
        }
        if (roverCode.equals("1"))
            return sol1StartRoverClocks[0];
        else if (roverCode.equals("2"))
            return sol1StartRoverClocks[1];
        else
            return 0;
    }

    public static int solFromTimeMillis(String roverCode, long timeMillis) {
        Calendar cal = sol1StartCalendar(roverCode);
        long sol1StartTime = cal.getTimeInMillis();
        long elapsedMillis = timeMillis - sol1StartTime;
        // 35.244 + 86400 + 2340 = 88775.244
        double marsDaySecs = (double)35.244 + (double)24*60*60 + 39*60;
        int sol = (int)(((double)(elapsedMillis/1000))/marsDaySecs) + 1;
        return sol;
    }

    /**
     * Calculate the Earth time in Java milliseconds from the spacecraft clock
     * Spacecraft clock is the number of seconds since January 1, 2000 at 11:58:55.816 UTC. 
     */
    public static long timeMillisFromRoverClock(int roverClock) {
    		// get spacecraft clock start time
    		Calendar cal = sol1StartCalendar(null);
    		long clockStartMillis = cal.getTimeInMillis();
    		long clockMillis = clockStartMillis + ((long)roverClock)*1000;
    		return clockMillis;
    }
    
    /**
     * Calculate the local Mars time for the specified rover from the Java (Earth)
     * time specified in milliseconds.
     */
    public static void marsTimeFromTimeMillis(String roverCode, long timeMillis, int[] marsTime) {
        Calendar cal = sol1StartCalendar(roverCode);
        long sol1StartTime = cal.getTimeInMillis();
        long elapsedMillis = timeMillis - sol1StartTime;
        marsTimeFromElapsedMillis(elapsedMillis, marsTime);
    }
    
    
    /**
     * Calculate the local Mars time from the number of milliseconds since 
     * the start of Sol 1.
     */
    private static void marsTimeFromElapsedMillis(long elapsedMillis, int[] marsTime) {
        double marsDaySecs = (double)35.244 + (double)24*60*60 + 39*60;
        double solDouble = ((double)(elapsedMillis/1000))/marsDaySecs;
        double hourDouble = (solDouble - ((long) solDouble)) * 24;
        double minuteDouble = (hourDouble - ((long)hourDouble)) * 60;
        double secondDouble = (minuteDouble - ((long)minuteDouble)) * 60;
        
        int sol = (int)(solDouble) + 1;
        int hour = (int)(hourDouble);
        int minute = (int)(minuteDouble);
        int second = (int)(secondDouble);
        marsTime[0] = sol;
        marsTime[1] = hour;
        marsTime[2] = minute;
        marsTime[3] = second;    		
    }
    

    public static int solFromRoverClock(String roverCode, int clock) {
        int sol1RoverClock = sol1StartRoverClock(roverCode);
        double marsDaySecs = 35.244 + 24*60*60 + 39*60;
        int sol = (int)(((double)(clock-sol1RoverClock))/marsDaySecs) + 1;
        return sol;
    }

    public static Integer roverClockIntegerFromFilename(String filename) {
    	return Integer.valueOf(filename.substring(2, 11));
    }
    
    public static int roverClockFromFilename(String filename) {
        return Integer.valueOf(filename.substring(2, 11)).intValue();
    }

    public static int solFromFilename(String filename) {
        String roverCode = roverCodeFromFilename(filename);
        int roverClock = roverClockFromFilename(filename);
        int sol = solFromRoverClock(roverCode, roverClock);
        return sol;
    }

    public static String roverCodeFromFilename(String filename) {
    	if (filename.charAt(1) == '1') {
    		return ROVERCODE_OPPORTUNITY;
    	}
    	else if (filename.charAt(1) == '2') {
    		return ROVERCODE_SPIRIT;
    	}
        return filename.substring(0,1);
    }

    public static String cameraCodeFromFilename(String filename) {
        return filename.substring(1,2).toLowerCase();
    }

    public static char cameraEyeFromFilename(String filename) {
        return filename.charAt(23);
    }
    
    public static int filterNumberFromFilename(String filename) {
    		char filterNumChar = filename.charAt(24);
    		if (filterNumChar == '0') {
    			return 0;
    		}
    		else {
    			return filterNumChar - '1' + 1;
    		}
    }
    
    public static String productTypeFromFilename(String filename) {
        return filename.substring(11,14);
    }
    
    public static String cmdSeqFromFilename(String filename) {
        return filename.substring(19,24);
    }
    
    public static boolean isOtherProductTypeFilename(String filename) {
    		String productType = productTypeFromFilename(filename);
    		if (productType.equalsIgnoreCase("EFF") || productType.equalsIgnoreCase("ESF")
    				|| productType.equalsIgnoreCase("EDN")) {
    			return false;
    		}
    		return true;
    }
    
    public static String localPathFrom(String roverCode, String cameraCode, int sol) {
        String path = roverCode + File.separator + cameraCode +
                    File.separator + MerUtils.zeroPad(sol, 3);
        return path;
    }

    public static String localPathnameFrom(String roverCode, String cameraCode,
                                           int sol, String filename) {
        String path = roverCode + File.separator + cameraCode +
                    File.separator + MerUtils.zeroPad(sol, 3) + File.separator
                    + filename;
        return path;
    }

    public static String exploratoriumUrlStringFrom(String roverCode) {
        String path = roverNameFromRoverCode(roverCode) + "/";
        String str = "http://nasa.exploratorium.edu/mars/"+path;
        return str;    		
    }
    
    public static String exploratoriumUrlStringFrom(String roverCode, String cameraCode) {
        String path = exploratoriumPathFrom(roverCode, cameraCode);
        String str = "http://nasa.exploratorium.edu/mars/"+path;
        return str;
    }

    public static String exploratoriumPathFrom(String roverCode, String cameraCode) {
        String path = roverNameFromRoverCode(roverCode) + "/";
        if (cameraCode.equals("n"))
            path += "navcam";
        else if (cameraCode.equals("f"))
            path += "forward_hazcam";
        else if (cameraCode.equals("r"))
            path += "rear_hazcam";
        else if (cameraCode.equals("p"))
            path += "pancam";
        else if (cameraCode.equals("m"))
            path += "micro_imager";
        path += "/";
        //path = path + "/" + year + "-" + zeroPad(month, 2) + "-" + zeroPad(day, 2) + "/";
        return path;
    }

    public static String jplUrlStringFromPathname(String pathname) {
    		String urlPathname = pathname.replace('\\', '/');
    		String urlString = "http://marsrovers.jpl.nasa.gov/gallery/all/"+urlPathname;
    		return urlString;
    }
    
    public static String roverNameFromRoverCode(String roverCode) {
        if (roverCode.equals("1"))
            return "opportunity";
        else if (roverCode.equals("2"))
            return "spirit";
        else
            return null;
    }

    public static int solFromPathname(String pathname) {
        int index = pathname.indexOf(File.separator);
        index = pathname.indexOf(File.separator, index + 1);
        int index2 = pathname.indexOf(File.separator, index+1);
        String str = pathname.substring(index + 1, index2);
        int sol = Integer.valueOf(str).intValue();
        return sol;
    }

    /**
     * Note this also returns our 'extended' camera codes (such as na or pc)
     */
    public static String cameraCodeFromPathname(String pathname) {
        int index = pathname.indexOf(File.separator);
        int index2 = pathname.indexOf(File.separator, index + 1);
        String str = pathname.substring(index + 1, index2);
        return str;
    }

    public static String filenameFromPathname(String pathname) {
        int index = pathname.indexOf(File.separator);
        index = pathname.indexOf(File.separator, index + 1);
        index = pathname.indexOf(File.separator, index + 1);
        return pathname.substring(index + 1, pathname.length());
    }
    
    public static String displayRoverNameFromRoverCode(String roverCode) {
        if (roverCode.equals("1"))
            return "Opportunity";
        else if (roverCode.equals("2"))
            return "Spirit";
        else
            return null;
    }

	public static final int IMAGECLASS_UNKNOWN = -1;
	public static final int IMAGECLASS_RAW = 0;
	public static final int IMAGECLASS_MMBANAGLYPH = 1;
	public static final int IMAGECLASS_MMBFALSECOLOR = 2;
	public static final int IMAGECLASS_DCCALIBRATEDCOLOR = 3;
	
	// TODO maybe move this somewhere more global
	public static int findImageClass(String filename) {
		try {
			filename = filename.toUpperCase();
			if (filename.length() >= 31) {
				if ((filename.endsWith(".JPG"))) {
					if (filename.length() == 31) {
						String roverCode = MerUtils.roverCodeFromFilename(filename);
						String cameraCode = MerUtils.cameraCodeFromFilename(filename);
						int sol = MerUtils.solFromFilename(filename);
						if ((filename.charAt(23)=='L') && (filename.charAt(25)=='R')
								&& (Character.isDigit(filename.charAt(24)))
								&& (Character.isDigit(filename.charAt(26)))) {
							return IMAGECLASS_MMBANAGLYPH;
						}
						else if (((filename.charAt(23)=='L') || (filename.charAt(23)=='R'))
								&& (Character.isDigit(filename.charAt(24)))
								&& (Character.isDigit(filename.charAt(25)))
								&& (Character.isDigit(filename.charAt(26)))) {
							return IMAGECLASS_MMBFALSECOLOR;
						}
					}
					if (filename.length() >= 31) {
						if ((filename.charAt(filename.length()-6) == 'C') 
								&& Character.isDigit(filename.charAt(filename.length()-5))
								&& MerUtils.productTypeFromFilename(filename).equals("RAD")) {
							String colorNumberSeq = filename.substring(24, filename.length()-6);
							try {
								Integer.parseInt(colorNumberSeq);
								return IMAGECLASS_DCCALIBRATEDCOLOR;								
							}
							catch (Exception e) {	
							}
						}
					}
					if (filename.length() == 31) {
						return IMAGECLASS_RAW;
					}
				}
			}
		}
		catch (Exception e) {	
		}
		return IMAGECLASS_UNKNOWN;
	}

    
    public static double round(double val, int precision) {
        return ((double)(Math.round(val * precision))) / precision;        
    }
    
    public static String siteFromFilename(String filename) {
    	return filename.substring(14, 16);
    }
    
    public static String driveFromFilename(String filename) {
    	return filename.substring(16, 18);
    }
    
    public static String siteDriveFromFilename(String filename) {
    	return filename.substring(14, 18);
    }
    
    public static boolean filenameMatchesSiteDrive(String filename, String siteDrive) {
    	return ((filename.charAt(14) == siteDrive.charAt(0))
    			&& (filename.charAt(15) == siteDrive.charAt(1))
    			&& (filename.charAt(16) == siteDrive.charAt(2))
    			&& (filename.charAt(17) == siteDrive.charAt(3)));
    }
    
    public static char spacecraftIdCharFromFilename(String filename) {
    	return filename.charAt(0);
    }
    
    public static char cameraFromFilename(String filename) {
    	return filename.charAt(1);
    }
    
    /**
     * TODO this probably shouldn't be in MerUtils
     * @param array
     * @return
     */
	public static String stringFromArray(String[] array) {
		if (array != null) {
			StringBuffer str = new StringBuffer("");
			for (int n=0; n<array.length; n++) {
				if (n>0) {
					str.append(" ");
				}
				if (array[n].length() > 0) {
					str.append(array[n]);
				}
			}
			return str.toString().trim();
		}
		else {
			return "";
		}
	}
	
	public static String[] arrayFromString(String str) {
		if ((str != null) && (str.length() > 0)) {
			return str.split("[ ,;]");
		}
		else {
			return null;
		}		
	}
    
}
