/*
 * Created on Jun 21, 2005
 *
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
package midnightmarsbrowser.metadata;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.StringTokenizer;
import java.util.Vector;

import midnightmarsbrowser.model.MerUtils;


/**
 * @author michaelhoward
 *
 */
public class OldPanModel {
	
	String filename = null;
	String title = null;
	int startSol = 0;		// these now need to be populate on load
	int endSol = 0;			// these now need to be populate on load
	int numEnabledImages = 0;	// WARNING these may be populated only when creating the model via fromElements
	
	boolean hasNavcamLeft = false;
	boolean hasNavcamRight = false;
	boolean hasNavcamAnaglyph = false;
	boolean hasPancamLeft = false;
	boolean hasPancamRight = false;
	boolean hasPancamAnaglyph = false;
	boolean hasPancamColor = false;
	boolean hasPancamDCColor = false;
	
	boolean hasRoverQuat = false;
	double roverQuatX = 0.0;
	double roverQuatY = 0.0;
	double roverQuatZ = 0.0;
	double roverQuatW = 0.0;
	
	double roverNorth = 0.0;
	
	OldPanModelElement[] elements = null;
	
	// navcam full FOV = 45.1766 <deg>  
	// pancam full FOV = 15.8412 <deg>

//	static final double navcamWidth =  0.384110014754 * 20;  //0.38410679187 * 20;
//	static final double navcamDistance = 0.923287331531 * 20; // 0.923288672322 * 20;
//	static final double pancamWidth = 0.137800662765 * 10; 
//	static final double pancamDistance = 0.990459982706 * 10;
	
//	static final double twoPI = Math.PI * 2;
	static final int significanceThresh = 512;
	
	public OldPanModel() {
	}

	/*
	public PanModelElement findElementClosestTo(double azimuthDeg, double elevationDeg) {
		PanModelElement closestElement = null;
		double distanceSq = 0.0;
		for (int n=0; n<elements.length; n++) {
			PanModelElement element = elements[n];
			if (element.actImagePathname != null) {
				double testDistanceSq = elementDistanceSq(azimuthDeg, elevationDeg, element.azimuthDeg, element.elevationDeg);
				if ((closestElement==null) || (testDistanceSq < distanceSq)) {
					distanceSq = testDistanceSq;
					closestElement = element;
				}
			}
		}
		return closestElement;
	}
	*/

	/*
	public static double elementDistanceSq(double azimuthDeg1, double elevationDeg1, double azimuthDeg2, double elevationDeg2) {
		double da = azimuthDeg1 - azimuthDeg2;
		while (da > 180)
			da -= 360;
		while (da < -180)
			da += 360;
		double de = elevationDeg1 - elevationDeg2;
		while (de > 180)
			de -= 360;
		while (de < -180)
			de += 360;
		double distanceSq = da*da + de*de;
		return distanceSq;
	}
	*/
	
	
	/**
	 * @param inElements
	 * @param oldModel if oldModel is not null, take editable properties from oldModel
	 */
	public static OldPanModel fromElements(Vector inElements, OldPanModel oldModel) {
		OldPanModel model = new OldPanModel(); 
		model.elements = new OldPanModelElement[inElements.size()];
		for (int n=0; n<inElements.size(); n++) {
			OldPanModelElement element = (OldPanModelElement) inElements.elementAt(n);
			model.elements[n] = element;
			if (element.enabled) {
				model.numEnabledImages ++;
			}
			int sol = MerUtils.solFromRoverClock(""+element.roverCode, element.startClock);
			if ((model.startSol==0) || (sol<model.startSol))
				model.startSol = sol;
			if ((model.endSol==0) || (sol>model.endSol))
				model.endSol = sol;
			if (element.cameraCode == 'n') {
				if (element.hasLeft) {
					model.hasNavcamLeft = true;
				}
				if (element.hasRight) {
					model.hasNavcamRight = true;
				}
				if (element.hasAnaglyph) {
					model.hasNavcamAnaglyph = true;
				}
			}
			else if (element.cameraCode == 'p') {
				if (element.hasLeft) {
					model.hasPancamLeft = true;
				}
				if (element.hasRight) {
					model.hasPancamRight = true;
				}
				if (element.hasAnaglyph) {
					model.hasPancamAnaglyph = true;
				}
				if (element.hasColor) {
					model.hasPancamColor = true;
				}
				if (element.hasDCColor) {
					model.hasPancamDCColor = true;
				}
			}
		}
		if (oldModel != null) {
			model.title = oldModel.title;
			model.roverNorth = oldModel.roverNorth;
			model.hasRoverQuat = oldModel.hasRoverQuat;
			model.roverQuatX = oldModel.roverQuatX;
			model.roverQuatY = oldModel.roverQuatY;
			model.roverQuatZ = oldModel.roverQuatZ;
			model.roverQuatW = oldModel.roverQuatW;
		}
		return model;
	}
	
