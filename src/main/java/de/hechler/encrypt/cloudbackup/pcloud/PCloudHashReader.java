package de.hechler.encrypt.cloudbackup.pcloud;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.pcloud.sdk.ApiError;
import com.pcloud.sdk.DataSink;
import com.pcloud.sdk.RemoteEntry;
import com.pcloud.sdk.RemoteFile;
import com.pcloud.sdk.RemoteFolder;

import de.hechler.encrypt.utils.Utils;
import okio.BufferedSource;

/**
 * Test using pCloud API - Java SDK https://github.com/pCloud/pcloud-sdk-java
 * 
 * SDK docu: https://pcloud.github.io/pcloud-sdk-java/
 * 
 * API docu: https://docs.pcloud.com/
 * 
 * @author feri
 */

public class PCloudHashReader {

	private Map<String, String> filename2hashMap;
	
	public PCloudHashReader() {
		this.filename2hashMap = new LinkedHashMap<>();
	}

	public Map<String, String> readRecursive(Path folder) {
		try {
			RemoteFolder rFolder = PCloudApiClient.getApiClient().listFolder(Utils.rPath(folder), true).execute();
			readRecursive(folder, rFolder);
			return filename2hashMap;
		} catch (ApiError | IOException e) {
			throw new RuntimeException(e.toString(), e);
		}
	}

	public void readRecursive(Path folder, long folderID) {
		try {
			RemoteFolder rFolder = PCloudApiClient.getApiClient().listFolder(folderID, true).execute();
			readRecursive(folder, rFolder);
		} catch (ApiError | IOException e) {
			throw new RuntimeException(e.toString(), e);
		}
	}

	private void readRecursive(Path folder, RemoteFolder rFolder) {
		for (RemoteEntry entry : rFolder.children()) {
			if (entry.isFile()) {
				readFileHeader(folder, entry.asFile());
			}
		}
		for (RemoteEntry entry : rFolder.children()) {
			if (entry.isFolder()) {
				readRecursive(folder.resolve(entry.name()), entry.asFolder());
			}
		}
	}		
	
	
	static class HeaderReceiverDataSink extends DataSink {

		private List<String> comments = new ArrayList<>();
		
		@Override
		public void readAll(BufferedSource source) throws IOException {
			String line = source.readUtf8Line();
			if (!line.equals("-----BEGIN PGP MESSAGE-----")) {
				return;
			}
			line = source.readUtf8Line();
			line = source.readUtf8Line();
			while ((line != null) && line.startsWith("Comment: ")) {
				comments.add(line.substring(9));
				line = source.readUtf8Line();
			}
		}

		public List<String> getComments() {
			return comments;
		}
	}
	
	private void readFileHeader(Path folder, RemoteFile rFile) {
		try {
			HeaderReceiverDataSink ds = new HeaderReceiverDataSink();
			rFile.download(ds);
			String fullPath = Utils.rPath(folder.resolve(rFile.name()));
			String hash = "invalid";
			for (String comment:ds.getComments()) {
				if (comment.startsWith("sha256=")) {
					hash = comment.substring(7);
				}
			}
			filename2hashMap.put(fullPath, hash);
			System.out.println("  o "+Utils.noPGP(fullPath));
		} catch (IOException e) {
			throw new RuntimeException(e.toString(), e);
		}
	}

	
	public static void main(String[] args) {
		PCloudHashReader reader = new PCloudHashReader();
		reader.readRecursive(Paths.get("/"));

	}
	
}
