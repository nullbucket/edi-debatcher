package org.null0.edi.debatcher.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

import java.io.InputStream;
import java.nio.file.Paths;

import org.junit.Ignore;
import org.null0.edi.debatcher.Debatcher;
import org.null0.edi.debatcher.DebatcherException;
import org.null0.edi.debatcher.interfaces.Config;
import org.null0.edi.debatcher.interfaces.EdiValidator;
import org.null0.edi.debatcher.EdiValidatorDefault;
import org.null0.edi.debatcher.MetadataLoggerDefault;

public class EdiValidatorTest {
	static String DateFormatForFileNames = "yyyyMMdd-HHmmss-SSS";
	
	@org.junit.Before
	public void setup() throws Exception {
	}

	@Ignore
	@org.junit.Test
	public void test_valid() throws Exception {
		checkError("valid", EdiValidator.TA1_ERROR_ISA06);
	}

	@Ignore
	@org.junit.Test
	public void test_ISA11() throws Exception {
		String id = "InvalidISA11";
		new Debatcher().debatch(id, openStream(id));
	}

	// Test
	@org.junit.Test
	public void test_ISA06InvalidInterchangeSenderId() throws Exception {
		checkError("InvalidInterchangeSenderId", EdiValidator.TA1_ERROR_ISA06);
	}

	@org.junit.Test
	public void test_ISA06EmptyInterchangeSenderId() throws Exception {	
		checkError("emptyInterchangeSenderId", EdiValidator.TA1_ERROR_ISA06);
	}

	@org.junit.Test
	public void test_ISA07ReceiverQualifier() throws Exception {
		checkError("InvalidISA07ReceiverQualifier", EdiValidator.TA1_ERROR_ISA07);
	}

	@Ignore
	@org.junit.Test
	public void test_InvalidISA08InvalidReceiver() throws Exception {
		checkError("InvalidISA08InvalidReceiver", EdiValidator.TA1_ERROR_ISA08);
	}

	@org.junit.Test
	public void test_ISA12() throws Exception {
		checkError("InvalidISA12", EdiValidator.TA1_ERROR_ISA12);
	}

	@org.junit.Test
	public void test_ISA13() throws Exception {
		checkError("InvalidISA13", EdiValidator.TA1_ERROR_ISA13);
	}

	@org.junit.Test
	public void test_ISA14() throws Exception {
		checkError("InvalidISA14", EdiValidator.TA1_ERROR_ISA14);
	}

	@org.junit.Test
	public void test_ISA15() throws Exception {
		checkError("InvalidISA15", EdiValidator.TA1_ERROR_ISA15);
	}

	@org.junit.Test
	public void test_ISA13End() throws Exception {
		checkError("InvalidISAEnd", EdiValidator.TA1_ERROR_ISAEnd);
	}

	@org.junit.Test
	public void test_IEA01() throws Exception {
		checkError("InvalidIEA01", EdiValidator.TA1_ERROR_IEA01);
	}

	@org.junit.Test
	public void test_IEAMissing() throws Exception {
		checkError("InvalidIEA", EdiValidator.TA1_ERROR_IEA_MISSING);
	}

	@org.junit.Test
	public void test_GSMissing() throws Exception {
		checkError("MissingGS", EdiValidator.TA1_ERROR_GS_MISSING);
	}

	@org.junit.Test
	public void test_ISA13Dup() throws Exception {
		checkError("InvalidISA13Dup", EdiValidator.TA1_ERROR_ISA13_DUP);
	}

	@org.junit.Test
	public void test_dataSeparator() throws Exception {
		checkError("InvalidDataSeparator", EdiValidator.TA1_ERROR_DATA_SEPARATOR);
	}

	@org.junit.Test
	public void test_componentSeparator() throws Exception {
		checkError("InvalidComponentSeparator", EdiValidator.TA1_ERROR_COMPONENT_SEPARATOR);
	}

	@org.junit.Test
	public void test_InvalidST01() throws Exception {
		checkError("InvalidST01", EdiValidator.IK5_999_ERROR_ST01);
	}

	@org.junit.Test
	public void test_missingSE() throws Exception {
		checkError("missingSE", EdiValidator.IK5_999_ERROR_SE);
	}

	@org.junit.Test
	public void test_invalidSt02() throws Exception {
		checkError("InvalidST02", EdiValidator.IK5_999_ERROR_ST02);
	}

	@org.junit.Test
	public void test_InvalidSE01() throws Exception {
		checkError("InvalidSE01", EdiValidator.IK5_999_ERROR_SE01);
	}

	@org.junit.Test
	public void test_InvalidSt03() throws Exception {
		checkError("InvalidSt03", EdiValidator.IK5_999_ERROR_ST03);
	}

	@org.junit.Test
	public void test_InvalidSt03_Ins() throws Exception {
		checkError("InvalidSt03_Ins", EdiValidator.IK5_999_ERROR_ST03);
	}

	@org.junit.Test
	public void test_InvalidGS01() throws Exception {
		checkError("InvalidGS01", EdiValidator.AK9_999_ERROR_GS01);
	}

	@org.junit.Test
	public void test_InvalidGS08() throws Exception {
		checkError("InvalidGS08", EdiValidator.AK9_999_ERROR_GS08);
	}

	@org.junit.Test
	public void test_missingGE() throws Exception {
		checkError("missingGE", EdiValidator.AK9_999_ERROR_GE_MISSING);
	}

	@org.junit.Test
	public void test_InvalidGE01() throws Exception {
		checkError("InvalidGE01", EdiValidator.AK9_999_ERROR_GE01);
	}

	@org.junit.Test
	public void test_InvalidGS06() throws Exception {
		checkError("InvalidGS06", EdiValidator.AK9_999_ERROR_GS06);
	}

	private void checkError(String id, String errorCode) throws Exception {
		long batchId = 0;
		try {
			createDebatcher().debatch(id, batchId, openStream(id));
			fail("Expecting error " + errorCode);
		} catch (DebatcherException e) {
			assertEquals(errorCode, e.getErrorCode());
		}
	}
	
	private Debatcher createDebatcher() {
		Config mockConfig = mock(Config.class);
		when(mockConfig.getBufferSize()).thenReturn(1024);
		when(mockConfig.getValidReceivers()).thenReturn(new String[]{"AAA"});
		when(mockConfig.getValidSenders()).thenReturn(new String[]{"BBB"});
		when(mockConfig.getOutputDirectory()).thenReturn(Paths.get("./"));
		
		return new Debatcher (
				mockConfig,
				new EdiValidatorDefault(true),
				new MetadataLoggerDefault());
	}
	
	private InputStream openStream(String id) throws Exception {
		String file = "files/validations/" + id + ".txt";
		return Thread.currentThread().getContextClassLoader().getResourceAsStream(file);
	}
}
