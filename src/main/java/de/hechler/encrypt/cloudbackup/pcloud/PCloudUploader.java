package de.hechler.encrypt.cloudbackup.pcloud;

import java.io.IOException;
import java.nio.file.Path;

import com.pcloud.sdk.ApiClient;
import com.pcloud.sdk.ApiError;
import com.pcloud.sdk.DataSource;
import com.pcloud.sdk.RemoteFile;
import com.pcloud.sdk.RemoteFolder;
import com.pcloud.sdk.UploadOptions;

import de.hechler.encrypt.utils.Utils;

/**
 * 
 * @author feri
 */

public class PCloudUploader {


	public long uploadFile(Path localFile, Path remotePath)  {
		ApiClient apiClient = PCloudApiClient.getApiClient();
		String filename = remotePath.getFileName().toString();
		long result = -1;
		try {
			long folderId = recursiveCreateFolder(apiClient, remotePath.getParent());
			
			RemoteFile rFile = apiClient.createFile(folderId, filename, DataSource.create(localFile.toFile()), UploadOptions.OVERRIDE_FILE).execute();
			result = rFile.fileId();
        } catch (IOException | ApiError e) {
            throw new RuntimeException("Error upload file '"+remotePath+"': "+e.toString(), e);
        }
        return result;
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
