package org.null0.x12.debatcher;

import org.null0.x12.debatcher.Validator.Error;

public class DebatcherException extends Exception {
	private static final long serialVersionUID = 1L;
	private final String errorCode; // NO_UCD (use default)
	private final Error errorType;
	private final long idBatch;

	public DebatcherException(String msg, String errorCode, Error errorType, long idBatch) {
		super(String.format("%s (code=%s, type=%s, batch id=%d)", msg, errorCode, errorType, idBatch));
		this.errorCode = errorCode;
		this.errorType = errorType;
		this.idBatch = idBatch;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public Error getErrorType() {
		return errorType;
	}

	public long getIdBatch() {
		return idBatch;
	}
}
