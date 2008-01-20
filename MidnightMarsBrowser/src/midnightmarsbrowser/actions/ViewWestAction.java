package midnightmarsbrowser.actions;

import org.eclipse.ui.IWorkbenchWindow;

public class ViewWestAction extends BasePanoramaAction {

	public final static String ID = "midnightmarsbrowser.actions.ViewWestAction";
		
	public ViewWestAction(IWorkbenchWindow window) {
		super(window);
		setId(ID);
		setText("View West");
		setToolTipText("View West");
		setAccelerator('w');
	}

	public void run() {
		if (currentViewer.getPanoramaCanvas() != null) {
			currentViewer.getPanoramaCanvas().startMoveToAzEl(-90.0f, 0.0f);
		}
	}
}
