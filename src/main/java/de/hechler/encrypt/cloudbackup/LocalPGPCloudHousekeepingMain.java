package de.hechler.encrypt.cloudbackup;

import java.io.IOException;

/**
 * 
 * @author feri
 */

public class LocalPGPCloudHousekeepingMain {
	
	public static void main(String[] args) throws IOException {
		PGPCloudHousekeepingMain.DEFAULT_CONFIG = "DONOTCHECKIN/.pcloud-config";
		PGPCloudHousekeepingMain.DEFAULT_REMOTE_FOLDER = "/VSERVERBACKUP/TEST";
		PGPCloudHousekeepingMain.DEFAULT_KEEP_PERIODS = "2d";
		PGPCloudHousekeepingMain.DEFAULT_IGNORE_MISSING_TIMESTAMPS = true;
//		PGPCloudHousekeepingMain.main(new String[] {"-P", PGPCloudHousekeepingMain.DEFAULT_KEY_PASSPHRASE});
		PGPCloudHousekeepingMain.main(args);
	}

}
