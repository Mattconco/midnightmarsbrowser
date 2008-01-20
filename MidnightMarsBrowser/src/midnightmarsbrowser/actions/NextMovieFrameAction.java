package midnightmarsbrowser.actions;

import midnightmarsbrowser.editors.PanoramaCanvas;

import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchWindow;

public class NextMovieFrameAction extends BasePanoramaAction {

	public final static String ID = "midnightmarsbrowser.actions.NextMovieFrameAction";
		
	public NextMovieFrameAction(IWorkbenchWindow window) {
		super(window);
		setId(ID);
		setText("Next Movie Frame");
		setAccelerator('.'+SWT.SHIFT);
	}
	
	public void run() {
		PanoramaCanvas panoramaCanvas = currentViewer.getPanoramaCanvas();
		if (panoramaCanvas != null) {
	        panoramaCanvas.nextMovieFrame();
		}
	}
}
