package midnightmarsbrowser.metadata;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import midnightmarsbrowser.application.MMBWorkspace;

/**
 * Image metadata is directly derived from the JPL data.
 * It does not include things like brightness corrections / de-vignetting, which are (currently) made up on our end.
 * @author michaelhoward
 */
public class ImageMetadata extends Metadata {

	public static final String NAME = "ImageMetadata";
	
	public ImageMetadata() {
		super(NAME);
	}

	// TODO changed to regular HashMap; sort array before writing
	private HashMap filenameMap = new HashMap();
	
	private HashMap roverMap = new HashMap();
	
	public ImageMetadataEntry getEntry(String filename) {
		return (ImageMetadataEntry) filenameMap.get(filename);
	}
	
	public ImageMetadataEntry getEntry(String roverCode, Integer roverClock) {
		HashMap roverClockMap = (HashMap) roverMap.get(roverCode);
		if (roverClockMap != null) {
			return (ImageMetadataEntry) roverClockMap.get(roverClock);
		}
		return null;
	}
	
	public void read(Reader reader) throws IOException {
		ExcelCSVReader csvReader = new ExcelCSVReader(reader);
		String[] lineItems;
		while ((lineItems = csvReader.readLine()) != null) {
			if (lineItems.length >= 9) {
				addEntry(lineItems);
			}
		}
	}
	
	public void write(Writer writer) throws IOException {
		ImageMetadataEntry[] entries = new ImageMetadataEntry[filenameMap.size()];
		entries = (ImageMetadataEntry[]) filenameMap.values().toArray(entries);
		Arrays.sort(entries);		
		for (int n=0; n<entries.length; n++) {
			ImageMetadataEntry entry = entries[n];
			writer.write(entry.filename);
			writer.write(",");
			writer.write(""+entry.inst_az_rover);
			writer.write(",");
			writer.write(""+entry.inst_el_rover);
			writer.write(",");
			writer.write(""+entry.first_line);
			writer.write(",");
			writer.write(""+entry.first_line_sample);
			writer.write(",");
			writer.write(""+entry.n_lines);
			writer.write(",");
			writer.write(""+entry.n_line_samples);
			writer.write(",");
			writer.write(""+entry.pixel_averaging_height);
			writer.write(",");
			writer.write(""+entry.pixel_averaging_width);
			if (entry.rmc_site != -1) {
				writer.write(",");
				writer.write(""+entry.rmc_site);
				writer.write(",");
				writer.write(""+entry.rmc_drive);
				if (entry.obsID != null) {
					writer.write(",");
					writer.write(""+entry.obsID);
				}
			}
			writer.write("\n");
		}
	}
	
	private void addEntry(String[] lineItems) {
		try {
			ImageMetadataEntry entry = new ImageMetadataEntry(lineItems[0]);
			entry.inst_az_rover = Double.parseDouble(lineItems[1]);
			entry.inst_el_rover = Double.parseDouble(lineItems[2]);
			entry.first_line = Integer.parseInt(lineItems[3]);
			entry.first_line_sample = Integer.parseInt(lineItems[4]);
			entry.n_lines = Integer.parseInt(lineItems[5]);
			entry.n_line_samples = Integer.parseInt(lineItems[6]);
			entry.pixel_averaging_height = Integer.parseInt(lineItems[7]);
			entry.pixel_averaging_width = Integer.parseInt(lineItems[8]);
			if (lineItems.length >= 11) {
				entry.rmc_site = Integer.parseInt(lineItems[9]);
				entry.rmc_drive = Integer.parseInt(lineItems[10]);
			}
			if (lineItems.length >= 12) {
				entry.obsID = lineItems[11];
			}
			addEntry(entry);
		}
		catch (Exception e) {
			System.err.println(e.toString());
		}
	}
	
	void addEntry(ImageMetadataEntry entry) {
		filenameMap.put(entry.filename, entry);		
		// Add to alternate index.
		// Left-right images can replace each other, but it doesn't matter 
		// for this metadata.
		HashMap roverClockMap = (HashMap) roverMap.get(entry.roverCode);
		if (roverClockMap == null) {
			roverClockMap = new HashMap();
			roverMap.put(entry.roverCode, roverClockMap);
		}
		roverClockMap.put(entry.roverClock, entry);
	}

	void stage(MMBWorkspace workspace) throws IOException {
		ImageMetadata archive = new ImageMetadata();
		archive.readStageArchive(workspace);
		ImageMetadata update = new ImageMetadata();
		Iterator iter = this.filenameMap.values().iterator();
		while (iter.hasNext()) {
			ImageMetadataEntry entry = (ImageMetadataEntry) iter.next();
			ImageMetadataEntry archiveEntry = archive.getEntry(entry.filename);
			if ((archiveEntry == null) || (!archiveEntry.equals(entry))) {
				update.addEntry(entry);
			}
		}
		update.writeToStage(workspace);
	}
}
