package midnightmarsbrowser.dialogs;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import midnightmarsbrowser.application.UpdateParams;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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

public class UpdateDialog extends Dialog {
	
	private static DateFormat dialogDateFormat = null;
	{
		dialogDateFormat = new SimpleDateFormat("MM/dd/yyyy");
		dialogDateFormat.setTimeZone(TimeZone.getTimeZone("PST"));
	}
	
	final static String JPL_PANEL = "JPL";
	final static String EXP_PANEL = "Exploratorium";
	final static String LYLE_PANEL = "lyle.org (calibrated color by Daniel Crotty)";
	final static String PDS_IMG_PANEL = "PDS Imaging Node";
	final static String DEV_PANEL1 = "development 1";
	final static String DEV_PANEL2 = "development 2";

	UpdateParams params = new UpdateParams();
	
	private Combo source_combo;
	private Combo rover_combo;
	private Composite options_composite;
	private StackLayout options_layout;
	private Group jplOptions_group;
	private Text fromSol_text;
	private Text toSol_text;
	private Button forwardHazcam_button;
	private Button rearHazcam_button;
	private Button navcam_button;
	private Button pancam_button;
	private Button micro_button;
	private Button generateImages_button;
	private Button fastUpdate_button;
	private Group explOptions_group;
	private Text expStartDateText;
	private Button expEndDateButton;
	private Text expEndDateText;
	private Button expUpdateStartDateButton;
	private Group update_group;
	
	public UpdateDialog(Shell parentShell) {
		super(parentShell);
	}
	
	public UpdateParams getParams() {
		return params;
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Advanced Update Images");
	}
	
