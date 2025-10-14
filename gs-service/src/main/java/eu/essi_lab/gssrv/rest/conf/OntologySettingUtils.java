/**
 * 
 */
package eu.essi_lab.gssrv.rest.conf;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.Optional;

import javax.ws.rs.core.Response.Status;

import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.OntologySetting;
import eu.essi_lab.cfga.gs.setting.OntologySetting.Availability;
import eu.essi_lab.cfga.gs.setting.OntologySetting.DataModel;
import eu.essi_lab.cfga.gs.setting.OntologySetting.QueryLanguage;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.gssrv.rest.conf.ConfigService.SettingFinder;
import eu.essi_lab.gssrv.rest.conf.requests.ontology.EditOntologyRequest;
import eu.essi_lab.gssrv.rest.conf.requests.ontology.PutOntologyRequest;
import eu.essi_lab.lib.utils.LabeledEnum;

/**
 * @author Fabrizio
 */
public class OntologySettingUtils {

    /**
     * @param request
     * @param ontologySetting
     * @return
     */
    static OntologySetting build(EditOntologyRequest request, OntologySetting setting) {

	OntologySetting out = SettingUtils.downCast(SelectionUtils.resetAndSelect(setting, false), OntologySetting.class);

	request.read(PutOntologyRequest.ONTOLOGY_ENDPOINT).ifPresent(v -> out.setOntologyEndpoint(v.toString()));

	request.read(PutOntologyRequest.ONTOLOGY_NAME).ifPresent(v -> out.setOntologyName(v.toString()));

	request.read(PutOntologyRequest.ONTOLOGY_AVAILABILITY)
		.ifPresent(v -> out.setOntologyAvailability(LabeledEnum.valueOf(Availability.class, v.toString()).get()));

	request.read(PutOntologyRequest.ONTOLOGY_DESCRIPTION).ifPresent(v -> out.setOntologyDescription(v.toString()));

	setting.clean();
	setting.afterClean();

	return out;
    }

    /**
     * @param request
     * @return
     */
    static OntologySetting build(PutOntologyRequest request) {

	OntologySetting setting = new OntologySetting();

	String id = request.read(PutOntologyRequest.ONTOLOGY_ID).get().toString();
	String endpoint = request.read(PutOntologyRequest.ONTOLOGY_ENDPOINT).get().toString();
	String name = request.read(PutOntologyRequest.ONTOLOGY_NAME).get().toString();

	String availability = request.read(PutOntologyRequest.ONTOLOGY_AVAILABILITY).map(v -> v.toString())
		.orElse(Availability.ENABLED.getLabel());

	String queryLang = request.read(PutOntologyRequest.ONTOLOGY_QUERY_LANGUAGE).map(v -> v.toString())
		.orElse(QueryLanguage.SPARQL.getLabel());

	String dataModel = request.read(PutOntologyRequest.ONTOLOGY_DATA_MODEL).map(v -> v.toString()).orElse(DataModel.SKOS.getLabel());

	Optional<String> optDesc = request.read(PutOntologyRequest.ONTOLOGY_DESCRIPTION).map(v -> v.toString());

	setting.setOntologyId(id);
	setting.setOntologyName(name);
	setting.setOntologyEndpoint(endpoint);
	setting.setOntologyAvailability(LabeledEnum.valueOf(Availability.class, availability).get());

	setting.setQueryLanguage(LabeledEnum.valueOf(QueryLanguage.class, queryLang).get());
	setting.setDataModel(LabeledEnum.valueOf(DataModel.class, dataModel).get());

	optDesc.ifPresent(desc -> setting.setOntologyDescription(desc));

	setting.clean();
	setting.afterClean();

	return setting;
    }

    /**
     * @param request
     * @return
     */
    static SettingFinder<OntologySetting> getOntologySettingFinder(ConfigRequest request) {

	Optional<String> optSourceId = request.read(PutOntologyRequest.ONTOLOGY_ID).map(v -> v.toString());

	OntologySetting setting = null;

	if (!optSourceId.isPresent()) {

	    return new SettingFinder<OntologySetting>(
		    ConfigService.buildErrorResponse(Status.METHOD_NOT_ALLOWED, "Missing ontology identifier"));

	} else {

	    String ontologyId = optSourceId.get();

	    if (!ConfigurationWrapper.getOntologySettings().//
		    stream().//
		    filter(s -> s.getOntologyId().equals(ontologyId)).//
		    findFirst().//
		    isPresent()) {

		return new SettingFinder<OntologySetting>(
			ConfigService.buildErrorResponse(Status.NOT_FOUND, "Ontology with id '" + ontologyId + "' not found"));
	    }

	    setting = ConfigurationWrapper.getOntologySettings().//
		    stream().//
		    filter(s -> s.getOntologyId().equals(ontologyId)).//
		    findFirst().//
		    get();
	}

	return new SettingFinder<OntologySetting>(setting);
    }

}
