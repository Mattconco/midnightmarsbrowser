package midnightmarsbrowser.actions;

import midnightmarsbrowser.application.Activator;
import midnightmarsbrowser.editors.MMBEditorBase;
import midnightmarsbrowser.model.ViewerSettings;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;

public class IncludeNavcamAction extends BaseViewerAction {

	public final static String ID = "midnightmarsbrowser.actions.NavcamAction";
		
	public IncludeNavcamAction(IWorkbenchWindow window) {
		super(window, Action.AS_CHECK_BOX);
		setId(ID);
		setText("Include Navcam Images");
		setImageDescriptor(Activator.getImageDescriptor("icons/navcam.gif"));
	}

	public void run() {
		ViewerSettings settings = currentViewer.getViewerSettings();
		settings.n = isChecked();
		currentViewer.setViewerSettings(settings, true);
	}
	
	public void currentEditorChanged(MMBEditorBase newEditor) {
		super.currentEditorChanged(newEditor);
		if (currentViewer != null) {
			setChecked(currentViewer.getViewerSettings().n);
		}
	}
}
