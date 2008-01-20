package midnightmarsbrowser.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Iterator;

/**
 * ImageIndex keeps an index of the raw and generated images in the MMB
 * Workspace. This is done for performance reasons: creating a slideshow image
 * set by scanning the directory structure takes way too long.
 * 
 * @author michaelhoward
 * 
 * TODO Add check for index last modified time, to check if anyone modifies
 * indexes behind our backs.
 * 
 * TODO Actually, all this code is going to have to be rewritten, eventually. 
 * Needs IMG support, single image lookup by filename (case insensitive), support for multiple directories...
 * We now have the metadata to use for determining panoramas, instead of an image index.
 * 
 * @author michaelhoward
 * 
 */
public class ImageIndex {

	public static final String CATEGORY_F = "f";
	public static final String CATEGORY_M = "m";
	public static final String CATEGORY_N = "n";
	public static final String CATEGORY_P = "p";
	public static final String CATEGORY_R = "r";
	public static final String CATEGORY_FA = "fa";
	public static final String CATEGORY_NA = "na";
	public static final String CATEGORY_PA = "pa";
	public static final String CATEGORY_PC = "pc";
	public static final String CATEGORY_PS = "ps";
	public static final String CATEGORY_RA = "ra";	
		
	private static final String imageIndexFilename = "imageindex";

	HashMap imageDirectories = new HashMap();
	
	boolean dirty = false;
	
	private int lastInt = 0;
	private Integer lastInteger = new Integer(0);

	public ImageIndex() {
	}

	/**
	 * Returns true if image index has been modified since last saved or loaded.
	 * @return
	 */
	public boolean isDirty() {
		return dirty;
	}
		
	public synchronized void addImageDirectory(File imageDirectory) throws Exception {
		HashMap imageIndex = new HashMap();
		imageDirectories.put(imageDirectory, imageIndex);
		File imageIndexFile = new File(imageDirectory, imageIndexFilename);
		if (imageIndexFile.exists()) {
			// read image index from file
			readImageIndex(imageIndex, imageIndexFile);
		} else {
			// build image index
			fillImageIndex(imageIndex, imageDirectory);
			// TODO do we want to save it here?
			try {
				saveImageIndex(imageIndex, imageDirectory);
			} catch (FileNotFoundException e2) {
				// ignore if directory doesn' exist
			}
		}
	}
	
	// TODO don't necessarily want to write all image indexes (some might be on read-only devices)
	public synchronized void rebuildImageIndexes() throws Exception {
		Iterator imageIndexIter = imageDirectories.keySet().iterator();
		while (imageIndexIter.hasNext()) {
			File imageDirectory = (File) imageIndexIter.next();
			HashMap imageIndex = new HashMap();
			imageDirectories.put(imageDirectory, imageIndex);
			fillImageIndex(imageIndex, imageDirectory);
			saveImageIndex(imageIndex, imageDirectory);
		}
		dirty = false;
	}

	// TODO don't necessarily want to write all image indexes (some might be on read-only devices)
	public synchronized void writeImageIndexes() throws Exception {
		Iterator imageIndexIter = imageDirectories.keySet().iterator();
		while (imageIndexIter.hasNext()) {
			File directory = (File) imageIndexIter.next();
			HashMap imageIndex = (HashMap) imageDirectories.get(directory);
			saveImageIndex(imageIndex, directory);
		}
		dirty = false;
	}

