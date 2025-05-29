/**
 * 
 */
package eu.essi_lab.profiler.arpa;

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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.GetViewIdentifiersRequest;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.DynamicView;
import eu.essi_lab.messages.bond.DynamicViewSource;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.View;
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
import eu.essi_lab.pdk.wrt.WebRequestTransformer;
import eu.essi_lab.profiler.arpa.HydroCSVParameters.HydroCSVParameter;
import eu.essi_lab.profiler.arpa.HydroCSVViewEncoder.CSV_Field;
import eu.essi_lab.request.executor.IStatisticsExecutor;

/**
 * @author boldrini
 */
public class HydroCSVViewsHandler extends DefaultRequestHandler {

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	HydroCSVViewsValidator validator = new HydroCSVViewsValidator();

	return validator.validate(request);
    }

    @Override
    public String getStringResponse(WebRequest webRequest) throws GSException {

	HydroCSVParameters parameters = new HydroCSVParameters(webRequest);

	String startStr = parameters.getParameter(HydroCSVParameter.START);
	int start = 0;
	// try {
	// start = Integer.parseInt(startStr);
	// } catch (Exception e) {
	// }
	String countStr = parameters.getParameter(HydroCSVParameter.COUNT);
	int count = 1000;
	// try {
	// count = Integer.parseInt(countStr);
	// } catch (Exception e) {
	// }

	StorageInfo databaseURI = ConfigurationWrapper.getStorageInfo();
	DatabaseReader reader = DatabaseProviderFactory.getReader(databaseURI);

	List<String> viewIdentifiers = reader.getViewIdentifiers(GetViewIdentifiersRequest.create(start, count));

	HydroCSVViewEncoder headers = new HydroCSVViewEncoder();
	CSV_Field[] fields = headers.getFields();
	for (CSV_Field field : fields) {
	    headers.add(field, field.toString());
	}
	String ret = headers.toString() + "\n"; // for the headers;

	List<GSSource> sources = ConfigurationWrapper.getAllSources();
	for (GSSource gsSource : sources) {
	    DynamicViewSource sourceView = new DynamicViewSource();
	    sourceView.setPostfix(gsSource.getUniqueIdentifier());
	    String dynamicViewId = sourceView.getId();
	    viewIdentifiers.add(dynamicViewId);
	}

	GSLoggerFactory.getLogger(getClass()).info("Found " + viewIdentifiers.size() + " views");

	for (int i = 0; i < viewIdentifiers.size(); i++) {
	    // long begin = System.currentTimeMillis();

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
	    statisticsMessage.setDataBaseURI(ConfigurationWrapper.getStorageInfo());
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

	    String viewIdentifier = viewIdentifiers.get(i);

	    GSLoggerFactory.getLogger(getClass())
		    .info("Calculating stats for view (" + i + "/" + viewIdentifiers.size() + "): " + viewIdentifier);

	    HydroCSVViewEncoder encoder = new HydroCSVViewEncoder();

	    WebRequestTransformer.setView(//
		    viewIdentifier, //
		    statisticsMessage.getDataBaseURI(), //
		    statisticsMessage);

	    ServiceLoader<IStatisticsExecutor> loader = ServiceLoader.load(IStatisticsExecutor.class);
	    IStatisticsExecutor executor = loader.iterator().next();

	    StatisticsResponse response = executor.compute(statisticsMessage);

	    List<ResponseItem> items = response.getItems();

	    if (!items.isEmpty()) {

		ResponseItem responseItem = items.get(0);
		String timeSeriesCount = responseItem.getCountDistinct(MetadataElement.ONLINE_ID).get().getValue();

		if (timeSeriesCount != null && !timeSeriesCount.isEmpty() && !timeSeriesCount.equals("0")) {

		    Optional<View> optionalView = reader.getView(viewIdentifier);
		    View view;
		    if (optionalView.isPresent()) {

			view = optionalView.get();
		    } else {

			Optional<DynamicView> optionalDynamicView = DynamicView.resolveDynamicView(viewIdentifier);
			if (optionalDynamicView.isPresent()) {
			    view = optionalDynamicView.get();
			} else {
			    continue;
			}
		    }

		    String siteCount = responseItem.getCountDistinct(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER).get().getValue();
		    String varCount = responseItem.getCountDistinct(MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER).get().getValue();
		    String valueCount = responseItem.getSum(MetadataElement.DATA_SIZE).get().getValue();
		    if (valueCount == null || valueCount.isEmpty() || valueCount.equals("0")) {
			valueCount = "100";
		    }
		    Optional<CardinalValues> optionalCardinals = responseItem.getBBoxUnion().getCardinalValues();
		    if (optionalCardinals.isPresent()) {
			CardinalValues cardinalValues = optionalCardinals.get();
			encoder.add(CSV_Field.SOUTH, cardinalValues.getSouth());
			encoder.add(CSV_Field.WEST, cardinalValues.getWest());
			encoder.add(CSV_Field.NORTH, cardinalValues.getNorth());
			encoder.add(CSV_Field.EAST, cardinalValues.getEast());

		    }

		    // ComputationResult optionalTempextent = responseItem.getTempExtentUnion();
		    Optional<ComputationResult> begin = responseItem.getMin(MetadataElement.TEMP_EXTENT_BEGIN);
		    if (begin.isPresent()) {
			encoder.add(CSV_Field.BEGIN, begin.get().getValue());
		    }
		    Optional<ComputationResult> end = responseItem.getMax(MetadataElement.TEMP_EXTENT_END);
		    if (end.isPresent()) {
			String value = end.get().getValue();
			if (value != null && !value.equals("") && !value.equals("null")) {
			    encoder.add(CSV_Field.END, value);
			} else {
			    encoder.add(CSV_Field.END, now);
			}
		    } else {
			encoder.add(CSV_Field.END, now);
		    }

		    encoder.add(CSV_Field.ID, viewIdentifier);
		    encoder.add(CSV_Field.LABEL, view.getLabel());
		    encoder.add(CSV_Field.BOND, view.getBond().toString());

		    encoder.add(CSV_Field.TIME_SERIES_COUNT, timeSeriesCount);
		    encoder.add(CSV_Field.SITE_COUNT, siteCount);
		    encoder.add(CSV_Field.VARIABLE_COUNT, varCount);
		    encoder.add(CSV_Field.VALUE_COUNT, valueCount);

		    encoder.add(CSV_Field.HIS_CENTRAL, servicesURL + viewIdentifier + "/" + HydroCSVUtils.getHISCentralPath());
		    encoder.add(CSV_Field.HYDRO_SERVER, servicesURL + viewIdentifier + "/" + HydroCSVUtils.getHydroServerPath());
		    encoder.add(CSV_Field.SOS, servicesURL + viewIdentifier + "/" + HydroCSVUtils.getSOSPath());
		    encoder.add(CSV_Field.HYDRO_CSV, servicesURL + viewIdentifier + "/" + HydroCSVUtils.getHydroCSVPath());
		    encoder.add(CSV_Field.GI_PORTAL, baseURL + "search?view=" + viewIdentifier);

		    String line = encoder.toString() + "\n";
		    ret += line;

		}

	    }

	    // long end = System.currentTimeMillis();
	    // long time = end - begin;
	    // GSLoggerFactory.getLogger(getClass()).info("Time needed: " + (time / 1000));

	}

	return ret;
    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {

	return new MediaType("text", "csv");
    }
}
