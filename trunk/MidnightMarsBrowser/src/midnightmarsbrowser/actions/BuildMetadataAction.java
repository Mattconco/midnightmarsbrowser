package midnightmarsbrowser.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import midnightmarsbrowser.application.Application;
import midnightmarsbrowser.application.UpdateParams;

public class BuildMetadataAction extends Action implements IWorkbenchAction {

	private final IWorkbenchWindow window;

	public final static String ID = "midnightmarsbrowser.actions.BuildMetadataAction";
		
	public BuildMetadataAction(IWorkbenchWindow window) {
		this.window = window;
		setId(ID);
		setText("Build Metadata (Admin only)");
		setToolTipText("Build Metadata");
//		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
//				Application.PLUGIN_ID, IImageKeys.ADD_CONTACT));
	}

	public void dispose() {
		
	}
	
	public void run() {
		UpdateParams params = new UpdateParams();		
		params.mode = UpdateParams.MODE_BUILD_METADATA;
		Application.getWorkspace().startUpdateTask(params, window);
	}
}
