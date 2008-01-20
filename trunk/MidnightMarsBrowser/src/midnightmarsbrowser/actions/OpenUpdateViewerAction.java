package midnightmarsbrowser.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import midnightmarsbrowser.editors.UpdateViewerEditor;
import midnightmarsbrowser.editors.MMBEditorInput;

public class OpenUpdateViewerAction extends Action implements IWorkbenchAction {

	private final IWorkbenchWindow window;

	public final static String ID = "midnightmarsbrowser.actions.OpenUpdateViewerAction";
		
	public OpenUpdateViewerAction(IWorkbenchWindow window) {
		this.window = window;
		setId(ID);
		setText("Update Viewer");
		setToolTipText("Open Update Viewer");
//		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
//				Application.PLUGIN_ID, IImageKeys.ADD_CONTACT));
	}

	public void dispose() {
		
	}
	
	public void run() {
		try {
			IWorkbenchPage page = window.getActivePage();
			MMBEditorInput input = new MMBEditorInput(null);
			page.openEditor(input, UpdateViewerEditor.ID);			
		} catch (PartInitException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}	
}
