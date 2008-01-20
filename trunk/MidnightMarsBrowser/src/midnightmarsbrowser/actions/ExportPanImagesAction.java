package midnightmarsbrowser.actions;

import java.io.File;

import midnightmarsbrowser.application.Application;
import midnightmarsbrowser.application.UpdateParams;

import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.ui.IWorkbenchWindow;

public class ExportPanImagesAction extends BasePanoramaAction {

	public final static String ID = "midnightmarsbrowser.actions.ExportPanImagesAction";
		
	public ExportPanImagesAction(IWorkbenchWindow window) {
		super(window);
		setId(ID);
		setText("Export Panorama Images...");
		setToolTipText("Export Panorama Images and PTGui project");
	}
	
	public void run() {
		if (currentViewer.getPanoramaCanvas() != null) {			
			// TODO handle dialog, etc.
			DirectoryDialog directoryDialog = new DirectoryDialog(window.getShell()); 
			String lastExportDir = Application.getWorkspace().getLastPanExportDir();
			if (lastExportDir == null) {
				File file = new File(Application.getWorkspace().getWorkspaceDir(), "panexport");
				lastExportDir = file.getAbsolutePath();
			}
			directoryDialog.setFilterPath(lastExportDir);
			directoryDialog.setMessage("Select Panorama Export Directory");
			directoryDialog.setText("Export Panorama Images");
			String newDir = directoryDialog.open();
			if (newDir != null) {
				Application.getWorkspace().setLastPanExportDir(newDir);
				UpdateParams params = new UpdateParams();
				params.mode = UpdateParams.MODE_EXPORT_PAN_IMAGES;
				params.panExportImageList = currentViewer.getPanoramaCanvas().getExportList();
				params.tgtDir = new File(newDir);
				// TODO redo clean directory option
				//params.cleanMovieDir = true;
				// TODO get apply image adjustments flag from panorama
 				//params.applyImageAdjustments = adjustImages;
				params.applyImageAdjustments = true;		
				Application.getWorkspace().startUpdateTask(params, window);			
			}
		}
	}
}
