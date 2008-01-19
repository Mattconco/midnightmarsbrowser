package midnightmarsbrowser.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import midnightmarsbrowser.application.Activator;
import midnightmarsbrowser.editors.ViewerEditor;
import midnightmarsbrowser.editors.MMBEditorInput;
import midnightmarsbrowser.model.MerUtils;
import midnightmarsbrowser.model.ViewerSettings;
import midnightmarsbrowser.views.ImagesView;
import midnightmarsbrowser.views.ViewerSettingsView;

public class NewOpportunityViewerAction extends Action implements IWorkbenchAction {

	private final IWorkbenchWindow window;

	public final static String ID = "midnightmarsbrowser.actions.NewOpportunityViewerAction";
		
	public NewOpportunityViewerAction(IWorkbenchWindow window) {
		this.window = window;
		setId(ID);
		setText("New Opportunity Viewer");
		setImageDescriptor(Activator.getImageDescriptor("icons/new_opportunity.gif"));
	}

	public void dispose() {
		
	}
	
	public void run() {
		try {
			IWorkbenchPage page = window.getActivePage();
			ViewerSettings viewerSettings = new ViewerSettings(MerUtils.ROVERCODE_OPPORTUNITY);
			MMBEditorInput input = new MMBEditorInput(viewerSettings);
			page.openEditor(input, ViewerEditor.ID);
		} catch (PartInitException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}	
}
