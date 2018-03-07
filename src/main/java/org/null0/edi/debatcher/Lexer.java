package org.null0.edi.debatcher;

import java.io.InputStream;
import java.io.OutputStream;

public class Lexer {
	private char EOL;
	private char EOLR;
	private char FLD;

	public Lexer() {
		this(new Delimiters());
	}

	public Lexer(Delimiters delimiters) {
		EOL = delimiters.getSegmentTerminator();
		EOLR = delimiters.getDataRepetitionSeparator();
		FLD = delimiters.getDataElementSeparator();
		delimiters.getComponentElementSeparator();
	}

	public OutputStream readSegmentData(InputStream stream) throws Exception {
		while (stream.available() > 0) {
			readSegment(stream, "ST");
		}
		return null;
	}

	private void readSegment(InputStream stream, String segmentid) throws Exception {
		switch (segmentid) {
		case "ISA":
			readSegmentBlock(stream, "ISA", "GS");
		case "GS":
			readSegmentBlock(stream, "GS", "ST");
		case "ST":
			readSegmentBlock(stream, "ST", "HL");
			break;
		}

	}

	/** Parse to an "untyped" segment as quickly as possible */
	private Segment readSegmentBlock(InputStream stream, String readfrom, String readto) throws Exception {
		StringBuilder buf = new StringBuilder();

		char c; // We will read one character at a time (sequential parse, no backtracking)

		// Eat whitespace noise before start of segment
		boolean whitespaceDone = false;
		do {
			c = (char) stream.read();
			if (!Character.isWhitespace(c)) {
				whitespaceDone = true;
			}
		} while (!whitespaceDone && stream.available() > 0);
		if (stream.available() < 1) {
			// return null ONLY for whitespace lines (typically at end-of-file, but could
			// occur anywhere)
			return null;
		}

		// Parse text into Segment
		final int MAX_BUF_SIZE = 1024 * 2; // Max allowed size of segment
		int elementCounter = 0; // sentinel -1 when we are done
		Segment segment = new Segment(true); // true => created by parser
		do {
			if (c != FLD && c != EOL && c != EOLR) {
				if (buf.length() >= MAX_BUF_SIZE) {
					// TODO: Error situation; handle the same
					break;
				}
				// If not a terminator, accumulate into buffer
				buf.append(c);
			} else {
				// We hit a terminator (FLD, EOL, EOLR), so write the element or handle as name
				if (segment.getId().isEmpty()) {
					StringBuilder test = new StringBuilder();
					segment.setId(buf.toString()); // Handle as name (beginning of line)
					if (segment.getId().equals(readfrom)) {
						StringBuilder temp = new StringBuilder();
						do {
							c = (char) stream.read(); // Advance the characters to read till the end segment
							if (c != FLD && c != EOL) {
								temp.append(c);
							} else {
								test.append(temp.toString());
								test.append(FLD); // Add the trimmed field
								if (temp.toString().trim().equals(readto)) {
									// System.out.println(test);
									segment.setLine(temp.toString() + EOL);
									elementCounter = -1; // Once we read the segment block we exit the inner loop
								}
								temp.setLength(0);
							}
						} while (elementCounter != -1);
					}
				}

				// Set up for the next state
				if (c == FLD) {
					buf.setLength(0); // CLEAR field buffer
				} else {
					// If line terminator (EOL or EOLR) then this was our last iteration (last
					// element)
					elementCounter = -1;
					if (c == EOL) {
						buf.setLength(0); // CLEAR field buffer
					}
				}
			}

			if (stream.available() < 1) {
				elementCounter = -1; // end
			}

			// READ next character
			if (elementCounter != -1) {
				c = (char) stream.read();
			}
		} while (elementCounter != -1);
		return segment;
	}

}