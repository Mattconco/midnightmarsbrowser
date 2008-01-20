package midnightmarsbrowser.application;

import org.eclipse.swt.graphics.ImageData;

public class ImageUtils {

	/**
	 * Fill a byte[] table with de-stretching values.
	 * 
	 * @param map
	 * @param srcLowVal
	 * @param srcHighVal
	 * okay, stretching table would be 
	 * 	 stretchVal = (srcVal-srcLowVal)/(srcHighVal-srcLowVal)*256
	 * where stretchVal is between 0 and 256 and srcLowVal and srcHighVal 
	 * specify the lowest and highest+1 srcVal values used in the src image.
	 * So... to de-strech, solve above for srcVal:
	 * 	 srcVal = stretchVal * (srcHighVal-srcLowVal) / 256 + lowVal
	 * We fill this table's integers with the srcVal value copied into the r,g,b bytes of 
	 * the integer, for efficiency in mapping grayscale to grayscale.
	 */
	private static void fillDestretchPixelByteTable(byte[] map, int srcLowVal, int srcHighVal) {
		double convert = ((double)(srcHighVal-srcLowVal)) / 256;
		int srcVal;
		for (int n=0; n<map.length; n++) {
			srcVal = ((int)(convert * n)) + srcLowVal;
			if (srcVal < 0)
				srcVal = 0;
			else if (srcVal >= 256)
				srcVal = 255;
    		map[n] = (byte) srcVal;
		}
	}
		
	private static void fillDestretchPixelIntTable(int[] map, int srcLowVal, int srcHighVal) {
		double convert = ((double)(srcHighVal-srcLowVal)) / 256;
		int srcVal;
		for (int n=0; n<map.length; n++) {
			srcVal = ((int)(convert * n)) + srcLowVal;
            map[n] = srcVal;
		}
	}
	
    public static void destretchGrayscaleImageData(ImageData imageData, 
    		int srcLowVal, int srcHighVal,
            int devigA, int devigB) {
    	if (imageData.depth != 24) {
    		throw new Error("ImageData was "+imageData.depth+"-bit not 24-bit");
    	}
        byte[] data = imageData.data;
        int index = 0;
        int bytesPerLine = imageData.bytesPerLine;
        int width = imageData.width;
        int height = imageData.height;
        int lineAdd = bytesPerLine - width*3;
        byte byteVal = 0;
        if ((devigA == 0) && (devigB == 0)) {
        	byte[] stretchTable = new byte[256];
            fillDestretchPixelByteTable(stretchTable, srcLowVal, srcHighVal);
            int val;
            for (int y=0; y<height; y++) {
                for (int x=0; x<width; x++) {
                	val = ((int)data[index]) & 0xFF;                	
                	byteVal = stretchTable[val];
                	data[index++] = byteVal;
                	data[index++] = byteVal;
                	data[index++] = byteVal;
                }
                index += lineAdd;
            }
        }
        else { // de-vignetting, too
            // the sin function version
        	int[] stretchTable = new int[256];
            fillDestretchPixelIntTable(stretchTable, srcLowVal, srcHighVal);
            int val;
            int halfW = width/2;
            for (int x=0; x<width; x++) {
                index = x*3;
                double factor = Math.cos(Math.PI  * (x - halfW) / width);  // cos from -pi/2 to pi/2
                factor = 1.0 - factor;
                int factorA = (int)(factor  * devigA);
                int factorB = devigB * x / width;
                for (int y=0; y<height; y++) {
                    //val = (int)((multFactor * devigA * val) + (multFactor * devigB) + val);
                	val = ((int)data[index]) & 0xFF;
                    val = stretchTable[val] + factorA + factorB;
                    if (val >= 256) {
                    	byteVal = (byte) 0xFF;
                    }
                    else if (val < 0) {
                    	byteVal = 0;
                    }
                    else {
                    	byteVal = (byte) val;
                    }
                    data[index] = byteVal;
                    data[index+1] = byteVal;
                    data[index+2] = byteVal;
                    index += bytesPerLine;
                }
            }
        }
    }
    
	
	
