package de.hechler.encrypt.cloudbackup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import de.hechler.encrypt.cloudbackup.pcloud.PCloudDownloader;
import de.hechler.encrypt.cloudbackup.pcloud.PCloudHashReader;
import de.hechler.encrypt.pgp.Decrypter;
import de.hechler.encrypt.utils.Utils;

/**
 * 
 * @author feri
 */

public class PGPCloudRestore {

	private Path privateDecryptionKey;
	private String passphrase;
	private Path pathToRestore;
	private Path remoteBaseFolder;
	private boolean deleteLocal;
	
	Decrypter decrypter;
	PCloudDownloader downloader;
	
	private Map<String, String> filename2hashMap;
	private Set<String> existingFiles;
	
	public PGPCloudRestore(Path privateDecryptionKey, String passphrase, Path pathToRestore, Path remoteBaseFolder, boolean deleteLocal) {
		this.privateDecryptionKey = privateDecryptionKey;
		this.passphrase = passphrase;
		this.pathToRestore = pathToRestore;
		this.remoteBaseFolder = remoteBaseFolder;
		this.deleteLocal = deleteLocal;
		this.decrypter = new Decrypter(privateDecryptionKey, passphrase);
		this.downloader = new PCloudDownloader();
	}
	
	public void startRestore() {
		readRemoteFiles();
		cleanupLocalFiles();
		restoreFiles();
	}
	

	public void readRemoteFiles() {
		System.out.println();
		System.out.println("[reading remote files from "+Utils.rPath(remoteBaseFolder)+"]");
		PCloudHashReader reader = new PCloudHashReader();
		// optimization: cache results in file and only do update missing files
		filename2hashMap = reader.readRecursive(remoteBaseFolder, true);
		existingFiles = new LinkedHashSet<>();
	}
	

	private void cleanupLocalFiles() {
		if (!deleteLocal) {
			System.out.println("existing local files are not deleted, option -d is not set.");
			return;
		}
		System.out.println("[cleanup files in "+pathToRestore+"]");
		recursiveCleanupFolder(pathToRestore, remoteBaseFolder);
	}

	private void recursiveCleanupFolder(Path localFolder, final Path remoteFolder) {
		try {
			Files.list(localFolder).filter(Files::isRegularFile).forEach(p -> recursiveCleanupFile(p, remoteFolder));
			Files.list(localFolder).filter(Files::isDirectory).forEach(p -> recursiveCleanupFolder(p, remoteFolder.resolve(p.getFileName())));
		} catch (IOException e) {
			throw new RuntimeException(e.toString(), e);
		}
	}

	private void recursiveCleanupFile(Path localFile, Path remoteFolder) {
		try {
			String filename = Utils.rPath(remoteFolder.resolve(localFile.getFileName())) + ".pgp";
			String hash = filename2hashMap.get(filename);
			if (hash == null) {
				System.out.println("  - "+Utils.noPGP(filename));
				Files.delete(localFile);
				return;
			}
			String localHash = Utils.calcFileSHA256(localFile);
			if (localHash.equals(hash)) {
				System.out.println("  o "+Utils.noPGP(filename));
				existingFiles.add(filename);
				return;
			}
			System.out.println("  # "+Utils.noPGP(filename));
			Files.delete(localFile);
		} catch (IOException e) {
			throw new RuntimeException(e.toString(), e);
		}
	}


	private void restoreFiles() {
		String baseFolder = Utils.rPath(remoteBaseFolder);
		System.out.println();
		System.out.println("[restoring files from "+baseFolder+"]");
		Set<String> filesToRestore = new LinkedHashSet<>(filename2hashMap.keySet());
		filesToRestore.removeAll(existingFiles);
		for (String fileToRestore:filesToRestore) {
			if (!fileToRestore.startsWith(baseFolder)) {
				throw new RuntimeException("unexpected filename "+fileToRestore+" should start with "+baseFolder);
			}
			restoreFile(fileToRestore, Utils.noPGP(fileToRestore.substring(baseFolder.length()+1)));
		}
	}

	private void restoreFile(String remotePath, String localRelPath) {
		Path localFile = pathToRestore.resolve(localRelPath);
		if (Files.isRegularFile(localFile)) {
			String sha256 = Utils.calcFileSHA256(localFile);
			if (sha256.equals(filename2hashMap.get(remotePath))) {
				System.out.println("  o "+Utils.noPGP(remotePath));
				return;
			}
		}
		System.out.println("  + "+Utils.noPGP(remotePath));
		downloader.downloadFile(Paths.get(remotePath), localFile, decrypter);
	}

}
