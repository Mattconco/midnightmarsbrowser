package midnightmarsbrowser.editors;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import midnightmarsbrowser.application.ImageUtils;
import midnightmarsbrowser.application.MMBWorkspace;
import midnightmarsbrowser.metadata.ImageStretchMetadataEntry;
import midnightmarsbrowser.model.ImageEntry;
import midnightmarsbrowser.util.PDSIMG;

import org.eclipse.swt.examples.openglview.ImageDataUtil;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;

class PanImageLoader implements Runnable {

	MMBWorkspace workspace;
	PanoramaCanvas canvas;
	private boolean suspended = true;
	private boolean shutdown = false;
	private int maxResolution = 1024;
	boolean mipmap = true;
	boolean loading = false;
	private int pdsBrightnessMinValue;
	private int pdsBrightnessMaxValue;
	
	PanImageLoader(MMBWorkspace workspace, PanoramaCanvas panoramaCanvas) {
		this.workspace = workspace;
		this.canvas = panoramaCanvas;
	}
	
	public int getMaxResolution() {
		return maxResolution;
	}
	
	public void setMaxResolution(int maxResolution) {
		this.maxResolution = maxResolution;
	}

	public void setPDSBrightnessMinValue(int pdsBrightnessMinValue) {
		this.pdsBrightnessMinValue = pdsBrightnessMinValue;
	}

	public void setPDSBrightnessMaxValue(int pdsBrightnessMaxValue) {
		this.pdsBrightnessMaxValue = pdsBrightnessMaxValue;
	}
	
	public void suspend() {
		suspended = true;
		loading = false;
	}
	
	public void resume() {
		suspended = false;
		loading = true;
	}

	public void shutdown() {
		suspended = true;
		shutdown = true;
	}
		
