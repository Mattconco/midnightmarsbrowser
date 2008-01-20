package midnightmarsbrowser.actions;

import midnightmarsbrowser.editors.PanoramaCanvas;

import org.eclipse.ui.IWorkbenchWindow;

public class ZoomToPancamResolutionAction extends BasePanoramaAction {

	public final static String ID = "midnightmarsbrowser.actions.ZoomToPancamResolutionAction";
		
	public ZoomToPancamResolutionAction(IWorkbenchWindow window) {
		super(window);
		setId(ID);
		setText("Zoom to Pancam Resolution");
	}
	
	public void run() {
		PanoramaCanvas panoramaCanvas = currentViewer.getPanoramaCanvas();
		if (panoramaCanvas != null) {
			int height = panoramaCanvas.getSize().y;
			double newVFOV = 16.5 / 1024 * height;
			panoramaCanvas.setViewVFOV((float)newVFOV);
		}
	}
}
