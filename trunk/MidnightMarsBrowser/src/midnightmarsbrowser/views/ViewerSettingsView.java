package midnightmarsbrowser.views;

import midnightmarsbrowser.application.Activator;
import midnightmarsbrowser.application.Application;
import midnightmarsbrowser.editors.ViewerEditor;
import midnightmarsbrowser.editors.MMBEditorInput;
import midnightmarsbrowser.model.MerUtils;
import midnightmarsbrowser.model.ViewerSettings;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

/**
 */
public class ViewerSettingsView extends MMBViewBase {
	public final static String ID = "midnightmarsbrowser.views.ViewerSettingsView";
	
	Label	viewType_label;
	Combo	viewType_combo;
	Label rover_label;
	Combo	rover_combo;
	Button	limitToUpdate_button;
	Text	fromSol_text;
	Text	toSol_text;
	Button	left_button;
	Button	right_button;
	Button	anaglyph_button;
	Button	forwardHazcam_button;
	Button	rearHazcam_button;
	Button 	navcam_button;
	Button	pancam_button;
	Button	microImager_button;
	Button	pancamRaw_button;
	Button	pancamMMBFalseColor_button;
	Button	pancamDCCalibratedColor_button;
	Button 	solarFilterImages_button;
	Button	sundialImages_button;
	Button	driveImages_button;	
	Button	fullFrame_button;
	Button	subFrame_button;
	Button	downsampled_button;
	Combo panHalfRes_combo;
	Combo panOrientation_combo;
	Composite button_composite;
	private Button roverTracking_button;
	private Button roverModel_button;
	
	boolean dirtyRecomputeImageList = false;
	boolean dirtyNoRecomputeImageList = false;

	private Text includePancamFilter_text;

	private Button splitByLocation_button;

	private Button splitBySol_button;

	private Button splitByElapsedTime_button;

	private Text splitElapsedTimeMinutes_text;

	private Button pdsEDR_button;

	private Button pdsRAD_button;

	private Button pdsMRD_button;

	private Text brightnessMinValue_text;

