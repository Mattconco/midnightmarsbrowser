package midnightmarsbrowser.metadata;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import midnightmarsbrowser.application.MMBWorkspace;
import midnightmarsbrowser.model.LocationCounter;

/**
 * Location metadata is directly derived from the JPL data.
 * It does not include things like location description, which are made up on our end.
 * @author michaelhoward
 */
public class LocationMetadata extends Metadata {

	public static final String NAME = "LocationMetadata";
	
	public LocationMetadata() {
		super(NAME);
	}

	private HashMap roverMap = new HashMap();
	
	public void read(Reader reader) throws IOException {
		ExcelCSVReader csvReader = new ExcelCSVReader(reader);
		String[] lineItems;
		while ((lineItems = csvReader.readLine()) != null) {
			if (lineItems.length >= 9) {
				try {
					int site = LocationCounter.valueFromString(lineItems[1]);
					if (site > 10000)
						continue;
					int drive = LocationCounter.valueFromString(lineItems[2]);
					LocationMetadataEntry entry = new LocationMetadataEntry(lineItems[0], site, drive);
					entry.startSol = Integer.parseInt(lineItems[3]);
					entry.endSol = Integer.parseInt(lineItems[4]);
					entry.rover_origin_rotation_quaternian_a = Double.parseDouble(lineItems[5]);
					entry.rover_origin_rotation_quaternian_b = Double.parseDouble(lineItems[6]);
					entry.rover_origin_rotation_quaternian_c = Double.parseDouble(lineItems[7]);
					entry.rover_origin_rotation_quaternian_d = Double.parseDouble(lineItems[8]);
					if (lineItems.length >= 12) {
						entry.has_site_rover_origin_offset_vector = true;
						entry.rover_origin_offset_vector_a = Double.parseDouble(lineItems[9]);
						entry.rover_origin_offset_vector_b = Double.parseDouble(lineItems[10]);
						entry.rover_origin_offset_vector_c = Double.parseDouble(lineItems[11]);
					}
					putEntry(entry);
				}
				catch (Exception e) {
					System.err.println(e.toString());
				}
			}
		}
	}
	
	public void write(Writer writer) throws IOException {
		Iterator roverIter = roverMap.values().iterator();
		while (roverIter.hasNext()) {
			HashMap entryMap = (HashMap) roverIter.next();
			LocationMetadataEntry[] entries = new LocationMetadataEntry[entryMap.size()];
			entries = (LocationMetadataEntry[]) entryMap.values().toArray(entries);
			Arrays.sort(entries);
			for (int n=0; n<entries.length; n++) {
				LocationMetadataEntry entry = entries[n];
				writer.write(entry.roverCode);
				writer.write(",");
				writer.write(""+entry.location.site);
				writer.write(",");
				writer.write(""+entry.location.drive);
				writer.write(",");
				writer.write(""+entry.startSol);
				writer.write(",");
				writer.write(""+entry.endSol);
				writer.write(",");
				writer.write(""+entry.rover_origin_rotation_quaternian_a);
				writer.write(",");
				writer.write(""+entry.rover_origin_rotation_quaternian_b);
				writer.write(",");
				writer.write(""+entry.rover_origin_rotation_quaternian_c);
				writer.write(",");
				writer.write(""+entry.rover_origin_rotation_quaternian_d);
				writer.write(",");
				writer.write(""+entry.rover_origin_offset_vector_a);
				writer.write(",");
				writer.write(""+entry.rover_origin_offset_vector_b);
				writer.write(",");
				writer.write(""+entry.rover_origin_offset_vector_c);
				writer.write("\n");
			}
		}
	}
		
	void putEntry(LocationMetadataEntry entry) {
		HashMap entryMap = (HashMap) roverMap.get(entry.roverCode);
		if (entryMap == null) {
			entryMap = new HashMap();
			roverMap.put(entry.roverCode, entryMap);
		}
		entryMap.put(entry.location, entry);
	}
		
	public LocationMetadataEntry getEntry(String roverCode, LocationCounter location) {
		HashMap entryMap = (HashMap) roverMap.get(roverCode);
		if (entryMap != null) {
			return (LocationMetadataEntry) entryMap.get(location);
		}
		return null;
	}
	
	public String[] getSpacecraftIds() {
		String[] spacecraftIds = new String[roverMap.keySet().size()];
		spacecraftIds = (String []) roverMap.keySet().toArray(spacecraftIds);
		Arrays.sort(spacecraftIds);
		return spacecraftIds;
	}
	
	/**
	 * Returns a sorted array of the LocationCounter keys for a rover.
	 * @return
	 */
	public LocationCounter[] getSortedKeys(String roverCode) {
		LocationCounter[] locations = null;
		HashMap entryMap = (HashMap) roverMap.get(roverCode);
		if (entryMap != null) {
			locations = new LocationCounter[entryMap.keySet().size()];
			locations = (LocationCounter []) entryMap.keySet().toArray(locations);
			Arrays.sort(locations);
		}
		return locations;
	}
	
	/**
	 * Returns a sorted array of the LocationMetadataEntry's for a rover.
	 * @return
	 */
	public LocationMetadataEntry[] getSortedEntries(String roverCode) {
		LocationMetadataEntry[] locations = null;
		HashMap entryMap = (HashMap) roverMap.get(roverCode);
		if (entryMap != null) {
			locations = new LocationMetadataEntry[entryMap.values().size()];
			locations = (LocationMetadataEntry []) entryMap.values().toArray(locations);
			Arrays.sort(locations);
		}
		return locations;
	}

	void stage(MMBWorkspace workspace) throws IOException {
		LocationMetadata archive = new LocationMetadata();
		archive.readStageArchive(workspace);
		LocationMetadata update = new LocationMetadata();

		Iterator roverIter = roverMap.values().iterator();
		while (roverIter.hasNext()) {
			HashMap entryMap = (HashMap) roverIter.next();
			Iterator entryIter = entryMap.values().iterator();
			while (entryIter.hasNext()) {
				LocationMetadataEntry entry = (LocationMetadataEntry) entryIter.next();
				LocationMetadataEntry archiveEntry = archive.getEntry(entry.roverCode, entry.location);
				if ((archiveEntry == null) || (!archiveEntry.extendedEquals(entry))) {
					update.putEntry(entry);
				}
			}
		}
		update.writeToStage(workspace);
	}	
}
