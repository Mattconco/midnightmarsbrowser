package midnightmarsbrowser.model;

import midnightmarsbrowser.metadata.LocationMetadataEntry;

/**
 * Represents each discreet frame of an animation.
 * @author michaelhoward
 *
 */
public class PanPosition {
	public String spacecraftId;
	public LocationCounter location;
	public float viewAz;
	public float viewEl;
	public float viewVFOV;
	// The LocationMetadataEntry may be made up (interpolated from the data)
	public LocationCounter roverModelLocation;
	public LocationMetadataEntry roverModelLocationMetadata;	
	// reference to the PanMovieEndpoint for which this is an interpolated point
	public PanMovieEndpoint panMovieEndpoint;
	
	public String toString() {
		return "PanPosition "+spacecraftId+" "+location.site+" "+location.drive+" "+viewAz+" "+viewEl+" "+viewVFOV;
	}
}
