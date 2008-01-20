package midnightmarsbrowser.editors;


import midnightmarsbrowser.application.Application;
import midnightmarsbrowser.application.MMBWorkspace;
import midnightmarsbrowser.dialogs.UnknownExceptionDialog;
import midnightmarsbrowser.model.ImageEntry;
import midnightmarsbrowser.model.MerUtils;
import midnightmarsbrowser.model.TimeInterval;
import midnightmarsbrowser.model.TimeIntervalList;
import midnightmarsbrowser.model.ViewerSettings;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.opengl.GLData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

// TODO rename this to something that sounds better like MMBViewer
public class ViewerEditor extends MMBEditorBase {

	public static String ID = "midnight.mer.editors.SlideshowEditor";

	ViewerSettings settings;

	private TimeIntervalList timeIntervalList = null;
	
	private ImageEntry selectedImage = null;
	
	private TimeInterval selectedTimeInterval = null;
	
	private MMBWorkspace workspace;
	
	private Composite composite;

	private ImageCanvas imageCanvas;
	
	private PanoramaCanvas panoramaCanvas;
	
	private Canvas noImagesCanvas;
	
	private StackLayout stackLayout;

	private boolean panoramaMode;

	
	public ViewerEditor() {
		// TODO Auto-generated constructor stub
	}

	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub
	}

	public void doSaveAs() {
		// TODO Auto-generated method stub
	}

	public PanoramaCanvas getPanoramaCanvas() {
		if (panoramaMode)
			return panoramaCanvas;
		else
			return null;
	}

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
		if (input instanceof MMBEditorInput) {
			workspace = Application.getWorkspace();
		} else {
			throw new PartInitException(
					"SlideshowEditor input was not SlideshowEditorInput");
		}
	}

	public boolean isDirty() {
		return false;
	}

	public boolean isSaveAsAllowed() {
		return false;
	}

	public void createPartControl(Composite parent) {
		try {
			composite = new Canvas(parent, SWT.NONE);
			stackLayout = new StackLayout();
			composite.setLayout(stackLayout);
			
			imageCanvas = new ImageCanvas(composite);
			
			GLData data = new GLData ();
			data.doubleBuffer = true;
			data.depthSize = 32; //16;
			panoramaCanvas = new PanoramaCanvas(composite, SWT.NO_BACKGROUND, data, workspace, this);
			
			noImagesCanvas = new Canvas(composite, SWT.NONE);
			noImagesCanvas.setLayout(new GridLayout());
			Label noImagesLabel = new Label(noImagesCanvas, SWT.NONE);
			noImagesLabel.setText("No images found matching the specified parameters");
			
			MMBEditorInput slideshowInput = (MMBEditorInput) this.getEditorInput();
			setViewerSettings(slideshowInput.slideshowSettings, true);
		}
		catch (Throwable e) {
			UnknownExceptionDialog.openDialog(this.getSite().getShell(), e.toString(), e);
			e.printStackTrace();
		}
	}
	
	public void setFocus() {
		if (panoramaMode) {
			panoramaCanvas.setFocus();
		}
		else {
			imageCanvas.setFocus();
		}
	}
	
	public void dispose() {
		// TODO is this necessary?
		if (imageCanvas != null) {
			imageCanvas.dispose();
		}
		if (panoramaCanvas != null) {
			panoramaCanvas.dispose();
		}
		super.dispose();
	}

	public ViewerSettings getViewerSettings() {
		try {
			return (ViewerSettings) settings.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public void setViewerSettings(ViewerSettings settings, boolean recomputeImageList) {
		try {
			ViewerSettings oldSettings = this.settings;
			this.settings = settings;
			panoramaMode = settings.panorama;
			
			String partName = MerUtils.displayRoverNameFromRoverCode(settings.roverCode);
			if (settings.limitToSet) {
				partName = partName+ " Updated Images";
			}
			setPartName(partName);
			
			ImageEntry oldEntry = selectedImage;
			
			if ((timeIntervalList == null) || recomputeImageList) {
				timeIntervalList = TimeIntervalList.findTimeFrameList(workspace, settings, workspace.getMostRecentUpdateFilenames(settings.roverCode));
				selectedImage = null;
			}
			
			if (settings.panorama) {
				panoramaCanvas.setMaxResolution(settings.panMaxResolution);
				panoramaCanvas.setPDSBrightnessMinValue(settings.pdsBrightnessMinValue);
				panoramaCanvas.setPDSBrightnessMaxValue(settings.pdsBrightnessMaxValue);
				panoramaCanvas.setGroundRelative(settings.panGroundRelative);
				panoramaCanvas.setRoverTrackingOn(settings.panShowRoverTracking);
//				panoramaCanvas.setRoverModelOn(settings.panRoverModel);
			}
			else {
				imageCanvas.setPDSBrightnessMinValue(settings.pdsBrightnessMinValue);
				imageCanvas.setPDSBrightnessMaxValue(settings.pdsBrightnessMaxValue);
			}
			
			fireViewSettingsChanged(oldSettings, settings);
			
			// Switch between slideshow and panorama widgets if necessary	
			if (timeIntervalList != null && timeIntervalList.getLength() > 0) {
				if (panoramaMode) {
					if (stackLayout.topControl != panoramaCanvas) {
						stackLayout.topControl = panoramaCanvas;
						composite.layout();						
					}
				}
				else {
					if (stackLayout.topControl != imageCanvas) {
						stackLayout.topControl = imageCanvas;
						composite.layout();
					}
				}
			}
			else {
				if (stackLayout.topControl != noImagesCanvas) {
					stackLayout.topControl = noImagesCanvas;
					composite.layout();
				}
			}
			
			if (timeIntervalList != null && timeIntervalList.getLength() > 0) {
				if (oldEntry != null) {
					if (settings.panorama) {
						setSelectedImage(timeIntervalList.findImageEntryClosestTo(oldEntry), false);
					}
					else {
						setSelectedImage(timeIntervalList.findImageEntryClosestTo(oldEntry));
					}
				}
				else {
					// For new viewers, start at first image of last time interval.
					// Except if it's updated images mode, start at the beginning.
					if (!settings.limitToSet) {
						TimeInterval timeInterval = timeIntervalList.getEntry(timeIntervalList.getLength()-1);
						setSelectedImage(timeInterval.getImageList()[0]);
					}
					else {
						TimeInterval timeInterval = timeIntervalList.getEntry(0);
						setSelectedImage(timeInterval.getImageList()[0]);						
					}
				}
			}
			
			if (panoramaMode) {
				panoramaCanvas.redraw();
			}
		}
		catch (Throwable e) {
			UnknownExceptionDialog.openDialog(this.getSite().getShell(), e.toString(), e);
		}
	}
		
	public ImageEntry getSelectedImage() {
		return selectedImage;
	}
	
	public void setSelectedImage(ImageEntry imageEntry) {
		setSelectedImage(imageEntry, true);
	}
	
	public void setSelectedImage(ImageEntry imageEntry, boolean reveal) {
//		System.out.println("setImageSelectionIndex "+index);
		try {
			if (settings.panorama) {
				// panorama
				if (selectedImage != imageEntry) {
					ImageEntry oldImageSelection = selectedImage;
					selectedImage = imageEntry;
					fireImageSelectionChanged(oldImageSelection, selectedImage);
					
					TimeInterval newTimeInterval = null;
					if (selectedImage != null) {
						newTimeInterval = selectedImage.getParent();
					}
					if (newTimeInterval != selectedTimeInterval) {
						setSelectedTimeInterval(newTimeInterval, false);						
					}
					// if pan has a different selected image, orient toward newly selected image
					panoramaCanvas.setSelectedImageListEntry(selectedImage);
				}
				if (reveal) {
					panoramaCanvas.revealSelectedImage();
				}
			}
			else {
				// slideshow
				if (selectedImage != imageEntry) {
					ImageEntry oldImageSelection = selectedImage;
					selectedImage = imageEntry;
					if (selectedImage != null) {
						imageCanvas.loadImage(selectedImage);
					}
					fireImageSelectionChanged(oldImageSelection, selectedImage);
					TimeInterval newTimeInterval = null;
					if (selectedImage != null) {
						newTimeInterval = selectedImage.getParent();
					}
					if (newTimeInterval != selectedTimeInterval) {
						TimeInterval oldTimeInterval = selectedTimeInterval;
						selectedTimeInterval = newTimeInterval;
						fireLocationSelectionChanged(oldTimeInterval, selectedTimeInterval);
					}
				}				
			}
		}
		catch (Exception e) {
			UnknownExceptionDialog.openDialog(this.getSite().getShell(), e.toString(), e);
		}
	}

	public void setImageEnabled(ImageEntry imageListEntry, boolean enabled) {
		imageListEntry.enabled = enabled;
		if (panoramaMode) {
			panoramaCanvas.redraw();
		}
	}
	
	public TimeIntervalList getTimeIntervalList() {
		return timeIntervalList;
	}
	
	public void setSelectedTimeInterval(TimeInterval timeFrame) {
		setSelectedTimeInterval(timeFrame, true);
	}
	
	public void setSelectedTimeInterval(TimeInterval timeInterval, boolean findSelectedImage) {
		try {
			if (settings.panorama) {
				// panorama
				if (selectedTimeInterval != timeInterval) {
					TimeInterval oldTimeInterval = selectedTimeInterval;
					selectedTimeInterval = timeInterval;
					panoramaCanvas.setSelectedTimeInterval(settings.roverCode, selectedTimeInterval, 
							findSelectedImage);
					fireLocationSelectionChanged(oldTimeInterval, selectedTimeInterval);
				}
			}
			else {
				// slideshow
				if (selectedTimeInterval != timeInterval) {
					if (timeInterval != null) {
						setSelectedImage(timeInterval.getImageList()[0], true);
					}
				}
			}
		}
		catch (Exception e) {
			UnknownExceptionDialog.openDialog(this.getSite().getShell(), e.toString(), e);
		}
	}

	public TimeInterval getSelectedTimeInterval() {
		return selectedTimeInterval;
	}

	public void reloadImage(ImageEntry imageListEntry) {
		if (panoramaMode) {
			panoramaCanvas.reloadImage(imageListEntry);
		}
	}
	
	public ImageEntry getNextImage() {
		ImageEntry nextImage = null;
		if (selectedImage != null) {
			nextImage = selectedImage;
			while (true) {
				TimeInterval timeInterval = nextImage.getParent();
				int imageIndex = timeInterval.indexOf(nextImage);
				int timeIntervalIndex = timeInterval.getParent().indexOf(timeInterval);
				if (imageIndex >= 0 && imageIndex < timeInterval.getImageList().length-1) {
					nextImage = timeInterval.getImageList()[imageIndex+1];
				}
				else if (timeIntervalIndex >= 0 && timeIntervalIndex < timeInterval.getParent().getLength()-1) {
					nextImage = timeInterval.getParent().getEntry(timeIntervalIndex+1).getImageList()[0];
				}
				else {
					nextImage = null;
				}
				if (nextImage == null || nextImage.enabled) {
					return nextImage;
				}
			}
		}
		return null;
	}
	
	public ImageEntry getNextImageIgnoreEnabledStatus() {
		ImageEntry nextImage = null;
		if (selectedImage != null) {
			TimeInterval timeInterval = selectedImage.getParent();
			int imageIndex = timeInterval.indexOf(selectedImage);
			int timeIntervalIndex = timeInterval.getParent().indexOf(timeInterval);
			if (imageIndex >= 0 && imageIndex < timeInterval.getImageList().length-1) {
				nextImage = timeInterval.getImageList()[imageIndex+1];
			}
			else if (timeIntervalIndex >= 0 && timeIntervalIndex < timeInterval.getParent().getLength()-1) {
				nextImage = timeInterval.getParent().getEntry(timeIntervalIndex+1).getImageList()[0];
			}			
		}
		return nextImage;
	}
	
	public ImageEntry getPreviousImage() {
		ImageEntry nextImage = null;
		if (selectedImage != null) {
			nextImage = selectedImage;
			while (true) {
				TimeInterval timeInterval = nextImage.getParent();
				int imageIndex = timeInterval.indexOf(nextImage);
				int timeIntervalIndex = timeInterval.getParent().indexOf(timeInterval);
				if (imageIndex > 0) {
					nextImage = timeInterval.getImageList()[imageIndex-1];
				}
				else if (timeIntervalIndex > 0) {
					TimeInterval prevTimeInterval = timeInterval.getParent().getEntry(timeIntervalIndex-1);
					nextImage = prevTimeInterval.getImageList()[prevTimeInterval.getImageList().length-1];
				}
				else {
					nextImage = null;
				}
				if (nextImage == null || nextImage.enabled) {
					return nextImage;
				}
			}
		}
		return null;
	}
	
	public ImageEntry getPreviousImageIgnoreEnabledStatus() {
		ImageEntry nextImage = null;
		if (selectedImage != null) {
			TimeInterval timeInterval = selectedImage.getParent();
			int imageIndex = timeInterval.indexOf(selectedImage);
			int timeIntervalIndex = timeInterval.getParent().indexOf(timeInterval);
			if (imageIndex > 0) {
				nextImage = timeInterval.getImageList()[imageIndex-1];
			}
			else if (timeIntervalIndex > 0) {
				TimeInterval prevTimeInterval = timeInterval.getParent().getEntry(timeIntervalIndex-1);
				nextImage = prevTimeInterval.getImageList()[prevTimeInterval.getImageList().length-1];
			}
		}
		return nextImage;
	}
	
	public TimeInterval getNextTimeInterval() {
		TimeInterval nextTimeInterval = null;
		if (selectedTimeInterval != null) {
			int timeIntervalIndex = selectedTimeInterval.getParent().indexOf(selectedTimeInterval);
			if (timeIntervalIndex >= 0 && timeIntervalIndex < selectedTimeInterval.getParent().getLength()-1) {
				nextTimeInterval = selectedTimeInterval.getParent().getEntries()[timeIntervalIndex+1];				
			}
		}		
		return nextTimeInterval;
	}
	
	public TimeInterval getPreviousTimeInterval() {
		TimeInterval nextTimeInterval = null;
		if (selectedTimeInterval != null) {
			int timeIntervalIndex = selectedTimeInterval.getParent().indexOf(selectedTimeInterval);
			if (timeIntervalIndex > 0) {
				nextTimeInterval = selectedTimeInterval.getParent().getEntries()[timeIntervalIndex-1];				
			}
		}		
		return nextTimeInterval;
	}
}
