package midnightmarsbrowser.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import midnightmarsbrowser.application.Activator;
import midnightmarsbrowser.views.TimeIntervalsView;
import midnightmarsbrowser.views.ViewerSettingsView;

public class ShowTimeIntervalsViewAction extends Action implements IWorkbenchAction {

	private final IWorkbenchWindow window;

	public final static String ID = "midnightmarsbrowser.actions.OpenTimeIntervalsViewAction";
		
	public ShowTimeIntervalsViewAction(IWorkbenchWindow window) {
		this.window = window;
		setId(ID);
		setText("Time Intervals");
		setToolTipText("Show Time Intervals list");
		setImageDescriptor(Activator.getImageDescriptor("icons/time_intervals.gif"));
	}

	public void dispose() {
		
	}
	
	public void run() {
		try {
			IWorkbenchPart part = window.getActivePage().getActivePart();
			if (part != null && part instanceof TimeIntervalsView) {
				window.getActivePage().hideView((IViewPart)part);
			}
			else {
				window.getActivePage().showView(TimeIntervalsView.ID);
			}
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
