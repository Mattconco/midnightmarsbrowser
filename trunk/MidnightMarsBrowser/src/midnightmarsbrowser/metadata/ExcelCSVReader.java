package midnightmarsbrowser.metadata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

/**
 * A quick class to read Excel-formatted CSV files. 
 * Unfortunately Microsoft Excel has it's own strange, non-standard format for CSV files.
 * This class may not cover every aspect of Microsoft's strange CSV format.
 */
public class ExcelCSVReader {
	BufferedReader reader;
	StringBuffer strbuf = new StringBuffer();

	public ExcelCSVReader(Reader inReader) {
		reader = new BufferedReader(inReader);
	}
	
	public String[] readLine() throws IOException {
		String line = reader.readLine();
		while ((line != null) && (line.startsWith("#") || (line.trim().length()==0))) {
			line = reader.readLine();
		}
		int ptr = 0;
		boolean inQuote = false;
		ArrayList list = new ArrayList();
		strbuf.setLength(0);
		if (line != null) {
			while (ptr < line.length()) {
				char ch = line.charAt(ptr);
				if (ch == '"') {
					if ((ptr < line.length()-1) && (line.charAt(ptr+1) == '"')) {
						strbuf.append("\"");
						ptr += 2;
					}
					else if (inQuote) {
						inQuote = false;
						ptr++;
					}
					else {
						inQuote = true;
						ptr++;
					}
				}
				else if (ch == ',') {
					if (inQuote) {
						strbuf.append(ch);
						ptr++;
					}
					else {
						list.add(strbuf.toString());
						strbuf.setLength(0);
						ptr++;
					}
				}
				else {
					strbuf.append(ch);
					ptr++;
				}
			}
			list.add(strbuf.toString());
			String[] array = new String[list.size()];
			array = (String []) list.toArray(array);
			return array;
		}
		else {
			return null;
		}
	}
	
	public void close() throws IOException {
		reader.close();
	}
}
