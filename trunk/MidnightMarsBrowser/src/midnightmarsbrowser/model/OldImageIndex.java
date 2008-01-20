package midnightmarsbrowser.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;

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
 * New requirements for image index:
 * 
 * Need to be able to retrieve images by image category, which can be a one character code for raw (JPG) images, or a 
 * two character code for generated images. 
 * Also need a way to separate out IMG files, though. (They can't be mixed in with the JPGs)
 * 
 * @author 3003
 * 
 */
public class OldImageIndex {

	private static final String imageIndexFilename = "imageindex";

	File rawImageDir;

	File generatedImageDir;

	HashMap imageIndexes;
	
	boolean dirty = false;

	public OldImageIndex(File rawImageDir, File generatedImageDir) {
		this.rawImageDir = rawImageDir;
		this.generatedImageDir = generatedImageDir;
	}

	/**
	 * Returns true if image index has been modified since last saved or loaded.
	 * @return
	 */
	public boolean isDirty() {
		return dirty;
	}
	
	public synchronized void readImageIndexes(boolean rebuild) throws Exception {
		imageIndexes = new HashMap();
		loadImageIndexes(rawImageDir, true, rebuild);
		loadImageIndexes(generatedImageDir, false, rebuild);
		dirty = false;
	}

	public synchronized void writeImageIndexes() throws Exception {
		if (imageIndexes == null) {
			throw new Error("Image indexes have not been read.");
		}
		Iterator imageIndexIter = imageIndexes.keySet().iterator();
		while (imageIndexIter.hasNext()) {
			File directory = (File) imageIndexIter.next();
			HashMap imageIndex = (HashMap) imageIndexes.get(directory);
			saveImageIndex(imageIndex, directory);
		}
		dirty = false;
	}

	public synchronized void addFilenameToImageIndex(File directory,
			String roverCode, String cameraCode, Integer sol, String filename) {
		HashMap imageIndex = (HashMap) imageIndexes.get(directory);
		if (imageIndex == null) {
			imageIndex = new HashMap();
			imageIndexes.put(directory, imageIndex);
		}
		addFilenameToImageIndex(imageIndex, roverCode, cameraCode, sol,
				filename);
		dirty = true;
	}

	public synchronized void removeFilenameFromImageIndex(File directory,
			String roverCode, String cameraCode, Integer sol, String filename) {
		HashMap imageIndex = (HashMap) imageIndexes.get(directory);
		if (imageIndex == null) {
			return;
		}
		removeFilenameFromImageIndex(imageIndex, roverCode, cameraCode, sol,
				filename);
		dirty = true;
	}

	public synchronized LinkedHashSet getFilenames(File directory,
			String roverCode, String cameraCode, int sol) {
		LinkedHashSet filenames = null;
		if (imageIndexes == null) {
			System.err.println("imageIndexes was null");
		}
		if (directory == null) {
			System.err.println("directory was null");
		}
		HashMap imageIndex = (HashMap) imageIndexes.get(directory);
		if (imageIndex != null) {
			HashMap cameraCodes = (HashMap) imageIndex.get(roverCode);
			if (cameraCodes != null) {
				HashMap sols = (HashMap) cameraCodes.get(cameraCode);
				if (sols != null) {
					LinkedHashSet filenames2 = (LinkedHashSet) sols
							.get(new Integer(sol));
					if (filenames2 != null) {
						filenames = (LinkedHashSet) filenames2.clone();
					}
				}
			}
		}
		return filenames;
	}

	private void loadImageIndexes(File directory, boolean raw, boolean rebuild)
			throws Exception {
		HashMap imageIndex = (HashMap) imageIndexes.get(directory);
		if (imageIndex == null) {
			imageIndex = new HashMap();
			imageIndexes.put(directory, imageIndex);
		}
		File imageIndexFile = new File(directory, imageIndexFilename);
		if (imageIndexFile.exists() && !rebuild) {
			// read image index from file
			// setProgressDialogStatus("Reading image indexes...");
			readImageIndex(imageIndex, imageIndexFile);
		} else {
			// build image index
			// setProgressDialogStatus("Indexing images; please wait.");
			fillImageIndex(imageIndex, directory, raw);
			// setProgressDialogStatus("Saving image index...");
			try {
				saveImageIndex(imageIndex, directory);
			} catch (FileNotFoundException e2) {
				// ignore if directory doesn' exist
			}
		}
	}

	private void readImageIndex(HashMap imageIndex, File imageIndexFile)
			throws Exception {
		// setSubProgressMax((int)imageIndexFile.length());
		BufferedReader reader = new BufferedReader(new FileReader(
				imageIndexFile));
		String line = reader.readLine();
		int lastInt = -1;
		Integer lastInteger = null;
		while (line != null) {
			// incSubProgress(line.length()+1);
			try {
				String[] params = line.split(";");
				if (params.length > 2) {
					throw new Exception("Invalid line format");
				}
				if (params.length > 1) {
					// fileLength is not used anymore
					// long fileLength = Long.parseLong(params[1]);
				}
				String[] parts = params[0].split("/");
				if (parts.length != 4)
					throw new Exception("Invalid partial pathname");
				String roverCode = parts[0];
				String cameraCode = parts[1];
				int sol = Integer.parseInt(parts[2]);
				String filename = parts[3];
				if (sol != lastInt) {
					lastInt = sol;
					lastInteger = new Integer(sol);
				}
				addFilenameToImageIndex(imageIndex, roverCode, cameraCode,
						lastInteger, filename);
			} catch (Exception e) {
				// line error
				String error = "Error parsing line from image index file "
						+ imageIndexFile.getAbsolutePath() + ". Error was "
						+ e.toString() + ". Line was \"" + line + "\".";
				throw new Exception(error);
			}
			line = reader.readLine();
		}
	}

	private void addFilenameToImageIndex(HashMap imageIndex, String roverCode,
			String cameraCode, Integer sol, String filename) {
		HashMap cameraCodes = (HashMap) imageIndex.get(roverCode);
		if (cameraCodes == null) {
			cameraCodes = new HashMap();
			imageIndex.put(roverCode, cameraCodes);
		}
		HashMap sols = (HashMap) cameraCodes.get(cameraCode);
		if (sols == null) {
			sols = new HashMap();
			cameraCodes.put(cameraCode, sols);
		}
		LinkedHashSet filenames = (LinkedHashSet) sols.get(sol);
		if (filenames == null) {
			filenames = new LinkedHashSet();
			sols.put(sol, filenames);
		}
		filenames.add(filename);
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
		LinkedHashSet filenames = (LinkedHashSet) sols.get(sol);
		if (filenames == null) {
			return;
		}
		filenames.remove(filename);
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
					LinkedHashSet filenames = (LinkedHashSet) sols.get(sol);
					Iterator filenameIterator = filenames.iterator();
					while (filenameIterator.hasNext()) {
						String filename = (String) filenameIterator.next();
						String lineStr = roverCode + "/" + cameraCode + "/"
								+ sol + "/" + filename + "\n";
						writer.write(lineStr);
					}
				}
			}
		}
		writer.flush();
		writer.close();
	}

	/**
	 * Index the images.
	 */
	private void fillImageIndex(HashMap imageIndex, File directory, boolean raw) {
		fillImageIndex(imageIndex, directory, "1", raw);
		fillImageIndex(imageIndex, directory, "2", raw);
	}

	private void fillImageIndex(HashMap imageIndex, File directory,
			String roverCode, boolean raw) {
		File newDir = new File(directory, roverCode);
		if ((newDir.exists()) && (newDir.isDirectory())) {
			HashMap cameras = new HashMap();
			imageIndex.put(roverCode, cameras);
			if (raw) {
				fillCameras(cameras, newDir, "f");
				fillCameras(cameras, newDir, "r");
				fillCameras(cameras, newDir, "n");
				fillCameras(cameras, newDir, "p");
				fillCameras(cameras, newDir, "m");
			} else {
				fillCameras(cameras, newDir, "fa");
				fillCameras(cameras, newDir, "ra");
				fillCameras(cameras, newDir, "na");
				fillCameras(cameras, newDir, "pa");
				fillCameras(cameras, newDir, "pc");
				fillCameras(cameras, newDir, "ps");
			}
		}
	}

	private void fillCameras(HashMap cameras, File roverDir, String cameraCode) {
		File newDir = new File(roverDir, cameraCode);
		if ((newDir.exists()) && (newDir.isDirectory())) {
			HashMap sols = new HashMap();
			cameras.put(cameraCode, sols);
			fillSols(sols, newDir);
		}
		// instance.incProgress(1);
	}

	private void fillSols(HashMap sols, File cameraDir) {
		int lastInt = -1;
		Integer lastInteger = null;
		File[] files = cameraDir.listFiles();
		// instance.setSubProgressMax(files.length);
		for (int n = 0; n < files.length; n++) {
			File newDir = files[n];
			if ((newDir.exists()) && (newDir.isDirectory())) {
				try {
					int sol = Integer.parseInt(newDir.getName());
					if (lastInt != sol) {
						lastInt = sol;
						lastInteger = new Integer(sol);
					}
					LinkedHashSet filenames = new LinkedHashSet();
					sols.put(lastInteger, filenames);
					fillFilenames(filenames, newDir);
				} catch (NumberFormatException e) {
				}
			}
			// instance.incSubProgress(1);
		}
	}

	private void fillFilenames(LinkedHashSet filenames, File solDir) {
		File[] files = solDir.listFiles();
		for (int n = 0; n < files.length; n++) {
			if (files[n].isFile()) {
				File file = files[n];
				String filename = file.getName();
				// TODO support PNG - really how to do that is a question
				if (filename.endsWith(".JPG") || filename.endsWith(".jpg")/* ||filename.endsWith(".png") */) {
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
					filenames.add(filename);
				}
			}
		}
	}

}
