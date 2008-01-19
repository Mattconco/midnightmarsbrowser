package midnightmarsbrowser.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import midnightmarsbrowser.application.Activator;
import midnightmarsbrowser.views.ViewerSettingsView;

public class ShowViewerSettingsViewAction extends Action implements IWorkbenchAction {

	private final IWorkbenchWindow window;

	public final static String ID = "midnightmarsbrowser.actions.OpenViewerSettingsViewAction";
		
	public ShowViewerSettingsViewAction(IWorkbenchWindow window) {
		this.window = window;
		setId(ID);
		setText("Viewer Settings");
		setToolTipText("Show Viewer Settings");
		setImageDescriptor(Activator.getImageDescriptor("icons/viewer_settings.gif"));
	}

	public void dispose() {
		
	}
	
	public void run() {
		try {
			IWorkbenchPart part = window.getActivePage().getActivePart();
			if (part != null && part instanceof ViewerSettingsView) {
				window.getActivePage().hideView((IViewPart)part);
			}
			else {
				window.getActivePage().showView(ViewerSettingsView.ID);
			}
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
