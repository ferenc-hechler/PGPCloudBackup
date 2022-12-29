package de.hechler.encrypt.cloudbackup.pcloud;

import java.nio.file.attribute.FileTime;

/**
 * 
 * @author feri
 */

public class RemoteFileInfo {

	String filename;
	String folder;
	long filesize;
	String sha256;
	long rHash;
	FileTime lastModified;
	
}
