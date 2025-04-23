/**
 * 
 */
package eu.essi_lab.gssrv.rest.conf;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import eu.essi_lab.cfga.option.InputPattern;
import eu.essi_lab.lib.utils.LabeledEnum;
import eu.essi_lab.model.Queryable.ContentType;

/**
 * @author Fabrizio
 */
public class PutSourceRequest extends ConfigRequest {

    public static final String SOURCE_ID = "sourceId";
    public static final String SOURCE_LABEL = "sourceLabel";
    public static final String SOURCE_ENDPOINT = "sourceEndpoint";
    public static final String SOURCE_TYPE = "sourceType";

    /**
     * @author Fabrizio
     */
    public enum SourceType implements LabeledEnum {

	/**
	 * 
	 */
	CSW("CSW"),
	/**
	 * 
	 */
	WMS("WMS"),
	/**
	 * 
	 */
	WCS("WCS"),
	/**
	 * 
	 */
	OAI_PMH("OAI-PMH");

	private String label;

	/**
	 * @param label
	 */
	private SourceType(String label) {

	    this.label = label;
	}

	@Override
	public String getLabel() {

	    return label;
	}

	@Override
	public String toString() {

	    return getLabel();
	}
    }

    /**
     * 
     */
    public PutSourceRequest() {

	this("PutSourceRequest");
    }

    /**
     * 
     */
    public PutSourceRequest(String name) {

	super(name);
    }

    /**
     * @param object
     */
    public PutSourceRequest(JSONObject object) {

	super(object);
    }

    @Override
    public List<Parameter> getSupportedParameters() {

	ArrayList<Parameter> list = new ArrayList<>();

	list.add(Parameter.of(SOURCE_ID, ContentType.TEXTUAL, InputPattern.ALPHANUMERIC_AND_UNDERSCORE, false));
	list.add(Parameter.of(SOURCE_LABEL, ContentType.TEXTUAL, true));
	list.add(Parameter.of(SOURCE_ENDPOINT, ContentType.TEXTUAL, true));
	list.add(Parameter.of(SOURCE_TYPE, ContentType.TEXTUAL, SourceType.class, true));

	return list;
    }
}
