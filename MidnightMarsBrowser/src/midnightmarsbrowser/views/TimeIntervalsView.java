package midnightmarsbrowser.views;

import midnightmarsbrowser.model.ImageEntry;
import midnightmarsbrowser.model.TimeIntervalList;
import midnightmarsbrowser.model.TimeInterval;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.SWT;

/**
 */
public class TimeIntervalsView extends MMBViewBase implements SelectionListener {
	public final static String ID = "midnightmarsbrowser.views.TimeIntervalsView";

	private TableViewer viewer;

	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			return ((TimeIntervalList) parent).getEntries();
		}
	}

	class ViewLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			TimeInterval entry = (TimeInterval) obj;
			if (index == 0) {
				return entry.getStartLocation().siteDriveCode;
			} else if (index == 1) {
				return entry.getSolsString();
			} else if (index == 2) {
				return entry.getDescription();
			} else {
				return null;
			}
		}

		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}

		public Image getImage(Object obj) {
			return null;
			// return PlatformUI.getWorkbench().getSharedImages().getImage(
			// ISharedImages.IMG_OBJ_ELEMENT);
		}
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		Table table = new Table(parent, SWT.CENTER | SWT.FULL_SELECTION);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.addSelectionListener(this);

		TableColumn column = new TableColumn(table, SWT.LEFT, 0);
		column.setText("Location");
		column.setWidth(55);
		column = new TableColumn(table, SWT.LEFT, 1);
		column.setText("Sols");
		column.setWidth(100);
		column = new TableColumn(table, SWT.LEFT, 2);
		column.setText("Description");
		column.setWidth(400);

		viewer = new TableViewer(table);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setInput(null);

		super.createPartControl(parent);
	}

	public void dispose() {
		super.dispose();
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub

	}

	public void widgetSelected(SelectionEvent e) {
		if (!isInNotification()) {
			StructuredSelection selection = (StructuredSelection) viewer.getSelection();
			TimeInterval selectedTimeInterval = (TimeInterval) selection.getFirstElement();
			if (selectedTimeInterval != null) {
				if (currentEditor != null) {
					currentEditor.setSelectedTimeInterval(selectedTimeInterval);
				}
			}
		}
	}

	void notificationCurrentEditorChanged() {
		if (currentEditor != null) {
			TimeIntervalList timeIntervalList = currentEditor.getTimeIntervalList();
			viewer.setInput(null);
			viewer.setInput(timeIntervalList);
			if (timeIntervalList != null) {
				// TODO Why am I doing the next line??
				viewer.setItemCount(timeIntervalList.getEntries().length);
				// TODO is it okay to bypass the viewer like this to set selection?
				viewer.setSelection(new StructuredSelection(currentEditor.getSelectedTimeInterval()), true);
			}
		} else {
			viewer.setInput(null);
		}
	}

	void notificationImageSelectionChanged(ImageEntry newImage) {
	}

	void notificationTimeIntervalSelectionChanged(TimeInterval newTimeInterval) {
		try {
			StructuredSelection selection = (StructuredSelection) viewer.getSelection();
			TimeInterval selectedTimeInterval = (TimeInterval) selection.getFirstElement();
			if (selectedTimeInterval != newTimeInterval) {
				viewer.setSelection(new StructuredSelection(newTimeInterval), true);
			}
		}
		catch (Exception e) {
			// TODO
			e.printStackTrace();
		}
	}
}