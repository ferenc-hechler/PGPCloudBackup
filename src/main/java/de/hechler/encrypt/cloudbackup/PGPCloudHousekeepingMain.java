package de.hechler.encrypt.cloudbackup;

import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import de.hechler.encrypt.cloudbackup.pcloud.PCloudApiClient;
import de.hechler.encrypt.cloudbackup.pcloud.PCloudConfig;
import de.hechler.encrypt.utils.SimpleCrypto;
import de.hechler.encrypt.utils.Utils;

/**
 * 
 * @author feri
 */

public class PGPCloudHousekeepingMain {

	static String DEFAULT_CONFIG = "conf/.pcloud-config";
	static String DEFAULT_REMOTE_FOLDER = "/VSERVERBACKUP/TEST";
	static String DEFAULT_KEEP_PERIODS = "7d4w3m*y";
	static boolean DEFAULT_IGNORE_MISSING_TIMESTAMPS = false;
	

	public static void main(String[] args) {
		try {
			System.out.println("PGPCloudHousekeeping started");
	        CommandLine cmd = null;
	        Options options = new Options();
	        try {
		        Option configOpt = new Option("c", "config", true, "path to pcloud config file, default is \""+DEFAULT_CONFIG+"\"");
		        configOpt.setRequired(false);
		        options.addOption(configOpt);
	
		        Option remoteFolderOpt = new Option("r", "remote-folder", true, "backup pCloud folder which will be used as source for restoring, default is \""+DEFAULT_REMOTE_FOLDER+"\"");
		        remoteFolderOpt.setRequired(false);
		        options.addOption(remoteFolderOpt);

		        Option keepPeriodsOpt = new Option("k", "keep-periods", true, "keep-period pattern in format '#d#w#m#y', default is 7 days, 4 weeks, 3 months infinites years: \""+DEFAULT_KEEP_PERIODS+"\"");
		        keepPeriodsOpt.setRequired(false);
		        options.addOption(keepPeriodsOpt);

		        Option ignoreMissingTimestampOpt = new Option("i", "ignore-missing-timestamps", false, "ignore files, that have no timestamp. If not set, housekeeping will fail with an error if files without timestamps ('*-YYYY-MM-DD.*') exist.");
		        ignoreMissingTimestampOpt.setRequired(false);
		        options.addOption(ignoreMissingTimestampOpt);

		        CommandLineParser parser = new DefaultParser();
		        cmd = parser.parse(options, args);
	        } catch (ParseException e) {
		        HelpFormatter formatter = new HelpFormatter();
	            System.out.println(e.getMessage());
	            formatter.printHelp("PGPCloudHousekeeping", options);
	            System.exit(1);
	        }
	        
	        String config       = getValue(cmd, "config",        "PGPCB_CONFIG_FILE",   DEFAULT_CONFIG);
	        String remoteFolder = getValue(cmd, "remote-folder", "PGPCB_REMOTE_FOLDER", DEFAULT_REMOTE_FOLDER);
	        String keepPeriods = getValue(cmd, "keep-periods",   "PGPCB_KEEP_PERIODS",  DEFAULT_KEEP_PERIODS);
	        boolean ignoreMissingTimestamps = getBooleanValue(cmd, "ignore-missing-timestamps", "PGPCB_IGNORE_MISSING_TIMESTAMPS",  DEFAULT_IGNORE_MISSING_TIMESTAMPS);

	        System.out.println("config: "+config);
	        System.out.println("remoteFolder: "+remoteFolder);
	        System.out.println("keepPeriods: "+keepPeriods);
	        System.out.println("ignoreMissingTimestamps: "+ignoreMissingTimestamps);

	        if (keepPeriods.isEmpty()) {
	        	throw new RuntimeException("keepPeriods has to contain at least one period.");
	        }
	        
			PCloudConfig.setConfigFilename(config);

			PGPCloudHousekeeping app = new PGPCloudHousekeeping(Paths.get(remoteFolder), keepPeriods, ignoreMissingTimestamps);
			app.startHousekeeping();
			System.out.println("PGPCloudHousekeeping finished");
		}
		catch (Exception e) {
			e.printStackTrace();
			PCloudApiClient.shutdownApiClient();
			System.exit(1);
		}
		PCloudApiClient.shutdownApiClient();
	}

	private static String getValue(CommandLine cmd, String cliName, String envName, String defaultValue) {
		String result = System.getenv(envName);
		if (result != null) {
			return result;
		}
		return cmd.getOptionValue(cliName, defaultValue);
	}	

	private static boolean getBooleanValue(CommandLine cmd, String cliName, String envName, boolean defaultValue) {
		String result = System.getenv(envName);
		if (result != null) {
			return Boolean.parseBoolean(result);
		}			
		return cmd.hasOption(cliName) || defaultValue;
	}

}
