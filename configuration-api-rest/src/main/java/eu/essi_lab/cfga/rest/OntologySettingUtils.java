/**
 * 
 */
package eu.essi_lab.cfga.rest;

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

import eu.essi_lab.cfga.*;
import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.cfga.gs.setting.ontology.*;
import eu.essi_lab.cfga.gs.setting.ontology.OntologySetting.*;
import eu.essi_lab.cfga.rest.ontology.*;
import eu.essi_lab.cfga.setting.*;
import eu.essi_lab.lib.utils.*;

import javax.ws.rs.core.Response.*;
import java.util.*;

/**
 * @author Fabrizio
 */
public class OntologySettingUtils {

    /**
     * @param request
     * @param ontologySetting
     * @return
     */
    public static OntologySetting build(EditOntologyRequest request, OntologySetting setting) {

	OntologySetting out = SettingUtils.downCast(SelectionUtils.resetAndSelect(setting, false), OntologySetting.class);

	request.readString(PutOntologyRequest.ONTOLOGY_ENDPOINT).ifPresent(out::setOntologyEndpoint);

	request.readString(PutOntologyRequest.ONTOLOGY_NAME).ifPresent(out::setOntologyName);

	request.readString(PutOntologyRequest.ONTOLOGY_AVAILABILITY)
		.ifPresent(v -> out.setOntologyAvailability(LabeledEnum.valueOf(Availability.class, v).get()));

	request.readString(PutOntologyRequest.ONTOLOGY_DESCRIPTION).ifPresent(out::setOntologyDescription);

	setting.clean();
	setting.afterClean();

	return out;
    }

    /**
     * @param request
     * @return
     */
    public static OntologySetting build(PutOntologyRequest request) {

	OntologySetting setting = new OntologySetting();

	String id = request.readString(PutOntologyRequest.ONTOLOGY_ID).get();
	String endpoint = request.readString(PutOntologyRequest.ONTOLOGY_ENDPOINT).get();
	String name = request.readString(PutOntologyRequest.ONTOLOGY_NAME).get();

	String availability = request.readString(PutOntologyRequest.ONTOLOGY_AVAILABILITY)
		.orElse(Availability.ENABLED.getLabel());

	String queryLang = request.readString(PutOntologyRequest.ONTOLOGY_QUERY_LANGUAGE)
		.orElse(QueryLanguage.SPARQL.getLabel());

	String dataModel = request.readString(PutOntologyRequest.ONTOLOGY_DATA_MODEL).
		orElse(DataModel.SKOS.getLabel());

	Optional<String> optDesc = request.readString(PutOntologyRequest.ONTOLOGY_DESCRIPTION);

	setting.setOntologyId(id);
	setting.setOntologyName(name);
	setting.setOntologyEndpoint(endpoint);
	setting.setOntologyAvailability(LabeledEnum.valueOf(Availability.class, availability).get());

	setting.setQueryLanguage(LabeledEnum.valueOf(QueryLanguage.class, queryLang).get());
	setting.setDataModel(LabeledEnum.valueOf(DataModel.class, dataModel).get());

	optDesc.ifPresent(setting::setOntologyDescription);

	setting.clean();
	setting.afterClean();

	return setting;
    }

    /**
     * @param request
     * @return
     */
    public static SettingFinder<OntologySetting> getOntologySettingFinder(ConfigRequest request) {

	Optional<String> optSourceId = request.readString(PutOntologyRequest.ONTOLOGY_ID);

	OntologySetting setting;

	if (optSourceId.isEmpty()) {

	    return new SettingFinder<>(ConfigRequest.buildErrorResponse(Status.METHOD_NOT_ALLOWED, "Missing ontology identifier"));

	} else {

	    String ontologyId = optSourceId.get();

	    if (ConfigurationWrapper.getOntologySettings().//
		    stream().//
		    noneMatch(s -> s.getOntologyId().equals(ontologyId))) {

		return new SettingFinder<>(
			ConfigRequest.buildErrorResponse(Status.NOT_FOUND, "Ontology with id '" + ontologyId + "' not found"));
	    }

	    setting = ConfigurationWrapper.getOntologySettings().//
		    stream().//
		    filter(
	s -> s.getOntologyId().equals(ontologyId)).//
		    findFirst().//
		    get();
	}

	return new SettingFinder<>(setting);
    }

}
