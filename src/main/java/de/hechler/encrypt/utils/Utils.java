package de.hechler.encrypt.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

public class Utils {
	
	public static String bytes2hex(byte[] bytes) {
		StringBuilder result = new StringBuilder();
		for (byte b : bytes) {
		    result.append(String.format("%02x", b));
		}
		return result.toString();
	}

	public static String rPath(Path folder) {
		String result = folder.toString().replace('\\', '/');
		if (!result.startsWith("/")) {
			result = "/"+result;
		}
		return result;
	}
	
	public static String noPGP(String filename) {
		if (filename.endsWith(".pgp")) {
			return filename.substring(0, filename.length()-4);
		}
		return filename+"?.pgp?";
	}
	
	public static String calcFileSHA256(Path inputFile) {
		try {
			ChecksumInputStream cin = new ChecksumInputStream("SHA-256", new FileInputStream(inputFile.toFile()));
			byte[] buf = new byte[32768];
			while (cin.read(buf) > 0) {}
			cin.close();
			return cin.getMD();
		} catch (IOException e) {
			throw new RuntimeException(e.toString(), e);
		}
	}
	
	private static String TEMP_FOLDER = System.getProperty("java.io.tmpdir");
	private static AtomicInteger count = new AtomicInteger(1);
	public static void setTempFolder(String tempFolder) {
		TEMP_FOLDER = tempFolder;
	}
	
	public static Path getTempPath() {
		return Paths.get(TEMP_FOLDER).resolve("PGPCloudVackup-TEMP-"+count.incrementAndGet()+".tmp");
	}
	
	
	
}
