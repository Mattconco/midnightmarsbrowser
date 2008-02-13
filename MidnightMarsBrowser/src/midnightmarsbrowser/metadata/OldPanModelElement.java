/*
 * Created on Jun 20, 2005
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

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Vector;

import javax.imageio.ImageIO; // import javax.media.j3d.Texture2D;

import midnightmarsbrowser.model.MerUtils;

/**
 * @author michaelhoward
 * 
 */
public class OldPanModelElement {
	// abstract values
	boolean enabled = true; // used in pan generation
	char roverCode;
	char cameraCode;
	boolean hasLeft = false;
	boolean hasRight = false;
	boolean hasAnaglyph = false;
	boolean hasColor = false;
	boolean hasDCColor = false;
	boolean hasSolarFilter = false;
	int startClock = 0;
	int endClock = 0;
	double azimuthDeg = 0.0;
	double elevationDeg = 0.0;
	int left = 0;
	int top = 0;
	int width = 1024;
	int height = 1024;

	// "live" values
	String actImagePathname = null; // discovered based on parameters
	// byte[] tempImageData = null; // holds the loaded file temporarily so we
	// don't need to reload it over and over
	// Texture2D newImage = null; // set when we have a new image to put into
	// the image plane
	// PanImageTG tgRef = null;
	int destretchLowVal = 0;
	int destretchHighVal = 256;
	int devigA = 0;
	int devigB = 0;
	byte[] tempThumbnailData = null; // holds the loaded raw image thumbnail
										// file so we don't need to reload

	// it from disk while editing brightnesses

	/*
	 * public File imagePathnameFile() { File imageDir; if
	 * (isImagePathnameRawImage()) imageDir =
	 * MyPreferences.instance().rawImageDir; else imageDir =
	 * MyPreferences.instance().generatedImageDir; File file = new
	 * File(imageDir, this.actImagePathname); return file; }
	 */
	/*
	 * public boolean isImagePathnameRawImage() { return
	 * (MerUtils.cameraCodeFromPathname(this.actImagePathname).length()==1); }
	 */

	/**
	 * See findPancamImageTypesAndTimecodes for the flavor of what this does.
	 * 
	 * @param file
	 * @param files
	 * @param anaglyphFiles
	 */
	void findNavcamImageTypesAndTimecode(File file, File[] files,
			File[] anaglyphFiles) {
		String matchString = file.getName().substring(0, 11);
		int newClock = MerUtils.roverClockFromFilename(file.getName());
		this.startClock = newClock;
		this.endClock = newClock;
		// see if there is left, right
		for (int i = 0; i < files.length; i++) {
			File file2 = files[i];
			if (file2.isFile() && (file2.getName().length() == 31)) {
				if (file2.getName().substring(0, 11).equalsIgnoreCase(
						matchString)) {
					char cameraEye = MerUtils.cameraEyeFromFilename(file2
							.getName());
					if ((cameraEye == 'L') || (cameraEye == 'l')) {
						this.hasLeft = true;
					} else if ((cameraEye == 'R') || (cameraEye == 'r')) {
						this.hasRight = true;
					}
				}
			}
		}
		// see if there is an anaglyph
		if (anaglyphFiles != null) {
			for (int i = 0; i < anaglyphFiles.length; i++) {
				File file2 = anaglyphFiles[i];
				if (file2.isFile() && (file2.getName().length() == 31)) {
					if (file2.getName().substring(0, 11).equalsIgnoreCase(
							matchString)) {
						this.hasAnaglyph = true;
					}
				}
			}
		}
	}

