package midnightmarsbrowser.actions;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.IWorkbenchWindow;

import midnightmarsbrowser.editors.PanoramaCanvas;
import midnightmarsbrowser.editors.ViewerEditor;
import midnightmarsbrowser.model.LocationCounter;

public class CopyPanPositionAction extends BasePanoramaAction {

	public final static String ID = "midnightmarsbrowser.actions.CopyPanPositionAction";
		
	public CopyPanPositionAction(IWorkbenchWindow window) {
		super(window);
		setId(ID);
		setText("Copy Panorama Position to Clipboard");
	}
	
	public void run() {
		PanoramaCanvas panoramaCanvas = currentViewer.getPanoramaCanvas();
		if (panoramaCanvas != null) {
			LocationCounter location = panoramaCanvas.getLocationCounter();
			StringBuffer buf = new StringBuffer();
			String sep = ",";
			buf.append("endpoint");
			buf.append(sep);
			buf.append(panoramaCanvas.getSpacecraftId());
			buf.append(sep);
			buf.append(location.site);
			buf.append(sep);
			buf.append(location.drive);
			buf.append(sep);
			buf.append(""+panoramaCanvas.getViewAzimuth());
			buf.append(sep);
			buf.append(""+panoramaCanvas.getViewElevation());
			buf.append(sep);
			buf.append(""+panoramaCanvas.getViewVFOV());
			if (currentViewer.getViewerSettings().panShowRoverModel) {
				buf.append(sep);
				buf.append(""+panoramaCanvas.getRoverModelLocation().site);				
				buf.append(sep);
				buf.append(""+panoramaCanvas.getRoverModelLocation().drive);
			}
			else {
				buf.append(sep);
				buf.append("-1");
				buf.append(sep);
				buf.append("-1");
			}
			
	        Clipboard clipboard = new Clipboard(window.getShell().getDisplay());
	        String plainText = buf.toString();
	        TextTransfer textTransfer = TextTransfer.getInstance();
	        clipboard.setContents(new String[]{plainText}, new Transfer[]{textTransfer});
	        clipboard.dispose();
		}
	}
}
