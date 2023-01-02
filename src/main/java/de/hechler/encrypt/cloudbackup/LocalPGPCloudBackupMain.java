package de.hechler.encrypt.cloudbackup;

/**
 * 
 * @author feri
 */

public class LocalPGPCloudBackupMain {
	
	public static void main(String[] args) {
		PGPCloudBackupMain.DEFAULT_CONFIG = "DONOTCHECKIN/.pcloud-config";
		PGPCloudBackupMain.DEFAULT_KEY = "encrypt-key.pub";
		PGPCloudBackupMain.DEFAULT_FOLDER = "./testdata";
		PGPCloudBackupMain.DEFAULT_TEMP_FOLDER = System.getProperty("java.io.tmpdir");
		PGPCloudBackupMain.DEFAULT_REMOTE_FOLDER = "/VSERVERBACKUP/TEST";
		PGPCloudBackupMain.DEFAULT_DELETE_REMOTE = true;
		PGPCloudBackupMain.main(args);
	}

}
