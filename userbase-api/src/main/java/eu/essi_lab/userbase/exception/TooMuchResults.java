package eu.essi_lab.userbase.exception;

import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * Raise this exception when persistence layer returns you more than one result
 * but you expected just one (e.g: you expect just one user as query result but
 * persistece layer returns you two).
 * 
 * @author pezzati
 */
public class TooMuchResults extends GSException {

    private static final long serialVersionUID = -6076019861860648445L;

    @SuppressWarnings("rawtypes")
    public TooMuchResults(Class clasz, Throwable cause) {
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
	return "TOO_MUCH_USERS_FOUND";
    }

    private String getErrorDescription() {
	return "Service has more than a user matching the given username.";
    }

    private String getUserErrorDescription() {
	return "Service has more than a user matching the given username.";
    }

    private String getErrorCorrection() {
	return "Please contact your system administrator and report the error.";
    }
}
