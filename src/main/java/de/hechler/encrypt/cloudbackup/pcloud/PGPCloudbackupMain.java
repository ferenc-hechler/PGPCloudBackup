package de.hechler.encrypt.cloudbackup.pcloud;

import java.io.FileInputStream;
import java.nio.file.Paths;

/**
 * 
 * @author feri
 */

public class PGPCloudbackupMain {

	private static String DEFAULT_BASE_FOLDER = "VSERVERBACKUP";

	public static void main(String[] args) {
		try {
			System.out.println("START");

			PCloudConfig.setConfigFilename("DONOTCHECKIN/.pcloud-config");

			PCloudUploader uploader = new PCloudUploader();
			uploader.uploadFile(Paths.get(DEFAULT_BASE_FOLDER+"/TEST/testdatei.txt.pgp"), new FileInputStream("testdata/input/testdatei.txt.pgp"));
			uploader.uploadFile(Paths.get(DEFAULT_BASE_FOLDER+"/TEST/testdatei.txt"), new FileInputStream("testdata/input/testdatei.txt"));
			
			PCloudReader reader = new PCloudReader();
			reader.readRecursive(Paths.get(DEFAULT_BASE_FOLDER));

			System.out.println("FINISHED");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
