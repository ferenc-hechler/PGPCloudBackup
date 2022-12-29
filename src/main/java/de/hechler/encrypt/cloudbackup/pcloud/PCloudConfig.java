package de.hechler.encrypt.cloudbackup.pcloud;

import java.io.FileInputStream;
import java.util.Properties;

import de.hechler.encrypt.utils.SimpleCrypto;

/**
 * using pCloud API - Java SDK https://github.com/pCloud/pcloud-sdk-java
 * 
 * SDK docu: https://pcloud.github.io/pcloud-sdk-java/
 * 
 * API docu: https://docs.pcloud.com/
 * 
 * @author feri
 */

public class PCloudConfig {

	private final static String APP_NAME = "PGPCloudBackup";

	private static String DEFAULT_PCLOUD_CONFIG_FILENAME = ".env";

	public static void setConfigFilename(String configFilename) {
		DEFAULT_PCLOUD_CONFIG_FILENAME = configFilename;
	}

	private String clientId;
	private String encClientSecret;
	private String encAccessToken;
	private String apiHost;


	public PCloudConfig() {
		this(System.getenv("PCLOUD_CONFIG_FILENAME"));
		
	}

	public PCloudConfig(String propertiesFile) {
		try {
			if (propertiesFile == null) {
				propertiesFile = DEFAULT_PCLOUD_CONFIG_FILENAME;
			}
			Properties envProps = new Properties();
			envProps.load(new FileInputStream(propertiesFile));
			clientId = envProps.getProperty("CLIENT_ID").trim();
			encClientSecret = envProps.getProperty("ENC_CLIENT_SECRET");
			if (encClientSecret == null) {
				encClientSecret = SimpleCrypto.encrypt(APP_NAME + 13, envProps.getProperty("CLIENT_SECRET").trim());
				System.out.println("ENC_CLIENT_SECRET=" + encClientSecret);
			}
			encAccessToken = envProps.getProperty("ENC_ACCESS_TOKEN");
			if (encAccessToken == null) {
				encAccessToken = SimpleCrypto.encrypt(APP_NAME + 13, envProps.getProperty("ACCESS_TOKEN").trim());
				System.out.println("ENC_ACCESS_TOKEN=" + encAccessToken);
			}
			apiHost = envProps.getProperty("API_HOST").trim();
		} catch (Exception e) {
			throw new RuntimeException("error reding config file '" + propertiesFile + "'", e);
		}
	}

	public String getClientId() {
		return clientId;
	}

	public String getClientSecret() {
		return SimpleCrypto.decrypt(APP_NAME + 13, encClientSecret);
	}

	public String getAccessToken() {
		return SimpleCrypto.decrypt(APP_NAME + 13, encAccessToken);
	}

	public String getApiHost() {
		return apiHost;
	}

}
