package de.hechler.encrypt.cloudbackup.pcloud;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.pcloud.sdk.ApiClient;
import com.pcloud.sdk.ApiError;
import com.pcloud.sdk.Authenticators;
import com.pcloud.sdk.DataSink;
import com.pcloud.sdk.PCloudSdk;
import com.pcloud.sdk.RemoteEntry;
import com.pcloud.sdk.RemoteFile;
import com.pcloud.sdk.RemoteFolder;

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

public class PCloudReader {


	private static PCloudConfig config;
	public static PCloudConfig getConfig() {
		if (config == null) {
			config = new PCloudConfig();
		}
		return config;
	}


	private static ApiClient internApiClient;
	public static synchronized ApiClient getApiClient() {
		if (internApiClient == null) {
			internApiClient = PCloudSdk.newClientBuilder()
					.authenticator(Authenticators.newOAuthAuthenticator(getConfig().getAccessToken()))
					.apiHost(getConfig().getApiHost())
					// Other configuration...
					.create();
		}
		return internApiClient;
	}
	public static synchronized void shutdownApiClient() {
		if (internApiClient == null) {
			return;
		}
		internApiClient.shutdown();
		internApiClient = null;
	}
	

	public void readRecursive(Path folder) {
		try {
			RemoteFolder rFolder = getApiClient().listFolder(rPath(folder), true).execute();
			readRecursive(folder, rFolder);
		} catch (ApiError | IOException e) {
			throw new RuntimeException(e.toString(), e);
		}
	}

	public void readRecursive(Path folder, long folderID) {
		try {
			RemoteFolder rFolder = getApiClient().listFolder(folderID, true).execute();
			readRecursive(folder, rFolder);
		} catch (ApiError | IOException e) {
			throw new RuntimeException(e.toString(), e);
		}
	}

	private void readRecursive(Path folder, RemoteFolder rFolder) {
		for (RemoteEntry entry : rFolder.children()) {
			if (entry.isFile()) {
				readFileHeader(entry.asFile());
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
			System.out.println("1.) " + line);
			if (!line.equals("-----BEGIN PGP MESSAGE-----")) {
				return;
			}
			line = source.readUtf8Line();
			System.out.println("2.) " + line);
			line = source.readUtf8Line();
			System.out.println("3.) " + line);
			while ((line != null) && line.startsWith("Comment: ")) {
				comments.add(line.substring(9));
				line = source.readUtf8Line();
				System.out.println("4+.) " + line);
			}
		}

		public List<String> getComments() {
			return comments;
		}
	}
	
	private void readFileHeader(RemoteFile rFile) {
		try {
			HeaderReceiverDataSink ds = new HeaderReceiverDataSink();
			rFile.download(ds);
			System.out.println(rFile.name()+": "+ds.getComments());
		} catch (IOException e) {
			throw new RuntimeException(e.toString(), e);
		}
	}

	
	
	
	public static String rPath(Path folder) {
		String result = folder.toString().replace('\\', '/');
		if (!result.startsWith("/")) {
			result = "/"+result;
		}
		return result;
	}

	private static long id2long(String idStr) {
		return Long.parseLong(idStr.replace("f", "").replace("d", ""));
	}

	
	public static void main(String[] args) {
		PCloudReader reader = new PCloudReader();
		reader.readRecursive(Paths.get("/"));

	}
	
}
