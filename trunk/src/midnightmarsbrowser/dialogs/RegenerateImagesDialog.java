package midnightmarsbrowser.dialogs;

import midnightmarsbrowser.application.UpdateParams;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class RegenerateImagesDialog extends Dialog {
	
	UpdateParams params = new UpdateParams();
	
	private Combo rover_combo;

	private Text fromSol_text;

	private Text toSol_text;
	
	private Button generateFACheckBox;
	
	private Button generateRACheckBox;
	
	private Button generateNACheckBox;
	
	private Button generatePACheckBox;
	
	private Button generatePCCheckBox;
	
	private Button forceGenerateAll_button;
	
	public RegenerateImagesDialog(Shell parentShell) {
		super(parentShell);
	}
	
	public UpdateParams getParams() {
		return params;
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Regenerate Images");
	}
	
	protected Control createDialogArea(Composite parent) {
		GridData gridData;
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        composite.setLayout(layout);
        
        Composite top_composite = new Composite(composite, SWT.NONE);        
        top_composite.setLayout(new GridLayout(6, false));
        
		Label rover_label = new Label(top_composite, SWT.NONE);
		rover_label.setText("Generate images for");
		rover_combo = new Combo(top_composite, SWT.READ_ONLY);
		rover_combo.setItems(new String[] {"both rovers", "Opportunity","Spirit"});
		
		Label fromSol_label = new Label(top_composite, SWT.BORDER);
		fromSol_label.setText(" from Sol ");
		fromSol_text = new Text(top_composite, SWT.NONE);
		gridData = new GridData(GridData.FILL, GridData.CENTER, true,
				false);
		gridData.widthHint = convertHeightInCharsToPixels(4);
		fromSol_text.setLayoutData(gridData);
		
		Label toSol_label = new Label(top_composite, SWT.BORDER);
		toSol_label.setText(" to Sol ");
		toSol_text = new Text(top_composite, SWT.NONE);
		gridData = new GridData(GridData.FILL, GridData.CENTER, true,
				false);
		gridData.widthHint = convertHeightInCharsToPixels(4);
		toSol_text.setLayoutData(gridData);
		
        Composite options_composite = new Composite(composite, SWT.NONE);        
        options_composite.setLayout(new GridLayout(2, true));
        
        generateFACheckBox = new Button(options_composite, SWT.CHECK);
        generateFACheckBox.setText("Generate forward hazcam anaglyphs");
        
        generateRACheckBox = new Button(options_composite, SWT.CHECK);
        generateRACheckBox.setText("Generate rear hazcam anaglyphs");
        
        generateNACheckBox = new Button(options_composite, SWT.CHECK);
        generateNACheckBox.setText("Generate navcam anaglyphs");
        
        generatePACheckBox = new Button(options_composite, SWT.CHECK);
        generatePACheckBox.setText("Generate pancam anaglyphs");
		
        generatePCCheckBox = new Button(options_composite, SWT.CHECK);
        generatePCCheckBox.setText("Generate pancam false-color");
		
        forceGenerateAll_button = new Button(composite, SWT.CHECK);
        forceGenerateAll_button.setText("Force generate all images");
        
		setControlsFromParams();
        
        return composite;
	}	

	private void setControlsFromParams() {
		if (params.updateOpportunity && params.updateSpirit) {
			rover_combo.select(0);
		}
		else if (params.updateOpportunity) {
			rover_combo.select(1);
		}
		else if (params.updateSpirit) {
			rover_combo.select(1);
		}
		fromSol_text.setText(Integer.toString(params.startSol));
		toSol_text.setText(Integer.toString(params.endSol));
		
		generateFACheckBox.setSelection(params.generateFA);
		generateRACheckBox.setSelection(params.generateRA);
		generateNACheckBox.setSelection(params.generateNA);
		generatePACheckBox.setSelection(params.generatePA);
		generatePCCheckBox.setSelection(params.generatePC);
		
		if (params.generateImagesAll) {
			forceGenerateAll_button.setSelection(true);
		}
		else if (params.generateImagesCheckAll) {
			forceGenerateAll_button.setSelection(false);
		}
		else {
			forceGenerateAll_button.setSelection(false);
		}
	}

	private void setParamsFromControls() throws Exception {
		params.downloadMode = UpdateParams.DOWNLOAD_NONE;
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
		
		params.generateFA = generateFACheckBox.getSelection();
		params.generateRA = generateRACheckBox.getSelection();
		params.generateNA = generateNACheckBox.getSelection();
		params.generatePA = generatePACheckBox.getSelection();
		params.generatePC = generatePCCheckBox.getSelection();
		
		if (forceGenerateAll_button.getSelection()) {
			params.generateImagesAll = true;
			params.generateImagesCheckAll = false;
		}
		else {
			params.generateImagesAll = false;
			params.generateImagesCheckAll = true;
		}
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
