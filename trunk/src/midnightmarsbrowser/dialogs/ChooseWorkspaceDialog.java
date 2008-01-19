package midnightmarsbrowser.dialogs;

import java.io.File;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Login dialog, which prompts for the user's account info, and has Login and
 * Cancel buttons.
 */
public class ChooseWorkspaceDialog extends Dialog {

	private Text directoryText;
	
	private File directory = null;
	
	private String lastWorkspace;
	
	private String message;
	
	private boolean restart;

	public ChooseWorkspaceDialog(Shell parentShell, String lastWorkspace, String message, 
			boolean restart) {
		super(parentShell);
		this.lastWorkspace = lastWorkspace;
		this.message = message;
		this.restart = restart;
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Choose MMB Workspace");
	}

	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		composite.setLayout(layout);

		if (message != null) {
			Label message_text = new Label(composite, SWT.READ_ONLY);
			message_text.setLayoutData(new GridData(GridData.FILL,
				GridData.CENTER, true, false, 3, 1) );
			message_text.setText(message);
		}
		
		Label accountLabel = new Label(composite, SWT.NONE);
		accountLabel.setText("Choose workspace location:");
		accountLabel.setLayoutData(new GridData(GridData.BEGINNING,
				GridData.CENTER, false, false, 3, 1));

		Label userIdLabel = new Label(composite, SWT.NONE);
		userIdLabel.setText("&Directory:");
		userIdLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER,
				false, false));

		directoryText = new Text(composite, SWT.BORDER);
		GridData gridData = new GridData(GridData.FILL, GridData.FILL, true,
				false);
		gridData.widthHint = convertHeightInCharsToPixels(30);
		directoryText.setLayoutData(gridData);
		
		// TODO remove
		directoryText.setText(lastWorkspace);

		Button chooseButton = new Button(composite, SWT.PUSH);
		chooseButton.setText("Choose...");
		chooseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Button button = (Button) e.getSource();
				DirectoryDialog directoryDialog = new DirectoryDialog(button.getShell());
				String newDir = directoryDialog.open();
				if (newDir != null) {
					directoryText.setText(newDir);
				}
			}
		});
		
		return composite;
	}

	
	
	protected void createButtonsForButtonBar(Composite parent) {
		String okayLabel = IDialogConstants.OK_LABEL;
		if (restart) {
			okayLabel = "Restart";
		}
		createButton(parent, IDialogConstants.OK_ID, okayLabel, true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	protected void okPressed() {
		String directoryString = directoryText.getText();
		if (directoryString.equals("")) {
			MessageDialog.openError(getShell(), "Invalid Directory",
					"Workspace directory field must not be blank.");
			return;
		}
		File path = new File(directoryString);
		if (!path.isDirectory()) {
			MessageDialog.openError(getShell(),"Invalid Directory", "Chosen workspace directory does not exist.");
			return;
		}
		directory = path;
		super.okPressed();
	}

	public String getDirectory() {
		return directory.getAbsolutePath();
	}
	
	
}
