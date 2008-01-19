package midnightmarsbrowser.actions;

import midnightmarsbrowser.application.Activator;
import midnightmarsbrowser.editors.MMBEditorBase;
import midnightmarsbrowser.model.ImageEntry;

import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchWindow;

public class PreviousImageAction extends BaseViewerAction {

	public final static String ID = "midnightmarsbrowser.actions.PreviousImageAction";
		
	public PreviousImageAction(IWorkbenchWindow window) {
		super(window);
		setId(ID);
		setText("Previous Image");
		setToolTipText("Previous Image");
		setAccelerator(';');
		setImageDescriptor(Activator.getImageDescriptor("icons/previous_image.gif"));		
	}
	
	public void currentEditorChanged(MMBEditorBase newEditor) {
		super.currentEditorChanged(newEditor);
		if (currentViewer != null) {
			ImageEntry nextImage = currentViewer.getPreviousImage();
			setEnabled(nextImage != null);
		}
	}
	
	public void run() {
		ImageEntry nextImage = currentViewer.getPreviousImage();
		if (nextImage != null) {
			currentViewer.setSelectedImage(nextImage);
		}
	}
	
}
