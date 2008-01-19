package midnightmarsbrowser.metadata;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import midnightmarsbrowser.application.MMBWorkspace;

abstract class Metadata {
	
	String metadataName;
	
	abstract void read(Reader reader) throws IOException;

	abstract void write(Writer writer) throws IOException;
	
	abstract void stage(MMBWorkspace workspace) throws IOException;
	
	Metadata(String metadataName) {
		this.metadataName = metadataName;
	}
	
	public void write(MMBWorkspace workspace) throws IOException {
		if (!workspace.getMetadataLocalDir().exists()) {
			workspace.getMetadataLocalDir().mkdirs();
		}
		File file = new File(workspace.getMetadataLocalDir(), metadataName + ".csv");
		write(file);
	}
	
	/**
	 * Write update metadata to staging area.
	 * If archive file does not exist then update metadata is written to archive and update file is made empty.
	 * @param workspace
	 * @throws IOException
	 */
	protected void writeToStage(MMBWorkspace workspace) throws IOException {
		if (!workspace.getMetadataStageDir().exists()) {
			workspace.getMetadataStageDir().mkdirs();
		}
		File archiveFile = new File(workspace.getMetadataStageDir(), metadataName + "_archive.csv");
		File updateFile = new File(workspace.getMetadataStageDir(), metadataName + "_update.csv");
		if (archiveFile.isFile()) {
			write(updateFile);
		}
		else {
			write(archiveFile);
			updateFile.delete();
			updateFile.createNewFile();
		}		
	}
	
	private void write(File file) throws IOException {
		Writer writer = new BufferedWriter(new FileWriter(file));
		this.write(writer);
		writer.flush();
		writer.close();		
	}
	
	public void read(MMBWorkspace workspace) throws IOException {
		boolean readLocal = readLocal(workspace);
		if (!readLocal) {
			// if no local, get from downloaded
			// TODO do we want to instead overwrite downloaded with local?
			readDownloadedArchive(workspace);
			readDownloadedUpdate(workspace);
		}
	}
	
	public boolean readLocal(MMBWorkspace workspace) throws IOException {
		File file = new File(workspace.getMetadataLocalDir(), metadataName + ".csv");
		return read(file);
	}
	
	public boolean readDownloadedArchive(MMBWorkspace workspace) throws IOException {
		File file = new File(workspace.getMetadataDownloadedDir(), metadataName + "_archive.csv");
		return read(file);		
	}
	
	public boolean readDownloadedUpdate(MMBWorkspace workspace) throws IOException {
		File file = new File(workspace.getMetadataDownloadedDir(), metadataName + "_update.csv");
		return read(file);
	}
	
	protected boolean readStageArchive(MMBWorkspace workspace) throws IOException {
		File file = new File(workspace.getMetadataStageDir(), metadataName + "_archive.csv");
		return read(file);		
	}
	
	private boolean read(File file) throws IOException {
		if (file.isFile()) {
			Reader reader = new BufferedReader(new FileReader(file));
			this.read(reader);
			reader.close();
			return true;
		}
		return false;
	}
}
