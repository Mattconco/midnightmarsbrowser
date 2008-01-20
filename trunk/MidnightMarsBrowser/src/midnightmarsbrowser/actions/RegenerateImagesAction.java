package midnightmarsbrowser.actions;

import midnightmarsbrowser.application.Application;
import midnightmarsbrowser.application.UpdateParams;
import midnightmarsbrowser.dialogs.RegenerateImagesDialog;
import midnightmarsbrowser.dialogs.UpdateDialog;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

public class RegenerateImagesAction extends Action implements IWorkbenchAction {

	private final IWorkbenchWindow window;

	private final static String ID = "midnightmarsbrowser.actions.RegenerateImagesAction";
		
	public RegenerateImagesAction(IWorkbenchWindow window) {
		this.window = window;
		setId(ID);
		setText("Regenerate Images...");
//		setToolTipText("Change workspaces.");
//		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
//				Application.PLUGIN_ID, IImageKeys.ADD_CONTACT));
	}

	public void dispose() {
		
	}
	
	public void run() {
		RegenerateImagesDialog dialog = new RegenerateImagesDialog(window.getShell());
		if (dialog.open() == Window.OK) {
			UpdateParams params = dialog.getParams();
			Application.getWorkspace().startUpdateTask(params, window);
		}
	}
}
