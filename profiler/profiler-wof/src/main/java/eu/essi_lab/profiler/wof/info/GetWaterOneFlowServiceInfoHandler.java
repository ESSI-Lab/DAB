/**
 * 
 */
package eu.essi_lab.profiler.wof.info;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.w3c.dom.Node;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLNodeReader;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResourceSelector.ExtendedElementsPolicy;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.stats.ResponseItem;
import eu.essi_lab.messages.stats.StatisticsMessage;
import eu.essi_lab.messages.stats.StatisticsResponse;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.index.jaxb.CardinalValues;
import eu.essi_lab.model.resource.Country;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.pdk.handler.DefaultRequestHandler;
import eu.essi_lab.pdk.wrt.WebRequestTransformer;
import eu.essi_lab.profiler.wof.HISCentralProfiler;
import eu.essi_lab.profiler.wof.WOFMapperUtils;
import eu.essi_lab.request.executor.IDiscoveryNodeExecutor;
import eu.essi_lab.request.executor.IStatisticsExecutor;

/**
 * @author Fabrizio
 */
public class GetWaterOneFlowServiceInfoHandler extends DefaultRequestHandler {

    /**
     * 
     */
    private static final String SERVICE_ID = "${SERVICE_ID}";

    public static final String VIEW_WHOS_COUNTRY = "whos-country";
    public static final String VIEW_WHOS_TRANSBOUNDARY = "whos-transboundary";

    public static final String[] transboundaryViews = new String[] { "whos-plata", "whos-arctic" };

    private static final String GET_WATER_ONE_FLOW_VIEW_NOT_PROVIDED_ERROR = "GET_WATER_ONE_FLOW_VIEW_NOT_PROVIDED_ERROR";

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	GetWaterOneFlowServiceInfoValidator validator = new GetWaterOneFlowServiceInfoValidator();

