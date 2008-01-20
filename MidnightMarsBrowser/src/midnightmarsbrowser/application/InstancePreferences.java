package midnightmarsbrowser.application;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;

public class InstancePreferences {
	private static InstancePreferences instance;
	
	private IEclipsePreferences prefs;

	private static final String LAST_WORKSPACE = "lastWorkspace";
	
	public static InstancePreferences getInstance() {
		if (instance == null) {
			instance = new InstancePreferences();
		}
		return instance;
	}
	
	private InstancePreferences() {
		prefs = new InstanceScope().getNode(Application.PLUGIN_ID);
	}
	
	public void flush() {
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}		
	}
	
	public String getLastWorkspace() {
		return prefs.get(LAST_WORKSPACE, "");		
	}
	
	public void setLastWorkspace(String val) {
		prefs.put(LAST_WORKSPACE, val);
	}
	
	
}