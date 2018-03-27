package org.null0.x12.debatcher;

import org.null0.x12.debatcher.DebatcherException.ErrorLevel;
import org.null0.x12.debatcher.Validator.Error;

class Delimiters {
	enum EdiWrapStyle {
		Unix, Unknown, Unwrapped, Windows
	}
	
	private char dataElementSeparator;
	private EdiWrapStyle ediWrap;
	private String eol;
	private final String osNewLine;
	private char segmentTerminator;

	public Delimiters(String isaSegment, long batchId) throws DebatcherException {
		if (!"ISA".equals(isaSegment.substring(0, 3))) {
			throw new DebatcherException(
					"Not a valid Interchange Segment",
					Validator.TA1_ERROR_ISAIEA,
					Error.TYPE_TA1,
					ErrorLevel.BATCH,
					batchId);
		}
		osNewLine = System.getProperty("line.separator");
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

	public EdiWrapStyle getLineWrap() {
		return ediWrap;
	}

	public String getOsNewLine() {
		return osNewLine;
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
		// default
		eol = "";
		ediWrap = EdiWrapStyle.Unwrapped;
		
		if (segmentTerminator == '\n' || segmentTerminator == '\r') {
			return; // if the segment terminator itself is eol, then treat it as logically unwrapped
		} else if (dataChunk.contains("\r\n")) {
			eol = "\r\n";
			ediWrap = EdiWrapStyle.Windows;			
		} else if (dataChunk.contains("\n")) {
			eol = "\r";
			ediWrap = EdiWrapStyle.Unix;			
		}
	}
}
