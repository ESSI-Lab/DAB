/**
 * 
 */
package eu.essi_lab.gssrv.rest.conf;

import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONObject;

/**
 * @author Fabrizio
 */
public class EditSourceRequest extends PutSourceRequest {

    /**
     * 
     */
    public EditSourceRequest() {

	super("EditSourceRequest");
    }

    /**
     * 
     */
    public EditSourceRequest(String name) {

	super(name);
    }

    /**
     * @param object
     */
    public EditSourceRequest(JSONObject object) {

	super(object);
    }

    @Override
    public List<Parameter> getSupportedParameters() {

	List<Parameter> supportedParameters = super.getSupportedParameters();

	return supportedParameters.//
		stream().// sourceId is mandatory in this request
		map(p -> p.getName().equals("sourceId") ? Parameter.of(p.getName(), p.getContentType(), p.getInputPattern().get(), true)
			: p)
		.//
		collect(Collectors.toList());
    }
}
