package midnightmarsbrowser.actions;

import org.eclipse.ui.IWorkbenchWindow;

public class ViewSouthAction extends BasePanoramaAction {

	public final static String ID = "midnightmarsbrowser.actions.ViewSouthAction";
		
	public ViewSouthAction(IWorkbenchWindow window) {
		super(window);
		setId(ID);
		setText("View South");
		setToolTipText("View South");
		setAccelerator('s');
	}
	
	public void run() {
		if (currentViewer.getPanoramaCanvas() != null) {
			currentViewer.getPanoramaCanvas().startMoveToAzEl(180.0f, 0.0f);
		}
	}
}
