package midnightmarsbrowser.metadata;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Arrays;

import midnightmarsbrowser.application.MMBWorkspace;
import midnightmarsbrowser.model.LocationCounter;

/**
 * Site metadata is pulled from the JPL data. This is the metadata that is per site as opposed to per location.
 * For now, that is the origin on the site frame in landing-frame coordinates, according to the rover motion counter.
 * The rover motion counter is not perfectly accurate but at least this allows us to tie site frames together 
 * for display.
 * @author michaelhoward
 */
public class SiteMetadata extends Metadata {

	public static final String NAME = "SiteMetadata";
	
	public SiteMetadata() {
		super(NAME);
	}

	private HashMap roverMap = new HashMap();
	
	public void read(Reader reader) throws IOException {
		ExcelCSVReader csvReader = new ExcelCSVReader(reader);
		String[] lineItems;
		while ((lineItems = csvReader.readLine()) != null) {
			if (lineItems.length >= 4) {
				try {
					String spacecraftId = lineItems[0];
					int site = Integer.valueOf(lineItems[1]).intValue();
					// check for bad data
					if (site > 10000)
						continue;
					SiteMetadataEntry entry = new SiteMetadataEntry(spacecraftId, site);
					entry.offset_vector_a = Double.parseDouble(lineItems[2]);
					entry.offset_vector_b = Double.parseDouble(lineItems[3]);
					entry.offset_vector_c = Double.parseDouble(lineItems[4]);
					//System.out.println("adding entry "+spacecraftId+" "+site+" "+entry.offset_vector_a+" "+
					//		entry.offset_vector_b+" "+entry.offset_vector_c);
					putEntry(entry);
				}
				catch (Exception e) {
					System.err.println(e.toString());
				}
			}
		}
	}
	
	public void write(Writer writer) throws IOException {
		Iterator roverIter = roverMap.keySet().iterator();
		while (roverIter.hasNext()) {
			String spacecraftId = (String) roverIter.next();
			SiteMetadataEntry[] entries = getSortedEntries(spacecraftId);
			for (int n=0; n<entries.length; n++) {
				SiteMetadataEntry entry = entries[n];
				writer.write(entry.spacecraftId);
				writer.write(",");
				writer.write(""+entry.rmc_site);
				writer.write(",");
				writer.write(""+entry.offset_vector_a);
				writer.write(",");
				writer.write(""+entry.offset_vector_b);
				writer.write(",");
				writer.write(""+entry.offset_vector_c);
				writer.write("\n");
			}
		}
	}
	
	void putEntry(SiteMetadataEntry entry) {
		HashMap entryMap = (HashMap) roverMap.get(entry.spacecraftId);
		if (entryMap == null) {
			entryMap = new HashMap();
			roverMap.put(entry.spacecraftId, entryMap);
		}
		entryMap.put(entry.rmc_site, entry);
	}
		
	public SiteMetadataEntry getEntry(String spacecraftId, Integer rmc_site) {
		HashMap entryMap = (HashMap) roverMap.get(spacecraftId);
		if (entryMap != null) {
			return (SiteMetadataEntry) entryMap.get(rmc_site);
		}
		return null;
	}
	
	/**
	 * Return a sorted array of the entries for a rover.
	 * @return
	 */
	public SiteMetadataEntry[] getSortedEntries(String roverCode) {
		SiteMetadataEntry[] entries = null;
		HashMap entryMap = (HashMap) roverMap.get(roverCode);
		if (entryMap != null) {
			entries = new SiteMetadataEntry[entryMap.values().size()];
			entries = (SiteMetadataEntry []) entryMap.values().toArray(entries);
			Arrays.sort(entries);
		}
		return entries;
	}

	void stage(MMBWorkspace workspace) throws IOException {
		SiteMetadata archive = new SiteMetadata();
		archive.readStageArchive(workspace);
		SiteMetadata update = new SiteMetadata();

		Iterator roverIter = roverMap.values().iterator();
		while (roverIter.hasNext()) {
			HashMap entryMap = (HashMap) roverIter.next();
			Iterator entryIter = entryMap.values().iterator();
			while (entryIter.hasNext()) {
				SiteMetadataEntry entry = (SiteMetadataEntry) entryIter.next();
				SiteMetadataEntry archiveEntry = archive.getEntry(entry.spacecraftId, entry.rmc_site);
				if ((archiveEntry == null) || (!archiveEntry.extendedEquals(entry))) {
					update.putEntry(entry);
				}
			}
		}
		update.writeToStage(workspace);
	}
	
	/**
	 * The site frame data comes from the PDS release, so it is not up-to-date. 
	 * For now, do a best-effort calculation making the assumption that the 
	 * last location in the site is the same as the first location in the next site.
	 * Unfortunately the assumption is not always true; probably depends on whether 
	 * they did a sun sighting or not and whether the image was returned.
	 * @param locationMetadata
	 */
	public void fillInMissingSitesFrom(LocationMetadata locationMetadata) {
		//System.out.println("fillInMissingSitesFrom");
		String[] spacecraftIds = locationMetadata.getSpacecraftIds();		
		for (int n=0; n<spacecraftIds.length; n++) {
			String spacecraftId = spacecraftIds[n];
			LocationMetadataEntry[] locations = locationMetadata.getSortedEntries(spacecraftId);
			LocationMetadataEntry previousLocation = null;
			LocationMetadataEntry currentLocation = null;
			if (this.getEntry(spacecraftId, new Integer(0)) == null) {
				SiteMetadataEntry siteEntry = new SiteMetadataEntry(spacecraftId, 0);
				siteEntry.offset_vector_a = 0.0;
				siteEntry.offset_vector_b = 0.0;
				siteEntry.offset_vector_c = 0.0;
				this.putEntry(siteEntry);
			}
			for (int i=0; i<locations.length; i++) {
				currentLocation = locations[i];
				// check for bad data
				if (currentLocation.location.site > 10000)
					continue;
				if ((previousLocation != null) && (previousLocation.location.site != currentLocation.location.site)) {
					SiteMetadataEntry siteEntry = this.getEntry(spacecraftId, new Integer(currentLocation.location.site));
					if (siteEntry == null) {
						SiteMetadataEntry previousSiteEntry = this.getEntry(spacecraftId, new Integer(previousLocation.location.site));
						if (previousSiteEntry != null) {
							siteEntry = new SiteMetadataEntry(spacecraftId, currentLocation.location.site);
							siteEntry.offset_vector_a = previousSiteEntry.offset_vector_a + previousLocation.rover_origin_offset_vector_a;
							siteEntry.offset_vector_b = previousSiteEntry.offset_vector_b + previousLocation.rover_origin_offset_vector_b;
							siteEntry.offset_vector_c = previousSiteEntry.offset_vector_c + previousLocation.rover_origin_offset_vector_c;
							this.putEntry(siteEntry);
							//System.out.println("adding entry "+spacecraftId+" "+siteEntry.rmc_site+" "+siteEntry.offset_vector_a +" "+
							//		siteEntry.offset_vector_b+" "+siteEntry.offset_vector_c);
						}
					}
				}
				previousLocation = currentLocation;
			}
		}
	}
}
