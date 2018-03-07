package org.null0.edi.debatcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Config {
	private static final Logger LOG = LoggerFactory.getLogger(Config.class);

	private static final String config_dir = "config/";
	private static final String data_dir = "data";
	private static Properties staticAndDynamicProperties = null;

	public static String getSharedBaseDirPath() {
		Properties fileProperties;
		String baseDir = null;
		try {
			fileProperties = loadConfiguration();
			baseDir = fileProperties.getProperty("sharedBaseDir");
		} catch (IOException e) {
			e.printStackTrace();
			LOG.error("Error loading configuration in Config, returning null for shared base directory", e);
		}
		if (baseDir != null) {
			if (!baseDir.endsWith("/")) {
				baseDir = baseDir + "/";
			}
		}
		return baseDir;
	}

	private static Properties loadConfiguration() throws IOException {
		if (staticAndDynamicProperties != null) {
			return staticAndDynamicProperties;
		}

		staticAndDynamicProperties = new Properties();
		Properties pStatic = new Properties();
		Properties pDynamic = new Properties();
		String configFilePath = getConfigPath() + "debatcher.static.properties";
		File f = new File(configFilePath);
		if (f.exists()) {
			pStatic.load(f.toURI().toURL().openStream());
			staticAndDynamicProperties.putAll(pStatic);
		}

		configFilePath = getConfigPath() + "debatcher.dynamic.properties";
		f = new File(configFilePath);
		if (f.exists()) {
			pDynamic.load(f.toURI().toURL().openStream());
			staticAndDynamicProperties.putAll(pDynamic);
		}

		return staticAndDynamicProperties;
	}

	private static String getConfigPath() throws InvalidPathException {
		String data_env = System.getenv(data_dir);
		if (data_env == null || data_env.isEmpty()) {
			data_env = System.getProperty(data_dir);
			if (data_env == null || data_env.isEmpty()) {
				throw new InvalidPathException(data_dir, "System Environment Variable is not found.");
			}
		}
		if (data_env.endsWith(";")) {
			data_env = data_env.replaceAll(";", "");
		}
		if (!data_env.endsWith("/")) {
			data_env += "//";
		}
		return data_env + config_dir;
	}
}
