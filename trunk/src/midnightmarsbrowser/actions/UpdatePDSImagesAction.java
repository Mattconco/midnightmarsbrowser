package midnightmarsbrowser.actions;

import midnightmarsbrowser.application.Application;
import midnightmarsbrowser.application.UpdateParams;
import midnightmarsbrowser.dialogs.UpdateDialog;
import midnightmarsbrowser.dialogs.UpdatePDSImagesDialog;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

public class UpdatePDSImagesAction extends Action implements IWorkbenchAction {

	private final IWorkbenchWindow window;

	private final static String ID = "midnightmarsbrowser.actions.UpdatePDSImagesAction";
		
	public UpdatePDSImagesAction(IWorkbenchWindow window) {
		this.window = window;
		setId(ID);
		setText("Update PDS Images...");
//		setToolTipText("Change workspaces.");
//		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
//				Application.PLUGIN_ID, IImageKeys.ADD_CONTACT));
	}

	public void dispose() {
		
	}
	
	public void run() {
		try {
			UpdatePDSImagesDialog dialog = new UpdatePDSImagesDialog(window.getShell());
			if (dialog.open() == Window.OK) {
				UpdateParams params = dialog.getParams();
				Application.getWorkspace().startUpdateTask(params, window);
			}
		}
		catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
