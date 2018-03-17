package org.null0.edi.debatcher;

class Delimiters {
	private char dataElementSeparator;
	private char segmentTerminator;
	private EdiWrapStyle ediWrap;
	private String osNewLine;
	
	enum EdiWrapStyle {
		Unknown, Unwrapped, Unix, Windows
	};

	public Delimiters() {
		this.osNewLine = System.getProperty("line.separator");

		// Defaults
		this.ediWrap = EdiWrapStyle.Unknown;
		this.dataElementSeparator = '*';
		this.segmentTerminator = '~';
	}

	public char getField() {
		return dataElementSeparator;
	}

	public void setField(char dataElementSeparator) {
		this.dataElementSeparator = dataElementSeparator;
	}

	public char getSegment() {
		return segmentTerminator;
	}

	public void setSegmen(char segmentTerminator) {
		this.segmentTerminator = segmentTerminator;
	}

	public EdiWrapStyle getLineWrap() {
		return ediWrap;
	}

	public void setLineWrap(EdiWrapStyle ediWrap) {
		this.ediWrap = ediWrap;
	}
	
	/**
	 * Reads up to maxBytesToScan for a data chunk in order to determine EDI line wrap style.
	 * Outcome can be determined by looking at the return value or the new state given by getLineWrap.
	 * @param dataChunk input byte buffer
	 * @param maxBytesToScan max bytes to scan before stopping/failing
	 * @return EdiWrapStyle This setter returns the wrap style (same as calling getLineWrap)
	 */
	public EdiWrapStyle setLineWrap(byte[] dataChunk, int maxBytesToScan) {
		this.ediWrap = EdiWrapStyle.Unwrapped;
		int max = Math.min(dataChunk.length, maxBytesToScan); 
		for (int i = 0; i < max - 1; i++) {
			if (dataChunk[i] == 0x0D && dataChunk[i + 1] == 0x0A) {
				this.ediWrap = EdiWrapStyle.Windows; // \r\n
				break;
			} else if (dataChunk[i] == 0x0A) {
				this.ediWrap = EdiWrapStyle.Unix; // \n
				break;
			}
		}
		return this.ediWrap;
	}
	
	public String getEOL() {
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

	public String getOsNewLine() {
		return osNewLine;
	}
}
