package org.null0.x12.debatcher.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.null0.x12.debatcher.Config;
import org.null0.x12.debatcher.DefaultConfig;

public class ConfigTest {
	Path configFile;
	
	@Test	
	public void testPropertiesFromEnvironmentVariable() throws Exception {
		setUpExternalProperties("./debatcher-alternative.properties");
		DefaultConfig.Environment mockEnvironment = mock(DefaultConfig.Environment.class);
		when(mockEnvironment.getVariable(DefaultConfig.ENVAR_NAME)).thenReturn(configFile.toString());
		
		testConfigDefault(mockEnvironment);
		
		cleanUpExternalProperties();
	}
	
	@Test
	public void testLocalPropertiesFile() throws Exception {
		testConfigDefault(null);
	}
	
	private void setUpExternalProperties(String configFilename) {
		configFile = Paths.get(configFilename);
		String configContents = "buffer_size=8096\noutput_directory=./\nwill_update_transaction_id=true\nvalid_senders_isa06=\nvalid_receivers_isa08=";
		try {
			Files.write(configFile, configContents.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void cleanUpExternalProperties() {
		for(int i = 1; i <=3; i++) {
			try {
				System.gc(); // hate to do this, but properties file is not always released in time
				Files.deleteIfExists(configFile);
			} catch (Exception e) {
				try {
					Thread.sleep(250);				
				} catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}
	
	private void testConfigDefault(DefaultConfig.Environment environment) throws Exception {
		Config config = testOutputDirectory(environment);
		assertEquals(8096, config.getBufferSize());
		assertEquals(".", config.getOutputDirectory().toString());
	}

	private Config testOutputDirectory(DefaultConfig.Environment environment) throws Exception {
		Config config = environment == null ? new DefaultConfig() : new DefaultConfig(environment);
		Path path = config.getOutputDirectory();
		assertTrue(Files.exists(path));
		return config;
	}
}
