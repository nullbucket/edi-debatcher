package org.null0.edi.debatcher;

public class Delimiters {
	private char dataElementSeparator;
	private char dataRepetitionSeparator;
	private char componentElementSeparator;
	private char segmentTerminator;

	public Delimiters() {
		// Initialize to the default (most common) EDI delimiters
		dataElementSeparator = '*';
		dataRepetitionSeparator = '^';
		componentElementSeparator = ':';
		segmentTerminator = '~';
	}

	public char getDataElementSeparator() {
		return dataElementSeparator;
	}

	public void setDataElementSeparator(char dataElementSeparator) {
		this.dataElementSeparator = dataElementSeparator;
	}

	public char getDataRepetitionSeparator() {
		return dataRepetitionSeparator;
	}

	public void setDataRepetitionSeparator(char dataRepetitionSeparator) {
		this.dataRepetitionSeparator = dataRepetitionSeparator;
	}

	public char getComponentElementSeparator() {
		return componentElementSeparator;
	}

	public void setComponentElementSeparator(char componentElementSeparator) {
		this.componentElementSeparator = componentElementSeparator;
	}

	public char getSegmentTerminator() {
		return segmentTerminator;
	}

	public void setSegmentTerminator(char segmentTerminator) {
		this.segmentTerminator = segmentTerminator;
	}
}
