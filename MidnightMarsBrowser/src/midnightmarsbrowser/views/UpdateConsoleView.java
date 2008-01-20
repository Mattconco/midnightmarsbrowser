package midnightmarsbrowser.views;

import midnightmarsbrowser.application.Application;
import midnightmarsbrowser.application.UpdateConsoleListener;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;

/**
 */
public class UpdateConsoleView extends MMBViewBase {
	public final static String ID = "midnightmarsbrowser.views.UpdateConsoleView";
	
	private Text text;
		
	UpdateConsoleListener listener = new UpdateConsoleListener() {
		public void write(String str) {		
			if (!text.isDisposed()) {
				text.setSelection(text.getCharCount());
				text.insert(str);
			}
		}
	};
	
	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		text = new Text(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY );
		text.setText(Application.getUpdateConsoleService().getText());
		Application.getUpdateConsoleService().addListener(listener);
		
		super.createPartControl(parent);
	}

	public void init(IViewSite site) throws PartInitException {
		// TODO Auto-generated method stub
		super.init(site);
	}

	public void dispose() {
		Application.getUpdateConsoleService().removeListener(listener);		
		super.dispose();
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		text.setFocus();
	}

	void notificationCurrentEditorChanged() {
	}
}