	/**
	 * If compareFile is not null, will not write the file unless it's different
	 * from compareFile.
	 */
	public void writeToFile(File file, File compareFile) throws IOException {
		Writer writer = null;
		try {
			if ((compareFile == null) || (!compareFile.isFile())) {
				writer = new BufferedWriter(new FileWriter(file));
				compareFile = null;
			}
			else {
				writer = new StringWriter();
			}
			writer.write("mmbpan version 1\n");
			if (title != null) {
				writer.write("title "+title+"\n");
			}
			if (this.hasRoverQuat) {
				writer.write("roverQuaternion");
				writer.write(" "+roundToFourDecimalPlaces(roverQuatX));
				writer.write(" "+roundToFourDecimalPlaces(roverQuatY));
				writer.write(" "+roundToFourDecimalPlaces(roverQuatZ));
				writer.write(" "+roundToFourDecimalPlaces(roverQuatW));
				writer.write("\n");
			}
			if (this.roverNorth != 0.0) {
				writer.write("roverNorth ");
				writer.write(""+roundToFourDecimalPlaces(roverNorth));
				writer.write("\n");
			}
			for (int n=0; n<elements.length; n++) {
				OldPanModelElement element = elements[n];
				if (element != null) {
					String imageType = "" + element.cameraCode;
					if (element.hasLeft) {
						imageType += "l";
					}
					if (element.hasRight) {
						imageType += "r";
					}
					if (element.hasAnaglyph) {
						imageType += "a";
					}
					if (element.hasColor) {
						imageType += "c";
					}
					if (element.hasDCColor) {
						imageType += "s";
					}
					if (!element.enabled) {
						// writer.write("#");
						// don't write disabled elements - they disappear if we save the pan,
						// and then show up as differences when regenerating pans
						continue;
					}
					writer.write("img ");
					writer.write(element.roverCode);
					writer.write(" ");
					writer.write(imageType);
					if (element.startClock == element.endClock) {
						writer.write(" "+element.startClock);
					}
					else {
						writer.write(" "+element.startClock);
						writer.write("-"+element.endClock);
					}
					writer.write(" "+roundToFourDecimalPlaces(element.azimuthDeg));
					writer.write(" "+roundToFourDecimalPlaces(element.elevationDeg));
					writer.write(" "+element.left);
					writer.write(" "+element.top);
					writer.write(" "+element.width);
					writer.write(" "+element.height);
					writer.write("\n");
				}
			}
		}
		finally {
			 try {
			 	if (writer != null) {
			 		writer.flush();
			 		writer.close();			 		
			 	}
			} catch (IOException e1) {
				System.err.println("ERROR closing pan file: "+e1.toString());
			}
		}
		
		if (compareFile != null) {
			BufferedReader compareReader = null;
			StringWriter compareWriter = null;
			BufferedWriter finalWriter = null;
			try {
				compareReader = new BufferedReader(new FileReader(compareFile));
				compareWriter = new StringWriter();
				String compareLine = compareReader.readLine();
				while (compareLine != null) {
					compareWriter.write(compareLine);
					compareWriter.write("\n");
					compareLine = compareReader.readLine();
				}
				String compareContents = compareWriter.toString();
				String writeContents = writer.toString();
				if (!writeContents.trim().equals(compareContents.trim())) {
					finalWriter = new BufferedWriter(new FileWriter(file));
					finalWriter.write(writeContents);
				}
			}
			finally {
				if (compareReader != null) {
					compareReader.close();
				}
				if (compareWriter != null) {
					compareWriter.close();
				}
				if (finalWriter != null) {
					finalWriter.close();
				}
			}
		}
	}
	
