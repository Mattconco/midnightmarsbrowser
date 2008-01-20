package midnightmarsbrowser.actions;

import org.eclipse.ui.IWorkbenchWindow;

public class ViewEastAction extends BasePanoramaAction {

	public final static String ID = "midnightmarsbrowser.actions.ViewEastAction";
		
	public ViewEastAction(IWorkbenchWindow window) {
		super(window);
		setId(ID);
		setText("View East");
		setToolTipText("View East");
		setAccelerator('e');
	}
	
	public void run() {
		if (currentViewer.getPanoramaCanvas() != null) {
			currentViewer.getPanoramaCanvas().startMoveToAzEl(90.0f, 0.0f);
		}
	}
}
