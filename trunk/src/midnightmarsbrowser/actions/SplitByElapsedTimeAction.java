package midnightmarsbrowser.actions;

import midnightmarsbrowser.application.Activator;
import midnightmarsbrowser.editors.MMBEditorBase;
import midnightmarsbrowser.model.ViewerSettings;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;

public class SplitByElapsedTimeAction extends BaseViewerAction {

	public final static String ID = "midnightmarsbrowser.actions.SplitByElapsedTimeAction";
		
	public SplitByElapsedTimeAction(IWorkbenchWindow window) {
		super(window, Action.AS_CHECK_BOX);
		setId(ID);
		setText("Split Time Intervals By Elapsed Time");
		setImageDescriptor(Activator.getImageDescriptor("icons/split_by_minutes.gif"));
	}

	public void run() {
		ViewerSettings settings = currentViewer.getViewerSettings();
		settings.splitByElapsedTime = isChecked();
		currentViewer.setViewerSettings(settings, true);
	}
	
	public void currentEditorChanged(MMBEditorBase newEditor) {
		super.currentEditorChanged(newEditor);
		if (currentViewer != null) {
			ViewerSettings settings = currentViewer.getViewerSettings();
			setChecked(settings.splitByElapsedTime);
		}
	}
}
