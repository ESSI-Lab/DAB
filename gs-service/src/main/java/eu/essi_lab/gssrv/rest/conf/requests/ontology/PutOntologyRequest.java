/**
 * 
 */
package eu.essi_lab.gssrv.rest.conf.requests.ontology;

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

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import eu.essi_lab.cfga.gs.setting.ontology.OntologySetting.Availability;
import eu.essi_lab.cfga.gs.setting.ontology.OntologySetting.DataModel;
import eu.essi_lab.cfga.gs.setting.ontology.OntologySetting.QueryLanguage;
import eu.essi_lab.cfga.option.InputPattern;
import eu.essi_lab.gssrv.rest.conf.ConfigRequest;
import eu.essi_lab.gssrv.rest.conf.Parameter;
import eu.essi_lab.model.Queryable.ContentType;

/**
 * @author Fabrizio
 */
public class PutOntologyRequest extends ConfigRequest {

    public static final String ONTOLOGY_ID = "id";
    public static final String ONTOLOGY_NAME = "name";
    public static final String ONTOLOGY_ENDPOINT = "endpoint";
    public static final String ONTOLOGY_DESCRIPTION = "description";
    public static final String ONTOLOGY_AVAILABILITY = "availability";
    public static final String ONTOLOGY_DATA_MODEL = "dataModel";
    public static final String ONTOLOGY_QUERY_LANGUAGE = "queryLanguage";

   
    /**
     * 
     */
    public PutOntologyRequest() {
    }

    /**
     * @param object
     */
    public PutOntologyRequest(JSONObject object) {

	super(object);
    }

    @Override
    public List<Parameter> getSupportedParameters() {

	ArrayList<Parameter> list = new ArrayList<>();

	list.add(Parameter.of(ONTOLOGY_ID, ContentType.TEXTUAL, InputPattern.ALPHANUMERIC_AND_UNDERSCORE, true));
	list.add(Parameter.of(ONTOLOGY_NAME, ContentType.TEXTUAL, true));
	list.add(Parameter.of(ONTOLOGY_ENDPOINT, ContentType.TEXTUAL, true));

	list.add(Parameter.of(ONTOLOGY_AVAILABILITY, ContentType.TEXTUAL, Availability.class, false));
	list.add(Parameter.of(ONTOLOGY_DESCRIPTION, ContentType.TEXTUAL, false));
	list.add(Parameter.of(ONTOLOGY_DATA_MODEL, ContentType.TEXTUAL, DataModel.class, false));
	list.add(Parameter.of(ONTOLOGY_QUERY_LANGUAGE, ContentType.TEXTUAL, QueryLanguage.class, false));

	return list;
    }
}
