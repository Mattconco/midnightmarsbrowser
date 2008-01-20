package midnightmarsbrowser.actions;

import midnightmarsbrowser.editors.MMBEditorBase;

import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;

public class MMBEditorChangePartListener implements IPartListener {

	private IMMBEditorChangeListener changeListener;

	private MMBEditorBase currentEditor;
	
	public MMBEditorChangePartListener(IMMBEditorChangeListener changeListener) {
		this.changeListener = changeListener;
	}
	
	public MMBEditorBase getCurrentEditor() {
		return currentEditor;
	}

	public void partActivated(IWorkbenchPart part) {
		if (part instanceof MMBEditorBase) {
			if (currentEditor != null) {
				currentEditor = null;
			}
			currentEditor = (MMBEditorBase) part;
			changeListener.currentEditorChanged(currentEditor);
		}
	}

	public void partBroughtToTop(IWorkbenchPart part) {
	}

	public void partClosed(IWorkbenchPart part) {
		if (part == currentEditor) {
			currentEditor = null;			
			changeListener.currentEditorChanged(currentEditor);
		}
	}

	public void partDeactivated(IWorkbenchPart part) {
	}

	public void partOpened(IWorkbenchPart part) {
	}

}
