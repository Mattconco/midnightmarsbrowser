package midnightmarsbrowser.views;

import java.util.ArrayList;

import midnightmarsbrowser.model.ImageEntry;
import midnightmarsbrowser.model.MerUtils;
import midnightmarsbrowser.model.TimeInterval;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PartInitException;

/**
 */
public class ImagesView extends MMBViewBase implements SelectionListener, ICheckStateListener {
	public final static String ID = "midnightmarsbrowser.views.ImagesView";
	
	private CheckboxTableViewer viewer;

	private Label interval_label;
		
	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			return ((TimeInterval) parent).getImageList();
		}
	}

	class ViewLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			ImageEntry entry = (ImageEntry) obj;
			if (index == 0) {
				return entry.getSolString();
			} else if (index == 1) {
				return entry.getFilename();
			}
			else if (index == 2) {
				return entry.observationDescription;
			}
			else {
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
	 * 
	 */
	public void createPartControl(Composite parent) {
		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		
		Composite interval_composite = new Composite(composite, SWT.NONE);
		interval_composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		interval_composite.setLayout(new GridLayout(2, false));
				
		Link interval_link = new Link(interval_composite, SWT.PUSH);
		interval_link.setText("<a>Time Interval</a>: ");
		interval_link.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {
					getSite().getPage().showView(TimeIntervalsView.ID);
				} catch (PartInitException ex) {
					ex.printStackTrace();
				}
			}});
		
		interval_label = new Label(interval_composite, SWT.NONE);
		interval_label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		Table table = new Table(composite, SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL | SWT.CHECK);
		table.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.addSelectionListener(this);
		
		TableColumn column = new TableColumn(table, SWT.CENTER, 0);
		column.setText("Sol");
		column.setWidth(40);
		column = new TableColumn(table, SWT.LEFT, 1);
		column.setText("Image");
		column.setWidth(240);
		column = new TableColumn(table, SWT.LEFT, 2);
		column.setText("Description");
		column.setWidth(500);
		
		
		viewer = new CheckboxTableViewer(table);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.addCheckStateListener(this);
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
			ImageEntry selectedImage = (ImageEntry) selection.getFirstElement();			
			if (selectedImage != null) {
				if (currentEditor != null) {
					currentEditor.setSelectedImage(selectedImage);
				}
			}
		}
	}

	public void checkStateChanged(CheckStateChangedEvent event) {
		if (!isInNotification()) {
			ImageEntry entry = (ImageEntry) event.getElement();
			if (currentEditor != null) {
				currentEditor.setImageEnabled(entry, event.getChecked());
			}
		}
	}

	void notificationCurrentEditorChanged() {
		try {
			if (currentEditor != null && currentEditor.getSelectedTimeInterval() != null) {
				TimeInterval timeInterval = currentEditor.getSelectedTimeInterval();
				
				interval_label.setText(timeInterval.getTimeString());
				
				viewer.setInput(null);
				viewer.setItemCount(0);
				viewer.setInput(timeInterval);
				if (currentEditor.getSelectedImage() != null)
					viewer.setSelection(new StructuredSelection(currentEditor.getSelectedImage()), true);
				else
					viewer.setSelection(null);
				
				if (timeInterval != null) {
					ArrayList checkedList = new ArrayList();
					ImageEntry[] entries = timeInterval.getImageList();
					for (int n=0; n<entries.length; n++) {
						ImageEntry entry = entries[n];
						if (entry.enabled) {
							checkedList.add(entry);
						}
					}
					ImageEntry[] checked = new ImageEntry[checkedList.size()];
					checked = (ImageEntry[]) checkedList.toArray(checked);
					viewer.setCheckedElements(checked);
				}
			}
			else {
				viewer.setInput(null);
			}
		}
		catch (Exception e) {
			// TODO
			e.printStackTrace();
		}
	}

	void notificationImageSelectionChanged(ImageEntry newImage) {
		try {
			StructuredSelection selection = (StructuredSelection) viewer.getSelection();
			ImageEntry selectedImage = (ImageEntry) selection.getFirstElement();			
			if (selectedImage != newImage) {
				viewer.setSelection(new StructuredSelection(newImage), true);
			}
		}
		catch (Exception e) {
			// TODO
			e.printStackTrace();
		}
	}

	void notificationTimeIntervalSelectionChanged(TimeInterval newTimeInterval) {
		notificationCurrentEditorChanged();		
	}
}