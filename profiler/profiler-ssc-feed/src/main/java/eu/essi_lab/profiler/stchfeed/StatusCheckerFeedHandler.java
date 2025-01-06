/**
 * 
 */
package eu.essi_lab.profiler.stchfeed;

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

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.core.MediaType;

import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Feed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.WireFeedOutput;

import eu.essi_lab.access.compliance.DataComplianceReport;
import eu.essi_lab.access.compliance.wrapper.ReportsMetadataHandler;
import eu.essi_lab.api.database.DatabaseExecutor;
import eu.essi_lab.api.database.DatabaseFinder;
import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.iso.datamodel.classes.Distribution;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StreamUtils;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResourceSelector;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.stats.ComputationResult;
import eu.essi_lab.messages.stats.ResponseItem;
import eu.essi_lab.messages.stats.StatisticsMessage;
import eu.essi_lab.messages.stats.StatisticsResponse;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.pdk.handler.DefaultRequestHandler;
import eu.essi_lab.pdk.wrt.WebRequestTransformer;
import eu.essi_lab.profiler.stchfeed.entry.ServiceEntry;

/**
 * @author Fabrizio
 */
public class StatusCheckerFeedHandler extends DefaultRequestHandler {

    /**
     * 
     */
    private static final String ST_CHECKER_FEED_CREATION_ERROR = "ST_CHECKER_FEED_CREATION_ERROR";

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	ValidationMessage message = new ValidationMessage();
	message.setResult(ValidationResult.VALIDATION_SUCCESSFUL);

