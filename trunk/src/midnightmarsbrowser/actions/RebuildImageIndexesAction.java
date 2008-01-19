package midnightmarsbrowser.actions;

import java.lang.reflect.InvocationTargetException;

import midnightmarsbrowser.application.Application;
import midnightmarsbrowser.application.UpdateParams;
import midnightmarsbrowser.dialogs.UnknownExceptionDialog;
import midnightmarsbrowser.dialogs.UpdateDialog;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

public class RebuildImageIndexesAction extends Action implements IWorkbenchAction {

	private final IWorkbenchWindow window;

	private final static String ID = "midnightmarsbrowser.actions.RebuildImageIndexesAction";
		
	public RebuildImageIndexesAction(IWorkbenchWindow window) {
		this.window = window;
		setId(ID);
		setText("Rebuild Image Indexes...");
//		setToolTipText("Change workspaces.");
//		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
//				Application.PLUGIN_ID, IImageKeys.ADD_CONTACT));
	}

	public void dispose() {
		                                                                                                                                                                                                           
	}
	
	public void run() {
//		UpdateDialog dialog = new UpdateDialog(window.getShell());
//		if (dialog.open() == Window.OK) {
//			UpdateParams params = dialog.getParams();
//			Application.getWorkspace().startUpdateTask(params, window);
//		}
		
		try {
			boolean okay = MessageDialog.openConfirm(window.getShell(), "Rebuild Image Indexes", 
					"Rebuilding image indexes is necessary if the images on your hard drive\n"
					+"get out of sync with the image indexes that Midnight Mars Browser maintains.\n"
					+"Rebuilding indexes can take awhile if you have many images downloaded.\n"
					+"Do you want to rebuild image indexes now?");
			if (okay) {
				ProgressMonitorDialog progress = new ProgressMonitorDialog(window.getShell());
				progress.run(true, false, new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						try {
							monitor.beginTask("Rebuilding Image Indexes", IProgressMonitor.UNKNOWN);
							Application.getWorkspace().getImageIndex().rebuildImageIndexes();
							monitor.done();
						} catch (Exception e) {
							throw new InvocationTargetException(e);
						}
					}});
			}
		} catch (Exception e) {
			UnknownExceptionDialog.openDialog(window.getShell(), e.toString(), e);			
		}
	}
}
