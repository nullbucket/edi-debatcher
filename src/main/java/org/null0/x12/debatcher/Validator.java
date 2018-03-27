package org.null0.x12.debatcher;

import java.util.HashMap;
import java.util.Map;

public interface Validator {
	enum ClaimType {
		INS, OTH, PRO
	};

	public enum Error {
		TYPE_999("999"), TYPE_BRE("BRE"), TYPE_TA1("TA1");
		private static Map<String, Error> stringToEnum = new HashMap<>();

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
	enum X12element {
		DATA_SEPARATOR, GE, GE01, GS, GS01, GS06, GS08, IEA01, IEA02, ISA06, ISA07, ISA08, ISA11, ISA12, ISA13, ISA14, ISA15, ISA16, ISAEnd, SE, SE01, ST01, ST02, ST03
	}
	String AK9_999_ERROR_GE_MISSING = "AK905-3";
	String AK9_999_ERROR_GE01 = "AK905-5";
	String AK9_999_ERROR_GS01 = "AK905-1";
	String AK9_999_ERROR_GS06 = "AK905-4";
	String AK9_999_ERROR_GS08 = "AK905-2";
	String IK3_999_ERROR_DATA_ELEMENT = "IK304-8";
	String IK3_999_ERROR_MISS_DATA_ELEMENT = "IK403-1";
	String IK3_999_ERROR_MISS_SEG = "IK304-3";
	String IK5_999_ERROR_SE = "IK502-2";
	String IK5_999_ERROR_SE01 = "IK502-4";
	String IK5_999_ERROR_ST01 = "IK502-1";
	String IK5_999_ERROR_ST02 = "IK502-3";
	String IK5_999_ERROR_ST03 = "IK502-19";
	String TA1_ERROR_COMPONENT_SEPARATOR = "027";
	String TA1_ERROR_DATA_SEPARATOR = "026";
	String TA1_ERROR_GS_MISSING = "024";
	String TA1_ERROR_IEA_MISSING = "023";
	String TA1_ERROR_IEA01 = "021";
	String TA1_ERROR_ISA06 = "006";
	String TA1_ERROR_ISA07 = "007";
	String TA1_ERROR_ISA08 = "008";
	String TA1_ERROR_ISA11 = "002";
	String TA1_ERROR_ISA12 = "003";
	String TA1_ERROR_ISA13 = "018";
	String TA1_ERROR_ISA13_DUP = "025";
	String TA1_ERROR_ISA14 = "019";
	String TA1_ERROR_ISA15 = "020";
	String TA1_ERROR_ISAEnd = "004";
	String TA1_ERROR_ISAIEA = "022";	
	
	/* TODO: Items below were hard-coded... this is NOT the proper way to validate!
	According to EDI standards, the valid values below are defined by fields from the special fixed-sized ISA01 segment.
	If what we are trying to do is enforce localized validation, these values below need to move to a configuration file, 
	AND their checks need to be switchable in the config file (enable/disable). */
	String[] validComponentSeparator = { ":", ">" }; 
	String[] validDataElementSeparator = { "*" };
	String[] validISA11 = { ":", "^" };
	String[] validISA12 = { "00501" }; // TODO: specific to 837 claims; decouple
	String[] validISA15 = { "P", "T" }; // TODO: specific to 837 claims; decouple
	String[] validISAEnd = { "~" }; // { ":", "~", "\n" }; // See TODO at bottom of class
	String[] validSt03ForI = { "005010X223A2", "005010X223A3" }; // TODO: specific to 837 claims; decouple
	String[] validSt03ForP = { "005010X222A1", "005010X222A2" }; // TODO: specific to 837 claims; decouple

	void logError(long batchId, String errorCode, Error errorType, String errorMessage) throws Exception;
	boolean validate(long batchId, X12element elementName, String data, String compareWithData) throws Exception;	
}
