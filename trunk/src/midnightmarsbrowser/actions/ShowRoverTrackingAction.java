package midnightmarsbrowser.actions;

import midnightmarsbrowser.application.Activator;
import midnightmarsbrowser.editors.MMBEditorBase;
import midnightmarsbrowser.model.ViewerSettings;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;

public class ShowRoverTrackingAction extends BasePanoramaAction {

	public final static String ID = "midnightmarsbrowser.actions.RoverTrackingAction";
		
	public ShowRoverTrackingAction(IWorkbenchWindow window) {
		super(window, Action.AS_CHECK_BOX);
		setId(ID);
		setText("Show Rover Tracking");
		setAccelerator('h');
		setImageDescriptor(Activator.getImageDescriptor("icons/rover_tracking.gif"));
	}

	public void run() {
		ViewerSettings settings = currentViewer.getViewerSettings();
		settings.panShowRoverTracking = isChecked();
		currentViewer.setViewerSettings(settings, false);
	}
	
	public void currentEditorChanged(MMBEditorBase newEditor) {
		super.currentEditorChanged(newEditor);
		if (currentViewer != null) {
			setChecked(currentViewer.getViewerSettings().panShowRoverTracking);
		}
	}
}
