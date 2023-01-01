package de.hechler.encrypt.cloudbackup.pcloud;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.pcloud.sdk.ApiError;
import com.pcloud.sdk.RemoteEntry;
import com.pcloud.sdk.RemoteFolder;

import de.hechler.encrypt.utils.Utils;

/**
 * Test using pCloud API - Java SDK https://github.com/pCloud/pcloud-sdk-java
 * 
 * SDK docu: https://pcloud.github.io/pcloud-sdk-java/
 * 
 * API docu: https://docs.pcloud.com/
 * 
 * @author feri
 */

public class PCloudTreeReader {

	private List<String> filenames;
	
	public PCloudTreeReader() {
		this.filenames = new ArrayList<>();
	}

	public List<String> readRecursive(Path folder) {
		try {
			RemoteFolder rFolder = PCloudApiClient.getApiClient().listFolder(Utils.rPath(folder), true).execute();
			readRecursive(folder, rFolder);
			return filenames;
		} catch (ApiError e) {
			if (e.errorCode() == 2005) {
				throw new RuntimeException("Remote folder '"+Utils.rPath(folder)+"' does not exist!", e);
			}
			throw new RuntimeException(e.toString(), e);
		} catch (IOException e) {
			throw new RuntimeException(e.toString(), e);
		}
	}

	private void readRecursive(Path folder, RemoteFolder rFolder) {
		for (RemoteEntry entry : rFolder.children()) {
			if (entry.isFile()) {
				String fullPath = Utils.rPath(folder.resolve(entry.name()));
				filenames.add(fullPath);
			}
		}
		for (RemoteEntry entry : rFolder.children()) {
			if (entry.isFolder()) {
				readRecursive(folder.resolve(entry.name()), entry.asFolder());
			}
		}
	}		
	
	
	
}
