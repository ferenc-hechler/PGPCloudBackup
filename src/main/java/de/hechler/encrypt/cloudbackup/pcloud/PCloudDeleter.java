package de.hechler.encrypt.cloudbackup.pcloud;

import java.io.IOException;
import java.nio.file.Path;

import com.pcloud.sdk.ApiClient;
import com.pcloud.sdk.ApiError;

import de.hechler.encrypt.utils.Utils;

/**
 * 
 * @author feri
 */

public class PCloudDeleter {


	public void deleteFile(Path remotePath)  {
		try {
			ApiClient apiClient = PCloudApiClient.getApiClient();
			String filename = Utils.rPath(remotePath);
			apiClient.deleteFile(filename).execute();
        } catch (IOException | ApiError e) {
            throw new RuntimeException("Error delete file '"+remotePath+"': "+e.toString(), e);
        }
	}

	
}