	/**
	 * Return a HashMap of Files indexed by uppercase filename.
	 * All image indexes are scanned.
	 * The files are in no particular order.
	 */
	public synchronized HashMap getFiles(String roverCode, String imageCategory, Integer sol) {
		HashMap files = new HashMap();
		Iterator imageIndexIter = imageDirectories.keySet().iterator();
		while (imageIndexIter.hasNext()) {
			File directory = (File) imageIndexIter.next();
			HashMap imageIndex = (HashMap) imageDirectories.get(directory);
			HashMap cameraCodes = (HashMap) imageIndex.get(roverCode);
			if (cameraCodes != null) {
				HashMap sols = (HashMap) cameraCodes.get(imageCategory);
				if (sols != null) {
					HashMap filenames = (HashMap) sols.get(sol);
					if (filenames != null) {
						Iterator filenameIter = filenames.keySet().iterator();
						while (filenameIter.hasNext()) {
							String filename = (String) filenameIter.next();
							String relPathname = (String) filenames.get(filename);
							File file = new File(directory, relPathname);
							files.put(filename, file);
						}
					}
				}
			}
		}
		return files;
	}
	
	
	/**
	 */
	public synchronized void addFileToImageIndex(File directory, String relPathname) {
		HashMap imageIndex = (HashMap) imageDirectories.get(directory);
		// TODO really we want to throw an error or something if the index has not been created yet
		if (imageIndex == null) {
			imageIndex = new HashMap();
			imageDirectories.put(directory, imageIndex);
		}
		addFileToImageIndex(imageIndex, relPathname);
		dirty = true;
	}

	// TODO legacy method signature
	public synchronized void removeFilenameFromImageIndex(File directory,
			String roverCode, String cameraCode, Integer sol, String filename) {
		HashMap imageIndex = (HashMap) imageDirectories.get(directory);
		if (imageIndex == null) {
			return;
		}
		removeFilenameFromImageIndex(imageIndex, roverCode, cameraCode, sol,
				filename);
		dirty = true;
	}

	/**
	 * Return one of the image category constants, or null, based on the passed string.
	 * This is partly to reduce object usage.
	 * @param category
	 * @return
	 */
	public static String imageCategory(String string) {
		if (string.equalsIgnoreCase(CATEGORY_F)) {
			return CATEGORY_F;
		}
		else if (string.equalsIgnoreCase(CATEGORY_M)) {
			return CATEGORY_M;
		}
		else if (string.equalsIgnoreCase(CATEGORY_N)) {
			return CATEGORY_N;
		}
		else if (string.equalsIgnoreCase(CATEGORY_P)) {
			return CATEGORY_P;
		}
		else if (string.equalsIgnoreCase(CATEGORY_R)) {
			return CATEGORY_R;
		}
		else if (string.equalsIgnoreCase(CATEGORY_FA)) {
			return CATEGORY_FA;
		}
		else if (string.equalsIgnoreCase(CATEGORY_NA)) {
			return CATEGORY_NA;
		}
		else if (string.equalsIgnoreCase(CATEGORY_PA)) {
			return CATEGORY_PA;
		}
		else if (string.equalsIgnoreCase(CATEGORY_PC)) {
			return CATEGORY_PC;
		}
		else if (string.equalsIgnoreCase(CATEGORY_PS)) {
			return CATEGORY_PS;
		}
		else if (string.equalsIgnoreCase(CATEGORY_RA)) {
			return CATEGORY_RA;
		}
		else {
			return null;
		}
	}

	private void readImageIndex(HashMap imageIndex, File imageIndexFile)
			throws Exception {
		BufferedReader reader = new BufferedReader(new FileReader(
				imageIndexFile));
		String line = reader.readLine();
		while (line != null) {
			try {
				String[] params = line.split(";");
				if (params.length > 2) {
					throw new Exception("Invalid line format");
				}
				addFileToImageIndex(imageIndex, params[0]);
			} catch (Exception e) {
				// line error
				String error = "Error parsing line from image index file "
						+ imageIndexFile.getAbsolutePath() + ". Error was "
						+ e.toString() + ". Line was \"" + line + "\".";
				System.err.println(error);
			}
			line = reader.readLine();
		}
	}

