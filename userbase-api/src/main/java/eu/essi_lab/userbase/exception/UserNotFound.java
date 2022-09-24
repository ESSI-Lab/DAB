package eu.essi_lab.userbase.exception;

import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * Raise this exception when persistence layer returns you no results but you
 * expected one (e.g: you expect one user as query resutl but persistence layer
 * returns you none).
 * 
 * @author pezzati
 */
public class UserNotFound extends GSException {

    private static final long serialVersionUID = 7742269107548193153L;

    @SuppressWarnings("rawtypes")
    public UserNotFound(Class clasz, Throwable cause) {
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
	return "USER_NOT_FOUND";
    }

    private String getErrorDescription() {
	return "Service was unable to find required user.";
    }

    private String getUserErrorDescription() {
	return "Service was unable to find required user.";
    }

    private String getErrorCorrection() {
	return "Please doublecheck search parameters and try again.";
    }
}
