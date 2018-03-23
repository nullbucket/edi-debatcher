package org.null0.edi.debatcher.test;

import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;
import org.null0.edi.debatcher.ConfigDefault;
import org.null0.edi.debatcher.interfaces.Config;

public class ConfigTest {


	@Test
	public void testLocalPropertiesFile() throws Exception {
		Config config = testOutputDirectory();		
		assertEquals(8096, config.getBufferSize());
		assertEquals(".", config.getOutputDirectory().toString());
	}	

	private Config testOutputDirectory() throws Exception {
		Config config = new ConfigDefault();
		Path path = config.getOutputDirectory();	
		assertTrue(Files.exists(path));
		return config;
	}
}
