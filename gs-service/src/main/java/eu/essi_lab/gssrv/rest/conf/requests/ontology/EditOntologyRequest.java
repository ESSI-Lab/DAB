/**
 * 
 */
package eu.essi_lab.gssrv.rest.conf.requests.ontology;

import java.util.ArrayList;
import java.util.List;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import org.json.JSONObject;

import eu.essi_lab.cfga.gs.setting.ontology.OntologySetting.Availability;
import eu.essi_lab.cfga.gs.setting.ontology.OntologySetting.DataModel;
import eu.essi_lab.cfga.gs.setting.ontology.OntologySetting.QueryLanguage;
import eu.essi_lab.cfga.option.InputPattern;
import eu.essi_lab.gssrv.rest.conf.Parameter;
import eu.essi_lab.model.Queryable.ContentType;

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
