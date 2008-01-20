package midnightmarsbrowser.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import midnightmarsbrowser.application.Application;
import midnightmarsbrowser.application.UpdateParams;
import midnightmarsbrowser.views.ImagesView;

public class CheckForNewImagesAction extends Action implements IWorkbenchAction {

	private final IWorkbenchWindow window;

	public final static String ID = "midnightmarsbrowser.actions.CheckForNewImagesAction";
		
	public CheckForNewImagesAction(IWorkbenchWindow window) {
		this.window = window;
		setId(ID);
		setText("Check for New Images");
		setToolTipText("Check for new images.");
//		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
//				Application.PLUGIN_ID, IImageKeys.ADD_CONTACT));
	}

	public void dispose() {
		
	}
	
	public void run() {
		UpdateParams params = new UpdateParams();		
		params.mode = UpdateParams.MODE_CHECK;
		Application.getWorkspace().startUpdateTask(params, window);
	}
}