	return message;
    }

    @Override
    public String getStringResponse(WebRequest webRequest) throws GSException {

	GSLoggerFactory.getLogger(getClass()).info("Phase [1/3] STARTED");

	List<String> links = retrieveDistinctLinkFromDistinctHostNames();

	GSLoggerFactory.getLogger(getClass()).info("Phase [1/3] ENDED");

	GSLoggerFactory.getLogger(getClass()).info("Phase [2/3] STARTED");

	List<GSResource> resources = findResources(links);

	GSLoggerFactory.getLogger(getClass()).info("Phase [2/3] ENDED");

	GSLoggerFactory.getLogger(getClass()).info("Phase [3/3] STARTED");

	String feed = createFeed(resources, webRequest);

	GSLoggerFactory.getLogger(getClass()).info("Phase [3/3] ENDED");

	return feed;
    }

    /**
     * Retrieves the distinct links which refer to distinct host names according to the {@link #inclusionFilter()},
     * that is from records in the geoss view, excluded the satellites sources records and only if they are executables.
     * This last constraint does NOT guarantee
     * that each returned link refers ONLY to executables resources; the same link can also be present in
     * resources which are NOT executables. Because of this, the {@link #inclusionFilter()} must be applied also in the
     * next phase
     * 
     * @param response
     * @return
     * @throws GSException
     * @throws FeedException
     * @throws IOException
     * @throws IllegalArgumentException
     */
    private List<String> retrieveDistinctLinkFromDistinctHostNames() throws GSException {

	GSLoggerFactory.getLogger(StatusCheckerFeedHandler.class).debug("Retrieving of distinct links from distinct host STARTED");

	StatisticsMessage message = new StatisticsMessage();

	//
	// 100 is the max number of links returned in the frequency item for a given source
	// it should be enough to find all or at least some of the distinct hosts in the links
	// increasing too much this limit can affect performances
	//
	message.computeFrequency(Arrays.asList(MetadataElement.ONLINE_LINKAGE), 100);
	message.groupBy(ResourceProperty.SOURCE_ID);

	StorageInfo uri = ConfigurationWrapper.getDatabaseURI();

	message.setPermittedBond(inclusionFilter());

	//
	// 500 is more than enough considering that the view has less then 300 sources
	//
	Page page = new Page(1, 500);
	message.setPage(page);

	DatabaseExecutor executor = DatabaseProviderFactory.getExecutor(uri);

	StatisticsResponse response = executor.compute(message);

	List<ResponseItem> items = response.getItems();

	List<String> targetLinks = new ArrayList<>();

	HashMap<String, String> hostToLinkMap = new HashMap<String, String>();

	for (ResponseItem responseItem : items) {

	    List<ComputationResult> frequency = responseItem.getFrequency();

	    String value = frequency.get(0).getValue();

	    List<String> links = Arrays.asList(value.split(" "));

	    for (String link : links) {

		try {

		    String[] split = link.split(ComputationResult.FREQUENCY_ITEM_SEP);

		    link = URLDecoder.decode(split[0], "UTF-8");

		    String hostName = WebRequest.retrieveCompleteHostName(new URL(link).toURI());

		    hostToLinkMap.put(hostName, link);

		} catch (Exception ex) {
		}
	    }
	}

	hostToLinkMap.values().forEach(link -> targetLinks.add(link));

	GSLoggerFactory.getLogger(StatusCheckerFeedHandler.class).debug("Retrieving of distinct links from distinct host ENDED");

	GSLoggerFactory.getLogger(StatusCheckerFeedHandler.class).debug("Retrieved " + targetLinks.size() + " distinct links");

	List<String> cleanLinks = targetLinks.//
		stream().//
		map(l -> l.endsWith("&") ? l.substring(0, l.length() - 1) : l).//
		collect(Collectors.toList());

	return cleanLinks;
    }

    /**
     * Finds the resources according to the {@link #inclusionFilter()} and having the supplied <code>links</code>
     * 
     * @param links
     * @return
     */
    private List<GSResource> findResources(List<String> links) throws GSException {

	GSLoggerFactory.getLogger(StatusCheckerFeedHandler.class).debug("Resources finding STARTED");

	LogicalBond orBond = BondFactory.createOrBond();

	links.forEach(link -> orBond.getOperands().add(//
		BondFactory.createSimpleValueBond(//
			BondOperator.EQUAL, //
			MetadataElement.ONLINE_LINKAGE, //
			link)));

	LogicalBond filter = inclusionFilter();
	filter.getOperands().add(orBond);

	DiscoveryMessage message = new DiscoveryMessage();
	message.setPage(new Page(1, links.size()));
	message.setPermittedBond(filter);

	message.setDistinctValuesElement(MetadataElement.ONLINE_LINKAGE);

	ResourceSelector selector = new ResourceSelector();
	selector.setIncludeOriginal(false);
	selector.setSubset(ResourceSubset.CORE_EXTENDED);
	selector.setIndexesPolicy(IndexesPolicy.NONE);

	message.setResourceSelector(selector);

	StorageInfo uri = ConfigurationWrapper.getDatabaseURI();

	DatabaseFinder finder = DatabaseProviderFactory.getFinder(uri);

	ResultSet<GSResource> resultSet = finder.discover(message);

	GSLoggerFactory.getLogger(StatusCheckerFeedHandler.class).debug("Resources finding ENDED");

	return resultSet.getResultsList();
    }

    /**
     * @param resources
     * @param uri
     * @return
     * @throws FeedException
     * @throws IllegalArgumentException
     */
    private String createFeed(List<GSResource> resources, WebRequest webRequest) throws GSException {

	Feed feed = new Feed();
	feed.setFeedType("atom_1.0");

	feed.setTitle("SERVICES LIST NAME");
	feed.setUpdated(new Date());

	List<Entry> entries = new ArrayList<>();

	String host = webRequest.retrieveCompleteHostName();

	resources.forEach(res -> {

	    Distribution dist = res.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDistribution();

	    Stream<Online> onlineStream = StreamUtils.iteratorToStream(dist.getDistributionOnlines());

	    ReportsMetadataHandler handler = new ReportsMetadataHandler(res);

	    List<DataComplianceReport> reports = handler.getReports();

	    for (DataComplianceReport report : reports) {

		ServiceEntry entry = new ServiceEntry();

		String onlineId = report.getOnlineId();

		Online online = onlineStream.filter(o -> o.getIdentifier().equals(onlineId)).collect(Collectors.toList()).get(0);

		DataDescriptor dataDescriptor = report.getFullDataDescriptor();

		DataType dataType = dataDescriptor.getDataType();

		String servicePath = null;
		String serviceType = null;

		//
		// service type must be set according to https://statuschecker.fgdc.gov/documentation#supported-types
		//
		switch (dataType) {
		case TIME_SERIES:

		    servicePath = "sos";
		    serviceType = "SOS";

		    break;
		case GRID:

		    servicePath = "wms";
		    serviceType = "WMS";

		    break;
		case POINT:
		case TRAJECTORY:
		case PROFILE:
		case TIME_SERIES_PROFILE:
		case TRAJECTORY_PROFILE:
		case VECTOR:
		    //
		    // NOT SUPPORTED YET
		    //
		}

		entry.setId(online.getIdentifier());

		entry.setTitle(res.getHarmonizedMetadata().getCoreMetadata().getTitle());

		entry.setServiceUrl(createTestLink(online, host, servicePath, serviceType));

		entry.setServiceType(servicePath);

		entries.add(entry);
	    }
	});

	feed.setEntries(entries);

	//
	// ----
	//

	WireFeedOutput output = new WireFeedOutput();

	try {
	    return output.outputString(feed);
	} catch (Exception e) {

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    ST_CHECKER_FEED_CREATION_ERROR, //
		    e);
	}
    }

    /**
     * @param online
     * @param requestHost
     * @param serviceType
     * @param servicePath
     * @return
     */
    private String createTestLink(Online online, String requestHost, String servicePath, String serviceType) {

	String identifier = online.getIdentifier();

	String link = requestHost + "/gs-service/services/essi/view/gs-view-online-id(" + identifier + ")/" + servicePath + "?service="
		+ serviceType + "&request=GetCapabilities";

	return link;
    }

    /**
     * This bond filters in only records from the geoss view excluded the satellites sources records and only if they
     * are executables
     * 
     * @return
     * @throws GSException
     */
    private LogicalBond inclusionFilter() throws GSException {

	StorageInfo uri = ConfigurationWrapper.getDatabaseURI();

	View view = WebRequestTransformer.findView(uri, "geoss").get();

	//
	// the satellite sources are removed from the view since we consider such services
	// always very reliable
	//
	LogicalBond viewBond = (LogicalBond) view.getBond();

	List<Bond> filteredOps = viewBond.getOperands().stream().filter(b -> {

	    ResourcePropertyBond bond = (ResourcePropertyBond) b;
	    return !bond.getPropertyValue().equals("sentinelscihudtest") && //
		    !bond.getPropertyValue().equals("landsat8dbidawstest") && //
		    !bond.getPropertyValue().equals("chinageosatellite");

	}).collect(Collectors.toList());

	LogicalBond andBond = BondFactory.createAndBond();

	andBond.getOperands().add(BondFactory.createOrBond(filteredOps));
	andBond.getOperands().add(BondFactory.createIsExecutableBond(true));

	return andBond;
    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {

	return MediaType.APPLICATION_XML_TYPE;
    }
}
