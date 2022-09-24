package eu.essi_lab.userbase.exception;

import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * Exception is raised when given {@link GSUser} is invalid.
 * 
 * @author pezzati
 */
public class InvalidUser extends GSException {

    private static final long serialVersionUID = 5453268401928877332L;

    @SuppressWarnings("rawtypes")
    public InvalidUser(Class clasz, Throwable cause) {
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
	return "INVALID_USER_EXCEPTION";
    }

    private String getErrorDescription() {
	return "The given user is not valid.";
    }

    private String getUserErrorDescription() {
	return "The given user is not valid.";
    }

    private String getErrorCorrection() {
	return "Doublecheck user's mail and authentication provider attributes and try operation again.";
    }
}
