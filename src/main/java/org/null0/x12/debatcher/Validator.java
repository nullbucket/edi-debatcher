package org.null0.x12.debatcher;

import java.util.HashMap;
import java.util.Map;

public interface Validator {
	static enum ClaimType {
		INS, OTH, PRO
	};

	public enum Error {
		TYPE_999("999"), TYPE_BRE("BRE"), TYPE_TA1("TA1");
		private static final Map<String, Error> stringToEnum = new HashMap<String, Error>();

		static {
			for (Error code : values()) {
				stringToEnum.put(code.getCode(), code);
			}
		}

		public static Error fromLegacyCode(String code) {
			return stringToEnum.get(code);
		}

		private final String code;

		Error(String code) {
			this.code = code;
		}

		public String getCode() {
			return code;
		}
	}
	static enum X12element {
		DATA_SEPARATOR, GE, GE01, GS, GS01, GS06, GS08, IEA01, IEA02, ISA06, ISA07, ISA08, ISA11, ISA12, ISA13, ISA14, ISA15, ISA16, ISAEnd, SE, SE01, ST01, ST02, ST03
	}
	static final String AK9_999_ERROR_GE_MISSING = "AK905-3";
	static final String AK9_999_ERROR_GE01 = "AK905-5";
	static final String AK9_999_ERROR_GS01 = "AK905-1";
	static final String AK9_999_ERROR_GS06 = "AK905-4";
	static final String AK9_999_ERROR_GS08 = "AK905-2";
	static final String IK3_999_ERROR_DATA_ELEMENT = "IK304-8";
	static final String IK3_999_ERROR_MISS_DATA_ELEMENT = "IK403-1";
	static final String IK3_999_ERROR_MISS_SEG = "IK304-3";
	static final String IK5_999_ERROR_SE = "IK502-2";
	static final String IK5_999_ERROR_SE01 = "IK502-4";
	static final String IK5_999_ERROR_ST01 = "IK502-1";
	static final String IK5_999_ERROR_ST02 = "IK502-3";
	static final String IK5_999_ERROR_ST03 = "IK502-19";
	static final String TA1_ERROR_COMPONENT_SEPARATOR = "027";
	static final String TA1_ERROR_DATA_SEPARATOR = "026";
	static final String TA1_ERROR_GS_MISSING = "024";
	static final String TA1_ERROR_IEA_MISSING = "023";
	static final String TA1_ERROR_IEA01 = "021";
	static final String TA1_ERROR_ISA06 = "006";
	static final String TA1_ERROR_ISA07 = "007";
	static final String TA1_ERROR_ISA08 = "008";
	static final String TA1_ERROR_ISA11 = "002";
	static final String TA1_ERROR_ISA12 = "003";
	static final String TA1_ERROR_ISA13 = "018";
	static final String TA1_ERROR_ISA13_DUP = "025";
	static final String TA1_ERROR_ISA14 = "019";
	static final String TA1_ERROR_ISA15 = "020";
	static final String TA1_ERROR_ISAEnd = "004";
	static final String TA1_ERROR_ISAIEA = "022";	
	
	/* TODO: Items below were hard-coded... this is NOT the proper way to validate!
	According to EDI standards, the valid values below are defined by fields from the special fixed-sized ISA01 segment.
	If what we are trying to do is enforce localized validation, these values below need to move to a configuration file, 
	AND their checks need to be switchable in the config file (enable/disable). */
	static final String[] validComponentSeparator = { ":", ">" }; 
	static final String[] validDataElementSeparator = { "*" };
	static final String[] validISA11 = { ":", "^" };
	static final String[] validISA12 = { "00501" }; // TODO: specific to 837 claims; decouple
	static final String[] validISA15 = { "P", "T" }; // TODO: specific to 837 claims; decouple
	static final String[] validISAEnd = { "~" }; // { ":", "~", "\n" }; // See TODO at bottom of class
	static final String[] validSt03ForI = { "005010X223A2", "005010X223A3" }; // TODO: specific to 837 claims; decouple
	static final String[] validSt03ForP = { "005010X222A1", "005010X222A2" }; // TODO: specific to 837 claims; decouple

	public void logError(long batchId, String errorCode, Error errorType, String errorMessage) throws Exception;
	public boolean validate(long batchId, X12element elementName, String data, String compareWithData) throws Exception;	
}
