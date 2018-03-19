package org.null0.edi.debatcher;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jdk.internal.jline.internal.Log;

/***
 * 
 * 
 * 
 */
// TODO: define an interface for Config so that for unit testing we can use it instead without any file I/O dependencies
public class Config {
	private static final String ENVAR_NAME = "edi_debatch_config_file"; // If this environment variable exists it must contain full valid path to properties file	
	private static final String FILE_NAME = "debatcher.properties"; // Default expected local properties file name if config_env_name environment variable does not exist 
	private static final Logger LOG = LoggerFactory.getLogger(Config.class); // Logger
	
	private Properties properties;
	private ConfigurationSource mode;
	private Path outDir; // configuration
	private int bufferSize; // configuration

	public Config() {
		if (!initFromEnvVar()) {
			if (!initFromLocal()) {
				initFromFailover();
			}
		}
	
		try {
			setProperties();
		} catch (Exception e) {
			Log.error("Exception in CTOR", e);
		}
	}
	
	public Config (String file) throws Exception {
		this.mode = ConfigurationSource.EXTERNAL_PROPERTIES;
		initFromFile(file);		
		setProperties();
	}	
	
	public enum ConfigurationSource { ENVIRONMENT_VARIABLE, LOCAL_PROPERTIES, EXTERNAL_PROPERTIES, OVERRIDE, FAILOVER } 	
	public ConfigurationSource getConfigurationSource() {
		return this.mode;		
	}
	
	/** @param the output directory */
	public Path getOutputDirectory()  {
		return this.outDir;
	}

	/** @return data chunk buffer size */
	public int getBufferSize() {
		return bufferSize;
	}
	
	private void setProperties() throws Exception {
		this.bufferSize = Integer.parseInt(this.properties.getProperty("buffer_size"));
		this.outDir = toPath(this.properties.getProperty("output_directory"));		
	}
	
	private boolean initFromEnvVar() {
		this.mode = ConfigurationSource.ENVIRONMENT_VARIABLE;
		String envVarValue = System.getenv(ENVAR_NAME);
		return envVarValue == null ? false : initFromFile(envVarValue);
	}
	
	private boolean initFromLocal() {
		this.mode = ConfigurationSource.LOCAL_PROPERTIES;
		try {
			this.properties = this.getPropertiesFromResource();
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	
	private boolean initFromFile(String path) {
		this.mode = ConfigurationSource.EXTERNAL_PROPERTIES;
		try {
			Properties p  = getPropertiesFromFile(path);
			if (p == null) {
				return false;
			}
			this.properties = p;
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	
	private void initFromFailover() {
		this.mode = ConfigurationSource.FAILOVER;
		
		final int DEFAULT_BUFFER_SIZE = 1024;
		this.bufferSize = DEFAULT_BUFFER_SIZE;
		
		try {
			this.outDir = toPath(getLocalDir());
		} catch (NotDirectoryException e) {
			LOG.error("Unable to determine java resource path", e); // This should never happen
		} 
		LOG.warn("Failover (see previous errors). Config.outDir set to dir relative to .jar: {}", this.outDir);
	}
	
	private String getLocalDir() {
		String path = "";
		try {
			path = Paths.get(Thread.currentThread().getContextClassLoader().getResource("").toURI()).toString();
		} catch (URISyntaxException e) {
			LOG.error("Unable to determine java resource path", e); // This should never happen
		}
		return path;
	}
	
	private Properties getPropertiesFromFile(String path) throws IOException {
		Properties p = new Properties();
		p.load(new FileInputStream(path));
		return p;
	}

	private Properties getPropertiesFromResource() throws IOException {
		Properties p = null;
		try(InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(FILE_NAME)) {				
			p = new Properties();
			p.load(stream);
		} catch (IOException e) {
			throw e;
		}
		return p;		
	}
	
	private Path toPath (final String path) throws NotDirectoryException {
		if (path == null || path.isEmpty()) {
			throw new NotDirectoryException("Path was empty or null"); 
		}
		final Path p = Paths.get(path);
		if (!Files.exists(p) || !Files.isDirectory(p)) {
			throw new NotDirectoryException(path);
		}
		return p;
	}
}
