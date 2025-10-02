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

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.OntologySetting;
import eu.essi_lab.gssrv.rest.conf.ConfigService.SettingFinder;
import eu.essi_lab.gssrv.rest.conf.requests.ontology.PutOntologyRequest;

/**
 * @author Fabrizio
 */
public class OntologySettingUtils {

    /**
     * @param request
     * @return
     */
    static OntologySetting build(PutOntologyRequest request) {

	OntologySetting setting = new OntologySetting();

	String id = request.read(PutOntologyRequest.ONTOLOGY_ID).get().toString();
	String endpoint = request.read(PutOntologyRequest.ONTOLOGY_ENDPOINT).get().toString();
	String name = request.read(PutOntologyRequest.ONTOLOGY_NAME).get().toString();
	boolean enabled = request.read(PutOntologyRequest.ONTOLOGY_AVAILABILITY).get().toString().equals("true");

	String queryLang = request.read(PutOntologyRequest.ONTOLOGY_QUERY_LANGUAGE).get().toString();
	String dataModel = request.read(PutOntologyRequest.ONTOLOGY_DATA_MODEL).get().toString();

	Optional<String> optDesc = request.read(PutOntologyRequest.ONTOLOGY_DESCRIPTION).map(v -> v.toString());

	setting.setOntologyId(id);
	setting.setOntologyName(name);
	setting.setOntologyEndpoint(endpoint);
	setting.setOntolgyEnabled(enabled);

	setting.setQueryLanguage(queryLang);
	setting.setDataModel(dataModel);

	optDesc.ifPresent(desc -> setting.setOntologyDescription(desc));

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
