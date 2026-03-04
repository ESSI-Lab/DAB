/**
 * 
 */
package eu.essi_lab.cfga.rest.ontology;

import eu.essi_lab.cfga.gs.setting.ontology.OntologySetting.*;
import eu.essi_lab.cfga.option.*;
import eu.essi_lab.cfga.rest.*;
import eu.essi_lab.model.Queryable.*;
import org.json.*;

import java.util.*;

/**
 * @author Fabrizio
 */
public class EditOntologyRequest extends PutOntologyRequest {

    /**
     * 
     */
    public EditOntologyRequest() {
    }

    /**
     * @param object
     */
    public EditOntologyRequest(JSONObject object) {

	super(object);
    }

    @Override
    public List<Parameter> getSupportedParameters() {

	ArrayList<Parameter> list = new ArrayList<>();

	list.add(Parameter.of(ONTOLOGY_ID, ContentType.TEXTUAL, InputPattern.ALPHANUMERIC_AND_UNDERSCORE, true));
	list.add(Parameter.of(ONTOLOGY_NAME, ContentType.TEXTUAL, false));
	list.add(Parameter.of(ONTOLOGY_ENDPOINT, ContentType.TEXTUAL, false));

	list.add(Parameter.of(ONTOLOGY_AVAILABILITY, ContentType.TEXTUAL, Availability.class, false));
	list.add(Parameter.of(ONTOLOGY_DESCRIPTION, ContentType.TEXTUAL, false));
	list.add(Parameter.of(ONTOLOGY_DATA_MODEL, ContentType.TEXTUAL, DataModel.class, false));
	list.add(Parameter.of(ONTOLOGY_QUERY_LANGUAGE, ContentType.TEXTUAL, QueryLanguage.class, false));

	return list;
    }

    /**
     * 
     */
    @Override
    protected void mandatoryCheck() {

	super.mandatoryCheck();

	if (readParameters().size() == 1) {

	    throw new IllegalArgumentException("At least one of the parameters '" + ONTOLOGY_NAME + "', '" + ONTOLOGY_DESCRIPTION + "', '"
		    + "', '" + ONTOLOGY_ENDPOINT + "', '" + ONTOLOGY_AVAILABILITY + "' must be provided'");
	}
    }

}
