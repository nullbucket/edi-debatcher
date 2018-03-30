package org.null0.x12.debatcher;

import java.nio.file.Path;

public interface Config {
	int getBufferSize();
	Path getOutputDirectory();
	String[] getValidReceivers();
	String[] getValidSenders();
	boolean ignoreWhitespaceBetweenSegments();
	boolean willRejectOnValidationError();
	boolean willUpdateTransactionId();
}