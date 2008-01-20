package midnightmarsbrowser.actions;

import midnightmarsbrowser.application.Application;
import midnightmarsbrowser.application.UpdateParams;
import midnightmarsbrowser.dialogs.UpdateDialog;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

public class AdvancedUpdateImagesAction extends Action implements IWorkbenchAction {

	private final IWorkbenchWindow window;

	private final static String ID = "midnightmarsbrowser.actions.AdvancedUpdateImagesAction";
		
	public AdvancedUpdateImagesAction(IWorkbenchWindow window) {
		this.window = window;
		setId(ID);
		setText("Advanced Update Images...");
//		setToolTipText("Change workspaces.");
//		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
//				Application.PLUGIN_ID, IImageKeys.ADD_CONTACT));
	}

	public void dispose() {
		
	}
	
	public void run() {
		UpdateDialog dialog = new UpdateDialog(window.getShell());
		if (dialog.open() == Window.OK) {
			UpdateParams params = dialog.getParams();
			Application.getWorkspace().startUpdateTask(params, window);
		}
	}
}
