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

	@Ignore
	@Test
	public void testDelimiters() {
		delimiters.getComponentElementSeparator();
		delimiters.getDataElementSeparator();
		delimiters.getDataRepetitionSeparator();
		delimiters.getSegmentTerminator();
		assertEquals(delimiters.getComponentElementSeparator(), ':');
		assertEquals(delimiters.getDataElementSeparator(), '*');
		assertEquals(delimiters.getDataRepetitionSeparator(), '^');
		assertEquals(delimiters.getSegmentTerminator(), '~');
	}

}
