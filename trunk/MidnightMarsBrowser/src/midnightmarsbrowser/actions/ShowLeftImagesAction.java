package midnightmarsbrowser.actions;

import midnightmarsbrowser.application.Activator;
import midnightmarsbrowser.editors.MMBEditorBase;
import midnightmarsbrowser.model.ViewerSettings;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;

public class ShowLeftImagesAction extends BaseViewerAction {

	public final static String ID = "midnightmarsbrowser.actions.ShowLeftImagesAction";
		
	public ShowLeftImagesAction(IWorkbenchWindow window) {
		super(window, Action.AS_RADIO_BUTTON);
		setId(ID);
		setText("Show Left Camera Images");
		setImageDescriptor(Activator.getImageDescriptor("icons/left_images.gif"));
	}

	public void run() {
		ViewerSettings settings = currentViewer.getViewerSettings();
		if (isChecked()) {
			settings.left = true;
			settings.right = false;
			settings.anaglyph = false;
		}
		currentViewer.setViewerSettings(settings, true);
	}
	
	public void currentEditorChanged(MMBEditorBase newEditor) {
		super.currentEditorChanged(newEditor);
		if (currentViewer != null) {
			ViewerSettings settings = currentViewer.getViewerSettings();
			setChecked(settings.left && !settings.right && !settings.anaglyph);
		}
	}
}