	private static double roundToFourDecimalPlaces(double value) {
		return ((double)(Math.round(value*1000)))/1000;
	}
	
	/**
	 * @param file
	 * @param fullRead If true, we're just reading to create a PanListEntry.
	 * @return
	 * @throws Exception
	 */
	public static OldPanModel readPanModelFromFile(File file, boolean indexMode) throws Exception {
		OldPanModel model = null;
		if (file.isFile()) {
			BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(file));
    			String inputLine;
    			//String[] params = null;
    			while (((inputLine = reader.readLine()) != null) && (inputLine.startsWith("#"))) {
    				// absorb any comment lines at start
    			}
    			if (inputLine==null)
    				throw new Exception("File was not in mmbpan format; empty file.");
    			StringTokenizer toks = new StringTokenizer(inputLine);
    			int numToks = toks.countTokens();
    			if ((numToks !=3) || (!toks.nextToken().equalsIgnoreCase("mmbpan"))
    					|| (!toks.nextToken().equalsIgnoreCase("version"))) {
    				throw new Exception("File was not is mmbpan format; invalid version.");
    			}
    			int version = Integer.parseInt(toks.nextToken());
    			if (version > 1) {
    				throw new Exception("The pan file requires a later version of Midnight Mars Browser."
    						+" Please update to the latest version.");				
    			}
    			
    			model = new OldPanModel();
    			model.filename = file.getName();
    			Vector elements = null;
    			if (!indexMode) {
    				elements = new Vector();
    			}
    			while ((inputLine = reader.readLine()) != null) {
    				if (inputLine.startsWith("#")) {
    					// do nothing for comment line
    				}
    				else if ((inputLine.length()>5) && (inputLine.substring(0,6).equalsIgnoreCase("title "))) {
    					String title = inputLine.substring(6).trim();
    					model.title = title;
    				}
    				else {
    					toks = new StringTokenizer(inputLine);
    					numToks = toks.countTokens();
    					if (toks.hasMoreTokens()) {
    						String cmd = toks.nextToken();
    						if (cmd.equalsIgnoreCase("img")) {
    							if (numToks < 6) {
    								throw new Exception("Invalid img tag (not enough parameters) \""
    										+inputLine+"\"");
    							}
    							OldPanModelElement element = new OldPanModelElement();
    							String roverCodeString = toks.nextToken();
    							if (roverCodeString.length() > 1)
    								throw new Exception("Invalid rover code \""+roverCodeString+"\"");
    							element.roverCode = roverCodeString.charAt(0);
    							String imageTypeString = toks.nextToken().toLowerCase();
    							element.cameraCode = imageTypeString.charAt(0);
    							if (imageTypeString.indexOf("l")>0) {
    								element.hasLeft = true;
    							}
    							if (imageTypeString.indexOf("r")>0) {
    								element.hasRight = true;
    							}
    							if (imageTypeString.indexOf("a")>0) {
    								element.hasAnaglyph = true;
    							}
    							if (imageTypeString.indexOf("c")>0) {
    								element.hasColor = true;
    							}
    							if (imageTypeString.indexOf("s")>0) {
    								element.hasDCColor = true;
    							}
    							String timecodesString = toks.nextToken();
    							int dashIndex = timecodesString.indexOf("-");
    							if (dashIndex>=0) {
    								String startClockString = timecodesString.substring(0,dashIndex);
    								String endClockString = timecodesString.substring(dashIndex+1, timecodesString.length());
    								element.startClock = Integer.parseInt(startClockString);
    								element.endClock = Integer.parseInt(endClockString);
    							}
    							else {
    								element.startClock = Integer.parseInt(timecodesString);
    								element.endClock = element.startClock;
    							}
    							element.azimuthDeg = Double.parseDouble(toks.nextToken());
    							element.elevationDeg = Double.parseDouble(toks.nextToken());
    							if (toks.hasMoreTokens()) {
    								element.left = Integer.parseInt(toks.nextToken());
    								element.top = Integer.parseInt(toks.nextToken());
    								element.width = Integer.parseInt(toks.nextToken());
    								element.height = Integer.parseInt(toks.nextToken());
    							}
    							if (element.cameraCode == 'n') {
    								if (element.hasLeft) {
    									model.hasNavcamLeft = true;
    								}
    								if (element.hasRight) {
    									model.hasNavcamRight = true;
    								}
    								if (element.hasAnaglyph) {
    									model.hasNavcamAnaglyph = true;
    								}
    							}
    							else if (element.cameraCode == 'p') {
    								if (element.hasLeft) {
    									model.hasPancamLeft = true;
    								}
    								if (element.hasRight) {
    									model.hasPancamRight = true;
    								}
    								if (element.hasAnaglyph) {
    									model.hasPancamAnaglyph = true;
    								}
    								if (element.hasColor) {
    									if ((element.width >= significanceThresh) || (element.height >= significanceThresh)) {
    										model.hasPancamColor = true;
    									}
    								}
    								if (element.hasDCColor) {
    									if ((element.width >= significanceThresh) || (element.height >= significanceThresh)) {
    										model.hasPancamDCColor = true;
    									}
    								}
    							}
    							int sol = MerUtils.solFromRoverClock(MerUtils.roverCodeStringFromChar(element.roverCode), element.startClock);
    							if ((model.startSol==0) || (sol<model.startSol))
    								model.startSol = sol;
    							if ((model.endSol==0) || (sol>model.endSol))
    								model.endSol = sol;
    							if (!indexMode) {								
    								elements.add(element);
    							}
    						}
    						else if (cmd.equalsIgnoreCase("roverQuaternion")) {
    							if (!indexMode) {
    								if (numToks < 5) {
    									throw new Exception("Invalid roverQuaternion tag (not enough parameters) \""
    											+inputLine+"\"");
    								}
    								model.roverQuatX = Double.parseDouble(toks.nextToken());
    								model.roverQuatY = Double.parseDouble(toks.nextToken());
    								model.roverQuatZ = Double.parseDouble(toks.nextToken());
    								model.roverQuatW = Double.parseDouble(toks.nextToken());
    								model.hasRoverQuat = true;
    							}
    						}
    						else if (cmd.equalsIgnoreCase("roverNorth")) {
    							if (numToks < 2) {
    								throw new Exception("Invalid roverNorth tag (not enough parameters) \""
    										+inputLine+"\"");
    							}
    							model.roverNorth = Double.parseDouble(toks.nextToken());
    						}
    					}
    				}
    			}
    			if (!indexMode) {
    				model.elements = (OldPanModelElement []) elements.toArray(new OldPanModelElement[] {});
    			}
            }
            finally {
                reader.close();
            }
		}
		return model;
	}
	
	static OldPanModelElement hasMatchingElement(Vector elements, OldPanModelElement srcElement, double tolerance) {
		for (int n=0; n<elements.size(); n++) {
			OldPanModelElement checkElement = (OldPanModelElement) elements.elementAt(n);
			if ((checkElement.enabled) && (checkElement.cameraCode == srcElement.cameraCode)) {
				double da = checkElement.azimuthDeg - srcElement.azimuthDeg;
				double de = checkElement.elevationDeg - srcElement.elevationDeg;
				if ((da<tolerance) && (da>-tolerance) && (de<tolerance) && (de>-tolerance)) {
					return checkElement;
				}
			}
		}
		return null;
	}
}
