package midnightmarsbrowser.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class PDSIMG {
	
	int pdsVersion = 0;
	int imagePtr = -1;
	int imageHistogramPtr = -1;
	int recordBytes = -1;
	int fileRecords = -1;
	int linePrefixBytes = -1;
	int lineSuffixBytes = -1;
	int lines = -1;
	int lineSamples = -1;
	int sampleBits = -1;
	String sampleType = null;
	ByteBuffer imageByteBuffer = null;
	
	private PDSIMG() {		
	}
	
	public int getPdsVersion() {
		return pdsVersion;
	}

	public int getImagePtr() {
		return imagePtr;
	}

	public int getImageHistogramPtr() {
		return imageHistogramPtr;
	}

	public int getRecordBytes() {
		return recordBytes;
	}

	public int getFileRecords() {
		return fileRecords;
	}

	public int getLinePrefixBytes() {
		return linePrefixBytes;
	}

	public int getLineSuffixBytes() {
		return lineSuffixBytes;
	}

	public int getLines() {
		return lines;
	}

	public int getLineSamples() {
		return lineSamples;
	}

	public int getSampleBits() {
		return sampleBits;
	}

	public String getSampleType() {
		return sampleType;
	}

	public ByteBuffer getImageByteBuffer() {
		return imageByteBuffer;
	}
	
	public byte[] getImageByteArray() {
		byte[] bytes;
		if (imageByteBuffer.hasArray()) {
			bytes = imageByteBuffer.array();
		}
		else {
			bytes = new byte[imageByteBuffer.limit()];
			imageByteBuffer.get(bytes);
		}
		return bytes;
	}

	/**
	 * @param filename
	 * @throws IOException 
	 * @throws Exception 
	 */
	public static PDSIMG readIMGFile(String filename, int clipMin, int clipMax) throws IOException {
		FileInputStream fis = new FileInputStream(filename);
		FileChannel fc = fis.getChannel();
		int sz = (int)fc.size();
		MappedByteBuffer fileData = fc.map(FileChannel.MapMode.READ_ONLY, 0, sz);
		PDSIMG img = new PDSIMG();
		try {
			img.read(fileData, clipMin, clipMax);
		}
		finally {
			fc.close();
		}
		return img;
	}

	/**
	 * Read IMG file data and return a ByteBuffer of byte values. 
	 * Image pixel values are mapped to 0..255 based on clipMin and clipMax.
	 * IMG file header parsing is loosely based on Bippy pds.py.
	 * @param fileData
	 * @throws IOException
	 */
	private void read(ByteBuffer fileData, int clipMin, int clipMax) throws IOException {
		int clipDiff = clipMax - clipMin;
		String str = null;
		boolean endFound = false;
		boolean verbose = false;
		boolean imageHeaderFlag = false;

		while ((str = readLine(fileData)) != null) {
			str = str.trim();
			if (str.length() == 0) 
				continue;
			if (str.length() > 3 && str.substring(0,3).equals("   ") && str.indexOf("=") == -1)
				// run on text; we don't need it yet
				continue;
			String[] t = str.split("\\s+");
			if (pdsVersion != 0) {
				if (t.length >= 3 && t[0].equals("PDS_VERSION_ID") && (t[1].equals("="))) {
					pdsVersion = Integer.parseInt(t[3]);
					continue;
				}
				else {
					throw new IOException("Not a Planetary Data System PDS file");
				}				
			}
			if (t.length < 3) {
				if (t[0].equals("END")) {
					endFound = true;
					break;
				}
				else if (verbose) {
					System.out.println("IMG: No \"=\" in: "+str);
				}
			}
			else if (t[0].equals("OBJECT") && t[2].equals("IMAGE")) {
				imageHeaderFlag = true;
			}
			else if (t[0].equals("ENDOBJECT") && t[2].equals("IMAGE")) {
				imageHeaderFlag = false;
			}
			else if (t[0].equals("^IMAGE")) {
				imagePtr = Integer.parseInt(t[2]);
			}
			else if (t[0].equals("^IMAGE_HISTOGRAM")) {
				imageHistogramPtr = Integer.parseInt(t[2]);
			}
			else if (t[0].equals("RECORD_BYTES")) {
				recordBytes = Integer.parseInt(t[2]);
			}
			else if (t[0].equals("FILE_RECORDS")) {
				fileRecords = Integer.parseInt(t[2]);
			}
			else if (t[0].equals("LINE_PREFIX_BYTES")) {
				if (imageHeaderFlag) {
					linePrefixBytes = Integer.parseInt(t[2]);
				}
			}
			else if (t[0].equals("LINE_SUFFIX_BYTES")) {
				if (imageHeaderFlag) {
					lineSuffixBytes = Integer.parseInt(t[2]);
				}
			}
			else if (t[0].equals("LINES")) {
				if (imageHeaderFlag) {
					lines = Integer.parseInt(t[2]);
				}
			}
			else if (t[0].equals("LINE_SAMPLES")) {
				if (imageHeaderFlag) {
					lineSamples = Integer.parseInt(t[2]);
				}
			}
			else if (t[0].equals("SAMPLE_BITS")) {
				if (imageHeaderFlag) {
					sampleBits = Integer.parseInt(t[2]);
				}
			}
			else if (t[0].equals("SAMPLE_TYPE")) {
				if (imageHeaderFlag) {
					sampleType = t[2];
				}
			}
		}
		if (!endFound) {
			throw new IOException("PDS header END not found");
		}
		// read the data
		if (imagePtr >= 0) {
			fileData.position((imagePtr-1)*recordBytes);
		}
		else {
			// not sure if +1 is correct...
			int start = fileData.position()/recordBytes + 1;
			fileData.position(recordBytes * start);
		}
		if (sampleBits == 16) {
			ByteBuffer newBuf = fileData.slice();
			newBuf.order(ByteOrder.BIG_ENDIAN);
			ByteBuffer outBuf = ByteBuffer.allocate(lines * lineSamples);
			for (int i=0; i<lines; i++) {
				for (int j=0; j<lineSamples; j++) {
					int val = (int) (newBuf.getShort());
					int clipVal = (val - clipMin) * 256 / clipDiff;
					byte byteVal;
					if (clipVal < 0) {
						byteVal = 0;
					}
					else if (clipVal >= 256) {
						byteVal = (byte) 255;
					}					
					else {
						byteVal = (byte) clipVal;
					}
					outBuf.put(byteVal);
				}
			}
			outBuf.flip();
			imageByteBuffer = outBuf;
		}
		else {
			throw new IOException(""+sampleBits+"-bit not supported.");
		}
	}
	
	static String readLine(ByteBuffer bb) {
		int pos = bb.position();
		int max = bb.limit();
		int start = pos;
		byte b = 0;
		
		while (pos < max) {
			b=bb.get();
			pos++;
			if (b == 0x0A || b == 0x0D) {
				break;
			}
		}
		int end = pos;
		if (b == 0x0A || b == 0x0D) {
			end = pos - 1;
			if (bb.get(pos) == 0x0A || bb.get(pos) == 0x0D) {
				pos++;
			}
		}

		if (end != start) {
			byte[] array = new byte[end-start];
			bb.position(start);
			bb.get(array, 0, array.length);
			bb.position(pos);
			String str;
			try {
				str = new String(array, "ISO-8859-1");
			}
			catch (Exception e) {
				str = new String(array);
			}
			return str;
		}
		else if (end == max) {
			return null;
		}
		else {
			bb.position(pos);
			return "";
		}
	}
}
