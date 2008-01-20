package midnightmarsbrowser.actions;

import midnightmarsbrowser.application.Activator;
import midnightmarsbrowser.editors.MMBEditorBase;
import midnightmarsbrowser.model.ViewerSettings;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;

public class IncludeForwardHazcamAction extends BaseSlideshowAction {

	public final static String ID = "midnightmarsbrowser.actions.IncludeForwardHazcamAction";
		
	public IncludeForwardHazcamAction(IWorkbenchWindow window) {
		super(window, Action.AS_CHECK_BOX);
		setId(ID);
		setText("Include Forward Hazcam Images");
		setImageDescriptor(Activator.getImageDescriptor("icons/fhazcam.gif"));
	}

	public void run() {
		ViewerSettings settings = currentViewer.getViewerSettings();
		settings.f = isChecked();
		currentViewer.setViewerSettings(settings, true);
	}
	
	public void currentEditorChanged(MMBEditorBase newEditor) {
		super.currentEditorChanged(newEditor);
		if (currentViewer != null) {
			setChecked(currentViewer.getViewerSettings().f);
		}
	}
}
