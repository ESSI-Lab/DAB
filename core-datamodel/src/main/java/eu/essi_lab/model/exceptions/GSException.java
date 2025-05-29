package eu.essi_lab.model.exceptions;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.output.ByteArrayOutputStream;

import com.google.common.base.Charsets;

import eu.essi_lab.lib.utils.GSLoggerFactory;

public class GSException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 4205641760399814704L;

    private List<ErrorInfo> errorInfoList;

    /**
     * 
     */
    private GSException() {

	errorInfoList = new ArrayList<>();
    }

    /**
     * Creates a new {@link GSException} with an empty {@link #getErrorInfoList()}
     * 
     * @return
     */
    public static GSException createException() {

	return new GSException();
    }

    /**
     * @param caller
     * @param errorDescription
     * @param errorCorrection
     * @param userError
     * @param errorType
     * @param errorSeverity
     * @param errorId
     * @param cause
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static GSException createException(//
	    Class caller, //
	    String errorDescription, //
	    String errorCorrection, //
	    String userError, //
	    String errorType, //
	    String errorSeverity, //
	    String errorId, //
	    Throwable cause) {

	GSException ex = new GSException();

	ErrorInfo errorInfo = new ErrorInfo();
	errorInfo.setCaller(caller);
	errorInfo.setErrorId(errorId);
	errorInfo.setCause(cause);
	errorInfo.setErrorDescription(errorDescription);
	errorInfo.setUserErrorDescription(userError);
	errorInfo.setErrorCorrection(errorCorrection);
	errorInfo.setErrorType(errorType);
	errorInfo.setSeverity(errorSeverity);

	ex.addErrorInfo(errorInfo);

	return ex;
    }

    /**
     * @param caller
     * @param errorId
     * @param cause
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static GSException createException(//
	    Class caller, //
	    String errorId, //
	    Throwable cause) {

	GSException ex = new GSException();

	ErrorInfo errorInfo = new ErrorInfo();
	errorInfo.setCaller(caller);
	errorInfo.setErrorId(errorId);
	errorInfo.setCause(cause);
	errorInfo.setErrorDescription(cause.getMessage());
	errorInfo.setErrorType(ErrorInfo.ERRORTYPE_INTERNAL);
	errorInfo.setSeverity(ErrorInfo.SEVERITY_ERROR);

	ex.addErrorInfo(errorInfo);

	return ex;
    }

    /**
     * @param caller
     * @param errorType
     * @param errorSeverity
     * @param errorId
     * @param cause
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static GSException createException(//
	    Class caller, //
	    String errorType, //
	    String errorSeverity, //
	    String errorId, //
	    Throwable cause) {

	GSException ex = new GSException();

	ErrorInfo errorInfo = new ErrorInfo();
	errorInfo.setCaller(caller);
	errorInfo.setErrorId(errorId);
	errorInfo.setCause(cause);
	errorInfo.setErrorDescription(cause.getMessage());
	errorInfo.setErrorType(errorType);
	errorInfo.setSeverity(errorSeverity);

	ex.addErrorInfo(errorInfo);

	return ex;
    }

    /**
     * @param caller
     * @param errorDescription
     * @param errorCorrection
     * @param userError
     * @param errorType
     * @param errorSeverity
     * @param errorId
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static GSException createException(//
	    Class caller, //
	    String errorDescription, //
	    String errorCorrection, //
	    String userError, //
	    String errorType, //
	    String errorSeverity, //
	    String errorId) {

	return createException(caller, errorDescription, errorCorrection, userError, errorType, errorSeverity, errorId, null);
    }

    /**
     * @param caller
     * @param errorDescription
     * @param userError
     * @param errorType
     * @param errorSeverity
     * @param errorId
     * @param cause
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static GSException createException(//
	    Class caller, //
	    String errorDescription, //
	    String userError, //
	    String errorType, //
	    String errorSeverity, //
	    String errorId, //
	    Throwable cause) {

	return createException(caller, errorDescription, null, userError, errorType, errorSeverity, errorId, cause);
    }

    /**
     * @param caller
     * @param errorDescription
     * @param userError
     * @param errorType
     * @param errorSeverity
     * @param errorId
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static GSException createException(//
	    Class caller, //
	    String errorDescription, //
	    String userError, //
	    String errorType, //
	    String errorSeverity, //
	    String errorId) {

	return createException(caller, errorDescription, null, userError, errorType, errorSeverity, errorId, null);
    }

    /**
     * @param caller
     * @param errorDescription
     * @param errorType
     * @param errorSeverity
     * @param errorId
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static GSException createException(//
	    Class caller, //
	    String errorDescription, //
	    String errorType, //
	    String errorSeverity, //
	    String errorId) {

	return createException(caller, errorDescription, null, null, errorType, errorSeverity, errorId, null);
    }

    /**
     * @param errorInfoList
     * @return
     */
    public static GSException createException(List<ErrorInfo> errorInfoList) {

	GSException ex = new GSException();
	errorInfoList.forEach(info -> ex.addErrorInfo(info));
	return ex;
    }

    /**
     * @param info
     * @return
     */
    public static GSException createException(ErrorInfo info) {

	return createException(Arrays.asList(info));
    }

    /**
     * @return
     */
    public List<ErrorInfo> getErrorInfoList() {

	return errorInfoList;
    }

    /**
     * 
     */
    @Override
    public void printStackTrace(PrintStream s) {

	for (int j = 0; j < getErrorInfoList().size(); j++) {

	    s.println("[ Error info " + j + " ]");

	    ErrorInfo i = getErrorInfoList().get(j);

	    if (i.getCause() != null) {

		i.getCause().printStackTrace(s);
	    }

	    s.println(i);
	}
    }

    @Override
    public String getMessage() {

	if (!errorInfoList.isEmpty()) {

	    String description = errorInfoList.get(0).getErrorDescription();

	    if (description != null && !description.isEmpty()) {

		return description;
	    }
	}

	return "No error description available";
    }

    /**
     * 
     */
    public void log() {

	if (getErrorInfoList().isEmpty()) {
	    // it should not happen
	    return;
	}

	ByteArrayOutputStream stream = new ByteArrayOutputStream();

	this.printStackTrace(new PrintStream(stream));

	GSLoggerFactory.getLogger(getErrorInfoList().get(0).getCaller()).error(stream.toString(Charsets.UTF_8), this);
    }

    /**
     * @param info
     * @return
     */
    private void addErrorInfo(ErrorInfo info) {

	this.errorInfoList.add(info);
    }
}
