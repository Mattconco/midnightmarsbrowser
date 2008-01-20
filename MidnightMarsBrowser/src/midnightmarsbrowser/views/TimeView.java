package midnightmarsbrowser.views;


import java.util.Date;

import midnightmarsbrowser.model.MerUtils;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.SWT;


/**
 */

public class TimeView extends ViewPart {
	public final static String ID = "midnightmarsbrowser.views.TimeView";
	
	private TableViewer viewer;

	TimeTask timeTask;
	
	class TimeEntry {
		String title = null;

		String value = null;

		TimeEntry(String title) {
			this.title = title;
		}
	}

	class TimeTask implements Runnable {
		TimeView view;

		boolean done = false;

		TimeTask(TimeView view) {
			this.view = view;
		}

		public void run() {
			int[] marsTime = new int[4];
			while (!done) {
				MerUtils.marsTimeFromTimeMillis("1",
						System.currentTimeMillis(), marsTime);
				view.opportunityTime.value = timeString(marsTime);
				MerUtils.marsTimeFromTimeMillis("2",
						System.currentTimeMillis(), marsTime);
				view.spiritTime.value = timeString(marsTime);
				view.earthTime.value = (new Date()).toString();
				
				if (!done) {
					Display display = view.getSite().getShell().getDisplay();
					display.syncExec(new Runnable() {
						public void run() {
							if (!view.viewer.getControl().isDisposed()) {
								view.viewer.refresh();
							}
						}
					});
				}
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		private String timeString(int[] marsTime) {
			StringBuffer buf = new StringBuffer();
			buf.append("Sol ");
			buf.append(marsTime[0]);
			buf.append(" ");
			buf.append(MerUtils.zeroPad(marsTime[1], 2));
			buf.append(":");
			buf.append(MerUtils.zeroPad(marsTime[2], 2));
			buf.append(":");
			buf.append(MerUtils.zeroPad(marsTime[3], 2));
			return buf.toString();
		}
	}

	TimeEntry opportunityTime = new TimeEntry(
			"1 MER-B Opportunity Current Time");

	TimeEntry spiritTime = new TimeEntry("2 MER-A Spirit Current Time");
	
	TimeEntry earthTime = new TimeEntry("Local Earth Time");
	
	TimeEntry[] timeEntries = new TimeEntry[] {opportunityTime, spiritTime, earthTime};

	/**
	 * The content provider class is responsible for providing objects to the
	 * view. It can wrap existing objects in adapters or simply return objects
	 * as-is. These objects may be sensitive to the current input of the view,
	 * or ignore it and always show the same content (like Task List, for
	 * example).
	 */
	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			return timeEntries;
		}
	}

	class ViewLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			TimeEntry entry = (TimeEntry) obj;
			if (index == 0) {
				return entry.title;
			} else {
				return entry.value;
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
		Table table = new Table(parent, SWT.CENTER);
		table.setLinesVisible(true);
		table.setHeaderVisible(false);
		// 1st column with image/checkboxes - NOTE: The SWT.CENTER has no
		// effect!!
		TableColumn column = new TableColumn(table, SWT.CENTER, 0);
		column.setText("name");
		column.setWidth(250);
		column = new TableColumn(table, SWT.LEFT, 1);
		column.setText("time");
		column.setWidth(250);

		viewer = new TableViewer(table);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setInput(getViewSite());
		
		//getSite().getWorkbenchWindow().getPartService().addPartListener(partListener);

		timeTask = new TimeTask(this);
		Thread thread = new Thread(timeTask);
		thread.start();
	}

	public void dispose() {
		if (timeTask != null) {
			timeTask.done = true;
		}
		//getSite().getWorkbenchWindow().getPartService().removePartListener(partListener);
		super.dispose();
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}