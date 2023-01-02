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

public class PGPCloudRestoreMain {

	static String DEFAULT_CONFIG = "conf/.pcloud-config";
	static String DEFAULT_KEY = "conf/.private-decrypt.key";
	static String DEFAULT_KEY_PASSPHRASE = "****";
	static String DEFAULT_FOLDER = "/input";
	static String DEFAULT_TEMP_FOLDER = System.getProperty("java.io.tmpdir");
	static String DEFAULT_REMOTE_FOLDER = "/VSERVERBACKUP/TEST";
	static String DEFAULT_FILTER_DATE = "";
	static boolean DEFAULT_DELETE_LOCAL = false;

	public static void main(String[] args) {
		try {
			System.out.println("PGPCloudRestore started");
	        CommandLine cmd = null;
	        Options options = new Options();
	        try {
		        Option configOpt = new Option("c", "config", true, "path to pcloud config file, default is \""+DEFAULT_CONFIG+"\"");
		        configOpt.setRequired(false);
		        options.addOption(configOpt);
	
		        Option keyOpt = new Option("k", "key", true, "private PGP key (in PEM format) used for decryption, default is \""+DEFAULT_KEY+"\"");
		        keyOpt.setRequired(false);
		        options.addOption(keyOpt);
	
		        Option encPassphraseOpt = new Option("p", "passphrase", true, "encrypted passphrase for private PGP key, use -P for encryption");
		        encPassphraseOpt.setRequired(false);
		        options.addOption(encPassphraseOpt);
	
		        Option passphraseOpt = new Option("P", "encrypt-passphrase", true, "plaintext passphrase, the encrypted value for -p <enc-passphrase> will be output");
		        passphraseOpt.setRequired(false);
		        options.addOption(passphraseOpt);
	
		        Option folderOpt = new Option("f", "folder", true, "local filesystem folder to be restored, default is \""+DEFAULT_FOLDER+"\"");
		        folderOpt.setRequired(false);
		        options.addOption(folderOpt);
	
		        Option tempFolderOpt = new Option("t", "temp-folder", true, "folder for creating temporary files, default is \""+DEFAULT_TEMP_FOLDER+"\"");
		        tempFolderOpt.setRequired(false);
		        options.addOption(tempFolderOpt);
	
		        Option filterDateOpt = new Option("F", "filter-date", true, "only restore files with the given timestamp in YYYY-MM-DD format, default is \""+DEFAULT_TEMP_FOLDER+"\"");
		        filterDateOpt.setRequired(false);
		        options.addOption(filterDateOpt);
	
		        Option remoteFolderOpt = new Option("r", "remote-folder", true, "backup pCloud folder which will be used as source for restoring, default is \""+DEFAULT_REMOTE_FOLDER+"\"");
		        remoteFolderOpt.setRequired(false);
		        options.addOption(remoteFolderOpt);

		        Option deleteRemoteOpt = new Option("d", "delete-local", false, "delet local files, which do not exist in the remote backup");
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
	        
	        if (cmd.hasOption("encrypt-passphrase")) {
	        	String encPW = SimpleCrypto.encrypt("ReStoReStoRe" + 32,cmd.getOptionValue("encrypt-passphrase"));
	        	System.out.println(encPW);
	        	System.exit(0);
	        }

	        String config       = getValue(cmd, "config",        "PGPCB_CONFIG_FILE",          DEFAULT_CONFIG);
	        String key          = getValue(cmd, "key",           "PGPCB_PRIVATE_PGP_KEY_FILE", DEFAULT_KEY);
	        String encPassphrase= getValue(cmd, "passphrase",    "PGPCB_ENC_KEY_PASSPHRASE",   DEFAULT_KEY_PASSPHRASE);
	        String folder       = getValue(cmd, "folder",        "PGPCB_LOCAL_FOLDER",         DEFAULT_FOLDER);
	        String tempFolder   = getValue(cmd, "temp-folder",   "PGPCB_TEMP_FOLDER",          DEFAULT_TEMP_FOLDER);
	        String remoteFolder = getValue(cmd, "remote-folder", "PGPCB_REMOTE_FOLDER",        DEFAULT_REMOTE_FOLDER);
	        String filterDate   = getValue(cmd, "filter-date",   "PGPCB_FILTER:DATE",          DEFAULT_FILTER_DATE);
	        boolean deleteLocal = getBooleanValue(cmd, "delete-remote", "PGPCB_DELETE_LOCAL",  DEFAULT_DELETE_LOCAL);

	        if (!filterDate.isEmpty()) {
	        	if (!filterDate.matches("[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]")) {
		        	throw new RuntimeException("invalid filter-date '"+filterDate+"', format is YYYY-MM-DD");
	        	}
	        	if (deleteLocal) {
	        		throw new RuntimeException("filter-date can not be combined with delete-local");
	        	}
	        }
	        
	        System.out.println("config: "+config);
	        System.out.println("key: "+key);
	        System.out.println("passphrase: ****");
	        System.out.println("folder: "+folder);
	        System.out.println("tempFolder: "+tempFolder);
	        System.out.println("remoteFolder: "+remoteFolder);
	        System.out.println("filterDate: "+filterDate);
	        System.out.println("deleteLocal: "+deleteLocal);

        	String passphrase = SimpleCrypto.decrypt("ReStoReStoRe" + 32, encPassphrase);

	        
			PCloudConfig.setConfigFilename(config);
			Utils.setTempFolder(tempFolder);

			PGPCloudRestore app = new PGPCloudRestore(Paths.get(key), passphrase, Paths.get(folder), Paths.get(remoteFolder), filterDate, deleteLocal);
			app.startRestore();
			System.out.println("PGPCloudRestore finished");
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
