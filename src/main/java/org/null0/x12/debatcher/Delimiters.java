package org.null0.x12.debatcher;

class Delimiters {
	enum EdiWrapStyle {
		Unix, Unknown, Unwrapped, Windows
	}
	
	private char dataElementSeparator;
	private EdiWrapStyle ediWrap;
	private String eol;
	private String osNewLine;
	private char segmentTerminator;;

	public Delimiters() {
		this.osNewLine = System.getProperty("line.separator");

		// Defaults
		this.ediWrap = EdiWrapStyle.Unknown;
		this.dataElementSeparator = '*';
		this.segmentTerminator = '~';
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

	/** Reads up to maxBytesToScan for a data chunk in order to determine EDI line wrap style.
	 * Outcome can be determined by looking at the return value or the new state given by getLineWrap.
	 * 
	 * @param dataChunk input byte buffer
	 * @param maxBytesToScan max bytes to scan before stopping/failing
	 * @return EdiWrapStyle This setter returns the wrap style (same as calling getLineWrap) */
	public EdiWrapStyle setLineWrap(byte[] dataChunk, int maxBytesToScan) {
		eol = "";
		ediWrap = EdiWrapStyle.Unwrapped;
		int max = Math.min(dataChunk.length, maxBytesToScan);
		for (int i = 0; i < max - 1; i++) {
			if (dataChunk[i] == 0x0D && dataChunk[i + 1] == 0x0A) {
				ediWrap = EdiWrapStyle.Windows;
				eol = "\r\n";
				break;
			} else if (dataChunk[i] == 0x0A) {
				ediWrap = EdiWrapStyle.Unix; // \n
				eol = "\n";
				break;
			}
		}
		return ediWrap;
	}

	public void setLineWrap(EdiWrapStyle ediWrap) {
		this.ediWrap = ediWrap;
	}

	public void setSegmentTerminator(char segmentTerminator) {
		this.segmentTerminator = segmentTerminator;
	}
}
