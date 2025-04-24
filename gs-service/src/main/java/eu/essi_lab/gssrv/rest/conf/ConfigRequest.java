/**
 * 
 */
package eu.essi_lab.gssrv.rest.conf;

import java.util.Date;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.json.JSONObject;

import eu.essi_lab.cfga.option.InputPattern;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.utils.LabeledEnum;
import eu.essi_lab.model.Queryable.ContentType;

/**
 * @author Fabrizio
 */
public abstract class ConfigRequest {

    private JSONObject object;

    /**
     * 
     */
    public ConfigRequest() {

	object = new JSONObject();

	object.put("request", getClass().getSimpleName());

	object.put("parameters", new JSONObject());
    }

    /**
     * @param object
     */
    public ConfigRequest(JSONObject object) {

	this.object = object;
    }

    /**
     * @param object
     * @return
     */
    public static Optional<String> getName(JSONObject object) {

	if (object.has("request")) {

	    return Optional.of(object.getString("request"));
	}

	return Optional.empty();
    }

    @Override
    public String toString() {

	return object.toString(3);
    }

    @Override
    public boolean equals(Object object) {

	return object instanceof ConfigRequest && ((ConfigRequest) object).toString().equals(this.toString());
    }

    /**
     * @return
     */
    public void validate() {

	if (!object.has("parameters")) {

	    throw new IllegalArgumentException("Missing request parameters");
	}

	mandatoryCheck();

	supportedCheck();

	readParameters().forEach(paramName -> {

	    Object value = readValue(paramName);

	    contentTypeCheck(paramName, value);

	    patternCheck(paramName, value);

	    enumCheck(paramName, value);
	});
    }

    /**
     * @return
     */
    public String getName() {

	return object.getString("request");
    }

    /**
     * @param paramName
     * @param value
     */
    public void put(String paramName, String value) {

	object.getJSONObject("parameters").put(paramName, value);
    }

    /**
     * @param parameter
     * @return
     */
    public Optional<Object> read(String parameter) {

	if (object.getJSONObject("parameters").has(parameter)) {

	    return Optional.of(readValue(parameter));
	}

	return Optional.empty();
    }

    /**
     * @return
     */
    protected abstract List<Parameter> getSupportedParameters();

    /**
     * 
     */
    protected void mandatoryCheck() {

	List<String> mandatoryParams = getSupportedParameters().//
		stream().//
		filter(p -> p.isMandatory()).//
		map(p -> p.getName()).//
		collect(Collectors.toList());

	mandatoryParams.removeAll(readParameters());
	if (!mandatoryParams.isEmpty()) {

	    throw new IllegalArgumentException(
		    "Missing mandatory parameters: " + mandatoryParams.stream().collect(Collectors.joining(", ")));
	}
    }

    /**
     * 
     */
    protected void supportedCheck() {

	List<String> supported = getSupportedParameters().//
		stream().//
		map(p -> p.getName()).//
		collect(Collectors.toList());

	List<String> requestParams = readParameters();
	requestParams.removeAll(supported);

	if (!requestParams.isEmpty()) {

	    throw new IllegalArgumentException("Unknown parameters: " + requestParams.stream().collect(Collectors.joining(", "))
		    + ". Supported parameters: " + supported.stream().collect(Collectors.joining(", ")));
	}
    }

    /**
     * @param value
     */
    @SuppressWarnings("incomplete-switch")
    protected void contentTypeCheck(String paramName, Object value) {

	ContentType type = getSupportedParameters().//
		stream().//
		filter(p -> p.getName().equals(paramName)).//
		findFirst().//
		get().//
		getContentType();

	switch (type) {
	case BOOLEAN:
	    if (!value.toString().equals("true") && !value.toString().equals("false")) {

		throw new IllegalArgumentException(
			"Unsupported value '" + value + "'. Parameter '" + paramName + "' should be of type boolean");
	    }

	    break;

	case DOUBLE:

	    try {
		Double.valueOf(value.toString());
	    } catch (NumberFormatException ex) {

		throw new IllegalArgumentException(
			"Unsupported value '" + value + "'. Parameter '" + paramName + "' should be of type double");
	    }

	    break;

	case INTEGER:

	    try {
		Integer.valueOf(value.toString());
	    } catch (NumberFormatException ex) {

		throw new IllegalArgumentException(
			"Unsupported value '" + value + "'. Parameter '" + paramName + "' should be of type integer");
	    }

	    break;

	case ISO8601_DATE_TIME:

	    Optional<Date> iso8601ToDate = ISO8601DateTimeUtils.parseISO8601ToDate(value.toString());

	    if (iso8601ToDate.isEmpty() || (value.toString().length() != "YYYY-MM-DDThh:mm:ssZ".length()
		    && value.toString().length() != "YYYY-MM-DDThh:mm:ss".length())) {

		throw new IllegalArgumentException("Unsupported value '" + value + "'. Parameter '" + paramName
			+ "' should be of type ISO8601 date time YYYY-MM-DDThh:mm:ssZ");
	    }

	    break;

	case LONG:

	    try {
		Long.valueOf(value.toString());
	    } catch (NumberFormatException ex) {

		throw new IllegalArgumentException(
			"Unsupported value '" + value + "'. Parameter '" + paramName + "' should be of type long");
	    }

	    break;
	}
    }

    /**
     * 
     */
    protected void patternCheck(String paramName, Object value) {

	Optional<InputPattern> optPattern = getSupportedParameters().//
		stream().//
		filter(p -> p.getName().equals(paramName)).//
		findFirst().//
		get().//
		getInputPattern();

	if (optPattern.isPresent()) {

	    Pattern pattern = Pattern.compile(optPattern.get().getPattern());
	    Matcher matcher = pattern.matcher(value.toString());

	    if (!matcher.matches()) {

		throw new IllegalArgumentException("Unsupported value '" + value + "'. Parameter '" + paramName + "' should match the '"
			+ optPattern.get().getPattern() + "' pattern");
	    }
	}
    }

    /**
     * @param paramName
     * @param value
     */
    protected void enumCheck(String paramName, Object value) {

	Optional<Class<? extends LabeledEnum>> optEnum = getSupportedParameters().//
		stream().//
		filter(p -> p.getName().equals(paramName)).//
		findFirst().//
		get().//
		getEnum();

	if (optEnum.isPresent()) {

	    if (!LabeledEnum.values(optEnum.get()).//
		    stream().//
		    map(e -> e.getLabel()).//
		    collect(Collectors.toList()).//
		    contains(value.toString())) {

		String supValues = LabeledEnum.values(optEnum.get()).//
			stream().//
			map(e -> e.getLabel()).//
			collect(Collectors.joining(", "));

		throw new IllegalArgumentException(
			"Unsupported value '" + value + "' for parameter '" + paramName + "'. Supported values are: " + supValues);

	    }
	}
    }

    /**
     * @return
     */
    protected List<String> readParameters() {

	return object.getJSONObject("parameters").//
		keySet().//
		stream().//
		collect(Collectors.toList());

    }

    /**
     * @param parameter
     * @return
     */
    private Object readValue(String parameter) {

	return object.getJSONObject("parameters").get(parameter);
    }
}
