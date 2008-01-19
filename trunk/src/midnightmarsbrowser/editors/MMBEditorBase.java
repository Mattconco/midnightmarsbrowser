package midnightmarsbrowser.editors;

import java.util.ArrayList;
import java.util.Iterator;

import midnightmarsbrowser.model.ImageEntry;
import midnightmarsbrowser.model.TimeInterval;
import midnightmarsbrowser.model.TimeIntervalList;
import midnightmarsbrowser.model.ViewerSettings;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.part.EditorPart;

public abstract class MMBEditorBase extends EditorPart {

	public static final String VIEW_SETTINGS_CHANGED = "viewSettingsChanged";
	
	public static final String IMAGE_SELECTION_CHANGED = "imageSelectionChanged";
	
	public static final String TIME_INTERVAL_SELECTION_CHANGED = "timeIntervalSelectionChanged";
	
	public static final String DIRECTION_CHANGED = "directionChanged";
		
	private ArrayList myListeners = new ArrayList();
	
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		if (!myListeners.contains(listener)) {
			myListeners.add(listener);
		}
	}

	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		myListeners.remove(listener);
	}

	void fireViewSettingsChanged(Object oldValue, Object newValue) {
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				MMBEditorBase.VIEW_SETTINGS_CHANGED, oldValue, newValue));
	}

	void fireImageSelectionChanged(Object oldValue, Object newValue) {
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				MMBEditorBase.IMAGE_SELECTION_CHANGED, oldValue, newValue));
	}
	
	void fireLocationSelectionChanged(Object oldValue, Object newValue) {
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				MMBEditorBase.TIME_INTERVAL_SELECTION_CHANGED, oldValue, newValue));
	}
	
	public void fireDirectionChanged() {
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				MMBEditorBase.DIRECTION_CHANGED, null, null));
	}
	
	private void firePropertyChangeEvent(PropertyChangeEvent event) {
		for (Iterator iter = myListeners.iterator(); iter.hasNext();) {
			IPropertyChangeListener element = (IPropertyChangeListener) iter
					.next();
			element.propertyChange(event);
		}
	}
		
	public abstract ViewerSettings getViewerSettings();
	
	public abstract void setViewerSettings(ViewerSettings settings, boolean recomputeImageList);
	
	public abstract TimeIntervalList getTimeIntervalList();
	
	public abstract TimeInterval getSelectedTimeInterval();
	
	public abstract void setSelectedTimeInterval(TimeInterval timeInterval);
		
	public abstract ImageEntry getSelectedImage();
	
	public abstract void setSelectedImage(ImageEntry imageListEntry);
	
	public abstract void setImageEnabled(ImageEntry imageListEntry, boolean enabled);
	
	public abstract void reloadImage(ImageEntry imageListEntry);
	
}
