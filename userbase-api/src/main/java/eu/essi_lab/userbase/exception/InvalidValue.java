package eu.essi_lab.userbase.exception;

import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * Exception is raised when parameter does not contains expected value.
 * 
 * @author pezzati
 */
public class InvalidValue extends GSException {

    private static final long serialVersionUID = 6915057396726861182L;

    @SuppressWarnings("rawtypes")
    public InvalidValue(Class clasz, Throwable cause) {
	ErrorInfo errorInfo = new ErrorInfo();
	errorInfo.setContextId(clasz.getName());
	errorInfo.setErrorId(getErrorId());
	errorInfo.setCause(cause);
	errorInfo.setErrorDescription(getErrorDescription());
	errorInfo.setUserErrorDescription(getUserErrorDescription());
	errorInfo.setErrorCorrection(getErrorCorrection());
	errorInfo.setErrorType(ErrorInfo.ERRORTYPE_CLIENT);
	errorInfo.setSeverity(ErrorInfo.SEVERITY_WARNING);
	addInfo(errorInfo);
    }

    private String getErrorId() {
	return "INVALID_VALUE_EXCEPTION";
    }

    private String getErrorDescription() {
	return "The given value is invalid in the current context.";
    }

    private String getUserErrorDescription() {
	return "The given value is invalid in the current context.";
    }

    private String getErrorCorrection() {
	return "Doublecheck given value and try operation again.";
    }
}
