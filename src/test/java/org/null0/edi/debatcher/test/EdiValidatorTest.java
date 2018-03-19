package org.null0.edi.debatcher.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.InputStream;

import org.junit.Ignore;
import org.null0.edi.debatcher.Config;
import org.null0.edi.debatcher.Debatcher;
import org.null0.edi.debatcher.DebatcherException;
import org.null0.edi.debatcher.EdiValidator;
import org.null0.edi.debatcher.EdiValidatorDefault;
import org.null0.edi.debatcher.MetadataLoggerDefault;

public class EdiValidatorTest {
	static String DateFormatForFileNames = "yyyyMMdd-HHmmss-SSS";
	private String directory;

	@org.junit.Before
	public void setup() throws Exception {
		directory = new Config().getOutputDirectory().toString();
	}

	@Ignore
	@org.junit.Test
	public void test_valid() throws Exception {
		validate("valid", EdiValidator.TA1_ERROR_ISA06);
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
		validate("InvalidInterchangeSenderId", EdiValidator.TA1_ERROR_ISA06);
	}

	@org.junit.Test
	public void test_ISA06EmptyInterchangeSenderId() throws Exception {
		validate("emptyInterchangeSenderId", EdiValidator.TA1_ERROR_ISA06);
	}

	@org.junit.Test
	public void test_ISA07ReceiverQualifier() throws Exception {
		validate("InvalidISA07ReceiverQualifier", EdiValidator.TA1_ERROR_ISA07);
	}

	@Ignore
	@org.junit.Test
	public void test_InvalidISA08InvalidReceiver() throws Exception {
		validate("InvalidISA08InvalidReceiver", EdiValidator.TA1_ERROR_ISA08);
	}

	@org.junit.Test
	public void test_ISA12() throws Exception {
		validate("InvalidISA12", EdiValidator.TA1_ERROR_ISA12);
	}

	@org.junit.Test
	public void test_ISA13() throws Exception {
		validate("InvalidISA13", EdiValidator.TA1_ERROR_ISA13);
	}

	@org.junit.Test
	public void test_ISA14() throws Exception {
		validate("InvalidISA14", EdiValidator.TA1_ERROR_ISA14);
	}

	@org.junit.Test
	public void test_ISA15() throws Exception {
		validate("InvalidISA15", EdiValidator.TA1_ERROR_ISA15);
	}

	@org.junit.Test
	public void test_ISA13End() throws Exception {
		validate("InvalidISAEnd", EdiValidator.TA1_ERROR_ISAEnd);
	}

	@org.junit.Test
	public void test_IEA01() throws Exception {
		validate("InvalidIEA01", EdiValidator.TA1_ERROR_IEA01);
	}

	@org.junit.Test
	public void test_IEAMissing() throws Exception {
		validate("InvalidIEA", EdiValidator.TA1_ERROR_IEA_MISSING);
	}

	@org.junit.Test
	public void test_GSMissing() throws Exception {
		validate("MissingGS", EdiValidator.TA1_ERROR_GS_MISSING);
	}

	@org.junit.Test
	public void test_ISA13Dup() throws Exception {
		validate("InvalidISA13Dup", EdiValidator.TA1_ERROR_ISA13_DUP);
	}

	@org.junit.Test
	public void test_dataSeparator() throws Exception {
		validate("InvalidDataSeparator", EdiValidator.TA1_ERROR_DATA_SEPARATOR);
	}

	@org.junit.Test
	public void test_componentSeparator() throws Exception {
		validate("InvalidComponentSeparator", EdiValidator.TA1_ERROR_COMPONENT_SEPARATOR);
	}

	@org.junit.Test
	public void test_InvalidST01() throws Exception {
		validate("InvalidST01", EdiValidator.IK5_999_ERROR_ST01);
	}

	@org.junit.Test
	public void test_missingSE() throws Exception {
		validate("missingSE", EdiValidator.IK5_999_ERROR_SE);
	}

	@org.junit.Test
	public void test_invalidSt02() throws Exception {
		validate("InvalidST02", EdiValidator.IK5_999_ERROR_ST02);
	}

	@org.junit.Test
	public void test_InvalidSE01() throws Exception {
		validate("InvalidSE01", EdiValidator.IK5_999_ERROR_SE01);
	}

	@org.junit.Test
	public void test_InvalidSt03() throws Exception {
		validate("InvalidSt03", EdiValidator.IK5_999_ERROR_ST03);
	}

	@org.junit.Test
	public void test_InvalidSt03_Ins() throws Exception {
		validate("InvalidSt03_Ins", EdiValidator.IK5_999_ERROR_ST03);
	}

	@org.junit.Test
	public void test_InvalidGS01() throws Exception {
		validate("InvalidGS01", EdiValidator.AK9_999_ERROR_GS01);
	}

	@org.junit.Test
	public void test_InvalidGS08() throws Exception {
		validate("InvalidGS08", EdiValidator.AK9_999_ERROR_GS08);
	}

	@org.junit.Test
	public void test_missingGE() throws Exception {
		validate("missingGE", EdiValidator.AK9_999_ERROR_GE_MISSING);
	}

	@org.junit.Test
	public void test_InvalidGE01() throws Exception {
		validate("InvalidGE01", EdiValidator.AK9_999_ERROR_GE01);
	}

	@org.junit.Test
	public void test_InvalidGS06() throws Exception {
		validate("InvalidGS06", EdiValidator.AK9_999_ERROR_GS06);
	}

	public void validate(String file, String errorCode) throws Exception {
		long batchId = 0;
		try {
			EdiValidator ediValidator = new EdiValidatorDefault(true);
			new Debatcher(new MetadataLoggerDefault(), ediValidator).debatch(file, batchId, getIs(file), directory, false);
			fail("Expecting error " + errorCode);
		} catch (DebatcherException e) {
			assertEquals(errorCode, e.getErrorCode());
		}
	}

	private InputStream getIs(String fileName) throws Exception {
		return Thread.currentThread().getContextClassLoader().getResourceAsStream("files/validations/" + fileName + ".txt");
	}
}
