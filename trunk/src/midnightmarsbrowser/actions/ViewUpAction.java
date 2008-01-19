package midnightmarsbrowser.actions;

import org.eclipse.ui.IWorkbenchWindow;

public class ViewUpAction extends BasePanoramaAction {

	public final static String ID = "midnightmarsbrowser.actions.ViewUpAction";
		
	public ViewUpAction(IWorkbenchWindow window) {
		super(window);
		setId(ID);
		setText("View Up");
		setToolTipText("View Up");
		setAccelerator('u');
	}
	
	public void run() {
		if (currentViewer.getPanoramaCanvas() != null) {
			currentViewer.getPanoramaCanvas().startMoveToAzEl(currentViewer.getPanoramaCanvas().getViewAzimuth(), 90.0f);
		}
	}
}
