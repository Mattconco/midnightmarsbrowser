package midnightmarsbrowser.perspectives;

import midnightmarsbrowser.views.*;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Perspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);
		layout.setFixed(false);
		
		IFolderLayout topLeft = layout.createFolder("topLeft", IPageLayout.LEFT, 0.4f,
				editorArea);
		topLeft.addView(ViewerSettingsView.ID);
		topLeft.addView(TimeIntervalsView.ID);
		topLeft.addView(ImagesView.ID);
		topLeft.addView(PanInfoView.ID);
		topLeft.addView(ImageInfoView.ID);
		topLeft.addView(TimeView.ID);		
		
		IFolderLayout bottom = layout.createFolder("bottom", IPageLayout.BOTTOM, 0.8f, editorArea);
		bottom.addView(UpdateConsoleView.ID);
	}
}
