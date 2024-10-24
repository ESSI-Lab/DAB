package eu.essi_lab.profiler.semantic;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.stats.ResponseItem;
import eu.essi_lab.messages.stats.StatisticsMessage;
import eu.essi_lab.messages.stats.StatisticsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.index.jaxb.CardinalValues;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.pdk.wrt.WebRequestTransformer;
import eu.essi_lab.request.executor.IStatisticsExecutor;

public class SourceStatistics {

    private HashMap<String, Stats> statistics = new HashMap<>();

    public HashMap<String, Stats> getStatistics() {
	return statistics;
    }

    public SourceStatistics(String source, Optional<String> viewId, MetadataElement groupBy) throws Exception {
	StatisticsMessage statisticsMessage = new StatisticsMessage();
	List<GSSource> allSources = ConfigurationWrapper.getAllSources();
	// set the required properties
	statisticsMessage.setSources(allSources);
	statisticsMessage.setDataBaseURI(ConfigurationWrapper.getDatabaseURI());

	// set the view
	if (viewId.isPresent()) {

	    WebRequestTransformer.setView(//
		    viewId.get(), //
		    statisticsMessage.getDataBaseURI(), //
		    statisticsMessage);
	}

	// set the user bond
	 statisticsMessage.setUserBond(BondFactory.createSourceIdentifierBond(source));

	// groups by source id
	if (groupBy != null) {
	    statisticsMessage.groupBy(groupBy);
	}

	// pagination works with grouped results. in this case there is one result item for each source.
	// in order to be sure to get all the items in the same statistics response,
	// we set the count equals to number of sources
	Page page = new Page();
	page.setStart(1);
	page.setSize(1000);

	statisticsMessage.setPage(page);

	// computes union of bboxes
	statisticsMessage.computeBboxUnion();
	statisticsMessage.computeMin(Arrays.asList(MetadataElement.TEMP_EXTENT_BEGIN,MetadataElement.ALTITUDE));
	statisticsMessage.computeMax(Arrays.asList(MetadataElement.TEMP_EXTENT_END,MetadataElement.ALTITUDE));
	

	// computes count distinct of 2 queryables
	statisticsMessage.countDistinct(//
		Arrays.asList(//
			MetadataElement.ATTRIBUTE_TITLE, //
			MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER, //
			MetadataElement.UNIQUE_PLATFORM_IDENTIFIER, //
			MetadataElement.ONLINE_ID//
		));

	// statisticsMessage.computeSum(Arrays.asList(MetadataElement.DATA_SIZE));

	ServiceLoader<IStatisticsExecutor> loader = ServiceLoader.load(IStatisticsExecutor.class);
	IStatisticsExecutor executor = loader.iterator().next();

	StatisticsResponse response = executor.compute(statisticsMessage);
	List<ResponseItem> items = response.getItems();
	for (ResponseItem responseItem : items) {
	    Stats stats = new Stats();
	    stats.setSiteCount(responseItem.getCountDistinct(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER).get().getValue());
	    stats.setUniqueAttributeCount(responseItem.getCountDistinct(MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER).get().getValue());
	    stats.setAttributeCount(responseItem.getCountDistinct(MetadataElement.ATTRIBUTE_TITLE).get().getValue());
	    stats.setTimeSeriesCount(responseItem.getCountDistinct(MetadataElement.ONLINE_ID).get().getValue());
	    CardinalValues cardinalValues = responseItem.getBBoxUnion().getCardinalValues().get();

	    stats.setEast(Double.parseDouble(cardinalValues.getEast()));
	    stats.setNorth(Double.parseDouble(cardinalValues.getNorth()));
	    stats.setWest(Double.parseDouble(cardinalValues.getWest()));
	    stats.setSouth(Double.parseDouble(cardinalValues.getSouth()));
	    stats.setBegin(responseItem.getMin(MetadataElement.TEMP_EXTENT_BEGIN).get().getValue());
	    stats.setEnd(responseItem.getMax(MetadataElement.TEMP_EXTENT_END).get().getValue());
	    stats.setMinimumAltitude(responseItem.getMin(MetadataElement.ALTITUDE).get().getValue());
	    stats.setMaximumAltitude(responseItem.getMax(MetadataElement.ALTITUDE).get().getValue());
	    String id = responseItem.getGroupedBy().isPresent() ? responseItem.getGroupedBy().get() : null;
	    this.statistics.put(id, stats);
	}
    }

}
