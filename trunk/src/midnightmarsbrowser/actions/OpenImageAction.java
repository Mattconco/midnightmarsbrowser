package midnightmarsbrowser.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import midnightmarsbrowser.application.Application;
import midnightmarsbrowser.editors.ImageEditor;
import midnightmarsbrowser.editors.ImageEditorInput;
import midnightmarsbrowser.editors.ViewerEditor;
import midnightmarsbrowser.editors.MMBEditorInput;
import midnightmarsbrowser.model.ViewerSettings;
import midnightmarsbrowser.views.ImagesView;
import midnightmarsbrowser.views.ViewerSettingsView;

public class OpenImageAction extends Action implements IWorkbenchAction {

	static FileDialog fileDialog = null;
	
	private final IWorkbenchWindow window;

	public final static String ID = "midnightmarsbrowser.actions.OpenImageAction";
		
	public OpenImageAction(IWorkbenchWindow window) {
		this.window = window;
		setId(ID);
		setText("Open Image");
		setToolTipText("Open an image in a new window");
//		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
//				Application.PLUGIN_ID, IImageKeys.ADD_CONTACT));
	}

	public void dispose() {
		
	}
	
	public void run() {
		try {
			if (fileDialog == null) {
				fileDialog = new FileDialog(window.getShell(), SWT.OPEN); 
				fileDialog.setText("Open Image");
				fileDialog.setFilterPath(Application.getWorkspace().getWorkspaceDir().getAbsolutePath());
		        String[] filterExt = { "*.jpg", "*.png", "*.img" };
		        fileDialog.setFilterExtensions(filterExt);
			}
	        String pathname = fileDialog.open();
			if (pathname != null) {
				IWorkbenchPage page = window.getActivePage();
				ImageEditorInput input = new ImageEditorInput(pathname);
				page.openEditor(input, ImageEditor.ID);
			}
		} catch (PartInitException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}	
}
