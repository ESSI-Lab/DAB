package eu.essi_lab.model.exceptions;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.HashMap;
import java.util.Map;

public class ErrorInfo {

    public static final int SEVERITY_WARNING = 0;
    public static final int SEVERITY_ERROR = 1;
    public static final int SEVERITY_FATAL = 2;

    public static final int ERRORTYPE_CLIENT = 0;
    public static final int ERRORTYPE_INTERNAL = 1;
    public static final int ERRORTYPE_SERVICE = 2;

    private Throwable cause = null;
    private String errorId = null;
    private String contextId = null;

    private int errorType = -1;

    private int severity = -1;

    private String userErrorDescription = null;
    private String errorDescription = null;
    private String errorCorrection = null;

    private Map<String, Object> parameters = new HashMap<String, Object>();
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
     * meaning the combination of {@link #getContextId()} and errorId should be unique throughout your application.
     * 
     * @return {@link String}
     */
    public String getErrorId() {
	return errorId;
    }

    /**
     * Sets a unique id that identifies this error.
     * The errorId tells what went wrong, like FILE_LOAD_ERROR. The id only has to be unique within the same context,
     * meaning the combination of {@link #getContextId()} and errorId should be unique throughout your application.
     * 
     * @param errorId
     */
    public void setErrorId(String errorId) {
	this.errorId = errorId;
    }

    /**
     * Returns a unique id that identifies the context where the error occurred. 
     * The contextId tells where the error occurred (in what class, component, layer etc.). The contextId and {@link #getErrorId()} combination
     * used at any specific exception handling point should be unique throughout the application.
     * 
     * @return {@link String}
     */
    public String getContextId() {
	return contextId;
    }

    /**
     * Sets a unique id that identifies the context where the error occurred. 
     * The contextId tells where the error occurred (in what class, component, layer etc.). The contextId and {@link #getErrorId()} combination
     * used at any specific exception handling point should be unique throughout the application.
     * 
     * @param contextId
     */
    public void setContextId(String contextId) {
	this.contextId = contextId;
    }

    /**
     * Returns the errorType field tells whether the error was caused by erroneous input to the application,
     * an external service that failed, or an internal error. The idea is to use this field to indicate to the
     * exception catching code what to do with this error.
     * Should only the user be notified, or should the application operators and developers be notified too?
     * 
     * @return int
     */
    public int getErrorType() {
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
    public void setErrorType(int errorType) {
	this.errorType = errorType;
    }

    /**
     * Returns the severity of the error. E.g. WARNING, ERROR, FATAL etc.
     * It is up to you to define the severity levels for your application.
     * 
     * @return int
     */
    public int getSeverity() {
	return severity;
    }

    /**
     * Important: Use static fields of this class as parameter.
     * Sets the severity of the error. E.g. WARNING, ERROR, FATAL etc.
     * It is up to you to define the severity levels for your application.
     * 
     * @param severity
     */
    public void setSeverity(int severity) {
	this.severity = severity;
    }

    /**
     * Returns the error description to show to the user.
     *  
     * @return {@link String}
     */
    public String getUserErrorDescription() {
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

    /**
     * Returns a map of any additional parameters needed to construct a meaningful error description,
     * either for the users or the application operators and developers.
     * 
     * @return {@link Map<String, Object>}
     */
    public Map<String, Object> getParameters() {
	return parameters;
    }

}
