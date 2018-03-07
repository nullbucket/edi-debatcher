package org.null0.edi.debatcher.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.null0.edi.debatcher.DebatcherException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EncounterEdiValidatorImpl implements EncounterEdiValidator {

	private static final Logger logger = LoggerFactory.getLogger(EncounterEdiValidatorImpl.class);
	private boolean rejectOnValidationError;
	private static final boolean stopAtFirstValidationFailure = true;
	private List<String> isa13List = new ArrayList<String>();

	public EncounterEdiValidatorImpl(boolean rejectOnValidationError) {
		this.rejectOnValidationError = rejectOnValidationError;
	}

	public boolean validate(long batchId, X12_837_ELEMENT elementName, String data, String compareWithData)
			throws Exception {

		logger.debug("Validating {} for data {} and compareWithData {}", elementName, data, compareWithData);

		switch (elementName) {
		case ISA06:
			if (!isValidSenderId(data, batchId)) {
				logError(batchId, TA1_ERROR_ISA06, ERROR.TYPE_TA1, "Invalid Interchange Sender ID(ISA06) " + data);
			}
			break;
		case ISA07:
			if (!equals("ZZ", data)) {
				logError(batchId, TA1_ERROR_ISA07, ERROR.TYPE_TA1,
						"Invalid Interchange Receiver ID(ISA07) Qualifier " + data);// ZZ Mutually Defined
			}
			break;
		case ISA08:

			/*
			 * String receiverIds =
			 * Config.getConfigPropertyValue("integration.properties", "receiverIds");
			 * if(!isEmpty(receiverIds)) { boolean validReceiver = false; String receivers[]
			 * = receiverIds.split(","); for (String receiver: receivers) {
			 * if(equals(receiver, data)) { validReceiver = true; break; } }
			 * if(!validReceiver) { logError(batchId, TA1_ERROR_ISA08, ERROR.TYPE_TA1,
			 * "Invalid Interchange Receiver ID(ISA08) "+data); } }
			 */

			break;
		case ISA11:
			if (!Arrays.asList(validISA11).contains(data)) {
				logError(batchId, TA1_ERROR_ISA11, ERROR.TYPE_TA1, "Invalid ISA11 data " + data);
			}
			break;
		case ISA12:
			if (!Arrays.asList(validISA12).contains(data)) {
				logError(batchId, TA1_ERROR_ISA12, ERROR.TYPE_TA1, "Invalid ISA12 data " + data);
			}
			break;
		case ISA13:
			if (isEmpty(data) || data.length() != 9 || !isNumeric(data)) {
				logError(batchId, TA1_ERROR_ISA13, ERROR.TYPE_TA1,
						"Invalid ISA13 Interchange control number data " + data);
			}

			if (isa13List.contains(data)) {
				logError(batchId, TA1_ERROR_ISA13_DUP, ERROR.TYPE_TA1,
						"Duplicate ISA13 Interchange control number " + data);
			}
			isa13List.add(data);

			break;
		case ISA14:
			if (!equals("0", data)) {
				logError(batchId, TA1_ERROR_ISA14, ERROR.TYPE_TA1, "Invalid Acknowledge Requested(ISA14) " + data);
			}
			break;
		case ISA15:
			if (!Arrays.asList(validISA15).contains(data)) {
				logError(batchId, TA1_ERROR_ISA15, ERROR.TYPE_TA1, "Invalid ISA15 data " + data);
			}
			break;
		case ISA16:
			if (!Arrays.asList(validComponentSeparator).contains(data)) {
				logError(batchId, TA1_ERROR_COMPONENT_SEPARATOR, ERROR.TYPE_TA1, "Invalid ISA16 data " + data);
			}
			break;
		case ISAEnd:
			if (!Arrays.asList(validISAEnd).contains(data)) {
				logError(batchId, TA1_ERROR_ISAEnd, ERROR.TYPE_TA1,
						"The Segment Terminator is invalid - ISA segment must be a fixed length " + data);
			}
			break;
		case IEA01:
			if (isEmpty(data)) {
				logError(batchId, TA1_ERROR_IEA_MISSING, ERROR.TYPE_TA1,
						"Improper (Premature) end-of-file (Transmission) " + data);
			} else if (!equals(data, compareWithData)) {
				logError(batchId, TA1_ERROR_IEA01, ERROR.TYPE_TA1,
						"Invalid Number of Included Group value(IEA01) " + data);
			}
			break;
		case IEA02:
			break;
		case DATA_SEPARATOR:
			if (!Arrays.asList(validDataElementSeparator).contains(data)) {
				logError(batchId, TA1_ERROR_DATA_SEPARATOR, ERROR.TYPE_TA1,
						"Invalid data element separator - ISA segment must be a fixed length " + data);
			}
			break;
		case GS:
			if (isEmpty(data)) {
				logError(batchId, TA1_ERROR_GS_MISSING, ERROR.TYPE_TA1, "No GS segment after ISA " + data);
			}
			break;
		case GS01:
			if (!equals("HC", data)) {
				logError(batchId, AK9_999_ERROR_GS01, ERROR.TYPE_999, "Functional Group Not Supported " + data);
			}
			break;
		case GS06:
			if (!equals(data, compareWithData)) {
				logError(batchId, AK9_999_ERROR_GS06, ERROR.TYPE_999,
						"Group Control Number in the Functional Group Header and trailer Do Not Agree " + data);
			}
			break;
		case GS08:
			if (!Arrays.asList(validSt03ForP).contains(data) && !Arrays.asList(validSt03ForI).contains(data)) {
				logError(batchId, AK9_999_ERROR_GS08, ERROR.TYPE_999, "Functional Group Version Not Supported " + data);
			}
			break;
		case GE:
			if (isEmpty(data)) {
				logError(batchId, AK9_999_ERROR_GE_MISSING, ERROR.TYPE_999, "Functional Group Trailer Missing " + data);
			}
			break;
		case GE01:
			if (!equals(data, compareWithData)) {
				logError(batchId, AK9_999_ERROR_GE01, ERROR.TYPE_999,
						"Number of Included Transaction Sets Does Not Match Actual Count " + data);
			}
			break;
		case ST01:
			if (isEmpty(data) || !"837".equals(data)) {
				logError(batchId, IK5_999_ERROR_ST01, ERROR.TYPE_999, "Transaction Set Not Supported " + data);
			}
			break;
		case ST02:
			if (!equals(data, compareWithData)) {
				logError(batchId, IK5_999_ERROR_ST02, ERROR.TYPE_999,
						"Transaction Set Control Number in Header and Trailer Do Not Match " + data);
			}
			break;
		case ST03:
			if ((CLAIM_TYPE.PRO.name().equals(compareWithData) && !Arrays.asList(validSt03ForP).contains(data))
					|| (CLAIM_TYPE.INS.name().equals(compareWithData) && !Arrays.asList(validSt03ForI).contains(data))
					|| !CLAIM_TYPE.PRO.name().equals(compareWithData)
							&& !CLAIM_TYPE.INS.name().equals(compareWithData)) {
				logError(batchId, IK5_999_ERROR_ST03, ERROR.TYPE_999,
						"Invalid Transaction Set Implementation Convention Reference " + data);
			}
			break;
		case SE:
			if (isEmpty(data)) {
				logError(batchId, IK5_999_ERROR_SE, ERROR.TYPE_999, "Transaction Set Trailer Missing " + data);
			}
			break;
		case SE01:
			if (!equals(data, compareWithData)) {
				logError(batchId, IK5_999_ERROR_SE01, ERROR.TYPE_999,
						"Number of Included Segments Does Not Match Actual Count " + data);
			}
			break;
		default:
			break;
		}

		return true;
	}

	/*
	 * public boolean validate(long batchId, X12_837_ELEMENT elementName, String
	 * data, String compareWithData) throws Exception {
	 * 
	 * switch (elementName) { case IEA01: if(!equals(data, compareWithData)) {
	 * logger.info("Invalid Interchange Sender ID(ISA06) {}",data);
	 * logError(batchId, TA1_ERROR_ISA06, ERROR.TYPE_TA1); } break; }
	 * 
	 * return true; }
	 */

	public void logError(long batchId, String errorCode, ERROR errorType, String errorMessage) throws Exception {

		if (rejectOnValidationError) {

			// new MetadataServicesImpl().insertValidationError(batchId, errorCode,
			// errorType);

			if (stopAtFirstValidationFailure) {
				throw new DebatcherException(errorMessage, errorCode, errorType, DebatcherException.ERROR_LEVEL.Batch,
						batchId, DebatcherException.ERROR_OR_EXCEPTION.Exception);
			}
		}

	}

	public boolean isValidSenderId(String senderId, long batchId) throws Exception {
		// TODO: replace this if it makes sense
		/*
		 * MetadataServicesImpl services = new MetadataServicesImpl();
		 * if(services.selectIdMMPBySenderId(senderId)==-1 || isEmpty(senderId)) {
		 * //services.updateSubmissionMmpId(batchId, null);
		 * services.updateSubmissionBatch(batchId, senderId); return false; }
		 */
		return true;
	}

	public boolean isEmpty(String data) {
		if (data == null || data.trim().length() == 0) {
			return true;
		}
		return false;
	}

	public boolean equals(String str1, String str2) {
		if (str1 == null || str2 == null) {
			return false;
		}
		if (!str1.trim().equals(str2.trim())) {
			return false;
		}
		return true;
	}

	public boolean isNumeric(String s) {

		if (s == null)
			return false;

		return s.matches("\\d+");
	}

}
