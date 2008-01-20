package midnightmarsbrowser.application;

public interface IUpdateTask {

	public void logln(String line);
	
	public void logln();
	
	public void debug(String line);
	
	public void debugln(String line);

	public void log(String str);

	public void checkForTerminate();
}
