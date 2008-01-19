package midnightmarsbrowser.dialogs;

import midnightmarsbrowser.model.ViewerSettings;
import midnightmarsbrowser.model.MerUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class SlideshowSettingsDialog extends Dialog {

	boolean newSlideshow;
	
	ViewerSettings settings;
	
	Combo	rover_combo;
	Text	fromSol_text;
	Text	toSol_text;
	Button	forwardHazcam_button;
	Button	rearHazcam_button;
	Button 	navcam_button;
	Button	pancam_button;
	Button	microImager_button;
	Button	leftRaw_button;
	Button	rightRaw_button;
	Button	mmbAnaglyph_button;	
	Button	pancamLeftRaw_button;
	Button	pancamRightRaw_button;
	Button	pancamMMBAnaglyph_button;
	Button	pancamMMBFalseColor_button;
	Button	pancamDCCalibratedColor_button;
	Button	fullFrame_button;
	Button	subFrame_button;
	Button	downsampled_button;
	
	public SlideshowSettingsDialog(Shell parentShell, boolean newSlideshow, ViewerSettings slideshowSettings) {
		super(parentShell);
		this.newSlideshow = newSlideshow;
		if (slideshowSettings != null) {
			this.settings = slideshowSettings;			
		}
		else {
			this.settings = new ViewerSettings("1");
		}
	}
	
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("New Slideshow");
	}
	
	protected Control createDialogArea(Composite parent) {
		GridData gridData;
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        composite.setLayout(layout);
        
        Composite rover_composite = new Composite(composite, SWT.NONE);        
        rover_composite.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        rover_composite.setLayout(new GridLayout(6, false));
        
        Label rover_label = new Label(rover_composite, SWT.NONE);
        rover_label.setText("Rover:");
        rover_label.setLayoutData(new GridData(GridData.BEGINNING,
                GridData.CENTER, false, false));
        
        rover_combo = new Combo(rover_composite, SWT.READ_ONLY);
		gridData = new GridData(GridData.FILL, GridData.FILL, false,
				false);
		gridData.widthHint = convertHeightInCharsToPixels(6);
        rover_combo.setLayoutData(gridData);
        rover_combo.setItems(new String[] {"Opportunity", "Spirit"});
        
        Label fromSol_label = new Label(rover_composite, SWT.NONE);
        fromSol_label.setText("from sol:");
        
        fromSol_text = new Text(rover_composite, SWT.BORDER);
		gridData = new GridData(GridData.FILL, GridData.FILL, false,
				false);
		gridData.widthHint = convertHeightInCharsToPixels(6);
		fromSol_text.setLayoutData(gridData);
        
        Label toSol_label = new Label(rover_composite, SWT.NONE);
        toSol_label.setText("to sol:");
        
        toSol_text = new Text(rover_composite, SWT.BORDER);
		gridData = new GridData(GridData.FILL, GridData.FILL, false,
				false);
		gridData.widthHint = convertHeightInCharsToPixels(6);
		toSol_text.setLayoutData(gridData);

        Group cameras_group = new Group(composite, SWT.NONE);
        cameras_group.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        cameras_group.setText("Cameras");
        cameras_group.setLayout(new GridLayout(5, true));
                
        forwardHazcam_button = new Button(cameras_group, SWT.CHECK);
        forwardHazcam_button.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        forwardHazcam_button.setText("Forward Hazcam");
        
        rearHazcam_button = new Button(cameras_group, SWT.CHECK);
        rearHazcam_button.setText("Rear Hazcam");
        
        navcam_button = new Button(cameras_group, SWT.CHECK);
        navcam_button.setText("Navcam");
        
        pancam_button = new Button(cameras_group, SWT.CHECK);
        pancam_button.setText("Pancam");
        
        microImager_button = new Button(cameras_group, SWT.CHECK);
        microImager_button.setText("Micro Imager");
        
        Group imageType_group = new Group(composite, SWT.NONE);
        imageType_group.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        imageType_group.setText("Navcam/Hazcam Image Types");
        imageType_group.setLayout(new GridLayout(5, true));
        
        leftRaw_button= new Button(imageType_group, SWT.CHECK);
        leftRaw_button.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        leftRaw_button.setText("Left Raw JPG");
        
        rightRaw_button = new Button(imageType_group, SWT.CHECK);
        rightRaw_button.setText("Right Raw JPG");
        
        mmbAnaglyph_button = new Button(imageType_group, SWT.CHECK);
        mmbAnaglyph_button.setText("MMB Anaglyph");
        
        Group pancamImageType_group = new Group(composite, SWT.NONE);
        pancamImageType_group.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        pancamImageType_group.setText("Pancam Image Types");
        pancamImageType_group.setLayout(new GridLayout(5, true));
        
        pancamLeftRaw_button = new Button(pancamImageType_group, SWT.CHECK);
        pancamLeftRaw_button.setText("Left Raw JPG");
        
        pancamRightRaw_button = new Button(pancamImageType_group, SWT.CHECK);
        pancamRightRaw_button.setText("Right Raw JPG");
        
        pancamMMBAnaglyph_button = new Button(pancamImageType_group, SWT.CHECK);
        pancamMMBAnaglyph_button.setText("MMB Anaglyph");
        
        pancamMMBFalseColor_button = new Button(pancamImageType_group, SWT.CHECK);
        pancamMMBFalseColor_button.setText("MMB False-Color");
        
        pancamDCCalibratedColor_button = new Button(pancamImageType_group, SWT.CHECK);
        pancamDCCalibratedColor_button.setText("DC Calibrated Color");
        
        Group imageSize_group = new Group(composite, SWT.NONE);
        imageSize_group.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        imageSize_group.setText("Image Size/Framing");
        imageSize_group.setLayout(new GridLayout(5, true));
        
        fullFrame_button = new Button(imageSize_group, SWT.CHECK);
        fullFrame_button.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        fullFrame_button.setText("Full-frame");
        
        subFrame_button = new Button(imageSize_group, SWT.CHECK);
        subFrame_button.setText("Sub-frame");
        
        downsampled_button = new Button(imageSize_group, SWT.CHECK);
        downsampled_button.setText("Downsampled");
        
        initContents();
        
        return composite;
	}
	
	private void initContents() {
		if (settings != null) {
			if (settings.roverCode.equals(MerUtils.ROVERCODE_OPPORTUNITY)) {
				rover_combo.select(0);
			}
			else if (settings.roverCode.equals(MerUtils.ROVERCODE_SPIRIT)) {
				rover_combo.select(1);
			}
			fromSol_text.setText(""+settings.fromSol);
			toSol_text.setText(""+settings.toSol);
			forwardHazcam_button.setSelection(settings.f);
			rearHazcam_button.setSelection(settings.r);
			navcam_button.setSelection(settings.n);
			pancam_button.setSelection(settings.p);
			microImager_button.setSelection(settings.m);
			pancamMMBFalseColor_button.setSelection(settings.pancamMMBFalseColor);
			pancamDCCalibratedColor_button.setSelection(settings.pancamDCCalibratedColor);
			fullFrame_button.setSelection(settings.fullFrame);
			subFrame_button.setSelection(settings.subFrame);
			downsampled_button.setSelection(settings.downsampled);			
		}
	}

	protected void okPressed() {
		settings = new ViewerSettings("1");
		
		int roverIndex = rover_combo.getSelectionIndex();
		switch (roverIndex) {
		case 0:
			settings.roverCode = MerUtils.ROVERCODE_OPPORTUNITY;
			break;
		case 1:
			settings.roverCode = MerUtils.ROVERCODE_SPIRIT;
			break;				
		}
		settings.fromSol = Integer.parseInt(fromSol_text.getText());
		settings.toSol = Integer.parseInt(toSol_text.getText());
		settings.f = forwardHazcam_button.getSelection();
		settings.r = rearHazcam_button.getSelection();
		settings.n = navcam_button.getSelection();
		settings.p = pancam_button.getSelection();
		settings.m = microImager_button.getSelection();
		settings.pancamMMBFalseColor = pancamMMBFalseColor_button.getSelection();
		settings.pancamDCCalibratedColor = pancamDCCalibratedColor_button.getSelection();
		settings.fullFrame = fullFrame_button.getSelection();
		settings.subFrame = subFrame_button.getSelection();
		settings.downsampled = downsampled_button.getSelection();		
		
		super.okPressed();
	}
	

	public ViewerSettings getSettings() {
		return settings;
	}

	
}
