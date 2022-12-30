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
import de.hechler.encrypt.utils.Utils;

/**
 * 
 * @author feri
 */

public class PGPCloudBackupMain {

	static String DEFAULT_CONFIG = "conf/.pcloud-config";
	static String DEFAULT_KEY = "conf/encrypt-key.pub";
	static String DEFAULT_FOLDER = "/input";
	static String DEFAULT_TEMP_FOLDER = System.getProperty("java.io.tmpdir");
	static String DEFAULT_REMOTE_FOLDER = "/VSERVERBACKUP/TEST";
	static boolean DEFAULT_DELETE_REMOTE = false;

	public static void main(String[] args) {
		try {
			System.out.println("PGPCloudBackup started");
	        CommandLine cmd = null;
	        Options options = new Options();
	        try {
		        Option configOpt = new Option("c", "config", true, "path to pcloud config file, default is \""+DEFAULT_CONFIG+"\"");
		        configOpt.setRequired(false);
		        options.addOption(configOpt);
	
		        Option keyOpt = new Option("k", "key", true, "public PGP key (in PEM format) used for encryption, default is \""+DEFAULT_KEY+"\"");
		        keyOpt.setRequired(false);
		        options.addOption(keyOpt);
	
		        Option folderOpt = new Option("f", "folder", true, "local filesystem folder to be backed up, default is \""+DEFAULT_FOLDER+"\"");
		        folderOpt.setRequired(false);
		        options.addOption(folderOpt);
	
		        Option tempFolderOpt = new Option("t", "temp-folder", true, "folder for creating temporary files, default is \""+DEFAULT_TEMP_FOLDER+"\"");
		        tempFolderOpt.setRequired(false);
		        options.addOption(tempFolderOpt);
	
		        Option remoteFolderOpt = new Option("r", "remote-folder", true, "target pCloud folder for backup, default is \""+DEFAULT_REMOTE_FOLDER+"\"");
		        remoteFolderOpt.setRequired(false);
		        options.addOption(remoteFolderOpt);

		        Option deleteRemoteOpt = new Option("d", "delete-remote", false, "delet remote files, which were deleted locally");
		        deleteRemoteOpt.setRequired(false);
		        options.addOption(deleteRemoteOpt);

		        CommandLineParser parser = new DefaultParser();
		        cmd = parser.parse(options, args);
	        } catch (ParseException e) {
		        HelpFormatter formatter = new HelpFormatter();
	            System.out.println(e.getMessage());
	            formatter.printHelp("PGPCloudBackup", options);
	            System.exit(1);
	        }

	        String config       = getValue(cmd, "config",        "PGPCB_CONFIG_FILE",         DEFAULT_CONFIG);
	        String key          = getValue(cmd, "key",           "PGPCB_PUBLIC_PGP_KEY_FILE", DEFAULT_KEY);
	        String folder       = getValue(cmd, "folder",        "PGPCB_LOCAL_FOLDER",        DEFAULT_FOLDER);
	        String tempFolder   = getValue(cmd, "temp-folder",   "PGPCB_TEMP_FOLDER",         DEFAULT_TEMP_FOLDER);
	        String remoteFolder = getValue(cmd, "remote-folder", "PGPCB_REMOTE_FOLDER",       DEFAULT_REMOTE_FOLDER);
	        boolean deleteRemote = getBooleanValue(cmd, "delete-remote", "PGPCB_DELETE_REMOTE", DEFAULT_DELETE_REMOTE);
	        
	        System.out.println("config: "+config);
	        System.out.println("key: "+key);
	        System.out.println("folder: "+folder);
	        System.out.println("tempFolder: "+tempFolder);
	        System.out.println("remoteFolder: "+remoteFolder);
	        System.out.println("deleteRemote: "+deleteRemote);

			PCloudConfig.setConfigFilename(config);
			Utils.setTempFolder(tempFolder);

			PGPCloudBackup app = new PGPCloudBackup(Paths.get(key), Paths.get(folder), Paths.get(remoteFolder), deleteRemote);
			app.startBackup();
			System.out.println("PGPCloudBackup finished");
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
