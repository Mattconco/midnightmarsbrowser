/*
 * Created on Jan 8, 2005
 *
 *  Midnight Mars Browser - http://midnightmarsbrowser.blogspot.com
 *  Copyright (c) 2005 by Michael R. Howard
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
 */

package midnightmarsbrowser.model;

/**
 * @author michaelhoward
 *
 */
// TODO change this to image criterion?

public class ViewerSettings implements Cloneable {
	
	public boolean panorama = false;	
	
	public String roverCode = null;
	public int fromSol = 0;
	public int toSol = 9999;
	public boolean limitToSet = false;
	
	public boolean splitByLocation = true;
	public boolean splitBySol = false;
	public boolean splitByElapsedTime = false;
	public int splitElapsedTimeMinutes = 15;
	
//	boolean playLoop = false;
//	double playDelay = 0.5;
//	boolean scaleUp = false;
    public int panMaxResolution = 1024;
    public boolean panGroundRelative = true;
    public boolean panShowRoverTracking = true;
    public boolean panShowRoverModel = false;
	
	public String[] includePancamFilter = null;
	public String[] excludePancamFilter = null;
	public String[] includeCmdSeqs = null;
	public String[] excludeCmdSeqs = null;
	public String[] includeSiteDriveNumber = null;
	public boolean excludeSolarFilter = false;
	public boolean excludeSundial = false;
	public boolean excludeDriveImages = false;
	
	public boolean doLimitKeywords = false;
	public String[] limitKeywords = null;
	
/*	
	// product types
	public boolean typeEFF;
	public boolean typeESF;
	public boolean typeEDN;
	public boolean typeOther;
	// Forward hazcam
	public boolean fl;
	public boolean fr;
	public boolean fa;
	// rear hazcam
	public boolean rl;
	public boolean rr;
	public boolean ra;
	// navcam
	public boolean nl;
	public boolean nr;
	public boolean na;
	// pancam
	public boolean pl;
	public boolean pr;
	public boolean pa;
	public boolean pc;
	public boolean ps;
	// micro
	public boolean m = true;
*/
	public boolean f = true;
	public boolean r = true;
	public boolean n = true;
	public boolean p = true;
	public boolean m = true;
	
	public boolean	left = true;
	public boolean	right = true;
	public boolean	anaglyph = true;
	
	public boolean pancamRaw = true;
	public boolean pancamMMBFalseColor = true;
	public boolean pancamDCCalibratedColor = true;
	public boolean pdsEDR = true;
	public boolean pdsMRD = true;
	public boolean pdsRAD = true;
	
    public int pdsBrightnessMinValue = 0;
    public int pdsBrightnessMaxValue = 6000;
	
	public int panoramaPancamFilterSelection = 0;
	
	public boolean	fullFrame = true;
	public boolean	subFrame = true;
	public boolean	downsampled = true;
	
//	public int panoramaResolution = 1024;
//	public int panoramaOrientation = ORIENTATION_GROUND;
		
	public ViewerSettings(String roverCode) {
		this.roverCode = roverCode;
// TODO fix		
//		this.playDelay = MyPreferences.instance().playDelay;
//		this.playLoop = MyPreferences.instance().playLoop;
//		this.scaleUp = MyPreferences.instance().lastScaleUp;
	}

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	
}
