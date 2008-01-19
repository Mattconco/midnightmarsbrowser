package midnightmarsbrowser.editors;

import midnightmarsbrowser.model.ViewerSettings;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class MMBEditorInput implements IEditorInput {

	ViewerSettings slideshowSettings;
	
	public MMBEditorInput(ViewerSettings slideshowSettings) {
		this.slideshowSettings = slideshowSettings;
	}
	
	public boolean exists() {
		return false;
	}

	public ImageDescriptor getImageDescriptor() {
		return ImageDescriptor.getMissingImageDescriptor();
	}

	public String getName() {
		return "Viewer";		// TODO ???
	}

	public IPersistableElement getPersistable() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getToolTipText() {
		// TODO Auto-generated method stub
		return "Viewer";
	}

	public Object getAdapter(Class adapter) {
		// TODO Auto-generated method stub
		return null;
	}

}
