package midnightmarsbrowser.actions;

import midnightmarsbrowser.application.Application;
import midnightmarsbrowser.editors.PanoramaCanvas;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchWindow;

public class OpenPanMovieAction extends BasePanoramaAction {

	public final static String ID = "midnightmarsbrowser.actions.OpenPanMovieAction";
		
	static FileDialog fileDialog = null;
	
	public OpenPanMovieAction(IWorkbenchWindow window) {
		super(window);
		setId(ID);
		setText("Open Panorama Movie");
	}
	
	public void run() {
		PanoramaCanvas panoramaCanvas = currentViewer.getPanoramaCanvas();
		if (panoramaCanvas != null) {
			if (fileDialog == null) {
				fileDialog = new FileDialog(window.getShell(), SWT.OPEN); 
				fileDialog.setText("Open Panorama Movie");
				fileDialog.setFilterPath(Application.getWorkspace().getWorkspaceDir().getAbsolutePath());
		        String[] filterExt = { "*.mmb", "*.*" };
		        fileDialog.setFilterExtensions(filterExt);
			}
	        String selected = fileDialog.open();
	        panoramaCanvas.loadPanoramaMovie(selected);
		}
	}
}
