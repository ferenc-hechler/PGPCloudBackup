package de.hechler.encrypt.cloudbackup.pcloud;

import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import de.hechler.encrypt.utils.Utils;

/**
 * 
 * @author feri
 */

public class PGPCloudBackupMain {

	private static String DEFAULT_CONFIG = "DONOTCHECKIN/.pcloud-config";
	private static String DEFAULT_KEY = "encrypt-key.pub";
	private static String DEFAULT_FOLDER = "./testdata";
	private static String DEFAULT_TEMP_FOLDER = System.getProperty("java.io.tmpdir");
	private static String DEFAULT_REMOTE_FOLDER = "/VSERVERBACKUP";
	
	public static void main(String[] args) {
		try {
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
		    	
		        CommandLineParser parser = new DefaultParser();
		        cmd = parser.parse(options, args);
	        } catch (ParseException e) {
		        HelpFormatter formatter = new HelpFormatter();
	            System.out.println(e.getMessage());
	            formatter.printHelp("PGPCloudBackup", options);
	            System.exit(1);
	        }

	        String config = cmd.getOptionValue("config", DEFAULT_CONFIG);
	        String key = cmd.getOptionValue("key", DEFAULT_KEY);
	        String folder = cmd.getOptionValue("folder", DEFAULT_FOLDER);
	        String tempFolder = cmd.getOptionValue("temp-folder", DEFAULT_TEMP_FOLDER);
	        String remoteFolder = cmd.getOptionValue("remote-folder", DEFAULT_REMOTE_FOLDER);

	        System.out.println("config: "+config);
	        System.out.println("key: "+key);
	        System.out.println("folder: "+folder);
	        System.out.println("tempFolder: "+tempFolder);
	        System.out.println("remoteFolder: "+remoteFolder);

			PCloudConfig.setConfigFilename(config);
			Utils.setTempFolder(tempFolder);

			PGPCloudBackup app = new PGPCloudBackup(Paths.get(key), Paths.get(folder), Paths.get(remoteFolder));
			app.startBackup();
		}
		catch (Exception e) {
			e.printStackTrace();
			PCloudApiClient.shutdownApiClient();
			System.exit(1);
		}
		PCloudApiClient.shutdownApiClient();
	}	
}
