package de.hechler.encrypt.cloudbackup.pcloud;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.pcloud.sdk.ApiClient;
import com.pcloud.sdk.ApiError;
import com.pcloud.sdk.RemoteFile;
import com.pcloud.sdk.RemoteFolder;

import de.hechler.encrypt.pgp.Decrypter;
import de.hechler.encrypt.pgp.Decrypter.DecryptResult;
import de.hechler.encrypt.utils.Utils;

/**
 * 
 * @author feri
 */

public class PCloudDownloader {


	public void downloadFile(Path remotePath, Path localFile, Decrypter decrypter)  {
		try {
			ApiClient apiClient = PCloudApiClient.getApiClient();
			if (!Files.exists(localFile.getParent())) {
				Files.createDirectories(localFile.getParent());
			}
			String filename = remotePath.getFileName().toString();
			if (!filename.endsWith(".pgp")) {
				throw new RuntimeException("Unencrypted file in Backup "+Utils.rPath(remotePath));
			}
			RemoteFile remoteFile = apiClient.loadFile(Utils.rPath(remotePath)).execute();
			try (InputStream in = remoteFile.byteStream()) {
				DecryptResult result = decrypter.decrypt(in, new FileOutputStream(localFile.toFile()));
				if (!result.comments.contains("sha256="+result.targetSHA256)) {
					Files.delete(localFile);
					throw new RuntimeException("WRONG HASHCODE! "+localFile+" "+result.targetSHA256+" (not in "+result+")");
				}
			}
		} catch (IOException | ApiError e) {
			throw new RuntimeException(e.toString(), e);
		}
	}

	
	public long recursiveCreateFolder(ApiClient apiClient, Path folder) {
		try {
			if (folder == null) {
				return RemoteFolder.ROOT_FOLDER_ID;
			}
			try {
				RemoteFolder fetchedFolder = apiClient.loadFolder(Utils.rPath(folder)).execute();
				return fetchedFolder.folderId();
			}
			catch (ApiError e) {
				if (e.errorCode() != 2005) { // Directory does not exist.
					throw e;
				}
			}
			long parentID = recursiveCreateFolder(apiClient, folder.getParent());
			RemoteFolder newFolder = apiClient.createFolder(parentID, folder.getFileName().toString()).execute();
			return newFolder.folderId();
		} catch (IOException | ApiError e) {
			throw new RuntimeException("Error creating folder '"+folder+"': "+e.toString(), e);
		}
	}

	
}
