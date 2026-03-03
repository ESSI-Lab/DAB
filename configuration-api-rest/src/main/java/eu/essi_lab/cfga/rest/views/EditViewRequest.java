package eu.essi_lab.cfga.rest.views;

import org.json.*;

/**
 * @author Fabrizio
 */
public class EditViewRequest extends PutViewRequest {

    /**
     *
     */
    public EditViewRequest() {
    }

    /**
     * @param object
     */
    public EditViewRequest(JSONObject object) {

	super(object);
    }
}
