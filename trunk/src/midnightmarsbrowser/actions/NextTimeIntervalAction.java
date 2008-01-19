package midnightmarsbrowser.actions;

import midnightmarsbrowser.application.Activator;
import midnightmarsbrowser.editors.MMBEditorBase;
import midnightmarsbrowser.model.TimeInterval;

import org.eclipse.ui.IWorkbenchWindow;

public class NextTimeIntervalAction extends BaseViewerAction {

	public final static String ID = "midnightmarsbrowser.actions.NextTimeIntervalAction";
		
	public NextTimeIntervalAction(IWorkbenchWindow window) {
		super(window);

		setId(ID);
		setText("Next Time Interval");
		setToolTipText("Next Time Interval");
		setAccelerator(']');
		setImageDescriptor(Activator.getImageDescriptor("icons/next_time.gif"));
	}
	
	public void currentEditorChanged(MMBEditorBase newEditor) {
		super.currentEditorChanged(newEditor);
		if (currentViewer != null) {
			TimeInterval nextTimeInterval = currentViewer.getNextTimeInterval();
			setEnabled(nextTimeInterval != null);
		}
	}
	
	public void run() {
		TimeInterval nextTimeInterval = currentViewer.getNextTimeInterval();
		if (nextTimeInterval != null) {
			currentViewer.setSelectedTimeInterval(nextTimeInterval);
		}
	}
}
