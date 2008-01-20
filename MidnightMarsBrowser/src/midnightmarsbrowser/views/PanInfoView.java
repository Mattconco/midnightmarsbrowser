package midnightmarsbrowser.views;

import midnightmarsbrowser.editors.PanoramaCanvas;
import midnightmarsbrowser.editors.ViewerEditor;
import midnightmarsbrowser.model.TimeInterval;
import midnightmarsbrowser.model.MerUtils;
import midnightmarsbrowser.model.ViewerSettings;

import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.SWT;

/**
 */
public class PanInfoView extends MMBViewBase {
	public final static String ID = "midnightmarsbrowser.views.PanInfoView";

	private Text locationCode_text;

	private Text site_text;
	
	private Text drive_text;

	private Text viewOrientation_text;

	private Text viewAzimuth_text;

	private Text viewElevation_text;

	private Text viewVFOV_text;

	private Text viewHFOV_text;

	private Text sols_text;

	private Text description_text;

	private Text resolution_text;

	private Text megapixels_text;

	private Text megabytes_text;

	/**
	 */
	public void createPartControl(Composite parent) {
		ScrolledComposite sc = new ScrolledComposite(parent, SWT.V_SCROLL
				| SWT.H_SCROLL);

		Composite composite = new Composite(sc, SWT.NONE);
		composite.setLayout(new GridLayout());

		Composite view_composite = new Composite(composite, SWT.NONE);
		view_composite.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,
				true, false));
		view_composite.setLayout(new GridLayout(2, false));

		Label location_label = new Label(view_composite, SWT.NONE);
		location_label.setText("Location");
		locationCode_text = new Text(view_composite, SWT.READ_ONLY);
		locationCode_text.setLayoutData(new GridData(GridData.FILL,
				GridData.CENTER, true, false));

		Label site_label = new Label(view_composite, SWT.NONE);
		site_label.setText("Site");
		site_text = new Text(view_composite, SWT.READ_ONLY);
		site_text.setLayoutData(new GridData(GridData.FILL,
				GridData.CENTER, true, false));

		Label drive_label = new Label(view_composite, SWT.NONE);
		drive_label.setText("Drive");
		drive_text = new Text(view_composite, SWT.READ_ONLY);
		drive_text.setLayoutData(new GridData(GridData.FILL,
				GridData.CENTER, true, false));
		
		Label viewOrientation_label = new Label(view_composite, SWT.NONE);
		viewOrientation_label.setText("Orientation  ");
		viewOrientation_text = new Text(view_composite, SWT.READ_ONLY);
		viewOrientation_text.setLayoutData(new GridData(GridData.FILL,
				GridData.CENTER, true, false));

		Label viewAzimuth_label = new Label(view_composite, SWT.NONE);
		viewAzimuth_label.setText("Azimuth");
		viewAzimuth_text = new Text(view_composite, SWT.READ_ONLY);
		viewAzimuth_text.setLayoutData(new GridData(GridData.FILL,
				GridData.CENTER, true, false));

		Label viewElevation_label = new Label(view_composite, SWT.NONE);
		viewElevation_label.setText("Elevation");
		viewElevation_text = new Text(view_composite, SWT.READ_ONLY);
		viewElevation_text.setLayoutData(new GridData(GridData.FILL,
				GridData.CENTER, true, false));

		Label viewHFOV_label = new Label(view_composite, SWT.NONE);
		viewHFOV_label.setText("Hor. FOV");
		viewHFOV_text = new Text(view_composite, SWT.READ_ONLY);
		viewHFOV_text.setLayoutData(new GridData(GridData.FILL,
				GridData.CENTER, true, false));

		Label viewVFOV_label = new Label(view_composite, SWT.NONE);
		viewVFOV_label.setText("Vert. FOV");
		viewVFOV_text = new Text(view_composite, SWT.READ_ONLY);
		viewVFOV_text.setLayoutData(new GridData(GridData.FILL,
				GridData.CENTER, true, false));

		Label sols_label = new Label(view_composite, SWT.NONE);
		sols_label.setText("Sols");
		sols_text = new Text(view_composite, SWT.READ_ONLY);
		sols_text.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,
				true, false));

		Label description_label = new Label(view_composite, SWT.NONE);
		description_label.setText("Description");
		description_text = new Text(view_composite, SWT.READ_ONLY);
		description_text.setLayoutData(new GridData(GridData.FILL,
				GridData.CENTER, true, false));

		Label resolution_label = new Label(view_composite, SWT.NONE);
		resolution_label.setText("Resolution");
		resolution_text = new Text(view_composite, SWT.READ_ONLY);
		resolution_text.setLayoutData(new GridData(GridData.FILL,
				GridData.CENTER, true, false));

		Label megapixels_label = new Label(view_composite, SWT.NONE);
		megapixels_label.setText("Megapixels");
		megapixels_text = new Text(view_composite, SWT.READ_ONLY);
		megapixels_text.setLayoutData(new GridData(GridData.FILL,
				GridData.CENTER, true, false));

		Label megabytes_label = new Label(view_composite, SWT.NONE);
		megabytes_label.setText("Memory");
		megabytes_text = new Text(view_composite, SWT.READ_ONLY);
		megabytes_text.setLayoutData(new GridData(GridData.FILL,
				GridData.CENTER, true, false));

		sc.setContent(composite);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		sc.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		updateDirectionFields();
		updateSettingsFields();
		updateLocationFields();

		super.createPartControl(parent);
	}

	public void dispose() {
		super.dispose();
	}

	void notificationCurrentEditorChanged() {
		updateDirectionFields();
		updateSettingsFields();
		updateLocationFields();
	}

	void notificationDirectionChanged() {
		updateDirectionFields();
	}

	void notificationTimeIntervalSelectionChanged(TimeInterval newTimeInterval) {
		updateLocationFields();
	}

	private void updateDirectionFields() {
		if ((currentEditor != null) && (currentEditor instanceof ViewerEditor)) {
			ViewerEditor editor = (ViewerEditor) currentEditor;
			PanoramaCanvas panoramaCanvas = editor.getPanoramaCanvas();
			if (panoramaCanvas != null) {
				float az = panoramaCanvas.getViewAzimuth();
				viewAzimuth_text.setText("" + MerUtils.round(az, 100));
				float el = panoramaCanvas.getViewElevation();
				viewElevation_text.setText("" + MerUtils.round(el, 100));
				float vfov = panoramaCanvas.getViewVFOV();
				viewVFOV_text.setText("" + MerUtils.round(vfov, 100));
				float hfov = panoramaCanvas.getViewHFOV();
				viewHFOV_text.setText("" + MerUtils.round(hfov, 100));
			}
		}
	}

	private void updateSettingsFields() {
		if ((currentEditor != null) && (currentEditor instanceof ViewerEditor)) {
			ViewerEditor editor = (ViewerEditor) currentEditor;
			ViewerSettings settings = editor.getViewerSettings();
			if (settings.panGroundRelative) {
				viewOrientation_text.setText("Ground-relative");
			} else {
				viewOrientation_text.setText("Rover-relative");
			}
		}
	}

	private void updateLocationFields() {
		if ((currentEditor != null) && (currentEditor instanceof ViewerEditor)) {
			ViewerEditor editor = (ViewerEditor) currentEditor;
			PanoramaCanvas panoramaCanvas = editor.getPanoramaCanvas();
			TimeInterval locationEntry = editor.getSelectedTimeInterval();
			if ((panoramaCanvas != null)
					&& (locationEntry != null)) {
				locationCode_text.setText(locationEntry.getStartLocation().siteDriveCode);
				site_text.setText(""+locationEntry.getStartLocation().site);
				drive_text.setText(""+locationEntry.getStartLocation().drive);				
				sols_text.setText(locationEntry.getSolsString());
				if (locationEntry.getDescription() != null) {
					description_text.setText(locationEntry.getDescription());
				} else {
					description_text.setText("");
				}
				String resolutionStr = 
					resolutionString(panoramaCanvas.getCurrentResolution());
				if (panoramaCanvas.getMaxResolution() != panoramaCanvas
						.getCurrentResolution()) {
					resolutionStr = resolutionStr + " (reduced from "
							+ resolutionString(panoramaCanvas.getMaxResolution()) + ")";
				}
				resolution_text.setText(resolutionStr);

				double megapixels = ((double) panoramaCanvas.getPixelCount()) / 1000000;
				double fullMegapixels = ((double) panoramaCanvas
						.getFullPixelCount()) / 1000000;
				String megaPixelsStr = "" + MerUtils.round(megapixels, 100);
				if (megapixels != fullMegapixels) {
					megaPixelsStr = megaPixelsStr + " (reduced from "
							+ MerUtils.round(fullMegapixels, 100) + ")";
				}
				megapixels_text.setText(megaPixelsStr);

				double megabytes = ((double) panoramaCanvas.getByteCount()) / 1000000;
				double fullMegabytes = ((double) panoramaCanvas
						.getFullByteCount()) / 1000000;
				String megabytesStr = "" + MerUtils.round(megabytes, 100)
						+ "Mb";
				if (megabytes != fullMegabytes) {
					megabytesStr = megabytesStr + " (reduced from "
							+ MerUtils.round(fullMegabytes, 100) + ")";
				}
				megabytes_text.setText(megabytesStr);
			}
		}
	}
	
	private String resolutionString(int resolution) {
		if (resolution == 1024) {
			return "Full";			
		}
		else if (resolution == 512) {
			return "Half";
		}
		else if (resolution == 256) {
			return "Quarter";
		}
		else {
			return ""+resolution;
		}
	} 

	public void setFocus() {
	}
}
