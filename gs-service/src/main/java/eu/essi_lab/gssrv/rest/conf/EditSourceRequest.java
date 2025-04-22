/**
 * 
 */
package eu.essi_lab.gssrv.rest.conf;

import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    protected List<Parameter> getSupportedParameters() {

	List<Parameter> supportedParameters = super.getSupportedParameters();

	return supportedParameters.//
		stream().// sourceId is mandatory in this request
		map(p -> p.getName().equals("sourceId") ? Parameter.of(p.getName(), p.getContentType(), p.getInputPattern().get(), true)
			: p)
		.//
		collect(Collectors.toList());
    }
}
