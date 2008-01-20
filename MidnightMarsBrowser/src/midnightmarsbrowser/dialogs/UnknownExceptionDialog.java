package midnightmarsbrowser.dialogs;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog used to show exception stack trace when there is an unexpected exception.
 */
public class UnknownExceptionDialog extends Dialog {

	private Text detailsText;
		
	private String message;
	
	private Throwable th;
	
	public static void openDialog(Shell parentShell, String message, Throwable th) {
		UnknownExceptionDialog dialog = new UnknownExceptionDialog(parentShell, message, th);		
		dialog.open();
	}
	
	public UnknownExceptionDialog(Shell parentShell, String message, Throwable th) {
		super(parentShell);
		this.message = message;
		this.th = th;
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Unexpected Error");
	}

	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);

		if (message != null) {
			Label message_text = new Label(composite, SWT.READ_ONLY);
			message_text.setLayoutData(new GridData(GridData.FILL,
				GridData.CENTER, true, false) );
			message_text.setText(message);
		}
		
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		th.printStackTrace(printWriter);
		printWriter.close();
		
		detailsText = new Text(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY);
		GridData gridData = new GridData(GridData.CENTER, GridData.CENTER, true,
				false);
		gridData.widthHint = convertHeightInCharsToPixels(50);
		gridData.heightHint = convertHeightInCharsToPixels(30);
		detailsText.setLayoutData(gridData);
		detailsText.setText(stringWriter.toString());
		
		return composite;
	}
	
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}

	protected void okPressed() {
		super.okPressed();
	}	
}
