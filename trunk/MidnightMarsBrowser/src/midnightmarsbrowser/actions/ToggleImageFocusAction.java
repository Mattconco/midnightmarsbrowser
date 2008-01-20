package midnightmarsbrowser.actions;

import org.eclipse.ui.IWorkbenchWindow;

public class ToggleImageFocusAction extends BasePanoramaAction {

	public final static String ID = "midnightmarsbrowser.actions.ToggleImageFocusAction";
		
	public ToggleImageFocusAction(IWorkbenchWindow window) {
		super(window);
		setId(ID);
		setText("Toggle image focus");
		setToolTipText("Toggle image focus");
		setAccelerator('f');
	}

	public void run() {
		if (currentViewer.getPanoramaCanvas() != null) {
			currentViewer.getPanoramaCanvas().setFocusMode(!currentViewer.getPanoramaCanvas().isFocusMode());
		}
	}
}
