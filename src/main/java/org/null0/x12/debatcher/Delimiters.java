package org.null0.x12.debatcher;

import org.apache.commons.lang3.StringUtils;
import org.null0.x12.debatcher.Validator.Error;

class Delimiters {
	private char dataElementSeparator;
	private String eol;
	private char segmentTerminator;

	public Delimiters(String isaSegment, long batchId) throws DebatcherException {
		if (!"ISA".equals(isaSegment.substring(0, 3))) {
			String error = String.format("Not a valid Interchange Segment. The first 106 characters are '%s'.", StringUtils.left(isaSegment, 106));
			throw new DebatcherException(error,	Validator.TA1_ERROR_ISAIEA,	Error.TYPE_TA1,	batchId);
		}
		setField(isaSegment.substring(103, 104).charAt(0));
		setSegmentTerminator(isaSegment.substring(105, 106).charAt(0));
		setLineWrap(isaSegment);
	}

	public String getEOL() {
		return eol;
	}

	public char getField() {
		return dataElementSeparator;
	}
	
	public char getSegmentTerminator() {
		return segmentTerminator;
	}

	public void setField(char dataElementSeparator) {
		this.dataElementSeparator = dataElementSeparator;
	}

	public void setSegmentTerminator(char segmentTerminator) {
		this.segmentTerminator = segmentTerminator;
	}
	
	private void setLineWrap(String dataChunk) {		
		eol = ""; // default	
		if (segmentTerminator == '\n' || segmentTerminator == '\r') {
			return; // if the segment terminator itself is eol then treat it as logically unwrapped
		} else if (dataChunk.contains("\r\n")) {
			eol = "\r\n";
		} else if (dataChunk.contains("\n")) {
			eol = "\r";
		}
	}
}
