package org.null0.edi.debatcher.interfaces;

import java.nio.file.Path;

public interface Config {
	public enum ConfigurationSource { ENVIRONMENT_VARIABLE, LOCAL_PROPERTIES, EXTERNAL_PROPERTIES, OVERRIDE, FAILOVER } 	
	public ConfigurationSource getConfigurationSource();
	public Path getOutputDirectory();
	public int getBufferSize();
	public boolean willUpdateTransactionId();
	public String[] getValidSenders();
	public String[] getValidReceivers();
}