package midnightmarsbrowser.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import midnightmarsbrowser.application.Application;
import midnightmarsbrowser.application.UpdateParams;

public class GetLatestImagesFromJPLAction extends Action implements IWorkbenchAction {

	private final IWorkbenchWindow window;

	public final static String ID = "midnightmarsbrowser.actions.GetLatestImagesFromJPLAction";
		
	public GetLatestImagesFromJPLAction(IWorkbenchWindow window) {
		this.window = window;
		setId(ID);
		setText("Get Latest Images from JPL");
		setToolTipText("Download latest images via the JPL raw images site.");
		setAccelerator('u' + SWT.MOD1 + SWT.SHIFT);
		
//		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
//				Application.PLUGIN_ID, IImageKeys.ADD_CONTACT));
	}

	public void dispose() {
		
	}
	
	public void run() {
		UpdateParams params = new UpdateParams();
		params.mode = UpdateParams.MODE_CHECK;
		params.autoUpdate = true;
		params.checkExpl = false;
		params.checkJPL = true;
		Application.getWorkspace().startUpdateTask(params, window);
	}
}
