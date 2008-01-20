package midnightmarsbrowser.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import midnightmarsbrowser.application.Activator;
import midnightmarsbrowser.application.Application;
import midnightmarsbrowser.application.UpdateParams;

public class GetLatestImagesFromExploratoriumAction extends Action implements IWorkbenchAction {

	private final IWorkbenchWindow window;

	public final static String ID = "midnightmarsbrowser.actions.GetLatestImagesFromExploratoriumAction";
		
	public GetLatestImagesFromExploratoriumAction(IWorkbenchWindow window) {
		this.window = window;
		setId(ID);
		setText("Get Latest Images from Exploratorium");
		setToolTipText("Download latest images via the Exploratorium site.");
		setAccelerator('u' + SWT.MOD1);
		setImageDescriptor(Activator.getImageDescriptor("icons/radio_telescope.gif"));
	}

	public void dispose() {
		
	}
	
	public void run() {
		UpdateParams params = new UpdateParams();
		params.mode = UpdateParams.MODE_CHECK;
		params.autoUpdate = true;
		params.checkExpl = true;
		params.checkJPL = false;
		Application.getWorkspace().startUpdateTask(params, window);
	}
}