	/**
	 * Combine a left and right image into an anaglyph. Assumes the images are
	 * grayscale and the same size.
	 */
	public static ImageData makeAnaglyphImage(
			ImageData leftImageData, int leftStretchMin, int leftStretchMax, int leftDevigA, int leftDevigB,
			ImageData rightImageData, int rightStretchMin, int rightStretchMax, int rightDevigA, int rightDevigB,
			double shiftPercent, int trimH)
			throws Exception {
//		startup();
//		System.out.println("makeAnaglyphImage");
		int width = leftImageData.width;
		int height = leftImageData.height;
		if ((rightImageData.width != width)
				|| (rightImageData.height != height)) {
			if (leftImageData.width < rightImageData.width) {
				// blow up left image to match right image
				leftImageData = leftImageData.scaledTo(rightImageData.width, rightImageData.height);
				width = rightImageData.width;
				height = rightImageData.height;
			} else {
				rightImageData = rightImageData.scaledTo(leftImageData.width, leftImageData.height);
			}
		}
//		System.out.println("makeAnaglyphImage "+leftImageData.width+" "+leftImageData.height+" "+leftImageData.depth);
		
		ImageData anaImageData = null;
		anaImageData = new ImageData(leftImageData.width, leftImageData.height, leftImageData.depth, leftImageData.palette);	
//		System.out.println("after new ImageData");
		
		// TODO this is the simplest case, for dev purposes
		int[] leftRow = new int[width];
		int[] rightRow = new int[width];
		int[] anaRow = new int[width];
		for (int y=0; y<height; y++) {
			leftImageData.getPixels(0, y, width, leftRow, 0);
			rightImageData.getPixels(0, y, width, rightRow, 0);
			for (int x=0; x<width; x++) {
				anaRow[x] = (rightRow[x] & 0x00FFFF00) | (leftRow[x] & 0xFF0000FF);
			}
			anaImageData.setPixels(0, y, width, anaRow, 0);
		}
		return anaImageData;
/*		
		int shift = (int) (shiftPercent * width);
		int twoShift = shift + shift;
//		int anaWidth = width + shift;
		int anaWidth = width - shift;
		int centerWidth = width - shift;
		int twoTrimH = trimH + trimH;
		leftImage.getRGB(0, 0, width, height, rgbArray1, 0, rgbArrayScansize);
		if ((leftStretchMin !=0) || (leftStretchMax != 256)) {
			destretchGrayscaleArray(rgbArray1, width, height, rgbArrayScansize, 
					leftStretchMin, leftStretchMax, leftDevigA, leftDevigB);
		}
		rightImage.getRGB(0, 0, width, height, rgbArray2, 0, rgbArrayScansize);
		if ((rightStretchMin !=0) || (rightStretchMax != 256)) {
			destretchGrayscaleArray(rgbArray2, width, height, rgbArrayScansize, 
					rightStretchMin, rightStretchMax, rightDevigA, rightDevigB);
		}
		// do the center (overlapped) section
		int leftIndex = shift + trimH;
		int rightIndex = 0 + trimH;
//		int anaIndex = shift + trimH;
		int anaIndex = trimH;
		int xWidth = centerWidth - twoTrimH;
		int rowAdd = shift + twoTrimH + (1024-width);
//		int anaRowAdd = twoShift + twoTrimH + (2048-anaWidth);
		int anaRowAdd = twoTrimH + (2048-anaWidth);
		int leftPixel;
		int rightPixel;
		int anaPixel;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < xWidth; x++) {
				leftPixel = rgbArray1[leftIndex];
				rightPixel = rgbArray2[rightIndex];
				//if (true) {
					anaPixel = (rightPixel & 0x0000FFFF)
							| (leftPixel & 0xFFFF0000);
				//} else {
					//int rightVal = (int) (0.75 * (rightPixel & 0x000000FF));
					//anaPixel = rightVal | (rightVal << 8)
					//		| (leftPixel & 0xFFFF0000);
				//}
				rgbArrayDouble[anaIndex] = anaPixel;
				leftIndex++;
				rightIndex++;
				anaIndex++;
			}
			leftIndex += rowAdd;
			rightIndex += rowAdd;
			anaIndex += anaRowAdd;
		}
		*/
		// do the left section
		// Note that this technique leaves trimH pixels *unset* on the left and right,
		// so we need to take that into account when we create the anaImage BufferedImage, below.
/*		leftIndex = trimH;
		anaIndex = trimH;
		xWidth = shift;
		rowAdd = width - shift + (1024-width);
		anaRowAdd = width + (2048-anaWidth);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < xWidth; x++) {
				leftPixel = rgbArray1[leftIndex];
				anaPixel = (leftPixel & 0xFFFF0000);
				rgbArrayDouble[anaIndex] = anaPixel;
				leftIndex++;
				anaIndex++;
			}
			leftIndex += rowAdd;
			anaIndex += anaRowAdd;
		}
*/
		// do the right section
/*		rightIndex = width - shift - trimH;
		anaIndex = width - trimH;
		xWidth = shift;
		rowAdd = width - shift + (1024-width);
		anaRowAdd = width + (2048 - anaWidth);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < xWidth; x++) {
				rightPixel = rgbArray2[rightIndex];
				anaPixel = (rightPixel & 0xFF00FFFF);
				rgbArrayDouble[anaIndex] = anaPixel;
				rightIndex++;
				anaIndex++;
			}
			rightIndex += rowAdd;
			anaIndex += anaRowAdd;
		}
*/
		/*
		BufferedImage anaImage = new BufferedImage(anaWidth-twoTrimH, height,
				BufferedImage.TYPE_INT_RGB);
		anaImage.setRGB(0, 0, anaWidth-twoTrimH, height, rgbArrayDouble, trimH, rgbArrayDoubleScansize);
		leftImage.flush();
		rightImage.flush();
		return anaImage;
		*/
	}

