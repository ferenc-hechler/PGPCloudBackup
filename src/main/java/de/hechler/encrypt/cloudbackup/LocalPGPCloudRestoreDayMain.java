package de.hechler.encrypt.cloudbackup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 
 * @author feri
 */

public class LocalPGPCloudRestoreDayMain {
	
	public static void main(String[] args) throws IOException {
		PGPCloudRestoreMain.DEFAULT_CONFIG = "DONOTCHECKIN/.pcloud-config";
		PGPCloudRestoreMain.DEFAULT_KEY = "DONOTCHECKIN/.private-decrypt.key";
		PGPCloudRestoreMain.DEFAULT_KEY_PASSPHRASE = new String(Files.readAllBytes(Paths.get("DONOTCHECKIN/enc-passphrase")));
		PGPCloudRestoreMain.DEFAULT_FOLDER = "./testdata/restore";
		PGPCloudRestoreMain.DEFAULT_TEMP_FOLDER = System.getProperty("java.io.tmpdir");
		PGPCloudRestoreMain.DEFAULT_REMOTE_FOLDER = "/VSERVERBACKUP/TEST";
		PGPCloudRestoreMain.DEFAULT_FILTER_DATE = "2023-01-03";
		PGPCloudRestoreMain.DEFAULT_DELETE_LOCAL= false;
//		PGPCloudRestoreMain.main(new String[] {"-P", PGPCloudRestoreMain.DEFAULT_KEY_PASSPHRASE});
		PGPCloudRestoreMain.main(args);
	}

}
