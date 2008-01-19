package midnightmarsbrowser.actions;

import midnightmarsbrowser.editors.PanoramaCanvas;

import org.eclipse.ui.IWorkbenchWindow;

public class ZoomToNavcamResolutionAction extends BasePanoramaAction {

	public final static String ID = "midnightmarsbrowser.actions.ZoomToNavcamResolutionAction";
		
	public ZoomToNavcamResolutionAction(IWorkbenchWindow window) {
		super(window);
		setId(ID);
		setText("Zoom to Navcam Resolution");
	}
	
	public void run() {
		PanoramaCanvas panoramaCanvas = currentViewer.getPanoramaCanvas();
		if (panoramaCanvas != null) {
			int height = panoramaCanvas.getSize().y;
			double newVFOV = (60.0) / 1024 * height;
			panoramaCanvas.setViewVFOV((float)newVFOV);
		}
	}
}
