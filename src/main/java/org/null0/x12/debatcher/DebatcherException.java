package org.null0.x12.debatcher;

import org.null0.x12.debatcher.Validator.Error;

// TODO: This was migrated from something too complex; simplify
public class DebatcherException extends Exception {
	public enum ErrorLevel {
		BATCH, CLAIMS
	}	
	private static final long serialVersionUID = 1L;
	
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
	
	public static String getStackTraceFromException(Throwable e) {
		return getStackTraceFromException(e, null);
	}
	
	private static String getStackTraceFromException(Throwable e, StringBuilder buf) {
		if (e == null) {
			return null;
		}

		StackTraceElement[] elements = e.getStackTrace();
		if (elements != null && elements.length > 0) {
			if (buf == null) {
				buf = e.getMessage() == null ? new StringBuilder() : new StringBuilder(e.getMessage());
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
	
	private String ediFileName;
	private String errorCode; // NO_UCD (use default)
	private ErrorLevel errorLevel;
	private Error errorType;
	private long idBatch;

	public DebatcherException(String msg, String errorCode, Error errorType, ErrorLevel errorLevel, long idBatch) {
		super(msg);
		this.errorCode = errorCode;
		this.errorType = errorType;
		this.errorLevel = errorLevel;
		this.idBatch = idBatch;
	}

	public String getEdiFileName() {
		return ediFileName;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public ErrorLevel getErrorLevel() {
		return errorLevel;
	}

	public Error getErrorType() {
		return errorType;
	}

	public long getIdBatch() {
		return idBatch;
	}

	public void setEdiFileName(String ediFileName) {
		this.ediFileName = ediFileName;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public void setErrorLevel(ErrorLevel errorLevel) {
		this.errorLevel = errorLevel;
	}

	public void setErrorType(Error errorType) {
		this.errorType = errorType;
	}

	public void setIdBatch(long idBatch) {
		this.idBatch = idBatch;
	}
}