	return validator.validate(request);
    }

    public enum WOFGroup {
	SOURCES, COUNTRY, TRANSBOUNDARY
    }

    @Override
    public String getStringResponse(WebRequest webRequest) throws GSException {

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

	Optional<String> viewId = webRequest.extractViewId();
	WOFGroup selected = null;

	if (viewId.isPresent()) {
	    if (viewId.get().equals(VIEW_WHOS_COUNTRY)) {
		// groups by source id
		selected = WOFGroup.COUNTRY;
	    } else if (viewId.get().equals(VIEW_WHOS_TRANSBOUNDARY)) {
		// groups by transboundary views
		selected = WOFGroup.TRANSBOUNDARY;
	    } else {
		// by default groups by source id
		selected = WOFGroup.SOURCES;
	    }
	} else {

	    throw GSException.createException(//
		    getClass(), //
		    "View not provided", //
		    ErrorInfo.ERRORTYPE_CLIENT, //
		    ErrorInfo.SEVERITY_ERROR, //
		    GET_WATER_ONE_FLOW_VIEW_NOT_PROVIDED_ERROR);
	}

	StatisticsGenerator generator = null;
	switch (selected) {
	case SOURCES:
	    generator = new StatisticsBySource();
	    break;
	case COUNTRY:
	    generator = new StatisticsByCountry();
	    break;
	case TRANSBOUNDARY:
	    generator = new StatisticsByView(transboundaryViews);
	    break;

	default:
	    break;
	}

	//
	// creates the message(s)
	//
	List<GSSource> allSources = ConfigurationWrapper.getAllSources();

	List<StatisticsMessage> statisticsMessages = generator.getStatisticMessages(webRequest, viewId.get(), selected, allSources,
		andBond);

	ServiceLoader<IStatisticsExecutor> loader = ServiceLoader.load(IStatisticsExecutor.class);
	IStatisticsExecutor executor = loader.iterator().next();

	ArrayOfServiceInfo arrayOfServiceInfo = new ArrayOfServiceInfo();

	for (StatisticsMessage statisticsMessage : statisticsMessages) {
	    // pagination works with grouped results. in this case there is one result item for each source/country/etc.
	    // in order to be sure to get all the items in the same statistics response,
	    // we set the count equals to number of sources
	    Page page = new Page();
	    page.setStart(1);
	    page.setSize(1000);

	    statisticsMessage.setPage(page);

	    // computes union of bboxes
	    statisticsMessage.computeBboxUnion();

	    // computes count distinct of 2 queryables
	    statisticsMessage.countDistinct(//
		    Arrays.asList(//
			    MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER, //
			    MetadataElement.UNIQUE_PLATFORM_IDENTIFIER));

	    statisticsMessage.computeSum(Arrays.asList(MetadataElement.DATA_SIZE));

	    StatisticsResponse response = executor.compute(statisticsMessage);

	    List<ResponseItem> items = response.getItems();

	    for (ResponseItem responseItem : items) {

		ServiceInfo serviceInfo = parseResponse(responseItem, selected, statisticsMessage, allSources);
		arrayOfServiceInfo.addServiceInfo(serviceInfo);
	    }
	}

	return arrayOfServiceInfo.toString();
    }

    private ServiceInfo parseResponse(ResponseItem responseItem, WOFGroup selected, StatisticsMessage statisticsMessage,
	    List<GSSource> allSources) {
	ServiceInfo serviceInfo = new ServiceInfo();

	String siteCount = responseItem.getCountDistinct(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER).get().getValue();
	serviceInfo.setSiteCount(siteCount);

	String varCount = responseItem.getCountDistinct(MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER).get().getValue();
	serviceInfo.setVariableCount(varCount);

	String valueCount = responseItem.getSum(MetadataElement.DATA_SIZE).get().getValue();
	if (valueCount == null || valueCount.isEmpty() || valueCount.equals("0") || valueCount.startsWith("-")) {
	    valueCount = "100";
	}
	serviceInfo.setValueCount(valueCount);

	CardinalValues cardinalValues = responseItem.getBBoxUnion().getCardinalValues().get();

	serviceInfo.setMaxx(cardinalValues.getEast());
	serviceInfo.setMaxy(cardinalValues.getNorth());
	serviceInfo.setMinx(cardinalValues.getWest());
	serviceInfo.setMiny(cardinalValues.getSouth());

	Optional<String> groupBy = responseItem.getGroupedBy();

	if (groupBy.isPresent()) {
	    switch (selected) {
	    case COUNTRY:
		updateByCountry(serviceInfo, groupBy.get(), statisticsMessage);
		break;
	    case SOURCES:
		updateBySource(serviceInfo, groupBy.get(), allSources, statisticsMessage);
		break;

	    default:
		break;
	    }
	} else {
	    // by view
	    updateByView(serviceInfo, statisticsMessage.getView().get(), statisticsMessage);
	}
	return serviceInfo;

    }

    private void updateByView(ServiceInfo serviceInfo, View view, StatisticsMessage statisticsMessage) {

	String title = view.getLabel();
	String servURL = WOFMapperUtils.getServiceUrl(new DiscoveryMessage(statisticsMessage), view);
	serviceInfo.setServURL(servURL);

	String serviceDescriptionURL = "https://"; //
	String email = ""; //
	String phone = ""; //
	String organization = ""; //

	String orgWebSite = "https://"; //
	String citation = ""; //
	String aabstract = "This service provides a virtual view on datasets from: " + view.getLabel(); //
	String networkName = "ESSI"; //

	serviceInfo.setTitle(title);
	serviceInfo.setAabstract(aabstract);
	serviceInfo.setCitation(citation);
	serviceInfo.setEmail(email);
	serviceInfo.setNetworkName(networkName);
	serviceInfo.setOrganization(organization);
	serviceInfo.setOrgWebSite(orgWebSite);
	serviceInfo.setPhone(phone);
	serviceInfo.setServiceDescriptionURL(serviceDescriptionURL);

	String serviceID;
	// the serviceID should be an integer for CUAHSI WOF
	if (WOFMapperUtils.IDENTIFIER_FROM_SOURCE) {
	    serviceID = HISCentralProfiler.generateUniqueIdFromString(view.getId());
	} else {
	    serviceID = SERVICE_ID;
	}

	serviceInfo.setServiceID(serviceID);

    }

    private void updateByCountry(ServiceInfo serviceInfo, String groupBy, StatisticsMessage statisticsMessage) {
	Country country = Country.decode(groupBy);

	if (country != null) {
	    String title = country.getShortName();
	    String servURL = WOFMapperUtils.getServiceUrl(new DiscoveryMessage(statisticsMessage), Country.decode(groupBy));
	    serviceInfo.setServURL(servURL);

	    String serviceDescriptionURL = "https://"; //
	    String email = ""; //
	    String phone = ""; //
	    String organization = ""; //

	    String orgWebSite = "https://"; //
	    String citation = ""; //
	    String aabstract = "This service provides a virtual view on datasets from a specific country: " + country.getShortName(); //
	    String networkName = "ESSI"; //

	    serviceInfo.setTitle(title);
	    serviceInfo.setAabstract(aabstract);
	    serviceInfo.setCitation(citation);
	    serviceInfo.setEmail(email);
	    serviceInfo.setNetworkName(networkName);
	    serviceInfo.setOrganization(organization);
	    serviceInfo.setOrgWebSite(orgWebSite);
	    serviceInfo.setPhone(phone);
	    serviceInfo.setServiceDescriptionURL(serviceDescriptionURL);

	    String serviceID;
	    // the serviceID should be an integer for CUAHSI WOF
	    if (WOFMapperUtils.IDENTIFIER_FROM_SOURCE) {
		serviceID = HISCentralProfiler.generateUniqueIdFromString(country.getISO3());
	    } else {
		serviceID = SERVICE_ID;
	    }

	    serviceInfo.setServiceID(serviceID);
	}

    }

    private void updateBySource(ServiceInfo serviceInfo, String groupBy, List<GSSource> allSources, StatisticsMessage statisticsMessage) {

	GSSource source = allSources.stream().//
		filter(s -> s.getUniqueIdentifier().equals(groupBy)).//
		findFirst().//
		get();

	String title = source.getLabel();

	String servURL = WOFMapperUtils.getServiceUrl(new DiscoveryMessage(statisticsMessage), source);
	serviceInfo.setServURL(servURL);

	String serviceDescriptionURL = ""; //
	String email = ""; //
	String phone = ""; //
	String organization = ""; //

	String orgWebSite = "http://localhost"; //
	String citation = ""; //
	String aabstract = "Original data publication service endpoint: " + source.getEndpoint(); //
	String networkName = "ESSI"; //

	serviceInfo.setTitle(title);
	serviceInfo.setAabstract(aabstract);
	serviceInfo.setCitation(citation);
	serviceInfo.setEmail(email);
	serviceInfo.setNetworkName(networkName);
	serviceInfo.setOrganization(organization);
	serviceInfo.setOrgWebSite(orgWebSite);
	serviceInfo.setPhone(phone);
	serviceInfo.setServiceDescriptionURL(serviceDescriptionURL);

	String serviceID;
	// the serviceID should be an integer for CUAHSI WOF
	if (WOFMapperUtils.IDENTIFIER_FROM_SOURCE) {
	    serviceID = HISCentralProfiler.generateUniqueIdFromString(source.getUniqueIdentifier());
	} else {
	    serviceID = SERVICE_ID;
	}

	serviceInfo.setServiceID(serviceID);

    }

    private List<String> getCountriesISO3(String requestId) throws GSException {
	List<String> ret = new ArrayList<>();
	DiscoveryMessage message = new DiscoveryMessage();

	message.setRequestId(requestId);

	message.setQueryRegistrationEnabled(false);

	message.setSources(ConfigurationWrapper.getAllSources());

	Page page = new Page(1, 1000);
	message.setPage(page);

	message.getResourceSelector().setIndexesPolicy(IndexesPolicy.NONE);
	message.getResourceSelector().setExtendedElementsPolicy(ExtendedElementsPolicy.ALL);
	message.getResourceSelector().addExtendedElement(MetadataElement.COUNTRY_ISO3);
	message.getResourceSelector().setSubset(ResourceSubset.EXTENDED);
	message.getResourceSelector().setIncludeOriginal(false);
	message.setSources(ConfigurationWrapper.getHarvestedSources());
	message.setDataBaseURI(ConfigurationWrapper.getDatabaseURI());
	// message.setSharedRepositoryInfo(ConfigurationWrapper.getSharedRepositoryInfo());
	message.setDistinctValuesElement(MetadataElement.COUNTRY_ISO3);
	WebRequestTransformer.setView(//
		"whos", //
		message.getDataBaseURI(), //
		message);

	GSLoggerFactory.getLogger(getClass()).info("Resource discovery STARTED");

	ServiceLoader<IDiscoveryNodeExecutor> loader = ServiceLoader.load(IDiscoveryNodeExecutor.class);
	IDiscoveryNodeExecutor executor = loader.iterator().next();

	ResultSet<Node> nodes = executor.retrieveNodes(message);
	for (Node node : nodes.getResultsList()) {
	    XMLNodeReader reader = new XMLNodeReader(node);
	    try {
		String country = reader.evaluateString("//*:CountryISO3[1]");
		ret.add(country);
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}

	return ret;
    }

    private class ServiceInfo {

	private String servURL;
	private String title;
	private String serviceDescriptionURL;
	private String email;
	private String phone;
	private String organization;
	private String orgWebSite;
	private String citation;
	private String aabstract;
	private String valueCount;
	private String variableCount;
	private String siteCount;
	private String serviceID;
	private String networkName;
	private String minx;
	private String miny;
	private String maxx;
	private String maxy;

	/**
	 * @param servURL
	 */
	public void setServURL(String servURL) {
	    this.servURL = servURL;
	}

	/**
	 * @param title
	 */
	public void setTitle(String title) {
	    this.title = title;
	}

	/**
	 * @param serviceDescriptionURL
	 */
	public void setServiceDescriptionURL(String serviceDescriptionURL) {
	    this.serviceDescriptionURL = serviceDescriptionURL;
	}

	/**
	 * @param email
	 */
	public void setEmail(String email) {
	    this.email = email;
	}

	/**
	 * @param phone
	 */
	public void setPhone(String phone) {
	    this.phone = phone;
	}

	/**
	 * @param organization
	 */
	public void setOrganization(String organization) {
	    this.organization = organization;
	}

	/**
	 * @param orgWebSite
	 */
	public void setOrgWebSite(String orgWebSite) {
	    this.orgWebSite = orgWebSite;
	}

	/**
	 * @param citation
	 */
	public void setCitation(String citation) {
	    this.citation = citation;
	}

	/**
	 * @param aabstract
	 */
	public void setAabstract(String aabstract) {
	    this.aabstract = aabstract;
	}

	/**
	 * @param valueCount
	 */
	public void setValueCount(String valueCount) {
	    this.valueCount = valueCount;
	}

	/**
	 * @param variableCount
	 */
	public void setVariableCount(String variableCount) {
	    this.variableCount = variableCount;
	}

	/**
	 * @param siteCount
	 */
	public void setSiteCount(String siteCount) {
	    this.siteCount = siteCount;
	}

	/**
	 * @param serviceID
	 */
	public void setServiceID(String serviceID) {
	    this.serviceID = serviceID;
	}

	/**
	 * @param networkName
	 */
	public void setNetworkName(String networkName) {
	    this.networkName = networkName;
	}

	/**
	 * @param minx
	 */
	public void setMinx(String minx) {
	    this.minx = minx;
	}

	/**
	 * @param miny
	 */
	public void setMiny(String miny) {
	    this.miny = miny;
	}

	/**
	 * @param maxx
	 */
	public void setMaxx(String maxx) {
	    this.maxx = maxx;
	}

	/**
	 * @param maxy
	 */
	public void setMaxy(String maxy) {
	    this.maxy = maxy;
	}

	@Override
	public String toString() {

	    return "<ServiceInfo>\n" + //
		    "	<servURL>" + servURL + "</servURL>\n" + // http://icewater.usu.edu/littlebearriverwof/cuahsi_1_1.asmx?WSDL
		    "	<Title>" + normalize(title) + "</Title>\n" + // Little Bear River Experimental Watershed, Northern
							  // Utah,
							  // USA\n" + //
		    "	<ServiceDescriptionURL>" + serviceDescriptionURL + "</ServiceDescriptionURL>\n" + // http://hiscentral.cuahsi.org/pub_network.aspx?n=52\n
		    "	<Email>" + email + "</Email>\n" + // jeff.horsburgh@usu.edu
		    "	<phone>" + phone + "</phone>\n" + // 435-797-2946
		    "	<organization>" + organization + "</organization>\n" + // Utah Water Research
									       // Laboratory, Utah
									       // State University
		    "	<orgwebsite>" + orgWebSite + "</orgwebsite>\n" + // http://littlebearriver.usu.edu
		    "	<citation>" + citation + "</citation>\n" + // Horsburgh, J. S., D. K. Stevens, D. G.
								   // Tarboton,
								   // N. O. ..." + //
		    "	<aabstract>" + normalize(aabstract) + "</aabstract>\n" + // Utah State University is conducting
								      // continuous
								      // monitoring...
		    "	<valuecount>" + valueCount + "</valuecount>\n" + // 4546654
		    "	<variablecount>" + variableCount + "</variablecount>\n" + // 59
		    "	<sitecount>" + siteCount + "</sitecount>\n" + // 16
		    "	<ServiceID>" + serviceID + "</ServiceID>\n" + // 52
		    "	<NetworkName>" + networkName + "</NetworkName>\n" + // LittleBearRiver
		    "	<minx>" + minx + "</minx>\n" + // -111.9464
		    "	<miny>" + miny + "</miny>\n" + // 41.49541
		    "	<maxx>" + maxx + "</maxx>\n" + // -111.7993
		    "	<maxy>" + maxy + "</maxy>\n" + // 41.71847
		    "	<serviceStatus />\n" + //
		    "</ServiceInfo>";
	}

	private String normalize(String str) {
	    return str.replace("&", "&amp;");
	}
    }

    private class ArrayOfServiceInfo {

	private List<ServiceInfo> list;

	/**
	 * 
	 */
	public ArrayOfServiceInfo() {

	    list = new ArrayList<>();
	}

	/**
	 * @param info
	 */
	public void addServiceInfo(ServiceInfo info) {

	    list.add(info);
	}

	@Override
	public String toString() {

	    String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + //
		    "<ArrayOfServiceInfo xmlns=\"http://hiscentral.cuahsi.org/20100205/\"\n" + //
		    "                    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" + //
		    "                    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n";

	    list.sort(new Comparator<ServiceInfo>() {

		@Override
		public int compare(ServiceInfo o1, ServiceInfo o2) {
		    String title1 = o1.toString().substring(o1.toString().indexOf("<Title>"), o1.toString().indexOf("</Title>"));
		    String title2 = o2.toString().substring(o2.toString().indexOf("<Title>"), o2.toString().indexOf("</Title>"));
		    return title1.compareTo(title2);
		}
	    });

	    for (int i = 0; i < list.size(); i++) {

		String serviceInfo = list.get(i).toString();

		if (!WOFMapperUtils.IDENTIFIER_FROM_SOURCE) {
		    serviceInfo = serviceInfo.replace(SERVICE_ID, "" + (i + 1));
		}

		response += serviceInfo;
	    }

	    response += "</ArrayOfServiceInfo>\n";
	    return response;
	}
    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {

	return MediaType.APPLICATION_XML_TYPE;
    }
}
