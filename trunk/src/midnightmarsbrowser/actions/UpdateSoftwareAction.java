package midnightmarsbrowser.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.update.ui.UpdateJob;
import org.eclipse.update.ui.UpdateManagerUI;

public class UpdateSoftwareAction extends Action implements IWorkbenchAction {

	private IWorkbenchWindow window;

	public static final String ID = "midnightmarsbrowser.actions.UpdateSoftwareAction";

	public UpdateSoftwareAction(IWorkbenchWindow window) {
		this.window = window;
		setId(ID);
		setText("Update Software");
		setToolTipText("Search for updates to Midnight Mars Browser");
		// setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
		// Application.PLUGIN_ID, IImageKeys.ADD_CONTACT));
	}

	public void run() {
		try {
			BusyIndicator.showWhile(window.getShell().getDisplay(), new Runnable() {
				public void run() {
					UpdateJob job = new UpdateJob("Searching for software updates",
							false, false);
					UpdateManagerUI.openInstaller(window.getShell(), job);
				}
			});
		}
		catch (Exception e) {
			// TODO
			e.printStackTrace();
		}
	}

	public void dispose() {

	}

}
