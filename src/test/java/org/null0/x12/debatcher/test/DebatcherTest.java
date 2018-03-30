package org.null0.x12.debatcher.test;

import java.io.InputStream;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.null0.x12.debatcher.Config;
import org.null0.x12.debatcher.Debatcher;
import org.null0.x12.debatcher.DefaultConfig;
import org.null0.x12.debatcher.DefaultMetadata;
import org.null0.x12.debatcher.DefaultValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DebatcherTest {
	private static final boolean doClean = true;
	private static final Logger logger = LoggerFactory.getLogger(DebatcherTest.class);
	private Debatcher debatcher;

	@Before
	public void setUp() throws Exception {
		Config config = new DefaultConfig();
		debatcher = new Debatcher(config, new DefaultValidator(config),	new DefaultMetadata());
	}

	@AfterClass
	public static void tearDown() {
		if (doClean) {
			TestFileCleaner.clean(".edi.txt");			
		}
	}

	@Test(expected = NullPointerException.class)
	public void test00() throws Exception {
		test("BAD_FILE_DOES_NOT_EXIST");
	}

	@Test
	public void test01() throws Exception {
		test("1 ISA - 1 GS - 1 ST - 1 HL 20 - 1 HL 22 - 1 CLM");
	}

	@Test
	public void test02() throws Exception {
		test("1 ISA - 1 GS - 1 ST - 1 HL 20 - 1 HL 22 - 2 CLMs");
	}

	@Test
	public void test03() throws Exception {
		test("1 ISA - 1 GS - 1 ST - 1 HL 20 - 1 HL 22 - 3 CLMs");
	}

	@Test
	public void test04() throws Exception {
		test("1 ISA - 1 GS - 1 ST - 1 HL 20 - 2 HL 22 - 1 CLM FOR EACH SUBSCRIBER");
	}

	@Test
	public void test05() throws Exception {
		test("1 ISA - 1 GS - 1 ST - 1 HL 20 - 2 HL 22 - 2 CLMs FOR EACH SUBSCRIBER");
	}

	@Test
	public void test06() throws Exception {
		test("1 ISA - 1 GS - 1 ST - 1 HL 20 - 1 HL 22 - 1 CLM - 1 HL 20 - 1 HL 22 - 1 CLM");
	}

	@Test
	public void test07() throws Exception {
		test("1 ISA - 1 GS - 1 ST - 1 HL 20 - 1 HL 22 - 2 CLMs - 1 HL 20 - 1 HL 22 - 2 CLMs");
	}

	@Test
	public void test08() throws Exception {
		test("1 ISA - 1 GS - 1 ST - 1 HL 20 - 1 HL 22 - 1 CLM - 1 ST - 1 HL 20 - 1 HL 22 - 1 CLM");
	}

	@Test
	public void test09() throws Exception {
		test("1 ISA - 1 GS - 3 STs");
	}

	@Test
	public void test10() throws Exception {
		test("1 ISA - 2 GSs");
	}

	@Test
	public void test11() throws Exception {
		test("2 ISAs");
	}

	@Test
	public void test12() throws Exception {
		test("837PFile_for de-batcher_multiple_fil_configs_inonefile");
	}

	@Test
	public void test13() throws Exception {
		test("Patient");
	}

	@Test
	public void testLargeP() throws Exception {
		long startTime = System.currentTimeMillis();
		test("837PFile_Large");
		long endTime = System.currentTimeMillis();
		logger.info("That took " + (endTime - startTime) + " milliseconds");
	}

	@Test
	public void testNonClaims() throws Exception {
		test("810 - 2 ISAs - 1 STs - 2 STs");
	}
	
	@Test
	public void testNoRefD9i() throws Exception {
		test("NoRefD9_I");
	}

	@Test
	public void testNoRefD9p() throws Exception {
		test("NoRefD9_P");
	}

	@Test
	public void testRefD9i() throws Exception {
		test("RefD9_I");
	}

	@Test
	public void testRefD9p() throws Exception {
		test("RefD9_P");
	}

	private InputStream getInputStream(String name) throws Exception {
		return Thread.currentThread().getContextClassLoader().getResourceAsStream("files/" + name + ".txt");
	}

	private void test(String name) throws Exception {
		debatcher.debatch(name, getInputStream(name));
	}
}
