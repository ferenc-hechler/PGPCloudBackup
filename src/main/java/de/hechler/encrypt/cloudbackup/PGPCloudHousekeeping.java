package de.hechler.encrypt.cloudbackup;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import de.hechler.encrypt.cloudbackup.pcloud.PCloudDeleter;
import de.hechler.encrypt.cloudbackup.pcloud.PCloudTreeReader;
import de.hechler.encrypt.utils.KeepPeriods;
import de.hechler.encrypt.utils.Utils;

/**
 * 
 * @author feri
 */

public class PGPCloudHousekeeping {

	private Path remoteBaseFolder;
	private String keepPeriodsPattern;
	private boolean ignoreMissingTimestamps;

	private PCloudDeleter deleter;
	
	private List<String> filenames;
	private Set<String> timestamps;
	
	public PGPCloudHousekeeping(Path remoteBaseFolder, String keepPeriodsPattern, boolean ignoreMissingTimestamps) {
		this.remoteBaseFolder = remoteBaseFolder;
		this.keepPeriodsPattern = keepPeriodsPattern;
		this.ignoreMissingTimestamps = ignoreMissingTimestamps;
		this.filenames = new ArrayList<>();
		this.deleter = new PCloudDeleter();
	}
	
	public void startHousekeeping() {
		readRemoteFiles();
		collectTimestamps();
		deleteOutdatedFiles();
	}
	

	public void readRemoteFiles() {
		System.out.println();
		System.out.println("[reading remote files from "+Utils.rPath(remoteBaseFolder)+"]");
		PCloudTreeReader reader = new PCloudTreeReader();
		filenames = reader.readRecursive(remoteBaseFolder);
	}
	
	private void collectTimestamps() {
		timestamps = new LinkedHashSet<>();
		for (String filename:filenames) {
			String timestamp = Utils.extractTimestamp(filename);
			if (timestamp == null) {
				if (ignoreMissingTimestamps) {
					continue;
				}
				throw new RuntimeException("Remote file without timestamp found: '"+filename+"'");
			}
			timestamps.add(timestamp);
		}
	}

	private void deleteOutdatedFiles() {
		KeepPeriods kp = new KeepPeriods(keepPeriodsPattern);
		List<String> keepDates = kp.filterKeep(timestamps);
		System.out.println("keeping: "+keepDates);
		List<String> removeDates = kp.filterRemove(timestamps);
		System.out.println("removing: "+removeDates);
		System.out.println("[remote files]");
		for (String filename:filenames) {
			String timestamp = Utils.extractTimestamp(filename);
			if (timestamp == null) {
				continue;
			}
			if (removeDates.contains(timestamp)) {
				System.out.println("  - "+filename);
				deleter.deleteFile(Paths.get(filename));
				
			}
			else {
				System.out.println("  o "+filename);
			}
		}
	}


}
