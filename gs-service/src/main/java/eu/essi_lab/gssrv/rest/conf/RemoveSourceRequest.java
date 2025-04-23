/**
 * 
 */
package eu.essi_lab.gssrv.rest.conf;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import eu.essi_lab.cfga.option.InputPattern;
import eu.essi_lab.model.Queryable.ContentType;

/**
 * @author Fabrizio
 */
public class RemoveSourceRequest extends PutSourceRequest {

    public static final String REMOVE_DATA = "removeData";

    /**
     * 
     */
    public RemoveSourceRequest() {

	super("RemoveSourceRequest");
    }

    /**
     * 
     */
    public RemoveSourceRequest(String name) {

	super(name);
    }

    /**
     * @param object
     */
    public RemoveSourceRequest(JSONObject object) {

	super(object);
    }

    @Override
    public List<Parameter> getSupportedParameters() {

	ArrayList<Parameter> list = new ArrayList<>();

	list.add(Parameter.of(SOURCE_ID, ContentType.TEXTUAL, InputPattern.ALPHANUMERIC_AND_UNDERSCORE, true));
	list.add(Parameter.of(REMOVE_DATA, ContentType.BOOLEAN, false));

	return list;
    }
}
