package midnightmarsbrowser.actions;

import midnightmarsbrowser.application.Activator;
import midnightmarsbrowser.editors.MMBEditorBase;
import midnightmarsbrowser.model.ViewerSettings;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;

public class ShowRightImagesAction extends BaseViewerAction {

	public final static String ID = "midnightmarsbrowser.actions.ShowRightImagesAction";
		
	public ShowRightImagesAction(IWorkbenchWindow window) {
		super(window, Action.AS_RADIO_BUTTON);
		setId(ID);
		setText("Show Right Camera Images");
		setImageDescriptor(Activator.getImageDescriptor("icons/right_images.gif"));
	}

	public void run() {
		ViewerSettings settings = currentViewer.getViewerSettings();
		if (isChecked()) {
			settings.left = false;
			settings.right = true;
			settings.anaglyph = false;
		}
		currentViewer.setViewerSettings(settings, true);
	}
	
	public void currentEditorChanged(MMBEditorBase newEditor) {
		super.currentEditorChanged(newEditor);
		if (currentViewer != null) {
			ViewerSettings settings = currentViewer.getViewerSettings();
			setChecked(!settings.left && settings.right && !settings.anaglyph);
		}
	}
}
