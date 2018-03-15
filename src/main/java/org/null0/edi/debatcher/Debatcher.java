package org.null0.edi.debatcher;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.lang3.StringUtils;
import org.null0.edi.debatcher.DebatcherException.ERROR_LEVEL;
import org.null0.edi.debatcher.DebatcherException.ERROR_OR_EXCEPTION;
import org.null0.edi.debatcher.EncounterEdiValidator.CLAIM_TYPE;
import org.null0.edi.debatcher.EncounterEdiValidator.ERROR;
import org.null0.edi.debatcher.EncounterEdiValidator.X12_837_ELEMENT;
import org.null0.edi.debatcher.MetadataLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Debatcher {

	private static final Logger logger = LoggerFactory.getLogger(Debatcher.class);
	private static final int hlBillProvLevelCode = 20;
	private static final int hlSubscriberLevelCode = 22;
	private static final String NEW_LINE = System.getProperty("line.separator");
	private CLAIM_TYPE claimType;
	private InputStream inputStream;
	private String[] segments = null;
	private int segmentIndex = 0;
	private boolean fileReadCompleted = false;
	private boolean isEndOfFile = false;
	private boolean isIeaFound = false;

	private enum EdiWrapStyle {
		Unknown, Unwrapped, Unix, Windows
	};

	private EdiWrapStyle ediWrap = EdiWrapStyle.Unknown;

	private boolean lastDataChunkReturned = false;
	private boolean initialFileValidation = false;
	private boolean checkForRefD9 = false;
	private String lastPartialSegment = "";
	private String fieldDlm;
	private String segmentDlm;
	private String isaSegment;
	private String gsSegment;
	private StringBuffer headerBuffer = new StringBuffer();
	private StringBuffer claimBuffer = new StringBuffer();
	private String st02;
	private String gs06;
	private String isa13;
	private String clm01;
	private String clm05;
	private String url;
	private int isaCnt = 0;
	private int gsCnt;
	private int stCnt;
	private int hlCnt;
	private int claimCnt = 0;
	private int segmentCnt = 0;
	private long batchIdMetadata;
	private long isaIdMetadata;
	private long gsIdMetadata;
	private long stIdMetadata;
	private long hlIdMetadata;
	private String transactionId;
	private Stack<HierarchicalLevel> hlStack = null;
	private Set<String> segmentsBeforeRefD9ForP;
	private Set<String> segmentsBeforeRefD9ForI;
	private String segment = null;
	private MetadataLogger metadataLogger;
	private EncounterEdiValidator ediValidator;
	private String outputLocation = "/home/developer/lnxshare/output/";
	private Map<String, Long> claimIdMap = new HashMap<String, Long>();
	private boolean transactionIdUpdate = true;

	public Debatcher(MetadataLogger metadataLogger) {
		this.metadataLogger = metadataLogger;
		this.ediValidator = new EncounterEdiValidatorDefault(false); // default implementation for the edi validator
	}

	public Debatcher(MetadataLogger metadataLogger, EncounterEdiValidator ediValidator) {
		this.metadataLogger = metadataLogger;
		this.ediValidator = ediValidator;
	}

	private void setOutputLocation() throws Exception {
		String baseDir = Config.getOutputDir().toString();
		if (baseDir != null) {
			outputLocation = baseDir + "output/";
		}
	}

	public void debatch(String transactionId, InputStream is) throws Exception {
		logger.info("debatching started..." + transactionId);
		this.transactionId = transactionId;
		this.inputStream = is;

		setOutputLocation();

		batchIdMetadata = metadataLogger.logBatchSubmissionData(transactionId); // Logging the batch submission data to
																				// the metadata store

		readInterchangeControls();
		is.close();
	}

	public Map<String, Long> debatch(String transactionId, long batchIdMetadata, InputStream is, String outputLocation,
			boolean transactionIdUpdate) throws Exception {
		logger.info("debatching started..." + transactionId);
		this.transactionId = transactionId;
		this.inputStream = is;
		this.batchIdMetadata = batchIdMetadata;
		this.outputLocation = outputLocation;
		this.transactionIdUpdate = transactionIdUpdate;

		readInterchangeControls();
		is.close();

		return claimIdMap;
	}

	private void readInterchangeControls() throws Exception {
		while (!fileReadCompleted) {
			getNextSegment();
			if (NEW_LINE.equals(segment)) {
				continue;
			}
			if (segment == null || segment.equals("\r\n")) {
				throw new DebatcherException("Invalid Control Structure", EncounterEdiValidatorDefault.TA1_ERROR_ISAIEA,
						ERROR.TYPE_TA1, ERROR_LEVEL.Batch, batchIdMetadata,
						DebatcherException.ERROR_OR_EXCEPTION.Exception);
			}
			isaSegment = segment.replaceAll("\\r|\\n", "");
			ediValidator.validate(batchIdMetadata, X12_837_ELEMENT.DATA_SEPARATOR, fieldDlm, null);
			isa13 = readField(segment, 13);
			isaCnt++;

			isaIdMetadata = metadataLogger.logIsaData(batchIdMetadata, isa13, isaSegment);
			String isa06 = readField(segment, 6);
			if (transactionIdUpdate) {
				transactionId = isa06.trim() + transactionId;
				transactionIdUpdate = false;
			}
			ediValidator.validate(batchIdMetadata, X12_837_ELEMENT.ISA06, isa06, null);
			ediValidator.validate(batchIdMetadata, X12_837_ELEMENT.ISA07, readField(segment, 7), null);
			ediValidator.validate(batchIdMetadata, X12_837_ELEMENT.ISA08, readField(segment, 8), null);
			ediValidator.validate(batchIdMetadata, X12_837_ELEMENT.ISA11, readField(segment, 11), null);
			ediValidator.validate(batchIdMetadata, X12_837_ELEMENT.ISA12, readField(segment, 12), null);
			ediValidator.validate(batchIdMetadata, X12_837_ELEMENT.ISA13, isa13, null);
			ediValidator.validate(batchIdMetadata, X12_837_ELEMENT.ISA14, readField(segment, 14), null);
			ediValidator.validate(batchIdMetadata, X12_837_ELEMENT.ISA15, readField(segment, 15), null);
			ediValidator.validate(batchIdMetadata, X12_837_ELEMENT.ISA16, readField(segment, 16), null);
			ediValidator.validate(batchIdMetadata, X12_837_ELEMENT.ISAEnd, segmentDlm, null);

			gsCnt = 0;

			readFuntionGroups();

			metadataLogger.updateIsaData(isaIdMetadata, gsCnt);

			if (!"IEA".equals(readField(segment, 0))) {
				// throw new Exception("Missing IEA segment");
				ediValidator.validate(batchIdMetadata, X12_837_ELEMENT.IEA01, null, null);
			}

			ediValidator.validate(batchIdMetadata, X12_837_ELEMENT.IEA01, readField(segment, 1), String.valueOf(gsCnt));

			String iea02 = readField(segment, 2);
			if (!isa13.equals(iea02)) {
				logger.error("ISA13 {} & IEA02 {} don't match", isa13, iea02);
				throw new DebatcherException("ISA13 & IEA02 don't match", EncounterEdiValidatorDefault.TA1_ERROR_ISA13,
						ERROR.TYPE_TA1, ERROR_LEVEL.Batch, batchIdMetadata, ERROR_OR_EXCEPTION.Exception);
			}
		}

		metadataLogger.updateBatchSubmissionData(batchIdMetadata, isaCnt);
	}

	private void readFuntionGroups() throws Exception {
		boolean readGsSegment = false;
		while (true) {
			getNextSegment();

			if (!readGsSegment) {
				if (!"GS".equals(readField(segment, 0))) {
					ediValidator.validate(batchIdMetadata, X12_837_ELEMENT.GS, null, null);
				}
				readGsSegment = true;
			}

			if ("IEA".equals(readField(segment, 0)) || "".equals(segment)) {
				return;
			}
			gsCnt++;

			gs06 = readField(segment, 6);

			gsSegment = segment;

			gsIdMetadata = metadataLogger.logGsData(isaIdMetadata, gs06, gsSegment);

			ediValidator.validate(batchIdMetadata, X12_837_ELEMENT.GS01, readField(gsSegment, 1), null);
			ediValidator.validate(batchIdMetadata, X12_837_ELEMENT.GS08, readField(gsSegment, 8), null);

			stCnt = 0;
			getNextSegment();
			readTransactionSets();
			if (!"GE".equals(readField(segment, 0))) {
				// throw new Exception("Missing GE segment");
				ediValidator.validate(batchIdMetadata, X12_837_ELEMENT.GE, null, null);
			}
			String ge02 = readField(segment, 2);
			if (!gs06.equals(ge02)) {
				logger.error("GS06 {} & GE02 {} don't match", gs06, ge02);
				// throw new Exception("GS06 & GE02 don't match");
				ediValidator.validate(batchIdMetadata, X12_837_ELEMENT.GS06, gs06, ge02);
			}
			ediValidator.validate(batchIdMetadata, X12_837_ELEMENT.GE01, readField(segment, 1), String.valueOf(stCnt));

			metadataLogger.updateGsData(gsIdMetadata, stCnt);
		}
	}

	private void readTransactionSets() throws Exception {
		while (true) {
			if (!"ST".equals(readField(segment, 0))) {
				throw new DebatcherException("Missing ST segment", EncounterEdiValidatorDefault.IK3_999_ERROR_MISS_SEG,
						ERROR.TYPE_999, ERROR_LEVEL.Batch, batchIdMetadata, ERROR_OR_EXCEPTION.Exception);
			}
			stCnt++;
			segmentCnt = 1;

			hlStack = new Stack<HierarchicalLevel>(); // Reset the HL Stack for each transaction set.
			headerBuffer = new StringBuffer(); // Reset the header buffer.

			String st01 = readField(segment, 1);

			ediValidator.validate(batchIdMetadata, X12_837_ELEMENT.ST01, st01, null);

			st02 = readField(segment, 2);
			claimType = getClaimType(segment);
			ediValidator.validate(batchIdMetadata, X12_837_ELEMENT.ST03, readField(segment, 3), claimType.name());

			hlCnt = 0;
			stIdMetadata = metadataLogger.logStData(gsIdMetadata, st02, segment.trim());

			initializeObjects();

			if ("837".equals(st01) && CLAIM_TYPE.OTH != claimType) {
				readHeader();
				readHierarchicalLevels();
			} else {
				// Not a valid claim. For implementation revisit this section later.
			}

			if (isIeaFound || readField(segment, 0).equals("SE")) {
				ediValidator.validate(batchIdMetadata, X12_837_ELEMENT.SE, readField(segment, 0), null);
				ediValidator.validate(batchIdMetadata, X12_837_ELEMENT.SE01, readField(segment, 1),
						String.valueOf(segmentCnt));
			} else { // If the SE segment is missing but the IEA segment is also missing, then treat
						// as missing IEA.
				ediValidator.validate(batchIdMetadata, X12_837_ELEMENT.IEA01, null, null);
			}

			String se02 = readField(segment, 2);
			if (!st02.equals(se02)) {
				logger.error("ST02 {} & SE02 {} don't match", st02, se02);
				// throw new Exception ("ST02 & SE02 don't match");
				ediValidator.validate(batchIdMetadata, X12_837_ELEMENT.ST02, st02, se02);
			}

			metadataLogger.updateStData(stIdMetadata, hlCnt);

			getNextSegment();

			if ("GE".equals(readField(segment, 0)) || !"ST".equals(readField(segment, 0))) {
				return;
			}
		}
	}

	private void readHeader() throws Exception {
		while (true) {
			headerBuffer.append(segment).append(segmentDlm);
			getNextSegment();
			segmentCnt++;
			if ("HL".equals(readField(segment, 0))) {
				return;
			}
		}
	}

	private void readHierarchicalLevels() throws Exception {

		// while(true)
		while (!isEndOfFile) {
			if ("HL".equals(readField(segment, 0))) {
				hlCnt++;

				HierarchicalLevel hl = new HierarchicalLevel();
				hl.setId(Integer.parseInt(readField(segment, 1)));
				hl.setLevelCode(Integer.parseInt(readField(segment, 3)));
				hl.setChildcode(Integer.parseInt(readField(segment, 4)));

				hlIdMetadata = metadataLogger.logHlData(stIdMetadata, hl.getId(), hl.getLevelCode(),
						readField(segment, 2));

				if (hl.getLevelCode() == hlBillProvLevelCode) {
					hl.setId(1);
					hl.setParentId(0);
				} else if (hl.getLevelCode() == hlSubscriberLevelCode) {
					hl.setId(2);
					hl.setParentId(1);
				} else {
					hl.setId(3);
					hl.setParentId(2);
				}
				segment = "HL" + fieldDlm + hl.getId() + fieldDlm + (hl.getParentId() == 0 ? "" : hl.getParentId())
						+ fieldDlm + hl.getLevelCode() + fieldDlm + hl.getChildcode();

				while (true) {
					if (hlStack.size() > 0) {
						if (hl.getLevelCode() <= hlStack.peek().getLevelCode())
							hlStack.pop();
						else
							break;
					} else {
						break;
					}
				}

				hlStack.push(hl);
			}

			hlStack.peek().getHlDataBuffer().append(segment).append(segmentDlm);

			getNextSegment();
			segmentCnt++;

			if ("CLM".equals(readField(segment, 0))) {
				readClaim();
				return;
			}
		}

		if (isIeaFound) {
			// throw new DebatcherException("Missing CLM segment",
			// EncounterEdiValidatorDefault.IK3_999_ERROR_MISS_SEG, ERROR.TYPE_999,
			// ERROR_LEVEL.Batch, batchIdMetadata, ERROR_OR_EXCEPTION.Exception);
			ediValidator.logError(batchIdMetadata, EncounterEdiValidatorDefault.IK3_999_ERROR_MISS_SEG, ERROR.TYPE_999,
					"Missing CLM segment");
		} else {
			ediValidator.validate(batchIdMetadata, X12_837_ELEMENT.IEA01, null, null);
		}
	}

	private void readClaim() throws Exception {
		claimCnt++;

		boolean addedRefD9 = false;
		boolean readClmSegment = false;
		while (true) {
			if (!addedRefD9) {
				if (isAtRefD9()) {
					StringBuffer refD9 = new StringBuffer("REF").append(fieldDlm).append("D9").append(fieldDlm)
							.append(transactionId).append("_").append(String.format("%05d", claimCnt));
					claimBuffer.append(getEdiLineWrap()).append(refD9).append(segmentDlm); // *** WRITE ***
					addedRefD9 = true;
				}
			}

			if (segment != null) {
				claimBuffer.append(segment).append(segmentDlm);
				if (!readClmSegment) {
					clm01 = readField(segment, 1);
					if (clm01 == null || clm01.isEmpty()) {
						ediValidator.logError(batchIdMetadata,
								EncounterEdiValidatorDefault.IK3_999_ERROR_MISS_DATA_ELEMENT, ERROR.TYPE_999,
								"Missing CLM01 value");
					}
					clm05 = readField(segment, 5);
					readClmSegment = true;
				}
			}

			getNextSegment();
			segmentCnt++;

			if ("".equals(segment)) {
				return;
			}

			if ("CLM".equals(readField(segment, 0)) || "SE".equals(readField(segment, 0))
					|| "HL".equals(readField(segment, 0))) {
				writeClaim();

				if ("HL".equals(readField(segment, 0))) {
					readHierarchicalLevels();
				}

				if ("CLM".equals(readField(segment, 0))) {
					readClaim();
				}

				return;
			}
		}
	}

	private String getEdiLineWrap() {
		String eol;
		switch (this.ediWrap) {
		case Windows:
			eol = "\r\n";
			break;
		case Unix:
			eol = "\n";
			break;
		default:
			eol = "";
			break;
		}
		return eol;
	}

	private boolean isAtRefD9() {
		if (!checkForRefD9) {
			checkForRefD9 = canCheckForRefD9();
		}

		if (checkForRefD9) {
			String firstElement = readField(segment, 0);
			String secondElement = readField(segment, 1);

			/*
			 * if("HI".equals(firstElement)) { //This check is not necessary but just to
			 * make sure REF*D9 is added before the HI segment. //If the control comes this
			 * far then segmentsBeforeRefD9ForP and segmentsBeforeRefD9ForI need to be
			 * updated with the correct segments. return true; }
			 */

			if ("REF".equals(firstElement) && "D9".equals(secondElement)) {
				segment = null; // Ignore the existing REF*D9
				return true; // The current segment is REF*D9.
			} else {
				if ("REF".equals(firstElement)) {
					firstElement = firstElement + fieldDlm + secondElement;
				}

				if (CLAIM_TYPE.INS == claimType) // Institutional
				{
					if (!segmentsBeforeRefD9ForI.contains(firstElement)) {
						return true; // REF*D9 needs to be added before the current segment
					}
				} else // Professional
				{
					if (!segmentsBeforeRefD9ForP.contains(firstElement)) {
						return true; // REF*D9 needs to be added before the current segment
					}
				}
			}

		}

		return false;
	}

	private boolean canCheckForRefD9() {
		if (CLAIM_TYPE.INS == claimType) {
			if ("CL1".equals(readField(segment, 0))) {
				// checkForRefD9 = true;
				return true;
			}
		} else if (CLAIM_TYPE.PRO == claimType) {
			if ("CLM".equals(readField(segment, 0))) {
				// checkForRefD9 = true;
				return true;
			}
		}
		return false;
	}

	private void writeClaim() throws Exception {
		String splitEncounterName = transactionId + "_" + String.format("%05d", claimCnt) + ".edi.txt";
		url = outputLocation + splitEncounterName;
		File statText = new File(url);
		FileOutputStream os = new FileOutputStream(statText);
		OutputStreamWriter osw = new OutputStreamWriter(os);
		Writer w = new BufferedWriter(osw);
		w.write(isaSegment);
		w.write(segmentDlm);
		w.write(gsSegment);
		w.write(segmentDlm);
		w.write(headerBuffer.toString());

		Iterator<HierarchicalLevel> iter = hlStack.iterator();
		int hlSegmentsCount = 0;
		while (iter.hasNext()) {
			String hlString = iter.next().getHlDataBuffer().toString();
			hlSegmentsCount += getSegmentCount(hlString);
			w.write(hlString);
		}

		w.write(claimBuffer.toString());

		int headerSegmentsCnt = getSegmentCount(headerBuffer.toString());
		int clmSegmentsCnt = getSegmentCount(claimBuffer.toString());
		int lxCnt = getLxCount(claimBuffer.toString());

		String eol = getEdiLineWrap();
		w.write(eol + "SE" + fieldDlm + (headerSegmentsCnt + hlSegmentsCount + clmSegmentsCnt + 1) + fieldDlm + st02
				+ segmentDlm);
		w.write(eol + "GE" + fieldDlm + 1 + fieldDlm + gs06 + segmentDlm);
		w.write(eol + "IEA" + fieldDlm + 1 + fieldDlm + isa13 + segmentDlm);

		w.close();
		osw.close();
		os.close();

		long encounterId = metadataLogger.logEncounter(batchIdMetadata, hlIdMetadata, clm01, clm05, lxCnt, null,
				splitEncounterName);
		claimIdMap.put(splitEncounterName, encounterId);

		claimBuffer = new StringBuffer();
		checkForRefD9 = false;
	}

	private void getNextSegment() throws Exception {

		if (segments == null) {
			getDataChunk();
		}
		segment = segments[segmentIndex];
		if (readField(segment, 0).equals("IEA")) {
			isIeaFound = true;
		}
		segmentIndex++;
		if (segments.length == segmentIndex) {
			segmentIndex = 0;
			segments = null;
			if (lastDataChunkReturned) {
				fileReadCompleted = true;
			}
		}

		// logger.debug("Segment: "+segment);
	}

	private void getDataChunk() throws Exception {
		if (isEndOfFile) {
			logger.error("Unexpected end of file while attempting to get next segment");
			throw new Exception("Unexpected end of file while attempting to read more segments");
		}

		byte[] dataChunk = new byte[1024];
		int bytesRead = 0;
		String data = "";
		boolean lastDataChunkEndsWithSgmtDlm = false;

		while ((bytesRead = inputStream.read(dataChunk)) != -1) {

			// If first time reading data, determine EDI Wrapping Style
			if (this.ediWrap == EdiWrapStyle.Unknown) {
				this.ediWrap = EdiWrapStyle.Unwrapped;
				for (int i = 0; i < bytesRead - 1; i++) {
					if (dataChunk[i] == 0x0D && dataChunk[i + 1] == 0x0A) {
						this.ediWrap = EdiWrapStyle.Windows; // \r\n
						break;
					} else if (dataChunk[i] == 0x0A) {
						this.ediWrap = EdiWrapStyle.Unix; // \n
						break;
					}
				}
			}

			// logger.debug("lastPartialSegment: "+lastPartialSegment);
			data = this.lastPartialSegment + new String(dataChunk, 0, bytesRead);
			// logger.debug("data----------------->"+data);
			if (bytesRead < 1024) {
				this.lastDataChunkReturned = true;
			}
			break;
		}
		isEndOfFile = (bytesRead == -1);

		if (!this.initialFileValidation) {
			if (!"ISA".equals(data.substring(0, 3))) {
				throw new DebatcherException("Not a valid Interchange Segment",
						EncounterEdiValidatorDefault.TA1_ERROR_ISAIEA, ERROR.TYPE_TA1, ERROR_LEVEL.Batch, batchIdMetadata,
						ERROR_OR_EXCEPTION.Exception);

			}
			this.initialFileValidation = true;
			fieldDlm = data.substring(103, 104);
			segmentDlm = data.substring(105, 106);
			if (segmentDlm.equals("\n") || segmentDlm.equals("\r")) {
				this.ediWrap = EdiWrapStyle.Unwrapped; // If segment terminator is a line-wrap char itself, then no
														// wrapping is necessary
			}
			logger.debug("field and segment delimiter are {} & {}", fieldDlm, segmentDlm);
		}

		if (data.endsWith(segmentDlm)) {
			lastDataChunkEndsWithSgmtDlm = true;
		}

		segments = data.split(java.util.regex.Pattern.quote(segmentDlm));
		lastPartialSegment = segments[segments.length - 1];

		if (lastDataChunkEndsWithSgmtDlm) {
			lastPartialSegment = "";
		}

		// if(!lastDataChunkReturned){
		if (!lastDataChunkReturned && !lastDataChunkEndsWithSgmtDlm) {
			// segments = ArrayUtils.removeElement(segments, lastPartialSegment);
			segments = Arrays.copyOf(segments, segments.length - 1);
		}
	}

	private String readField(String segment, int position) {
		String[] data = segment.split("\\" + fieldDlm);
		if (data.length > position) {
			String field = data[position];
			return field.replaceAll("\\r|\\n", "");
		}
		return null;
	}

	private CLAIM_TYPE getClaimType(String segment) {
		if (readField(segment, 3).startsWith("005010X222"))
			return CLAIM_TYPE.PRO; // Professional
		else if (readField(segment, 3).startsWith("005010X223"))
			return CLAIM_TYPE.INS; // Institutional

		return CLAIM_TYPE.OTH; // Other
	}

	private int getSegmentCount(String str) {
		return StringUtils.countMatches(str, segmentDlm);
	}

	private int getLxCount(String str) {
		return StringUtils.countMatches(str, "LX" + fieldDlm);
	}

	private void initializeObjects() {
		if (CLAIM_TYPE.PRO == claimType) {
			segmentsBeforeRefD9ForP = new HashSet<String>(
					Arrays.asList(new String[] { "CLM", "DTP", "PWK", "CN1", "AMT", "REF" + fieldDlm + "4N",
							"REF" + fieldDlm + "F5", "REF" + fieldDlm + "EW", "REF" + fieldDlm + "9F",
							"REF" + fieldDlm + "G1", "REF" + fieldDlm + "F8", "REF" + fieldDlm + "X4" }));
		} else if (CLAIM_TYPE.INS == claimType) {
			segmentsBeforeRefD9ForI = new HashSet<String>(Arrays.asList(new String[] { "CL1", "PWK", "CN1", "AMT",
					"REF" + fieldDlm + "4N", "REF" + fieldDlm + "9F", "REF" + fieldDlm + "G1", "REF" + fieldDlm + "F8",
					"REF" + fieldDlm + "9A", "REF" + fieldDlm + "9C", "REF" + fieldDlm + "LX" }));
		}
	}
}
