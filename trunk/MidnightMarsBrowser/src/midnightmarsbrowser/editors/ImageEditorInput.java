package midnightmarsbrowser.editors;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

/**
 * I have no idea...
 *
 */
public class ImageEditorInput implements IEditorInput {

	String pathname = null;
	
	public ImageEditorInput(String pathname) {
		this.pathname = pathname;
	}
	
	public String getPathname() {
		return pathname;
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
