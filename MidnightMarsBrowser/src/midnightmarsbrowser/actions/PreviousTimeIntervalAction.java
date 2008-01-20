package midnightmarsbrowser.actions;

import midnightmarsbrowser.application.Activator;
import midnightmarsbrowser.editors.MMBEditorBase;
import midnightmarsbrowser.model.TimeInterval;

import org.eclipse.ui.IWorkbenchWindow;

public class PreviousTimeIntervalAction extends BaseViewerAction {

	public final static String ID = "midnightmarsbrowser.actions.PreviousTimeIntervalAction";

	public PreviousTimeIntervalAction(IWorkbenchWindow window) {
		super(window);

		setId(ID);
		setText("Previous Time Interval");
		setToolTipText("Previous Time Interval");
		setAccelerator('[');
		setImageDescriptor(Activator.getImageDescriptor("icons/prev_time.gif"));
	}
	
	public void currentEditorChanged(MMBEditorBase newEditor) {
		super.currentEditorChanged(newEditor);
		if (currentViewer != null) {
			TimeInterval nextTimeInterval = currentViewer.getPreviousTimeInterval();
			setEnabled(nextTimeInterval != null);
		}
	}
	
	public void run() {
		TimeInterval nextTimeInterval = currentViewer.getPreviousTimeInterval();
		if (nextTimeInterval != null) {
			currentViewer.setSelectedTimeInterval(nextTimeInterval);
		}
	}
}
