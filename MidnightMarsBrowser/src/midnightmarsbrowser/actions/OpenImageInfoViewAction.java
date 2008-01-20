package midnightmarsbrowser.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import midnightmarsbrowser.views.ImageInfoView;

public class OpenImageInfoViewAction extends Action implements IWorkbenchAction {

	private final IWorkbenchWindow window;

	public final static String ID = "midnightmarsbrowser.actions.OpenImageInfoViewAction";
		
	public OpenImageInfoViewAction(IWorkbenchWindow window) {
		this.window = window;
		setId(ID);
		setText("Image Info");
		setToolTipText("Open \"Image Info\" view.");
//		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
//				Application.PLUGIN_ID, IImageKeys.ADD_CONTACT));
	}

	public void dispose() {
		
	}
	
	public void run() {
		try {
			window.getActivePage().showView(ImageInfoView.ID);
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
