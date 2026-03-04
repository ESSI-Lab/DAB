package eu.essi_lab.cfga.rest.views;

import eu.essi_lab.cfga.option.*;
import eu.essi_lab.cfga.rest.*;
import eu.essi_lab.model.*;
import org.json.*;

import java.util.*;

/**
 *
 * @author Fabrizio
 *
 */
public class ListViewsRequest extends ConfigRequest {

    /**
     *
     */
    public static String VIEW_ID = "id";

    /**
     *
     */
    public ListViewsRequest() {
    }

    /**
     * @param object
     */
    public ListViewsRequest(JSONObject object) {

	super(object);
    }

    @Override
    public List<Parameter> getSupportedParameters() {

	ArrayList<Parameter> list = new ArrayList<>();

	Parameter parameter = Parameter.of(VIEW_ID, Queryable.ContentType.TEXTUAL, InputPattern.ALPHANUMERIC_AND_UNDERSCORE_AND_MINUS, false);
	parameter.setMultiValue();

	list.add(parameter);

	return list;
    }
}
