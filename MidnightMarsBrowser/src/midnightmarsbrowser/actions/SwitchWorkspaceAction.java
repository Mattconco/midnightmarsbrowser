package midnightmarsbrowser.actions;

import midnightmarsbrowser.application.InstancePreferences;
import midnightmarsbrowser.dialogs.ChooseWorkspaceDialog;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

public class SwitchWorkspaceAction extends Action implements IWorkbenchAction {

	private final IWorkbenchWindow window;

	private final static String ID = "midnightmarsbrowser.actions.SwitchWorkspaceAction";
		
	public SwitchWorkspaceAction(IWorkbenchWindow window) {
		this.window = window;
		setId(ID);
		setText("Switch Workspace...");
		setToolTipText("Change workspaces.");
//		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
//				Application.PLUGIN_ID, IImageKeys.ADD_CONTACT));
	}

	public void dispose() {
		
	}
	
	public void run() {
		String lastWorkspace = InstancePreferences.getInstance().getLastWorkspace();
		ChooseWorkspaceDialog dialog = new ChooseWorkspaceDialog(null, lastWorkspace, null, false);
		if (dialog.open() == Window.OK) {
			InstancePreferences.getInstance().setLastWorkspace(dialog.getDirectory());
			InstancePreferences.getInstance().flush();			
			PlatformUI.getWorkbench().restart();
		}
	}
}
