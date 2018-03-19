package org.null0.edi.debatcher.test;

import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.null0.edi.debatcher.ConfigDefault;
import org.null0.edi.debatcher.interfaces.Config;
import org.null0.edi.debatcher.interfaces.Config.ConfigurationSource;

public class ConfigTest {


	@Test
	public void testLocalPropertiesFile() throws Exception {
		Config config = testOutputDirectory();		
		assertEquals(ConfigurationSource.LOCAL_PROPERTIES, config.getConfigurationSource());
		assertEquals(8096, config.getBufferSize());
		assertEquals(".", config.getOutputDirectory().toString());
	}	

	@Test
	public void testExternalPropertiesFile() throws Exception {
		// TODO: we need to set up the local file "debatcher.properties" for this to pass
		Config config = testOutputDirectory();		
		assertEquals(ConfigurationSource.EXTERNAL_PROPERTIES, config.getConfigurationSource());
	}	

	private Config testOutputDirectory() throws Exception {
		Config config = new ConfigDefault();
		Path path = config.getOutputDirectory();	
		assertTrue(Files.exists(path));
		return config;
	}
}
