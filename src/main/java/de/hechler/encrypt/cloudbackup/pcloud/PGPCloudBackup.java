package de.hechler.encrypt.cloudbackup.pcloud;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.Map;

import de.hechler.encrypt.pgp.Encrypter;
import de.hechler.encrypt.pgp.Encrypter.EncryptResult;
import de.hechler.encrypt.utils.Utils;

/**
 * 
 * @author feri
 */

public class PGPCloudBackup {

	private Path publicEncryptionKey;
	private Path pathToBackup;
	private Path remoteBaseFolder;
	
	private Map<String, String> filename2hashMap;
	PCloudUploader uploader;
	Encrypter encrypter;
	
	public PGPCloudBackup(Path publicEncryptionKey, Path pathToBackup, Path remoteBaseFolder) {
		this.publicEncryptionKey = publicEncryptionKey;
		this.pathToBackup = pathToBackup;
		this.remoteBaseFolder = remoteBaseFolder;
		this.uploader = new PCloudUploader();
		this.encrypter = new Encrypter(this.publicEncryptionKey);
	}
	
	public void startBackup() {
		readRemoteFiles();
		backupFiles();
		// cleanupUnreferencedFiles();
	}
	
	public void readRemoteFiles() {
		PCloudHashReader reader = new PCloudHashReader();
		// optimization: cache results in file and only do update missing files
		filename2hashMap = reader.readRecursive(remoteBaseFolder);
		System.out.println("[current files in cloud backup]");
		for (String filename:filename2hashMap.keySet()) {
			System.out.println("  "+ filename);
		}
	}
	
	public void backupFiles() {
		recursiveBackupFolder(pathToBackup, remoteBaseFolder);
	}

	private void recursiveBackupFolder(Path local, final Path remoteFolder) {
		try {
			Files.list(local).filter(Files::isRegularFile).forEach(p -> backupFile(p, remoteFolder));
			Files.list(local).filter(Files::isDirectory).forEach(p -> recursiveBackupFolder(p, remoteFolder.resolve(p.getFileName())));
		} catch (IOException e) {
			throw new RuntimeException("error reading "+local+": "+e.toString(), e);
		}
	}

	private void backupFile(Path localFile, Path remoteFolder) {
		try {
			String remoteFilename = rPath(remoteFolder.resolve(localFile.getFileName()))+".pgp";
			String remoteSHA256 = filename2hashMap.get(remoteFilename);
			String sha256 = null;
			if (remoteSHA256 != null) {
				sha256 = Utils.calcFileSHA256(localFile);
				if (remoteSHA256.equals(sha256)) {
					System.out.println("SKIPING: "+remoteFilename);
					return;
				}
			}
			String name = localFile.getFileName().toString();
			FileTime time = Files.getLastModifiedTime(localFile);
			long filesize = Files.size(localFile);
			if (sha256 == null) {
				sha256 = Utils.calcFileSHA256(localFile);
			}
			
			String comment = "sha256="+sha256
					+ "\nsize=" + filesize
					+ "\ntime=" + time.toString()
					+ "\nname=" +name;
			if (encrypter.getKeyName() != null) {
				comment += "\nkey=" + encrypter.getKeyName();
			}
			Path tempFile = Utils.getTempPath();
			System.out.println("ENCRYPTING: "+remoteFilename);
			encrypter.encrypt(localFile, tempFile, comment);
			System.out.println("  - uploading");
			uploader.uploadFile(tempFile, Paths.get(remoteFilename));
		}
		catch (IOException e) {
			throw new RuntimeException("error creating backup for "+localFile+": "+e.toString(), e);
		}
	}

	
	public static String rPath(Path folder) {
		String result = folder.toString().replace('\\', '/');
		if (!result.startsWith("/")) {
			result = "/"+result;
		}
		return result;
	}

}
