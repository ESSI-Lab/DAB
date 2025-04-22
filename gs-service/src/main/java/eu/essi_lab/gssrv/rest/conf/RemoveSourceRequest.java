/**
 * 
 */
package eu.essi_lab.gssrv.rest.conf;

import java.util.ArrayList;
import java.util.List;

import eu.essi_lab.cfga.option.InputPattern;
import eu.essi_lab.model.Queryable.ContentType;

/**
 * @author Fabrizio
 */
public class RemoveSourceRequest extends ConfigRequest {

    /**
     * 
     */
    public RemoveSourceRequest() {

	super("RemoveSourceRequest");
    }

    @Override
    protected List<Parameter> getSupportedParameters() {

	ArrayList<Parameter> list = new ArrayList<>();

	list.add(Parameter.of("sourceId", ContentType.TEXTUAL, InputPattern.ALPHANUMERIC_AND_UNDERSCORE, true));
	list.add(Parameter.of("removeData", ContentType.BOOLEAN, false));

	return list;
    }
}
