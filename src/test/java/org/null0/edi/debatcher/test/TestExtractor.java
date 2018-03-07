package org.null0.edi.debatcher.test;

import java.io.IOException;
import javax.naming.NameNotFoundException;

import org.junit.BeforeClass;
import org.junit.Before;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;
import java.net.URL;

import org.null0.edi.debatcher.Extractor;

public class TestExtractor {
	private Extractor extractor = null; // TODO: get rid of warning
	private URL edifilepath = null; // TODO: get rid of warning

	@BeforeClass
	public static void testFixureSetup() {
	}

	@AfterClass
	public static void testFixureTeardown() {
	}

	@Before
	public void testSetup() {
		ClassLoader loader = getClass().getClassLoader();
		loader.getResource("files/Demo_837P_Batch_1_compress2.edi");
		this.edifilepath = loader.getResource("files/Demo_837P_Batch_1_compress2.edi");
		this.extractor = new Extractor(edifilepath);
	}

	@After
	public void testTeardown() {
		this.extractor = null;
	}

	@Test
	public void testExtractor() throws NameNotFoundException, IOException, Exception {
		this.extractor.Debatch(this.edifilepath.openStream());
	}
}