	public void run() {
		while (!shutdown) {
			PanImageEntry foundPanImageEntry = null;
			
			if (suspended) {
				try {
					Thread.sleep(10);
				}
				catch (Throwable e) {					
				}
				continue;
			}
			
			Thread.yield();
			
			synchronized(this) {
				PanImageEntry[] sortedPanImageList = canvas.sortedPanImageList;
				for (int n=0; n<sortedPanImageList.length; n++) {
					PanImageEntry panImageEntry = sortedPanImageList[n];
					if ((panImageEntry.imageListEntry != null)
							&& (panImageEntry.imageListEntry.enabled)
							&& (!panImageEntry.loaded)
							&& (panImageEntry.loadImageBuffer == null)) {
						foundPanImageEntry = panImageEntry;
						break;
					}
				}
			}
			
	        if (suspended) 
	        	continue;
			
			if (foundPanImageEntry != null) {
				loading = true;
				
				try {
					ImageEntry entry = foundPanImageEntry.imageListEntry;
					ImageData imageData = null;
					if (entry.isIMG()) {
						PDSIMG img = PDSIMG.readIMGFile(entry.getFile(), pdsBrightnessMinValue, pdsBrightnessMaxValue);
						byte[] imageBytes = img.getImageByteArray();
						PaletteData palette = new PaletteData(0xFF , 0xFF , 0xFF);
						imageData = new ImageData(img.getLineSamples(),img.getLines(),8,palette,1,imageBytes);
					}
					else {
				        imageData = new ImageData(entry.getFile());
					}
				        
					Thread.yield();
			        if (suspended) 
			        	continue;
			        
					ImageStretchMetadataEntry stretch = workspace.getImageStretchMetadata().getEntry(entry.getFilename());
					if (stretch != null) {
						ImageUtils.destretchGrayscaleImageData(imageData, stretch.minVal, stretch.maxVal, stretch.devigA, stretch.devigB);
					}
					
					Thread.yield();
			        if (suspended) 
			        	continue;
					
			        int newWidth = fixDimension(imageData.width);
			        int newHeight = fixDimension(imageData.height);
			        while ((newWidth > maxResolution) || (newHeight > maxResolution)) {
			        	newWidth = newWidth >> 1;
			        	newHeight = newHeight >> 1;
			        }
					if ((imageData.width != newWidth) || (imageData.height != newHeight)) {
						imageData = imageData.scaledTo(newWidth, newHeight);
					}
					
					Thread.yield();
			        if (suspended) 
			        	continue;
					
			        ImageData convImageData = ImageDataUtil.convertImageData(imageData);
//			        System.out.println("convImageData.depth=="+convImageData.depth+" bytesPerLine=="+convImageData.bytesPerLine);
//			        System.out.println("convImageData.width=="+convImageData.width);
//			        System.out.println("convert time = "+(stopTime-startTime));
			        			        
					Thread.yield();
			        if (suspended) 
			        	continue;
			        
			        ArrayList loadImageWidth = new ArrayList();
			        ArrayList loadImageHeight = new ArrayList();
			        ArrayList loadImageBuffer = new ArrayList();
			        
			        while (true) {
				        ByteBuffer convImageBuffer = null;
				        try {
					        convImageBuffer = ByteBuffer.allocateDirect(convImageData.data.length);
				        }
				        catch (Throwable e) {
				        	e.printStackTrace();
							return;
				        }
				        convImageBuffer.put(convImageData.data);
				        convImageBuffer.flip();
			        	loadImageWidth.add(new Integer(convImageData.width));
			        	loadImageHeight.add(new Integer(convImageData.height));
				        loadImageBuffer.add(convImageBuffer);
				        
				        if ((!mipmap) || ((convImageData.width==1) && convImageData.height==1)) {
				        	break;
				        }
				        else {
				        	newWidth = convImageData.width / 2;
				        	if (newWidth < 1) 
				        		newWidth = 1;
				        	newHeight = convImageData.height / 2;
				        	if (newHeight < 1) 
				        		newHeight = 1;
				        	if ((convImageData.width > 1) && (convImageData.height > 1)) {
				        		PaletteData pd = new PaletteData(convImageData.palette.redMask, convImageData.palette.greenMask, convImageData.palette.blueMask);
				        		ImageData newImageData = new ImageData(newWidth, newHeight, convImageData.depth, pd);
				        		byte[] srcData = convImageData.data;
				        		byte[] dstData = newImageData.data;
				                int srcBytesPerLine = convImageData.bytesPerLine;
				                int srcLineAdd = srcBytesPerLine - convImageData.width*3;
				                int dstBytesPerLine = newImageData.bytesPerLine;
				                int dstLineAdd = dstBytesPerLine - newImageData.width*3;				        		
				        		int srcPtr = 0;
				        		int dstPtr = 0;
				        		int bytes[][] = new int[2][6];
				        		for (int y=0; y<newHeight; y++) {
				        			for (int x=0; x<newWidth; x++) {
				        				for (int i=0; i<6; i++) {
				        					bytes[0][i] = ((int)srcData[srcPtr]) & 0xFF;
				        					bytes[1][i] = ((int)srcData[srcPtr+srcBytesPerLine]) & 0xFF;
				        					srcPtr++;
				        				}
				        				dstData[dstPtr++] = (byte)((bytes[0][0] + bytes[0][3] + bytes[1][0] + bytes[1][3]) >> 2);
				        				dstData[dstPtr++] = (byte)((bytes[0][1] + bytes[0][4] + bytes[1][1] + bytes[1][4]) >> 2);
				        				dstData[dstPtr++] = (byte)((bytes[0][2] + bytes[0][5] + bytes[1][2] + bytes[1][5]) >> 2);
				        			}
				        			srcPtr = srcPtr + srcLineAdd + srcBytesPerLine;
				        			dstPtr += dstLineAdd;
				        		}
				        		convImageData = newImageData;
				        	}
				        	else {
					        	convImageData = convImageData.scaledTo(newWidth, newHeight);
				        	}
				        }
						Thread.yield();
			        }
			        
			        foundPanImageEntry.loadImageWidth = loadImageWidth;
			        foundPanImageEntry.loadImageHeight = loadImageHeight;
			        foundPanImageEntry.loadImageBuffer = loadImageBuffer;
			        canvas.getDisplay().asyncExec(new PanoramaCanvasRefresh(canvas));			        
				}
				catch (Throwable e) {
					// TODO is this right?
					foundPanImageEntry.loaded = true;
					e.printStackTrace();
				}
			}
			else {
				try {
					Thread.sleep(10);
				}
				catch (Throwable e) {					
				}
				loading = false;
			}
		}
		loading = false;
		workspace = null;
		canvas = null;
	}
	
	static int fixDimension(int val) {
		int newVal = 1024;
		if (val <= 32) {
			newVal = 32;
		}
		else if (val <= 64) {
			newVal = 64;
		}
		else if (val <= 128) {
			newVal = 128;
		}
		else if (val <= 256) {
			newVal = 256;
		}
		else if (val <= 512) {
			newVal = 512;
		}
		else {
			newVal = 1024;
		}
		return newVal;
	}
}
