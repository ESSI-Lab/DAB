package eu.essi_lab.profiler.oaipmh.handler.srvinfo;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.ServiceLoader;

import eu.essi_lab.configuration.ConfigurationUtils;
import eu.essi_lab.jaxb.oaipmh.ListSetsType;
import eu.essi_lab.jaxb.oaipmh.OAIPMHtype;
import eu.essi_lab.jaxb.oaipmh.SetType;
import eu.essi_lab.jaxb.oaipmh.VerbType;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.stats.ResponseItem;
import eu.essi_lab.messages.stats.StatisticsMessage;
import eu.essi_lab.messages.stats.StatisticsResponse;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.pdk.wrt.WebRequestTransformer;
import eu.essi_lab.request.executor.IStatisticsExecutor;
public class OAIPMHListSetsHandler extends OAIPMHServiceInfoHandler {

    @Override
    protected OAIPMHtype createResponseElement(WebRequest webRequest) throws GSException {

	StatisticsMessage statisticsMessage = new StatisticsMessage();

	List<GSSource> allSources = ConfigurationUtils.getAllSources();

	// set the required properties
	statisticsMessage.setSources(allSources);
	statisticsMessage.setDataBaseURI(ConfigurationUtils.getStorageURI());
	// statisticsMessage.setSharedRepositoryInfo(ConfigurationUtils.getSharedRepositoryInfo());
	statisticsMessage.setWebRequest(webRequest);

	Optional<String> viewId = extractViewId(webRequest);

	// set the view
	if (viewId.isPresent()) {

	    WebRequestTransformer.setView(//
		    viewId.get(), //
		    statisticsMessage.getDataBaseURI(), //
		    statisticsMessage);
	}

	// groups by source id
	statisticsMessage.groupBy(ResourceProperty.SOURCE_ID);

	// pagination works with grouped results. in this case there is one result item for each source.
	// in order to be sure to get all the items in the same statistics response,
	// we set the count equals to number of sources
	Page page = new Page();
	page.setStart(1);
	page.setSize(allSources.size());

	statisticsMessage.setPage(page);

	IStatisticsExecutor executor = getStatisticsExecutor();

	StatisticsResponse response = executor.compute(statisticsMessage);

	List<ResponseItem> items = response.getItems();

	ListSetsType listSets = new ListSetsType();

	for (ResponseItem item : items) {

	    String sourceId = item.getGroupedBy().get();

	    GSSource gsSource = allSources.stream().//
		    filter(s -> s.getUniqueIdentifier().equals(sourceId)).//
		    findFirst().//
		    get();

	    if (gsSource.getBrokeringStrategy() == BrokeringStrategy.HARVESTED) {

		SetType setType = new SetType();
		setType.setSetName(gsSource.getLabel());
		setType.setSetSpec(gsSource.getUniqueIdentifier());

		listSets.getSet().add(setType);
	    }
	}

	OAIPMHtype oai = new OAIPMHtype();
	oai.setListSets(listSets);

	return oai;
    }
    
    /**
     * 
     * @param webRequest
     * @return
     */
    protected Optional<String> extractViewId(WebRequest webRequest) {
	
	return webRequest.extractViewId();
    }

    protected IStatisticsExecutor getStatisticsExecutor() {
	ServiceLoader<IStatisticsExecutor> loader = ServiceLoader.load(IStatisticsExecutor.class);
	IStatisticsExecutor ret = loader.iterator().next();
	return ret;
    }

    @Override
    protected VerbType getVerbType() {

	return VerbType.LIST_SETS;
    }

}
