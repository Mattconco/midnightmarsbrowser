package midnightmarsbrowser.metadata;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import midnightmarsbrowser.application.MMBWorkspace;
import midnightmarsbrowser.model.MerUtils;

// TODO extend Metadata
public class LocationDescriptionMetadata extends Metadata {
	
	public static final String NAME = "LocationDescription";
	
	private HashMap roverMap = new HashMap();
	
	public LocationDescriptionMetadata() {
		super(NAME);
	}
	
	public void read(Reader reader) throws IOException {
		ExcelCSVReader csvReader = new ExcelCSVReader(reader);
		String[] lineItems;
		while ((lineItems = csvReader.readLine()) != null) {
			if (lineItems.length >= 4) {
				addEntry(lineItems);
			}
		}
	}
	
	public void write(Writer writer) throws IOException {
		Iterator roverIter = roverMap.values().iterator();
		while (roverIter.hasNext()) {
			HashMap entryMap = (HashMap) roverIter.next();
			Iterator entryIter = entryMap.values().iterator();
			while (entryIter.hasNext()) {
				LocationDescriptionMetadataEntry entry = (LocationDescriptionMetadataEntry) entryIter.next();
				writer.write(entry.roverCode);
				writer.write(",");
				writer.write(entry.site);
				writer.write(",");
				writer.write(entry.drive);
				writer.write(",");
				writer.write(entry.description);
				writer.write("\n");
			}
		}
	}
	
	private void addEntry(String[] lineItems) {
		try {
			LocationDescriptionMetadataEntry entry = new LocationDescriptionMetadataEntry(lineItems[0], lineItems[1], lineItems[2]);
			entry.description = lineItems[3];
			addEntry(entry);
		}
		catch (Exception e) {
			System.err.println(e.toString());
		}
	}
	
	void addEntry(LocationDescriptionMetadataEntry entry) {
		HashMap siteMap = (HashMap) roverMap.get(entry.roverCode);
		if (siteMap == null) {
			siteMap = new LinkedHashMap();
			roverMap.put(entry.roverCode, siteMap);
		}
		siteMap.put(entry.siteDrive, entry);
	}
	
	public LocationDescriptionMetadataEntry getEntry(String roverCode, String siteDrive) {
		HashMap siteMap = (HashMap) roverMap.get(roverCode);
		if (siteMap != null) {
			return (LocationDescriptionMetadataEntry) siteMap.get(siteDrive);
		}
		return null;
	}
	
	void stage(MMBWorkspace workspace) throws IOException {
		LocationDescriptionMetadata archive = new LocationDescriptionMetadata();
		archive.readStageArchive(workspace);
		LocationDescriptionMetadata update = new LocationDescriptionMetadata();

		Iterator roverIter = roverMap.values().iterator();
		while (roverIter.hasNext()) {
			HashMap entryMap = (HashMap) roverIter.next();
			Iterator entryIter = entryMap.values().iterator();
			while (entryIter.hasNext()) {
				LocationDescriptionMetadataEntry entry = (LocationDescriptionMetadataEntry) entryIter.next();
				LocationDescriptionMetadataEntry archiveEntry = (LocationDescriptionMetadataEntry) archive.getEntry(entry.roverCode, entry.siteDrive);
				if ((archiveEntry == null) || (!archiveEntry.equals(entry))) {
					update.addEntry(entry);
				}
			}
		}		
		update.writeToStage(workspace);
	}
	
}
