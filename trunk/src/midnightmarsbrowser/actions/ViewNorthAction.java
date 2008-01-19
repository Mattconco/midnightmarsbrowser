package midnightmarsbrowser.actions;

import org.eclipse.ui.IWorkbenchWindow;

public class ViewNorthAction extends BasePanoramaAction {
	
	public final static String ID = "midnightmarsbrowser.actions.ViewNorthAction";
	
	public ViewNorthAction(IWorkbenchWindow window) {
		super(window);
		setId(ID);
		setText("View North");
		setToolTipText("View North");
		setAccelerator('n');
	}

	public void run() {
		if (currentViewer.getPanoramaCanvas() != null) {
			currentViewer.getPanoramaCanvas().startMoveToAzEl(0.0f, 0.0f);
		}
	}
}
