package midnightmarsbrowser.editors;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import javax.imageio.ImageIO;
import javax.vecmath.Matrix4d;

import midnightmarsbrowser.application.Application;
import midnightmarsbrowser.application.MMBWorkspace;
import midnightmarsbrowser.dialogs.UnknownExceptionDialog;
import midnightmarsbrowser.metadata.ExcelCSVReader;
import midnightmarsbrowser.metadata.ImageMetadataEntry;
import midnightmarsbrowser.metadata.LocationMetadataEntry;
import midnightmarsbrowser.metadata.SiteMetadataEntry;
import midnightmarsbrowser.model.ImageEntry;
import midnightmarsbrowser.model.LocationCounter;
import midnightmarsbrowser.model.MerUtils;
import midnightmarsbrowser.model.PanMovieEndpoint;
import midnightmarsbrowser.model.PanPosition;
import midnightmarsbrowser.model.TimeInterval;
import midnightmarsbrowser.model.TimeIntervalList;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.examples.openglview.ImageDataUtil;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.opengl.GLData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.glu.GLU;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;


public class PanoramaCanvas extends GLCanvas implements KeyListener, MouseListener, MouseMoveListener {
	private static final int MOVE_LOCATION_VECTOR_MIN_TIME_MILLIS = 150; //400;
	private static final int MOVE_LOCATION_VECTOR_MAX_TIME_MILLIS = 700;
	private static final double MOVE_LOCATION_VECTOR_RATE_PER_MILLI = 0.02; //0.01;
	private static final double MOVE_LOCATION_VECTOR_MIN_DISTANCE = 1.0;
	ViewerEditor editor;
	private boolean  supressError = false;
	
	int rot = 0;
    private float viewAzimuth = 0.0f;
    private float viewElevation = 0.0f;
    private float viewVFOV = 100.0f;
    // Note tinkering with zNear has impact on maximum FOV
    private final float zNear = 2.0f;
    private final float zFar = 100f;
    private final float zNearModerate = 10.f;
    private final float zNearModerateFOV = 100.0f;
	private float zNearHotpots = 0.25f; //0.1f;
	//private float zNearRover = 0.05f;
	private float zFarHotspots = 1000.0f;
    private final float maxFOV = 170.0f;
    private final int trackingSiteSpread = 6;
	final double maxTrackingRange = 150.0;
	final double maxTrackingRangeSq = maxTrackingRange * maxTrackingRange;
    
    IntBuffer textures;
    boolean hasQuaternion = false;
    private DoubleBuffer quaternionMatrix;
    private DoubleBuffer roverModelQuaternionMatrix;
    FloatBuffer projectionMatrix;
    float[][] projectionArray;
    FloatBuffer modelMatrix;
    float[][] modelArray;
    IntBuffer	viewportMatrix;
    int[]	viewportArray;
    
    private MMBWorkspace workspace;
    private String selectedSpacecraftId;
    private LocationCounter currentLocationCounter;
    private PanImageEntry[] panImageList;	// in image list order
    PanImageEntry[] sortedPanImageList;	// sorted by distance
    private PanImageEntry selectedPanImageEntry;
    private int textureLoadIndex = 0;
    private final int maxNumTextures = 500;
    boolean mipmap = true;
    boolean	focusMode = false;
    PanImageLoader imageLoader;
    private int maxResolution = 1024;
    private long fullByteCount;
    private long byteCount;
    private long fullPixelCount;
    private long pixelCount;
    
    private boolean groundRelative = true;
    private boolean roverTrackingOn = true;
//    private boolean roverModelOn = true;

    // all location counters, visible or not, for rendering rover trail 
    private LocationCounter[] allLocations;
	private TimeInterval selectedTimeInterval;
	//private LocationCounter roverModelLocation = null;
	private LocationMetadataEntry roverModelLocationMetadata = null;
	
	// about 1.2 meters from bottom of WEB to top of camera mast
	// (about 0.3 meters from bottom of WEB to ground)
	final float roverModelEyeX = 0.35f; //0.3f;
	final float roverModelEyeY = -1.25f; //-1.15f;
	final float roverModelEyeZ = 0.1f;
	
	Object3D mer_model;
	
	private PanPosition[] movieFrames = null;	
	private boolean playMovieMode = false;
	private boolean exportMovieFramesMode = false;
	private int movieFrameIndex;

	boolean moveToMode = false;
	long	moveToLastTime = 0;
	float moveToAz = 0.0f;
	float moveToEl = 0.0f;
	float moveToAzRate = 0.0f;
	float moveToElRate = 0.0f;
	
	boolean mousePressed = false;
	boolean mouseDragged = false;
	long mouseStartTime = 0;
	long mouseStopTime = 0;
	int mouseStartX = 0;
	int mouseStartY = 0;
	int mouseX = 0;
	int mouseY = 0;
	int mouseButton = 0;
	int mouseStateMask = 0;
	private boolean boundAll;
	
	private int pdsBrightnessMinValue;
	private int pdsBrightnessMaxValue;
	private double dstLocationVectorA;
	private double dstLocationVectorB;
	private double dstLocationVectorC;
	private boolean dstLocationVectorExists;
	private double currentLocationVectorA;
	private double currentLocationVectorB;
	private double currentLocationVectorC;
	private boolean currentLocationVectorExists;
	private double moveLocationVectorA;
	private double moveLocationVectorB;
	private double moveLocationVectorC;
	private Object3DImageLoader roverImageLoader;
	private boolean mer_model_loaded = false;
	private ArrayList roverTrackingList;
	
	class RoverTrackingListEntry {
		boolean segmentStart = false;
		LocationMetadataEntry locationMetadataEntry;
		SiteMetadataEntry siteMetadataEntry;
		TimeInterval locationListEntry;
		float screenX;
		float screenY;
	}
	
	
	public PanoramaCanvas(Composite parent, int style, GLData data, MMBWorkspace workspace, ViewerEditor editor) {
		super(parent, style, data);
		this.workspace = workspace;
		this.editor = editor;
		try {
			this.setCurrent();
			GLContext.useContext(this);

	        GL11.glEnable(GL11.GL_DEPTH_TEST);							// Enables Depth Testing
	        GL11.glEnable(GL11.GL_TEXTURE_2D);
	        GL11.glShadeModel(GL11.GL_FLAT);              // Enable Smooth Shading
	        GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);    // Black Background
	        GL11.glClearDepth(1.0);                      // Depth Buffer Setup
	        //GL11.glDepthFunc(GL11.GL_LEQUAL);								// The Type Of Depth Testing To Do
	        GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);	// Really Nice Perspective Calculations
	        
	        // read rover model
	        // According to http://hobbiton.thisside.net/rovermanual/ 
	        // the length of the WEB is about 34" == 0.8636 meters
	        // in the .obj file the length of the WEB is 33.849996
	        // whoah - that can't be a coincidence, can it? :)
	        // So I guess the coversion factor is 0.0254 (inches to meters)
	        // The rover navigation frame seems to be centered at the WEB center-bottom 
	        // (http://anserver1.eprsl.wustl.edu/anteam/mera/sis/Flight_School_coordinate_systems.ppt)
	        // so we should center the model there, so the quaternion rotation is applied properly.
	        // update: scale changed - new 1a model WEB length is 0.881854, so I guess we switched to meter scale	        
//	        URL url = Platform.getBundle("MidnightMarsBrowser.DJEllisonRoverModel").getResource("3dmodel/mer.obj");
//	        mer_model = new Object3D(url, false, 0.0f, -11.812945f, 0.0f, 0.0254f);
	        URL url = Platform.getBundle("MidnightMarsBrowser.DJEllisonRoverModel").getResource("3dmodel/mer_1a.obj");
	        mer_model = new Object3D(url, false, 0.0f, -0.3f, 0.0f, 1.0f);	        
	        roverImageLoader = new Object3DImageLoader(this, mer_model);
	        Thread roverImageLoaderThread= new Thread(roverImageLoader);
	        roverImageLoaderThread.setPriority(Thread.MIN_PRIORITY);
	        roverImageLoaderThread.start();

	        // Image textures
	        textures = BufferUtils.createIntBuffer(maxNumTextures);
	        GL11.glGenTextures(textures);
	        
	        quaternionMatrix = BufferUtils.createDoubleBuffer(16);
	        roverModelQuaternionMatrix = BufferUtils.createDoubleBuffer(16);
	        projectionMatrix = BufferUtils.createFloatBuffer(16);
	        projectionArray = new float[4][4];
	        modelMatrix = BufferUtils.createFloatBuffer(16);
	        modelArray = new float[4][4];
	        viewportMatrix = BufferUtils.createIntBuffer(16);
	        viewportArray = new int[4];
	        
		}
		catch (OutOfMemoryError e) {
			UnknownExceptionDialog.openDialog(this.getShell(), "Error creating PanoramaCanvas", e);
		}
		catch(Throwable e) {
			UnknownExceptionDialog.openDialog(this.getShell(), "Error creating PanoramaCanvas", e);
//			Status status = new Status(Status.ERROR, Application.PLUGIN_ID, 1, "message", e);
//			Platform.getLog(Platform.getBundle(Application.PLUGIN_ID)).log(status);
//			MessageDialog.openError(this.getShell(), "Error creating PanoramaCanvas", stringWriter.toString());
		}

        imageLoader = new PanImageLoader(workspace, this);
        Thread imageLoaderThread= new Thread(imageLoader);
        //imageLoaderThread.setPriority(Thread.MIN_PRIORITY);
        imageLoaderThread.start();
		
		addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				try {
					drawView();
				}
				catch (Throwable ex) {
					ex.printStackTrace();
				}
			}
		});
/*
        addControlListener(new ControlAdapter() {
            public void controlResized(ControlEvent e) {
                resizeScene();
            }
        });
        */
        addKeyListener(this);
        addMouseListener(this);
        addMouseMoveListener(this);
        
        addListener(SWT.MouseWheel, new Listener()
		{
			public void handleEvent(Event event)
			{
				int c = event.count;
				viewVFOV = fixFOV(viewVFOV + (-0.5f * c));
				redraw();
				fireDirectionChanged();
			}
		});
        
        addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				imageLoader.shutdown();
			}
        });
        
	}
	
	void setMaxResolution(int maxResolution) {
		this.maxResolution = maxResolution;
	}
	
	public int getMaxResolution() {
		return this.maxResolution;
	}
	
	void setPDSBrightnessMinValue(int brightnessMinValue) {
		this.pdsBrightnessMinValue = brightnessMinValue;
	}

	void setPDSBrightnessMaxValue(int brightnessMaxValue) {
		this.pdsBrightnessMaxValue = brightnessMaxValue;
	}
	
	public int getCurrentResolution() {
		return imageLoader.getMaxResolution();
	}
	
	public long getByteCount() {
		return byteCount;
	}
	
	public long getFullByteCount() {
		return fullByteCount;
	}
	
	public long getFullPixelCount() {
		return fullPixelCount;
	}

	public long getPixelCount() {
		return pixelCount;
	}

	void setGroundRelative(boolean groundRelative) {
		this.groundRelative = groundRelative;
	}
	
	boolean isGroundRelative() {
		return this.groundRelative;
	}
	
	boolean isRoverTrackingOn() {
		return roverTrackingOn;
	}

	void setRoverTrackingOn(boolean roverTrackingOn) {
		this.roverTrackingOn = roverTrackingOn;
		this.redraw();
	}
	
