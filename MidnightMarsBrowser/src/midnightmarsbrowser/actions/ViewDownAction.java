package midnightmarsbrowser.actions;

import org.eclipse.ui.IWorkbenchWindow;

public class ViewDownAction extends BasePanoramaAction {

	public final static String ID = "midnightmarsbrowser.actions.ViewDownAction";
		
	public ViewDownAction(IWorkbenchWindow window) {
		super(window);
		setId(ID);
		setText("View Down");
		setToolTipText("View Down");
		setAccelerator('d');
	}
	
	public void run() {
		if (currentViewer.getPanoramaCanvas() != null) {
			currentViewer.getPanoramaCanvas().startMoveToAzEl(currentViewer.getPanoramaCanvas().getViewAzimuth(), -90.0f);
		}
	}
}