	protected Control createDialogArea(Composite parent) {
		GridData gridData;
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        composite.setLayout(layout);
        
        Composite top_composite = new Composite(composite, SWT.NONE);        
        top_composite.setLayout(new GridLayout(2, false));
        
        Label source_label = new Label(top_composite, SWT.NONE);
        source_label.setText("Update images from");
        source_combo = new Combo(top_composite, SWT.READ_ONLY);
//		gridData = new GridData(GridData.FILL, GridData.CENTER, true,
//				false);
//		source_combo.setLayoutData(gridData);
		source_combo.setItems(new String[] {JPL_PANEL, EXP_PANEL, LYLE_PANEL});
		source_combo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateControls();
			}
		});
		
		Label rover_label = new Label(top_composite, SWT.NONE);
		rover_label.setText("Update images for");
		rover_combo = new Combo(top_composite, SWT.READ_ONLY);
		rover_combo.setItems(new String[] {"both rovers", "Opportunity","Spirit"});		        
		
		options_composite = new Composite(composite, SWT.NONE);
		options_layout = new StackLayout();
		options_composite.setLayout(options_layout);
		gridData = new GridData(GridData.FILL, GridData.CENTER, true,
				false);
		options_composite.setLayoutData(gridData);
		
		jplOptions_group = new Group(options_composite, SWT.NONE);
		jplOptions_group.setText("Update Options");
		jplOptions_group.setLayout(new GridLayout(4, false));
		
		options_layout.topControl = jplOptions_group;
		
		Label fromSol_label = new Label(jplOptions_group, SWT.NONE);
		fromSol_label.setText("From sol");		
		fromSol_text = new Text(jplOptions_group, SWT.BORDER);
		gridData = new GridData(GridData.FILL, GridData.CENTER, true,
				false);
		gridData.widthHint = convertHeightInCharsToPixels(4);
		fromSol_text.setLayoutData(gridData);
		
		Label toSol_label = new Label(jplOptions_group, SWT.NONE);
		toSol_label.setText("to sol");
		toSol_text = new Text(jplOptions_group, SWT.BORDER);
		gridData = new GridData(GridData.FILL, GridData.CENTER, true,
				false);
		gridData.widthHint = convertHeightInCharsToPixels(4);
		toSol_text.setLayoutData(gridData);
		
		explOptions_group = new Group(options_composite, SWT.NONE);
		explOptions_group.setText("Exploratorium Update Options");
		explOptions_group.setLayout(new GridLayout(2, false));
		
		Label expStartDateLabel = new Label(explOptions_group, SWT.NONE);
		expStartDateLabel.setText("Exploratorium download start date");
		expStartDateText = new Text(explOptions_group, SWT.BORDER);
		gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
		expStartDateText.setLayoutData(gridData);
		expEndDateButton = new Button(explOptions_group, SWT.CHECK);
		expEndDateButton.setText("Download End Date");
		expEndDateText = new Text(explOptions_group, SWT.BORDER);
		gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
		expEndDateText.setLayoutData(gridData);
		expUpdateStartDateButton = new Button(explOptions_group, SWT.CHECK);
		expUpdateStartDateButton.setText("Update download start date when done");
		
		update_group = new Group(composite, SWT.NONE);
		update_group.setLayout(new GridLayout(2, true));
		update_group.setText("Update Raw Images");
		gridData = new GridData(GridData.FILL, GridData.CENTER, true,
				false);
		update_group.setLayoutData(gridData);
		
		forwardHazcam_button = new Button(update_group, SWT.CHECK);
		forwardHazcam_button.setText("Forward Hazcam");
		rearHazcam_button = new Button(update_group, SWT.CHECK);
		rearHazcam_button.setText("Rear Hazcam");
		navcam_button = new Button(update_group, SWT.CHECK);
		navcam_button.setText("Navcam");
		pancam_button = new Button(update_group, SWT.CHECK);
		pancam_button.setText("Pancam");
		micro_button = new Button(update_group, SWT.CHECK);
		micro_button.setText("Micro Imager");
		
		generateImages_button = new Button(composite, SWT.CHECK);
		generateImages_button.setText("Generate images");
		
		fastUpdate_button = new Button(composite, SWT.CHECK);
		fastUpdate_button.setText("Fast update (new images only)");
		
		
		setControlsFromParams();
        
        return composite;
	}
	
	private void updateControls() {
		String sourceText = source_combo.getText();
		boolean devPanelEnable = (sourceText.equals(DEV_PANEL1));
	    boolean enableRaw = true;
	    boolean enableGenerated = true;			    
	    boolean enableFastUpdate = true;
	    if (sourceText.equals(LYLE_PANEL)) {
	    		//enableCards = false;
	    		enableRaw = false;
	    		enableGenerated = false;
	    		enableFastUpdate = false;
	    }
	    if (sourceText.equals(PDS_IMG_PANEL)) {
	    	enableGenerated = false;
	    }
	    if (sourceText.equals(DEV_PANEL1) || sourceText.equals(DEV_PANEL2)) {
	    	enableGenerated = false;
	    }
	    if (sourceText.equals(EXP_PANEL)) {
	    	options_layout.topControl = explOptions_group;	    	
	    }
	    else {
	    	options_layout.topControl = jplOptions_group;
	    }
    	options_composite.layout();
		
    	update_group.setEnabled(enableRaw);
		forwardHazcam_button.setEnabled(enableRaw);
		rearHazcam_button.setEnabled(enableRaw);
		navcam_button.setEnabled(enableRaw);
		pancam_button.setEnabled(enableRaw);
		micro_button.setEnabled(enableRaw);
		generateImages_button.setEnabled(enableGenerated);
	    fastUpdate_button.setEnabled(enableFastUpdate);
	    
//	    devPanel.setVisible(devPanelEnable);		
	}
	
	private void setControlsFromParams() {
		source_combo.select(0);
		if (params.updateOpportunity && params.updateSpirit) {
			rover_combo.select(0);
		}
		else if (params.updateOpportunity) {
			rover_combo.select(1);
		}
		else if (params.updateSpirit) {
			rover_combo.select(2);
		}
		forwardHazcam_button.setSelection(params.updateF);
		rearHazcam_button.setSelection(params.updateR);
		navcam_button.setSelection(params.updateN);
		pancam_button.setSelection(params.updateP);
		micro_button.setSelection(params.updateM);
		// JPL
		fromSol_text.setText(Integer.toString(params.startSol));
		toSol_text.setText(Integer.toString(params.endSol));
//		this.imgMetadataOnlyCheckBox.setSelected(params.imgMetadataOnly);
//		if (params.productCode != null)
//			this.imgProductCodeTextField.setText(params.productCode);
		// Expl
		expStartDateText.setText(dialogDateFormat.format(params.downloadStartExplDate));
		expEndDateButton.setSelection(params.downloadEndExplDateFlag);
		if (params.downloadEndExplDate != null) {
			this.expEndDateText.setText(dialogDateFormat.format(params.downloadEndExplDate));
		}
		else {
			this.expEndDateText.setText("");
		}
		// both again
		fastUpdate_button.setSelection(params.fastUpdate);
		generateImages_button.setSelection(params.generateFA || params.generateRA || params.generateNA || params.generatePA || params.generatePC);		
		
		expUpdateStartDateButton.setSelection(params.updateDownloadStartDate);
	}
	
	private void setParamsFromControls() throws Exception {
		switch (rover_combo.getSelectionIndex()) {
			case 0: 
				params.updateOpportunity = true;
				params.updateSpirit = true;
				break;
			case 1:
				params.updateOpportunity = true;
				params.updateSpirit = false;
				break;
			case 2:
				params.updateOpportunity = false;
				params.updateSpirit = true;				
				break;
		}
		params.updateF = forwardHazcam_button.getSelection();
		params.updateR = rearHazcam_button.getSelection();
		params.updateN = navcam_button.getSelection();
		params.updateP = pancam_button.getSelection();
		params.updateM = micro_button.getSelection();
		String selectedSource = (String) source_combo.getText();
		if (selectedSource.equals(EXP_PANEL)) {
			params.downloadMode = UpdateParams.DOWNLOAD_FROM_EXPL;
			params.downloadStartExplDate = dialogDateFormat.parse(expStartDateText.getText().trim());
			params.downloadEndExplDateFlag = this.expEndDateButton.getSelection()
				&& this.expEndDateButton.getSelection();
			if (expEndDateButton.getSelection()) {
				params.downloadEndExplDate = dialogDateFormat.parse(expEndDateText.getText().trim());
				if (params.downloadStartExplDate.after(params.downloadEndExplDate)) {
					throw new Exception("Download start date must be less than or equal to download end date.");
				}
			}
			else {
				params.downloadEndExplDate  = null;
			}
			params.updateDownloadStartDate = expUpdateStartDateButton.getSelection() 
				&& expUpdateStartDateButton.isEnabled();
		}
		else if ( selectedSource.equals(JPL_PANEL)
				|| selectedSource.equals(DEV_PANEL1) 
				|| selectedSource.equals(DEV_PANEL2) 
				|| selectedSource.equals(LYLE_PANEL)
				|| selectedSource.equals(PDS_IMG_PANEL)) {
			if (selectedSource.equals(JPL_PANEL)) {
				params.downloadMode = UpdateParams.DOWNLOAD_FROM_JPL;
			}
			else if (selectedSource.equals(LYLE_PANEL)) {
				params.downloadMode = UpdateParams.DOWNLOAD_FROM_LYLE;
			}
			else if (selectedSource.equals(DEV_PANEL1)) {
				params.mode = UpdateParams.MODE_DEVELOPMENT1;
			}
			else if (selectedSource.equals(DEV_PANEL2)) {
				params.mode = UpdateParams.MODE_DEVELOPMENT2;
			}
			else if (selectedSource.equals(PDS_IMG_PANEL)) {
				params.downloadMode = UpdateParams.DOWNLOAD_FROM_PDS_IMG;
			}
			try {
				params.startSol = Integer.parseInt(fromSol_text.getText().trim());
			}
			catch (Exception e) {
				throw new Exception("\"from sol\" field must be a valid number");
			}
			try {
				params.endSol = Integer.parseInt(toSol_text.getText().trim());
			}
			catch (Exception e) {
				throw new Exception("\"to sol\" field must be a valid number");
			}
			if (params.startSol > params.endSol) 
				throw new Exception("\"from sol\" field must be less than or equal to \"to sol\" field");	
//			params.imgMetadataOnly = this.imgMetadataOnlyCheckBox.isSelected();
//			params.productCode = this.imgProductCodeTextField.getText().trim();
		}
		params.fastUpdate = fastUpdate_button.getSelection();		
		params.generateFA = generateImages_button.getSelection();
		params.generateRA = generateImages_button.getSelection();
		params.generateNA = generateImages_button.getSelection();
		params.generatePA = generateImages_button.getSelection();
		params.generatePC = generateImages_button.getSelection();
		params.generateImagesAll = false;
		params.generateImagesCheckAll = false;
	}

	protected void okPressed() {
		try {
			setParamsFromControls();
		} catch (Exception e) {
			MessageDialog.openError(this.getShell(), "Dialog error", e.getMessage());
			return;
		}
		super.okPressed();
	}
	
}
