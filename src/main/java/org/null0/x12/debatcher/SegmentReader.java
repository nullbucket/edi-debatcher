package org.null0.x12.debatcher;

import java.io.EOFException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SegmentReader {
	private static final Logger logger = LoggerFactory.getLogger(SegmentReader.class);
	
	private Config config;
	private Delimiters delimiters;
	private InputStream inputStream;
	private long batchId;
	
	private String[] fields;
	private boolean fileReadCompleted;
	private boolean isEOF;
	private boolean isfirstDataChunk;
	private boolean isIeaFound;
	private boolean lastDataChunkReturned;
	private String lastPartialSegment;
	private String segment;
	private int segmentIndex;
	private String[] segments;

	SegmentReader(Config config, InputStream inputStream, long batchIdMetadata) {
		this.config = config;
		this.delimiters = null; // to be initialized after first call to next()/getDataChunk()
		this.inputStream = inputStream;
		this.batchId = batchIdMetadata;
		fields = null;
		fileReadCompleted = false;
		isfirstDataChunk = true;
		isEOF = false; // is this the same as fileReadCompleted?
		isIeaFound = false;
		lastDataChunkReturned = false;
		lastPartialSegment = "";
		segmentIndex = 0;
		segment = null;
	}

	public String current() {
		return segment;
	}

	public String field(int position) {
		if (fields == null) {
			logger.warn("SegmentReader.fields was null; this will probably cause an error. Did you forget to call next()?");
			return null;
		}
		if (position >= fields.length) {
			return ""; // either out of bounds OR we just ended early because field was optional
		}
		return fields[position];
	}

	public boolean fileReadCompleted() {
		return fileReadCompleted;
	}
	
	public Delimiters getDelimiters() {
		return delimiters;
	}

	public boolean isEOF() {
		return isEOF;
	}

	public boolean isIeaFound() {
		return isIeaFound;
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

	// TODO: I don't like having this public, but it is used in current tri-state logic for REF-D9
	public void setCurrent(String value) {
		segment = value;
		if (segment != null) {
			segment = StringUtils.stripStart(segment, config.ignoreWhitespaceBetweenSegments() ? null : "\r\n");
			fields = segment.split("\\" + delimiters.getField()); // all fields for current segment
		}
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

		// Read chunks into data buffer
		bytesRead = inputStream.read(dataChunk);
		isEOF = (bytesRead == -1);		
		if (!isEOF) {
			// Data buffer must contain complete segments, no partials
			data = lastPartialSegment + new String(dataChunk, 0, bytesRead);
			if (bytesRead < config.getBufferSize()) {
				this.lastDataChunkReturned = true;
			}
		}

		// If this is our very first chunk, do some validation and initialization
		if (isfirstDataChunk) {
			this.delimiters = new Delimiters(data, batchId);
			logger.debug("field and segment delimiter are {} & {}", delimiters.getField(), delimiters.getSegmentTerminator());
			isfirstDataChunk = false;
		}

		String terminator = String.valueOf(delimiters.getSegmentTerminator());
		segments = data.split(java.util.regex.Pattern.quote(terminator));
		lastPartialSegment = segments[segments.length - 1];

		if (data.endsWith(terminator)) {
			lastPartialSegment = "";
		}

		if (!lastDataChunkReturned && !data.endsWith(terminator)) {
			segments = Arrays.copyOf(segments, segments.length - 1);
		}
	}
}
