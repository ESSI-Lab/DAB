package eu.essi_lab.userbase.exception;

import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * Exception is raised when underlying persistence service throws an exception
 * on startup.
 * 
 * @author pezzati
 */
public class UserbaseApiMisconfigured extends GSException {

    private static final long serialVersionUID = -2260182805014554130L;

    @SuppressWarnings("rawtypes")
    public UserbaseApiMisconfigured(Class clasz, Throwable cause) {
	ErrorInfo errorInfo = new ErrorInfo();
	errorInfo.setContextId(clasz.getName());
	errorInfo.setErrorId(getErrorId());
	errorInfo.setCause(cause);
	errorInfo.setErrorDescription(getErrorDescription());
	errorInfo.setUserErrorDescription(getUserErrorDescription());
	errorInfo.setErrorCorrection(getErrorCorrection());
	errorInfo.setErrorType(ErrorInfo.ERRORTYPE_INTERNAL);
	errorInfo.setSeverity(ErrorInfo.SEVERITY_FATAL);
	addInfo(errorInfo);
    }

    private String getErrorId() {
	return "API_MISCONFIGURATION";
    }

    private String getErrorDescription() {
	return "Service is misconfigured or a grave error occurred.";
    }

    private String getUserErrorDescription() {
	return "Service is misconfigured or a grave error occurred.";
    }

    private String getErrorCorrection() {
	return "Notify your sysadmin and wait for solution.";
    }
}
