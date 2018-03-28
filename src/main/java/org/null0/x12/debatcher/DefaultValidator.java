package org.null0.x12.debatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* TODO: stuff specific to claims (837) needs to be moved to a separate implementation
then all the other code in this class could just be left here as a base (default) implementation. */

/* TODO: If you compare the validations here against an actual EDI spec (such as ASC12 X223A3_Consolidated from WPC, 2014),
you'll see that the validations here are WAY TOO LOCALIZED to be used as for general purpose validation. This validator needs 
to be redesigned to be configurable: allow a validation to be turned off, and/or make the validations configurable, especially lists!
 */

/* TODO: This class is a result of a serious lack of object-oriented design; smells horrible. */

public class DefaultValidator implements Validator {
	public enum ClaimType {
		INS, OTH, PRO
	}


	private static final Logger logger = LoggerFactory.getLogger(DefaultValidator.class);
	private final Config config;
	private final List<String> isa13List = new ArrayList<>();

	public DefaultValidator(Config config) {
		this.config = config;
	}

	@Override
	public void logError(long batchId, String errorCode, Error errorType, String errorMessage) throws DebatcherException {
		logError(batchId, errorCode, errorType, errorMessage, null, null); // public doesn't log actual/expected
	}

	@Override
	public boolean validate(long batchId, X12element elementName, String actual, String expected) throws Exception {
		logger.debug("Validating {} for data {} and expected {}", elementName, actual, expected);

		// TODO: this is fugly... each case should just be a public method
		String trimmed = StringUtils.trimToEmpty(actual);
		switch (elementName) {
		case ISA06:
			String[] senders = config.getValidSenders();
			if (senders.length > 0 && !Arrays.asList(senders).contains(trimmed)) {
				logError(batchId, TA1_ERROR_ISA06, Error.TYPE_TA1, "Invalid Interchange Sender ID(ISA06)", trimmed, expected);
			}
			break;
		case ISA07:
			// Why is this hard-coded to ZZ? 'Mutually defined' doesn't apply in all cases.
			if (!equals("ZZ", trimmed)) {
				logError(batchId, TA1_ERROR_ISA07, Error.TYPE_TA1, "Invalid Interchange Receiver ID(ISA07) Qualifier", trimmed, expected); // ZZ Mutually Defined
			}
			break;
		case ISA08:
			String[] receivers = config.getValidReceivers();
			if (receivers.length > 0 && !Arrays.asList(receivers).contains(trimmed)) {
				logError(batchId, TA1_ERROR_ISA08, Error.TYPE_TA1, "Invalid Interchange Receiver ID(ISA08)", trimmed, expected);
			}
			break;
		case ISA11:
			if (!Arrays.asList(validISA11).contains(trimmed)) {
				logError(batchId, TA1_ERROR_ISA11, Error.TYPE_TA1, "Invalid ISA11 data", trimmed, expected);
			}
			break;
		case ISA12:
			if (!Arrays.asList(validISA12).contains(trimmed)) {
				logError(batchId, TA1_ERROR_ISA12, Error.TYPE_TA1, "Invalid ISA12 data", trimmed, expected);
			}
			break;
		case ISA13:
			if (StringUtils.isAllBlank(actual) || actual.length() != 9 || !isNumeric(actual)) {
				logError(batchId, TA1_ERROR_ISA13, Error.TYPE_TA1, "Invalid ISA13 Interchange control number data", actual, expected);
			}
			if (isa13List.contains(trimmed)) {
				logError(batchId, TA1_ERROR_ISA13_DUP, Error.TYPE_TA1, "Duplicate ISA13 Interchange control number", trimmed, expected);
			}
			isa13List.add(trimmed);

			break;
		case ISA14:
			if (!(equals("0", actual) || equals("1", actual))) { // 0 = no interchange ACK requested, 1= interchange ACK requested (TA1)
				logError(batchId, TA1_ERROR_ISA14, Error.TYPE_TA1, "Invalid Acknowledge Requested(ISA14)", actual, expected);
			}
			break;
		case ISA15:
			if (!Arrays.asList(validISA15).contains(trimmed)) {
				logError(batchId, TA1_ERROR_ISA15, Error.TYPE_TA1, "Invalid ISA15 data", trimmed, expected);
			}
			break;
		case ISA16:
			if (!Arrays.asList(validComponentSeparator).contains(actual)) {
				logError(batchId, TA1_ERROR_COMPONENT_SEPARATOR, Error.TYPE_TA1, "Invalid ISA16 data", actual, expected);
			}
			break;
		case ISAEnd:
			if (!Arrays.asList(validISAEnd).contains(actual)) {
				logError(batchId, TA1_ERROR_ISAEnd, Error.TYPE_TA1, "The Segment Terminator is invalid - ISA segment must be a fixed length ", actual, expected);
			}
			break;
		case IEA01:
			if (StringUtils.isAllBlank(actual)) {
				logError(batchId, TA1_ERROR_IEA_MISSING, Error.TYPE_TA1, "Improper (Premature) end-of-file (Transmission)", actual, expected);
			} else if (!equals(actual, expected)) {
				logError(batchId, TA1_ERROR_IEA01, Error.TYPE_TA1, "Invalid Number of Included Group value(IEA01) ", actual, expected);
			}
			break;
		case IEA02:
			break;
		case DATA_SEPARATOR:
			if (!Arrays.asList(validDataElementSeparator).contains(actual)) {
				logError(batchId, TA1_ERROR_DATA_SEPARATOR, Error.TYPE_TA1, "Invalid data element separator - ISA segment must be a fixed length", actual, expected);
			}
			break;
		case GS:
			if (StringUtils.isAllBlank(actual)) {
				logError(batchId, TA1_ERROR_GS_MISSING, Error.TYPE_TA1, "No GS segment after ISA", null, expected);
			}
			break;
		case GS01:
			// TODO: this is way too localized, hard-coded to "HC Health Care Claim (837)"
			if (!equals("HC", actual)) {
				logError(batchId, AK9_999_ERROR_GS01, Error.TYPE_999, "Functional Group Not Supported", actual, expected);
			}
			break;
		case GS06:
			if (!equals(actual, expected)) {
				logError(batchId, AK9_999_ERROR_GS06, Error.TYPE_999, "Group Control Number in the Functional Group Header and trailer Do Not Agree", actual, expected);
			}
			break;
		case GS08:
			// TODO: this is way too localized, hard-coded to certain versions of claims
			if (!Arrays.asList(validSt03ForP).contains(trimmed) && !Arrays.asList(validSt03ForI).contains(trimmed)) {
				logError(batchId, AK9_999_ERROR_GS08, Error.TYPE_999, "Functional Group Version Not Supported", trimmed, expected);
			}
			break;
		case GE:
			if (StringUtils.isAllBlank(trimmed)) {
				logError(batchId, AK9_999_ERROR_GE_MISSING, Error.TYPE_999, "Functional Group Trailer Missing", trimmed, expected);
			}
			break;
		case GE01:
			if (!equals(trimmed, expected)) {
				logError(batchId, AK9_999_ERROR_GE01, Error.TYPE_999, "Number of Included Transaction Sets Does Not Match Actual Count", trimmed, expected);
			}
			break;
		case ST01:
			if (StringUtils.isAllBlank(trimmed) || !"837".equals(trimmed)) {
				logError(batchId, IK5_999_ERROR_ST01, Error.TYPE_999, "Transaction Set Not Supported", trimmed, expected);
			}
			break;
		case ST02:
			if (!equals(trimmed, expected)) {
				logError(batchId, IK5_999_ERROR_ST02, Error.TYPE_999, "Transaction Set Control Number in Header and Trailer Do Not Match ", trimmed, expected);
			}
			break;

		// TODO: This is way too localized, domain-specific to 837 claims
		case ST03:
			if (ClaimType.PRO.name().equals(expected) && !Arrays.asList(validSt03ForP).contains(trimmed)
					|| ClaimType.INS.name().equals(expected) && !Arrays.asList(validSt03ForI).contains(trimmed)
					|| !ClaimType.PRO.name().equals(expected) && !ClaimType.INS.name().equals(expected)) {
				logError(batchId, IK5_999_ERROR_ST03, Error.TYPE_999, "Invalid Transaction Set Implementation Convention Reference ", trimmed, expected);
			}
			break;
		case SE:
			if (StringUtils.isAllBlank(trimmed)) {
				logError(batchId, IK5_999_ERROR_SE, Error.TYPE_999, "Transaction Set Trailer Missing", trimmed, expected);
			}
			break;
		case SE01:
			if (!equals(trimmed, expected)) {
				logError(batchId, IK5_999_ERROR_SE01, Error.TYPE_999, "Number of Included Segments Does Not Match Actual Count", trimmed, expected);
			}
			break;
		default:
			break;
		}

		return true;
	}

	private boolean equals(String str1, String str2) {
		return str1 != null && str2 != null && str1.trim().equals(str2.trim());
	}

	private boolean isNumeric(String s) {
		return s != null && s.matches("\\d+");
	}

	private void logError(long batchId, String errorCode, Error errorType, String errorMessage, String actual, String expected) throws DebatcherException {
		// TODO: enable metadata logging but reuse same instance
		// new MetadataServicesImpl().insertValidationError(batchId, errorCode, errorType);

		String msg = String.format("batch=%d, type=%s, code=%s, msg='%s'%s%s",
				batchId,
				errorType.getCode(),
				errorCode,
				errorMessage,
				actual == null ? "" : String.format(", value=%s", actual),
				expected == null ? "" : String.format(", expected=%s", expected));

		if (!config.willRejectOnValidationError()) {
			logger.warn(msg);
			return;
		}

		logger.error(msg);
		throw new DebatcherException(errorMessage, errorCode, errorType, batchId);
	}
}
