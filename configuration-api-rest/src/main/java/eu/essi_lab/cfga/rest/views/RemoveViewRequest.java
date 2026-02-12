package eu.essi_lab.cfga.rest.views;

import eu.essi_lab.cfga.option.*;
import eu.essi_lab.cfga.rest.*;
import eu.essi_lab.model.*;
import org.json.*;

import java.util.*;

/**
 * @author Fabrizio
 */
public class RemoveViewRequest extends ConfigRequest {

    /**
     *
     */
    public static String VIEW_ID = "id";

    /**
     *
     */
    public RemoveViewRequest() {
    }

    /**
     * @param object
     */
    public RemoveViewRequest(JSONObject object) {

	super(object);
    }

    @Override
    protected List<Parameter> getSupportedParameters() {

	ArrayList<Parameter> list = new ArrayList<>();

	list.add(Parameter.of(VIEW_ID, Queryable.ContentType.TEXTUAL, InputPattern.ALPHANUMERIC_AND_UNDERSCORE_AND_MINUS, true));

	return list;
    }
}


