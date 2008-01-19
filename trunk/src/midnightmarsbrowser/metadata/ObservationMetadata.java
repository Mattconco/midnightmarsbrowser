package midnightmarsbrowser.metadata;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import midnightmarsbrowser.application.MMBWorkspace;

/**
 * @author michaelhoward
 */
public class ObservationMetadata extends Metadata {

	public static final String NAME = "ObservationMetadata";
	
	public static final String UNEXPECTED_SEQUENCE = "Unexpected sequence!!!!";
	
	public ObservationMetadata() {
		super(NAME);
	}

	private HashMap obsIDMap = new HashMap();
	
	public ObservationMetadataEntry getEntry(String obsID) {
		return (ObservationMetadataEntry) obsIDMap.get(obsID);
	}
	
	public void read(Reader reader) throws IOException {
		ExcelCSVReader csvReader = new ExcelCSVReader(reader);
		String[] lineItems;
		while ((lineItems = csvReader.readLine()) != null) {
			if (lineItems.length >= 2) {
				addEntry(lineItems);
			}
		}
	}
	
	public void write(Writer writer) throws IOException {
		String[] obsIDs = new String[obsIDMap.size()];
		obsIDs = (String []) obsIDMap.keySet().toArray(obsIDs);
		Arrays.sort(obsIDs);
		for (int n=0; n<obsIDs.length; n++) {
			String obsID = obsIDs[n];
			ObservationMetadataEntry entry = (ObservationMetadataEntry) obsIDMap.get(obsID);
			writer.write(entry.obsID);
			writer.write(",");
			writer.write(entry.description);
			writer.write("\n");
		}
	}
	
	private void addEntry(String[] lineItems) {
		try {
			ObservationMetadataEntry entry = new ObservationMetadataEntry(lineItems[0]);
			if (lineItems[1].equals(UNEXPECTED_SEQUENCE)) {
				// This eliminates some object allocations, since "Unexpected sequence!!!!"
				// appears over and over again.
				entry.description = UNEXPECTED_SEQUENCE;
			}
			else {
				entry.description = lineItems[1];
			}
			addEntry(entry);
		}
		catch (Exception e) {
			System.err.println(e.toString());
		}
	}
	
	void addEntry(ObservationMetadataEntry entry) {
		obsIDMap.put(entry.obsID, entry);
	}

	void stage(MMBWorkspace workspace) throws IOException {
		ObservationMetadata archive = new ObservationMetadata();
		archive.readStageArchive(workspace);
		ObservationMetadata update = new ObservationMetadata();
		Iterator iter = this.obsIDMap.values().iterator();
		while (iter.hasNext()) {
			ObservationMetadataEntry entry = (ObservationMetadataEntry) iter.next();
			ObservationMetadataEntry archiveEntry = archive.getEntry(entry.obsID);
			if ((archiveEntry == null) || (!archiveEntry.equals(entry))) {
				update.addEntry(entry);
			}
		}
		update.writeToStage(workspace);
	}
}
