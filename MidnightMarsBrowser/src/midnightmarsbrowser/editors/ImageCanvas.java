package midnightmarsbrowser.editors;


import midnightmarsbrowser.model.ImageEntry;
import midnightmarsbrowser.util.PDSIMG;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

/**
 * A basic image viewer canvas without scrolling
 */
public class ImageCanvas extends Canvas {
	private Image sourceImage; /* original image */
	private Image screenImage; /* screen image */

	private int pdsBrightnessMinValue;
	private int pdsBrightnessMaxValue;
	private int pixel_averaging_width;
	private int pixel_averaging_height;
	
	public ImageCanvas(final Composite parent) {
		this(parent, SWT.NULL);
	}

	/**
	 * Constructor for ScrollableCanvas.
	 * @param parent the parent of this control.
	 * @param style the style of this control.
	 */
	public ImageCanvas(final Composite parent, int style) {
		super( parent, style | SWT.BORDER | SWT.NO_BACKGROUND);
		addControlListener(new ControlAdapter() { /* resize listener. */
			public void controlResized(ControlEvent event) {
				//syncScrollBars();
			}
		});
		addPaintListener(new PaintListener() { /* paint listener. */
			public void paintControl(final PaintEvent event) {
				paint(event.gc);
			}
		});
		//initScrollBars();		
	}

	/**
	 * Dispose the garbage here
	 */
	public void dispose() {
		super.dispose();	// TODO ?
		if (sourceImage != null && !sourceImage.isDisposed()) {
			sourceImage.dispose();
		}
		if (screenImage != null && !screenImage.isDisposed()) {
			screenImage.dispose();
		}
	}

	/* Paint function */
	private void paint(GC gc) {
		Rectangle clientRect = getClientArea(); /* Canvas' painting area */
		if (sourceImage != null) {
//			int canvasWidth = clientRect.width;
//			int canvasHeight = clientRect.height;
			Rectangle imageBound = sourceImage.getBounds();
			
			double scaleFactor;
			int scaledWidth = imageBound.width * pixel_averaging_width;
			int scaledHeight = imageBound.height * pixel_averaging_height;
			if (scaledWidth > clientRect.width) {
				scaleFactor = (double) clientRect.width / scaledWidth;
				scaledWidth = (int) (scaleFactor * scaledWidth);
				scaledHeight = (int) (scaleFactor * scaledHeight);
			}
			if (scaledHeight > clientRect.height) {
				scaledWidth = imageBound.width * pixel_averaging_width;
				scaledHeight = imageBound.height * pixel_averaging_height;				
				scaleFactor = (double) clientRect.height / scaledHeight;
				scaledWidth = (int) (scaleFactor * scaledWidth);
				scaledHeight = (int) (scaleFactor * scaledHeight);
			}
			int offsetX = (clientRect.width - scaledWidth) / 2;
			int offsetY = (clientRect.height - scaledHeight) / 2;
			
			if (screenImage != null)
				screenImage.dispose();
			screenImage =
				new Image(getDisplay(), clientRect.width, clientRect.height);
			GC newGC = new GC(screenImage);
			newGC.setClipping(clientRect);
			newGC.drawImage(
				sourceImage,
				0,
				0,
				imageBound.width,
				imageBound.height,
				offsetX,
				offsetY,
				scaledWidth,
				scaledHeight);
			newGC.dispose();

			gc.drawImage(screenImage, 0, 0);
		} else {
			gc.setClipping(clientRect);
			gc.fillRectangle(clientRect);
//			initScrollBars();
		}
	}

	/**
	 * Source image getter.
	 * @return sourceImage.
	 */
	public Image getSourceImage() {
		return sourceImage;
	}

	/**
	 * Reload image from a file
	 * @param filename image file
	 * @return swt image created from image file
	 */
	public Image loadImage(ImageEntry imageEntry) {
		try {
			if (sourceImage != null && !sourceImage.isDisposed()) {
				sourceImage.dispose();
				sourceImage = null;
			}
			String file = imageEntry.getFile();
			if (imageEntry.getImageMetadataEntry() != null) {
				pixel_averaging_height = imageEntry.getImageMetadataEntry().pixel_averaging_height;
				pixel_averaging_width = imageEntry.getImageMetadataEntry().pixel_averaging_width;
			}
			else {
				pixel_averaging_height = 1;
				pixel_averaging_width = 1;				
			}
			if (file.toUpperCase().endsWith(".IMG")) {
				PDSIMG img = PDSIMG.readIMGFile(file, pdsBrightnessMinValue, pdsBrightnessMaxValue);
				byte[] imageBytes = img.getImageByteArray();
				PaletteData palette = new PaletteData(0xFF , 0xFF , 0xFF);
				ImageData imageData = new ImageData(img.getLineSamples(),img.getLines(),8,palette,1,imageBytes);
				sourceImage = new Image(getDisplay(), imageData);				
			}
			else {		
				sourceImage = new Image(getDisplay(), file);
			}
			redraw();
//			showOriginal();
			//		fitCanvas();
			//		fitCanvas1024();
		}
		catch (Exception e) {
			// TODO
			e.printStackTrace();
		}
		return sourceImage;
	}

	/**
	 * Reset the image data and update the image
	 * @param data image data to be set
	 */
	public void setImageData(ImageData data) {
		if (sourceImage != null)
			sourceImage.dispose();
		if (data != null)
			sourceImage = new Image(getDisplay(), data);
		pixel_averaging_height = 1;
		pixel_averaging_width = 1;		
		redraw();
	}
	
	public void setPDSBrightnessMinValue(int pdsBrightnessMinValue) {
		this.pdsBrightnessMinValue = pdsBrightnessMinValue;
	}

	public void setPDSBrightnessMaxValue(int pdsBrightnessMaxValue) {
		this.pdsBrightnessMaxValue = pdsBrightnessMaxValue;
	}
}
