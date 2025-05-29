package eu.essi_lab.messages;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.configuration.ExecutionMode;
import eu.essi_lab.lib.utils.GSLoggerFactory.GSLogger;
import eu.essi_lab.lib.utils.HostNamePropertyUtils;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.GSProperty;
import eu.essi_lab.model.RuntimeInfoElement;
import eu.essi_lab.rip.RuntimeInfoProvider;

/**
 * Extends the superclass to provides specific information on the result of a
 * {@link WebRequestValidator#validate(WebRequest)} call.<br>
 * The rules by which a {@link WebRequest} is validated depends from the specific {@link WebRequestValidator}
 * implementation
 * 
 * @see WebRequestTransformer#validate(WebRequest)
 * @see AbstractRequestHandler#validate(WebRequest)
 * @author Fabrizio
 */
public class ValidationMessage extends GSMessage implements RuntimeInfoProvider {

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
    private String responseEncoding;

    @Override
    public HashMap<String, List<String>> provideInfo() {

	HashMap<String, List<String>> map = new HashMap<>();

	map.put(RuntimeInfoElement.PROVIDER_NAME.getName(), Arrays.asList(getName()));
	map.put(RuntimeInfoElement.HOST_NAME.getName(), Arrays.asList(HostNamePropertyUtils.getHostNameProperty()));
	map.put(RuntimeInfoElement.EXECUTION_MODE.getName(), Arrays.asList(ExecutionMode.get().name()));
	map.put(RuntimeInfoElement.FREE_MEMORY.getName(), Arrays.asList(String.valueOf(GSLogger.getFreeMemory())));
	map.put(RuntimeInfoElement.TOTAL_MEMORY.getName(), Arrays.asList(String.valueOf(GSLogger.getTotalMemory())));
	map.put(RuntimeInfoElement.USED_MEMORY.getName(), Arrays.asList(String.valueOf(GSLogger.getUsedMemory())));

	map.put(RuntimeInfoElement.VALIDATION_MESSAGE_RESULT.getName(), Arrays.asList(getResult().name()));

	if (!getExceptions().isEmpty()) {
	    map.put(RuntimeInfoElement.VALIDATION_MESSAGE_ERROR_MESSAGE.getName(), Arrays.asList(getError()));
	    map.put(RuntimeInfoElement.VALIDATION_MESSAGE_ERROR_CODE.getName(), Arrays.asList(getErrorCode()));
	    map.put(RuntimeInfoElement.VALIDATION_MESSAGE_LOCATOR.getName(), Arrays.asList(getLocator()));
	}

	return map;
    }

    /**
     * @param encoding
     */
    public void setResponseEncoding(String responseEncoding) {

	this.responseEncoding = responseEncoding;
    }

    /**
     * @return the responseEncoding
     */
    public Optional<String> getResponseEncoding() {

	return Optional.ofNullable(responseEncoding);
    }

    /**
     * @param result
     */
    public void setResult(ValidationResult result) {

	getPayload().add(new GSProperty<ValidationResult>(VALIDATION_RESULT, result));
    }

    /**
     * @return
     */
    public ValidationResult getResult() {

	return getPayload().get(VALIDATION_RESULT, ValidationResult.class);
    }

    /**
     * @param exception
     */
    public void addException(ValidationException exception) {
	getExceptions().add(exception);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<ValidationException> getExceptions() {
	List exceptions = getPayload().get(VALIDATION_EXCEPTIONS, List.class);
	if (exceptions == null) {
	    exceptions = new ArrayList<ValidationException>();
	    getPayload().add(new GSProperty<List>(VALIDATION_EXCEPTIONS, exceptions));
	}
	return exceptions;
    }

    /**
     * Convenience methods to use in case of a single exception
     * 
     * @param error
     */
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

    /**
     * @return
     */
    public String getError() {
	List<ValidationException> exceptions = getExceptions();
	if (exceptions.isEmpty()) {
	    return null;
	} else {
	    return exceptions.get(0).getMessage();
	}
    }

    /**
     * @param info
     */
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

    /**
     * @return
     */
    public String getLocator() {
	List<ValidationException> exceptions = getExceptions();
	if (exceptions.isEmpty()) {
	    return null;
	} else {
	    return exceptions.get(0).getLocator();
	}
    }

    /**
     * @param error
     */
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

    /**
     * @return
     */
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

    @Override
    public String getName() {

	return getClass().getSimpleName();
    }
}
