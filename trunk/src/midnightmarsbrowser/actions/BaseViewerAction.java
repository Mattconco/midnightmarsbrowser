package midnightmarsbrowser.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import midnightmarsbrowser.editors.ViewerEditor;
import midnightmarsbrowser.editors.MMBEditorBase;

public abstract class BaseViewerAction extends Action implements IWorkbenchAction, IPartListener, IPropertyChangeListener {

	protected final IWorkbenchWindow window;
	
	ViewerEditor currentViewer = null;
	
	MMBEditorBase currentEditor = null;
	
	public BaseViewerAction(IWorkbenchWindow window) {
		this(window, Action.AS_PUSH_BUTTON);
	}
	
	public BaseViewerAction(IWorkbenchWindow window, int style) {
		super(null, style);
		this.window = window;
		window.getPartService().addPartListener(this);
		setEnabled(false);
	}
	
	public void dispose() {
		window.getPartService().removePartListener(this);
	}
	
	public boolean isMyEditor(MMBEditorBase newEditor) {
		return (newEditor != null && newEditor instanceof ViewerEditor);
	}
	
	public void currentEditorChanged(MMBEditorBase newEditor) {
		if (isMyEditor(newEditor)) {
			currentViewer = (ViewerEditor)newEditor;
			setEnabled(true);
		}
		else {
			currentViewer = null;
			setEnabled(false);
		}
	}
	
	public void partActivated(IWorkbenchPart part) {
		if (part instanceof MMBEditorBase) {
			if (currentEditor != null) {
				currentEditor.removePropertyChangeListener(this);
				currentEditor = null;
			}
			currentEditor = (MMBEditorBase) part;
			currentEditor.addPropertyChangeListener(this);
			currentEditorChanged(currentEditor);
		}
	}
	
	public void partBroughtToTop(IWorkbenchPart part) {
	}

	public void partClosed(IWorkbenchPart part) {
		try {
			if (part == currentEditor) {
				currentEditor.removePropertyChangeListener(this);
				currentEditor = null;
				currentEditorChanged(currentEditor);
			}
		}
		catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public void partDeactivated(IWorkbenchPart part) {
	}

	public void partOpened(IWorkbenchPart part) {
	}
	
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(MMBEditorBase.IMAGE_SELECTION_CHANGED)) {
			currentEditorChanged(currentEditor);
		}
		else if (event.getProperty().equals(MMBEditorBase.TIME_INTERVAL_SELECTION_CHANGED)) {
			currentEditorChanged(currentEditor);
		}
		else if (event.getProperty().equals(MMBEditorBase.VIEW_SETTINGS_CHANGED)) {
			currentEditorChanged(currentEditor);
		}
		else if (event.getProperty().equals(MMBEditorBase.DIRECTION_CHANGED)) {
		}
	}
}