/*	
	boolean isRoverModelOn() {
		return roverTrackingOn;
	}

	void setRoverModelOn(boolean roverModelOn) {
		this.roverModelOn = roverModelOn;
		if (roverModelOn == false) {
			roverModelSiteDrive = null;
		}
		this.redraw();
	}
*/
	
	void fireDirectionChanged() {
        editor.fireDirectionChanged();		
	}

	public void setSelectedTimeInterval(String spacecraftId, TimeInterval timeInterval, boolean findSelectedImage) {		
		imageLoader.suspend();
		String lastSpacecraftId = selectedSpacecraftId;
		TimeInterval lastTimeInterval = selectedTimeInterval;
		selectedSpacecraftId = spacecraftId;
		selectedTimeInterval = timeInterval;
		this.currentLocationCounter = timeInterval.getStartLocation();
		
		// Get array of all location counters for this rover whether they are in the visible location set or not
		allLocations = workspace.getLocationMetadata().getSortedKeys(spacecraftId);
		
		ImageEntry[] siteImageList = timeInterval.getImageList();
		panImageList = new PanImageEntry[siteImageList.length];
		sortedPanImageList = new PanImageEntry[siteImageList.length];
		for (int n=0; n<siteImageList.length; n++) {
			panImageList[n] = new PanImageEntry(siteImageList[n]);
			sortedPanImageList[n] = panImageList[n];
		}
		
		// find max resolution for pan images
		long maxPanBytes = Application.getMaxPanBytes();
		int newMaxRes = maxResolution;
		fullByteCount = countPanBytes(newMaxRes);
		fullPixelCount = countPanPixels(newMaxRes);
		byteCount = fullByteCount;
		pixelCount = fullPixelCount;
		while (byteCount > maxPanBytes) {
			newMaxRes = newMaxRes / 2;
			byteCount = countPanBytes(newMaxRes);
			pixelCount = countPanPixels(newMaxRes);
		}
		imageLoader.setMaxResolution(newMaxRes);
		imageLoader.setPDSBrightnessMinValue(pdsBrightnessMinValue);
		imageLoader.setPDSBrightnessMaxValue(pdsBrightnessMaxValue);
		
		// fill rover quaternion matrix once; is used in other operations after this
		fillRoverQuaternionMatrix();
		
		startView();
		
		this.textureLoadIndex = 0;
		if (findSelectedImage) {
			findSelectedImageEntry();
		}
		
		dstLocationVectorA = selectedTimeInterval.getLocationMetadataEntry().rover_origin_offset_vector_a;
		dstLocationVectorB = selectedTimeInterval.getLocationMetadataEntry().rover_origin_offset_vector_b;
		dstLocationVectorC = selectedTimeInterval.getLocationMetadataEntry().rover_origin_offset_vector_c;
		SiteMetadataEntry panSiteMetadataEntry = workspace.getSiteMetadata().getEntry(selectedSpacecraftId, new Integer(selectedTimeInterval.getLocationMetadataEntry().location.site));
		if (panSiteMetadataEntry != null) {
			dstLocationVectorA += panSiteMetadataEntry.offset_vector_a;
			dstLocationVectorB += panSiteMetadataEntry.offset_vector_b;
			dstLocationVectorC += panSiteMetadataEntry.offset_vector_c;
			dstLocationVectorExists = true;
		}
		else {
			dstLocationVectorExists = false;
		}
		
		if (currentLocationVectorExists && dstLocationVectorExists
				&& !playMovieMode && !exportMovieFramesMode
				&& selectedSpacecraftId.equals(lastSpacecraftId) && groundRelative 
				&& (roverTrackingOn || editor.settings.panShowRoverModel)) {
			double da = dstLocationVectorA - currentLocationVectorA;
			double db = dstLocationVectorB - currentLocationVectorB;
			double dc = dstLocationVectorC - currentLocationVectorC;
			double dist = Math.sqrt(da*da + db*db + dc*dc);
			if (dist > MOVE_LOCATION_VECTOR_MIN_DISTANCE /*|| selectedTimeInterval.getNumEnabledImages() > 2*/) {
				double rate = MOVE_LOCATION_VECTOR_RATE_PER_MILLI;
				double time = dist / rate;
				if (time > MOVE_LOCATION_VECTOR_MAX_TIME_MILLIS) {
					rate = dist / MOVE_LOCATION_VECTOR_MAX_TIME_MILLIS;
				}
				else if (time < MOVE_LOCATION_VECTOR_MIN_TIME_MILLIS) {
					rate = dist / MOVE_LOCATION_VECTOR_MIN_TIME_MILLIS;
				}
				moveLocationVectorA = (double)(da / dist * rate);
				moveLocationVectorB = (double)(db / dist * rate);
				moveLocationVectorC = (double)(dc / dist * rate);					
				moveToLastTime = System.currentTimeMillis();		
				this.redraw();
			}
			else {
				currentLocationVectorA = dstLocationVectorA;
				currentLocationVectorB = dstLocationVectorB;
				currentLocationVectorC = dstLocationVectorC;
				currentLocationVectorExists = dstLocationVectorExists;		
				if (roverModelLocationMetadata != null && roverModelLocationMetadata.location.equals(currentLocationCounter)) {
					editor.settings.panShowRoverModel = false;
					// Can we do this safely here?
					//editor.fireViewSettingsChanged(null, editor.settings);
				}
			}
		}
		else {
			currentLocationVectorA = dstLocationVectorA;
			currentLocationVectorB = dstLocationVectorB;
			currentLocationVectorC = dstLocationVectorC;
			currentLocationVectorExists = dstLocationVectorExists;
		}

		// find rover tracking list for this location
		roverTrackingList = new ArrayList();
		if (selectedTimeInterval.getLocationMetadataEntry() != null && dstLocationVectorExists) {
			int cacheSite = -1;
			boolean outOfRange = true;
			SiteMetadataEntry cacheSiteMetadataEntry = null;
			RoverTrackingListEntry lastListEntry = null;
			double lastda = 0.0;
			double lastdb = 0.0;
			double lastdc = 0.0;
			final double tolerance = 0.001;
			for (int i=0; i<allLocations.length; i++) {
				LocationMetadataEntry locationMetadataEntry = workspace.getLocationMetadata().getEntry(selectedSpacecraftId, allLocations[i]) ;
				int siteDiff = locationMetadataEntry.location.site - currentLocationCounter.site;
				if (siteDiff < -trackingSiteSpread)
					continue;
				if (!locationMetadataEntry.has_site_rover_origin_offset_vector)
					continue;
				if (cacheSite != locationMetadataEntry.location.site) {
					cacheSiteMetadataEntry = workspace.getSiteMetadata().getEntry(selectedSpacecraftId, new Integer(locationMetadataEntry.location.site));
					if (cacheSiteMetadataEntry == null)  {
						continue;
					}
					else {
						cacheSite = locationMetadataEntry.location.site;
					}
				}
	            double da = locationMetadataEntry.rover_origin_offset_vector_a + cacheSiteMetadataEntry.offset_vector_a - dstLocationVectorA;
	            double db = locationMetadataEntry.rover_origin_offset_vector_b + cacheSiteMetadataEntry.offset_vector_b - dstLocationVectorB;
	            double dc = locationMetadataEntry.rover_origin_offset_vector_c + cacheSiteMetadataEntry.offset_vector_c - dstLocationVectorC;
				double distsq = da*da + db*db + dc*dc;
				if (distsq > maxTrackingRangeSq) {
					outOfRange = true;
				}
				else {
					// myLocationListEntry will be null if the location is not in the list of visible (selected) locations
					// TODO this doesn't exactly work right, since a single time interval may not include all the 
					// images for a location
					TimeInterval myLocationListEntry = selectedTimeInterval.getParent().getEntry(locationMetadataEntry.location);
					RoverTrackingListEntry listEntry = new RoverTrackingListEntry();
					listEntry.locationMetadataEntry = locationMetadataEntry;
					listEntry.siteMetadataEntry = cacheSiteMetadataEntry;
					listEntry.locationListEntry = myLocationListEntry;
					if (outOfRange) {
						listEntry.segmentStart = true;
						outOfRange = false;
						roverTrackingList.add(listEntry);
					}
					else if ((lastListEntry != null) && (Math.abs(lastda-da) < tolerance) && (Math.abs(lastdb-db) < tolerance) && (Math.abs(lastdc-dc) < tolerance)) {
						// In cases where two hotspots share the same physical location (like at site resets), 
						// take the one with the most images, so we don't miss some significant pans (Bonneville, Missoula, etc.)
						if (myLocationListEntry != null) {
							if (lastListEntry.locationListEntry == null || myLocationListEntry.getNumEnabledImages() > lastListEntry.locationListEntry.getNumEnabledImages()) {
								roverTrackingList.set(roverTrackingList.size()-1, listEntry);
								lastListEntry = listEntry;
								lastda = da;
								lastdb = db;
								lastdc = dc;
							}
						}
					}
					else {
						roverTrackingList.add(listEntry);
						lastListEntry = listEntry;
						lastda = da;
						lastdb = db;
						lastdc = dc;
					}
				}
				if (siteDiff >= trackingSiteSpread) {
					break;
				}
			}
		}
		
		imageLoader.resume();
	}
		
	/**
	 * 	Find number of bytes in all the enabled images in the pan, plus mipmapped images
	 *  The method of finding the number of pixels in the texture map must 
	 *  match the scaling method in PanImageLoader
	 * @return
	 */
	private long countPanBytes(int maxRes) {
		long numPixels = 0;
		for (int n=0; n<panImageList.length; n++) {
			PanImageEntry panEntry = panImageList[n];
			if (panEntry.imageListEntry.enabled) {
				int width = panEntry.imageListEntry.getImageMetadataEntry().n_line_samples;
				int height = panEntry.imageListEntry.getImageMetadataEntry().n_lines;
				width = PanImageLoader.fixDimension(width);
				height = PanImageLoader.fixDimension(height);
		        while ((width > maxRes) || (height > maxRes)) {
		        	width = width >> 1;
		        	height = height >> 1;
		        }
		        if (mipmap) {
		        	while ((width>4) || (height>4)) {
						numPixels = numPixels + width * height * 4;
			        	width = width >> 1;
			        	height = height >> 1;
		        	}
		        }
		        else {
					numPixels = numPixels + width * height * 4;
		        }
			}
		}
		return numPixels;		
	}
	
	/**
	 * Count the pixels in the panorama enabled images, not including mipmapping.
	 * @param maxRes
	 * @return
	 */
	private long countPanPixels(int maxRes) {
		long numPixels = 0;
		for (int n=0; n<panImageList.length; n++) {
			PanImageEntry panEntry = panImageList[n];
			if (panEntry.imageListEntry.enabled) {
				int width = panEntry.imageListEntry.getImageMetadataEntry().n_line_samples;
				int height = panEntry.imageListEntry.getImageMetadataEntry().n_lines;
				width = PanImageLoader.fixDimension(width);
				height = PanImageLoader.fixDimension(height);
		        while ((width > maxRes) || (height > maxRes)) {
		        	width = width >> 1;
		        	height = height >> 1;
		        }
				numPixels = numPixels + width * height;
			}
		}
		return numPixels;		
	}
	
	/**
	 * Set the selected image; this does not center the view on the selected image, 
	 * call centerOnSelectedImage() for that.
	 * @param entry
	 */
	public void setSelectedImageListEntry(ImageEntry entry) {
		if (entry != null) {
			for (int n=0; n<sortedPanImageList.length; n++) {
				if (sortedPanImageList[n].imageListEntry == entry) {
					selectedPanImageEntry = sortedPanImageList[n];
				}
			}
		}
		else {
			selectedPanImageEntry = null;
		}
	}
	
	/**
	 * Mark the specified imageListEntry as needing reload
	 * @param entry
	 */
	public void reloadImage(ImageEntry entry) {
		if (entry != null) {
			for (int n=0; n<sortedPanImageList.length; n++) {
				if (sortedPanImageList[n].imageListEntry == entry) {
					sortedPanImageList[n].loaded = false;
					sortedPanImageList[n].loadImageBuffer = null;
					break;
				}
			}
		}
	}
	
	public ImageEntry getSelectedImageListEntry() {
		if (selectedPanImageEntry != null) {
			return selectedPanImageEntry.imageListEntry;
		}
		return null;
	}
	
	public void revealSelectedImage() {
		if (selectedPanImageEntry != null) {
			double[] yawPitchRoll = new double[3];
			getImageYawPitchRoll(selectedPanImageEntry.imageListEntry, yawPitchRoll, 0.0, 0.0, 0.0);
			startMoveToAzEl((float) yawPitchRoll[0], (float) yawPitchRoll[1]);
			//viewAzimuth = (float) yawPitchRoll[0];
			//viewElevation = (float) yawPitchRoll[1];
			//this.redraw();
		}
	}
	
	public float getViewAzimuth() {
		return viewAzimuth;
	}

	public void setViewAzimuth(float viewAzimuth) {
		this.viewAzimuth = viewAzimuth;
		findSelectedImageEntry();
		this.redraw();
	}

	public float getViewElevation() {
		return viewElevation;
	}

	public void setViewElevation(float viewElevation) {
		this.viewElevation = viewElevation;
		findSelectedImageEntry();
		this.redraw();
	}

	public float getViewVFOV() {
		return viewVFOV;
	}

	public void setViewVFOV(float viewFOV) {
		this.viewVFOV = viewFOV;
		this.redraw();
	}
	
	public float getViewHFOV() {
        Rectangle rect = this.getClientArea();
        // tan(vfov) = height / distance
        double dist = ((double) rect.height) / Math.tan(Math.toRadians(viewVFOV/2));
        // tan(hfov) = width / distance
        double hfov = Math.toDegrees(Math.atan(((double)rect.width) / dist))*2;
        return (float) hfov;
	}
	
	public boolean isFocusMode() {
		return focusMode;
	}

	public void setFocusMode(boolean focusMode) {
		this.focusMode = focusMode;
		this.redraw();
	}

	/**
	 * Find the new selected image based on where we're pointing, 
	 * and notify the editor accordingly. 
	 * Called when the view moves or when we skip to a new location;
	 * should not be called when we move to a new location due to selecting an image.
	 */
	private void findSelectedImageEntry() {
		PanImageEntry oldSelected = selectedPanImageEntry;
		PanImageEntry panImageEntry = findPanImageEntryClosestToCenter();
		if ((panImageEntry != null) && (panImageEntry != oldSelected)) {
			selectedPanImageEntry = panImageEntry;
			editor.setSelectedImage(selectedPanImageEntry.imageListEntry, false);
		}
	}
	
	public void centerOnClosestImage() {
		PanImageEntry panImageEntry = findPanImageEntryClosestToCenter();
		if (panImageEntry != null) {
			double[] yawPitchRoll = new double[3];
			this.getImageYawPitchRoll(panImageEntry.imageListEntry, yawPitchRoll, 0.0, 0.0, 0.0);
			this.startMoveToAzEl((float)(yawPitchRoll[0]), (float)yawPitchRoll[1]);
		}
	}
	
	void resizeScene() {
		drawView();
	}
	
	void drawView() {
		if (!exportMovieFramesMode) {
			renderView();
		}
	}
	
	void renderView() {
		if (!this.isDisposed()) {
			try {
				if (panImageList == null)
					return;
				
				startView();
				
				synchronized(imageLoader) {
					sortPanImages();
					bindTextures();	
					if (!mer_model_loaded ) {
						mer_model_loaded = mer_model.bindTextures();
					}
				}
		        
				findDrawList();
				
				boolean moved = false;
				if (handleMovementByKey())
					moved = true;
				if (handleMovementByMouse())
					moved = true;
				if (handleMoveTo())
					moved = true;
				
				GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
				
				if (!isMovingToLocation()) {
					// set perspective after move
					startPerspective();
					
					renderImages("n");
					
					GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
					
					renderImages("p");
	
					GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
					
					renderImages("pc");
					
					GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
					
					renderImages("na");
					
					GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
					
					renderImages("pa");
					
					GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
					
					renderImages("ps");
					
					GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
				}
								
				// Don't restart perspective between hotspots and model rendering; wipes out z-buffer.
				startPerspective(zNearHotpots, zFarHotspots);					
				if (roverTrackingOn) {
					// re-set perspective with different z clipping bounds,
					// to work better with rover tracking
					renderHotspots();
				}				
				if (editor.settings.panShowRoverModel) {
					//startPerspective(zNearRover, zFarHotspots);					
					renderRoverModel();
				}
				else {
					roverModelLocationMetadata = null;
				}
				
				GL11.glFlush();
								
		        this.swapBuffers();
		        
		        if (!moveToMode && !isMovingToLocation()) {
		        	// TODO this is problematic
					this.findSelectedImageEntry();
		        }
		        
		        
		        if (playMovieMode || exportMovieFramesMode) {
		        	if ((movieFrames != null) && (movieFrames.length > 0)) {
			        	if (boundAll) {
			        		if (exportMovieFramesMode) {
			        			//takePanoramaScreenshot(true, playMovieIndex);
			        			takePanoramaScreenshotSWT(movieFrameIndex);
			        		}
			        		movieFrameIndex++;
			        		if (movieFrameIndex < movieFrames.length) {
			        			setPanPosition(movieFrames[movieFrameIndex]);
			        		}
			        		else {
			        			playMovieMode = false;
			        			exportMovieFramesMode = false;
			        		}
			        	}
	        			if (playMovieMode) {
				            this.getDisplay().timerExec(100, new PanoramaCanvasRefresh(this));
				            editor.fireDirectionChanged();
	        			}
		        	}
		        	else
		        	{
		        		playMovieMode = false;
		        		exportMovieFramesMode = false;
		        	}
		        }
		        else {
			        if (moved || moveToMode || isMovingToLocation()) {
			        	//this.redraw();
			            //this.getDisplay().asyncExec(new PanoramaCanvasRefresh(this));
			            this.getDisplay().timerExec(5, new PanoramaCanvasRefresh(this));
			            editor.fireDirectionChanged();
			        }
		        }
			}
			catch (OutOfMemoryError e) {
				if (!supressError) {
					supressError = true;
					UnknownExceptionDialog.openDialog(this.getShell(), "Error rendering panorama", e);
					e.printStackTrace(); 
				}
			}
			catch (Throwable e) {
				if (!supressError) {
					supressError = true;
					UnknownExceptionDialog.openDialog(this.getShell(), "Error rendering panorama", e);
					e.printStackTrace(); 
				}
			}
		}
	}

	private void startView() {
		try {
			this.setCurrent();
			GLContext.useContext(this);
			startPerspective();
		} catch (LWJGLException e) {
			if (!supressError) {
				e.printStackTrace();
				supressError = true;
				UnknownExceptionDialog.openDialog(this.getShell(), "Error starting panorama view", e);
			}
		}
	}
	
	private void startPerspective() {
		float zNear = this.zNear;
		float zFar = this.zFar;
		if (viewVFOV < zNearModerateFOV) {
			zNear = zNearModerate;
		}
		startPerspective(zNear, zFar);		
	}
	
	private void startPerspective(float zNear, float zFar) {
        Rectangle rect = this.getClientArea();
        GL11.glViewport(0, 0, rect.width, rect.height);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();	        
        float aspectRatio = (float) rect.width / (float) rect.height;		     
        // TODO is this a slightly dodgy computation? I want FOV to be reckoned on the x axis not y
        //sfloat fovY = viewFOV; //* aspectRatio;
        GLU.gluPerspective(viewVFOV, aspectRatio, zNear, zFar);	       
		GL11.glRotatef(viewElevation, -1.0f, 0.0f, 0.0f);
		GL11.glRotatef(viewAzimuth, 0.0f, 1.0f, 0.0f);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);		
	}
		
