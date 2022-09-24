package eu.essi_lab.userbase.exception;

import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * Wraps all the exception that the underlying users persistence service can
 * raise.
 * 
 * @author pezzati
 */
public class ApiEngineError extends GSException {

    private static final long serialVersionUID = -4208370328819742049L;

    @SuppressWarnings("rawtypes")
    public ApiEngineError(Class clasz, Throwable cause) {
	ErrorInfo errorInfo = new ErrorInfo();
	errorInfo.setContextId(clasz.getName());
	errorInfo.setErrorId(getErrorId());
	errorInfo.setCause(cause);
	errorInfo.setErrorDescription(getErrorDescription());
	errorInfo.setUserErrorDescription(getUserErrorDescription());
	errorInfo.setErrorCorrection(getErrorCorrection());
	errorInfo.setErrorType(ErrorInfo.ERRORTYPE_INTERNAL);
	errorInfo.setSeverity(ErrorInfo.SEVERITY_ERROR);
	addInfo(errorInfo);
    }

    private String getErrorId() {
	return "PERSISTENCE_ENGINE_ERROR";
    }

    private String getErrorDescription() {
	return "The underlying engine raises an exception.";
    }

    private String getUserErrorDescription() {
	return "Api service is temporarily unavailable.";
    }

    private String getErrorCorrection() {
	return "Please notify to you sysadmin and wait for solution.";
    }
}
