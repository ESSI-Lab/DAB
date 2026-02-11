package eu.essi_lab.cfga.rest.views;

import eu.essi_lab.cfga.rest.*;
import eu.essi_lab.cfga.rest.source.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.model.*;
import org.json.*;

import java.util.*;

/**
 *
 * @author Fabrizio
 *
 */
public class ListBondQLPropertiesRequest extends PutSourceRequest {

    /**
     *
     */
    public static final String ENUM_NAME = "enumName";

    /**
     *
     * @author Fabrizio
     *
     */
    public enum EnumName implements LabeledEnum {

	/**
	 *
	 */
	RESOURCE_PROPERTY("ResourceProperty"),

	/**
	 *
	 */
	METADATA_ELEMENT("MetadataElement");

	private String label;

	/**
	 * @param label
	 */
	private EnumName(String label) {

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
    public ListBondQLPropertiesRequest() {
    }

    /**
     * @param object
     */
    public ListBondQLPropertiesRequest(JSONObject object) {

	super(object);
    }

    @Override
    public List<Parameter> getSupportedParameters() {

	ArrayList<Parameter> list = new ArrayList<>();

	Parameter parameter = Parameter.of(ENUM_NAME, Queryable.ContentType.TEXTUAL, EnumName.class, false);
	parameter.setMultiValue();

	list.add(parameter);

	return list;
    }
}
