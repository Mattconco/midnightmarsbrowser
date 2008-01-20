package midnightmarsbrowser.actions;

import midnightmarsbrowser.application.Activator;
import midnightmarsbrowser.views.ImagesView;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

public class ShowImagesViewAction extends Action implements IWorkbenchAction {

	private final IWorkbenchWindow window;

	public final static String ID = "midnightmarsbrowser.actions.ShowImagesViewAction";
		
	public ShowImagesViewAction(IWorkbenchWindow window) {
		this.window = window;
		setId(ID);
		setText("Images");
		setToolTipText("Show Images List");
		setImageDescriptor(Activator.getImageDescriptor("icons/images.gif"));
	}

	public void dispose() {
		
	}
	
	public void run() {
		try {
			IWorkbenchPart part = window.getActivePage().getActivePart();
			if (part != null && part instanceof ImagesView) {
				window.getActivePage().hideView((IViewPart)part);
			}
			else {
				window.getActivePage().showView(ImagesView.ID);
			}
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
