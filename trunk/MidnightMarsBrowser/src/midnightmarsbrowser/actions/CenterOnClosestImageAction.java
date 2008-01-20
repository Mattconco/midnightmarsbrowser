package midnightmarsbrowser.actions;

import org.eclipse.ui.IWorkbenchWindow;

public class CenterOnClosestImageAction extends BasePanoramaAction {

	public final static String ID = "midnightmarsbrowser.actions.CenterOnClosestImageAction";
		
	public CenterOnClosestImageAction(IWorkbenchWindow window) {
		super(window);
		setId(ID);
		setText("Center on Closest Image");
		setAccelerator('g');
	}

	public void run() {
		if (currentViewer.getPanoramaCanvas() != null) {
			currentViewer.getPanoramaCanvas().centerOnClosestImage();
		}
	}
}
