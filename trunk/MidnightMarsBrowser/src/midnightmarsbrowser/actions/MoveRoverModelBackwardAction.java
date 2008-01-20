package midnightmarsbrowser.actions;

import org.eclipse.ui.IWorkbenchWindow;

public class MoveRoverModelBackwardAction extends BasePanoramaAction {

	public final static String ID = "midnightmarsbrowser.actions.MoveRoverModelBackwardAction";
		
	public MoveRoverModelBackwardAction(IWorkbenchWindow window) {
		super(window);
		setId(ID);
		setText("Move Rover Model Backward");
		setAccelerator(',');
	}
	
	public void run() {
		if (currentViewer.getPanoramaCanvas() != null) {
			currentViewer.getPanoramaCanvas().moveRoverModelBackward();
		}
	}
}
