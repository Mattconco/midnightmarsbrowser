package midnightmarsbrowser.editors;

import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;

import midnightmarsbrowser.editors.Object3D.Material;

import org.eclipse.swt.examples.openglview.ImageDataUtil;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;

public class Object3DImageLoader implements Runnable {

	private PanoramaCanvas canvas;
	
	private Object3D object3D;
	
	boolean shutdown = false;

	boolean mipmap = true;
	
	Object3DImageLoader(PanoramaCanvas panoramaCanvas, Object3D object3D) {
		this.canvas = panoramaCanvas;
		this.object3D = object3D;
	}
	
	public void run() {
		int textureCounter = 0;        
        Iterator iter = object3D.materials.values().iterator();
        while (iter.hasNext() && (textureCounter < object3D.maxTextures)) {
        	if (shutdown) return;
        	
        	Material mtl = (Material) iter.next();
        	if (mtl.map_Kd == null)
        		continue;
        	
        	try {
	    		URL mapURL = new URL(object3D.url, mtl.map_Kd);
	            InputStream mapIS = mapURL.openStream(); 
	            ImageData imageData = new ImageData(mapIS);
	            mapIS.close();
	
		        int newWidth = fixDimension(imageData.width);
		        int newHeight = fixDimension(imageData.height);
	
				Thread.yield();
	        	if (shutdown) return;
		        
				if ((imageData.width != newWidth) || (imageData.height != newHeight)) {
					imageData = imageData.scaledTo(newWidth, newHeight);
				}
	
				Thread.yield();
	        	if (shutdown) return;
				
	    		PaletteData newPalette = new PaletteData (0xff0000, 0xff00, 0xff);
	    		ImageData convImageData = new ImageData (imageData.width, imageData.height, 24, newPalette);
	    		ImageDataUtil.blit (
	    			1,
	    			imageData.data,
	    			imageData.depth,
	    			imageData.bytesPerLine,
	    			(imageData.depth != 16) ? ImageDataUtil.MSB_FIRST : ImageDataUtil.LSB_FIRST,
	    			0,
	    			0,
	    			imageData.width,
	    			imageData.height,
	    			imageData.palette.redMask,
	    			imageData.palette.greenMask,
	    			imageData.palette.blueMask,
	    			255,
	    			null,
	    			0,
	    			0,
	    			0,
	    			convImageData.data,
	    			convImageData.depth,
	    			convImageData.bytesPerLine,
	    			(convImageData.depth != 16) ? ImageDataUtil.MSB_FIRST : ImageDataUtil.LSB_FIRST,
	    			0,
	    			0,
	    			convImageData.width,
	    			convImageData.height,
	    			convImageData.palette.redMask,
	    			convImageData.palette.greenMask,
	    			convImageData.palette.blueMask,
	    			false,
	    			false);
	    		
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
		        	if (shutdown) return;
		        }
		        
		        mtl.loadImageWidth = loadImageWidth;
		        mtl.loadImageHeight = loadImageHeight;
		        mtl.loadImageBuffer = loadImageBuffer;
		        canvas.getDisplay().asyncExec(new PanoramaCanvasRefresh(canvas));
        	}
        	catch (Throwable e) {
        		e.printStackTrace();
        	}
        }
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