	private Text brightnessMaxValue_text;

	
	/**
	 */
	public void createPartControl(Composite parent) {
		SelectionListener updateSelectionAdapter = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (!isInNotification()) {
					dirtyRecomputeImageList = true;
					//applyChanges_button.setEnabled(true);
					//updateEditor(true);
				}
			}
		};
		
		SelectionListener passiveUpdateSelectionAdapter = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (!isInNotification()) {
					dirtyNoRecomputeImageList = true;
					//applyChanges_button.setEnabled(true);
					//updateEditor(false);
				}
			}
		};
		
		ModifyListener modifyListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (!isInNotification()) {
					dirtyRecomputeImageList = true;
					//applyChanges_button.setEnabled(true);
				}
			}
		};
		
		ScrolledComposite sc = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);	
		
		Composite composite = new Composite(sc, SWT.NONE);
		composite.setLayout(new GridLayout());
		
        Group presentation_group = new Group(composite, SWT.NONE);
        presentation_group.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        presentation_group.setText("Presentation");
        presentation_group.setLayout(new GridLayout(2, true));
		
		rover_label = new Label(presentation_group, SWT.NONE);
		rover_label.setText("Rover");
		
		rover_combo = new Combo(presentation_group, SWT.READ_ONLY);
		rover_combo.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        rover_combo.setItems(new String[] {"Opportunity", "Spirit"});
        rover_combo.addSelectionListener(updateSelectionAdapter);

		viewType_label = new Label(presentation_group, SWT.NONE);
		viewType_label.setText("View type");
		
		viewType_combo = new Combo(presentation_group, SWT.READ_ONLY);
		viewType_combo.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		viewType_combo.setItems(new String[] {"Slideshow", "Panorama"});
		viewType_combo.addSelectionListener(updateSelectionAdapter);
		
		limitToUpdate_button = new Button(presentation_group, SWT.CHECK);
		limitToUpdate_button.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));		
		limitToUpdate_button.setText("Limit to latest update");
		limitToUpdate_button.addSelectionListener(updateSelectionAdapter);
		
        Group interval_group = new Group(composite, SWT.NONE);
        interval_group.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        interval_group.setText("Time intervals");
        interval_group.setLayout(new GridLayout(4, true));
                
        Label fromSol_label = new Label(interval_group, SWT.NONE);
        fromSol_label.setText("From sol");
        fromSol_text = new Text(interval_group, SWT.BORDER);
        fromSol_text.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        fromSol_text.addModifyListener(modifyListener);
        
        Label toSol_label = new Label(interval_group, SWT.NONE);
        toSol_label.setText("to sol");
        toSol_text = new Text(interval_group, SWT.BORDER);
        toSol_text.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        toSol_text.addModifyListener(modifyListener);
        
        splitByLocation_button = new Button(interval_group, SWT.CHECK);
        splitByLocation_button.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));
        splitByLocation_button.setText("Split by location");
        splitByLocation_button.addSelectionListener(updateSelectionAdapter);
        
        splitBySol_button = new Button(interval_group, SWT.CHECK);
        splitBySol_button.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));
        splitBySol_button.setText("Split by sol");
        splitBySol_button.addSelectionListener(updateSelectionAdapter);
        
        splitByElapsedTime_button = new Button(interval_group, SWT.CHECK);
        splitByElapsedTime_button.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));
        splitByElapsedTime_button.setText("by elapsed minutes:");
        splitByElapsedTime_button.addSelectionListener(updateSelectionAdapter);
        
        splitElapsedTimeMinutes_text = new Text(interval_group, SWT.BORDER);
        splitElapsedTimeMinutes_text.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));
        splitElapsedTimeMinutes_text.addModifyListener(modifyListener);
		
        Group eye_group = new Group(composite, SWT.NONE);
        eye_group.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        eye_group.setText("Camera eye");
        eye_group.setLayout(new GridLayout(4, true));
                
        left_button = new Button(eye_group, SWT.CHECK);
        left_button.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        left_button.setText("Left");
        left_button.addSelectionListener(updateSelectionAdapter);
        
        right_button = new Button(eye_group, SWT.CHECK);
        right_button.setText("Right");
        right_button.addSelectionListener(updateSelectionAdapter);
        
        anaglyph_button = new Button(eye_group, SWT.CHECK);
        anaglyph_button.setText("Anaglyph");
        anaglyph_button.addSelectionListener(updateSelectionAdapter);
        
        Group cameras_group = new Group(composite, SWT.NONE);
        cameras_group.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        cameras_group.setText("Cameras");
        cameras_group.setLayout(new GridLayout(2, true));
                
        forwardHazcam_button = new Button(cameras_group, SWT.CHECK);
        forwardHazcam_button.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        forwardHazcam_button.setText("Forward Hazcam");
        forwardHazcam_button.addSelectionListener(updateSelectionAdapter);
        
        rearHazcam_button = new Button(cameras_group, SWT.CHECK);
        rearHazcam_button.setText("Rear Hazcam");
        rearHazcam_button.addSelectionListener(updateSelectionAdapter);
        
        navcam_button = new Button(cameras_group, SWT.CHECK);
        navcam_button.setText("Navcam");
        navcam_button.addSelectionListener(updateSelectionAdapter);
        
        pancam_button = new Button(cameras_group, SWT.CHECK);
        pancam_button.setText("Pancam");
        pancam_button.addSelectionListener(updateSelectionAdapter);
        
        microImager_button = new Button(cameras_group, SWT.CHECK);
        microImager_button.setText("Micro Imager");
        microImager_button.addSelectionListener(updateSelectionAdapter);
        
        Group pancamImageType_group = new Group(composite, SWT.NONE);
        pancamImageType_group.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        pancamImageType_group.setText("Image Types");
        pancamImageType_group.setLayout(new GridLayout(2, true));
        
        pancamRaw_button = new Button(pancamImageType_group, SWT.CHECK);
        pancamRaw_button.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        pancamRaw_button.setText("JPEG Raw Images");
        pancamRaw_button.addSelectionListener(updateSelectionAdapter);
                
        pdsEDR_button = new Button(pancamImageType_group, SWT.CHECK);
        pdsEDR_button.setText("PDS Raw Images (EDR)");
        pdsEDR_button.addSelectionListener(updateSelectionAdapter);
        if (!Application.isAdvancedMode()) {
        	pdsEDR_button.setEnabled(false);
        }
        
        pdsMRD_button = new Button(pancamImageType_group, SWT.CHECK);
        pdsMRD_button.setText("PDS Radiometric (MRD)");
        pdsMRD_button.addSelectionListener(updateSelectionAdapter);
        if (!Application.isAdvancedMode()) {
        	pdsMRD_button.setEnabled(false);
        }

        pdsRAD_button = new Button(pancamImageType_group, SWT.CHECK);
        pdsRAD_button.setText("PDS Radiometric (RAD)");
        pdsRAD_button.addSelectionListener(updateSelectionAdapter);
        if (!Application.isAdvancedMode()) {
        	pdsRAD_button.setEnabled(false);
        }
                
        pancamDCCalibratedColor_button = new Button(pancamImageType_group, SWT.CHECK);
        pancamDCCalibratedColor_button.setText("DC Calibrated Color");
        pancamDCCalibratedColor_button.addSelectionListener(updateSelectionAdapter);
