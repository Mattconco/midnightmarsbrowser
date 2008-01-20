package midnightmarsbrowser.actions;

import midnightmarsbrowser.editors.PanoramaCanvas;

import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchWindow;

public class PreviousMovieFrameAction extends BasePanoramaAction {

	public final static String ID = "midnightmarsbrowser.actions.PreviousMovieFrameAction";
		
	public PreviousMovieFrameAction(IWorkbenchWindow window) {
		super(window);
		setId(ID);
		setText("Previous Movie Frame");
		setAccelerator(','+SWT.SHIFT);
	}
	
	public void run() {
		PanoramaCanvas panoramaCanvas = currentViewer.getPanoramaCanvas();
		if (panoramaCanvas != null) {
	        panoramaCanvas.previousMovieFrame();
		}
	}
}
