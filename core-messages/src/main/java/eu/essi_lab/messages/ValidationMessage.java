package eu.essi_lab.messages;

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

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.handlers.AbstractRequestHandler;

import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.GSProperty;
public class ValidationMessage extends GSMessage {

    /**
     * 
     */
    private static final long serialVersionUID = 7285102693776114634L;

    /**
     * @author Fabrizio
     */
    public enum ValidationResult {
	/**
	 * 
	 */
	VALIDATION_FAILED,
	/**
	 * 
	 */
	VALIDATION_SUCCESSFUL
    }

    private static final String VALIDATION_EXCEPTIONS = "VALIDATION_EXCEPTIONS";
    private static final String VALIDATION_RESULT = "VALIDATION_RESULT_NAME";

    public void setResult(ValidationResult result) {

	getPayload().add(new GSProperty<ValidationResult>(VALIDATION_RESULT, result));
    }

    public ValidationResult getResult() {

	return getPayload().get(VALIDATION_RESULT, ValidationResult.class);
    }

    //

    public void addException(ValidationException exception) {
	getExceptions().add(exception);
    }

    public List<ValidationException> getExceptions() {
	List exceptions = getPayload().get(VALIDATION_EXCEPTIONS, List.class);
	if (exceptions == null) {
	    exceptions = new ArrayList<ValidationException>();
	    getPayload().add(new GSProperty<List>(VALIDATION_EXCEPTIONS, exceptions));
	}
	return exceptions;
    }

    // convenience methods to use in case of a single exception

    public void setError(String error) {
	List<ValidationException> exceptions = getExceptions();
	ValidationException exception;
	if (exceptions.isEmpty()) {
	    exception = new ValidationException();
	    exceptions.add(exception);
	} else {
	    exception = exceptions.get(0);
	}
	exception.setMessage(error);
    }

    public String getError() {
	List<ValidationException> exceptions = getExceptions();
	if (exceptions.isEmpty()) {
	    return null;
	} else {
	    return exceptions.get(0).getMessage();
	}
    }

    public void setLocator(String info) {
	List<ValidationException> exceptions = getExceptions();
	ValidationException exception;
	if (exceptions.isEmpty()) {
	    exception = new ValidationException();
	    exceptions.add(exception);
	} else {
	    exception = exceptions.get(0);
	}
	exception.setLocator(info);
    }

    public String getLocator() {
	List<ValidationException> exceptions = getExceptions();
	if (exceptions.isEmpty()) {
	    return null;
	} else {
	    return exceptions.get(0).getLocator();
	}
    }

    public void setErrorCode(String error) {
	List<ValidationException> exceptions = getExceptions();
	ValidationException exception;
	if (exceptions.isEmpty()) {
	    exception = new ValidationException();
	    exceptions.add(exception);
	} else {
	    exception = exceptions.get(0);
	}
	exception.setCode(error);
    }

    public String getErrorCode() {
	List<ValidationException> exceptions = getExceptions();
	if (exceptions.isEmpty()) {
	    return null;
	} else {
	    return exceptions.get(0).getCode();
	}
    }

    @Override
    public String toString() {

	String out = "Result: " + getResult() + "\n";
	out += "Error: " + getError() + "\n";
	out += "Error code: " + getErrorCode() + "\n";
	out += "Locator: " + getLocator() + "\n";

	return out;
    }

}
