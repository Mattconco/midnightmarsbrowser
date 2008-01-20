package midnightmarsbrowser.application;

public interface UpdateViewerListener {

	/**
	 * Write a string to this console listener. 
	 * This must always be called from the UI thread. 
	 * The implementing class should check if any UI part has been disposed, 
	 * since this is probably scheduled to be called asynchronously.
	 */
	public void newUpdateImage();
}
