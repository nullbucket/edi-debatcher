package org.null0.edi.debatcher.test;

import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.null0.edi.debatcher.Config;

public class ConfigTest {

	@Test
	public void testSetOutputDir() {
		String dir = Paths.get(".").toAbsolutePath().normalize().toString();
		try {
			Config config = new Config();
			config.setOutputDir(dir);
			assertEquals(Config.ConfigurationMode.OVERRIDE, config.getMode());
		} catch (NotDirectoryException e) {
			fail("NotDirectoryException");
		}		
	}

	@Test
	public void testGetOutputDir_Failover() {
		Config config = testOutputDir();		
		assertEquals(Config.ConfigurationMode.FAILOVER, config.getMode());
	}	

	@Test
	public void testGetOutputDir_PropertiesFile() {
		// TODO: we need to set up the local file "debatcher.properties" for this to pass
		Config config = testOutputDir();		
		assertEquals(Config.ConfigurationMode.PROPERTIES, config.getMode());
	}	

	@Test
	public void testGetOutputDir_EnvironmentVariable() {
		// TODO: we need to set up the a "debatcher.properties" files in a subdirectory and point an environment variable to it for this to pass
		Config config =testOutputDir();		
		assertEquals(Config.ConfigurationMode.ENVIRONMENT_VARIABLE, config.getMode());
	}
	
	private Config testOutputDir() {
		Config config = new Config();
		Path path = config.getOutputDir();	
		assertTrue(Files.exists(path));
		return config;
	}

}
