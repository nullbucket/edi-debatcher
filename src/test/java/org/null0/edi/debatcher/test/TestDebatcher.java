package org.null0.edi.debatcher.test;

import java.io.InputStream;

import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.null0.edi.debatcher.Config;
// import org.null0.metadata.services.DeBatchedDataLogger;
import org.null0.edi.debatcher.MetadataLogger;

// TODO: This is scary. All the useful tests have been commented out! Why?
// First we'll get the other unit tests to pass (and verify their accuracy)
// then we'll start enabling and verify these unit tests.
//
public class TestDebatcher {
	private static final Logger logger = LoggerFactory.getLogger(TestDebatcher.class);
	private String directory;
	// private MetadataLogger metadataLogger = new DeBatchedDataLogger(); // TODO: metadata logger implementation class 

	@Before
	public void setUp() throws Exception {
		directory = new Config().getOutputDir().toString();
		/*
		 * logger.info("deleting the old messages from the output folder..."); File dir
		 * = new File(DIRECTORY+"/output"); for(File file: dir.listFiles())
		 * file.delete();
		 */
	}
	// Ignored because of new code implementation where we need to have idBatch
	// value in Submission table. So, test cases will fail when there is no entry in
	// submission table.

	/*
	 * @org.junit.Test public void test_0() {
	 * 
	 * try {
	 * 
	 * String file = "BAD FILE"; new
	 * Debatcher(metadataLogger).debatch("transactionId", getIs(file));
	 * 
	 * } catch (Exception e) { e.printStackTrace(); Assert.fail(e.getMessage()); }
	 * 
	 * }
	 */

	/*
	 * @org.junit.Test public void test_1() throws Exception {
	 * 
	 * String file = "1 ISA - 1 GS - 1 ST - 1 HL 20 - 1 HL 22 - 1 CLM"; new
	 * Debatcher(metadataLogger).debatch(file, getIs(file)); }
	 * 
	 * @Ignore
	 * 
	 * @org.junit.Test public void test_2() throws Exception {
	 * 
	 * String file = "1 ISA - 1 GS - 1 ST - 1 HL 20 - 1 HL 22 - 2 CLMs"; new
	 * Debatcher(metadataLogger).debatch(file, getIs(file)); }
	 * 
	 * @Ignore
	 * 
	 * @org.junit.Test public void test_3() throws Exception {
	 * 
	 * String file = "1 ISA - 1 GS - 1 ST - 1 HL 20 - 1 HL 22 - 3 CLMs"; new
	 * Debatcher(metadataLogger).debatch(file, getIs(file)); }
	 * 
	 * @Ignore
	 * 
	 * @org.junit.Test public void test_4() throws Exception {
	 * 
	 * String file =
	 * "1 ISA - 1 GS - 1 ST - 1 HL 20 - 2 HL 22 - 1 CLM FOR EACH SUBSCRIBER"; new
	 * Debatcher(metadataLogger).debatch(file, getIs(file)); }
	 * 
	 * @Ignore
	 * 
	 * @org.junit.Test public void test_5() throws Exception {
	 * 
	 * String file =
	 * "1 ISA - 1 GS - 1 ST - 1 HL 20 - 2 HL 22 - 2 CLMs FOR EACH SUBSCRIBER"; new
	 * Debatcher(metadataLogger).debatch(file, getIs(file)); }
	 * 
	 * @Ignore
	 * 
	 * @org.junit.Test public void test_6() throws Exception {
	 * 
	 * String file =
	 * "1 ISA - 1 GS - 1 ST - 1 HL 20 - 1 HL 22 - 1 CLM - 1 HL 20 - 1 HL 22 - 1 CLM"
	 * ; new Debatcher(metadataLogger).debatch(file, getIs(file)); }
	 * 
	 * @Ignore
	 * 
	 * @org.junit.Test public void test_7() throws Exception {
	 * 
	 * String file =
	 * "1 ISA - 1 GS - 1 ST - 1 HL 20 - 1 HL 22 - 2 CLMs - 1 HL 20 - 1 HL 22 - 2 CLMs"
	 * ; new Debatcher(metadataLogger).debatch(file, getIs(file)); }
	 * 
	 * @Ignore
	 * 
	 * @org.junit.Test public void test_8() throws Exception {
	 * 
	 * String file =
	 * "1 ISA - 1 GS - 1 ST - 1 HL 20 - 1 HL 22 - 1 CLM - 1 ST - 1 HL 20 - 1 HL 22 - 1 CLM"
	 * ; new Debatcher(metadataLogger).debatch(file, getIs(file)); }
	 * 
	 * @Ignore
	 * 
	 * @org.junit.Test public void test_9() throws Exception {
	 * 
	 * String file = "1 ISA - 1 GS - 3 STs"; new
	 * Debatcher(metadataLogger).debatch(file, getIs(file)); }
	 * 
	 * @Ignore
	 * 
	 * @org.junit.Test public void test_10() throws Exception {
	 * 
	 * String file = "1 ISA - 2 GSs"; new Debatcher(metadataLogger).debatch(file,
	 * getIs(file)); }
	 * 
	 * @Ignore
	 * 
	 * @org.junit.Test public void test_11() throws Exception {
	 * 
	 * String file = "2 ISAs"; new Debatcher(metadataLogger).debatch(file,
	 * getIs(file)); }
	 * 
	 * @Ignore
	 * 
	 * @org.junit.Test public void test_12() throws Exception {
	 * 
	 * String file = "837PFile_for de-batcher_multiple_fil_configs_inonefile"; new
	 * Debatcher(metadataLogger).debatch(file, getIs(file)); }
	 * 
	 * @Ignore
	 * 
	 * @org.junit.Test public void test_14() throws Exception {
	 * 
	 * long startTime = System.currentTimeMillis();
	 * 
	 * String file = "LargeP"; new Debatcher(metadataLogger).debatch(file,
	 * getIs(file));
	 * 
	 * long endTime = System.currentTimeMillis(); logger.info("That took " +
	 * (endTime - startTime) + " milliseconds"); }
	 * 
	 * @Ignore
	 * 
	 * @org.junit.Test public void test_15() throws Exception {
	 * 
	 * String file = "Patient"; new Debatcher(metadataLogger).debatch(file,
	 * getIs(file)); }
	 * 
	 * @Ignore
	 * 
	 * @org.junit.Test public void test_P_NoRefD9() throws Exception {
	 * 
	 * String file = "NoRefD9_P"; new Debatcher(metadataLogger).debatch(file,
	 * getIs(file)); }
	 * 
	 * @Ignore
	 * 
	 * @org.junit.Test public void test_P_RefD9() throws Exception {
	 * 
	 * String file = "RefD9_P"; new Debatcher(metadataLogger).debatch(file,
	 * getIs(file)); }
	 * 
	 * @Ignore
	 * 
	 * @org.junit.Test public void test_I_NoRefD9() throws Exception {
	 * 
	 * String file = "NoRefD9_I"; new Debatcher(metadataLogger).debatch(file,
	 * getIs(file)); }
	 * 
	 * @Ignore
	 * 
	 * @org.junit.Test public void test_I_RefD9() throws Exception {
	 * 
	 * String file = "RefD9_I"; new Debatcher(metadataLogger).debatch(file,
	 * getIs(file)); }
	 */

	private InputStream getIs(String fileName) throws Exception {
		// return Files.newInputStream(Paths.get(DIRECTORY, fileName+".txt"));
		return getClass().getClassLoader().getResourceAsStream("files/" + fileName + ".txt");
	}

}
