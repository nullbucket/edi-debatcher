package org.null0.edi.debatcher.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.InputStream;

import org.junit.Ignore;
import org.null0.edi.debatcher.Config;
import org.null0.edi.debatcher.Debatcher;
import org.null0.edi.debatcher.DebatcherException;
import org.null0.edi.debatcher.EncounterEdiValidator;
import org.null0.edi.debatcher.EncounterEdiValidatorDefault;
import org.null0.edi.debatcher.MetadataLoggerDefault;

public class TestEdiValidator {
	
	private String outputLocation = "/home/developer/lnxshare/output/";
	static String DateFormatForFileNames = "yyyyMMdd-HHmmss-SSS";

	@org.junit.Before
	public void setup() throws Exception {
		String outputBase = Config.getSharedBaseDirPath();
		if (outputBase != null) {
			outputLocation = outputBase + "output/";
		}
	}

	@Ignore
	@org.junit.Test
	public void test_valid() throws Exception {

		validate("valid", EncounterEdiValidator.TA1_ERROR_ISA06);
	}

	@Ignore
	@org.junit.Test
	public void test_ISA11() throws Exception {

		String file = "InvalidISA11";
		new Debatcher(new MetadataLoggerDefault()).debatch(file, getIs(file));
	}

	// Test
	@org.junit.Test
	public void test_ISA06InvalidInterchangeSenderId() throws Exception {

		validate("InvalidInterchangeSenderId", EncounterEdiValidator.TA1_ERROR_ISA06);
	}

	@org.junit.Test
	public void test_ISA06EmptyInterchangeSenderId() throws Exception {

		validate("emptyInterchangeSenderId", EncounterEdiValidator.TA1_ERROR_ISA06);
	}

	@org.junit.Test
	public void test_ISA07ReceiverQualifier() throws Exception {

		validate("InvalidISA07ReceiverQualifier", EncounterEdiValidator.TA1_ERROR_ISA07);
	}

	@Ignore
	@org.junit.Test
	public void test_InvalidISA08InvalidReceiver() throws Exception {

		validate("InvalidISA08InvalidReceiver", EncounterEdiValidator.TA1_ERROR_ISA08);
	}

	@org.junit.Test
	public void test_ISA12() throws Exception {

		validate("InvalidISA12", EncounterEdiValidator.TA1_ERROR_ISA12);
	}

	@org.junit.Test
	public void test_ISA13() throws Exception {

		validate("InvalidISA13", EncounterEdiValidator.TA1_ERROR_ISA13);
	}

	@org.junit.Test
	public void test_ISA14() throws Exception {

		validate("InvalidISA14", EncounterEdiValidator.TA1_ERROR_ISA14);
	}

	@org.junit.Test
	public void test_ISA15() throws Exception {

		validate("InvalidISA15", EncounterEdiValidator.TA1_ERROR_ISA15);
	}

	@org.junit.Test
	public void test_ISA13End() throws Exception {

		validate("InvalidISAEnd", EncounterEdiValidator.TA1_ERROR_ISAEnd);
	}

	@org.junit.Test
	public void test_IEA01() throws Exception {

		validate("InvalidIEA01", EncounterEdiValidator.TA1_ERROR_IEA01);
	}

	@org.junit.Test
	public void test_IEAMissing() throws Exception {

		validate("InvalidIEA", EncounterEdiValidator.TA1_ERROR_IEA_MISSING);
	}

	@org.junit.Test
	public void test_GSMissing() throws Exception {

		validate("MissingGS", EncounterEdiValidator.TA1_ERROR_GS_MISSING);
	}

	@org.junit.Test
	public void test_ISA13Dup() throws Exception {

		validate("InvalidISA13Dup", EncounterEdiValidator.TA1_ERROR_ISA13_DUP);
	}

	@org.junit.Test
	public void test_dataSeparator() throws Exception {

		validate("InvalidDataSeparator", EncounterEdiValidator.TA1_ERROR_DATA_SEPARATOR);
	}

	@org.junit.Test
	public void test_componentSeparator() throws Exception {

		validate("InvalidComponentSeparator", EncounterEdiValidator.TA1_ERROR_COMPONENT_SEPARATOR);
	}

	@org.junit.Test
	public void test_InvalidST01() throws Exception {

		validate("InvalidST01", EncounterEdiValidator.IK5_999_ERROR_ST01);
	}

	@org.junit.Test
	public void test_missingSE() throws Exception {

		validate("missingSE", EncounterEdiValidator.IK5_999_ERROR_SE);
	}

	@org.junit.Test
	public void test_invalidSt02() throws Exception {

		validate("InvalidST02", EncounterEdiValidator.IK5_999_ERROR_ST02);
	}

	@org.junit.Test
	public void test_InvalidSE01() throws Exception {

		validate("InvalidSE01", EncounterEdiValidator.IK5_999_ERROR_SE01);
	}

	@org.junit.Test
	public void test_InvalidSt03() throws Exception {

		validate("InvalidSt03", EncounterEdiValidator.IK5_999_ERROR_ST03);
	}

	@org.junit.Test
	public void test_InvalidSt03_Ins() throws Exception {

		validate("InvalidSt03_Ins", EncounterEdiValidator.IK5_999_ERROR_ST03);
	}

	@org.junit.Test
	public void test_InvalidGS01() throws Exception {

		validate("InvalidGS01", EncounterEdiValidator.AK9_999_ERROR_GS01);
	}

	@org.junit.Test
	public void test_InvalidGS08() throws Exception {

		validate("InvalidGS08", EncounterEdiValidator.AK9_999_ERROR_GS08);
	}

	@org.junit.Test
	public void test_missingGE() throws Exception {

		validate("missingGE", EncounterEdiValidator.AK9_999_ERROR_GE_MISSING);
	}

	@org.junit.Test
	public void test_InvalidGE01() throws Exception {

		validate("InvalidGE01", EncounterEdiValidator.AK9_999_ERROR_GE01);
	}

	@org.junit.Test
	public void test_InvalidGS06() throws Exception {

		validate("InvalidGS06", EncounterEdiValidator.AK9_999_ERROR_GS06);
	}

	public void validate(String file, String errorCode) throws Exception {
		long batchId = 0;
		try {
			EncounterEdiValidator ediValidator = new EncounterEdiValidatorDefault(true);
			new Debatcher(new MetadataLoggerDefault(), ediValidator).debatch(file, batchId, getIs(file), outputLocation, false);
			fail("Expecting error " + errorCode);
		} catch (DebatcherException e) {
			assertEquals(errorCode, e.getErrorCode());
		}
	}

	private InputStream getIs(String fileName) throws Exception {
		return getClass().getClassLoader().getResourceAsStream("files/validations/" + fileName + ".txt");
	}

}
