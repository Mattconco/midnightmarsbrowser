/**
 *  Midnight Mars Browser - http://midnightmarsbrowser.blogspot.com
 *  Copyright (c) 2005-2007 by Michael R. Howard
 *
 *  Midnight Mars Browser is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 * 
 *  Midnight Mars Browser is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Midnight Mars Browser; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 */

package midnightmarsbrowser.application;

import midnightmarsbrowser.dialogs.ChooseWorkspaceDialog;

import org.eclipse.core.runtime.IPlatformRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

/**
 * This class controls all aspects of the application's execution
 */
public class Application implements IPlatformRunnable {

	// TODO?
	public static final String PLUGIN_ID = "MidnightMarsBrowser.application";

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IPlatformRunnable#run(java.lang.Object)
	 */
	public Object run(Object args) throws Exception {
		Display display = PlatformUI.createDisplay();
		try {
			if (!chooseWorkspace())
				return IPlatformRunnable.EXIT_OK;			
			
			updateConsoleService = new UpdateConsoleService(display);
			updateViewerService = new UpdateViewerService(display);
			
			int returnCode = PlatformUI.createAndRunWorkbench(display, new ApplicationWorkbenchAdvisor());
			if (returnCode == PlatformUI.RETURN_RESTART) {
				return IPlatformRunnable.EXIT_RESTART;
			}
			return IPlatformRunnable.EXIT_OK;
		} finally {
			display.dispose();
		}
	}
	
	private static MMBWorkspace workspace;
	
	private static UpdateConsoleService updateConsoleService;
	
	private static UpdateViewerService updateViewerService;
	
	public static MMBWorkspace getWorkspace() {
		return workspace;
	}
	
	public static UpdateConsoleService getUpdateConsoleService() {
		return updateConsoleService;
	}
	
	public static UpdateViewerService getUpdateViewerService() {
		return updateViewerService;
	}
	
	public static boolean isAdminMode() {
		String adminProp = System.getProperty("admin");
		return ((adminProp != null) && (adminProp.equalsIgnoreCase("true")));
	}
	
	public static boolean isAdvancedMode() {
		String prop = System.getProperty("advanced");
		return isAdminMode() || (prop != null && prop.equalsIgnoreCase("true"));
	}
	
	private static long maxPanBytes = 0;
	
	public static long getMaxPanBytes() {
		if (maxPanBytes == 0) {
			maxPanBytes = 90000000;
			String prop = null;
			try {
				prop = System.getProperty("maxPanBytes");
				if (prop != null) {
					int multFactor = 1;
					if (prop.endsWith("m") || prop.endsWith("M")) {
						multFactor = 1000000;
						prop = prop.substring(0, prop.length()-1);
					}
					else if (prop.endsWith("g") || prop.endsWith("G")) {
						multFactor = 1000000000;
						prop = prop.substring(0, prop.length()-1);
					}
					maxPanBytes = Integer.parseInt(prop) * multFactor;
				}
			}
			catch (Exception e) {
				System.out.println("Could not read maxPanBytes property: "+prop);
			}
		}
		return maxPanBytes;
	}
	
	private boolean chooseWorkspace() {
		String message = null;
		
		String lastWorkspace = InstancePreferences.getInstance().getLastWorkspace();
		
		if ((lastWorkspace.length() > 0)) {
			if (MMBWorkspace.workspaceExists(lastWorkspace)) {
				try {
					workspace = new MMBWorkspace(lastWorkspace);
					workspace.startWorkspace();
					return true;
				}
				catch (Exception e) {
					// TODO error handling
					e.printStackTrace();
					return false;
				}
			}			
		}		
		else {
			try {
				// See if there is an existing MMB1.x preferences file
				MMB1Prefs oldPrefs = MMB1Prefs.findMMB1Prefs();
				if (oldPrefs != null) {
					if ((oldPrefs.homeDir != null) && (oldPrefs.rawImageDir != null)
							&& (oldPrefs.generatedImageDir != null) 
							&& (oldPrefs.rawImageDir.getParentFile().equals(oldPrefs.homeDir))
							&& (oldPrefs.generatedImageDir.getParentFile().equals(oldPrefs.homeDir))	
					) {
						message = "An existing MMB workspace was detected.";
						lastWorkspace = oldPrefs.homeDir.getAbsolutePath();
					}
					else {
						message = "A MMB 1.x workspace was detected by the rawImages or generatedImages "
							 + "directory was not located under the home directory of "
							 + oldPrefs.homeDir.getAbsolutePath() +".";						
					}
				}
				else {
					message = "You must select a workspace directory, where Midnight Mars Browser will store image and metadata files.";
				}
			}
			catch (Exception e) {
				message = "There was an error attempting to find an existing MMB 1.x workspace: "+e.toString();
			}
		}
		
		Platform.endSplash();
		ChooseWorkspaceDialog dialog = new ChooseWorkspaceDialog(null, lastWorkspace, message, false);
		if (dialog.open() != Window.OK) {
			return false;
		}
		try {
			workspace = new MMBWorkspace(dialog.getDirectory());
			workspace.startWorkspace();
		}
		catch (Exception e) {
			// TODO error handling
			e.printStackTrace();
			return false;
		}
		InstancePreferences.getInstance().setLastWorkspace(dialog.getDirectory());
		InstancePreferences.getInstance().flush();
		workspace.writeProperties();
		return true;
	}
}
