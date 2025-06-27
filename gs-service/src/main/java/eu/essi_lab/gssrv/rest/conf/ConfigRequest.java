/**
 * 
 */
package eu.essi_lab.gssrv.rest.conf;

import java.util.Arrays;

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

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONObject;

import eu.essi_lab.cfga.option.InputPattern;
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

	object.put("request", computeName(getClass()));

	object.put("parameters", new JSONObject());
    }

    /**
     * @param clazz
     * @return
     */
    static String computeName(Class<? extends ConfigRequest> clazz) {

	return clazz.getSimpleName().replace("Request", "");
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

	List<Parameter> mandatoryParameters = getMandatoryParameters(false);
	mandatoryParameters.addAll(getMandatoryParameters(true));

	if (!mandatoryParameters.isEmpty() && !object.has("parameters")) {

	    throw new IllegalArgumentException("Missing request parameters");
	}

	mandatoryCheck();

	supportedCheck();

	readParameters().forEach(paramName -> {

	    Object value = readValue(paramName);

	    List<Parameter> supportedParameters = getSupportedParameters();

	    contentTypeCheck(supportedParameters, paramName, value);

	    patternCheck(supportedParameters, paramName, value);

	    enumCheck(supportedParameters, paramName, value);
	});

	readCompositeParameters().forEach(compositeName -> {

	    readNestedParameters(compositeName).forEach(subParam -> {

		Object value = readNestedValue(compositeName, subParam);

		List<Parameter> supportedNestedParams = getSupportedParameters().//
			stream().filter(p -> p.getCompositeName().isPresent()).//
			filter(p -> p.getCompositeName().get().equals(compositeName)).//
			collect(Collectors.toList());

		contentTypeCheck(supportedNestedParams, subParam, value);

		patternCheck(supportedNestedParams, subParam, value);

		enumCheck(supportedNestedParams, subParam, value);
	    });
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

	getParametersObject().put(paramName, value);
    }

    /**
     * @param compositeParam
     * @param nestedParam
     * @param value
     */
    public void put(String compositeParam, String nestedParam, String value) {

	JSONObject compositeObject = new JSONObject();

	if (!getParametersObject().has(compositeParam)) {

	    getParametersObject().put(compositeParam, compositeObject);

	} else {

	    compositeObject = getParametersObject().getJSONObject(compositeParam);
	}

	compositeObject.put(nestedParam, value);
    }

    /**
     * @param parameter
     * @return
     */
    public Optional<Object> read(String parameter) {

	if (object.has("parameters") && getParametersObject().has(parameter)) {

	    return Optional.of(readValue(parameter));
	}

	return Optional.empty();
    }

    /**
     * @param compositeParam
     * @param nestedParam
     * @return
     */
    public Optional<Object> read(String compositeParam, String nestedParam) {

	if (getParametersObject().has(compositeParam)) {

	    JSONObject parentObject = getParametersObject().getJSONObject(compositeParam);

	    if (parentObject.has(nestedParam)) {

		return Optional.of(readNestedValue(compositeParam, nestedParam));
	    }
	}

	return Optional.empty();
    }

    /**
     * @return the object
     */
    public JSONObject getObject() {

	return object;
    }

    /**
     * @return
     */
    protected abstract List<Parameter> getSupportedParameters();

    /**
     * 
     */
    protected void mandatoryCheck() {

	//
	// mandatory check
	//

	List<String> mandatoryParams = getSupportedParameters().//
		stream().//
		filter(p -> p.getCompositeName().isEmpty()).//
		filter(p -> p.isMandatory()).//
		map(p -> p.getName()).//
		collect(Collectors.toList());

	mandatoryParams.removeAll(readParameters());
	if (!mandatoryParams.isEmpty()) {

	    throw new IllegalArgumentException("Missing mandatory parameter/s: " + mandatoryParams.//
		    stream().//
		    map(p -> "'" + p + "'").//
		    collect(Collectors.joining(", ")));
	}

	//
	// mandatory composite check
	//

	List<String> mandatoryCompositeNames = getMandatoryParameters(true).//
		stream().//
		map(p -> p.getCompositeName().get()).//
		distinct().//
		collect(Collectors.toList());

	mandatoryCompositeNames.removeAll(readCompositeParameters());

	if (!mandatoryCompositeNames.isEmpty()) {

	    throw new IllegalArgumentException("Missing mandatory composite parameter/s: " + mandatoryCompositeNames.//
		    stream().//
		    map(p -> "'" + p + "'").//
		    collect(Collectors.joining(", ")));
	}

	//
	// mandatory nested check
	//

	List<Parameter> mandatoryCompositeParams = getSupportedParameters().//
		stream().//
		filter(p -> p.getCompositeName().isPresent()).//
		filter(p -> p.isMandatory()).//
		filter(p -> readCompositeParameters().contains(p.getCompositeName().get())).//
		collect(Collectors.toList());

	List<String> mandatoryNestedParamsNames = mandatoryCompositeParams.//
		stream().//
		map(p -> p.getName()).//
		collect(Collectors.toList());

	mandatoryNestedParamsNames.removeAll(readNestedParameters());

	if (!mandatoryNestedParamsNames.isEmpty()) {

	    throw new IllegalArgumentException("Missing mandatory nested parameter/s: " + mandatoryCompositeParams.//
		    stream().//
		    map(p -> "'" + p.getCompositeName().get() + "." + p.getName() + "'").//
		    collect(Collectors.joining(", ")));
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

	    throw new IllegalArgumentException("Unknown parameters: " + requestParams.//
		    stream().//
		    map(p -> "'" + p + "'").//
		    collect(Collectors.joining(", ")) + ". Supported parameters: " + supported.//
			    stream().//
			    map(p -> "'" + p + "'").//
			    collect(Collectors.joining(", ")));
	}

	//
	//
	//

	List<String> supportedCompositeNames = getSupportedParameters().//
		stream().//
		filter(p -> p.getCompositeName().isPresent()).//
		map(p -> p.getCompositeName().get()).//
		distinct().//
		collect(Collectors.toList());

	List<String> compositeParameters = readCompositeParameters();
	compositeParameters.removeAll(supportedCompositeNames);

	if (!compositeParameters.isEmpty()) {

	    throw new IllegalArgumentException("Unsupported composite parameters: " + compositeParameters.//
		    stream().//
		    map(p -> "'" + p + "'").//
		    collect(Collectors.joining(", ")) + ". Supported composite parameters: " + supportedCompositeNames.//
			    stream().//
			    map(p -> "'" + p + "'").//
			    collect(Collectors.joining(", ")));
	}

	//
	//
	//

	List<Parameter> supportedNestedParams = getSupportedParameters().//
		stream().//
		filter(p -> p.getCompositeName().isPresent()).//
		collect(Collectors.toList());

	List<String> supportedNestedNames = supportedNestedParams.//
		stream().//
		map(p -> p.getName()).//
		collect(Collectors.toList());

	List<String> nestedParameters = readNestedParameters();
	nestedParameters.removeAll(supportedNestedNames);

	if (!nestedParameters.isEmpty()) {

	    throw new IllegalArgumentException("Unsupported nested parameters: " + nestedParameters.//
		    stream().//
		    map(p -> "'" + p + "'").//
		    collect(Collectors.joining(", ")) + ". Supported nested parameters: " + supportedNestedParams.//
			    stream().//
			    map(p -> "'" + p.getCompositeName().get() + "." + p.getName() + "'").//
			    collect(Collectors.joining(", ")));
	}
    }

    /**
     * @param supportedParams
     * @param paramName
     * @param value
     */
    @SuppressWarnings("incomplete-switch")
    protected void contentTypeCheck(List<Parameter> supportedParams, String paramName, Object value) {

	Parameter parameter = supportedParams.//
		stream().//
		filter(p -> p.getName().equals(paramName)).//
		findFirst().//
		get();

	ContentType type = parameter.getContentType();

	boolean multiValue = parameter.isMultiValue();

	List<String> values = Arrays.asList(value.toString());

	if (multiValue) {

	    values = Arrays.asList(value.toString().split(",")).//
		    stream().//
		    map(v -> v.trim().strip()).//
		    collect(Collectors.toList());
	}

	values.forEach(val -> {

	    switch (type) {
	    case BOOLEAN:
		if (!val.toString().equals("true") && !val.toString().equals("false")) {

		    throw new IllegalArgumentException(
			    "Unsupported value '" + val + "'. Parameter '" + paramName + "' must be of type boolean");
		}

		break;

	    case DOUBLE:

		try {
		    Double.valueOf(val.toString());
		} catch (NumberFormatException ex) {

		    throw new IllegalArgumentException(
			    "Unsupported value '" + val + "'. Parameter '" + paramName + "' must be of type double");
		}

		break;

	    case INTEGER:

		try {
		    Integer.valueOf(val.toString());
		} catch (NumberFormatException ex) {

		    throw new IllegalArgumentException(
			    "Unsupported value '" + val + "'. Parameter '" + paramName + "' must be of type integer");
		}

		break;

	    case ISO8601_DATE_TIME:

		if (!parseDateTime(val.toString()) || val.toString().length() != "YYYY-MM-DDThh:mm:ss".length()) {

		    throw new IllegalArgumentException("Unsupported value '" + val + "'. Parameter '" + paramName
			    + "' must be of type ISO8601 date time according to the 'Europe/Berlin' TimeZone: 'YYYY-MM-DDThh:mm:ss'");
		}

		break;

	    case LONG:

		try {
		    Long.valueOf(val.toString());
		} catch (NumberFormatException ex) {

		    throw new IllegalArgumentException(
			    "Unsupported value '" + val + "'. Parameter '" + paramName + "' must be of type long");
		}

		break;
	    }
	});
    }

    /**
     * @param supportedParams
     * @param paramName
     * @param value
     */
    protected void patternCheck(List<Parameter> supportedParams, String paramName, Object value) {

	Parameter parameter = supportedParams.//
		stream().//
		filter(p -> p.getName().equals(paramName)).//
		findFirst().//
		get();

	Optional<InputPattern> optPattern = parameter.getInputPattern();

	boolean multiValue = parameter.isMultiValue();

	if (optPattern.isPresent()) {

	    List<String> values = Arrays.asList(value.toString());

	    if (multiValue) {

		values = Arrays.asList(value.toString().split(",")).//
			stream().//
			map(v -> v.trim().strip()).//
			collect(Collectors.toList());
	    }

	    values.forEach(val -> {

		Pattern pattern = Pattern.compile(optPattern.get().getPattern());
		Matcher matcher = pattern.matcher(val);

		if (!matcher.matches()) {

		    throw new IllegalArgumentException("Unsupported value '" + val + "'. Parameter '" + paramName + "' should match the '"
			    + optPattern.get().getPattern() + "' pattern");
		}
	    });
	}
    }

    /**
     * @param supportedParams
     * @param paramName
     * @param value
     */
    protected void enumCheck(List<Parameter> supportedParams, String paramName, Object value) {

	Parameter parameter = supportedParams.//
		stream().//
		filter(p -> p.getName().equals(paramName)).//
		findFirst().//
		get();

	boolean multiValue = parameter.isMultiValue();

	List<String> values = Arrays.asList(value.toString());

	if (multiValue) {

	    values = Arrays.asList(value.toString().split(",")).//
		    stream().//
		    map(v -> v.trim().strip()).//
		    collect(Collectors.toList());
	}

	Optional<Class<? extends LabeledEnum>> optEnum = parameter.getEnum();

	if (optEnum.isPresent()) {

	    values.forEach(val -> {

		if (!LabeledEnum.values(optEnum.get()).//
			stream().//
			map(e -> e.getLabel()).//
			collect(Collectors.toList()).//
			contains(val.toString())) {

		    String supValues = LabeledEnum.values(optEnum.get()).//
			    stream().//
			    map(e -> "'" + e.getLabel() + "'").//
			    collect(Collectors.joining(", "));

		    throw new IllegalArgumentException(
			    "Unsupported value '" + val + "' for parameter '" + paramName + "'. Supported values are: " + supValues);

		}
	    });
	}
    }

    /**
     * @return
     */
    protected List<String> readParameters() {

	return getParametersObject().//
		keySet().//
		stream().//
		filter(key -> !(getParametersObject().get(key) instanceof JSONObject)).//
		collect(Collectors.toList());
    }

    /**
     * @return
     */
    protected List<String> readCompositeParameters() {

	return getParametersObject().//
		keySet().//
		stream().filter(key -> getParametersObject().get(key) instanceof JSONObject).//
		collect(Collectors.toList());//
    }

    /**
     * @return
     */
    protected List<String> readNestedParameters() {

	return readCompositeParameters().//
		stream().//
		map(key -> getParametersObject().getJSONObject(key)).//
		flatMap(object -> object.keySet().stream()).//
		collect(Collectors.toList());//
    }

    /**
     * @param compositeName
     * @return
     */
    protected List<String> readNestedParameters(String compositeName) {

	return readCompositeParameters().//
		stream().//
		filter(p -> p.equals(compositeName)).//
		map(key -> getParametersObject().getJSONObject(key)).//
		flatMap(object -> object.keySet().stream()).//
		collect(Collectors.toList());//
    }

    /**
     * @param parameter
     * @return
     */
    protected Object readValue(String parameter) {

	return getParametersObject().get(parameter);
    }

    /**
     * @param composite
     * @param nested
     * @return
     */
    protected Object readNestedValue(String composite, String nested) {

	return getParametersObject().getJSONObject(composite).get(nested);
    }

    /**
     * @return
     */
    protected JSONObject getParametersObject() {

	return object.has("parameters") ? object.getJSONObject("parameters") : new JSONObject();
    }

    /**
     * @param composite
     * @return
     */
    private List<Parameter> getMandatoryParameters(boolean composite) {

	return getSupportedParameters().//
		stream().//
		filter(p -> composite ? p.getCompositeName().isPresent() : p.getCompositeName().isEmpty()).//
		filter(p -> composite ? p.isCompositeMandatory() : p.isMandatory()).//
		collect(Collectors.toList());
    }

    /**
     * @param dateTime
     * @return
     */
    private boolean parseDateTime(String dateTime) {

	DateTimeFormatter parser = ISODateTimeFormat.dateTimeParser();

	try {
	    parser.parseDateTime(dateTime);
	    return true;

	} catch (Exception ex) {

	}

	return false;
    }
}
