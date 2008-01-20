package midnightmarsbrowser.views;


import midnightmarsbrowser.application.Application;
import midnightmarsbrowser.metadata.ImageStretchMetadataEntry;
import midnightmarsbrowser.model.ImageEntry;
import midnightmarsbrowser.model.TimeInterval;

import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.SWT;

/**
 */
public class ImageDestretchView extends MMBViewBase {
	public final static String ID = "midnightmarsbrowser.views.ImageDestretchView";
	
	Label	min_label;
	Text	min_text;
	Button	minDown_button;
	Button	minUp_button;
	
	Label	max_label;
	Text	max_text;
	Button	maxDown_button;
	Button	maxUp_button;

	Label	a_label;
	Text	a_text;
	Button	aDown_button;
	Button	aUp_button;
	
	Label	b_label;
	Text	b_text;
	Button	bDown_button;
	Button	bUp_button;
	
	Button 	apply_button;
	
	private static final int buttonIncValue = 5;
	boolean dirty = false;
	
	/**
	 */
	public void createPartControl(Composite parent) {
		
		ModifyListener modifyListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (!isInNotification()) {
					dirty = true;
				}
			}
		};
		
		FocusListener focusListener = new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				updateValuesFromControls();
			}
		};
		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		
        Group group1 = new Group(composite, SWT.NONE);
        group1.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
//        destretch_group.setText("Destretch");
        group1.setLayout(new GridLayout(4, false));		
		
        min_label = new Label(group1, SWT.NONE);
        min_label.setText("Min");
        min_text = new Text(group1, SWT.BORDER);
        min_text.addModifyListener(modifyListener);
        min_text.addFocusListener(focusListener);
        minDown_button = new Button(group1, SWT.PUSH);
        minDown_button.setText("<");
        minDown_button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleChangeButton(min_text, -buttonIncValue, 0);
			}        	
        });
        minUp_button = new Button(group1, SWT.PUSH);
        minUp_button.setText(">");
        minUp_button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleChangeButton(min_text, buttonIncValue, 0);
			}
        });
        
        max_label = new Label(group1, SWT.NONE);
        max_label.setText("Max");        
        max_text = new Text(group1, SWT.BORDER);
        max_text.addModifyListener(modifyListener);
        max_text.addFocusListener(focusListener);
        maxDown_button = new Button(group1, SWT.PUSH);
        maxDown_button.setText("<");
        maxDown_button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleChangeButton(max_text, -buttonIncValue, 255);
			}        	
        });
        maxUp_button = new Button(group1, SWT.PUSH);
        maxUp_button.setText(">");
        maxUp_button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleChangeButton(max_text, buttonIncValue, 255);
			}        	
        });
        		
        a_label = new Label(group1, SWT.NONE);
        a_label.setText("Devig A");
        a_text = new Text(group1, SWT.BORDER); 
        a_text.addModifyListener(modifyListener);
        a_text.addFocusListener(focusListener);        
        aDown_button = new Button(group1, SWT.PUSH);
        aDown_button.setText("<");
        aDown_button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleChangeButton(a_text, -buttonIncValue, 0);
			}        	
        });
        aUp_button = new Button(group1, SWT.PUSH);
        aUp_button.setText(">");
        aUp_button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleChangeButton(a_text, buttonIncValue, 0);
			}        	
        });
        
        b_label = new Label(group1, SWT.NONE);
        b_label.setText("Devig B");
        b_text = new Text(group1, SWT.BORDER);        
        b_text.addModifyListener(modifyListener);
        b_text.addFocusListener(focusListener);        
        bDown_button = new Button(group1, SWT.PUSH);
        bDown_button.setText("<");
        bDown_button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleChangeButton(b_text, -buttonIncValue, 0);
			}        	
        });
        bUp_button = new Button(group1, SWT.PUSH);
        bUp_button.setText(">");
        bUp_button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleChangeButton(b_text, buttonIncValue, 0);
			}        	
        });
        
        apply_button = new Button(composite, SWT.PUSH);
        apply_button.setText("Apply");
        apply_button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				applyValuesFromControls();
			}
        });
        
		super.createPartControl(parent);
	}

	private void handleChangeButton(Text text, int inc, int defaultVal) {
		int val = defaultVal;
		try {
			val = Integer.parseInt(text.getText());
		}
		catch (Exception e) {			
		}
		val = val + inc;
		text.setText(""+val);
		applyValuesFromControls();
	}
	
	public void dispose() {
		super.dispose();
	}

	void notificationCurrentEditorChanged() {
		if (currentEditor != null) {
		}
	}

	void notificationImageSelectionChanged(ImageEntry newImage) {
		if (newImage != null) {
			ImageEntry imageListEntry = currentEditor.getSelectedImage();
			ImageStretchMetadataEntry imageStretchEntry = 
				Application.getWorkspace().getImageStretchMetadata().getEntry(imageListEntry.getFilename());
			if (imageStretchEntry != null) {
				int min = imageStretchEntry.minVal;
				int max = imageStretchEntry.maxVal;
				int devigA = imageStretchEntry.devigA;
				int devigB = imageStretchEntry.devigB;
				min_text.setText(""+min);
				max_text.setText(""+max);
				a_text.setText(""+devigA);
				b_text.setText(""+devigB);
			}
			else {
				min_text.setText("");
				max_text.setText("");
				a_text.setText("");
				b_text.setText("");
			}
		}
	}
	
	void notificationTimeIntervalSelectionChanged(TimeInterval newTimeInterval) {
	}

	public void setFocus() {		
	}

	public void widgetDefaultSelected(SelectionEvent e) {
	}
	
	private void updateValuesFromControls() {
		if (dirty) {
			dirty = false;
			applyValuesFromControls();
		}
	}

	private void applyValuesFromControls() {
		ImageEntry imageListEntry = currentEditor.getSelectedImage();
		if (imageListEntry != null) {
			ImageStretchMetadataEntry imageStretchEntry = 
				Application.getWorkspace().getImageStretchMetadata().getEntry(imageListEntry.getFilename());
			if (imageStretchEntry == null) {
				imageStretchEntry = new ImageStretchMetadataEntry(imageListEntry.getFilename());
				Application.getWorkspace().getImageStretchMetadata().addEntry(imageStretchEntry);
			}
			try {
				imageStretchEntry.minVal = Integer.parseInt(min_text.getText());
			}
			catch (Exception e) {				
			}
			try {
				imageStretchEntry.maxVal = Integer.parseInt(max_text.getText());
			}
			catch (Exception e) {
			}
			try {
				imageStretchEntry.devigA = Integer.parseInt(a_text.getText());
			}
			catch (Exception e) {
			}
			try {
				imageStretchEntry.devigB = Integer.parseInt(b_text.getText());
			}
			catch (Exception e) {
			}
			currentEditor.reloadImage(imageListEntry);
		}
	}
}