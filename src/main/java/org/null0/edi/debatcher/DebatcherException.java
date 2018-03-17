package org.null0.edi.debatcher;
import org.null0.edi.debatcher.EncounterEdiValidator.ERROR;

// TODO: This was migrated from something too complex; simplify
public class DebatcherException extends Exception {
	private static final long serialVersionUID = 1L;
	private String errorCode;
	private long idBatch;
	private ERROR errorType;
	private ERROR_LEVEL errorLevel;
	private String ediFileName;
	private ERROR_OR_EXCEPTION errorOrException;

	public static enum ERROR_OR_EXCEPTION {
		Exception
	};

	public static enum ERROR_LEVEL {
		Batch, Encounter
	};

	public DebatcherException() {
		super();
	}

	public DebatcherException(Throwable e) {
		super(e);
	}

	public DebatcherException(String msg, String errorCode) {
		super(msg);
		this.errorCode = errorCode;
	}

	public DebatcherException(String msg, String errorCode, long idBatch, ERROR errorType, ERROR_LEVEL errorLevel) {
		super(msg);
		this.errorCode = errorCode;
		this.idBatch = idBatch;
		this.errorType = errorType;
		this.errorLevel = errorLevel;
	}

	public DebatcherException(String msg, long idBatch, String ediFileName, ERROR_OR_EXCEPTION errorOrException) {
		super(msg);
		this.idBatch = idBatch;
		this.ediFileName = ediFileName;
		this.errorOrException = errorOrException;
	}

	public DebatcherException(String msg, String errorCode, ERROR errorType, ERROR_LEVEL errorLevel, long idBatch, ERROR_OR_EXCEPTION errorOrException) {
		super(msg);
		this.errorCode = errorCode;
		this.errorType = errorType;
		this.errorLevel = errorLevel;
		this.idBatch = idBatch;
		this.errorOrException = errorOrException;
	}

	public ERROR_OR_EXCEPTION getErrorOrException() {
		return errorOrException;
	}

	public void setErrorOrException(ERROR_OR_EXCEPTION errorOrException) {
		this.errorOrException = errorOrException;
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
		String stackTrace = null;

		if (e != null) {
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
			stackTrace = new String(buf);
		}

		return stackTrace;
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
