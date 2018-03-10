package org.null0.edi.debatcher;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

// TODO: don't think this is used. See why.
public class Extractor {

	public Extractor(URL edifilepath) {
	}

	public OutputStream Debatch(InputStream input) throws IOException, Exception {
		Lexer lexer = new Lexer();
		return lexer.readSegmentData(input);
	}

}
