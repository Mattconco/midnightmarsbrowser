package midnightmarsbrowser.application;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.swt.widgets.Display;


public class UpdateConsoleService {

	Display display;
	
	private ArrayList myListeners = new ArrayList();

	private StringBuffer text = new StringBuffer();
	
	public UpdateConsoleService(Display display) {
		this.display = display;
	}
	
	public synchronized void addListener(UpdateConsoleListener listener) {
		if (!myListeners.contains(listener)) {
			myListeners.add(listener);
		}
	}

	public synchronized void removeListener(UpdateConsoleListener listener) {
		myListeners.remove(listener);
	}
	
	public synchronized String getText() {
		return text.toString();
	}

	public synchronized void write(String str) {
		text.append(str);
		for (Iterator iter = myListeners.iterator(); iter.hasNext();) {
			UpdateConsoleListener listener = (UpdateConsoleListener) iter
					.next();
			display.asyncExec(new UpdateConsoleServiceRunnable(listener, str));
		}
	}
	
	public synchronized void writeln(String str) {
		write(str + "\n");
	}
	
	public synchronized void writeln() {
		write("\n");
	}	
	
	class UpdateConsoleServiceRunnable implements Runnable {
		UpdateConsoleListener listener;
		String str;
		
		UpdateConsoleServiceRunnable(UpdateConsoleListener listener, String str) {
			this.listener = listener;
			this.str = str;
		}

		public void run() {
			listener.write(str);
		}
	}
}
