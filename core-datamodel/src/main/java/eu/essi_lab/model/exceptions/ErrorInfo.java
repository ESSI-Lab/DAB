package eu.essi_lab.model.exceptions;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import org.json.JSONObject;

public class ErrorInfo {

    public static final String SEVERITY_WARNING = "SEVERITY_WARNING";
    public static final String SEVERITY_ERROR = "SEVERITY_ERROR";
    public static final String SEVERITY_FATAL = "SEVERITY_FATAL";

    public static final String ERRORTYPE_CLIENT = "ERRORTYPE_CLIENT";
    public static final String ERRORTYPE_INTERNAL = "ERRORTYPE_INTERNAL";
    public static final String ERRORTYPE_SERVICE = "ERRORTYPE_SERVICE";

    private String errorType;
    private String errorSeverity;

    private Throwable cause;

    private String errorId;
    private Class<?> caller;
    private String userErrorDescription;
    private String errorDescription;
    private String errorCorrection;

    /**
     * 
     */
    public ErrorInfo() {
    }

    /**
     * Returns the error cause, if an alien exception is caught and wrapped.
     * 
     * @return {@link Throwable}
     */
    public Throwable getCause() {
	return cause;
    }

    /**
     * Sets the error cause, if an alien exception is caught and wrapped.
     * 
     * @param cause
     */
    public void setCause(Throwable cause) {
	this.cause = cause;
    }

    /**
     * Returns a unique id that identifies this error.
     * The errorId tells what went wrong, like FILE_LOAD_ERROR. The id only has to be unique within the same context,
     * meaning the combination of {@link #getCaller()} and errorId should be unique throughout your application.
     * 
     * @return {@link String}
     */
    public String getErrorId() {

	if (errorId == null || errorId.isEmpty()) {

	    return "No error id available";
	}

	return errorId;
    }

    /**
     * Sets a unique id that identifies this error.
     * The errorId tells what went wrong, like FILE_LOAD_ERROR. The id only has to be unique within the same context,
     * meaning the combination of {@link #getCaller()} and errorId should be unique throughout your application.
     * 
     * @param errorId
     */
    public void setErrorId(String errorId) {
	this.errorId = errorId;
    }

    /**
     * @return
     */
    public Class<?> getCaller() {
	return caller;
    }

    /**
     * @param caller
     */
    public void setCaller(Class<?> caller) {
	this.caller = caller;
    }

    /**
     * Returns the errorType field tells whether the error was caused by erroneous input to the application,
     * an external service that failed, or an internal error. The idea is to use this field to indicate to the
     * exception catching code what to do with this error.
     * Should only the user be notified, or should the application operators and developers be notified too?
     * 
     * @return int
     */
    public String getErrorType() {

	if (errorType == null || errorType.isEmpty()) {

	    return "No error type available";
	}

	return errorType;
    }

    /**
     * Important: Use static fields of this class as parameter.
     * Sets the errorType field tells whether the error was caused by erroneous input to the application,
     * an external service that failed, or an internal error.
     * The idea is to use this field to indicate to the exception catching code what to do with this error.
     * Should only the user be notified, or should the application operators and developers be notified too?
     * 
     * @param errorType
     */
    public void setErrorType(String errorType) {
	this.errorType = errorType;
    }

    /**
     * Returns the errorSeverity of the error. E.g. WARNING, ERROR, FATAL etc.
     * It is up to you to define the errorSeverity levels for your application.
     * 
     * @return int
     */
    public String getSeverity() {

	if (errorSeverity == null || errorSeverity.isEmpty()) {

	    return "No error severity available";
	}

	return errorSeverity;
    }

    /**
     * Important: Use static fields of this class as parameter.
     * Sets the errorSeverity of the error. E.g. WARNING, ERROR, FATAL etc.
     * It is up to you to define the errorSeverity levels for your application.
     * 
     * @param errorSeverity
     */
    public void setSeverity(String severity) {
	this.errorSeverity = severity;
    }