	/**
	 * Finds the image types and timecode range for the element based on the
	 * image set passed in matchedFiles. Returns the first file in the
	 * matchedFiles image set (so that we can parse it for image tags in
	 * Update.doGenPanFiles()).
	 * 
	 * @param matchedFiles
	 *            a LinkedHashMap of eyefilter->filename, a single set as
	 *            discovered by Update.findPancamImageSets()
	 * 
	 * @param anaglyphFiles
	 *            is an array of all the files from the "pa" directory, which is
	 *            scanned to see if there is an anaglyph file corresponding to
	 *            this image set
	 * 
	 * @param colorFiles
	 *            is an array of all the files from the "pc" directory
	 */
	File findPancamImageTypesAndTimecodes(LinkedHashMap matchedFiles,
			File[] anaglyphFiles, File[] colorFiles, File[] dcColorFiles) {
		File firstFile = null;
		Iterator iter = matchedFiles.keySet().iterator();
		File file = null;
		while (iter.hasNext()) {
			String key = (String) iter.next();
			file = (File) matchedFiles.get(key);
			char cameraEye = MerUtils.cameraEyeFromFilename(file.getName());
			int filterNumber = MerUtils
					.filterNumberFromFilename(file.getName());
			if (firstFile == null) {
				firstFile = file;
			} else {
				// take the first left eye file in the sequence, otherwise the
				// first file period
				char firstFileCameraEye = MerUtils
						.cameraEyeFromFilename(firstFile.getName());
				if (((firstFileCameraEye == 'R') || (firstFileCameraEye == 'r'))
						&& ((cameraEye == 'L') || (cameraEye == 'l'))) {
					firstFile = file;
				}
			}
			if ((cameraEye == 'L') || (cameraEye == 'l')) {
				this.hasLeft = true;
			} else if ((cameraEye == 'R') || (cameraEye == 'r')) {
				this.hasRight = true;
			}
			if (filterNumber == 8) {
				this.hasSolarFilter = true;
			}
			int time = MerUtils.roverClockFromFilename(file.getName());
			if ((startClock == 0) || (time < startClock))
				this.startClock = time;
			if ((endClock == 0) || (time > endClock))
				this.endClock = time;
		}
		// see if there is an anaglyph
		if (anaglyphFiles != null) {
			for (int i = 0; i < anaglyphFiles.length; i++) {
				File file2 = anaglyphFiles[i];
				if (file2.isFile() && (file2.getName().length() == 31)) {
					int time = MerUtils.roverClockFromFilename(file2.getName());
					if ((time >= startClock) && (time <= endClock)) {
						this.hasAnaglyph = true;
					}
				}
			}
		}
		// see if there is a false-color
		if (colorFiles != null) {
			for (int i = 0; i < colorFiles.length; i++) {
				File file2 = colorFiles[i];
				if (file2.isFile() && (file2.getName().length() == 31)) {
					int time = MerUtils.roverClockFromFilename(file2.getName());
					if ((time >= startClock) && (time <= endClock)) {
						this.hasColor = true;
					}
				}
			}
		}
		// see if there is a DC color file
		if (dcColorFiles != null) {
			for (int i = 0; i < dcColorFiles.length; i++) {
				File file2 = dcColorFiles[i];
				if (file2.isFile() && (file2.getName().length() >= 31)) {
					int time = MerUtils.roverClockFromFilename(file2.getName());
					if ((time >= startClock) && (time <= endClock)) {
						this.hasDCColor = true;
					}
				}
			}
		}
		return firstFile;
	}

	void bestGuessDimensions(File firstFile) {
		// if Subframe, find the best guess dimensions
		if (MerUtils.productTypeFromFilename(firstFile.getName())
				.equalsIgnoreCase("ESF")) {
			try {
				BufferedImage temp = ImageIO.read(firstFile);
				if (temp != null) {
					this.width = temp.getWidth();
					this.height = temp.getHeight();
					this.left = 512 - this.width / 2;
					this.top = 512 - this.height / 2;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * See if a Vector of PanModelElements already contains an element matching
	 * the timecodes of this element, and if so update the existing element
	 * based on this element. If not, return false, the element is does not
	 * existing in any form in the Vector.
	 * 
	 * @param elements
	 *            the Vector of existing PanModelElements
	 * @return true if the element was merged into the elements Vector, false if
	 *         there was not match.
	 */
	public boolean mergeIntoExistingElements(Vector elements) {
		for (int n = 0; n < elements.size(); n++) {
			OldPanModelElement testElement = (OldPanModelElement) elements
					.elementAt(n);
			if (testElement.roverCode != roverCode)
				continue;
			if (testElement.cameraCode != cameraCode)
				continue;
			if ((endClock < testElement.startClock)
					|| (startClock > testElement.endClock)) {
				continue;
			}
			testElement.startClock = startClock;
			testElement.endClock = endClock;
			testElement.hasAnaglyph = hasAnaglyph;
			testElement.hasColor = hasColor;
			testElement.hasLeft = hasLeft;
			testElement.hasRight = hasRight;
			testElement.left = left;
			testElement.top = top;
			testElement.width = width;
			testElement.height = height;
			return true;
		}
		return false;
	}

}
