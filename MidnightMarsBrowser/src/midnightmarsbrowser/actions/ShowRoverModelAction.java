package midnightmarsbrowser.actions;

import midnightmarsbrowser.application.Activator;
import midnightmarsbrowser.editors.MMBEditorBase;
import midnightmarsbrowser.model.ViewerSettings;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;

public class ShowRoverModelAction extends BasePanoramaAction {

	public final static String ID = "midnightmarsbrowser.actions.RoverModelAction";
	
	public ShowRoverModelAction(IWorkbenchWindow window) {
		super(window, Action.AS_CHECK_BOX);
		setId(ID);
		setText("Show Rover Model");
		setAccelerator('r');
		setImageDescriptor(Activator.getImageDescriptor("icons/rover_model.gif"));
	}

	public void run() {
		ViewerSettings settings = currentViewer.getViewerSettings();
		settings.panShowRoverModel = isChecked();
		currentViewer.setViewerSettings(settings, false);
	}
	
	public void currentEditorChanged(MMBEditorBase newEditor) {
		super.currentEditorChanged(newEditor);
		if (currentViewer != null) {
			setChecked(currentViewer.getViewerSettings().panShowRoverModel);
		}
	}
}
