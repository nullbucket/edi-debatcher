package org.null0.x12.debatcher;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.null0.x12.debatcher.Validator.Error;
import org.null0.x12.debatcher.Validator.X12element;
import org.null0.x12.debatcher.DefaultValidator.ClaimType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Debatcher {
	private static final Logger logger = LoggerFactory.getLogger(Debatcher.class);
	private static final int hlBillProvLevelCode = 20; // Specific to claims (837). Decouple.
	private static final int hlSubscriberLevelCode = 22; // Specific to claims (837). Decouple.	
	private final Config config; // Class invariant, injected by greedy CTOR
	private final Validator ediValidator; // Class invariant, injected by greedy CTOR
	private final Metadata metadataLogger; // Class invariant, injected by greedy CTOR
	private Delimiters delimiters;
	private long batchIdMetadata; // debatch() input param
	private boolean checkForRefD9;
	private StringBuilder claimBuffer;
	private int claimCnt; // TODO: specific to claims (837). Decouple.
	private Map<String, Long> claimIdMap = new HashMap<>(); // debatch() output param
	private ClaimType claimType; // TODO: specific to claims (837). Decouple.
	private String clm01; // TODO: specific to claims (873). Decouple.
	private String clm05; // TODO: specific to claims (873). Decouple.	
	private String gs06;
	private int gsCnt;
	private long gsIdMetadata;
	private String gsSegment;
	private StringBuilder headerBuffer;
	//private int hlCnt;
	private long hlIdMetadata;
	private Deque<HierarchicalLevel> hlStack;
	private String isa13;
	private int isaCnt;
	private long isaIdMetadata;
	private String isaSegment;
	private boolean needToUpdateTransactionId; // reset for each call to debatch()
	private int segmentCnt;
	private SegmentReader segmentReader; // reset for each call to debatch()
	private Set<String> segmentsBeforeRefD9ForI; // TODO: specific to claims (873). Decouple. 
	private Set<String> segmentsBeforeRefD9ForP; // TODO: specific to claims (873). Decouple. 
	private String st02;
	private int stCnt;
	private long stIdMetadata;
	private String transactionId; // debatch() input param

	public Debatcher(Config config, Validator ediValidator, Metadata metadataLogger) {
		this.config = config;
		this.ediValidator = ediValidator;
		this.metadataLogger = metadataLogger;
		this.delimiters = null; // to be determined later by each ISA segment
		headerBuffer = new StringBuilder();
		hlStack = new ArrayDeque<>();
		claimBuffer = new StringBuilder();
	}

	public void debatch(String transactionId, InputStream inputStream) throws Exception {
		debatch(transactionId, -1, inputStream);
	}

	public Map<String, Long> debatch(String transactionId, long batchIdMetadata, InputStream inputStream) throws Exception {
		logger.info("debatching started... {}", transactionId);

		// Input parameters
		if (inputStream == null) {
			final String msg = "Null input stream, nothing to do!";
			logger.error(msg);
			throw new NullPointerException(msg);
		}
		this.transactionId = transactionId;
		this.needToUpdateTransactionId = config.willUpdateTransactionId();
		this.batchIdMetadata = batchIdMetadata < 0 ? this.metadataLogger.logBatchSubmissionData(transactionId) : batchIdMetadata;

		// Initialize debatcher session state
		this.segmentReader = new SegmentReader(this.config, inputStream, this.batchIdMetadata);
		checkForRefD9 = false;
		isaCnt = 0;
		claimCnt = 0;
		segmentCnt = 0;

		// Let's go down the rabbit hole...
		readInterchangeControls();

		// Clean up
		inputStream.close(); // TODO: Why are we closing a stream we do not own? Shouldn't that be the responsibility of the caller that passed it in?
		headerBuffer.setLength(0);
		claimBuffer.setLength(0);

		return claimIdMap;
	}

	private boolean canCheckForRefD9() {
		return ClaimType.INS == claimType && "CL1".equals(segmentReader.field(0))
				|| ClaimType.PRO == claimType && "CLM".equals(segmentReader.field(0));
	}

	private ClaimType getClaimType() {
		if (segmentReader.field(3).startsWith("005010X222")) {
			return ClaimType.PRO; // Professional
		} else if (segmentReader.field(3).startsWith("005010X223")) {
			return ClaimType.INS; // Institutional
		}
		return ClaimType.OTH; // Other
	}

	private int getLxCount(String str) {
		return StringUtils.countMatches(str, "LX" + delimiters.getField());
	}

	private int getSegmentCount(String str) {
		return StringUtils.countMatches(str, delimiters.getSegmentTerminator());
	}

	private HashSet<String> idPattern(String pattern, char fieldDelimiter) {
		if (fieldDelimiter != '*') {
			pattern = pattern.replace(fieldDelimiter, '*');
		}
		return new HashSet<>(Arrays.asList(pattern.split(",")));
	}

	private void initRefD9idPattern() {
		if (ClaimType.PRO == claimType) {
			segmentsBeforeRefD9ForP = idPattern("CLM,DTP,PWK,CN1,AMT,REF*4N,REF*F5,REF*EW,REF*9F,REF*G1,REF*F8,REF*X4", delimiters.getField());
		} else if (ClaimType.INS == claimType) {
			segmentsBeforeRefD9ForI = idPattern("CL1,PWK,CN1,AMT,REF*4N,REF*9F,REF*G1,REF*F8,REF*9A,REF*9C,REF*LX", delimiters.getField());
		}
	}

	private boolean isAtRefD9() {
		if (!checkForRefD9) {
			checkForRefD9 = canCheckForRefD9();
		}

		if (checkForRefD9) {
			String firstElement = segmentReader.field(0);
			String secondElement = segmentReader.field(1);

			// TODO: is this dead code?
			/* if("HI".equals(firstElement)) { //This check is not necessary but just to
			 * make sure REF*D9 is added before the HI segment. //If the control comes this
			 * far then segmentsBeforeRefD9ForP and segmentsBeforeRefD9ForI need to be
			 * updated with the correct segments. return true; }
			 */

			if ("REF".equals(firstElement) && "D9".equals(secondElement)) {
				segmentReader.setCurrent(null); // Ignore the existing REF*D9
				return true; // The current segment is REF*D9.
			} else {
				if ("REF".equals(firstElement)) {
					firstElement = firstElement + delimiters.getField() + secondElement;
				}
				return ClaimType.INS == claimType && !segmentsBeforeRefD9ForI.contains(firstElement)
						|| ClaimType.PRO == claimType && !segmentsBeforeRefD9ForP.contains(firstElement);
			}
		}
		return false;
	}

	private void readClaim() throws Exception {
		claimCnt++;

		boolean addedRefD9 = false;
		boolean readClmSegment = false;
		while (true) {
			if (!addedRefD9) {
				if (isAtRefD9()) {
					StringBuilder refD9 = new StringBuilder("REF").append(delimiters.getField())
							.append("D9")
							.append(delimiters.getField())
							.append(transactionId)
							.append("_")
							.append(String.format("%05d", claimCnt));
					claimBuffer.append(delimiters.getEOL())
							.append(refD9)
							.append(delimiters.getSegmentTerminator()); // will be used when we *** WRITE ***
					addedRefD9 = true;
				}
			}

			if (segmentReader.current() != null) {
				claimBuffer.append(segmentReader.current()).append(delimiters.getSegmentTerminator());
				if (!readClmSegment) {
					clm01 = segmentReader.field(1);
					if (clm01 == null || clm01.isEmpty()) {
						ediValidator.logError(
								batchIdMetadata,
								Validator.IK3_999_ERROR_MISS_DATA_ELEMENT, Error.TYPE_999,
								"Missing CLM01 value");
					}
					clm05 = segmentReader.field(5);
					readClmSegment = true;
				}
			}

			segmentReader.next();
			segmentCnt++;

			// TODO: this is questionable. Why are we exiting on an empty line?
			if ("".equals(segmentReader.current())) {
				return; // exit recursion
			}

			if ("CLM".equals(segmentReader.field(0)) || "SE".equals(segmentReader.field(0)) || "HL".equals(segmentReader.field(0))) {
				// *** WRITE ***
				writeClaim(); // output, possible end to recursion
				
				if ("HL".equals(segmentReader.field(0))) {
					// Let's go down the rabbit hole... (indirect recursion)
					readHierarchicalLevels();
				}
				if ("CLM".equals(segmentReader.field(0))) {
					// Let's go down the rabbit hole... (direct recursion)
					readClaim();
				}
				return; // exit recursion
			}
		}
	}

	private void readFunctionGroups() throws Exception {
		boolean readGsSegment = false;
		while (true) {
			String segment = segmentReader.next();

			if (!readGsSegment) {
				if (!"GS".equals(segmentReader.field(0))) {
					ediValidator.validate(batchIdMetadata, X12element.GS, null, null);
				}
				readGsSegment = true;
			}

			if ("IEA".equals(segmentReader.field(0)) || "".equals(segment)) {
				return;
			}
			gsCnt++;

			gs06 = segmentReader.field(6);

			gsSegment = segment;
			gsIdMetadata = metadataLogger.logGsData(isaIdMetadata, gs06, gsSegment);
			ediValidator.validate(batchIdMetadata, X12element.GS01, segmentReader.field(1), null);
			ediValidator.validate(batchIdMetadata, X12element.GS08, segmentReader.field(8), null);

			stCnt = 0;
			segmentReader.next();

			// Let's go down the rabbit hole...
			readTransactionSets();

			if (!"GE".equals(segmentReader.field(0))) {
				// throw new Exception("Missing GE segment");
				ediValidator.validate(batchIdMetadata, X12element.GE, null, null);
			}
			String ge02 = segmentReader.field(2);
			if (!gs06.equals(ge02)) {
				logger.error("GS06 {} & GE02 {} don't match", gs06, ge02);
				ediValidator.validate(batchIdMetadata, X12element.GS06, gs06, ge02);
			}
			ediValidator.validate(batchIdMetadata, X12element.GE01, segmentReader.field(1), String.valueOf(stCnt));
			metadataLogger.updateGsData(gsIdMetadata, stCnt);
		}
	}

	private void readHeader() throws Exception {
		while (true) {
			headerBuffer.append(segmentReader.current()).append(delimiters.getSegmentTerminator());
			segmentReader.next();
			segmentCnt++;
			if ("HL".equals(segmentReader.field(0))) {
				return;
			}
		}
	}

	private int readHierarchicalLevels() throws Exception {
		int hlCnt = 0;
		while (!segmentReader.isEOF()) {
			String segment = segmentReader.current();
			if ("HL".equals(segmentReader.field(0))) {
				hlCnt++;

				HierarchicalLevel hl = new HierarchicalLevel();
				hl.setId(Integer.parseInt(segmentReader.field(1)));
				hl.setLevelCode(Integer.parseInt(segmentReader.field(3)));
				hl.setChildcode(Integer.parseInt(segmentReader.field(4)));

				hlIdMetadata = metadataLogger.logHlData(stIdMetadata, hl.getId(), hl.getLevelCode(), segmentReader.field(2));

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
				segment = "HL" + delimiters.getField() + hl.getId() + delimiters.getField() + (hl.getParentId() == 0 ? "" : hl.getParentId())
						+ delimiters.getField() + hl.getLevelCode() + delimiters.getField() + hl.getChildcode();

				while (true) {
					if (!hlStack.isEmpty()) {
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

			hlStack.peek().getHlDataBuffer().append(segment).append(delimiters.getSegmentTerminator());

			segmentReader.next();
			segmentCnt++;

			if ("CLM".equals(segmentReader.field(0))) {
				// Let's go down the rabbit hole...
				readClaim();
				return hlCnt;
			}
		}
		logger.warn("Didn't expect to get here, errors may follow...");
		return hlCnt;
	}

	private void readInterchangeControls() throws Exception {	
		while (!segmentReader.fileReadCompleted()) {
			String segment = segmentReader.next();
			delimiters = segmentReader.getDelimiters(); // new delimiters for each ISA-IEA set

			if (StringUtils.isAllBlank(segment)) {
				logger.warn("Ignoring unexpected whitespace.");
				continue;
			}

			isaSegment = segment.replaceAll("\\r|\\n", "");
			ediValidator.validate(batchIdMetadata, X12element.DATA_SEPARATOR, String.valueOf(delimiters.getField()), null);
			isa13 = segmentReader.field(13);
			isaCnt++;

			isaIdMetadata = metadataLogger.logIsaData(batchIdMetadata, isa13, isaSegment);
			String isa06 = segmentReader.field(6);
			if (needToUpdateTransactionId) {
				transactionId = isa06.trim() + transactionId;
				needToUpdateTransactionId = false;
			}
			ediValidator.validate(batchIdMetadata, X12element.ISA06, isa06, null);
			ediValidator.validate(batchIdMetadata, X12element.ISA07, segmentReader.field(7), null);
			ediValidator.validate(batchIdMetadata, X12element.ISA08, segmentReader.field(8), null);
			ediValidator.validate(batchIdMetadata, X12element.ISA11, segmentReader.field(11), null);
			ediValidator.validate(batchIdMetadata, X12element.ISA12, segmentReader.field(12), null);
			ediValidator.validate(batchIdMetadata, X12element.ISA13, isa13, null);
			ediValidator.validate(batchIdMetadata, X12element.ISA14, segmentReader.field(14), null);
			ediValidator.validate(batchIdMetadata, X12element.ISA15, segmentReader.field(15), null);
			ediValidator.validate(batchIdMetadata, X12element.ISA16, segmentReader.field(16), null);
			ediValidator.validate(batchIdMetadata, X12element.ISAEnd, String.valueOf(delimiters.getSegmentTerminator()), null);

			gsCnt = 0;

			// Let's go down the rabbit hole...
			readFunctionGroups();

			metadataLogger.updateIsaData(isaIdMetadata, gsCnt);

			if (!"IEA".equals(segmentReader.field(0))) {
				ediValidator.validate(batchIdMetadata, X12element.IEA01, null, null);
			}

			ediValidator.validate(batchIdMetadata, X12element.IEA01, segmentReader.field(1), String.valueOf(gsCnt));

			String iea02 = segmentReader.field(2);
			if (!isa13.equals(iea02)) {
				String error = "ISA13 {} & IEA02 {} don't match";
				logger.error(error, isa13, iea02);
				throw new DebatcherException(error, Validator.TA1_ERROR_ISA13, Error.TYPE_TA1, batchIdMetadata);
			}
		}
		metadataLogger.updateBatchSubmissionData(batchIdMetadata, isaCnt);
	}

	private void readTransactionSets() throws Exception {
		while (true) {
			if (!"ST".equals(segmentReader.field(0))) {
				String error = String.format("Missing ST segment. Segment text is '%s'.", segmentReader.current());
				throw new DebatcherException(error, Validator.IK3_999_ERROR_MISS_SEG, Error.TYPE_999, batchIdMetadata);
			}
			stCnt++;
			segmentCnt = 1;

			// Reset for each transaction set.
			hlStack.clear();
			headerBuffer.setLength(0);

			String st01 = segmentReader.field(1);
			ediValidator.validate(batchIdMetadata, X12element.ST01, st01, null);

			st02 = segmentReader.field(2);
			stIdMetadata = metadataLogger.logStData(gsIdMetadata, st02, segmentReader.current().trim());


			// TODO: This logic is specific to claims (837). We could decouple this a bit more.
			claimType = getClaimType();
			if ("837".equals(st01) && ClaimType.OTH != claimType) {
				ediValidator.validate(batchIdMetadata, X12element.ST03, segmentReader.field(3), claimType.name());
				initRefD9idPattern();
				
				readHeader();

				// Let's go down the rabbit hole...
				int hlCnt = readHierarchicalLevels();	
				
				metadataLogger.updateStData(stIdMetadata, hlCnt);
			} else {
				// TODO: implement general handling; refactor to factory
				
				// Not an 837i/p claim, so skip all segments until we hit the end of the transaction set (SE).
				do {
					claimBuffer.append(segmentReader.current()).append(delimiters.getSegmentTerminator());
					segmentReader.next();
					segmentCnt++;
				} while (!segmentReader.field(0).equals("SE") /* && !segmentReader.isEOF()*/);
				claimCnt++;
				writeClaim(); // TODO: misnomer; actually means "write transaction set"	here				
			}

			if (segmentReader.isIeaFound() || segmentReader.field(0).equals("SE")) {
				ediValidator.validate(batchIdMetadata, X12element.SE, segmentReader.field(0), null);
				ediValidator.validate(batchIdMetadata, X12element.SE01, segmentReader.field(1), String.valueOf(segmentCnt));
			} else { // If the SE segment is missing but the IEA segment is also missing, then treat as missing IEA.
				ediValidator.validate(batchIdMetadata, X12element.IEA01, null, null);
			}

			String se02 = segmentReader.field(2);
			if (!st02.equals(se02)) {
				logger.error("ST02 {} & SE02 {} don't match", st02, se02);
				ediValidator.validate(batchIdMetadata, X12element.ST02, st02, se02);
			}

			segmentReader.next();

			if ("GE".equals(segmentReader.field(0)) || !"ST".equals(segmentReader.field(0))) {
				return;
			}
		}
	}

	// TODO: roughly a third of this method is specific to claims (837), the rest is generic (concerned with EDI envelopes) 
	private void writeClaim() throws IOException {
		String claimName = transactionId + "_" + String.format("%05d", claimCnt) + ".edi.txt";
		String url = this.config.getOutputDirectory().toString() + claimName;
		File statText = new File(url);

		try (FileOutputStream os = new FileOutputStream(statText); // TODO: later this stream needs to be passed in
				OutputStreamWriter osw = new OutputStreamWriter(os);
				Writer w = new BufferedWriter(osw)) {

			final char trm = delimiters.getSegmentTerminator();
			w.write(isaSegment);
			w.write(trm);
			w.write(gsSegment);
			w.write(trm);
			
			final String eol = delimiters.getEOL();
			w.write(headerBuffer.toString());

			Iterator<HierarchicalLevel> iter = hlStack.iterator();
			int hlSegmentsCount = 0;
			while (iter.hasNext()) {
				final String hlString = iter.next().getHlDataBuffer().toString();
				hlSegmentsCount += getSegmentCount(hlString);
				w.write(eol);
				w.write(hlString);
			}

			w.write(claimBuffer.toString());
			w.write(eol);

			final char fld = delimiters.getField();
			int headerSegmentsCnt = getSegmentCount(headerBuffer.toString());
			int clmSegmentsCnt = getSegmentCount(claimBuffer.toString());
			w.write("SE" + fld + (headerSegmentsCnt + hlSegmentsCount + clmSegmentsCnt + 1) + fld + st02 + trm + eol);
			w.write("GE" + fld + 1 + fld + gs06 + trm + eol);
			w.write("IEA" + fld + 1 + fld + isa13 + trm + eol);
			w.flush(); // we were losing the last few writes without this
		}

		int lxCnt = getLxCount(claimBuffer.toString());
		long claimId = metadataLogger.logClaim(batchIdMetadata, hlIdMetadata, clm01, clm05, lxCnt, null, claimName);
		claimIdMap.put(claimName, claimId);

		claimBuffer.setLength(0);
		checkForRefD9 = false;
	}
}
