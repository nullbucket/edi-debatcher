package org.null0.x12.debatcher.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;
import org.null0.x12.debatcher.Config;
import org.null0.x12.debatcher.DefaultConfig;

public class ConfigTest {
	@Test
	public void testLocalPropertiesFile() throws Exception {
		Config config = testOutputDirectory();
		assertEquals(8096, config.getBufferSize());
		assertEquals(".", config.getOutputDirectory().toString());
	}

	private Config testOutputDirectory() throws Exception {
		Config config = new DefaultConfig();
		Path path = config.getOutputDirectory();
		assertTrue(Files.exists(path));
		return config;
	}
}
