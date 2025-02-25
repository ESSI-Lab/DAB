package eu.essi_lab.profiler.oaipmh.handler.srvinfo;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.jaxb.oaipmh.ListSetsType;
import eu.essi_lab.jaxb.oaipmh.OAIPMHtype;
import eu.essi_lab.jaxb.oaipmh.SetType;
import eu.essi_lab.jaxb.oaipmh.VerbType;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;

/**
 * Specific handler for the OAIPMH ListSets request
 * 
 * @author Fabrizio
 */
public class OAIPMHListSetsHandler extends OAIPMHServiceInfoHandler {

    @Override
    protected OAIPMHtype createResponseElement(WebRequest webRequest) throws GSException {

	Optional<String> viewId = extractViewId(webRequest);

	Optional<View> view = Optional.empty();

	if (viewId.isPresent()) {

	    view = DiscoveryRequestTransformer.findView(ConfigurationWrapper.getStorageInfo(), viewId.get());
	}

	List<GSSource> list = view.isPresent() ? //
		ConfigurationWrapper.getViewSources(view.get()).stream().collect(Collectors.toList()) : //
		ConfigurationWrapper.getHarvestedAndMixedSources().stream().collect(Collectors.toList());

	ListSetsType listSets = new ListSetsType();

	for (GSSource source : list) {

	    SetType setType = new SetType();
	    setType.setSetName(source.getLabel());
	    setType.setSetSpec(source.getUniqueIdentifier());

	    listSets.getSet().add(setType);
	}

	OAIPMHtype oai = new OAIPMHtype();
	oai.setListSets(listSets);

	return oai;
    }

    /**
     * @param webRequest
     * @return
     */
    protected Optional<String> extractViewId(WebRequest webRequest) {

	return webRequest.extractViewId();
    }

    @Override
    protected VerbType getVerbType() {

	return VerbType.LIST_SETS;
    }

}
