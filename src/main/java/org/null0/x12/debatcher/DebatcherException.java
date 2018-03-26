package org.null0.x12.debatcher;

import org.null0.x12.debatcher.Validator.ERROR;

// TODO: This was migrated from something too complex; simplify
public class DebatcherException extends Exception {
	private static final long serialVersionUID = 1L;
	private String errorCode;
	private long idBatch;
	private ERROR errorType;
	private ERROR_LEVEL errorLevel;
	private String ediFileName;

	public static enum ERROR_LEVEL {
		Batch, Encounter
	}; // NO_UCD (use default)

	public DebatcherException(String msg, String errorCode, ERROR errorType, ERROR_LEVEL errorLevel, long idBatch) {
		super(msg);
		this.errorCode = errorCode;
		this.errorType = errorType;
		this.errorLevel = errorLevel;
		this.idBatch = idBatch;
	}

	public String getEdiFileName() {
		return ediFileName;
	}

	public void setEdiFileName(String ediFileName) {
		this.ediFileName = ediFileName;
	}

	public ERROR_LEVEL getErrorLevel() {
		return errorLevel;
	}

	public void setErrorLevel(ERROR_LEVEL errorLevel) {
		this.errorLevel = errorLevel;
	}

	public long getIdBatch() {
		return idBatch;
	}

	public void setIdBatch(long idBatch) {
		this.idBatch = idBatch;
	}

	public ERROR getErrorType() {
		return errorType;
	}

	public void setErrorType(ERROR errorType) {
		this.errorType = errorType;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public static String getStackTraceFromException(Throwable e) {
		return getStackTraceFromException(e, null);
	}

	private static String getStackTraceFromException(Throwable e, StringBuffer buf) {
		if (e == null) {
			return null;
		}

		StackTraceElement[] elements = e.getStackTrace();
		if (elements != null && elements.length > 0) {
			if (buf == null) {
				if (e.getMessage() != null)
					buf = new StringBuffer(e.getMessage());
				else
					buf = new StringBuffer();
			} else {
				buf.append("\n\n");
				buf.append(e.getMessage());
			}
			for (int i = 0; i < elements.length; i++) {
				StackTraceElement element = elements[i];
				buf.append("\nat ");
				buf.append(element.toString());
			}
		}

		if (e.getCause() != null) {
			getStackTraceFromException(e.getCause(), buf);
		}

		return new String(buf);
	}

	public static Throwable getLowestLevelCause(Throwable e) {
		Throwable result = null;
		// null will be returned unless there is a cause
		if (e != null && e.getCause() != null) {
			result = e;
			while (result.getCause() != null) {
				result = result.getCause();
			}
		}
		return result;
	}
}