	private void addFileToImageIndex(HashMap imageIndex, String relPathname)  {
		relPathname = relPathname.replace('\\', '/');
		String[] parts = relPathname.split("/");
		String filename = parts[parts.length-1];
		String imageCategory = null;
		for (int n=0; n<parts.length-1; n++) {
			String temp = imageCategory(parts[n]);
			if (temp != null) {
				imageCategory = temp;
			}
		}		
		String roverCode = MerUtils.roverCodeFromFilename(filename);
		if (imageCategory == null) {
			imageCategory = imageCategory(MerUtils.cameraCodeFromFilename(filename));
		}
		int sol = MerUtils.solFromFilename(filename);
		if (sol != lastInt) {
			lastInt = sol;
			lastInteger = new Integer(sol);
		}
		
		HashMap cameraCodes = (HashMap) imageIndex.get(roverCode);
		if (cameraCodes == null) {
			cameraCodes = new HashMap();
			imageIndex.put(roverCode, cameraCodes);
		}
		HashMap sols = (HashMap) cameraCodes.get(imageCategory);
		if (sols == null) {
			sols = new HashMap();
			cameraCodes.put(imageCategory, sols);
		}
		HashMap filenames = (HashMap) sols.get(lastInteger);
		if (filenames == null) {
			filenames = new HashMap();
			sols.put(lastInteger, filenames);
		}
		filenames.put(filename.toUpperCase(), relPathname);
	}

	private void removeFilenameFromImageIndex(HashMap imageIndex,
			String roverCode, String cameraCode, Integer sol, String filename) {
		HashMap cameraCodes = (HashMap) imageIndex.get(roverCode);
		if (cameraCodes == null) {
			return;
		}
		HashMap sols = (HashMap) cameraCodes.get(cameraCode);
		if (sols == null) {
			return;
		}
		HashMap filenames = (HashMap) sols.get(sol);
		if (filenames == null) {
			return;
		}
		filenames.remove(filename.toUpperCase());
	}

	private static void saveImageIndex(HashMap imageIndex, File directory)
			throws Exception {
		File imageIndexFile = new File(directory, imageIndexFilename);
		if (!directory.exists()) {
			directory.mkdirs();
		}
		BufferedWriter writer = new BufferedWriter(new FileWriter(
				imageIndexFile));
		Iterator roverIter = imageIndex.keySet().iterator();
		while (roverIter.hasNext()) {
			String roverCode = (String) roverIter.next();
			HashMap cameraCodes = (HashMap) imageIndex.get(roverCode);
			Iterator cameraIterator = cameraCodes.keySet().iterator();
			while (cameraIterator.hasNext()) {
				String cameraCode = (String) cameraIterator.next();
				HashMap sols = (HashMap) cameraCodes.get(cameraCode);
				Iterator solIterator = sols.keySet().iterator();
				while (solIterator.hasNext()) {
					Integer sol = (Integer) solIterator.next();
					HashMap filenames = (HashMap) sols.get(sol);
					Iterator filenameIterator = filenames.keySet().iterator();
					while (filenameIterator.hasNext()) {
						String filename = (String) filenameIterator.next();
						String relPathname = (String) filenames.get(filename);
						writer.write(relPathname);
						writer.write("\n");
					}
				}
			}
		}
		writer.flush();
		writer.close();
	}

	/**
	 * Index the images.
	 * TODO: Make sure there is no possibility of recursive loop.
	 */
	private void fillImageIndex(HashMap imageIndex, File directory) {
		if (!directory.exists() || !directory.isDirectory())
			return;
		fillImageIndex(imageIndex, directory, null, 0);
	}
	
	private void fillImageIndex(HashMap imageIndex, File directory, String relPathname, int level) {
		if (level > 5) {
			return;
		}
		File[] files = directory.listFiles();
		for (int n=0; n<files.length; n++) {
			File file = files[n];
			String filename = file.getName();
			String filenameUppercase = filename.toUpperCase();
			String newRelPathname;
			if (relPathname == null)
				newRelPathname = filename;
			else 
				newRelPathname = relPathname + "/" + filename;
			if (file.isDirectory()) {
				fillImageIndex(imageIndex, file, newRelPathname, level+1);
			}
			else {
				try {
					if (filenameUppercase.endsWith(".JPG") || filenameUppercase.endsWith(".IMG")) {
						if (filename.length() < 31)
							continue;
						if (!Character.isDigit(filename.charAt(0)))
							continue;
						try {
							Integer.valueOf(filename.substring(2, 11));
						} catch (Throwable e) {
							continue;
						}
						if (filename.substring(16, 18).equals("__")) {
							continue;
						}
						addFileToImageIndex(imageIndex, newRelPathname);
					}
				}
				catch (Exception e) {
					System.err.println("Error filling image index: "+e.toString());
				}
			}
		}
	}
}
