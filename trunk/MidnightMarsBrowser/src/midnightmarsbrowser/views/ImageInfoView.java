package midnightmarsbrowser.views;

import java.util.Date;

import midnightmarsbrowser.editors.ViewerEditor;
import midnightmarsbrowser.model.ImageEntry;
import midnightmarsbrowser.model.MerUtils;

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
public class ImageInfoView extends MMBViewBase {
	public final static String ID = "midnightmarsbrowser.views.ImageInfoView";

	private Text filename_text;

	private Text type_text;

	private Text spacecraftId_text;

	private Text camera_text;

	private Text spacecraftClock_text;

	private Text acquisitionTimeMars_text;

	private Text acquisitionTimeEarth_text;

	private Text productType_text;

	private Text site_text;

	private Text drive_text;

	private Text cmdSeq_text;

	private Text cameraEye_text;

	private Text cameraFilter_text;

	private Text productProducer_text;

	private Text productVersion_text;

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

		Label filename_label = new Label(view_composite, SWT.NONE);
		filename_label.setText("Filename");
		filename_text = new Text(view_composite, SWT.READ_ONLY);
		filename_text.setLayoutData(new GridData(GridData.FILL,
				GridData.CENTER, true, false));

		Label type_label = new Label(view_composite, SWT.NONE);
		type_label.setText("Type");
		type_text = new Text(view_composite, SWT.READ_ONLY);
		type_text.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,
				true, false));

		Label spacecraftId_label = new Label(view_composite, SWT.NONE);
		spacecraftId_label.setText("Spacecraft ID");
		spacecraftId_text = new Text(view_composite, SWT.READ_ONLY);
		spacecraftId_text.setLayoutData(new GridData(GridData.FILL,
				GridData.CENTER, true, false));

		Label camera_label = new Label(view_composite, SWT.NONE);
		camera_label.setText("Camera");
		camera_text = new Text(view_composite, SWT.READ_ONLY);
		camera_text.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,
				true, false));

		Label spacecraftClock_label = new Label(view_composite, SWT.NONE);
		spacecraftClock_label.setText("Spacecraft clock");
		spacecraftClock_text = new Text(view_composite, SWT.READ_ONLY);
		spacecraftClock_text.setLayoutData(new GridData(GridData.FILL,
				GridData.CENTER, true, false));

		Label acquisitionTimeMars_label = new Label(view_composite, SWT.NONE);
		acquisitionTimeMars_label.setText("Time (Mars)");
		acquisitionTimeMars_text = new Text(view_composite, SWT.READ_ONLY);
		acquisitionTimeMars_text.setLayoutData(new GridData(GridData.FILL,
				GridData.CENTER, true, false));

		Label acquisitionTimeEarth_label = new Label(view_composite, SWT.NONE);
		acquisitionTimeEarth_label.setText("Time (Earth)");
		acquisitionTimeEarth_text = new Text(view_composite, SWT.READ_ONLY);
		acquisitionTimeEarth_text.setLayoutData(new GridData(GridData.FILL,
				GridData.CENTER, true, false));

		Label productType_label = new Label(view_composite, SWT.NONE);
		productType_label.setText("Product type");
		productType_text = new Text(view_composite, SWT.READ_ONLY);
		productType_text.setLayoutData(new GridData(GridData.FILL,
				GridData.CENTER, true, false));

		Label site_label = new Label(view_composite, SWT.NONE);
		site_label.setText("Site number");
		site_text = new Text(view_composite, SWT.READ_ONLY);
		site_text.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,
				true, false));

		Label drive_label = new Label(view_composite, SWT.NONE);
		drive_label.setText("Drive number");
		drive_text = new Text(view_composite, SWT.READ_ONLY);
		drive_text.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,
				true, false));

		Label cmdSeq_label = new Label(view_composite, SWT.NONE);
		cmdSeq_label.setText("Command sequence  ");
		cmdSeq_text = new Text(view_composite, SWT.READ_ONLY);
		cmdSeq_text.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,
				true, false));

		Label cameraEye_label = new Label(view_composite, SWT.NONE);
		cameraEye_label.setText("Camera eye");
		cameraEye_text = new Text(view_composite, SWT.READ_ONLY);
		cameraEye_text.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,
				true, false));
		
		Label cameraFilter_label = new Label(view_composite, SWT.NONE);
		cameraFilter_label.setText("Camera filter");
		cameraFilter_text = new Text(view_composite, SWT.READ_ONLY);
		cameraFilter_text.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,
				true, false));
		
		Label productProducer_label = new Label(view_composite, SWT.NONE);
		productProducer_label.setText("Product producer");
		productProducer_text = new Text(view_composite, SWT.READ_ONLY);
		productProducer_text.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,
				true, false));
		
		Label productVersion_label = new Label(view_composite, SWT.NONE);
		productVersion_label.setText("Product version");
		productVersion_text = new Text(view_composite, SWT.READ_ONLY);
		productVersion_text.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,
				true, false));
		
		sc.setContent(composite);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		sc.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		super.createPartControl(parent);

		updateImageFields();
	}

	public void dispose() {
		super.dispose();
	}

	void notificationImageSelectionChanged(ImageEntry newImage) {
		updateImageFields();
	}

	private void updateImageFields() {
		if ((currentEditor != null) && (currentEditor instanceof ViewerEditor)) {
			ViewerEditor editor = (ViewerEditor) currentEditor;
			ImageEntry imageEntry = editor.getSelectedImage();
			if (imageEntry != null) {
				updateImageFields(imageEntry);
			}
		}
	}

	private void updateImageFields(ImageEntry entry) {
		String filename = entry.getFilename();
		int filenameSize = filename.length();
		boolean isRawImage = false;
		boolean isGeneratedImage = false;
		boolean isDeprecatedType = false;
		int roverClock = 0;
		String type = "Unknown";
		String errorString = "error";
		String unknownString = "";
		String spacecraftID = errorString;
		String displayCamera = errorString;
		String acquisitionTimeMars = errorString;
		String acquisitionTimeEarth = errorString;
		String displayProductType = errorString;
		String siteNumber = errorString;
		String driveNumber = errorString;
		String cmdSeq = errorString;
		String cameraEyeStr = unknownString;
		String cameraFilterStr = unknownString;
		String productProducerStr = unknownString;
		String productVersionStr = unknownString;

		try {
			roverClock = 0;
			if (filenameSize < 11) {
				throw new Exception("Invalid filename");
			}
			try {
				roverClock = MerUtils.roverClockFromFilename(filename);
			} catch (Exception e) {
				throw new Exception("Invalid filename");
			}
			int imageClass = MerUtils.findImageClass(filename);
			if (imageClass == MerUtils.IMAGECLASS_UNKNOWN) {
				if ((filenameSize > 17)
						&& ((filename.substring(14, 17).equals("_an")) || (filename
								.substring(14, 17).equals("_fc")))) {
					type = "Generated Image from previous version";
					isDeprecatedType = true;
				}
			} else if (imageClass == MerUtils.IMAGECLASS_RAW) {
				type = "Raw Image";
				isRawImage = true;
			} else if (imageClass == MerUtils.IMAGECLASS_MMBANAGLYPH) {
				type = "MMB Generated Anaglyph";
				isGeneratedImage = true;
			} else if (imageClass == MerUtils.IMAGECLASS_MMBFALSECOLOR) {
				type = "MMB Generated False-Color";
				isGeneratedImage = true;
			} else if (imageClass == MerUtils.IMAGECLASS_DCCALIBRATEDCOLOR) {
				// type = "<a href=\"http://www.lyle.org/~markoff/\">Calibrated
				// Color by Daniel Crotty</a> (<a
				// href=\"http://www.lyle.org/~markoff/methods.html\">method</a>)";
				type = "Calibrated Color by Daniel Crotty";
				isGeneratedImage = true;
			}
			String roverCode = MerUtils.roverCodeFromFilename(filename);
			spacecraftID = roverCode + " - "
					+ MerUtils.displayRoverNameFromRoverCode(roverCode);
			char camera = filename.charAt(1);
			displayCamera = getDisplayCamera(camera);

			long timeMillis = MerUtils.timeMillisFromRoverClock(roverClock);
			int[] marsTime = new int[4];
			MerUtils.marsTimeFromTimeMillis(roverCode, timeMillis, marsTime);

			acquisitionTimeMars = "Sol " + marsTime[0] + " "
					+ MerUtils.zeroPad(marsTime[1], 2) + ":"
					+ MerUtils.zeroPad(marsTime[2], 2) + ":"
					+ MerUtils.zeroPad(marsTime[3], 2);

			Date date = new Date(timeMillis);
			acquisitionTimeEarth = date.toString();

			String productType = MerUtils.productTypeFromFilename(filename);
			displayProductType = productType + " - "
					+ getDisplayProductType(productType);

			siteNumber = filename.substring(14, 16);
			driveNumber = filename.substring(16, 18);
			cmdSeq = filename.substring(18, 23);

			if (isRawImage) {
				char cameraEye = filename.charAt(23);
				cameraEyeStr = "" + cameraEye + " - "
						+ getDisplayCameraEye(cameraEye);

				char cameraFilter = filename.charAt(24);
				String displayCameraFilter = getDisplayCameraFilter(camera,
						cameraEye, cameraFilter);
				if (displayCameraFilter != null) {
					cameraFilterStr = "" + cameraFilter + " - "
							+ displayCameraFilter;
				} else {
					cameraFilterStr = "" + cameraFilter;
				}

				char producer = filename.charAt(25);
				String displayProducer = getDisplayProducer(producer);
				productProducerStr = "" + producer + " - " + displayProducer;

				char productVersionNumber = filename.charAt(26);
				productVersionStr = "" + productVersionNumber;
			}
			// buf
			// .append("<br><a
			// href=\"http://marsrovers.jpl.nasa.gov/gallery/edr_filename_key.html\">Info
			// on decoding filenames</a>");
		} catch (Exception e) {
		}

		filename_text.setText(filename);
		type_text.setText(type);
		spacecraftId_text.setText(spacecraftID);
		camera_text.setText(displayCamera);
		spacecraftClock_text.setText("" + roverClock);
		acquisitionTimeMars_text.setText(acquisitionTimeMars);
		acquisitionTimeEarth_text.setText(acquisitionTimeEarth);
		productType_text.setText(displayProductType);
		site_text.setText(siteNumber);
		drive_text.setText(driveNumber);
		cmdSeq_text.setText(cmdSeq);
		cameraEye_text.setText(cameraEyeStr);
		cameraFilter_text.setText(cameraFilterStr);
		productProducer_text.setText(productProducerStr);
		productVersion_text.setText(productVersionStr);
	}

	public void setFocus() {
	}

	private String getDisplayCamera(char camera) {
		String displayCamera = "unknown";
		if (camera == 'F')
			displayCamera = "Forward HAZCAM";
		else if (camera == 'R')
			displayCamera = "Rear HAZCAM";
		else if (camera == 'N')
			displayCamera = "NAVCAM";
		else if (camera == 'P')
			displayCamera = "PANCAM";
		else if (camera == 'M')
			displayCamera = "Microscopic Imager";
		else if (camera == 'E')
			displayCamera = "EDLcam (Descent Imager)";
		return displayCamera;
	}

	private String getDisplayProductType(String productType) {
		String displayProductType = "unknown";
		if (productType.equals("EFF"))
			displayProductType = "Full Frame EDR";
		else if (productType.equals("ESF"))
			displayProductType = "Sub-frame EDR";
		else if (productType.equals("EDN"))
			displayProductType = "Downsampled EDR";
		else if (productType.equals("ETH"))
			displayProductType = "Thumbnail EDR";
		else if (productType.equals("ERS"))
			displayProductType = "Row Summed EDR";
		else if (productType.equals("ECS"))
			displayProductType = "Column Summed EDR";
		else if (productType.equals("ERP"))
			displayProductType = "Reference Pixels EDR";
		else if (productType.equals("EHG"))
			displayProductType = "Histogram EDR";
		return displayProductType;
	}

	private String getDisplayCameraEye(char cameraEye) {
		String displayCameraEye = "";
		if (cameraEye == 'L')
			displayCameraEye = "Left";
		else if (cameraEye == 'R')
			displayCameraEye = "Right";
		else if (cameraEye == 'M')
			displayCameraEye = "Monoscopic";
		else if (cameraEye == 'N')
			displayCameraEye = "Not Applicable";
		return displayCameraEye;
	}

	private String getDisplayCameraFilter(char camera, char cameraEye,
			char cameraFilter) {
		String str = null;
		if (camera == 'P') {
			if (cameraEye == 'L') {
				switch (cameraFilter) {
				case '1':
					str = "739nm (338nm bandpass)";
					break;
				case '2':
					str = "753nm (20nm bandpass)";
					break;
				case '3':
					str = "673nm (16nm bandpass)";
					break;
				case '4':
					str = "601nm (17nm bandpass)";
					break;
				case '5':
					str = "535nm (20nm bandpass)";
					break;
				case '6':
					str = "482nm (30nm bandpass)";
					break;
				case '7':
					str = "432nm (32nm Short-pass)";
					break;
				case '8':
					str = "440nm (20) Solar ND 5.0";
					break;
				}
			} else if (cameraEye == 'R') {
				switch (cameraFilter) {
				case '1':
					str = "436nm (37nm Short-pass)";
					break;
				case '2':
					str = "754nm (20nm bandpass) ";
					break;
				case '3':
					str = "803nm (20nm bandpass)";
					break;
				case '4':
					str = "864nm (17nm bandpass)";
					break;
				case '5':
					str = "904nm (26nm bandpass)";
					break;
				case '6':
					str = "934nm (25nm bandpass)";
					break;
				case '7':
					str = "1009nm (38nm Long-pass)";
					break;
				case '8':
					str = "880nm (20) Solar ND 5.0";
					break;
				}
			}
		} else if (camera == 'M') {
			switch (cameraFilter) {
			case '1':
				str = "MI window/cover closed (500-700 nm response)";
				break;
			case '2':
				str = "MI window/cover open (400-700 nm response)";
				break;
			}
		}
		return str;
	}

	private String getDisplayProducer(char producer) {
		String displayProducer = "";
		switch (producer) {
		case 'A':
			displayProducer = "Arizona State University";
			break;
		case 'C':
			displayProducer = "Cornell University";
			break;
		case 'F':
			displayProducer = "USGS at Flagstaff";
			break;
		case 'J':
			displayProducer = "Johannes Gutenberg Univ. (Germany)";
			break;
		case 'M':
			displayProducer = "MIPL (OPGS) at JPL";
			break;
		case 'N':
			displayProducer = "NASA Ames Research (L. Edwards)";
			break;
		case 'P':
			displayProducer = " Max Plank Institute (Germany)";
			break;
		case 'S':
			displayProducer = "SOAS at JPL";
			break;
		case 'U':
			displayProducer = "University of Arizona";
			break;
		case 'V':
			displayProducer = "SSV Team (E. De Jong) at JPL";
			break;
		case 'X':
			displayProducer = "Other";
			break;
		}
		return displayProducer;
	}

}
