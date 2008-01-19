package midnightmarsbrowser.application;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class MMB1Prefs {
	
	private final static String propertiesFilename = "MidnightMarsBrowser.properties";
	
	public File homeDir = null;
	
	public File rawImageDir = null;
	
	public File generatedImageDir = null;	
	
	private MMB1Prefs(Properties properties) {
		String str;
		try {
			str = properties.getProperty("homeDir");
			homeDir = new File(str);
		}
		catch (Exception e) {
			System.out.println("Couldn't read homeDir preference; "+e.toString());
		}
		
		try {
			str = properties.getProperty("rawImageDir");
			rawImageDir = new File(str);
		}
		catch (Exception e) {
			System.out.println("Couldn't read rawImageDir preference");
		}

		try {
			str = properties.getProperty("generatedImageDir");
			generatedImageDir = new File(str);
			generatedImageDir.mkdirs();
		}
		catch (Exception e) {
			System.out.println("Couldn't read generatedImageDir preference; using default.");
		}
	}
	
	public static MMB1Prefs findMMB1Prefs() {
		File prefsFile = null;
		if (System.getProperty("mrj.version") == null) {
			prefsFile = new File(System.getProperty("user.home")+File.separator+propertiesFilename);
		}
		else {
			// Mac OS: put in Library/Preferences
			prefsFile = new File(System.getProperty("user.home")+File.separator
					+"Library"+File.separator+"Preferences"+File.separator+propertiesFilename);
		}		
		if (!prefsFile.isFile()) {
			return null;
		}
		
		Properties properties = new Properties();		
		FileInputStream in = null;
		try {
			in = new FileInputStream(prefsFile);
			properties.load(in);
			in.close();
		} catch (java.io.FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (java.io.IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (java.io.IOException e) {
				}
				in = null;
			}
		}
		
		return new MMB1Prefs(properties);
	}
}
