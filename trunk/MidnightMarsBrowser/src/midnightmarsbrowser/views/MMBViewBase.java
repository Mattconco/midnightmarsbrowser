package midnightmarsbrowser.views;

import midnightmarsbrowser.editors.MMBEditorBase;
import midnightmarsbrowser.model.ImageEntry;
import midnightmarsbrowser.model.TimeInterval;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

public abstract class MMBViewBase extends ViewPart implements IPartListener, IPropertyChangeListener {

	protected MMBEditorBase currentEditor;
	
	private boolean inNotification = false;
	
	void notificationCurrentEditorChanged() {}
	
	void notificationImageSelectionChanged(ImageEntry newImage) {};
	
	void notificationTimeIntervalSelectionChanged(TimeInterval newTimeInterval) {};
	
	void notificationDirectionChanged() {}
	
	public boolean isInNotification() {
		return inNotification;
	}

	public void createPartControl(Composite parent) {
		IWorkbenchPage activePage = getSite().getWorkbenchWindow().getActivePage();
		if (activePage != null) {
			IEditorPart activeEditor = activePage.getActiveEditor();
			if ((activeEditor != null) && (activeEditor instanceof MMBEditorBase)) {
				currentEditor = (MMBEditorBase) activeEditor;
				currentEditor.addPropertyChangeListener(this);
			}
		}
		callCurrentEditorChanged();
		getSite().getWorkbenchWindow().getPartService().addPartListener(this);		
	}
	
	public void dispose() {
		getSite().getWorkbenchWindow().getPartService().removePartListener(this);
		if (currentEditor != null) {
			currentEditor.removePropertyChangeListener(this);
		}
		super.dispose();
	}
	
	public void partActivated(IWorkbenchPart part) {
		try {
			if (part instanceof MMBEditorBase) {
				if (currentEditor != part) {
					if (currentEditor != null) {
						currentEditor.removePropertyChangeListener(this);
						currentEditor = null;
					}
					currentEditor = (MMBEditorBase) part;
					currentEditor.addPropertyChangeListener(this);
					callCurrentEditorChanged();
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void partBroughtToTop(IWorkbenchPart part) {
	}

	public void partClosed(IWorkbenchPart part) {
		if (part == currentEditor) {
			currentEditor.removePropertyChangeListener(this);			
			currentEditor = null;			
			callCurrentEditorChanged();
		}
	}

	public void partDeactivated(IWorkbenchPart part) {
	}

	public void partOpened(IWorkbenchPart part) {
	}

	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(MMBEditorBase.IMAGE_SELECTION_CHANGED)) {
			callImageSelectionIndexChanged((ImageEntry)(event.getNewValue()));
		}
		else if (event.getProperty().equals(MMBEditorBase.TIME_INTERVAL_SELECTION_CHANGED)) {
			callTimeIntervalSelectionIndexChanged((TimeInterval)(event.getNewValue()));
		}
		else if (event.getProperty().equals(MMBEditorBase.VIEW_SETTINGS_CHANGED)) {
			// For now, currenEditorChanged() should be equivalent 
			// to "viewSettingsChanged()"
			callCurrentEditorChanged();
		}
		else if (event.getProperty().equals(MMBEditorBase.DIRECTION_CHANGED)) {
			callDirectionChanged();
		}
	}
	
	private void callCurrentEditorChanged() {
		if (!inNotification) {
			inNotification = true;
			notificationCurrentEditorChanged();
			inNotification = false;
		}
	}
	
	private void callImageSelectionIndexChanged(ImageEntry newImage) {
		if (!inNotification) {
			inNotification = true;
			notificationImageSelectionChanged(newImage);
			inNotification = false;
		}
	}
	
	private void callTimeIntervalSelectionIndexChanged(TimeInterval newTimeInterval) {
		if (!inNotification) {
			inNotification = true;
			notificationTimeIntervalSelectionChanged(newTimeInterval);
			inNotification = false;
		}
	}
	
	private void callDirectionChanged() {
		if (!inNotification) {
			inNotification = true;
			notificationDirectionChanged();
			inNotification = false;
		}
	}
	
}
