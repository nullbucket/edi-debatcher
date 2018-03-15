package org.null0.edi.debatcher;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * 
 * 
 *
 */
public class Config {
	/** If this environment variable exists it must contain full valid path to properties file */
	public static final String ENVAR_NAME = "edi_debatch_config_file";
	
	/** Default expected local properties file name if config_env_name environment variable does not exist */
	public static final String FILE_NAME = "debatcher.properties";
	
	/** Expected name for output directory variable in java properties file */
	public static final String OUTDIR_PROP_NAME = "outputDirectory";
	
	private static final Logger LOG = LoggerFactory.getLogger(Config.class); // Logger
	private Properties properties;
	private Path outDir;
	private ConfigurationMode mode;

	public Config() {
		
		// Determine path to configuration file.
		// First try to use environment variable override
		this.mode = ConfigurationMode.ENVIRONMENT_VARIABLE;
		String path = System.getenv(ENVAR_NAME);
		
		boolean ok = true;
		try {
			// ...if environment variable not defined then default to local
			if (path == null || path.isEmpty()) {
				path = getLocalDir();
				if (!path.endsWith("/")) {
					path += "//";
				}
				path += FILE_NAME;
				this.mode = ConfigurationMode.PROPERTIES;
			}
			
			// Initialize properties from path
			this.properties = new Properties();
			this.properties.load(new FileInputStream(path)); // Load it
		} catch (FileNotFoundException e) {
			LOG.warn("Bad or missing java properties file '{}'", path);
			ok = false;		
		} catch (IOException e) {
			LOG.warn("I/O error reading java properties file '{}'", path);
			ok = false;
		}
		
		// If for any reason we couldn't fetch a configuration file, use defaults
		if(!ok) {
			setToFailover();
		}
	}
	
	public void setOutputDir(String path) throws NotDirectoryException {
		this.outDir = toPath(path);
		this.mode = ConfigurationMode.OVERRIDE;
	}

	public Path getOutputDir()  {
		if (this.outDir == null ) {
			String dirName = null;
			try {
				dirName = this.properties.getProperty(Config.OUTDIR_PROP_NAME);
				this.outDir = toPath(dirName);
			} catch (NotDirectoryException e) {
				LOG.warn("Bad output directory name '{}'", dirName);
				setToFailover();
			}
		}
		return this.outDir;
	}

	public enum ConfigurationMode { ENVIRONMENT_VARIABLE, PROPERTIES, OVERRIDE, FAILOVER } 
	public ConfigurationMode getMode() {
		return this.mode;		
	}
	
	private void setToFailover() {
		try {
			this.outDir = toPath(getLocalDir());
			this.mode = ConfigurationMode.FAILOVER;
		} catch (NotDirectoryException e) {
			LOG.error("Unable to determine java resource path", e); // This should never happen
		} 
		LOG.warn("Failover (see previous errors). Config.outDir set to dir relative to .jar: {}", this.outDir);
	}
	
	private String getLocalDir() {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		String path = "";
		try {
			path = Paths.get(classloader.getResource("").toURI()).toString();
		} catch (URISyntaxException e) {
			LOG.error("Unable to determine java resource path", e); // This should never happen
		}
		return path;
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
