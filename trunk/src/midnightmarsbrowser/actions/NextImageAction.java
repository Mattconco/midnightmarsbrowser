package midnightmarsbrowser.actions;

import midnightmarsbrowser.application.Activator;
import midnightmarsbrowser.editors.MMBEditorBase;
import midnightmarsbrowser.model.ImageEntry;

import org.eclipse.ui.IWorkbenchWindow;

public class NextImageAction extends BaseViewerAction {

	public final static String ID = "midnightmarsbrowser.actions.NextImageAction";
		
	public NextImageAction(IWorkbenchWindow window) {
		super(window);
		setId(ID);
		setText("Next Image");
		setToolTipText("Next Image");
		setAccelerator('\'');
		setImageDescriptor(Activator.getImageDescriptor("icons/next_image.gif"));		
	}
	
	public void currentEditorChanged(MMBEditorBase newEditor) {
		super.currentEditorChanged(newEditor);
		if (currentViewer != null) {
			ImageEntry nextImage = currentViewer.getNextImage();
			setEnabled(nextImage != null);
		}
	}
	
	public void run() {
		ImageEntry nextImage = currentViewer.getNextImage();
		if (nextImage != null) {
			currentViewer.setSelectedImage(nextImage);
		}
	}
}
