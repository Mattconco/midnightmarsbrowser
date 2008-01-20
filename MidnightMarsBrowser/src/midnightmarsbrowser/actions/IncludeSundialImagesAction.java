package midnightmarsbrowser.actions;

import midnightmarsbrowser.application.Activator;
import midnightmarsbrowser.editors.MMBEditorBase;
import midnightmarsbrowser.model.ViewerSettings;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;

public class IncludeSundialImagesAction extends BaseViewerAction {

	public final static String ID = "midnightmarsbrowser.actions.ShowSundialImagesAction";
		
	public IncludeSundialImagesAction(IWorkbenchWindow window) {
		super(window, Action.AS_CHECK_BOX);
		setId(ID);
		setText("Include Sundial Images");
		setImageDescriptor(Activator.getImageDescriptor("icons/sundial_images.gif"));
	}

	public void run() {
		ViewerSettings settings = currentViewer.getViewerSettings();
		settings.excludeSundial = !isChecked();
		currentViewer.setViewerSettings(settings, true);
	}
	
	public void currentEditorChanged(MMBEditorBase newEditor) {
		super.currentEditorChanged(newEditor);
		if (currentViewer != null) {
			ViewerSettings settings = currentViewer.getViewerSettings();
			setChecked(!settings.excludeSundial);
		}
	}
}