    /**
     * Returns the error description to show to the user.
     * 
     * @return {@link String}
     */
    public String getUserErrorDescription() {

	if (userErrorDescription == null || userErrorDescription.isEmpty()) {

	    return "No user error description available";
	}

	return userErrorDescription;
    }

    /**
     * Sets the error description to show to the user.
     * 
     * @return {@link String}
     */
    public void setUserErrorDescription(String userErrorDescription) {
	this.userErrorDescription = userErrorDescription;
    }

    /**
     * Contains a description of the error with all the necessary details needed for the application operators,
     * and possibly the application developers, to understand what error occurred.
     * 
     * @return {@link String}
     */
    public String getErrorDescription() {

	if (errorDescription == null || errorDescription.isEmpty()) {

	    return "No error description available";
	}

	return errorDescription;
    }

    /**
     * Sets a description of the error with all the necessary details needed for the application operators,
     * and possibly the application developers, to understand what error occurred.
     * 
     * @param errorDescription
     */
    public void setErrorDescription(String errorDescription) {
	this.errorDescription = errorDescription;
    }

    /**
     * Returns a description of how the error can be corrected, if you know how. For instance,
     * if loading a configuration file fails, this text may say that the operator should check that the
     * configuration file that failed to load is located in the correct directory.
     * 
     * @return {@link String}
     */
    public String getErrorCorrection() {

	if (errorCorrection == null || errorCorrection.isEmpty()) {

	    return "No error correction available";
	}

	return errorCorrection;
    }

    /**
     * Sets a description of how the error can be corrected, if you know how. For instance,
     * if loading a configuration file fails, this text may say that the operator should check that the
     * configuration file that failed to load is located in the correct directory.
     * 
     * @param errorCorrection
     */
    public void setErrorCorrection(String errorCorrection) {
	this.errorCorrection = errorCorrection;
    }

    @Override
    public String toString() {

	StringBuilder builder = new StringBuilder();

	if (caller != null) {

	    builder.append("- Caller: " + caller.getName() + "\n");
	}

	if (errorId != null) {

	    builder.append("- Error id: " + errorId + "\n");
	}

	if (errorDescription != null && !errorDescription.isEmpty()) {

	    builder.append("- Description: " + errorDescription + "\n");
	}

	if (userErrorDescription != null && !userErrorDescription.isEmpty()) {

	    builder.append("- User decription: " + userErrorDescription + "\n");
	}

	if (errorCorrection != null && !errorCorrection.isEmpty()) {

	    builder.append("- Error correction: " + errorCorrection + "\n");
	}

	if (errorType != null && !errorType.isEmpty()) {

	    builder.append("- Error type: " + errorType + "\n");
	}

	if (errorSeverity != null && !errorSeverity.isEmpty()) {

	    builder.append("- Error severity: " + errorSeverity + "\n");
	}

	return builder.toString();
    }

    /**
     * @return
     */
    public JSONObject toJSONObject() {

	JSONObject json = new JSONObject();

	if (caller != null) {

	    json.put("Caller: ", caller.getName());
	}

	if (errorId != null && !errorId.isEmpty()) {

	    json.put("Error id: ", errorId);
	}

	if (errorDescription != null && !errorDescription.isEmpty()) {

	    json.put("Error message: ", errorDescription);
	}

	if (userErrorDescription != null && !userErrorDescription.isEmpty()) {

	    json.put("Error decription: ", userErrorDescription);
	}

	if (errorCorrection != null && !errorCorrection.isEmpty()) {

	    json.put("User error: ", errorCorrection);
	}

	if (errorType != null && !errorType.isEmpty()) {

	    json.put("Error type: ", errorType);
	}

	if (errorSeverity != null && !errorSeverity.isEmpty()) {

	    json.put("Error severity: ", errorSeverity);
	}

	return json;
    }
}
