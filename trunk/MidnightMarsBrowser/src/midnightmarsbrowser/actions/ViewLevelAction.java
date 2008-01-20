package midnightmarsbrowser.actions;

import org.eclipse.ui.IWorkbenchWindow;
import midnightmarsbrowser.editors.ViewerEditor;

public class ViewLevelAction extends BasePanoramaAction {

	public final static String ID = "midnightmarsbrowser.actions.ViewLevelAction";
		
	public ViewLevelAction(IWorkbenchWindow window) {
		super(window);
		setId(ID);
		setText("View Level");
		setToolTipText("View Level");
		setAccelerator('l');
	}
	
	public void run() {
		if (currentViewer.getPanoramaCanvas() != null) {
			currentViewer.getPanoramaCanvas().startMoveToAzEl(currentViewer.getPanoramaCanvas().getViewAzimuth(), 0.0f);
		}
	}
}
