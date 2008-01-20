package midnightmarsbrowser.metadata;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import midnightmarsbrowser.application.MMBWorkspace;

/**
 * ImageStrech metadata is made up to perform crude image de-stretching and de-vignetting.
 * @author michaelhoward
 */
public class ImageStretchMetadata extends Metadata {

	public static final String NAME = "ImageStretchMetadata";
	
	public ImageStretchMetadata() {
		super(NAME);
	}

	private HashMap filenameMap = new LinkedHashMap();
	
	public ImageStretchMetadataEntry getEntry(String filename) {
		return (ImageStretchMetadataEntry) filenameMap.get(filename);
	}
	
	public void read(Reader reader) throws IOException {
		ExcelCSVReader csvReader = new ExcelCSVReader(reader);
		String[] lineItems;
		while ((lineItems = csvReader.readLine()) != null) {
			if (lineItems.length >= 5) {
				addEntry(lineItems);
			}
		}
	}
	
	public void write(Writer writer) throws IOException {
		Iterator iter = filenameMap.values().iterator();
		while (iter.hasNext()) {
			ImageStretchMetadataEntry entry = (ImageStretchMetadataEntry) iter.next();
			writer.write(entry.filename);
			writer.write(",");
			writer.write(""+entry.minVal);
			writer.write(",");
			writer.write(""+entry.maxVal);
			writer.write(",");
			writer.write(""+entry.devigA);
			writer.write(",");
			writer.write(""+entry.devigB);
			writer.write("\n");
		}
	}
	
	private void addEntry(String[] lineItems) {
		try {
			ImageStretchMetadataEntry entry = new ImageStretchMetadataEntry(lineItems[0]);
			entry.minVal = Integer.parseInt(lineItems[1]);
			entry.maxVal = Integer.parseInt(lineItems[2]);
			entry.devigA = Integer.parseInt(lineItems[3]);
			entry.devigB = Integer.parseInt(lineItems[4]);
			addEntry(entry);
		}
		catch (Exception e) {
			System.err.println(e.toString());
		}
	}
	
	public void addEntry(ImageStretchMetadataEntry entry) {
		filenameMap.put(entry.filename, entry);
	}

	void stage(MMBWorkspace workspace) throws IOException {
		ImageStretchMetadata archive = new ImageStretchMetadata();
		archive.readStageArchive(workspace);
		ImageStretchMetadata update = new ImageStretchMetadata();
		Iterator iter = this.filenameMap.values().iterator();
		while (iter.hasNext()) {
			ImageStretchMetadataEntry entry = (ImageStretchMetadataEntry) iter.next();
			ImageStretchMetadataEntry archiveEntry = archive.getEntry(entry.filename);
			if ((archiveEntry == null) || (!archiveEntry.equals(entry))) {
				update.addEntry(entry);
			}
		}
		update.writeToStage(workspace);
	}
}
