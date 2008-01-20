package midnightmarsbrowser.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import midnightmarsbrowser.views.TimeIntervalsView;
import midnightmarsbrowser.views.UpdateConsoleView;

public class OpenUpdateConsoleViewAction extends Action implements IWorkbenchAction {

	private final IWorkbenchWindow window;

	public final static String ID = "midnightmarsbrowser.actions.OpenUpdateConsoleViewAction";
		
	public OpenUpdateConsoleViewAction(IWorkbenchWindow window) {
		this.window = window;
		setId(ID);
		setText("Update Console");
		setToolTipText("Open Update Console view.");
//		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
//				Application.PLUGIN_ID, IImageKeys.ADD_CONTACT));
	}

	public void dispose() {
		
	}
	
	public void run() {
		try {
			window.getActivePage().showView(UpdateConsoleView.ID);
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
