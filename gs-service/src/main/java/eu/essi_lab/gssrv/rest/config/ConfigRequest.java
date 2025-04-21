/**
 * 
 */
package eu.essi_lab.gssrv.rest.config;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.checkerframework.checker.units.qual.m;
import org.json.JSONObject;

import eu.essi_lab.model.Queryable.ContentType;

/**
 * @author Fabrizio
 */
public abstract class ConfigRequest {

    private JSONObject object;

    /**
     * 
     */
    public ConfigRequest(String name) {

	object = new JSONObject();

	object.put("name", name);

	object.put("parameters", new JSONObject());
    }

    /**
     * @param object
     */
    public ConfigRequest(JSONObject object) {

	this.object = object;
    }

    /**
     * @return
     */
    protected abstract List<Parameter> getSupportedParameters();

    /**
     * @return
     */
    private List<String> readParameters() {

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

    /**
     * @return
     */
    public void validate() {

	if (!object.has("parameters")) {

	    throw new IllegalArgumentException("Missing 'parameters' object");
	}

	//
	// mandatory parameters check
	//

	List<String> mandatoryParams = getSupportedParameters().//
		stream().//
		filter(p -> p.isMandatory()).//
		map(p -> p.getName()).//
		collect(Collectors.toList());

	mandatoryParams.removeAll(readParameters());
	if (!mandatoryParams.isEmpty()) {

	    throw new IllegalArgumentException(
		    "Missing mandatory parameters: " + mandatoryParams.stream().collect(Collectors.joining(",")));
	}

	//
	// supported parameters check
	//

	List<String> supported = getSupportedParameters().//
		stream().//
		map(p -> p.getName()).//
		collect(Collectors.toList());

	List<String> requestParams = readParameters();
	requestParams.removeAll(supported);

	if (!requestParams.isEmpty()) {

	    throw new IllegalArgumentException("Unknown parameters: " + requestParams.stream().collect(Collectors.joining(","))
		    + ". Supported parameters: " + supported.stream().collect(Collectors.joining(",")));
	}

	//
	// values check
	//

	requestParams.forEach(paramName -> {

	    Object value = readValue(paramName);

	    ContentType type = getSupportedParameters().//
		    stream().//
		    filter(p -> p.getName().equals(paramName)).//
		    findFirst().//
		    get().//
		    getType();

	    switch (type) {
	    case BOOLEAN:

		break;

	    case DOUBLE:

		break;

	    case INTEGER:

		break;

	    case ISO8601_DATE_TIME:

		break;

	    case LONG:

		break;

	    case TEXTUAL:

		break;
	    }

	});

    }

    /**
     * @return
     */
    public String getName() {

	return object.getString("name");
    }

    /**
     * @param paramName
     * @param value
     */
    public void put(String paramName, String value) {

	object.getJSONObject("parameters").put(paramName, value);
    }

    /**
     * @param paramName
     * @return
     */
    protected Optional<String> get(String paramName) {

	if (object.getJSONObject("parameters").has("name")) {

	    return Optional.ofNullable(object.getJSONObject("parameters").optString(paramName, null));
	}

	throw new IllegalArgumentException("Parameter");
    }
}
