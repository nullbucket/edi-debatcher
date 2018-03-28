package org.null0.x12.debatcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: define an interface for Config so that for unit testing we can use it instead without any file I/O dependencies
public class DefaultConfig implements Config {
	
	// To make test mocking easier
	public class Environment {
		public String getVariable(String name) {
			return System.getenv(name);
		}
	}

	public static final String ENVAR_NAME = "edi_debatch_config_file"; // If this environment variable exists it must contain full valid path to properties file	
	private static final String FILE_NAME = "debatcher.properties"; // Default expected local properties file name if config_env_name environment variable does not exist 
	private static final Logger logger = LoggerFactory.getLogger(Config.class); // Logger
	private int bufferSize;
	private Environment environment;
	private Path outDir; // from debatcher.properties
	private Properties properties;
	private String[] validReceiversISA08; // from debatcher.properties
	private String[] validSendersISA06; // from debatcher.properties
	private boolean willRejectOnValidationError; // from debatcher.properties

	private boolean willUpdateTransactionId; // from debatcher.properties

	public DefaultConfig() {
		this(null);
	}
	
	public DefaultConfig(Environment envVar) {
		environment = envVar == null ? new Environment() : envVar;	
		if (!initFromEnvVar()) {
			initFromLocal();
		}

		try {
			setProperties();
		} catch (Exception e) {
			logger.error("Exception in CTOR", e);
		}
	}
	

	@Override
	public int getBufferSize() {
		return bufferSize;
	}

	@Override
	public Path getOutputDirectory() {
		return this.outDir;
	}

	@Override
	public String[] getValidReceivers() {
		return validReceiversISA08;
	}

	@Override
	public String[] getValidSenders() {
		return validSendersISA06;
	}

	@Override
	public boolean willRejectOnValidationError() {
		return willRejectOnValidationError;
	}

	@Override
	public boolean willUpdateTransactionId() {
		return willUpdateTransactionId;
	}

	private Properties getPropertiesFromFile(String path) throws IOException {
		Properties p = new Properties();
		p.load(new FileInputStream(path));
		return p;
	}

	private Properties getPropertiesFromResource() throws IOException {
		Properties p = null;
		try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(FILE_NAME)) {
			p = new Properties();
			p.load(stream);
		}
		return p;
	}

	private boolean initFromEnvVar() {
		String envVarValue = environment.getVariable(ENVAR_NAME);
		if (!StringUtils.isAllBlank(envVarValue)) {
			if (initFromFile(envVarValue)) {
				logger.info("Loaded properties from file '{}' specified by environment variable {}.", toAbsolute(envVarValue), ENVAR_NAME);
				return true;
			}
		}
		return false;
	}

	private boolean initFromFile(String path) {
		try {
			Properties p = getPropertiesFromFile(path);
			this.properties = p;
		} catch (IOException e) {
			logger.warn("Unable to locate properties file '{}'.", toAbsolute(path));							
			return false;
		}
		if (logger.isInfoEnabled()) {
			logger.info("Loaded properties from file '{}'.", toAbsolute(path));			
		}
		return true;
	}

	private boolean initFromLocal() {
		try {
			this.properties = this.getPropertiesFromResource();
		} catch (IOException e) {
			logger.info("Loaded properties from local (embedded) resource '{}'.", FILE_NAME);
			return false;
		}
		return true;
	}

	private String[] setList(String propertyName) {
		String s = this.properties.getProperty(propertyName);
		return StringUtils.isAllBlank(s) ? new String[0] : s.split(",");
	}

	private void setProperties() throws NotDirectoryException {
		this.bufferSize = Integer.parseInt(this.properties.getProperty("buffer_size"));
		this.outDir = toPath(this.properties.getProperty("output_directory"));
		this.willUpdateTransactionId = BooleanUtils.toBoolean(this.properties.getProperty("will_update_transaction_id"));
		this.validSendersISA06 = setList("valid_senders_isa06");
		this.validReceiversISA08 = setList("valid_receivers_isa08");
	}
	
	private String toAbsolute(final String path) {
		String fullPath = null;
		try {
			final Path p = Paths.get(path);
			final File f = p.toFile();
			fullPath = f.getCanonicalPath();
		} catch(Exception e) {
			logger.warn(path, e);											
			fullPath = path;
		}
		return fullPath;
	}

	private Path toPath(final String path) throws NotDirectoryException {
		if (path == null || path.isEmpty()) {
			throw new NotDirectoryException("Path was empty or null");
		}
		final Path p = Paths.get(path);
		final File f = p.toFile();
		if (!f.exists() || !f.isDirectory()) {
			throw new NotDirectoryException(path);
		}
		return p;
	}
}