//	private float testTwist = 0.0f;
	
	void renderImages(String imageCategory) {
		GL11.glColor3f(1.0f, 1.0f, 1.0f);
//		boolean focusOnCmdSeq = true;
//		String selectedCmdSeq = MerUtils.cmdSeqFromFilename(selectedPanImageEntry.imageListEntry.getFilename());		
		for (int i=0; i<panImageList.length; i++) {
			PanImageEntry panEntry = panImageList[i];
			ImageEntry entry = panEntry.imageListEntry;
			if ((!entry.enabled) || (!panEntry.draw))
				continue;
			if (!entry.getImageCategory().equalsIgnoreCase(imageCategory))
				continue;
			ImageMetadataEntry metadataEntry = entry.getImageMetadataEntry();
			if (metadataEntry == null)
				continue;
			String filename = entry.getFilename();
			char spacecraftIdChar = MerUtils.spacecraftIdCharFromFilename(filename);
			char camera = MerUtils.cameraFromFilename(filename);
			char cameraEye = MerUtils.cameraEyeFromFilename(filename);
			double twist = 0.0;
			double imageFOV = 10.0;
			double radius = 50.0;
			double toeInAz = 0.0;
			double	toeInEl = 0.0;
			int left = (metadataEntry.first_line_sample - 1) /* * metadataEntry.pixel_averaging_width*/;
			int width = metadataEntry.n_line_samples * metadataEntry.pixel_averaging_width;
			int top = (metadataEntry.first_line - 1) /* * metadataEntry.pixel_averaging_height*/;
			int height = metadataEntry.n_lines * metadataEntry.pixel_averaging_height;
			
			// Move downsampled images back
			if (metadataEntry.pixel_averaging_width > 1) {
				radius = 75.0;
			}
/* needs to be activited by a key command			
			else if ((focusOnCmdSeq) 
					&& (!MerUtils.cmdSeqFromFilename(panEntry.imageListEntry.getFilename()).equalsIgnoreCase(selectedCmdSeq))) {
				radius = 75.0;
			}			
			*/
			
			if (panEntry == selectedPanImageEntry) {
				if (focusMode)
					radius = 25.0;
			}
			
			if (camera == 'N') {
				imageFOV = 45.1766; // the "official" FOV
				if (spacecraftIdChar == MerUtils.SPACECRAFT_ID_CHAR_OPPORTUNITY) {
					if (cameraEye == 'R' || cameraEye == 'r') {
						twist = 0.25;
						imageFOV = imageFOV * 1.012;						
					}
					else {
						twist = -0.1;
						imageFOV = imageFOV * 1.006;						
					}
				}
				else if (spacecraftIdChar == MerUtils.SPACECRAFT_ID_CHAR_SPIRIT) {
					//if (params.viewMode != PanParams.VIEWMODE_RIGHT_RAW) {
						twist = -0.7;
					//}
					imageFOV = imageFOV * 1.012; //1.015;
				}
			}
			else if (camera == 'P') {
				imageFOV = 15.8412; // the official value
				/*if (params.viewMode != PanParams.VIEWMODE_RIGHT_RAW) {
					// compensate for toe-in left
					azimuthDeg += 1.0;
					elevationDeg += 0.3;
				}*/					
				// TODO reverse toe in for right camera
				//toeInComp = 0.6;
				if (spacecraftIdChar == MerUtils.SPACECRAFT_ID_CHAR_OPPORTUNITY) {
//						twistDeg = 0.3; // was 0.4;
					twist = 0.3;
					imageFOV = imageFOV * 1.015;
					toeInAz = 1.1;		
					toeInEl = -0.35;
				}
				else if (spacecraftIdChar == MerUtils.SPACECRAFT_ID_CHAR_SPIRIT) {
					twist = +0.0;
					imageFOV = imageFOV * 1.015;
					//fovDeg = fovDeg * 1.015;
					toeInAz = 0.0;
					toeInEl = 0.0;
				}
			}
			
			double tranWidth = Math.sin(imageFOV * Math.PI / 360) * radius;
			float floatDistance = (float) (Math.cos(imageFOV * Math.PI / 360) * radius);
			float floatLeft = (float)(tranWidth * (left - 512) / 512);
			float floatTop = (float)(tranWidth * (512-(top+height)) / 512);
			float floatRight = (float)(tranWidth * (left+width-512) / 512);
			float floatBottom = (float)(tranWidth * (512-top) / 512);
			
			setImageRotation((float)(metadataEntry.inst_az_rover + toeInAz), (float)(metadataEntry.inst_el_rover + toeInEl), (float)twist);
			
			if (panEntry.textureNumber >= 0) {
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, panEntry.textureNumber);				
				GL11.glBegin(GL11.GL_QUADS);
				GL11.glTexCoord2f(0.0f, 0.0f);
				GL11.glVertex3f(floatLeft, floatTop, -floatDistance);
				GL11.glTexCoord2f(1.0f, 0.0f);
				GL11.glVertex3f(floatRight, floatTop, -floatDistance);
				GL11.glTexCoord2f(1.0f, 1.0f);
				GL11.glVertex3f(floatRight, floatBottom, -floatDistance);
				GL11.glTexCoord2f(0.0f, 1.0f);
				GL11.glVertex3f(floatLeft, floatBottom, -floatDistance);				
		        GL11.glEnd();
		        
		        if ((panEntry == selectedPanImageEntry) && focusMode) {
					GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);		// no texture
					GL11.glColor3f(1.0f, 1.0f, 1.0f);
					GL11.glBegin(GL11.GL_LINES);
					GL11.glVertex3f(floatLeft, floatTop, -floatDistance);
					GL11.glVertex3f(floatRight, floatTop, -floatDistance);
					GL11.glVertex3f(floatRight, floatTop, -floatDistance);
					GL11.glVertex3f(floatRight, floatBottom, -floatDistance);
					GL11.glVertex3f(floatRight, floatBottom, -floatDistance);
					GL11.glVertex3f(floatLeft, floatBottom, -floatDistance);				
					GL11.glVertex3f(floatLeft, floatBottom, -floatDistance);				
					GL11.glVertex3f(floatLeft, floatTop, -floatDistance);
			        GL11.glEnd();
		        }
			}
			else {
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);		// no texture
				GL11.glColor3f(1.0f, 1.0f, 1.0f);
				GL11.glBegin(GL11.GL_LINES);
				GL11.glVertex3f(floatLeft, floatTop, -floatDistance);
				GL11.glVertex3f(floatRight, floatTop, -floatDistance);
				GL11.glVertex3f(floatRight, floatTop, -floatDistance);
				GL11.glVertex3f(floatRight, floatBottom, -floatDistance);
				GL11.glVertex3f(floatRight, floatBottom, -floatDistance);
				GL11.glVertex3f(floatLeft, floatBottom, -floatDistance);				
				GL11.glVertex3f(floatLeft, floatBottom, -floatDistance);				
				GL11.glVertex3f(floatLeft, floatTop, -floatDistance);
		        GL11.glEnd();
			}
		}		
	}
	
    void renderHotspots() {
//      LocationListEntry[] locations = locationList.getEntries();
        boolean afterCurrent = false;
        boolean hasLastPos = false;
        float lastX = 0.0f;
        float lastY = 0.0f;
        float lastZ = 0.0f;
        
        final float halfWidth = 0.1f / 2;
        final float halfLength = 0.1f / 2;
        final float halfHeight = 0.1f / 2;
        final float eyeHeight = 0.91712f;//1.1f;
                
        // Find pan position offset vector
        if (selectedTimeInterval.getLocationMetadataEntry() == null) {
                return;
        }
        
        if (!dstLocationVectorExists)
                return;
        
        GL11.glLoadIdentity();
        // set up for tracking hotspot screen x,y
		float projectedXYZ[] = new float[3];        		
		projectionMatrix.clear();
		GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projectionMatrix);
		getMatrixAsArray(projectionMatrix, projectionArray);
		viewportMatrix.clear();
		GL11.glGetInteger(GL11.GL_VIEWPORT, viewportMatrix);
		viewportArray[0] = viewportMatrix.get(0);
		viewportArray[1] = viewportMatrix.get(1);
		viewportArray[2] = viewportMatrix.get(2);
		viewportArray[3] = viewportMatrix.get(3);
        //System.out.println(""+viewportArray[0] + " "+ viewportArray[1]+" "+viewportArray[2]+" "+viewportArray[3]   );        
		modelMatrix.clear();
		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelMatrix);
		getMatrixAsArray(modelMatrix, modelArray);
        
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);              // no texture
        
        for (int i=0; i<roverTrackingList.size()-1; i++) {
        	RoverTrackingListEntry listEntry = (RoverTrackingListEntry) roverTrackingList.get(i);
			listEntry.screenX = 0.0f;
			listEntry.screenY = 0.0f;
        	
            double da = listEntry.locationMetadataEntry.rover_origin_offset_vector_a + listEntry.siteMetadataEntry.offset_vector_a - currentLocationVectorA;
            double db = listEntry.locationMetadataEntry.rover_origin_offset_vector_b + listEntry.siteMetadataEntry.offset_vector_b - currentLocationVectorB;
            double dc = listEntry.locationMetadataEntry.rover_origin_offset_vector_c + listEntry.siteMetadataEntry.offset_vector_c - currentLocationVectorC;
                
            float x = (float) db;
            float y = (-(float)dc) - eyeHeight;
            float z = - (float) da;

            // calculate brightness of point based on number of images visible for location
            float brightness = 0.0f;
                                
            if (listEntry.locationListEntry != null && listEntry.locationListEntry.getNumEnabledImages() > 2) {
            	// track screen x, y of hotspot
    			GLU.gluProject(x, y, z, modelArray, projectionArray, viewportArray, projectedXYZ);
    			if (projectedXYZ[2] < 1.0f) {
	    			listEntry.screenX = projectedXYZ[0];
	    			// transform y from opengl screen coords to regular screen coords
	    			listEntry.screenY = viewportArray[3] - projectedXYZ[1];
    			}
            	
            	// render hotspot
                GL11.glLoadIdentity();
                GL11.glTranslatef(x, y, z);
                //System.out.println(""+listEntry.screenX+" "+listEntry.screenY);
                
                brightness = 0.5f + 0.1f * listEntry.locationListEntry.getNumEnabledImages();
                if (brightness > 1.0f) {
                        brightness = 1.0f;
                }
                                        
                GL11.glBegin(GL11.GL_QUADS);            // D draw A Quad
                
                if (listEntry.locationListEntry.getStartLocation().equals(selectedTimeInterval.getStartLocation())) {
                        GL11.glColor3f(brightness, 0.0f, brightness);                           
                }
                else if (afterCurrent) {
                        GL11.glColor3f(0.0f, 0.0f, brightness);
                }
                else {
                        GL11.glColor3f(brightness, 0.0f, 0.0f);
                }
                        
                GL11.glVertex3f(halfWidth, halfHeight, -halfLength);                    // Top Right Of The Quad (Top)
                GL11.glVertex3f(-halfWidth, halfHeight, -halfLength);                   // Top Left Of The Quad (Top)
                GL11.glVertex3f(-halfWidth, halfHeight, halfLength);                    // Bottom Left Of The Quad (Top)
                GL11.glVertex3f(halfWidth, halfHeight, halfLength);                     // Bottom Right Of The Quad (Top)

                GL11.glVertex3f(halfWidth, -halfHeight, halfLength);                    // Top Right Of The Quad (Bottom)
                GL11.glVertex3f(-halfWidth, -halfHeight, halfLength);                   // Top Left Of The Quad (Bottom)
                GL11.glVertex3f(-halfWidth, -halfHeight, -halfLength);                  // Bottom Left Of The Quad (Bottom)
                GL11.glVertex3f(halfWidth, -halfHeight, -halfLength);                   // Bottom Right Of The Quad (Bottom)

                GL11.glVertex3f(halfWidth, halfHeight, halfLength);                     // Top Right Of The Quad (Front)
                GL11.glVertex3f(-halfWidth, halfHeight, halfLength);                    // Top Left Of The Quad (Front)
                GL11.glVertex3f(-halfWidth, -halfHeight, halfLength);                   // Bottom Left Of The Quad (Front)
                GL11.glVertex3f(halfWidth, -halfHeight, halfLength);                    // Bottom Right Of The Quad (Front)

                GL11.glVertex3f(halfWidth, -halfHeight, -halfLength);                   // Bottom Left Of The Quad (Back)
                GL11.glVertex3f(-halfWidth, -halfHeight, -halfLength);                  // Bottom Right Of The Quad (Back)
                GL11.glVertex3f(-halfWidth, halfHeight, -halfLength);                   // Top Right Of The Quad (Back)
                GL11.glVertex3f(halfWidth, halfHeight, -halfLength);                    // Top Left Of The Quad (Back)

                GL11.glVertex3f(-halfWidth, halfHeight, halfLength);                    // Top Right Of The Quad (Left)
                GL11.glVertex3f(-halfWidth, halfHeight, -halfLength);                   // Top Left Of The Quad (Left)
                GL11.glVertex3f(-halfWidth, -halfHeight, -halfLength);                  // Bottom Left Of The Quad (Left)
                GL11.glVertex3f(-halfWidth, -halfHeight, halfLength);                   // Bottom Right Of The Quad (Left)

                GL11.glVertex3f(halfWidth, halfHeight, -halfLength);                    // Top Right Of The Quad (Right)
                GL11.glVertex3f(halfWidth, halfHeight, halfLength);                     // Top Left Of The Quad (Right)
                GL11.glVertex3f(halfWidth, -halfHeight, halfLength);                    // Bottom Left Of The Quad (Right)
                GL11.glVertex3f(halfWidth, -halfHeight, -halfLength);                   // Bottom Right Of The Quad (Right)
                        
                GL11.glEnd();                           // Done Drawing The Quads
	        }
                
            if (hasLastPos && !listEntry.segmentStart) {
                brightness = 0.5f;
                if (afterCurrent) {
                        GL11.glColor3f(0.0f, 0.0f, brightness);
                }
                else {
                        GL11.glColor3f(brightness, 0.0f, 0.0f);
                }                               
                GL11.glLoadIdentity();
                GL11.glBegin(GL11.GL_LINES);    
//                      GL11.glColor3f(1.0f, 0.0f, 0.0f);                               
                GL11.glVertex3f(lastX, lastY, lastZ);
                GL11.glVertex3f(x, y, z);
                GL11.glEnd();
            }
                
            lastX = x;
            lastY = y;
            lastZ = z;
            hasLastPos = true;
            if (listEntry.locationListEntry != null && listEntry.locationListEntry.getStartLocation().equals(this.selectedTimeInterval.getStartLocation())) {
                afterCurrent = true;
            }
        }
    }
	
	void renderRoverModel() {
		if (roverModelLocationMetadata == null) {
			roverModelLocationMetadata = workspace.getLocationMetadata().getEntry(selectedSpacecraftId, currentLocationCounter);
		}
				
		if (roverModelLocationMetadata == null)
			return;
		if (!roverModelLocationMetadata.has_site_rover_origin_offset_vector)
			return;
		
		// Find pan position vector
		if (selectedTimeInterval.getLocationMetadataEntry() == null)
			return;
		if (!dstLocationVectorExists) {
			return;
		}
		
		SiteMetadataEntry roverSiteMetadataEntry = workspace.getSiteMetadata().getEntry(selectedSpacecraftId, new Integer(roverModelLocationMetadata.location.site));
		
		double da = roverModelLocationMetadata.rover_origin_offset_vector_a;
		double db = roverModelLocationMetadata.rover_origin_offset_vector_b;
		double dc = roverModelLocationMetadata.rover_origin_offset_vector_c;
		if (roverSiteMetadataEntry != null)  {
			da += roverSiteMetadataEntry.offset_vector_a;
			db += roverSiteMetadataEntry.offset_vector_b;
			dc += roverSiteMetadataEntry.offset_vector_c;
		}
		else {
			System.out.println("roverSiteMetadataEntry "+ roverModelLocationMetadata.location.site +" not found");
			return;
		}
		da -= currentLocationVectorA;
		db -= currentLocationVectorB;
		dc -= currentLocationVectorC;
		
		float x = (float) db;
		float y = (-(float)dc);	// - eyeHeight;
		float z = -( (float) da);
		
    	double q_x = roverModelLocationMetadata.rover_origin_rotation_quaternian_a;
        double q_y = roverModelLocationMetadata.rover_origin_rotation_quaternian_b;
        double q_z = roverModelLocationMetadata.rover_origin_rotation_quaternian_c;
        double q_w = roverModelLocationMetadata.rover_origin_rotation_quaternian_d;
    	fillMatrixFromQuaternion(roverModelQuaternionMatrix, q_z, -q_w, -q_y, q_x);
    	
		FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(4);
		
		GL11.glEnable(GL11.GL_LIGHTING);	
		GL11.glEnable(GL11.GL_LIGHT1);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_NORMALIZE);
		    	
		GL11.glLoadIdentity();
		
		floatBuffer.rewind();
		floatBuffer.put(-2.0f);
		floatBuffer.put(20.0f);
		floatBuffer.put(0.0f);
		floatBuffer.put(1.0f);
