package midnightmarsbrowser.application;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import midnightmarsbrowser.metadata.ImageMetadata;
import midnightmarsbrowser.metadata.ImageStretchMetadata;
import midnightmarsbrowser.metadata.LocationDescriptionMetadata;
import midnightmarsbrowser.metadata.LocationMetadata;
import midnightmarsbrowser.metadata.ObservationMetadata;
import midnightmarsbrowser.metadata.SiteMetadata;
import midnightmarsbrowser.model.OldImageIndex;
import midnightmarsbrowser.model.ImageIndex;
import midnightmarsbrowser.views.UpdateConsoleView;


public class MMBWorkspace extends MMBWorkspaceProperties {
	private ImageIndex imageIndex;
	
	private HashMap mostRecentUpdateFilenames = new HashMap();
	
	private UpdateTask updateTask;
	
	private ImageMetadata imageMetadata;
	
	private ObservationMetadata observationMetadata;
	
	private ImageStretchMetadata imageStretchMetadata;
	
	private LocationMetadata locationMetadata;

	private SiteMetadata siteMetadata;

	private LocationDescriptionMetadata locationDescriptionMetadata;
	
	public static boolean workspaceExists(String pathString) {
		File path = new File(pathString);
		if (path.isDirectory()) {
			// TODO write properties file
			File propertiesFile = new File(path, PROPERTIES_FILENAME);
			if (propertiesFile.isFile()) {
				return true;
			}
		}
		return false;
	}
	
	public MMBWorkspace(String pathString) {
		super(pathString);
	}
	
	public void startWorkspace() throws Exception {
		readProperties();
		imageIndex = new ImageIndex();
		imageIndex.addImageDirectory(getRawImageDir());
		imageIndex.addImageDirectory(getGeneratedImageDir());
		imageIndex.addImageDirectory(getImgDir());
		readMetadata();
	}
	
	public void readMetadata() throws IOException {
		imageMetadata = new ImageMetadata();
		imageMetadata.read(this);
		observationMetadata = new ObservationMetadata();
		observationMetadata.read(this);
		imageStretchMetadata = new ImageStretchMetadata();
		imageStretchMetadata.read(this);
		locationMetadata = new LocationMetadata();
		locationMetadata.read(this);
		siteMetadata = new SiteMetadata();
		// The site frame data as derived from the svf files appears to be incompatible with the tracking data;
		// perhaps it is adjusted. Anyway for now do not read it, just compute based on location metadata.
		// siteMetadata.read(this);
		siteMetadata.fillInMissingSitesFrom(locationMetadata);
		locationDescriptionMetadata = new LocationDescriptionMetadata();
		locationDescriptionMetadata.read(this);
	}
	
	public synchronized void startUpdateTask(UpdateParams params, IWorkbenchWindow window) {
		if ((updateTask != null) && (updateTask.running)) {
			MessageDialog.openInformation(window.getShell(), "Update already in progress", "An update is already in progress.");
			return;
		} 
		try {
			IViewPart updateView = window.getActivePage().showView(UpdateConsoleView.ID);
			updateTask = new UpdateTask(params, this, window);
			Thread updateThread = new Thread(updateTask);
			updateThread.start();
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void stopUpdateTask() {
		if ((updateTask != null) && (updateTask.running)) {
			updateTask.stop();
		}
	}
	
	public ImageIndex getImageIndex() {
		return imageIndex;
	}

	public LocationDescriptionMetadata getLocationDescriptionMetadata() {
		return locationDescriptionMetadata;
	}
	
	public HashSet getMostRecentUpdateFilenames(String roverCode) {
		return (HashSet) mostRecentUpdateFilenames.get(roverCode);
	}
	
	public void setMostRecentUpdateFilenames(String roverCode, HashSet set) {
		mostRecentUpdateFilenames.put(roverCode, set);
	}
	
	public boolean hasSeqStart(String filename) {
		// TODO implement
		/*
		if (seqStarts == null) {
			loadBrightnesses();
		}
		return seqStarts.contains(filename);
		*/
		return false;
	}

	public ImageMetadata getImageMetadata() {
		return imageMetadata;
	}
	
	public ObservationMetadata getObservationMetadata() {
		return observationMetadata;
	}

	public ImageStretchMetadata getImageStretchMetadata() {
		return imageStretchMetadata;
	}

	public LocationMetadata getLocationMetadata() {
		return locationMetadata;
	}
	
	public SiteMetadata getSiteMetadata() {
		return siteMetadata;
	}
	
	
}
