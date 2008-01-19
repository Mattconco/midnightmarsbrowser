package midnightmarsbrowser.actions;

import midnightmarsbrowser.editors.PanoramaCanvas;

import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchWindow;

public class NextMovieEndpointAction extends BasePanoramaAction {

	public final static String ID = "midnightmarsbrowser.actions.NextMovieEndpointAction";
		
	public NextMovieEndpointAction(IWorkbenchWindow window) {
		super(window);
		setId(ID);
		setText("Next Movie Endpoint");
		setAccelerator('.'+SWT.SHIFT+SWT.CTRL);
	}
	
	public void run() {
		PanoramaCanvas panoramaCanvas = currentViewer.getPanoramaCanvas();
		if (panoramaCanvas != null) {
	        panoramaCanvas.nextMovieEndpoint();
		}
	}
}