	public static ImageData makeColorImage(
			ImageData redImageData, int redStretchMin, int redStretchMax, int redDevigA, int redDevigB,
			ImageData greenImageData, int greenStretchMin, int greenStretchMax, int greenDevigA, int greenDevigB,
			ImageData blueImageData, int blueStretchMin, int blueStretchMax, int blueDevigA, int blueDevigB,
			int trimH) throws Exception {
		int twoTrimH = trimH + trimH;
		int width = redImageData.width;
		int height = redImageData.height;
		if (greenImageData.width > width) {
			width = greenImageData.width;
			height = greenImageData.height;
		}
		if (blueImageData.width > width) {
			width = blueImageData.width;
			height = blueImageData.height;
		}
		if ((redImageData.width != width)
				|| (redImageData.height != height)) {
			redImageData = redImageData.scaledTo(width, height);
		}
		if ((greenImageData.width != width)
				|| (greenImageData.height != height)) {
			greenImageData = greenImageData.scaledTo(width, height);
		}
		if ((blueImageData.width != width)
				|| (blueImageData.height != height)) {
			blueImageData = blueImageData.scaledTo(width, height);
		}
		ImageData colorImageData = new ImageData(width, height, blueImageData.depth, blueImageData.palette);
		// TODO this is the simplest case to get it working for development purposes
		int[] redRow = new int[width];
		int[] greenRow = new int[width];
		int[] blueRow = new int[width];
		int[] colorRow = new int[width];
		for (int y=0; y<height; y++) {
			redImageData.getPixels(0, y, width, redRow, 0);
			greenImageData.getPixels(0, y, width, greenRow, 0);
			blueImageData.getPixels(0, y, width, blueRow, 0);
			for (int x=0; x<width; x++) {
				colorRow[x] = (blueRow[x] & 0x00ff0000)
					| (greenRow[x] & 0x0000ff00)
					| (redRow[x] & 0x000000ff);
			}
			colorImageData.setPixels(0, y, width, colorRow, 0);
		}
		return colorImageData;
/*		
		redImage.getRGB(0, 0, width, height, rgbArray1, 0, rgbArrayScansize);
		if ((redStretchMin !=0) || (redStretchMax != 256)) {
			destretchGrayscaleArray(rgbArray1, width, height, rgbArrayScansize, 
					redStretchMin, redStretchMax, redDevigA, redDevigB);
		}
		greenImage.getRGB(0, 0, width, height, rgbArray2, 0, rgbArrayScansize);
		if ((greenStretchMin !=0) || (greenStretchMax != 256)) {
			destretchGrayscaleArray(rgbArray2, width, height, rgbArrayScansize, 
					greenStretchMin, greenStretchMax, greenDevigA, greenDevigB);   
		}
		blueImage.getRGB(0, 0, width, height, rgbArrayDouble, 0, rgbArrayDoubleScansize);
		if ((blueStretchMin !=0) || (blueStretchMax != 256)) {
			destretchGrayscaleArray(rgbArrayDouble, width, height, rgbArrayDoubleScansize, 
					blueStretchMin, blueStretchMax, blueDevigA, blueDevigB);
		}
		// do the center (overlapped) section
		int index = 0;	
		int blueIndex = 0; // use the first half of the double-size int array
		int tgtIndex = rgbArrayScansize; // use the second half of the double-size int array
		int redPixel;
		int greenPixel;
		int bluePixel;
		int colorPixel;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				rgbArrayDouble[tgtIndex] = (rgbArray1[index] & 0x00ff0000)
						| (rgbArray2[index] & 0x0000ff00)
						| (rgbArrayDouble[blueIndex] & 0x000000ff);
				index++;
				blueIndex++;
				tgtIndex++;
			}
			index = index + (rgbArrayScansize-width);
			blueIndex = blueIndex + (rgbArrayDoubleScansize-width);
			tgtIndex = tgtIndex + (rgbArrayDoubleScansize-width);
		}
		BufferedImage colorImage = new BufferedImage(width-twoTrimH, height,
				BufferedImage.TYPE_INT_RGB);
		colorImage.setRGB(0, 0, width-twoTrimH, height, rgbArrayDouble, rgbArrayScansize+trimH, 
				rgbArrayDoubleScansize);
		redImage.flush();
		greenImage.flush();
		blueImage.flush();
		return colorImage;
		*/
	}	
}