//        pancamDCCalibratedColor_button.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));

        pancamMMBFalseColor_button = new Button(pancamImageType_group, SWT.CHECK);
        pancamMMBFalseColor_button.setText("MMB False-Color");
        pancamMMBFalseColor_button.addSelectionListener(updateSelectionAdapter);
        
        Label includePancamFilter_label = new Label(pancamImageType_group, SWT.NONE);
        includePancamFilter_label.setText("Include filters");
        
        includePancamFilter_text = new Text(pancamImageType_group, SWT.BORDER);        
        includePancamFilter_text.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        includePancamFilter_text.addModifyListener(modifyListener);
        
        Group pds_group = new Group(composite, SWT.NONE);
        pds_group.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        pds_group.setText("PDS Image Brightness");
        pds_group.setLayout(new GridLayout(4, true));
        
        Label brightnessMinValue_label = new Label(pds_group, SWT.NONE);
        brightnessMinValue_label.setText("Clip min");
        brightnessMinValue_text = new Text(pds_group, SWT.BORDER);
        brightnessMinValue_text.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        brightnessMinValue_text.addModifyListener(modifyListener);
        
        Label brightnessMaxValue_label = new Label(pds_group, SWT.NONE);
        brightnessMaxValue_label.setText("Clip max");
        brightnessMaxValue_text = new Text(pds_group, SWT.BORDER);
        brightnessMaxValue_text.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        brightnessMaxValue_text.addModifyListener(modifyListener);
        
        Group include_group = new Group(composite, SWT.NONE);
        include_group.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        include_group.setText("Include");
        include_group.setLayout(new GridLayout(2, true));
        
        solarFilterImages_button = new Button(include_group, SWT.CHECK);
        solarFilterImages_button.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        solarFilterImages_button.setText("Solar filter images");
        solarFilterImages_button.addSelectionListener(updateSelectionAdapter);
        
        sundialImages_button = new Button(include_group, SWT.CHECK);
        sundialImages_button.setText("Sundial images");
        sundialImages_button.addSelectionListener(updateSelectionAdapter);
        
        driveImages_button = new Button(include_group, SWT.CHECK);
        driveImages_button.setText("Drive images");
        driveImages_button.addSelectionListener(updateSelectionAdapter);
        
        Group imageSize_group = new Group(composite, SWT.NONE);
        imageSize_group.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        imageSize_group.setText("Image Size/Framing");
        imageSize_group.setLayout(new GridLayout(2, true));
        
        fullFrame_button = new Button(imageSize_group, SWT.CHECK);
        fullFrame_button.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        fullFrame_button.setText("Full-frame");
        fullFrame_button.addSelectionListener(updateSelectionAdapter);
        
        subFrame_button = new Button(imageSize_group, SWT.CHECK);
        subFrame_button.setText("Sub-frame");
        subFrame_button.addSelectionListener(updateSelectionAdapter);
        
        downsampled_button = new Button(imageSize_group, SWT.CHECK);
        downsampled_button.setText("Downsampled");
        downsampled_button.addSelectionListener(updateSelectionAdapter);
        
        Group panOptions_group = new Group(composite, SWT.NONE);
        panOptions_group.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        panOptions_group.setText("Panorama Options");
        panOptions_group.setLayout(new GridLayout(2, true));
        
		Label panHalfRes_label = new Label(panOptions_group, SWT.NONE);
		panHalfRes_label.setText("Resolution");
		
		panHalfRes_combo = new Combo(panOptions_group, SWT.READ_ONLY);
		panHalfRes_combo.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		panHalfRes_combo.setItems(new String[] {"Full", "Half", "Quarter"});
		panHalfRes_combo.addSelectionListener(updateSelectionAdapter);

		Label panOrientation_label = new Label(panOptions_group, SWT.NONE);
		panOrientation_label.setText("Orientation");
		
		panOrientation_combo = new Combo(panOptions_group, SWT.READ_ONLY);
		panOrientation_combo.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		panOrientation_combo.setItems(new String[] {"Rover Relative", "Ground Relative"});
		panOrientation_combo.addSelectionListener(passiveUpdateSelectionAdapter);
		
		roverTracking_button = new Button(panOptions_group, SWT.CHECK);
		roverTracking_button.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));
		roverTracking_button.setText("Show rover tracking");
		roverTracking_button.addSelectionListener(passiveUpdateSelectionAdapter);
		
		roverModel_button = new Button(panOptions_group, SWT.CHECK);
		roverModel_button.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));
		roverModel_button.setText("Show rover model");
		roverModel_button.addSelectionListener(passiveUpdateSelectionAdapter);
		
		Composite button_composite = new Composite(composite, SWT.NONE);
		button_composite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		button_composite.setLayout(new GridLayout());
		Button applyChanges_button = new Button(button_composite, SWT.PUSH);
		applyChanges_button.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
		applyChanges_button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				applyChanges();
			}
		});
		applyChanges_button.setText("Apply Settings");
		        
        Action newViewerAction = new Action("New Viewer") {
			public void run() {
				createNewViewer();
			}
		};
		newViewerAction.setImageDescriptor(Activator.getImageDescriptor("icons/new_viewer.gif"));
        
        Action applyChangesAction = new Action("Apply Settings") {
			public void run() {
				applyChanges();
			}
		};
		applyChangesAction.setImageDescriptor(Activator.getImageDescriptor("icons/apply_changes.gif"));		
		
		IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
        mgr.add(newViewerAction);
        mgr.add(applyChangesAction);
		
		ViewerSettings settings = new ViewerSettings(MerUtils.ROVERCODE_OPPORTUNITY);
		settings.panorama = true;
		setControlsFromViewerSettings(settings);
        
		sc.setContent(composite);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		sc.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		super.createPartControl(parent);
	}
		
	public void dispose() {
		super.dispose();
	}

	void notificationCurrentEditorChanged() {
/*		
		viewType_label.setEnabled(haveEditor);
		viewType_combo.setEnabled(haveEditor);
		rover_label.setEnabled(haveEditor);
		rover_combo.setEnabled(haveEditor);
		limitToUpdate_button.setEnabled(haveEditor);
		left_button.setEnabled(haveEditor);
		right_button.setEnabled(haveEditor);
		anaglyph_button.setEnabled(haveEditor);
		forwardHazcam_button.setEnabled(haveEditor);
		rearHazcam_button.setEnabled(haveEditor);
		navcam_button.setEnabled(haveEditor);
		pancam_button.setEnabled(haveEditor);
		microImager_button.setEnabled(haveEditor);
		pancamRaw_button.setEnabled(haveEditor);
		pancamMMBFalseColor_button.setEnabled(haveEditor);;
		pancamDCCalibratedColor_button.setEnabled(haveEditor);;
		fullFrame_button.setEnabled(haveEditor);;
		subFrame_button.setEnabled(haveEditor);;
		downsampled_button.setEnabled(haveEditor);;
*/
		if (currentEditor != null) {
			ViewerSettings settings = currentEditor.getViewerSettings();
			if (settings != null) {
				setControlsFromViewerSettings(settings);
			}
		}
		dirtyRecomputeImageList = false;
		dirtyNoRecomputeImageList = false;
		//applyChanges_button.setEnabled(false);
	}

	public void setFocus() {		
	}
	
	private void applyChanges() {
		if (currentEditor == null) {
			createNewViewer();
		}
		else {
			updateEditor(dirtyRecomputeImageList);
		}		
	}
	
	private void updateEditor(boolean recomputeImageList) {
		if (currentEditor != null) {
			ViewerSettings settings = viewerSettingsFromControls();
			currentEditor.setViewerSettings(settings, recomputeImageList);
		}
		dirtyRecomputeImageList = false;
		dirtyNoRecomputeImageList = false;
		//applyChanges_button.setEnabled(false);
	}

	public ViewerSettings viewerSettingsFromControls() {
		ViewerSettings settings = new ViewerSettings(null);
		settings.panorama = (viewType_combo.getSelectionIndex() == 1);		
		if (rover_combo.getSelectionIndex() == 0) {
			settings.roverCode = MerUtils.ROVERCODE_OPPORTUNITY;
		}
		else if (rover_combo.getSelectionIndex() == 1) {
			settings.roverCode = MerUtils.ROVERCODE_SPIRIT;
		}
		settings.limitToSet = limitToUpdate_button.getSelection();
		settings.left = left_button.getSelection();
		settings.right = right_button.getSelection();
		settings.anaglyph = anaglyph_button.getSelection();
		settings.fromSol = 0;
		try {
			settings.fromSol = Integer.parseInt(fromSol_text.getText());
		}
		catch (Exception e) {			
		}		
		settings.toSol = 9999;
		try {
			settings.toSol = Integer.parseInt(toSol_text.getText());
		}
		catch (Exception e) {			
		}		
		settings.splitByLocation = splitByLocation_button.getSelection();
		settings.splitBySol = splitBySol_button.getSelection();
		settings.splitByElapsedTime = splitByElapsedTime_button.getSelection();
		settings.splitElapsedTimeMinutes = 0;
		try {
			settings.splitElapsedTimeMinutes = Integer.parseInt(splitElapsedTimeMinutes_text.getText());
		}
		catch (Exception e) {
		}
		settings.f = forwardHazcam_button.getSelection();
		settings.r = rearHazcam_button.getSelection();
		settings.n = navcam_button.getSelection();
		settings.p = pancam_button.getSelection();
		settings.m = microImager_button.getSelection();
		settings.pancamRaw = pancamRaw_button.getSelection();
		settings.pancamMMBFalseColor = pancamMMBFalseColor_button.getSelection();
		settings.pancamDCCalibratedColor = pancamDCCalibratedColor_button.getSelection();
		settings.pdsEDR = pdsEDR_button.getSelection();
		settings.pdsMRD = pdsMRD_button.getSelection();
		settings.pdsRAD = pdsRAD_button.getSelection();
		settings.pdsBrightnessMinValue = 0;
		try {
			settings.pdsBrightnessMinValue = Integer.parseInt(brightnessMinValue_text.getText());
		}
		catch (Exception e) {
		}
		settings.pdsBrightnessMaxValue = 0;
		try {
			settings.pdsBrightnessMaxValue = Integer.parseInt(brightnessMaxValue_text.getText());
		}
		catch (Exception e) {
		}		
		settings.includePancamFilter = MerUtils.arrayFromString(includePancamFilter_text.getText());
		settings.excludeSolarFilter = !solarFilterImages_button.getSelection();
		settings.excludeSundial = !sundialImages_button.getSelection();
		settings.excludeDriveImages = !driveImages_button.getSelection();
		settings.fullFrame = fullFrame_button.getSelection();
		settings.subFrame = subFrame_button.getSelection();
		settings.downsampled = downsampled_button.getSelection();
		if (panHalfRes_combo.getSelectionIndex() == 1) {
			settings.panMaxResolution = 512;
		}
		else if (panHalfRes_combo.getSelectionIndex() == 2) {
			settings.panMaxResolution = 256;
		}
		else {
			settings.panMaxResolution = 1024;
		}
		if (panOrientation_combo.getSelectionIndex() == 1) {
			settings.panGroundRelative = true;
		}
		else {
			settings.panGroundRelative = false;
		}
		settings.panShowRoverTracking = roverTracking_button.getSelection();
		settings.panShowRoverModel = roverModel_button.getSelection();
		return settings;
	}
	
	private void setControlsFromViewerSettings(ViewerSettings settings) {
		if (settings.panorama) {
			viewType_combo.select(1);				
		}
		else {
			viewType_combo.select(0);
		}
		if (settings.roverCode.equals(MerUtils.ROVERCODE_OPPORTUNITY)) {
			rover_combo.select(0);
		}
		else if (settings.roverCode.equals(MerUtils.ROVERCODE_SPIRIT)) {
			rover_combo.select(1);
		}
		limitToUpdate_button.setSelection(settings.limitToSet);
		left_button.setSelection(settings.left);
		right_button.setSelection(settings.right);
		anaglyph_button.setSelection(settings.anaglyph);
		fromSol_text.setText(""+settings.fromSol);
		toSol_text.setText(""+settings.toSol);
		splitByLocation_button.setSelection(settings.splitByLocation);
		splitBySol_button.setSelection(settings.splitBySol);
		splitByElapsedTime_button.setSelection(settings.splitByElapsedTime);
		splitElapsedTimeMinutes_text.setText(""+settings.splitElapsedTimeMinutes);
		forwardHazcam_button.setSelection(settings.f);
		rearHazcam_button.setSelection(settings.r);
		navcam_button.setSelection(settings.n);
		pancam_button.setSelection(settings.p);
		microImager_button.setSelection(settings.m);
		pancamRaw_button.setSelection(settings.pancamRaw);
		pancamMMBFalseColor_button.setSelection(settings.pancamMMBFalseColor);
		pancamDCCalibratedColor_button.setSelection(settings.pancamDCCalibratedColor);
		pdsEDR_button.setSelection(settings.pdsEDR);
		pdsMRD_button.setSelection(settings.pdsMRD);
		pdsRAD_button.setSelection(settings.pdsRAD);
		brightnessMinValue_text.setText(""+settings.pdsBrightnessMinValue);
		brightnessMaxValue_text.setText(""+settings.pdsBrightnessMaxValue);
		includePancamFilter_text.setText(MerUtils.stringFromArray(settings.includePancamFilter));
		solarFilterImages_button.setSelection(!settings.excludeSolarFilter);
		sundialImages_button.setSelection(!settings.excludeSundial);
		driveImages_button.setSelection(!settings.excludeDriveImages);
		fullFrame_button.setSelection(settings.fullFrame);
		subFrame_button.setSelection(settings.subFrame);
		downsampled_button.setSelection(settings.downsampled);
		int resolution = settings.panMaxResolution;
		if (resolution == 512) {
			panHalfRes_combo.select(1);
		}
		else if (resolution == 256) {
			panHalfRes_combo.select(2);
		}
		else {
			panHalfRes_combo.select(0);
		}
		if (settings.panGroundRelative) {
			panOrientation_combo.select(1);
		}
		else {
			panOrientation_combo.select(0);
		}
		roverTracking_button.setSelection(settings.panShowRoverTracking);
		roverModel_button.setSelection(settings.panShowRoverModel);
	}
	
	void createNewViewer() {
		try {
			IWorkbenchPage page = this.getSite().getPage();
			ViewerSettings viewerSettings = this.viewerSettingsFromControls();
			MMBEditorInput input = new MMBEditorInput(viewerSettings);
			page.openEditor(input, ViewerEditor.ID);
		} catch (PartInitException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		
	}	
	

}