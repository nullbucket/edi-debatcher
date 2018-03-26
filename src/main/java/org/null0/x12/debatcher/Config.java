package org.null0.x12.debatcher;

import java.nio.file.Path;

public interface Config {
	public Path getOutputDirectory();
	public int getBufferSize();
	public boolean willUpdateTransactionId();
	boolean willRejectOnValidationError();
	public String[] getValidSenders();
	public String[] getValidReceivers();
}