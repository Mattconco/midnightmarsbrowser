package midnightmarsbrowser.actions;

import org.eclipse.ui.IWorkbenchWindow;

public class TestAction extends BasePanoramaAction {

	public final static String ID = "midnightmarsbrowser.actions.TestAction";
		
	public TestAction(IWorkbenchWindow window) {
		super(window);
		setId(ID);
		setText("Set View Size");
		setToolTipText("test.");
	}
	
	public void run() {
		if (currentViewer.getPanoramaCanvas() != null) {
//			editor.getPanoramaCanvas().setSize(1024,576);
//			editor.getPanoramaCanvas().setSize(320,240);
			
//			editor.getPanoramaCanvas().setSize(720,480);
			currentViewer.getPanoramaCanvas().setSize(640,480);
//			editor.getPanoramaCanvas().setSize(960,720);			
//			editor.getPanoramaCanvas().setSize(1024,768);
		}
	}
}
