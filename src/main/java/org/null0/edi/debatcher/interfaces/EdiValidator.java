package org.null0.edi.debatcher.interfaces;

import java.util.HashMap;
import java.util.Map;

// TODO: WTF? This thing imported itself before?
//import org.null0.edi.debatcher.validation.EncounterEdiValidator.ERROR;

public interface EdiValidator {
	static enum X12_ELEMENT {
		ISA06, ISA07, ISA08, ISA11, ISA12, ISA13, ISA14, ISA15, ISA16, IEA01, IEA02, ISAEnd, DATA_SEPARATOR, GS, GS01, GS06, GS08, GE, GE01, ST01, ST02, ST03, SE, SE01
	};

	static final String TA1_ERROR_ISA06 = "006";
	static final String TA1_ERROR_ISA07 = "007";
	static final String TA1_ERROR_ISA08 = "008";
	static final String TA1_ERROR_ISA11 = "002";
	static final String TA1_ERROR_ISA12 = "003";
	static final String TA1_ERROR_ISA13 = "018";
	static final String TA1_ERROR_ISA13_DUP = "025";
	static final String TA1_ERROR_ISA14 = "019";
	static final String TA1_ERROR_ISA15 = "020";
	static final String TA1_ERROR_IEA01 = "021";
	static final String TA1_ERROR_ISAIEA = "022";
	static final String TA1_ERROR_IEA_MISSING = "023";
	static final String TA1_ERROR_GS_MISSING = "024";
	static final String TA1_ERROR_ISAEnd = "004";
	static final String TA1_ERROR_DATA_SEPARATOR = "026";
	static final String TA1_ERROR_COMPONENT_SEPARATOR = "027";
	static final String IK3_999_ERROR_MISS_SEG = "IK304-3";
	static final String IK3_999_ERROR_DATA_ELEMENT = "IK304-8";
	static final String IK3_999_ERROR_MISS_DATA_ELEMENT = "IK403-1";
	static final String IK5_999_ERROR_ST01 = "IK502-1";
	static final String IK5_999_ERROR_ST02 = "IK502-3";
	static final String IK5_999_ERROR_ST03 = "IK502-19";
	static final String IK5_999_ERROR_SE01 = "IK502-4";
	static final String IK5_999_ERROR_SE = "IK502-2";
	static final String AK9_999_ERROR_GS01 = "AK905-1";
	static final String AK9_999_ERROR_GS06 = "AK905-4";
	static final String AK9_999_ERROR_GS08 = "AK905-2";
	static final String AK9_999_ERROR_GE01 = "AK905-5";
	static final String AK9_999_ERROR_GE_MISSING = "AK905-3";
	
	static final String[] validISA12 = { "00501" };  // TODO: specific to 837 claims; decouple
	static final String[] validISA15 = { "P", "T" }; // TODO: specific to 837 claims; decouple
	
	// TODO: all these below were hard-coded AND this is NOT the proper way to validate!
	// According to EDI standards, the valid values below are defined by fields
	// from the special fixed-sized ISA01 segment.
	static final String[] validISA11 = { ":", "^" };
	static final String[] validISAEnd = { "~" }; // { ":", "~", "\n" };
	static final String[] validDataElementSeparator = { "*" };
	static final String[] validComponentSeparator = { ":", ">" };
	
	// TODO: these below are specific to 837 claims; decouple
	static final String[] validSt03ForP = { "005010X222A1", "005010X222A2" };
	static final String[] validSt03ForI = { "005010X223A2", "005010X223A3" };
	static enum CLAIM_TYPE {
		PRO, INS, OTH
	};

	public boolean validate(long batchId, X12_ELEMENT elementName, String data, String compareWithData) throws Exception;
	public void logError(long batchId, String errorCode, ERROR errorType, String errorMessage) throws Exception;

	public enum ERROR {
		TYPE_TA1("TA1"), TYPE_999("999"), TYPE_BRE("BRE");
		private static final Map<String, ERROR> stringToEnum = new HashMap<String, ERROR>();

		static {
			for (ERROR code : values()) {
				stringToEnum.put(code.getCode(), code);
			}
		}

		public static ERROR fromLegacyCode(String code) {
			return stringToEnum.get(code);
		}

		private final String code;
		ERROR(String code) {
			this.code = code;
		}
		public String getCode() {
			return code;
		}
	}
}
