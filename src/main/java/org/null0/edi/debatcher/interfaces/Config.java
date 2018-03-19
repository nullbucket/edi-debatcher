package org.null0.edi.debatcher.interfaces;

import java.nio.file.Path;

public interface Config {
	public enum ConfigurationSource { ENVIRONMENT_VARIABLE, LOCAL_PROPERTIES, EXTERNAL_PROPERTIES, OVERRIDE, FAILOVER } 	

	public ConfigurationSource getConfigurationSource();

	Path getOutputDirectory();

	/** @return data chunk buffer size */
	public int getBufferSize();

}