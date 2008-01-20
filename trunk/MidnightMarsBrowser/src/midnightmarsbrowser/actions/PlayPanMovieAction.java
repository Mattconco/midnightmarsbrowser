package midnightmarsbrowser.actions;

import midnightmarsbrowser.editors.PanoramaCanvas;

import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchWindow;

public class PlayPanMovieAction extends BasePanoramaAction {

	public final static String ID = "midnightmarsbrowser.actions.PlayPanMovieAction";
		
	static FileDialog fileDialog = null;
	
	public PlayPanMovieAction(IWorkbenchWindow window) {
		super(window);
		setId(ID);
		setText("Play Panorama Movie");
	}
	
	public void run() {
		PanoramaCanvas panoramaCanvas = currentViewer.getPanoramaCanvas();
		if (panoramaCanvas != null) {
	        panoramaCanvas.playMovie();
		}
	}
}
