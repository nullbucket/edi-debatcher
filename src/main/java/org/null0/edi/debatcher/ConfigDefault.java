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

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.null0.edi.debatcher.interfaces.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * 
 * 
 * 
 */
// TODO: define an interface for Config so that for unit testing we can use it instead without any file I/O dependencies
public class ConfigDefault implements Config {
	private static final String ENVAR_NAME = "edi_debatch_config_file"; // If this environment variable exists it must contain full valid path to properties file	
	private static final String FILE_NAME = "debatcher.properties"; // Default expected local properties file name if config_env_name environment variable does not exist 
	private static final Logger LOG = LoggerFactory.getLogger(Config.class); // Logger
	
	private Properties properties;
	
	// Configuration Values from debatcher.properties
	private Path outDir;
	private int bufferSize;
	private boolean willUpdateTransactionId;
	private boolean willRejectOnValidationError;
	private String[] validSendersISA06;
	private String[] validReceiversISA08;

	public ConfigDefault() {
		if (!initFromEnvVar()) {
			if (!initFromLocal()) {
				initFromFailover();
			}
		}
	
		try {
			setProperties();
		} catch (Exception e) {
			LOG.error("Exception in CTOR", e);
		}
	}
	
	public ConfigDefault (String file) throws Exception {
		initFromFile(file);		
		setProperties();
	}	
	
	@Override
	public Path getOutputDirectory()  {
		return this.outDir;
	}

	@Override
	public int getBufferSize() {
		return bufferSize;
	}
	
	@Override
	public boolean willUpdateTransactionId() {
		return willUpdateTransactionId;
	}
	
	@Override
	public boolean willRejectOnValidationError() {
		return willRejectOnValidationError;
	}
	
	@Override
	public String[] getValidSenders() {
		return validSendersISA06;
	}

	@Override
	public String[] getValidReceivers() {
		return validReceiversISA08;
	}

	private void setProperties() throws Exception {
		this.bufferSize = Integer.parseInt(this.properties.getProperty("buffer_size"));
		this.outDir = toPath(this.properties.getProperty("output_directory"));		
		this.willUpdateTransactionId = BooleanUtils.toBoolean(this.properties.getProperty("will_update_transaction_id"));
		this.validSendersISA06 = setList("valid_senders_isa06");
		this.validReceiversISA08 = setList("valid_receivers_isa08");
	}
	
	private String[] setList(String propertyName) {
		String s = this.properties.getProperty(propertyName);
		return StringUtils.isAllBlank(s) ? new String[0] : s.split(",");
	}
	
	private boolean initFromEnvVar() {
		String envVarValue = System.getenv(ENVAR_NAME);
		if (!StringUtils.isAllBlank(envVarValue)) {
			if (initFromFile(envVarValue)) {
				LOG.info("Loaded properties from file '{}' specified by environment variable {}.", envVarValue, ENVAR_NAME);
				return true;
			}
		}
		return false;
	}
	
	private boolean initFromLocal() {
		try {
			this.properties = this.getPropertiesFromResource();
		} catch (IOException e) {
			LOG.info("Loaded properties from local (embedded) resource '{}'.", FILE_NAME);
			return false;
		}
		return true;
	}
	
	private boolean initFromFile(String path) {
		try {
			Properties p  = getPropertiesFromFile(path);
			if (p == null) {
				return false;
			}
			this.properties = p;
		} catch (IOException e) {
			return false;
		}
		LOG.info("Loaded properties from file '{}'.", path);
		return true;
	}
	
	private void initFromFailover() {
		final int DEFAULT_BUFFER_SIZE = 1024;
		this.bufferSize = DEFAULT_BUFFER_SIZE;
		
		try {
			this.outDir = toPath(getLocalDir());
			LOG.warn("Failover (see previous errors). Config.outDir set to dir relative to .jar: {}", this.outDir);
		} catch (NotDirectoryException e) {
			LOG.error("Unable to recover using failover: could not determine java resource path", e); // This should never happen
		} 
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
		try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(FILE_NAME)) {				
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
