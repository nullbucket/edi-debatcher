package org.null0.edi.debatcher.test;

import java.io.InputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.null0.edi.debatcher.Debatcher;

// TODO: This is scary. All the useful tests have been commented out! Why?
// First we'll get the other unit tests to pass (and verify their accuracy)
// then we'll start enabling and verify these unit tests.
//
public class DebatcherTest {
	private static final Logger logger = LoggerFactory.getLogger(DebatcherTest.class);
	private static Debatcher debatcher;

	@BeforeClass
	public static void setUpClass() {
		// TODO: delete output test directory... or avoid files altogether and stream to
		// memory
	}

	@Before
	public void setUp() throws Exception {
		DebatcherTest.debatcher = new Debatcher();
	}
	// TODO: FIX THIS. Tests were commented out... not good. Get these tests working
	// again.
	// Ignored because of new code implementation where we need to have idBatch
	// value in Submission table. So, test cases will fail when there is no entry in
	// submission table.

	@org.junit.Test
	public void test_00() {
		try {
			test ("BAD_FILE");
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	// TODO: Begin to uncomment these tests and get them to work. This one reads past the last segment (IEA) and throws null pointer exception comparing IEA-02 to ISA-13.  
	@org.junit.Test
	public void test_01() throws Exception {
		test ("1 ISA - 1 GS - 1 ST - 1 HL 20 - 1 HL 22 - 1 CLM");
	}

	@Ignore
	@org.junit.Test
	public void test_02() throws Exception {
		test ("1 ISA - 1 GS - 1 ST - 1 HL 20 - 1 HL 22 - 2 CLMs");
	}

	@Ignore
	@org.junit.Test
	public void test_03() throws Exception {
		test ("1 ISA - 1 GS - 1 ST - 1 HL 20 - 1 HL 22 - 3 CLMs");
	}

	@Ignore
	@org.junit.Test
	public void test_04() throws Exception {
		test ("1 ISA - 1 GS - 1 ST - 1 HL 20 - 2 HL 22 - 1 CLM FOR EACH SUBSCRIBER");
	}

	@Ignore
	@org.junit.Test
	public void test_05() throws Exception {
		test ("1 ISA - 1 GS - 1 ST - 1 HL 20 - 2 HL 22 - 2 CLMs FOR EACH SUBSCRIBER");
	}

	@Ignore
	@org.junit.Test
	public void test_06() throws Exception {
		test ("1 ISA - 1 GS - 1 ST - 1 HL 20 - 1 HL 22 - 1 CLM - 1 HL 20 - 1 HL 22 - 1 CLM");
	}

	@Ignore
	@org.junit.Test
	public void test_07() throws Exception {
		test ("1 ISA - 1 GS - 1 ST - 1 HL 20 - 1 HL 22 - 2 CLMs - 1 HL 20 - 1 HL 22 - 2 CLMs");
	}

	@Ignore
	@org.junit.Test
	public void test_08() throws Exception {
		test ("1 ISA - 1 GS - 1 ST - 1 HL 20 - 1 HL 22 - 1 CLM - 1 ST - 1 HL 20 - 1 HL 22 - 1 CLM");
	}

	@Ignore
	@org.junit.Test
	public void test_09() throws Exception {
		test ("1 ISA - 1 GS - 3 STs");
	}

	@Ignore
	@org.junit.Test
	public void test_10() throws Exception {
		test ("1 ISA - 2 GSs");
	}

	@Ignore
	@org.junit.Test
	public void test_11() throws Exception {
		test ("2 ISAs");
	}

	@Ignore
	@org.junit.Test
	public void test_12() throws Exception {
		test ("837PFile_for de-batcher_multiple_fil_configs_inonefile");
	}

	@Ignore
	@org.junit.Test
	public void test_13() throws Exception {
		test ("Patient");
	}

	@Ignore
	@org.junit.Test
	public void test_Large() throws Exception {
		long startTime = System.currentTimeMillis();
		test ("LargeP");
		long endTime = System.currentTimeMillis();
		logger.info("That took " + (endTime - startTime) + " milliseconds");
	}

	@Ignore
	@org.junit.Test
	public void test_P_NoRefD9() throws Exception {
		test ("NoRefD9_P");
	}

	@Ignore
	@org.junit.Test
	public void test_P_RefD9() throws Exception {
		test ("RefD9_P");
	}

	@Ignore
	@org.junit.Test
	public void test_I_NoRefD9() throws Exception {
		test ("NoRefD9_I");
	}

	@Ignore
	@org.junit.Test
	public void test_I_RefD9() throws Exception {
		test ("RefD9_I");
	}
	
	private void test (String name) throws Exception {
		debatcher.debatch (name, getInputStream(name));
	}

	private InputStream getInputStream(String name) throws Exception {
		return Thread.currentThread().getContextClassLoader().getResourceAsStream("files/" + name + ".txt");
	}
}
