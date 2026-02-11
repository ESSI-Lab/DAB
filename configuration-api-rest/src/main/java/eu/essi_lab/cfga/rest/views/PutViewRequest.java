package eu.essi_lab.cfga.rest.views;

import com.fasterxml.jackson.core.*;
import eu.essi_lab.api.database.*;
import eu.essi_lab.api.database.factory.*;
import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.cfga.gs.setting.database.*;
import eu.essi_lab.cfga.rest.*;
import eu.essi_lab.cfga.rest.source.*;
import eu.essi_lab.messages.bond.*;
import eu.essi_lab.messages.bond.jaxb.*;
import eu.essi_lab.model.exceptions.*;
import org.json.*;

import java.util.*;

/**
 * @author Fabrizio
 */
public class PutViewRequest extends PutSourceRequest {

    /**
     *
     */
    public static String VIEW = "view";

    /**
     *
     */
    public PutViewRequest() {
    }

    /**
     * @param object
     */
    public PutViewRequest(JSONObject object) {

	super(object);
    }

    @Override
    public List<Parameter> getSupportedParameters() {

	ArrayList<Parameter> list = new ArrayList<>();

	list.add(Parameter.of(VIEW, true));

	return list;
    }

    @Override
    public void validate() {

	Optional<String> stringValue = read(VIEW).map(Object::toString);

	if (stringValue.isEmpty()) {

	    throw new IllegalArgumentException("Missing mandatory 'view' payload");
	}

	JSONObject viewPayload = new JSONObject(stringValue.get());

	try {
	    ViewFactory.fromJSONObject(viewPayload);

	} catch (JsonProcessingException ex) {

	    throw new IllegalArgumentException("Invalid JSON encoding: " + ex.getMessage());

	} catch (Exception e) {

	    throw new IllegalArgumentException("Error occurred: " + e.getMessage());
	}
    }

}
