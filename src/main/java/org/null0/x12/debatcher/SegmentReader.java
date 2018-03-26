package org.null0.x12.debatcher;

import java.io.EOFException;
import java.io.InputStream;
import java.util.Arrays;

import org.null0.x12.debatcher.DebatcherException.ERROR_LEVEL;
import org.null0.x12.debatcher.Delimiters.EdiWrapStyle;
import org.null0.x12.debatcher.Validator.ERROR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SegmentReader {
	private static final Logger logger = LoggerFactory.getLogger(SegmentReader.class);

	// class invariants
	private Config config;
	private Delimiters delimiters;
	private InputStream inputStream;
	private long batchId;

	// fields
	private String segment;
	private String[] fields;
	private boolean initialFileValidation;
	private boolean isIeaFound;
	private boolean isEOF;

	// internal state
	private boolean lastDataChunkReturned;
	private String lastPartialSegment;
	private boolean fileReadCompleted;
	private String[] segments;
	private int segmentIndex;

	SegmentReader(Config config, Delimiters delimiters, InputStream inputStream, long batchIdMetadata) {
		this.config = config;
		this.delimiters = delimiters;
		this.inputStream = inputStream;
		fields = null;
		segment = null;
		segmentIndex = 0;
		fileReadCompleted = false;
		isEOF = false; // is this the same as fileReadCompleted?
		isIeaFound = false;
		lastDataChunkReturned = false;
		initialFileValidation = false;
		lastPartialSegment = "";
	}
	public boolean fileReadCompleted() {
		return fileReadCompleted;
	}

	public boolean isIeaFound() {
		return isIeaFound;
	}

	public boolean isEOF() {
		return isEOF;
	}

	public String field(int position) {
		if (fields == null || position >= fields.length) {
			return null; // didn't call next() or out of bounds
		}
		return fields[position].replaceAll("\\r|\\n", "");
	}

	public String current() {
		return segment;
	}
	
	// TODO: I don't like having this public, but it is used in current tri-state logic for REF-D9
	public void setCurrent(String value) {
		segment = value;
		if (segment != null) {
			fields = segment.split("\\" + delimiters.getField()); // all fields for current segment
		}
	}
	
	public String next() throws Exception {
		if (segments == null) {
			getDataChunk();
		}
		setCurrent(segments[segmentIndex]);

		if (field(0).equals("IEA")) {
			isIeaFound = true;
		}
		segmentIndex++;
		if (segments.length == segmentIndex) {
			segmentIndex = 0;
			segments = null; // call getDataChunk on next call
			if (lastDataChunkReturned) {
				fileReadCompleted = true;
			}
		}
		return segment;
		// logger.debug("Segment: {}", segment);
	}

	private void getDataChunk() throws Exception {
		if (isEOF) {
			final String msg = "Unexpected end of file while attempting to get next data chunk";
			logger.error(msg);
			throw new EOFException(msg);
		}

		byte[] dataChunk = new byte[config.getBufferSize()]; // chunks
		String data = ""; // data buffer
		int bytesRead = 0;
		boolean lastDataChunkEndsWithSgmtDlm = false;

		// Read chunks into data buffer
		while ((bytesRead = inputStream.read(dataChunk)) != -1) {
			// If first time reading data, determine EDI Wrapping Style
			if (delimiters.getLineWrap() == EdiWrapStyle.Unknown) {
				delimiters.setLineWrap(dataChunk, bytesRead);
			}

			// Data buffer must contain complete segments, no partials
			data = lastPartialSegment + new String(dataChunk, 0, bytesRead);
			if (bytesRead < config.getBufferSize()) {
				this.lastDataChunkReturned = true;
			}
			break;
		}
		isEOF = (bytesRead == -1);

		// If this is our very first chunk, do some validation and initialization
		if (!initialFileValidation) {
			if (!"ISA".equals(data.substring(0, 3))) {
				throw new DebatcherException(
						"Not a valid Interchange Segment",
						DefaultValidator.TA1_ERROR_ISAIEA,
						ERROR.TYPE_TA1,
						ERROR_LEVEL.Batch,
						batchId);
			}
			initialFileValidation = true;

			delimiters.setField(data.substring(103, 104).charAt(0));
			delimiters.setSegmentTerminator(data.substring(105, 106).charAt(0));
			if (delimiters.getSegmentTerminator() == '\n' || delimiters.getSegmentTerminator() == '\r') {
				// If segment terminator is a line-wrap char itself, then no wrapping is necessary
				delimiters.setLineWrap(EdiWrapStyle.Unwrapped);
			}
			logger.debug("field and segment delimiter are {} & {}", delimiters.getField(),
					delimiters.getSegmentTerminator());
		}

		String segmentTerminator = String.valueOf(delimiters.getSegmentTerminator());
		if (data.endsWith(segmentTerminator)) {
			lastDataChunkEndsWithSgmtDlm = true;
		}

		segments = data.split(java.util.regex.Pattern.quote(segmentTerminator));
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
}
