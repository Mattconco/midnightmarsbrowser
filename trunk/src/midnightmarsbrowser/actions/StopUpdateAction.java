package midnightmarsbrowser.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import midnightmarsbrowser.application.Application;

public class StopUpdateAction extends Action implements IWorkbenchAction {

	private final IWorkbenchWindow window;

	public final static String ID = "midnightmarsbrowser.actions.StopUpdateAction";
		
	public StopUpdateAction(IWorkbenchWindow window) {
		this.window = window;
		setId(ID);
		setText("Stop Update");
		setToolTipText("Stop update in progress");
		setAccelerator(',' | SWT.MOD1);
//		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
//				Application.PLUGIN_ID, IImageKeys.ADD_CONTACT));
	}

	public void dispose() {
	}
	
	public void run() {
		Application.getWorkspace().stopUpdateTask();
	}
}
