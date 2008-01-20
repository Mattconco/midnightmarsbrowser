package midnightmarsbrowser.actions;

import org.eclipse.ui.IWorkbenchWindow;

public class MoveRoverModelForwardAction extends BasePanoramaAction {

	public final static String ID = "midnightmarsbrowser.actions.MoveRoverModelForwardAction";
		
	public MoveRoverModelForwardAction(IWorkbenchWindow window) {
		super(window);
		setId(ID);
		setText("Move Rover Model Forward");
		setAccelerator('.');
	}
	
	public void run() {
		if (currentViewer.getPanoramaCanvas() != null) {
			currentViewer.getPanoramaCanvas().moveRoverModelForward();
		}
	}
}
