package midnightmarsbrowser.actions;

import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchWindow;

public class TakePanScreenshotAction extends BasePanoramaAction {

	public final static String ID = "midnightmarsbrowser.actions.TakePanScreenshotAction";
		
	public TakePanScreenshotAction(IWorkbenchWindow window) {
		super(window);
		setId(ID);
		setText("Take Panorama Screenshot");
		setAccelerator('t' + SWT.MOD1);
	}
	
	public void run() {
		if (currentViewer.getPanoramaCanvas() != null) {
			currentViewer.getPanoramaCanvas().takePanoramaScreenshot(false, 0);
		}
	}
}
