package midnightmarsbrowser.actions;

import java.io.IOException;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import midnightmarsbrowser.application.Application;
import midnightmarsbrowser.application.UpdateParams;
import midnightmarsbrowser.dialogs.UnknownExceptionDialog;
import midnightmarsbrowser.views.ImagesView;

public class SaveImageDestretchMetadataAction extends Action implements IWorkbenchAction {

	private final IWorkbenchWindow window;

	public final static String ID = "midnightmarsbrowser.actions.SaveImageDestretchMetadataAction";
		
	public SaveImageDestretchMetadataAction(IWorkbenchWindow window) {
		this.window = window;
		setId(ID);
		setText("Save Image Destretch Metadata");
		setToolTipText("Save Image Destretch Metadata");
//		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
//				Application.PLUGIN_ID, IImageKeys.ADD_CONTACT));
	}

	public void dispose() {
		
	}
	
	public void run() {
		try {
			Application.getWorkspace().getImageStretchMetadata().write(Application.getWorkspace());
		} catch (Exception e) {
			e.printStackTrace();
			UnknownExceptionDialog.openDialog(window.getShell(), "Error saving image destretch metadata!", e);
		}
	}
}
