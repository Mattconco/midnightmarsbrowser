package midnightmarsbrowser.actions;

import midnightmarsbrowser.application.Activator;
import midnightmarsbrowser.editors.MMBEditorBase;
import midnightmarsbrowser.model.ViewerSettings;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;

public class IncludeSolarFilterImagesAction extends BaseViewerAction {

	public final static String ID = "midnightmarsbrowser.actions.ShowSolarFilterImagesAction";
		
	public IncludeSolarFilterImagesAction(IWorkbenchWindow window) {
		super(window, Action.AS_CHECK_BOX);
		setId(ID);
		setText("Include Sun Filter Images");
		setImageDescriptor(Activator.getImageDescriptor("icons/solar_filter_images.gif"));
	}

	public void run() {
		ViewerSettings settings = currentViewer.getViewerSettings();
		settings.excludeSolarFilter = !isChecked();
		currentViewer.setViewerSettings(settings, true);
	}
	
	public void currentEditorChanged(MMBEditorBase newEditor) {
		super.currentEditorChanged(newEditor);
		if (currentViewer != null) {
			ViewerSettings settings = currentViewer.getViewerSettings();
			setChecked(!settings.excludeSolarFilter);
		}
	}
}
