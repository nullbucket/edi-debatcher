package org.null0.x12.debatcher;

import java.nio.file.Path;

public interface Config {
	public int getBufferSize();
	public Path getOutputDirectory();
	public String[] getValidReceivers();
	public String[] getValidSenders();
	public boolean willUpdateTransactionId();
	boolean willRejectOnValidationError();
}