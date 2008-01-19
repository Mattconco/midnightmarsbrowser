package midnightmarsbrowser.actions;

import midnightmarsbrowser.editors.PanoramaCanvas;

import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchWindow;

public class ExportPanMovieFramesAction extends BasePanoramaAction {

	public final static String ID = "midnightmarsbrowser.actions.ExportPanMovieFramesAction";
		
	static FileDialog fileDialog = null;
	
	public ExportPanMovieFramesAction(IWorkbenchWindow window) {
		super(window);
		setId(ID);
		setText("Export Panorama Movie Frames");
	}
	
	public void run() {
		PanoramaCanvas panoramaCanvas = currentViewer.getPanoramaCanvas();
		if (panoramaCanvas != null) {
	        panoramaCanvas.exportMovieFrames();
		}
	}
}
