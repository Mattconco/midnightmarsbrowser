package midnightmarsbrowser.dialogs;

import midnightmarsbrowser.application.UpdateParams;
import midnightmarsbrowser.model.MerUtils;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
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

public class UpdatePDSImagesDialog extends Dialog {
	
	static UpdateParams params = null;
	
	private Combo rover_combo;
	private Group timeframe_group;
	private Text fromSol_text;
	private Text toSol_text;
	private Button forwardHazcam_button;
	private Button rearHazcam_button;
	private Button navcam_button;
	private Button pancam_button;
	private Button micro_button;
	private Button fastUpdate_button;
	private Group cameras_group;

	private Group options_group;

	private Text filters_text;

	private Text productTypes_text;
	
	public UpdatePDSImagesDialog(Shell parentShell) {
		super(parentShell);
		if (params == null) {
			params = new UpdateParams();
			params.fastUpdate = true;
			params.updateF = false;
			params.updateM = false;
			params.updateN = false;
			params.updateP = false;
			params.updateR = false;		
			params.productTypes = new String[] {"MRD"};
			params.filters = new String[] {"L"};
		}
	}
	
	public UpdateParams getParams() {
		return params;
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Update PDS Images");
	}
	
	protected Control createDialogArea(Composite parent) {
		GridData gridData;
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        composite.setLayout(layout);
        
        Composite top_composite = new Composite(composite, SWT.NONE);        
        top_composite.setLayout(new GridLayout(2, false));
        
		Label rover_label = new Label(top_composite, SWT.NONE);
		rover_label.setText("Update PDS images for");
		rover_combo = new Combo(top_composite, SWT.READ_ONLY);
		rover_combo.setItems(new String[] {"both rovers", "Opportunity","Spirit"});		        
				
		timeframe_group = new Group(composite, SWT.NONE);
		timeframe_group.setText("Timeframe");
		timeframe_group.setLayout(new GridLayout(4, false));
				
		Label fromSol_label = new Label(timeframe_group, SWT.NONE);
		fromSol_label.setText("From sol");		
		fromSol_text = new Text(timeframe_group, SWT.BORDER);
		gridData = new GridData(GridData.FILL, GridData.CENTER, true,
				false);
		gridData.widthHint = convertHeightInCharsToPixels(4);
		fromSol_text.setLayoutData(gridData);
		
		Label toSol_label = new Label(timeframe_group, SWT.NONE);
		toSol_label.setText("to sol");
		toSol_text = new Text(timeframe_group, SWT.BORDER);
		gridData = new GridData(GridData.FILL, GridData.CENTER, true,
				false);
		gridData.widthHint = convertHeightInCharsToPixels(4);
		toSol_text.setLayoutData(gridData);
		
		cameras_group = new Group(composite, SWT.NONE);
		cameras_group.setLayout(new GridLayout(2, true));
		cameras_group.setText("Cameras");
		gridData = new GridData(GridData.FILL, GridData.CENTER, true,
				false);
		cameras_group.setLayoutData(gridData);
		
		forwardHazcam_button = new Button(cameras_group, SWT.CHECK);
		forwardHazcam_button.setText("Forward Hazcam");
		rearHazcam_button = new Button(cameras_group, SWT.CHECK);
		rearHazcam_button.setText("Rear Hazcam");
		navcam_button = new Button(cameras_group, SWT.CHECK);
		navcam_button.setText("Navcam");
		pancam_button = new Button(cameras_group, SWT.CHECK);
		pancam_button.setText("Pancam");
		micro_button = new Button(cameras_group, SWT.CHECK);
		micro_button.setText("Micro Imager");
		
		options_group = new Group(composite, SWT.NONE);
		options_group.setLayout(new GridLayout(2, false));
		options_group.setText("Options");
		gridData = new GridData(GridData.FILL, GridData.CENTER, true,
				false);
		options_group.setLayoutData(gridData);
		
		Label productTypes_label = new Label(options_group, SWT.NONE);
		productTypes_label.setText("Product types:");
		productTypes_text = new Text(options_group, SWT.BORDER);
		gridData = new GridData(GridData.FILL, GridData.CENTER, true,
				false);
		productTypes_text.setLayoutData(gridData);
		
		Label filter_label = new Label(options_group, SWT.NONE);
		filter_label.setText("Filters:");
		filters_text = new Text(options_group, SWT.BORDER);
		gridData = new GridData(GridData.FILL, GridData.CENTER, true,
				false);
		filters_text.setLayoutData(gridData);

		fastUpdate_button = new Button(composite, SWT.CHECK);
		fastUpdate_button.setText("Fast update (new images only)");
		
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
			rover_combo.select(2);
		}
		forwardHazcam_button.setSelection(params.updateF);
		rearHazcam_button.setSelection(params.updateR);
		navcam_button.setSelection(params.updateN);
		pancam_button.setSelection(params.updateP);
		micro_button.setSelection(params.updateM);

		fromSol_text.setText(Integer.toString(params.startSol));
		toSol_text.setText(Integer.toString(params.endSol));
		
		productTypes_text.setText(MerUtils.stringFromArray(params.productTypes));
		filters_text.setText(MerUtils.stringFromArray(params.filters));
		
		fastUpdate_button.setSelection(params.fastUpdate);		
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
		
		params.downloadMode = UpdateParams.DOWNLOAD_FROM_PDS_IMG;
		
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

		params.fastUpdate = fastUpdate_button.getSelection();		
		params.generateFA = false;
		params.generateRA = false;
		params.generateNA = false;
		params.generatePA = false;
		params.generatePC = false;
		params.generateImagesAll = false;
		params.generateImagesCheckAll = false;
		
		params.productTypes = MerUtils.arrayFromString(productTypes_text.getText());
		params.filters = MerUtils.arrayFromString(filters_text.getText());
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
