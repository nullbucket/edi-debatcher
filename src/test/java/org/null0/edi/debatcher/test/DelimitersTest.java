package org.null0.edi.debatcher.test;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.null0.edi.debatcher.FileProperties;

public class DelimitersTest {
	private FileProperties delimiters = null;

	@BeforeClass
	public static void testFixureSetup() {
	}

	@AfterClass
	public static void testFixureTeardown() {
	}

	@Before
	public void testSetup() {
		delimiters = new FileProperties();

	}

	@Test
	public void testDelimiters() {
		assertEquals(delimiters.getComponentElementSeparator(), ':');
		assertEquals(delimiters.getDataElementSeparator(), '*');
		assertEquals(delimiters.getDataRepetitionSeparator(), '^');
		assertEquals(delimiters.getSegmentTerminator(), '~');
	}

}
