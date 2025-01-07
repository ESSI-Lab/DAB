/**
 * 
 */
package eu.essi_lab.profiler.arpa.rest;

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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.DynamicViewSource;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.stats.ComputationResult;
import eu.essi_lab.messages.stats.ResponseItem;
import eu.essi_lab.messages.stats.StatisticsMessage;
import eu.essi_lab.messages.stats.StatisticsResponse;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.index.jaxb.CardinalValues;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.pdk.handler.DefaultRequestHandler;
import eu.essi_lab.request.executor.IStatisticsExecutor;

/**
 * @author boldrini
 */
public class ARPARESTSourcesHandler extends DefaultRequestHandler {

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	ARPARESTValidator validator = new ARPARESTValidator();

	return validator.validate(request);
    }

    @Override
    public String getStringResponse(WebRequest webRequest) throws GSException {

	StorageInfo databaseURI = ConfigurationWrapper.getDatabaseURI();
	DatabaseReader reader = DatabaseProviderFactory.getReader(databaseURI);

	JSONArray ret = new JSONArray();

	List<GSSource> sources = ConfigurationWrapper.getAllSources();
	for (int i = 0; i < sources.size(); i++) {

	    GSSource gsSource = sources.get(i);
	    String sourceId = gsSource.getUniqueIdentifier();

	    JSONObject sourceObject = new JSONObject();
	    //
	    // creates the bonds
	    //
	    Set<Bond> operands = new HashSet<>();

	    // we are interested only on downloadable datasets
	    ResourcePropertyBond accessBond = BondFactory.createIsExecutableBond(true);
	    operands.add(accessBond);

	    // we are interested only on downloadable datasets
	    ResourcePropertyBond downBond = BondFactory.createIsDownloadableBond(true);
	    operands.add(downBond);

	    // we are interested only on TIME SERIES datasets
	    ResourcePropertyBond timeSeriesBond = BondFactory.createIsTimeSeriesBond(true);
	    operands.add(timeSeriesBond);

	    LogicalBond andBond = BondFactory.createAndBond(operands);

	    //
	    // creates the message
	    //
	    StatisticsMessage statisticsMessage = new StatisticsMessage();

	    List<GSSource> allSources = ConfigurationWrapper.getAllSources();

	    // set the required properties
	    statisticsMessage.setSources(allSources);
	    statisticsMessage.setDataBaseURI(ConfigurationWrapper.getDatabaseURI());
	    statisticsMessage.setWebRequest(webRequest);

	    // set the user bond
	    statisticsMessage.setUserBond(andBond);

	    // pagination works with grouped results. in this case there is one result item for each source.
	    // in order to be sure to get all the items in the same statistics response,
	    // we set the count equals to number of sources
	    Page page = new Page();
	    page.setStart(1);
	    page.setSize(1);

	    statisticsMessage.setPage(page);

	    // computes union of bboxes
	    statisticsMessage.computeBboxUnion();

	    statisticsMessage.computeMin(Arrays.asList(MetadataElement.TEMP_EXTENT_BEGIN));
	    statisticsMessage.computeMax(Arrays.asList(MetadataElement.TEMP_EXTENT_END));
	    // statisticsMessage.computeTempExtentUnion();

	    // computes count distinct of 2 queryables
	    statisticsMessage.countDistinct(//
		    Arrays.asList(//
			    MetadataElement.ONLINE_ID, //
			    MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER, //
			    MetadataElement.UNIQUE_PLATFORM_IDENTIFIER));

	    statisticsMessage.computeSum(Arrays.asList(MetadataElement.DATA_SIZE));

	    String now = ISO8601DateTimeUtils.getISO8601DateTime();

	    String baseURL = "";
	    String servicesURL = "";
	    try {
		UriInfo uri = webRequest.getUriInfo();
		servicesURL = uri.getBaseUri().toString();

		baseURL = servicesURL.replace("services/essi", "");

		servicesURL += "/" + WebRequest.VIEW_PATH + "/";

	    } catch (Exception e) {
	    }

	    GSLoggerFactory.getLogger(getClass()).info("Calculating stats for source (" + i + "/" + sources.size() + "): " + sourceId);

	    DynamicViewSource view = new DynamicViewSource(sourceId);

	    statisticsMessage.setView(view);

	    ServiceLoader<IStatisticsExecutor> loader = ServiceLoader.load(IStatisticsExecutor.class);
	    IStatisticsExecutor executor = loader.iterator().next();

	    StatisticsResponse response = executor.compute(statisticsMessage);

	    List<ResponseItem> items = response.getItems();

	    if (!items.isEmpty()) {

		ResponseItem responseItem = items.get(0);
		String timeSeriesCount = responseItem.getCountDistinct(MetadataElement.ONLINE_ID).get().getValue();

		if (timeSeriesCount != null && !timeSeriesCount.isEmpty() && !timeSeriesCount.equals("0")) {

		    String siteCount = responseItem.getCountDistinct(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER).get().getValue();
		    String varCount = responseItem.getCountDistinct(MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER).get().getValue();
		    String valueCount = responseItem.getSum(MetadataElement.DATA_SIZE).get().getValue();
		    if (valueCount == null || valueCount.isEmpty() || valueCount.equals("0")) {
			valueCount = "100";
		    }

		    Optional<CardinalValues> optionalCardinals = responseItem.getBBoxUnion().getCardinalValues();
		    if (optionalCardinals.isPresent()) {
			CardinalValues cardinalValues = optionalCardinals.get();
			JSONObject extentObject = new JSONObject();
			extentObject.put("south", cardinalValues.getSouth());
			extentObject.put("west", cardinalValues.getWest());
			extentObject.put("north", cardinalValues.getNorth());
			extentObject.put("east", cardinalValues.getEast());
			sourceObject.put("extent", extentObject);

		    }

		    // ComputationResult optionalTempextent = responseItem.getTempExtentUnion();
		    Optional<ComputationResult> begin = responseItem.getMin(MetadataElement.TEMP_EXTENT_BEGIN);
		    if (begin.isPresent()) {
			sourceObject.put("time_begin", begin.get().getValue());
		    }
		    Optional<ComputationResult> end = responseItem.getMax(MetadataElement.TEMP_EXTENT_END);
		    if (end.isPresent()) {
			String value = end.get().getValue();
			if (value != null && !value.equals("") && !value.equals("null")) {
			    sourceObject.put("time_end", value);
			} else {
			    sourceObject.put("time_end", now);
			}
		    } else {
			sourceObject.put("time_end", now);
		    }

		    String viewIdentifier = view.getId();

		    sourceObject.put("time_series_count", timeSeriesCount);
		    sourceObject.put("site_count", siteCount);
		    sourceObject.put("variable_count", varCount);
		    sourceObject.put("value_count", valueCount);

		    sourceObject.put("CUAHSI HIS Central", servicesURL + viewIdentifier + "/" + ARPAUtils.getHISCentralPath());
		    sourceObject.put("Hydro Server", servicesURL + viewIdentifier + "/" + ARPAUtils.getHydroServerPath());
		    sourceObject.put("OGC SOS", servicesURL + viewIdentifier + "/" + ARPAUtils.getSOSPath());
		    sourceObject.put("HYDRO CSV", servicesURL + viewIdentifier + "/" + ARPAUtils.getHydroCSVPath());
		    sourceObject.put("GI-Portal", baseURL + "search?view=" + viewIdentifier);

		    sourceObject.put("id", sourceId);
		    sourceObject.put("title", gsSource.getLabel());
		    sourceObject.put("endpoint", gsSource.getEndpoint());

		    ret.put(sourceObject);

		}

	    }

	    // long end = System.currentTimeMillis();
	    // long time = end - begin;
	    // GSLoggerFactory.getLogger(getClass()).info("Time needed: " + (time / 1000));

	}

	return ret.toString();
    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {

	return new MediaType("application", "json");
    }
}