//		floatBuffer.put(-10.1f);
//		floatBuffer.put(6.0f);
//		floatBuffer.put(0.0f);
//		floatBuffer.put(0.0f);
		floatBuffer.flip();
		GL11.glLight(GL11.GL_LIGHT1, GL11.GL_POSITION, floatBuffer);

		floatBuffer.rewind();
		floatBuffer.put(0.3f);
		floatBuffer.put(0.3f);
		floatBuffer.put(0.3f);
		floatBuffer.put(1.0f);
//		floatBuffer.put(0.0f);
//		floatBuffer.put(0.0f);
//		floatBuffer.put(0.0f);
//		floatBuffer.put(1.0f);
		floatBuffer.flip();
		GL11.glLight(GL11.GL_LIGHT1, GL11.GL_AMBIENT, floatBuffer);
		
		floatBuffer.rewind();
		floatBuffer.put(0.2f);
		floatBuffer.put(0.2f);
		floatBuffer.put(0.2f);
		floatBuffer.put(1.0f);
//		floatBuffer.put(0.1f);
//		floatBuffer.put(0.1f);
//		floatBuffer.put(0.1f);
//		floatBuffer.put(1.0f);
		floatBuffer.flip();
		GL11.glLight(GL11.GL_LIGHT1, GL11.GL_DIFFUSE, floatBuffer);
		
		floatBuffer.rewind();
		floatBuffer.put(0.4f);
		floatBuffer.put(0.4f);
		floatBuffer.put(0.4f);
		floatBuffer.put(1.0f);
		floatBuffer.flip();
		GL11.glLight(GL11.GL_LIGHT1, GL11.GL_SPECULAR, floatBuffer);
	
		GL11.glColor3f(1.0f, 1.0f, 1.0f);
		
		GL11.glLoadIdentity();
		GL11.glTranslatef(x, y, z);
		if (groundRelative) {
        	GL11.glMultMatrix(roverModelQuaternionMatrix);
		}
		GL11.glRotatef(-90.0f, 0.0f, 1.0f, 0.0f);
					
		GL11.glTranslatef(roverModelEyeX, roverModelEyeY, roverModelEyeZ);
		
		mer_model.opengldraw();
		
		GL11.glDisable(GL11.GL_LIGHTING);
	}
	
	private RoverTrackingListEntry findHotspotAt(float screenx, float screeny) {
		RoverTrackingListEntry foundEntry = null;
		float foundDistsq = -1.0f;
        for (int i=0; i<roverTrackingList.size()-1; i++) {
        	RoverTrackingListEntry listEntry = (RoverTrackingListEntry) roverTrackingList.get(i);
        	if (listEntry.screenX != 0.0f || listEntry.screenY != 0.0f) {
        		float dx = screenx - listEntry.screenX;
        		float dy = screeny - listEntry.screenY;
        		float distsq = dx*dx + dy*dy;
        		if (distsq < 400.0f && (foundDistsq < 0.0f || distsq < foundDistsq)) {
        			foundEntry = listEntry;
        			foundDistsq = distsq;
        		}
        	}
        }
        return foundEntry;
	}
	
	private void fillRoverQuaternionMatrix() {
		hasQuaternion = false;
		LocationMetadataEntry siteMetadataEntry = workspace.getLocationMetadata().getEntry(selectedSpacecraftId, currentLocationCounter);
        if (siteMetadataEntry != null) {
        	double q_x = siteMetadataEntry.rover_origin_rotation_quaternian_a;
	        double q_y = siteMetadataEntry.rover_origin_rotation_quaternian_b;
	        double q_z = siteMetadataEntry.rover_origin_rotation_quaternian_c;
	        double q_w = siteMetadataEntry.rover_origin_rotation_quaternian_d;
	        if ((q_x != 0.0f) || (q_y != 0.0f) || (q_z != 0.0f) || (q_w != 0.0f)) {
	        	fillMatrixFromQuaternion(quaternionMatrix, q_z, -q_w, -q_y, q_x);
	        	hasQuaternion = true;
	        }
        }
	}
	
	private void setImageRotation(float azimuth, float elevation, float twist) {
		GL11.glLoadIdentity();
		if (groundRelative && hasQuaternion) {
        	GL11.glMultMatrix(quaternionMatrix);
		}
		GL11.glRotatef(-azimuth, 0.0f, 1.0f, 0.0f);
		GL11.glRotatef(-elevation, 1.0f, 0.0f, 0.0f);
		GL11.glRotatef(-twist, 0.0f, 0.0f, -1.0f);
	}
	
	/**
	 * Get an image's yaw-pitch-roll in the viewer frame of reference.
	 * @param imageListEntry
	 * @param yawPitchRoll
	 */
	void getImageYawPitchRoll(ImageEntry imageListEntry, double yawPitchRoll[], 
			double toeInAz, double toeInEl, double twist) {
		try {
			yawPitchRoll[0] = 0.0;
			yawPitchRoll[1] = 0.0;
			yawPitchRoll[2] = 0.0;			
			this.setCurrent();
			GLContext.useContext(this);
	        GL11.glMatrixMode(GL11.GL_MODELVIEW);
//			fillRoverQuaternionMatrix();
			ImageMetadataEntry imageMetadataEntry = imageListEntry.getImageMetadataEntry();
			setImageRotation((float)(imageMetadataEntry.inst_az_rover+toeInAz), (float)(imageMetadataEntry.inst_el_rover+toeInEl), (float)(twist));
			modelMatrix.clear();
			GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelMatrix);
			getMatrixAsArray(modelMatrix, modelArray);
			double az = -Math.atan2(modelArray[2][0], modelArray[2][2]);
			yawPitchRoll[0] = az / Math.PI * 180;
			double el = Math.atan2(-modelArray[2][1], Math.sqrt(modelArray[0][1]*modelArray[0][1]+modelArray[1][1]*modelArray[1][1]));
			yawPitchRoll[1] = el / Math.PI * 180;
			double rl = -Math.atan2(modelArray[0][1], modelArray[1][1]);
			yawPitchRoll[2] = rl / Math.PI * 180;			
		} catch (LWJGLException e) {
			if (!supressError) {
				supressError = true;
				UnknownExceptionDialog.openDialog(this.getShell(), "Error finding image yaw pitch roll", e);
				e.printStackTrace(); 
			}
		}
	}
	
	/**
	 * Bind textures that have been pre-loaded by the imageLoader thread.
	 */
	private void bindTextures() {
		try {
//			if (textureLoadIndex < maxNumTextures) {
			boundAll = true;
			for (int n=0; n<sortedPanImageList.length; n++) {
				PanImageEntry panImageEntry = sortedPanImageList[n];
				if (panImageEntry.imageListEntry.enabled && (!panImageEntry.loaded)) {
					boundAll = false;
				}
				if (panImageEntry.loadImageBuffer != null) {
					if (panImageEntry.textureNumber < 0) {
						// find new texture number
						if (textureLoadIndex < maxNumTextures) {
							panImageEntry.textureNumber = textures.get(textureLoadIndex);
					        textureLoadIndex++;							
						}
						else {
							// no more textures available
							System.out.println("out of available textures");
							panImageEntry.loadImageBuffer = null;
							imageLoader.suspend();
							break;
						}
					}
					
//				    System.out.println("*** binding texture "+panImageEntry.textureNumber);
//				    long startTime = System.currentTimeMillis();
			        GL11.glBindTexture(GL11.GL_TEXTURE_2D, panImageEntry.textureNumber);
/*			        
			        if (mipmap) {
			            GLU.gluBuild2DMipmaps(GL11.GL_TEXTURE_2D, GL11.GL_RGB8, panImageEntry.loadImageWidth, panImageEntry.loadImageHeight, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, panImageEntry.loadImageBuffer);
				        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
			            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
			        }
			        else {
			            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB8, panImageEntry.loadImageWidth, panImageEntry.loadImageHeight, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, panImageEntry.loadImageBuffer);
			            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
			            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
			        }
*/
			        int numLevels = panImageEntry.loadImageBuffer.size();
			        if (numLevels == 1) {
			        	int width  = ((Integer) panImageEntry.loadImageWidth.get(0)).intValue();
			        	int height  = ((Integer) panImageEntry.loadImageHeight.get(0)).intValue();
			        	ByteBuffer buffer = (ByteBuffer) panImageEntry.loadImageBuffer.get(0);
			            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB8, width, height, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, buffer);
			            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
			            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
			        }
			        else {
				        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
			            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR/*GL11.GL_NEAREST*/);
			        	for (int level=0; level<numLevels; level++) {
				        	int width  = ((Integer) panImageEntry.loadImageWidth.get(level)).intValue();
				        	int height  = ((Integer) panImageEntry.loadImageHeight.get(level)).intValue();
				        	ByteBuffer buffer = (ByteBuffer) panImageEntry.loadImageBuffer.get(level);
				            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, level, GL11.GL_RGB8, width, height, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, buffer);
			        	}
			        }
			        panImageEntry.loadImageBuffer = null;
			        panImageEntry.loadImageHeight = null;
			        panImageEntry.loadImageWidth = null;
			        panImageEntry.loaded = true;
//				        System.out.println("done");
//			        long time = System.currentTimeMillis() - startTime;
//			        System.out.println("took "+time+" millis");
			        break;
				}
			}
		}
		catch (Throwable e) {
			UnknownExceptionDialog.openDialog(this.getShell(), "Error loading image", e);
			e.printStackTrace(); 
		}
		
	}	
	
	private Comparator comp = new Comparator() {
		public int compare(Object object, Object object1) {
			PanImageEntry element1 = (PanImageEntry) object;
			PanImageEntry element2 = (PanImageEntry) object1;
			if (element1 == selectedPanImageEntry) {
				return -1;
			}
			else if (element2 == selectedPanImageEntry) {
				return 1;
			}
			if ((element1.camera == 'N') && (element2.camera == 'P')) {
				return -1;
			}
			else if ((element1.camera == 'P') && (element2.camera == 'N')) {
				return 1;
			}
			else {
				if ((element2.projectedZ) < (element1.projectedZ)) {
					return -1;
				}
				else if ((element2.projectedZ) > (element1.projectedZ)) {
					return 1;
				}
				else {
					return 0;
				}
			}
		}
		public boolean equals(Object object) {
			return false;
		}
	};
	
	private void sortPanImages() {
		findProjectedZs();
		for (int n=0; n<panImageList.length; n++) {
			sortedPanImageList[n] = panImageList[n];
		}
		Arrays.sort(sortedPanImageList, comp);
	}
		
	private void findProjectedZs() {
		float projectedXYZ[] = new float[3];
		
		projectionMatrix.clear();
		GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projectionMatrix);
		getMatrixAsArray(projectionMatrix, projectionArray);
		viewportMatrix.clear();
		GL11.glGetInteger(GL11.GL_VIEWPORT, viewportMatrix);
		viewportArray[0] = viewportMatrix.get(0);
		viewportArray[1] = viewportMatrix.get(1);
		viewportArray[2] = viewportMatrix.get(2);
		viewportArray[3] = viewportMatrix.get(3);
		
		for (int n=0; n<panImageList.length; n++) {
			ImageMetadataEntry metadataEntry = panImageList[n].imageListEntry.getImageMetadataEntry();
			setImageRotation((float)metadataEntry.inst_az_rover, (float)metadataEntry.inst_el_rover, 0.0f);
			modelMatrix.clear();
			GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelMatrix);
			getMatrixAsArray(modelMatrix, modelArray);
			GLU.gluProject(0.0f, 0.0f, -50.0f, modelArray, projectionArray, viewportArray, projectedXYZ);			
			float projectedZ = projectedXYZ[2];
			if (projectedZ > 1.0f) 
				projectedZ = 1.0f - projectedZ;
			panImageList[n].projectedZ = projectedZ;
		}		
	}
	
	private static double selectTolerance = 0.1;
	
	/**
	 * Find the image who's center is closest to screen center.
	 * We can't just get the closest image to center from the sorted pan image list,
	 * because that list is also sorted by image type, whereas here we just want the closest.
	 * Also, if the selected image is sufficiently close to the "closest" image, want to 
	 * return the selected image instead, to avoid selection bouncing.
	 */
	private PanImageEntry findPanImageEntryClosestToCenter() {
		findProjectedZs();
		
		PanImageEntry closestEntry = null;
		for (int n=0; n<panImageList.length; n++) {
			PanImageEntry entry = panImageList[n];
			if (entry.draw) {
				if (closestEntry == null) {
					closestEntry = entry;
				}
				else if (entry.projectedZ > closestEntry.projectedZ) {
					closestEntry = entry;
				}
				else if (entry.projectedZ == closestEntry.projectedZ) {
					if (isSuperiorImageCat(entry.imageListEntry.getImageCategory(), closestEntry.imageListEntry.getImageCategory())) {
						closestEntry = entry;
					}
				}
			}
		}
		if (selectedPanImageEntry != null) {
			double dAz = closestEntry.imageListEntry.getImageMetadataEntry().inst_az_rover - 
			selectedPanImageEntry.imageListEntry.getImageMetadataEntry().inst_az_rover;
			double dEl = closestEntry.imageListEntry.getImageMetadataEntry().inst_el_rover - 
			selectedPanImageEntry.imageListEntry.getImageMetadataEntry().inst_el_rover;
			if ((selectedPanImageEntry.imageListEntry.getImageMetadataEntry().inst_el_rover < -85.0 
					&&  closestEntry.imageListEntry.getImageMetadataEntry().inst_el_rover < 85.0)
					|| (selectedPanImageEntry.imageListEntry.getImageMetadataEntry().inst_el_rover > 85
						&& closestEntry.imageListEntry.getImageMetadataEntry().inst_el_rover > 85)	
					) {
				// this is a fix because the distance computation doesn't work right at high or low elevations...
				// in fact, the whole thing is pretty messed up
				closestEntry = selectedPanImageEntry;				
			}
			else if ((Math.abs(dAz) < selectTolerance) && (Math.abs(dEl) < selectTolerance)) {
				closestEntry = selectedPanImageEntry;
			}
		}
		return closestEntry;
	}
	
	/**
	 * Go through the pan image list (which stays in the original image list order)
	 * and choose the best image for each individual pointing, setting the draw
	 * flag accordingly.
	 */
	private void findDrawList() {
		PanImageEntry chosenPanImage = null;
		for (int n=0; n < panImageList.length; n++) {
			PanImageEntry panImage = panImageList[n];
			if (!panImage.imageListEntry.enabled) {
				panImage.draw = false;
				continue;
			}
			if ((chosenPanImage == null) 
					|| (chosenPanImage.imageListEntry.getImageCategory().charAt(0) !=  panImage.imageListEntry.getImageCategory().charAt(0))
					|| (chosenPanImage.imageListEntry.getImageMetadataEntry().inst_az_rover != panImage.imageListEntry.getImageMetadataEntry().inst_az_rover)
					|| (chosenPanImage.imageListEntry.getImageMetadataEntry().inst_el_rover != panImage.imageListEntry.getImageMetadataEntry().inst_el_rover)
					) {
				// different pointing
				panImage.draw = true;
				chosenPanImage = panImage;
			}
			else {
				// same pointing as lastPanImage
				// find out if this image is better choice (selected or better image class)
				String imageCat = panImage.imageListEntry.getImageCategory();
				String chosenImageCat = chosenPanImage.imageListEntry.getImageCategory();
				if (panImage == selectedPanImageEntry) {
					chosenPanImage.draw = false;
					panImage.draw = true;
					chosenPanImage = panImage;
				}
				else if ((chosenPanImage != selectedPanImageEntry) && (isSuperiorImageCat(imageCat, chosenImageCat)))
				{
					chosenPanImage.draw = false;
					panImage.draw = true;
					chosenPanImage = panImage;
				}
				else {
					panImage.draw = false;
				}
			}
		}
	}
	
	/**
	 * Get a list of PanExportImageEntry's, with yaw-pitch-roll, for export.
	 */
	public PanExportImageEntry[] getExportList() {
		PanExportImageEntry[] list = new PanExportImageEntry[panImageList.length];
		for (int n=0; n<panImageList.length; n++) {
			PanExportImageEntry entry  = new PanExportImageEntry(panImageList[n].imageListEntry);
			list[n] = entry;
			
			// TODO this duplicates code in renderImages; need to consolidate this somehow
			char spacecraftIdChar = selectedSpacecraftId.charAt(0);
			double twist = 0.0;
			double imageFOV = 10.0;
			double toeInAz = 0.0;
			double	toeInEl = 0.0;
			if (entry.imageListEntry.getImageCategory().startsWith("n")) {
				imageFOV = 45.1766; // the "official" FOV
				if (spacecraftIdChar == MerUtils.SPACECRAFT_ID_CHAR_OPPORTUNITY) {
					twist = -0.1;
					imageFOV = imageFOV * 1.006;
				}
				else if (spacecraftIdChar == MerUtils.SPACECRAFT_ID_CHAR_SPIRIT) {
					//if (params.viewMode != PanParams.VIEWMODE_RIGHT_RAW) {
						twist = -0.7;
					//}
					imageFOV = imageFOV * 1.015;
				}
			}
			else if (entry.imageListEntry.getImageCategory().startsWith("p")) {
				imageFOV = 15.8412; // the official value
				/*if (params.viewMode != PanParams.VIEWMODE_RIGHT_RAW) {
					// compensate for toe-in left
					azimuthDeg += 1.0;
					elevationDeg += 0.3;
				}*/					
				// TODO reverse toe in for right camera
				//toeInComp = 0.6;
				if (spacecraftIdChar == MerUtils.SPACECRAFT_ID_CHAR_OPPORTUNITY) {
//						twistDeg = 0.3; // was 0.4;
					twist = 0.4;
					imageFOV = imageFOV * 1.01;
					toeInAz = 1.1;		
					toeInEl = -0.35;
				}
				else if (spacecraftIdChar == MerUtils.SPACECRAFT_ID_CHAR_SPIRIT) {
					twist = +0.0;
					imageFOV = imageFOV * 1.015;
					//fovDeg = fovDeg * 1.015;
					toeInAz = 0.0;
					toeInEl = 0.0;
				}
			}							
			
			double[] yawPitchRoll = new double[3];
			this.getImageYawPitchRoll(panImageList[n].imageListEntry, yawPitchRoll,toeInAz,toeInEl,twist);
			entry.yaw = yawPitchRoll[0];
			entry.pitch = yawPitchRoll[1];
			entry.roll = yawPitchRoll[2];	
			entry.fov = imageFOV;
		}
		return list;
	}
	
	/**
	 * Returns true if newImageCat is "better" than chosenImageCat.
	 * @param chosenImageCat
	 * @param newImageCat
	 * @return
	 */	
	boolean isSuperiorImageCat(String newImageCat, String chosenImageCat) {
		return (	(newImageCat.equals("pc") && (chosenImageCat.equals("p")))
					|| (newImageCat.equals("ps") && ((chosenImageCat.equals("p") || (chosenImageCat.equals("pc")))))
				);
	}
	
	public void moveRoverModelForward() {
		LocationCounter roverModelLocation = null;
		if (roverModelLocationMetadata != null) {
			roverModelLocation = roverModelLocationMetadata.location;
		}
		if (roverModelLocation == null) {
			roverModelLocation = currentLocationCounter;
		}
		int index = Arrays.binarySearch(allLocations, roverModelLocation);
		if (index >= 0 && index < allLocations.length-1) {
			roverModelLocation = allLocations[index + 1];
		}
		else if (index < 0) {
			index = -index -1;
			if (index > allLocations.length-1)
				index = allLocations.length-1;
			roverModelLocation = allLocations[index];
		}
		roverModelLocationMetadata =workspace.getLocationMetadata().getEntry(selectedSpacecraftId, roverModelLocation);
		editor.settings.panShowRoverModel = true;
		editor.fireViewSettingsChanged(null, editor.settings);
		this.redraw();
	}
	
	public void moveRoverModelBackward() {
		LocationCounter roverModelLocation = null;
		if (roverModelLocationMetadata != null) {
			roverModelLocation = roverModelLocationMetadata.location;
		}
		if (roverModelLocation == null) {
			roverModelLocation = currentLocationCounter;
		}
		int index = Arrays.binarySearch(allLocations, roverModelLocation);
		if (index > 0) {
			roverModelLocation = allLocations[index - 1];
		}
		else if (index < 0) {
			index = -index -1 - 1;
			if (index < 0) {
				index = 0;
			}
			roverModelLocation = allLocations[index];
		}
		roverModelLocationMetadata =workspace.getLocationMetadata().getEntry(selectedSpacecraftId, roverModelLocation);
		editor.settings.panShowRoverModel = true;
		editor.fireViewSettingsChanged(null, editor.settings);
		this.redraw();
	}
	
	
	
	int keyCode;
	int	keyStateMask;
	long keyStartTime = 0;
	long keyStopTime = 0;
	
	final float fovAdjFactor = 1.5f;
	
	/**
	 * Handle movement caused by keyboard.
	 * Returns true if moved.
	 * @return
	 */
	private boolean handleMovementByKey() {
		boolean moved = false;
		if (keyCode != 0) {
			moved = true;
			double moveRate = 10.0 / 1000;	// degrees per second
			
			if ((keyStateMask & SWT.SHIFT) != 0) {
				moveRate = moveRate * 10.0;
			}
			else if ((keyStateMask & SWT.ALT) != 0) {
				moveRate = moveRate / 5.0;
			}
			
			long moveTime = 0;
			if (keyStopTime != 0) {
				moveTime = keyStopTime - keyStartTime;
			}
			else {
				long currentTime = System.currentTimeMillis();
				moveTime = currentTime - keyStartTime;
				keyStartTime = currentTime;
			}
			
			float move = (float)(moveRate * moveTime);
			
			switch (keyCode) {
			case 16777219:
				// left
				viewAzimuth = fixAzimuth(viewAzimuth - move);
				break;
			case 16777220:
				// right
				viewAzimuth = fixAzimuth(viewAzimuth + move);
				break;
			case 16777217:
				// up
				viewElevation = fixElevation(viewElevation + move);
				break;
			case 16777218:
				// down
				viewElevation = fixElevation(viewElevation - move);
				break;
			case 97:
				// a
				viewVFOV = fixFOV(viewVFOV - (move * fovAdjFactor));
				break;
			case 122:
				// z
				viewVFOV = fixFOV(viewVFOV + (move * fovAdjFactor));
				break;
			}
			
			if (keyStopTime != 0) {
				keyCode = 0;
				keyStartTime = 0;
			}
		}
		return moved;
	}
	
	public void keyPressed(KeyEvent e) {
//		System.out.println("keyPressed "+e);
		if (keyCode != e.keyCode) {
			keyCode = e.keyCode;
			keyStateMask = e.stateMask;
			keyStartTime = System.currentTimeMillis();
			keyStopTime = 0;
			moveToMode = false;
			this.redraw();
		}
	}

	public void keyReleased(KeyEvent e) {
		keyStopTime = System.currentTimeMillis();
	}
	
	public void mouseDoubleClick(MouseEvent e) {
	}

	public void mouseDown(MouseEvent e) {
//		System.out.println("mouseDown "+e);
		mousePressed = true;
		mouseDragged = false;
		mouseStartTime = System.currentTimeMillis();
		mouseStopTime = 0;
		mouseStartX = e.x;
		mouseStartY = e.y;
		mouseX = e.x;
		mouseY = e.y;
		mouseButton = e.button;
		mouseStateMask = e.stateMask;
		moveToMode = false;
		this.redraw();
	}

	public void mouseUp(MouseEvent e) {
		mousePressed = false;
		mouseStopTime = System.currentTimeMillis();
		if (!mouseDragged) {
			RoverTrackingListEntry entry = findHotspotAt(e.x, e.y);
			if (entry != null) {
				editor.setSelectedTimeInterval(entry.locationListEntry, false);
			}
		}
	}
	
	public void mouseMove(MouseEvent e) {
		if (mousePressed) {
//			System.out.println("mouseMove "+e);
			mouseX = e.x;
			mouseY = e.y;
			mouseStateMask = e.stateMask;
			mouseDragged = true;
		}
		else {
			RoverTrackingListEntry entry = findHotspotAt(e.x, e.y);
			if (entry != null) {
				roverModelLocationMetadata = entry.locationMetadataEntry;
				editor.settings.panShowRoverModel = true;
				this.redraw();
			}			
		}
	}
	
	private boolean handleMovementByMouse() {
		boolean moved = false;
		if (mouseStartTime != 0) {
			moved = true;
			double moveRate = 20.0 / (1000 * 100);	// degrees per second per 100 pixels
			
			long moveTime = 0;
			if (mouseStopTime != 0) {
				moveTime = mouseStopTime - mouseStartTime;
				mouseStartTime = 0;
				mouseStopTime = 0;
			}
			else {
				long currentTime = System.currentTimeMillis();
				moveTime = currentTime - mouseStartTime;
				mouseStartTime = currentTime;
			}
			
			int dx = mouseX - mouseStartX;
			int dy = mouseY - mouseStartY;
						
			float moveX = (float)(moveRate * moveTime * dx);
			float moveY = (float)(moveRate * moveTime * dy);
			
			viewAzimuth = fixAzimuth(viewAzimuth + moveX);
			if (((mouseStateMask & SWT.ALT) != 0) 
					|| (mouseButton == 3)) {
				viewVFOV = fixFOV(viewVFOV + (moveY * fovAdjFactor));
			}
			else {
				viewElevation = fixElevation(viewElevation - moveY);
			}
		}
		return moved;
	}
	
	public void startMoveToAzEl(float moveToAz, float moveToEl) {
		moveToMode = true;
		moveToLastTime = System.currentTimeMillis();		
		viewAzimuth = fixAzimuth(viewAzimuth);
		viewElevation = fixElevation(viewElevation);
		moveToAz = fixAzimuth(moveToAz);
		moveToEl = fixElevation(moveToEl);
		this.moveToAz = moveToAz;
		this.moveToEl = moveToEl;
		double deltaAz = fixAzimuth(moveToAz - viewAzimuth);	
		double deltaEl = moveToEl - viewElevation;
		double dist = Math.sqrt(deltaAz*deltaAz+deltaEl*deltaEl);
		double moveRatePerMilli = 0.3;
		moveToAzRate = (float)(deltaAz / dist * moveRatePerMilli);
		moveToElRate = (float)(deltaEl / dist * moveRatePerMilli);
//		moveToAzRate = deltaAz / (1000 / 5);
//		moveToElRate = deltaEl / (1000 / 5);
		redraw();
	}
	
	public void playMovie() {
		if ((movieFrames != null) && (movieFrames.length > 0)) {
			playMovieMode = !playMovieMode;
			if (playMovieMode) {
				if (movieFrameIndex >= movieFrames.length-1) {
					movieFrameIndex = 0;
				}
				redraw();
			}			
		}
	}
	
	public void exportMovieFrames() {
		if ((movieFrames != null) && (movieFrames.length > 0)) {
			exportMovieFramesMode = true;
			movieFrameIndex = 0;
			setPanPosition(movieFrames[movieFrameIndex]);
			while (exportMovieFramesMode) {
				renderView();
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void nextMovieFrame() {
		if ((movieFrames != null) && (movieFrames.length > 0)) {
			playMovieMode = false;
			if (movieFrameIndex < movieFrames.length-1) {
				movieFrameIndex++;
				setPanPosition(movieFrames[movieFrameIndex]);	
				this.redraw();
			}
		}
	}
	
	public void previousMovieFrame() {
		if ((movieFrames != null) && (movieFrames.length > 0)) {
			playMovieMode = false;
			if (movieFrameIndex > 0) {
				movieFrameIndex--;
				setPanPosition(movieFrames[movieFrameIndex]);	
				this.redraw();
			}
		}
	}
	
	public void nextMovieEndpoint() {
		if ((movieFrames != null) && (movieFrames.length > 0)) {
			playMovieMode = false;
			PanMovieEndpoint endpoint = movieFrames[movieFrameIndex].panMovieEndpoint;
			while (movieFrameIndex < movieFrames.length-1) {
				movieFrameIndex++;
				if (movieFrames[movieFrameIndex].panMovieEndpoint != endpoint) {
					break;
				}
			}
			setPanPosition(movieFrames[movieFrameIndex]);	
			this.redraw();
		}
	}
	
	public void previousMovieEndpoint() {
		if ((movieFrames != null) && (movieFrames.length > 0)) {
			playMovieMode = false;
			if (movieFrameIndex > 0) { 
				movieFrameIndex--;
				PanMovieEndpoint endpoint = movieFrames[movieFrameIndex].panMovieEndpoint;
				while (movieFrameIndex > 0) {
					if (movieFrames[movieFrameIndex-1].panMovieEndpoint != endpoint) {
						break;
					}
					movieFrameIndex--;
				}
				setPanPosition(movieFrames[movieFrameIndex]);	
				this.redraw();
			}
		}
	}
	
	
	void setPanPosition(PanPosition pos) {
		viewAzimuth = fixAzimuth(pos.viewAz);
		viewElevation = fixElevation(pos.viewEl);
		viewVFOV = pos.viewVFOV;
		
		if (pos.roverModelLocationMetadata != null) {
			if (!editor.settings.panShowRoverModel) {
				editor.settings.panShowRoverModel = true;
				editor.fireViewSettingsChanged(null, editor.settings);
			}
			roverModelLocationMetadata = pos.roverModelLocationMetadata;
		}
		else {
			if (editor.settings.panShowRoverModel) {
				editor.settings.panShowRoverModel = false;
				editor.fireViewSettingsChanged(null, editor.settings);
			}
			roverModelLocationMetadata = null;
		}
		
		if (!pos.location.equals(currentLocationCounter)) {
//			setPanImageList(pos.spacecraftId, this.imageList, this.locationList, pos.location, false);			
//			int index = locationList.findIndexOfLocation(locationCounter);
//			editor.fireLocationSelectionChanged(null, new Integer(index));
			// Have to go through the editor to keep things synced up;
			// sorry, this is kind of a mess.
			TimeIntervalList locationList = editor.getTimeIntervalList();
			TimeInterval entry = locationList.findEntryClosestToLocation(pos.location);
			editor.setSelectedTimeInterval(entry, false);
		}
		else {
            editor.fireDirectionChanged();
		}
	}
	
	private float fixAzimuth(float azimuth) {
		while (azimuth > 180.0f) {
			azimuth -= 360.0f;
		}
		while (azimuth < -180.0f) {
			azimuth += 360.0f;
		}
		return azimuth;
	}
	
	private float fixElevation(float elevation) {
		if (elevation > 90.0f) {
			elevation = 90.0f;
		}
		else if (elevation < -90.0f) {
			elevation = -90.0f;
		}
		return elevation;
	}
	
	float fixFOV(float fov) {
		if (fov < 1.0f) {
			fov = 1.0f;
		}
		else if (fov > maxFOV) {
			fov = maxFOV;
		}
		return fov;
	}
	
	private boolean handleMoveTo() {
		boolean moved = false;
		long currentTime = System.currentTimeMillis();
		long moveTime = currentTime - moveToLastTime;
		moveToLastTime = currentTime;
		if ((viewAzimuth == moveToAz) && (viewElevation == moveToEl)) {
			moveToMode = false;
		}
		if (moveToMode) {
			float dAz = moveToAzRate * moveTime;
			float dEl = moveToElRate * moveTime;
			if (Math.abs(moveToAzRate) > Math.abs(moveToElRate)) {
				if (Math.abs(dAz) >= Math.abs(fixAzimuth(moveToAz - viewAzimuth))) {
					viewAzimuth = moveToAz;
					viewElevation = moveToEl;
				}
				else {
					viewAzimuth = viewAzimuth += dAz;
					viewElevation = viewElevation += dEl;
				}
			}
			else {
				if (Math.abs(dEl) >= Math.abs(fixElevation(moveToEl - viewElevation))) {
					viewAzimuth = moveToAz;
					viewElevation = moveToEl;
				}
				else {
					viewAzimuth = viewAzimuth += dAz;
					viewElevation = viewElevation += dEl;					
				}
			}
			moved = true;
		}
		if (isMovingToLocation()) {
			currentLocationVectorA = simpleMove(currentLocationVectorA, dstLocationVectorA, moveLocationVectorA*moveTime);
			currentLocationVectorB = simpleMove(currentLocationVectorB, dstLocationVectorB, moveLocationVectorB*moveTime);
			currentLocationVectorC = simpleMove(currentLocationVectorC, dstLocationVectorC, moveLocationVectorC*moveTime);			
			moved = true;
			if (!isMovingToLocation()) {
				if (roverModelLocationMetadata != null && roverModelLocationMetadata.location.equals(currentLocationCounter)) {
					editor.settings.panShowRoverModel = false;
					// huh. Can we do this safely here? Probably not.
					//editor.fireViewSettingsChanged(null, editor.settings);
				}
			}
		}
		return moved;
	}

	boolean isMovingToLocation() {
		return (currentLocationVectorExists && dstLocationVectorExists && 
				(currentLocationVectorA != dstLocationVectorA || currentLocationVectorB != dstLocationVectorB || currentLocationVectorC != dstLocationVectorC));
	}
	
	double simpleMove(double currentVal, double dstVal, double moveVal) {
		if (dstVal > currentVal) {
			if (moveVal > 0) {
				currentVal += moveVal;
				if (currentVal > dstVal) {
					currentVal = dstVal;
				}
			}
			else {
				currentVal = dstVal;
			}
		}
		else if (dstVal < currentVal) {
			if (moveVal < 0) {
				currentVal += moveVal;
				if (currentVal < dstVal) {
					currentVal = dstVal;
				}
			}
			else {
				currentVal = dstVal;
			}
		}
		return currentVal;
	}
	
	
	/* This version of takePanoramaScreenshot() works for SWT, but SWT support for saving images 
	 * is deficient in Eclipse 3.2, so we need to do it the old fashioned way with ImageBuffer instead.
	 * At the moment for SWT, all we can get away with appears to be BMP - this is useful for pan 
	 * movie screenshots that don't blow up the AWT-SWT thread issue.
	 */
	public void takePanoramaScreenshotSWT(int movieFrameNum) {
		try {
			this.setCurrent();
			GLContext.useContext(this);
			//System.out.println("takeScreenshot");
	        Rectangle rect = this.getClientArea();
			ByteBuffer convImageBuffer = ByteBuffer.allocateDirect(rect.width * rect.height * 4);
			GL11.glReadBuffer(GL11.GL_FRONT);
			GL11.glReadPixels(0, 0, rect.width, rect.height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, convImageBuffer);
			byte[] bytes = new byte[rect.width * rect.height * 4];
			convImageBuffer.get(bytes);
			
			// TODO
			PaletteData palette = new PaletteData (0xff000000, 0xff0000, 0xff00);
			ImageData imageData = new ImageData(rect.width, rect.height, 32, palette, 1, bytes);
			ImageData convImageData = ImageDataUtil.convertImageDataBack(imageData);
			
			String filename = ""+movieFrameNum+".bmp";
			File screenshotDir = new File(workspace.getWorkspaceDir(), "movies/frames");
			if (!screenshotDir.exists()) {
				screenshotDir.mkdirs();
			}			
			File file = new File(screenshotDir, filename);
			ImageLoader imageLoader = new ImageLoader();
			imageLoader.data = new ImageData[] {convImageData};
			imageLoader.save(file.getAbsolutePath(), SWT.IMAGE_BMP);
		} catch (Throwable e) {
//			UnknownExceptionDialog.openDialog(this.getShell(), "Error taking screenshot", e);
			System.err.println(e);
		}
	}
	
	public void takePanoramaScreenshot(boolean movie, int movieFrameNum) {
		try {
			this.setCurrent();
			GLContext.useContext(this);
			System.out.println("takeScreenshot");
	        Rectangle rect = this.getClientArea();
	        int width = rect.width;
	        int height = rect.height;
	        int length = width * height;
			ByteBuffer byteBuffer = ByteBuffer.allocateDirect(length * 4);
			GL11.glReadBuffer(GL11.GL_FRONT);
			GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, byteBuffer);
			
			int[] pixels = new int[length];
			byteBuffer.rewind();
			int index;
			for (int y=height-1; y>=0; y--) {
				index = y*width;
				for (int x=0; x<width; x++) {
		            pixels[index++] =
		                0xFF000000
		                | (((int)(byteBuffer.get()) & 0x000000FF) << 16)
		                | (((int)(byteBuffer.get()) & 0x000000FF) << 8)
		                | (((int)(byteBuffer.get()) & 0x000000FF));
		            byteBuffer.get();
				}
			}
			byteBuffer = null;		// done with byteBuffer; release the memory

			// Create BufferedImage
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            image.setRGB(0, 0, width, height, pixels, 0, width);
            
            // caption
            if (!exportMovieFramesMode) {
			    Graphics2D graphics = image.createGraphics();
			    FontMetrics metrics = graphics.getFontMetrics();
				int fontHeight = metrics.getHeight();
				String caption = "";			
				caption = caption + MerUtils.displayRoverNameFromRoverCode(selectedSpacecraftId) + 
					" location " + selectedTimeInterval.getStartLocation().siteDriveCode + " sol "
			    	+ selectedTimeInterval.getSolsString();
			    if (groundRelative) {
			    	caption = caption + "  ground-relative az ";
			    }
			    else {
			    	caption = caption + "  rover-relative az ";
			    }
			    caption = caption + MerUtils.round(viewAzimuth, 100) + " el "
			    	+ MerUtils.round(viewElevation, 100) + " HFOV "
			    	+ MerUtils.round(getViewHFOV(), 100) +".";
				if (editor.settings.panShowRoverModel) {
					graphics.drawString(caption, 5, height-10-fontHeight);
					// TODO FIX
					caption = "Rover model at location "+this.roverModelLocationMetadata.location.siteDriveCode;
/* TODO need to fix this up to take different sites into account
					LocationListEntry entry = locationList.getEntry(locationList.findIndexOfLocation(this.roverModelLocation));
					if (entry != null) {
						caption = caption + " sol "+entry.getSolsString();
					}
					// TODO deal with site frame
					if (roverModelLocation.site == currentLocationCounter.site) {
						double da = panLocationListEntry.getLocationMetadataEntry().rover_origin_offset_vector_a 
							- entry.getLocationMetadataEntry().rover_origin_offset_vector_a;
						double db = panLocationListEntry.getLocationMetadataEntry().rover_origin_offset_vector_b 
						- entry.getLocationMetadataEntry().rover_origin_offset_vector_b;
						double dc = panLocationListEntry.getLocationMetadataEntry().rover_origin_offset_vector_c 
						- entry.getLocationMetadataEntry().rover_origin_offset_vector_c;
						double dist = Math.sqrt(da*da + db*db + dc*dc);
						caption = caption + " distance "+MerUtils.round(dist, 100)+"m";
					}
*/					
					caption = caption + ".";
				    graphics.drawString(caption, 5, height-10);
				}		    
				else {
				    graphics.drawString(caption, 5, height-10);			
				}
            }
            
			File screenshotDir = workspace.getScreenshotDir();
			File numbersDir = new File(screenshotDir, "numbers");
			String filenameRoot = null;
			if (movie) {
				screenshotDir = new File(workspace.getWorkspaceDir(), "movies/frames");
			}
			else {
				if (!numbersDir.isDirectory()) {
					filenameRoot = MerUtils.displayRoverNameFromRoverCode(selectedSpacecraftId) +"Sol"
						+ selectedTimeInterval.getSolsString();
				}
				else {
					filenameRoot = null;
					screenshotDir = numbersDir;
				}
			}
			String filetype = "png";
			float quality = 0.9f;
			if (screenshotDir != null) {
				if (!screenshotDir.exists()) {
					screenshotDir.mkdirs();
				}
				File outFile = null;
				String filename;
				int count = 1;
				do {
					if (movie) {
						filename = ""+movieFrameNum+"."+filetype;
					}
					else {
						if (filenameRoot != null) {
							filename = filenameRoot+"_"+count+"."+filetype;
						}
						else {
							filename = ""+count+"."+filetype;
						}
					}
					outFile = new File(screenshotDir, filename);
					count++;
				}
				while (outFile.exists());
			
				if (filetype.equalsIgnoreCase("jpg")) {
					FileOutputStream out = new FileOutputStream(outFile);
					JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
					JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(image);
					param.setQuality(quality, false); // don't know if this really makes a difference
					encoder.setJPEGEncodeParam(param);
					encoder.encode(image);
					out.close();
				}
				else if (filetype.equalsIgnoreCase("png")) {
					ImageIO.write(image, "png", outFile);
				}
				else {
					throw new Exception("Unsupported file type \""+filetype+"\"");
				}
			}
			else {
				throw new Exception("Screenshot directory not set");
			}
		} catch (Throwable e) {
			UnknownExceptionDialog.openDialog(this.getShell(), "Error taking screenshot", e);
		}
	}
	
	/**
	 * Adapted from http://nehe.gamedev.net/data/lessons/lesson.asp?lesson=Quaternion_Camera_Class
	 * make a 4x4 homogeneous matrix that can be applied to an OpenGL Matrix
	 * @param pMatrix
	 * @param m_x
	 * @param m_y
	 * @param m_z
	 * @param m_w
	 */
	void fillMatrixFromQuaternion(DoubleBuffer matrix, double m_x, double m_y, double m_z, double m_w)
	{		
		// First row
		matrix.put(1.0 - 2.0 * ( m_y * m_y + m_z * m_z ));
		matrix.put(2.0 * (m_x * m_y + m_z * m_w));
		matrix.put(2.0 * (m_x * m_z - m_y * m_w));
		matrix.put(0.0);
		
		// Second row
		matrix.put(2.0 * ( m_x * m_y - m_z * m_w ));
		matrix.put(1.0 - 2.0 * ( m_x * m_x + m_z * m_z ));
		matrix.put(2.0 * (m_z * m_y + m_x * m_w ));
		matrix.put(0.0);

		// Third row
		matrix.put(2.0 * ( m_x * m_z + m_y * m_w ));
		matrix.put(2.0 * ( m_y * m_z - m_x * m_w ));
		matrix.put(1.0 - 2.0 * ( m_x * m_x + m_y * m_y ));
		matrix.put(0.0);

		// Fourth row
		matrix.put(0.0);
		matrix.put(0.0);
		matrix.put(0.0);
		matrix.put(1.0);
		
		matrix.flip();
	}	
	
	void fillMatrix(DoubleBuffer matrix, Matrix4d m) {
		matrix.put(m.m00);
		matrix.put(m.m01);
		matrix.put(m.m02);
		matrix.put(m.m03);
		matrix.put(m.m10);
		matrix.put(m.m11);
		matrix.put(m.m12);
		matrix.put(m.m13);
		matrix.put(m.m20);
		matrix.put(m.m21);
		matrix.put(m.m22);
		matrix.put(m.m23);
		matrix.put(m.m30);
		matrix.put(m.m31);
		matrix.put(m.m32);
		matrix.put(m.m33);
	}
	
	void fillIdentityMatrix(FloatBuffer matrix) {
		matrix.rewind();
		
		matrix.put(1.0f);
		matrix.put(0.0f);
		matrix.put(0.0f);
		matrix.put(0.0f);
		
		matrix.put(0.0f);
		matrix.put(1.0f);
		matrix.put(0.0f);
		matrix.put(0.0f);
		
		matrix.put(0.0f);
		matrix.put(0.0f);
		matrix.put(1.0f);
		matrix.put(0.0f);
		
		matrix.put(0.0f);
		matrix.put(0.0f);
		matrix.put(0.0f);
		matrix.put(1.0f);
		
		matrix.flip();
	}
	
	public static void getMatrixAsArray(FloatBuffer fb , float[][] fa) {
        fa[0][0] = fb.get();
        fa[0][1] = fb.get();
        fa[0][2] = fb.get();
        fa[0][3] = fb.get();
        fa[1][0] = fb.get();
        fa[1][1] = fb.get();
        fa[1][2] = fb.get();
        fa[1][3] = fb.get();
        fa[2][0] = fb.get();
        fa[2][1] = fb.get();
        fa[2][2] = fb.get();
        fa[2][3] = fb.get();
        fa[3][0] = fb.get();
        fa[3][1] = fb.get();
        fa[3][2] = fb.get();
        fa[3][3] = fb.get();
    }

	public String getSpacecraftId() {
		return selectedSpacecraftId;
	}

	public LocationCounter getLocationCounter() {
		return currentLocationCounter;
	}
	
	public void loadPanoramaMovie(String filename) {
		//final int maxSingleDriveCount = 8;
		//final int defaultFrames = 15;
		//final int defaultPostFrames = 10;
		final int driveMultFactor = 2;
		double[] vector = new double[3];
		double[] quaternion = new double[4];
		
		ArrayList endpoints = new ArrayList();
		try {
			File movieFile = new File(filename);
			ExcelCSVReader csvReader = new ExcelCSVReader(new FileReader(movieFile));
			String[] lineItems;
			while ((lineItems = csvReader.readLine()) != null) {
				if (lineItems.length < 7) {
					throw new Exception("Movie file line did not have enough entries.");
				}
				if (!lineItems[0].trim().equalsIgnoreCase("endpoint")) {
					throw new Exception("Movie file line did not start with \"endpoint\".");
				}
				PanMovieEndpoint endpoint = new PanMovieEndpoint();
				endpoint.spacecraftId = lineItems[1];
				endpoint.location = new LocationCounter(lineItems[2], lineItems[3]);
				endpoint.viewAz = Float.parseFloat(lineItems[4]);
				endpoint.viewEl = Float.parseFloat(lineItems[5]);
				endpoint.viewVFOV = Float.parseFloat(lineItems[6]);
				endpoint.frames = -1;
				endpoint.postFrames = -1;
				if (lineItems.length > 8) {
					int site = Integer.parseInt(lineItems[7]);
					int drive = Integer.parseInt(lineItems[8]);
					if (site >=0 && drive >= 0) {
						endpoint.roverModelLocation = new LocationCounter(lineItems[7], lineItems[8]);
					}
				}
				if (lineItems.length > 9) {
					endpoint.frames = Integer.parseInt(lineItems[9]);
				}
				endpoints.add(endpoint);
			}
			// convert position endpoint to individual movie 'frames'
			ArrayList frames = new ArrayList();
			PanMovieEndpoint endpoint;
			PanMovieEndpoint nextEndpoint;
			//int locationIndex;
			//int nextLocationIndex;
			for (int n=0; n<endpoints.size()-1; n++) {
				endpoint = (PanMovieEndpoint) endpoints.get(n);
				nextEndpoint = (PanMovieEndpoint) endpoints.get(n+1);
//				locationIndex = locationList.findIndexOfLocation(endpoint.location);
//				nextLocationIndex = locationList.findIndexOfLocation(nextEndpoint.location);
				float dAz = fixAzimuth(nextEndpoint.viewAz - endpoint.viewAz);				
				float dEl = (nextEndpoint.viewEl - endpoint.viewEl);
				float dVFOV = (nextEndpoint.viewVFOV - endpoint.viewVFOV);
				int dDrive = LocationCounter.driveDiff(endpoint.location, nextEndpoint.location, allLocations);
				int dModelDrive = 0;
				if (endpoint.roverModelLocation != null && nextEndpoint.roverModelLocation != null) {
					dModelDrive = LocationCounter.driveDiff(endpoint.roverModelLocation, nextEndpoint.roverModelLocation, allLocations);
				}
				int numFrames = nextEndpoint.frames;
				if (numFrames < 0) {
					if (dModelDrive != 0) {
						numFrames = Math.abs(dModelDrive) * driveMultFactor;
					}
					else if (dDrive != 0) {
						numFrames = Math.abs(dDrive) * driveMultFactor;						
					}
					else {
						numFrames = (int) Math.sqrt(dAz*dAz + dEl*dEl + dVFOV*dVFOV) * 4;
						if (numFrames < 1) {
							numFrames = 1;
						}
					}
				}
				if (dModelDrive != 0 &&  nextEndpoint.frames < 0) {
					// rover model drive; automatic length
					int frameNum = 0;
					int startIndex = Arrays.binarySearch(allLocations, endpoint.roverModelLocation);
					int endIndex = Arrays.binarySearch(allLocations, nextEndpoint.roverModelLocation);
					if (startIndex < 0 || endIndex < 0) {
						System.out.println("invalid rover model location");
						continue;
					}
					int indexInc = startIndex < endIndex ? 1 : -1;
					for (int i = startIndex; i != endIndex; i = i + indexInc) {
						LocationCounter modelLocation1 = allLocations[i];
						LocationCounter modelLocation2 = allLocations[i + indexInc];
						LocationMetadataEntry entry1 = workspace.getLocationMetadata().getEntry(selectedSpacecraftId, modelLocation1);
						LocationMetadataEntry entry2 = workspace.getLocationMetadata().getEntry(selectedSpacecraftId, modelLocation2);
						//System.out.println("entry1 "+entry1.location.site+" "+entry1.location.drive);
						//System.out.println("entry2 "+entry2.location.site+" "+entry2.location.drive);
						
						if (!entry1.has_site_rover_origin_offset_vector || !entry2.has_site_rover_origin_offset_vector)
							continue;
						//System.out.println("-- "+entry1.rover_origin_offset_vector_a+" "+entry1.rover_origin_offset_vector_b+" "
						//		+entry1.rover_origin_offset_vector_c+" -> "+entry2.rover_origin_offset_vector_a+" "
						//		+entry2.rover_origin_offset_vector_b+" "+entry2.rover_origin_offset_vector_c);
						int delta = Math.abs(LocationCounter.driveDiff(modelLocation1, modelLocation2, allLocations)) * driveMultFactor;
						for (int j=0; j < delta; j++) {
							// interpolate view location
							LocationCounter viewLocation = endpoint.location;					
							if (dDrive > 0) {
								int driveAmount = frameNum * dDrive / numFrames;
								viewLocation = LocationCounter.driveAdd(viewLocation, driveAmount, allLocations);						
								// viewLocation is resolved to existing view location at display time
							}
							// new PanPosition							
							PanPosition position = new PanPosition();
							position.spacecraftId = endpoint.spacecraftId;
							position.location = viewLocation;
							position.viewAz = fixAzimuth((dAz * frameNum / numFrames) + endpoint.viewAz);
							position.viewEl = (dEl * frameNum / numFrames) + endpoint.viewEl;
							position.viewVFOV = (dVFOV * frameNum / numFrames) + endpoint.viewVFOV;
							position.panMovieEndpoint = endpoint;
							position.roverModelLocationMetadata = new LocationMetadataEntry(selectedSpacecraftId, entry1.location.site, entry1.location.drive);
							double ratio = ((double)j) / delta;
							double da = (entry2.rover_origin_offset_vector_a-entry1.rover_origin_offset_vector_a);
							double db = (entry2.rover_origin_offset_vector_b-entry1.rover_origin_offset_vector_b);
							double dc = (entry2.rover_origin_offset_vector_c-entry1.rover_origin_offset_vector_c);
							double dist = Math.sqrt((da*da)+(db*db)+(dc*dc));
							interpolateVector(entry1.rover_origin_offset_vector_a, entry1.rover_origin_offset_vector_b, entry1.rover_origin_offset_vector_c, 
									entry2.rover_origin_offset_vector_a, entry2.rover_origin_offset_vector_b, entry2.rover_origin_offset_vector_c, 
									vector, ratio);
							position.roverModelLocationMetadata.rover_origin_offset_vector_a = vector[0];
							position.roverModelLocationMetadata.rover_origin_offset_vector_b = vector[1];
							position.roverModelLocationMetadata.rover_origin_offset_vector_c = vector[2];
							//System.out.println(""+vector[0]+" "+vector[1]+" "+vector[2]);
							if (dist < 1.0) {								
								interpolateQuaternion(entry1.rover_origin_rotation_quaternian_a, entry1.rover_origin_rotation_quaternian_b, 
										entry1.rover_origin_rotation_quaternian_c, entry1.rover_origin_rotation_quaternian_d,
										entry2.rover_origin_rotation_quaternian_a, entry2.rover_origin_rotation_quaternian_b,
										entry2.rover_origin_rotation_quaternian_c, entry2.rover_origin_rotation_quaternian_d,
										quaternion, ratio);
								position.roverModelLocationMetadata.has_site_rover_origin_offset_vector = true;
								position.roverModelLocationMetadata.rover_origin_rotation_quaternian_a = quaternion[0];
								position.roverModelLocationMetadata.rover_origin_rotation_quaternian_b = quaternion[1];
								position.roverModelLocationMetadata.rover_origin_rotation_quaternian_c = quaternion[2];
								position.roverModelLocationMetadata.rover_origin_rotation_quaternian_d = quaternion[3];
							}
							else {
								position.roverModelLocationMetadata.has_site_rover_origin_offset_vector = true;
								position.roverModelLocationMetadata.rover_origin_rotation_quaternian_a = entry2.rover_origin_rotation_quaternian_a;
								position.roverModelLocationMetadata.rover_origin_rotation_quaternian_b = entry2.rover_origin_rotation_quaternian_b;
								position.roverModelLocationMetadata.rover_origin_rotation_quaternian_c = entry2.rover_origin_rotation_quaternian_c;
								position.roverModelLocationMetadata.rover_origin_rotation_quaternian_d = entry2.rover_origin_rotation_quaternian_d;									
							}
							frames.add(position);							
							frameNum++;
						}
					}					
				}
				else {
					// no rover model drive
					for (int frameNum=0; frameNum<numFrames; frameNum++) {
						// interpolate view location
						LocationCounter viewLocation = endpoint.location;					
						if (dDrive > 0) {
							int driveAmount = frameNum * dDrive / numFrames;
							viewLocation = LocationCounter.driveAdd(viewLocation, driveAmount, allLocations);						
							// viewLocation is resolved to existing view location at display time
						}
						// new PanPosition
						PanPosition position = new PanPosition();
						position.spacecraftId = endpoint.spacecraftId;
						position.location = viewLocation;
						position.viewAz = fixAzimuth((dAz * frameNum / numFrames) + endpoint.viewAz);
						position.viewEl = (dEl * frameNum / numFrames) + endpoint.viewEl;
						position.viewVFOV = (dVFOV * frameNum / numFrames) + endpoint.viewVFOV;
						position.panMovieEndpoint = endpoint;
						position.roverModelLocation = endpoint.roverModelLocation;						
						// interpolate model physical position
						// even the the rover model doesn't move here, the model may be shown, and 
						// we still may need to interpolate
						if (endpoint.roverModelLocation != null && nextEndpoint.roverModelLocation != null) {
							position.roverModelLocation = endpoint.roverModelLocation;
							if (dModelDrive > 0) {
								int driveAmount = frameNum * dModelDrive / numFrames;
								position.roverModelLocation = LocationCounter.driveAdd(position.roverModelLocation, driveAmount, allLocations);
							}
							int modelIndex = Arrays.binarySearch(allLocations, position.roverModelLocation);
							if (modelIndex >= 0) {
								LocationMetadataEntry entry = workspace.getLocationMetadata().getEntry(selectedSpacecraftId, allLocations[modelIndex]);
								if (entry != null && entry.has_site_rover_origin_offset_vector) {
									position.roverModelLocationMetadata = new LocationMetadataEntry(selectedSpacecraftId, position.roverModelLocation.site, position.roverModelLocation.drive);
									position.roverModelLocationMetadata.rover_origin_offset_vector_a = entry.rover_origin_offset_vector_a;
									position.roverModelLocationMetadata.rover_origin_offset_vector_b = entry.rover_origin_offset_vector_b;
									position.roverModelLocationMetadata.rover_origin_offset_vector_c = entry.rover_origin_offset_vector_c;
									position.roverModelLocationMetadata.has_site_rover_origin_offset_vector = true;
									position.roverModelLocationMetadata.rover_origin_rotation_quaternian_a = entry.rover_origin_rotation_quaternian_a;
									position.roverModelLocationMetadata.rover_origin_rotation_quaternian_b = entry.rover_origin_rotation_quaternian_b;
									position.roverModelLocationMetadata.rover_origin_rotation_quaternian_c = entry.rover_origin_rotation_quaternian_c;
									position.roverModelLocationMetadata.rover_origin_rotation_quaternian_d = entry.rover_origin_rotation_quaternian_d;
								}
							}
							else {
								modelIndex = -modelIndex -1;
								if (modelIndex > 0 && modelIndex < allLocations.length) {
									LocationCounter modelLocation1 = allLocations[modelIndex-1];
									LocationCounter modelLocation2 = allLocations[modelIndex];
									LocationMetadataEntry entry1 = workspace.getLocationMetadata().getEntry(selectedSpacecraftId, modelLocation1);
									LocationMetadataEntry entry2 = workspace.getLocationMetadata().getEntry(selectedSpacecraftId, modelLocation2);								
									position.roverModelLocationMetadata = new LocationMetadataEntry(selectedSpacecraftId, position.roverModelLocation.site, position.roverModelLocation.drive);
									int diff = LocationCounter.driveDiff(modelLocation1, position.roverModelLocation, allLocations);
									int diff2 = LocationCounter.driveDiff(modelLocation1, modelLocation2, allLocations);
									double ratio = ((double)diff) / diff2;
									double da = (entry2.rover_origin_offset_vector_a-entry1.rover_origin_offset_vector_a);
									double db = (entry2.rover_origin_offset_vector_b-entry1.rover_origin_offset_vector_b);
									double dc = (entry2.rover_origin_offset_vector_c-entry1.rover_origin_offset_vector_c);
									double dist = Math.sqrt((da*da)+(db*db)+(dc*dc));
									interpolateVector(entry1.rover_origin_offset_vector_a, entry1.rover_origin_offset_vector_b, entry1.rover_origin_offset_vector_c, 
											entry2.rover_origin_offset_vector_a, entry2.rover_origin_offset_vector_b, entry2.rover_origin_offset_vector_c, 
											vector, ratio);
									position.roverModelLocationMetadata.rover_origin_offset_vector_a = vector[0];
									position.roverModelLocationMetadata.rover_origin_offset_vector_b = vector[1];
									position.roverModelLocationMetadata.rover_origin_offset_vector_c = vector[2];
									if (dist < 1.0) {
										interpolateQuaternion(entry1.rover_origin_rotation_quaternian_a, entry1.rover_origin_rotation_quaternian_b, 
												entry1.rover_origin_rotation_quaternian_c, entry1.rover_origin_rotation_quaternian_d,
												entry2.rover_origin_rotation_quaternian_a, entry2.rover_origin_rotation_quaternian_b,
												entry2.rover_origin_rotation_quaternian_c, entry2.rover_origin_rotation_quaternian_d,
												quaternion, ratio);
										position.roverModelLocationMetadata.has_site_rover_origin_offset_vector = true;
										position.roverModelLocationMetadata.rover_origin_rotation_quaternian_a = quaternion[0];
										position.roverModelLocationMetadata.rover_origin_rotation_quaternian_b = quaternion[1];
										position.roverModelLocationMetadata.rover_origin_rotation_quaternian_c = quaternion[2];
										position.roverModelLocationMetadata.rover_origin_rotation_quaternian_d = quaternion[3];
									}
									else {
										position.roverModelLocationMetadata.has_site_rover_origin_offset_vector = true;
										position.roverModelLocationMetadata.rover_origin_rotation_quaternian_a = entry2.rover_origin_rotation_quaternian_a;
										position.roverModelLocationMetadata.rover_origin_rotation_quaternian_b = entry2.rover_origin_rotation_quaternian_b;
										position.roverModelLocationMetadata.rover_origin_rotation_quaternian_c = entry2.rover_origin_rotation_quaternian_c;
										position.roverModelLocationMetadata.rover_origin_rotation_quaternian_d = entry2.rover_origin_rotation_quaternian_d;									
									}
								}
							}
						}
						frames.add(position);
					}
				}				
			}
			PanPosition[] movieFrames = new PanPosition[frames.size()];
			this.movieFrames = (PanPosition[]) frames.toArray(movieFrames);
			this.movieFrameIndex = 0;
		}
		catch (Exception e) {
			UnknownExceptionDialog.openDialog(getShell(), "Error reading movie file", e);
			System.err.println(e.toString());
		}
	}
	
	void interpolateVector(double v1a, double v1b, double v1c, double v2a, double v2b, double v2c, double[] out, double d) {
		out[0] = (v2a-v1a) * d + v1a;
		out[1] = (v2b-v1b) * d + v1b;
		out[2] = (v2c-v1c) * d + v1c;		
	}
	
	// for quaternion interpolation, see http://www.euclideanspace.com/maths/algebra/realNormedAlgebra/quaternions/slerp/index.htm
	void interpolateQuaternion(double q1w, double q1x, double q1y, double q1z, double q2w, double q2x, double q2y, double q2z, double[] out, double t) {
		
		//System.out.println("interpolateQuaternion 1 "+q1w+" "+q1x+" "+q1y+" "+q1z);
		//System.out.println("interpolateQuaternion 2 "+q2w+" "+q2x+" "+q2y+" "+q2z);		
		//System.out.println("interpolateQuaternion t=="+t);
		// Calculate angle between them.
		double cosHalfTheta = q1w * q2w + q1x * q2x + q1y * q2y + q1z * q2z;
		//System.out.println("cosHalfTheta=="+cosHalfTheta);
		// if qa=qb or qa=-qb then theta = 0 and we can return qa
		if (Math.abs(cosHalfTheta) >= 1.0){
			out[0] = q1w; out[1] = q1x; out[2] = q1y; out[3] = q1z;
			return;
		}
		// Calculate temporary values.
		double halfTheta = Math.acos(cosHalfTheta);
		
		// This is a workaround because the math is not right and I can't fix it - 
		// prevents rover model from spinning around in the wrong direction, 
		// in other words turning 358 degrees instead of 2 degrees.
		if (halfTheta > Math.PI/2) {
			if (t < 0.5) {
				out[0] = q1w;
				out[1] = q1x;
				out[2] = q1y;
				out[3] = q1z;
			}
			else {
				out[0] = q2w;
				out[1] = q2x;
				out[2] = q2y;
				out[3] = q2z;
			}
			return;
		}
		
		double sinHalfTheta = Math.sqrt(1.0 - cosHalfTheta*cosHalfTheta);
		//System.out.println("halfTheta=="+halfTheta+" sinHalfTheta=="+sinHalfTheta);
		
		// if theta = 180 degrees then result is not fully defined
		// we could rotate around any axis normal to qa or qb
		if (Math.abs(sinHalfTheta) < 0.0001){
			out[0] = (q1w * 0.5 + q2w * 0.5);
			out[1] = (q1x * 0.5 + q2x * 0.5);
			out[2] = (q1y * 0.5 + q2y * 0.5);
			out[3] = (q1z * 0.5 + q2z * 0.5);
			return;
		}
		double ratioA = Math.sin((1 - t) * halfTheta) / sinHalfTheta;
		double ratioB = Math.sin(t * halfTheta) / sinHalfTheta; 
		//System.out.println("ratioA=="+ratioA+" ratioB=="+ratioB);
		//calculate Quaternion.
		out[0] = (q1w * ratioA + q2w * ratioB);
		out[1] = (q1x * ratioA + q2x * ratioB);
		out[2] = (q1y * ratioA + q2y * ratioB);
		out[3] = (q1z * ratioA + q2z * ratioB);
	}


	public LocationCounter getRoverModelLocation() {
		return roverModelLocationMetadata.location;
	}
}
