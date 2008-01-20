package midnightmarsbrowser.actions;

import midnightmarsbrowser.application.Activator;
import midnightmarsbrowser.editors.MMBEditorBase;
import midnightmarsbrowser.model.ViewerSettings;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;

public class IncludePancamAction extends BaseViewerAction {

	public final static String ID = "midnightmarsbrowser.actions.PancamAction";
		
	public IncludePancamAction(IWorkbenchWindow window) {
		super(window, Action.AS_CHECK_BOX);
		setId(ID);
		setText("Include Pancam Images");
		setImageDescriptor(Activator.getImageDescriptor("icons/pancam.gif"));
	}

	public void run() {
		ViewerSettings settings = currentViewer.getViewerSettings();
		settings.p = isChecked();
		currentViewer.setViewerSettings(settings, true);
	}
	
	public void currentEditorChanged(MMBEditorBase newEditor) {
		super.currentEditorChanged(newEditor);
		if (currentViewer != null) {
			setChecked(currentViewer.getViewerSettings().p);
		}
	}
}
