package midnightmarsbrowser.application;

import midnightmarsbrowser.actions.*;

import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

/**
 * An action bar advisor is responsible for creating, adding, and disposing of
 * the actions added to a workbench window. Each window will be populated with
 * new actions.
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

	// Actions - important to allocate these only in makeActions, and then use
	// them
	// in the fill methods. This ensures that the actions aren't recreated
	// when fillActionBars is called with FILL_PROXY.
	private IWorkbenchAction exitAction;
	
	private IWorkbenchAction aboutAction;
	
	private NewViewerAction newViewerAction;
	private OpenImageAction openImageAction;
	private SwitchWorkspaceAction switchWorkspaceAction;
	private SaveImageDestretchMetadataAction saveImageDestretchMetadataAction;
	private ExportPanImagesAction exportPanImagesAction;
	private TakePanScreenshotAction takePanScreenshotAction;
	private OpenPanMovieAction openPanMovieAction;
	private	ExportPanMovieFramesAction 		exportPanMovieFramesAction;
	
	private CopyPanPositionAction copyPanPositionAction;
	
	private ShowImagesViewAction showImagesViewAction;
	private ShowTimeIntervalsViewAction showTimeIntervalsViewAction;
	private ShowViewerSettingsViewAction showViewerSettingsViewAction;
	private OpenTimeViewAction openTimeViewAction;	
	private OpenUpdateConsoleViewAction openUpdateViewAction;
	private OpenUpdateViewerAction openUpdateViewerAction;
	private OpenImageDestretchViewAction openImageDestretchViewAction;
	private OpenPanInfoViewAction openPanInfoViewAction;
	private OpenImageInfoViewAction openImageInfoViewAction;
	
	private GetLatestImagesFromExploratoriumAction	getLatestImagesFromExploratoriumAction;
	private GetLatestImagesFromJPLAction	getLatestImagesFromJPLAction;
	private CheckForNewImagesAction checkForNewImagesAction;
	private AdvancedUpdateImagesAction advancedImageUpdateAction;
	private UpdatePhoenixImagesAction updatePhoenixImagesAction;
	private UpdatePhoenixImagesFullAction updatePhoenixImagesFullAction;
	private UpdatePDSImagesAction updatePDSImagesAction;
	private RegenerateImagesAction 	regenerateImagesAction;
	private RebuildImageIndexesAction rebuildImageIndexesAction;
	private StopUpdateAction		stopUpdateAction;
	private UpdateMetadataAction	updateMetadataAction;
	private	TestAction				testAction;
	private BuildMetadataAction		buildMetadataAction;
	private BuildMetadata1xAction	buildMetadata1xAction;
	
	private NextImageAction			nextImageAction;
	private PreviousImageAction		previousImageAction;
	private NextTimeIntervalAction		nextTimeIntervalAction;
	private PreviousTimeIntervalAction	previousTimeIntervalAction;	
	private ViewNorthAction			viewNorthAction;
	private ViewEastAction			viewEastAction;
	private ViewSouthAction			viewSouthAction;
	private ViewWestAction			viewWestAction;	
	private ViewLevelAction			viewLevelAction;
	private ViewUpAction			viewUpAction;
	private ViewDownAction			viewDownAction;
	private CenterOnClosestImageAction	centerOnClosestImageAction;
	private ToggleImageFocusAction	toggleImageFocusAction;
	private ShowRoverTrackingAction roverTrackingAction;
	private ShowRoverModelAction roverModelAction;
	private MoveRoverModelForwardAction moveRoverModelForwardAction;
	private MoveRoverModelBackwardAction moveRoverModelBackwardAction;
	private ZoomToNavcamResolutionAction zoomToNavcamResolutionAction;
	private ZoomToPancamResolutionAction zoomToPancamResolutionAction;
	private	PlayPanMovieAction 		playPanMovieAction;
	private NextMovieFrameAction	nextMovieFrameAction;
	private PreviousMovieFrameAction	previousMovieFrameAction;
	private NextMovieEndpointAction nextMovieEndpointAction;
	private PreviousMovieEndpointAction previousMovieEndpointAction;
	
	private IWorkbenchAction helpAction;
	private IWorkbenchAction updateSoftwareAction;

	private NewSpiritViewerAction newSpiritViewerAction;
	private NewOpportunityViewerAction newOpportunityViewerAction;
	
	private PanoramaModeAction panoramaModeAction;
	
	private IncludeForwardHazcamAction includeForwardHazcamAction;
	private IncludeMicroAction includeMicroAction;
	private IncludeNavcamAction includeNavcamAction;
	private IncludePancamAction includePancamAction;
	private IncludeRearHazcamAction includeRearHazcamAction;
	
	private ShowLeftImagesAction showLeftImagesAction;
	private ShowRightImagesAction showRightImagesAction;
	private ShowLeftAndRightImagesAction showLeftAndRightImagesAction;
	private ShowAnaglyphImagesAction showAnaglyphImagesAction;

	private IncludeDriveImagesAction showDriveImagesAction;
	private IncludeSundialImagesAction showSundialImagesAction;
	private IncludeSolarFilterImagesAction showSolarFilterImagesAction;
	
	private SplitByLocationAction splitByLocationAction;
	private SplitBySolAction splitBySolAction;
	private SplitByElapsedTimeAction splitByElapsedTimeAction;
	

	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
	}

	protected void makeActions(final IWorkbenchWindow window) {
		// Creates the actions and registers them.
		// Registering is needed to ensure that key bindings work.
		// The corresponding commands keybindings are defined in the plugin.xml
		// file.
		// Registering also provides automatic disposal of the actions when
		// the window is closed.

		exitAction = ActionFactory.QUIT.create(window);
		register(exitAction);
		
		newViewerAction = new NewViewerAction(window);
		register(newViewerAction);
		openImageAction = new OpenImageAction(window);
		register(openImageAction);
		switchWorkspaceAction = new SwitchWorkspaceAction(window);
		register(switchWorkspaceAction);
		saveImageDestretchMetadataAction = new SaveImageDestretchMetadataAction(window);
		register(saveImageDestretchMetadataAction);
		exportPanImagesAction = new ExportPanImagesAction(window);
		register(exportPanImagesAction);	
		takePanScreenshotAction = new TakePanScreenshotAction(window);
		register(takePanScreenshotAction);
		openPanMovieAction = new OpenPanMovieAction(window);
		register(openPanMovieAction);
		exportPanMovieFramesAction = new ExportPanMovieFramesAction(window);
		register(exportPanMovieFramesAction);
		
		copyPanPositionAction = new CopyPanPositionAction(window);
		register(copyPanPositionAction);
		
		showViewerSettingsViewAction = new ShowViewerSettingsViewAction(window);
		register(showViewerSettingsViewAction);
		showImagesViewAction = new ShowImagesViewAction(window);
		register(showImagesViewAction);
		showTimeIntervalsViewAction = new ShowTimeIntervalsViewAction(window);
		register(showTimeIntervalsViewAction);
		openTimeViewAction = new OpenTimeViewAction(window);
		register(openTimeViewAction);
		openUpdateViewAction = new OpenUpdateConsoleViewAction(window);
		register(openUpdateViewAction);
		openUpdateViewerAction = new OpenUpdateViewerAction(window);
		register(openUpdateViewerAction);
		openImageDestretchViewAction = new OpenImageDestretchViewAction(window);
		register(openImageDestretchViewAction);
		openPanInfoViewAction = new OpenPanInfoViewAction(window);
		register(openPanInfoViewAction);
		openImageInfoViewAction = new OpenImageInfoViewAction(window);
		register(openImageInfoViewAction);
		
		getLatestImagesFromExploratoriumAction = new GetLatestImagesFromExploratoriumAction(window);
		register(getLatestImagesFromExploratoriumAction);
		getLatestImagesFromJPLAction = new GetLatestImagesFromJPLAction(window);
		register(getLatestImagesFromJPLAction);
		checkForNewImagesAction = new CheckForNewImagesAction(window);
		register(checkForNewImagesAction);
		advancedImageUpdateAction = new AdvancedUpdateImagesAction(window);
		register(advancedImageUpdateAction);
		updatePDSImagesAction = new UpdatePDSImagesAction(window);
		register(updatePDSImagesAction);
		updatePhoenixImagesAction = new UpdatePhoenixImagesAction(window);
		register(updatePhoenixImagesAction);
		updatePhoenixImagesFullAction = new UpdatePhoenixImagesFullAction(window);
		register(updatePhoenixImagesFullAction);
		regenerateImagesAction = new RegenerateImagesAction(window);
		register(regenerateImagesAction);
		rebuildImageIndexesAction = new RebuildImageIndexesAction(window);
		register(rebuildImageIndexesAction);		
		buildMetadataAction = new BuildMetadataAction(window);
		register(buildMetadataAction);
		buildMetadata1xAction = new BuildMetadata1xAction(window);
		register(buildMetadata1xAction);
		stopUpdateAction = new StopUpdateAction(window);
		register(stopUpdateAction);
		updateMetadataAction = new UpdateMetadataAction(window);
		register(updateMetadataAction);
		testAction = new TestAction(window);
		register(testAction);
		
		nextImageAction = new NextImageAction(window);
		register(nextImageAction);
		previousImageAction = new PreviousImageAction(window);
		register(previousImageAction);
		nextTimeIntervalAction = new NextTimeIntervalAction(window);
		register(nextTimeIntervalAction);
		previousTimeIntervalAction = new PreviousTimeIntervalAction(window);
		register(previousTimeIntervalAction);
		viewNorthAction = new ViewNorthAction(window);
		register(viewNorthAction);
		viewEastAction = new ViewEastAction(window);
		register(viewEastAction);
		viewSouthAction = new ViewSouthAction(window);
		register(viewSouthAction);
		viewWestAction = new ViewWestAction(window);
		register(viewWestAction);
		viewLevelAction = new ViewLevelAction(window);
		register(viewLevelAction);
		viewUpAction = new ViewUpAction(window);
		register(viewUpAction);
		viewDownAction = new ViewDownAction(window);
		register(viewDownAction);
		centerOnClosestImageAction = new CenterOnClosestImageAction(window);
		register(centerOnClosestImageAction);
		toggleImageFocusAction = new ToggleImageFocusAction(window);
		register(toggleImageFocusAction);
		roverTrackingAction = new ShowRoverTrackingAction(window);
		register(roverTrackingAction);
		roverModelAction = new ShowRoverModelAction(window);
		register(roverModelAction);
		moveRoverModelForwardAction = new MoveRoverModelForwardAction(window);
		register(moveRoverModelForwardAction);
		moveRoverModelBackwardAction = new MoveRoverModelBackwardAction(window);
		register(moveRoverModelBackwardAction);
		zoomToNavcamResolutionAction = new ZoomToNavcamResolutionAction(window);
		register(zoomToNavcamResolutionAction);
		zoomToPancamResolutionAction = new ZoomToPancamResolutionAction(window);
		register(zoomToPancamResolutionAction);
		playPanMovieAction = new PlayPanMovieAction(window);
		register(playPanMovieAction);
		nextMovieFrameAction = new NextMovieFrameAction(window);
		register(nextMovieFrameAction);
		previousMovieFrameAction = new PreviousMovieFrameAction(window);
		register(previousMovieFrameAction);
		nextMovieEndpointAction = new NextMovieEndpointAction(window);
		register(nextMovieEndpointAction);
		previousMovieEndpointAction = new PreviousMovieEndpointAction(window);
		register(previousMovieEndpointAction);
		
		aboutAction = ActionFactory.ABOUT.create(window);
		register(aboutAction);
		helpAction = ActionFactory.HELP_CONTENTS.create(window);
		register(helpAction);
		updateSoftwareAction = new UpdateSoftwareAction(window);		
		register(updateSoftwareAction);
		
		newSpiritViewerAction = new NewSpiritViewerAction(window);
		register(newSpiritViewerAction);
		newOpportunityViewerAction = new NewOpportunityViewerAction(window);
		register(newOpportunityViewerAction);
		
		panoramaModeAction = new PanoramaModeAction(window);
		register(panoramaModeAction);
		
		includeForwardHazcamAction = new IncludeForwardHazcamAction(window);
		register(includeForwardHazcamAction);
		includeMicroAction = new IncludeMicroAction(window);
		register(includeMicroAction);
		includeNavcamAction = new IncludeNavcamAction(window);
		register(includeNavcamAction);
		includePancamAction = new IncludePancamAction(window);
		register(includePancamAction);
		includeRearHazcamAction = new IncludeRearHazcamAction(window);
		register(includeRearHazcamAction);
		
		showLeftImagesAction = new ShowLeftImagesAction(window);
		register(showLeftImagesAction);
		showRightImagesAction = new ShowRightImagesAction(window);
		register(showRightImagesAction);
		showLeftAndRightImagesAction = new ShowLeftAndRightImagesAction(window);
		register(showLeftAndRightImagesAction);
		showAnaglyphImagesAction = new ShowAnaglyphImagesAction(window);
		register(showAnaglyphImagesAction);		
		
		showDriveImagesAction = new IncludeDriveImagesAction(window);
		register(showDriveImagesAction);
		showSundialImagesAction = new IncludeSundialImagesAction(window);
		register(showSundialImagesAction);
		showSolarFilterImagesAction = new IncludeSolarFilterImagesAction(window);
		register(showSolarFilterImagesAction);
		
		splitByLocationAction = new SplitByLocationAction(window);
		register(splitByLocationAction);
		splitBySolAction = new SplitBySolAction(window);
		register(splitBySolAction);
		splitByElapsedTimeAction = new SplitByElapsedTimeAction(window);
		register(splitByElapsedTimeAction);
		
	}

	protected void fillMenuBar(IMenuManager menuBar) {
		
		MenuManager fileMenu = new MenuManager("&File",
				IWorkbenchActionConstants.M_FILE);
		fileMenu.add(newViewerAction);
		if (Application.isAdvancedMode()) {
			fileMenu.add(openImageAction);
		}
		fileMenu.add(new Separator()); 
		fileMenu.add(switchWorkspaceAction);
		fileMenu.add(new Separator());
		fileMenu.add(exportPanImagesAction);
		fileMenu.add(takePanScreenshotAction);
		if (Application.isAdminMode()) {
			fileMenu.add(new Separator());
			fileMenu.add(saveImageDestretchMetadataAction);
		}
		if (Application.isAdvancedMode()) {
			fileMenu.add(new Separator());
			fileMenu.add(openPanMovieAction);
			fileMenu.add(exportPanMovieFramesAction);
		}
		
		fileMenu.add(new Separator());
		fileMenu.add(exitAction);
		
		MenuManager editMenu = new MenuManager("Edit", IWorkbenchActionConstants.M_EDIT );
		if (Application.isAdvancedMode()) {
			editMenu.add(copyPanPositionAction);
		}
		
		MenuManager updateMenu = new MenuManager("Update", "update");
		updateMenu.add(getLatestImagesFromExploratoriumAction);
		updateMenu.add(getLatestImagesFromJPLAction);
		updateMenu.add(checkForNewImagesAction);
		updateMenu.add(advancedImageUpdateAction);
		if (Application.isAdvancedMode()) {
			updateMenu.add(updatePDSImagesAction);
		}
		updateMenu.add(updatePhoenixImagesAction);
		updateMenu.add(updatePhoenixImagesFullAction);
		updateMenu.add(regenerateImagesAction);
		updateMenu.add(rebuildImageIndexesAction);
		updateMenu.add(new Separator());
		updateMenu.add(updateMetadataAction);
		updateMenu.add(stopUpdateAction);
		if (Application.isAdminMode()) {
			updateMenu.add(new Separator());
			updateMenu.add(buildMetadataAction);
			updateMenu.add(buildMetadata1xAction);
		}
		
		MenuManager navigateMenu = new MenuManager("Navigate", "navigate");
		navigateMenu.add(nextImageAction);
		navigateMenu.add(previousImageAction);
		navigateMenu.add(nextTimeIntervalAction);
		navigateMenu.add(previousTimeIntervalAction);
		navigateMenu.add(moveRoverModelForwardAction);
		navigateMenu.add(moveRoverModelBackwardAction);
		navigateMenu.add(new Separator());
		navigateMenu.add(viewNorthAction);
		navigateMenu.add(viewSouthAction);
		navigateMenu.add(viewEastAction);
		navigateMenu.add(viewWestAction);
		navigateMenu.add(viewLevelAction);
		navigateMenu.add(viewUpAction);
		navigateMenu.add(viewDownAction);
		navigateMenu.add(new Separator());
		navigateMenu.add(centerOnClosestImageAction);
		navigateMenu.add(toggleImageFocusAction);
		navigateMenu.add(roverTrackingAction);
		navigateMenu.add(roverModelAction);
		navigateMenu.add(new Separator());
		navigateMenu.add(zoomToNavcamResolutionAction);
		navigateMenu.add(zoomToPancamResolutionAction);
		
		if (Application.isAdvancedMode()) {
			navigateMenu.add(new Separator());
			navigateMenu.add(playPanMovieAction);
			navigateMenu.add(nextMovieFrameAction);
			navigateMenu.add(previousMovieFrameAction);
			navigateMenu.add(nextMovieEndpointAction);
			navigateMenu.add(previousMovieEndpointAction);
		
			navigateMenu.add(new Separator());
			navigateMenu.add(testAction);
		}
		
		// TODO is there a hard-coded constant
		MenuManager windowsMenu = new MenuManager("&Window", "window");
		windowsMenu.add(showViewerSettingsViewAction);
		windowsMenu.add(showTimeIntervalsViewAction);
		windowsMenu.add(showImagesViewAction);
		windowsMenu.add(openPanInfoViewAction);
		windowsMenu.add(openImageInfoViewAction);
		windowsMenu.add(openTimeViewAction);
		windowsMenu.add(openUpdateViewAction);
		windowsMenu.add(openUpdateViewerAction);
		if (Application.isAdminMode()) {
			windowsMenu.add(openImageDestretchViewAction);
		}
		
		MenuManager helpMenu = new MenuManager("&Help", IWorkbenchActionConstants.M_HELP);
		helpMenu.add(aboutAction);
		helpMenu.add(helpAction);
		helpMenu.add(updateSoftwareAction);
		
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(updateMenu);
		menuBar.add(navigateMenu);
		menuBar.add(windowsMenu);
		menuBar.add(helpMenu);
	}

	protected void fillCoolBar(ICoolBarManager coolBar) {
		IToolBarManager toolbar = new ToolBarManager(coolBar.getStyle());
		coolBar.add(toolbar);
		toolbar.add(getLatestImagesFromExploratoriumAction);
		toolbar.add(new Separator());
		toolbar.add(newSpiritViewerAction);
		toolbar.add(newOpportunityViewerAction);
		toolbar.add(new Separator());
		toolbar.add(panoramaModeAction);
		toolbar.add(new Separator());
		toolbar.add(showViewerSettingsViewAction);
		toolbar.add(showTimeIntervalsViewAction);
		toolbar.add(showImagesViewAction);
		toolbar.add(new Separator());
		toolbar.add(includeForwardHazcamAction);
		toolbar.add(includeMicroAction);
		toolbar.add(includeNavcamAction);
		toolbar.add(includePancamAction);
		toolbar.add(includeRearHazcamAction);
		toolbar.add(new Separator());
		toolbar.add(showLeftImagesAction);
		toolbar.add(showRightImagesAction);
		toolbar.add(showLeftAndRightImagesAction);
		toolbar.add(showAnaglyphImagesAction);
		toolbar.add(new Separator());
		toolbar.add(showDriveImagesAction);
		toolbar.add(showSundialImagesAction);
		toolbar.add(showSolarFilterImagesAction);
		toolbar.add(new Separator());
		toolbar.add(splitByLocationAction);
		toolbar.add(splitBySolAction);
		toolbar.add(splitByElapsedTimeAction);
		toolbar.add(new Separator());		
		toolbar.add(roverTrackingAction);
		toolbar.add(roverModelAction);
		toolbar.add(new Separator());
		toolbar.add(previousTimeIntervalAction);
		toolbar.add(previousImageAction);
		toolbar.add(nextImageAction);
		toolbar.add(nextTimeIntervalAction);
	}
}
