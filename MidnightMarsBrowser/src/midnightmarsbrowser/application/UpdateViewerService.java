package midnightmarsbrowser.application;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;


public class UpdateViewerService {

	Display display;
	
	private ArrayList myListeners = new ArrayList();
	
	ImageData imageData;
	String title;
	String roverCode; 
	int sol;
	String pathname;
	
	public UpdateViewerService(Display display) {
		this.display = display;
	}
	
	public synchronized ImageData getImageData() {
		return imageData;
	}

	public synchronized String getPathname() {
		return pathname;
	}

	public synchronized String getRoverCode() {
		return roverCode;
	}

	public synchronized int getSol() {
		return sol;
	}

	public synchronized String getTitle() {
		return title;
	}

	public synchronized void addListener(UpdateViewerListener listener) {
		if (!myListeners.contains(listener)) {
			myListeners.add(listener);
		}
	}

	public synchronized void removeListener(UpdateViewerListener listener) {
		myListeners.remove(listener);
	}
	
	public synchronized void newUpdateImage(ImageData imageData, String title, String roverCode, 
			int sol, String pathname) {
		this.imageData = imageData;
		this.title = title;
		this.roverCode = roverCode;
		this.sol = sol;
		this.pathname = pathname;
		for (Iterator iter = myListeners.iterator(); iter.hasNext();) {
			UpdateViewerListener listener = (UpdateViewerListener) iter
					.next();
			display.asyncExec(new UpdateViewerServiceRunnable(listener));
		}
	}
	
	class UpdateViewerServiceRunnable implements Runnable {
		UpdateViewerListener listener;
		
		UpdateViewerServiceRunnable(UpdateViewerListener listener) {
			this.listener = listener;
		}

		public void run() {
			listener.newUpdateImage();
		}
	}
}
