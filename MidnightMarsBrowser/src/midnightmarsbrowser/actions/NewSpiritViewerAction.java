package midnightmarsbrowser.actions;

import midnightmarsbrowser.application.Activator;
import midnightmarsbrowser.editors.MMBEditorInput;
import midnightmarsbrowser.editors.ViewerEditor;
import midnightmarsbrowser.model.MerUtils;
import midnightmarsbrowser.model.ViewerSettings;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

public class NewSpiritViewerAction extends Action implements IWorkbenchAction {

	private final IWorkbenchWindow window;

	public final static String ID = "midnightmarsbrowser.actions.NewSpiritViewerAction";
		
	public NewSpiritViewerAction(IWorkbenchWindow window) {
		this.window = window;
		setId(ID);
		setText("New Spirit Viewer");
		setImageDescriptor(Activator.getImageDescriptor("icons/new_spirit.gif"));
	}

	public void dispose() {
		
	}
	
	public void run() {
		try {
			IWorkbenchPage page = window.getActivePage();
			ViewerSettings viewerSettings = new ViewerSettings(MerUtils.ROVERCODE_SPIRIT);
			MMBEditorInput input = new MMBEditorInput(viewerSettings);
			page.openEditor(input, ViewerEditor.ID);			
		} catch (PartInitException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}	
}
