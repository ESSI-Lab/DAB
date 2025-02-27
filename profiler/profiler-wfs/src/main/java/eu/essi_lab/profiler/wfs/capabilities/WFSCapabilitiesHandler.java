/**
 * 
 */
package eu.essi_lab.profiler.wfs.capabilities;

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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.apache.commons.io.IOUtils;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.stats.ComputationResult;
import eu.essi_lab.messages.stats.ResponseItem;
import eu.essi_lab.messages.stats.StatisticsMessage;
import eu.essi_lab.messages.stats.StatisticsResponse;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.index.jaxb.CardinalValues;
import eu.essi_lab.pdk.handler.DefaultRequestHandler;
import eu.essi_lab.pdk.wrt.WebRequestTransformer;
import eu.essi_lab.profiler.wfs.JAXBWFS;
import eu.essi_lab.profiler.wfs.WFSProfilerSetting;
import eu.essi_lab.profiler.wfs.feature.FeatureType;
import eu.essi_lab.request.executor.IStatisticsExecutor;
import net.opengis.filter.v_1_1_0.ComparisonOperatorType;
import net.opengis.filter.v_1_1_0.ComparisonOperatorsType;
import net.opengis.filter.v_1_1_0.FilterCapabilities;
import net.opengis.filter.v_1_1_0.GeometryOperandsType;
import net.opengis.filter.v_1_1_0.ScalarCapabilitiesType;
import net.opengis.filter.v_1_1_0.SpatialCapabilitiesType;
import net.opengis.filter.v_1_1_0.SpatialOperatorNameType;
import net.opengis.filter.v_1_1_0.SpatialOperatorType;
import net.opengis.filter.v_1_1_0.SpatialOperatorsType;
import net.opengis.ows.v_1_0_0.AddressType;
import net.opengis.ows.v_1_0_0.CodeType;
import net.opengis.ows.v_1_0_0.ContactType;
import net.opengis.ows.v_1_0_0.DCP;
import net.opengis.ows.v_1_0_0.DomainType;
import net.opengis.ows.v_1_0_0.KeywordsType;
import net.opengis.ows.v_1_0_0.Operation;
import net.opengis.ows.v_1_0_0.OperationsMetadata;
import net.opengis.ows.v_1_0_0.RequestMethodType;
import net.opengis.ows.v_1_0_0.ResponsiblePartySubsetType;
import net.opengis.ows.v_1_0_0.ServiceIdentification;
import net.opengis.ows.v_1_0_0.ServiceProvider;
import net.opengis.ows.v_1_0_0.WGS84BoundingBoxType;
import net.opengis.wfs.v_1_1_0.FeatureTypeListType;
import net.opengis.wfs.v_1_1_0.FeatureTypeType;
import net.opengis.wfs.v_1_1_0.ObjectFactory;
import net.opengis.wfs.v_1_1_0.OperationsType;
import net.opengis.wfs.v_1_1_0.WFSCapabilitiesType;

/**
 * @author boldrini
 */
public class WFSCapabilitiesHandler extends DefaultRequestHandler {

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	ValidationMessage ret = new ValidationMessage();
	try {
	    new WFSGetCapabilitiesRequest(request);
	    ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	} catch (Exception e) {
	    ret.setResult(ValidationResult.VALIDATION_FAILED);
	}

	return ret;
    }

    @Override
    public String getStringResponse(WebRequest webRequest) throws GSException {
	ObjectFactory factory = new ObjectFactory();
	net.opengis.ows.v_1_0_0.ObjectFactory owsFactory = new net.opengis.ows.v_1_0_0.ObjectFactory();

	try {
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
	    ResourcePropertyBond gridBond = BondFactory.createIsGridBond(true);
	    operands.add(gridBond);

	    LogicalBond andBond = BondFactory.createAndBond(operands);

	    List<GSSource> allSources = ConfigurationWrapper.getAllSources();
	    Optional<String> viewId = webRequest.extractViewId();

	    // //
	    // // creates the message
	    // //
	    // DiscoveryMessage discoveryMessage = new DiscoveryMessage();
	    //
	    //
	    // // set the required properties
	    // discoveryMessage.setSources(allSources);
	    // discoveryMessage.setDataBaseURI(ConfigurationWrapper.getDatabaseURI());
	    //
	    // discoveryMessage.setWebRequest(webRequest);
	    //
	    // ResourceSelector selector = new ResourceSelector();
	    // selector.setSubset(ResourceSubset.CORE_EXTENDED);
	    // selector.setIndexesPolicy(IndexesPolicy.NONE);
	    // discoveryMessage.setResourceSelector(selector);
	    // discoveryMessage.setDistinctValuesElement(MetadataElement.TEAM_CATEGORY);
	    //
	    // // set the view
	    // if (viewId.isPresent()) {
	    //
	    // WebRequestTransformer.setView(//
	    // viewId.get(), //
	    // discoveryMessage.getDataBaseURI(), //
	    // discoveryMessage);
	    // }
	    //
	    // // set the user bond
	    // discoveryMessage.setUserBond(andBond);
	    //
	    // // pagination works with grouped results. in this case there is one result item for each source.
	    // // in order to be sure to get all the items in the same statistics response,
	    // // we set the count equals to number of sources
	    // Page page = new Page();
	    // page.setStart(1);
	    // page.setSize(1000);
	    //
	    // discoveryMessage.setPage(page);
	    //
	    // // computes union of bboxes
	    //
	    // ServiceLoader<IDiscoveryExecutor> loader = ServiceLoader.load(IDiscoveryExecutor.class);
	    // IDiscoveryExecutor executor = loader.iterator().next();
	    //
	    // ResultSet<GSResource> response = executor.retrieve(discoveryMessage);

	    WFSCapabilitiesType capabilities = new WFSCapabilitiesType();
	    capabilities.setVersion("1.1.0");

	    ServiceIdentification serviceInfo = new ServiceIdentification();
	    serviceInfo.setTitle("DAB powered Web Feature Service");
	    serviceInfo.setAbstract("This WFS is implemented by the DAB");
	    List<KeywordsType> keywords = new ArrayList<>();
	    KeywordsType k = new KeywordsType();
	    List<String> keys = new ArrayList<>();
	    keys.add("WFS");
	    keys.add("DAB");
	    k.setKeyword(keys);
	    keywords.add(k);
	    serviceInfo.setKeywords(keywords);
	    CodeType type = new CodeType();
	    type.setValue("WFS");
	    serviceInfo.setServiceType(type);
	    List<String> versions = new ArrayList<>();
	    versions.add("1.1.0");
	    serviceInfo.setServiceTypeVersion(versions);
	    serviceInfo.setFees("NONE");
	    serviceInfo.setAccessConstraints(Arrays.asList(new String[] { "NONE" }));

	    capabilities.setServiceIdentification(serviceInfo);

	    ServiceProvider serviceProvider = new ServiceProvider();
	    serviceProvider.setProviderName("My provider");
	    ResponsiblePartySubsetType rpst = new ResponsiblePartySubsetType();
	    rpst.setIndividualName("My person name");
	    rpst.setPositionName("My position");
	    ContactType contactType = new ContactType();
	    AddressType address = new AddressType();
	    address.setElectronicMailAddress(Arrays.asList(new String[] { "test@mail.com" }));
	    contactType.setAddress(address);
	    rpst.setContactInfo(contactType);
	    serviceProvider.setServiceContact(rpst);
	    capabilities.setServiceProvider(serviceProvider);

	    OperationsMetadata operations = new OperationsMetadata();

	    List<Operation> ops = new ArrayList<>();

	    String wfsURL = "";
	    try {
		UriInfo uri = webRequest.getUriInfo();
		wfsURL = uri.getBaseUri().toString() + "/" + new WFSProfilerSetting().getServicePath();
	    } catch (Exception e) {
	    }

	    // CAPABILITIES
	    Operation capabilitiesOperation = new Operation();
	    ops.add(capabilitiesOperation);
	    capabilitiesOperation.setName("GetCapabilities");
	    List<DCP> dcp = new ArrayList<>();
	    DCP capDCP = new DCP();
	    net.opengis.ows.v_1_0_0.HTTP capHTTP = new net.opengis.ows.v_1_0_0.HTTP();
	    List<JAXBElement<RequestMethodType>> getsposts = new ArrayList<JAXBElement<RequestMethodType>>();

	    RequestMethodType getrmt = new RequestMethodType();
	    getrmt.setHref(wfsURL);
	    JAXBElement<RequestMethodType> capGet = owsFactory.createHTTPGet(getrmt);
	    getsposts.add(capGet);

	    capHTTP.setGetOrPost(getsposts);
	    capDCP.setHTTP(capHTTP);
	    dcp.add(capDCP);
	    capabilitiesOperation.setDCP(dcp);
	    operations.setOperation(ops);

	    List<DomainType> dtps = new ArrayList<>();
	    DomainType dtp = new DomainType();
	    dtp.setName("AcceptVersions");
	    dtp.setValue(Arrays.asList(new String[] { "1.1.0" }));
	    dtps.add(dtp);
	    DomainType dtp2 = new DomainType();
	    dtp2.setName("AcceptFormats");
	    dtp2.setValue(Arrays.asList(new String[] { "text/xml" }));
	    dtps.add(dtp2);
	    capabilitiesOperation.setParameter(dtps);
	    capabilities.setOperationsMetadata(operations);

	    // GET FEATURE
	    Operation getFeatureOperation = new Operation();
	    ops.add(getFeatureOperation);
	    getFeatureOperation.setName("GetFeature");
	    List<DCP> dcpFeature = new ArrayList<>();
	    DCP featureDCP = new DCP();
	    net.opengis.ows.v_1_0_0.HTTP feaHTTP = new net.opengis.ows.v_1_0_0.HTTP();
	    List<JAXBElement<RequestMethodType>> featureGetsposts = new ArrayList<JAXBElement<RequestMethodType>>();
	    RequestMethodType getrmtFeature = new RequestMethodType();
	    getrmtFeature.setHref(wfsURL);
	    JAXBElement<RequestMethodType> feaGet = owsFactory.createHTTPGet(getrmtFeature);
	    featureGetsposts.add(feaGet);

	    feaHTTP.setGetOrPost(featureGetsposts);
	    featureDCP.setHTTP(feaHTTP);
	    dcpFeature.add(featureDCP);
	    getFeatureOperation.setDCP(dcpFeature);
	    operations.setOperation(ops);

	    List<DomainType> feaDtps = new ArrayList<>();

	    addParameter(feaDtps, "resultType", "results");
	    addParameter(feaDtps, "outputFormat", //
		    "text/xml; subtype=gml/3.1.1", //
		    "GML2", //
		    // "KML", //
		    // "SHAPE-ZIP", //
		    "application/gml+xml; version=3.2", //
		    // "application/json", //
		    // "application/vnd.google-earth.kml xml", //
		    // "application/vnd.google-earth.kml+xml", //
		    // "csv", //
		    "gml3", //
		    "gml32", //
		    // "json", //
		    // "text/csv", //
		    // "text/javascript", //
		    "text/xml; subtype=gml/2.1.2", //
		    "text/xml; subtype=gml/3.2"//
	    //
	    );
	    addParameter(feaDtps, "srsName", "EPSG:4326", "EPSG:3857");

	    getFeatureOperation.setParameter(feaDtps);

	    capabilities.setOperationsMetadata(operations);

	    // feature type list

	    FeatureTypeListType ftl = new FeatureTypeListType();
	    OperationsType ots = new OperationsType();
	    List<net.opengis.wfs.v_1_1_0.OperationType> oplist = new ArrayList<net.opengis.wfs.v_1_1_0.OperationType>();

	    oplist.add(net.opengis.wfs.v_1_1_0.OperationType.QUERY);
	    ots.setOperation(oplist);
	    ftl.setOperations(ots);

	    String myView = viewId.isPresent() ? viewId.get() : null;

	    List<FeatureType> features = FeatureType.getFeatureTypes(myView);

	    for (FeatureType feature : features) {
		FeatureTypeType ftt = new FeatureTypeType();
		ftt.setName(feature.getQName());
		ftt.setTitle(feature.getTitle());
		ftt.setAbstract(feature.getAbstract());
		List<KeywordsType> kkks = new ArrayList<>();
		KeywordsType kks = new KeywordsType();
		kks.setKeyword(Arrays.asList(feature.getKeywords()));
		kkks.add(kks);
		ftt.setKeywords(kkks);
		ftt.setDefaultSRS("EPSG:4326");
		ftt.setOtherSRS(Arrays.asList(new String[] { "EPSG:3857" }));

		// BBOX calculation

		Bond bond = feature.getBond();

		StatisticsMessage statisticsMessage = new StatisticsMessage();

		// set the required properties
		statisticsMessage.setSources(allSources);
		statisticsMessage.setDataBaseURI(ConfigurationWrapper.getStorageInfo());
		// statisticsMessage.setSharedRepositoryInfo(ConfigurationUtils.getSharedRepositoryInfo());
		statisticsMessage.setWebRequest(webRequest);

		if (viewId.isPresent()) {
		    WebRequestTransformer.setView(//
			    viewId.get(), //
			    statisticsMessage.getDataBaseURI(), //
			    statisticsMessage);
		}

		// set the user bond
		statisticsMessage.setUserBond(bond);

		ServiceLoader<IStatisticsExecutor> statsLoader = ServiceLoader.load(IStatisticsExecutor.class);
		IStatisticsExecutor statsExecutor = statsLoader.iterator().next();

		Page spage = new Page();
		spage.setStart(1);
		spage.setSize(1000);

		statisticsMessage.setPage(spage);

		// computes union of bboxes
		statisticsMessage.computeBboxUnion();

		// computes count distinct of 2 queryables
		// statisticsMessage.countDistinct(//
		// Arrays.asList(//
		// MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER, //
		// MetadataElement.UNIQUE_PLATFORM_IDENTIFIER));

		// statisticsMessage.computeSum(Arrays.asList(MetadataElement.DATA_SIZE));

		StatisticsResponse statsResponse = statsExecutor.compute(statisticsMessage);

		List<ResponseItem> items = statsResponse.getItems();

		boolean bboxPresent = false;
		for (ResponseItem responseItem : items) {

		    ComputationResult bbox = responseItem.getBBoxUnion();
		    List<WGS84BoundingBoxType> bboxes = new ArrayList<>();
		    WGS84BoundingBoxType bbox84 = new WGS84BoundingBoxType();
		    Optional<CardinalValues> optionalCards = bbox.getCardinalValues();
		    if (optionalCards.isPresent()) {
			CardinalValues cards = optionalCards.get();
			bbox84.setLowerCorner(
				Arrays.asList(new Double[] { Double.parseDouble(cards.getWest()), Double.parseDouble(cards.getSouth()) }));
			bbox84.setUpperCorner(
				Arrays.asList(new Double[] { Double.parseDouble(cards.getEast()), Double.parseDouble(cards.getNorth()) }));
			bboxes.add(bbox84);
			ftt.setWGS84BoundingBox(bboxes);
			bboxPresent = true;
		    }
		}
		if (bboxPresent) {
		    ftl.getFeatureType().add(ftt);
		}
	    }
	    capabilities.setFeatureTypeList(ftl);

	    // filter capabilities

	    FilterCapabilities filterCapabilities = new FilterCapabilities();
	    SpatialCapabilitiesType spatialCapabilitiesType = new SpatialCapabilitiesType();
	    GeometryOperandsType geometricOperands = new GeometryOperandsType();
	    List<QName> gops = new ArrayList<QName>();
	    gops.add(new QName("http://www.opengis.net/gml", "Envelope", "gml"));
	    geometricOperands.setGeometryOperand(gops);
	    spatialCapabilitiesType.setGeometryOperands(geometricOperands);

	    SpatialOperatorsType sotype = new SpatialOperatorsType();
	    List<SpatialOperatorType> sots = new ArrayList<>();
	    SpatialOperatorType sot = new SpatialOperatorType();
	    sot.setName(SpatialOperatorNameType.INTERSECTS);
	    sot.setName(SpatialOperatorNameType.BBOX);
	    sot.setName(SpatialOperatorNameType.CONTAINS);
	    sots.add(sot);
	    sotype.setSpatialOperator(sots);
	    spatialCapabilitiesType.setSpatialOperators(sotype);

	    filterCapabilities.setSpatialCapabilities(spatialCapabilitiesType);

	    ScalarCapabilitiesType scalarCapabilitiesType = new ScalarCapabilitiesType();
	    ComparisonOperatorsType comparisonTypes = new ComparisonOperatorsType();
	    List<ComparisonOperatorType> copts = new ArrayList<>();
	    copts.add(ComparisonOperatorType.EQUAL_TO);
	    copts.add(ComparisonOperatorType.GREATER_THAN);
	    copts.add(ComparisonOperatorType.GREATER_THAN_EQUAL_TO);
	    copts.add(ComparisonOperatorType.LESS_THAN);
	    copts.add(ComparisonOperatorType.LESS_THAN_EQUAL_TO);
	    copts.add(ComparisonOperatorType.LIKE);
	    copts.add(ComparisonOperatorType.NOT_EQUAL_TO);
	    comparisonTypes.setComparisonOperator(copts);
	    scalarCapabilitiesType.setComparisonOperators(comparisonTypes);
	    // scalarCapabilitiesType.setArithmeticOperators(null)
	    filterCapabilities.setScalarCapabilities(scalarCapabilitiesType);

	    capabilities.setFilterCapabilities(filterCapabilities);

	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    JAXBElement<WFSCapabilitiesType> wfsCapabilities = factory.createWFSCapabilities(capabilities);
	    JAXBWFS.getInstance().getMarshaller().marshal(wfsCapabilities, baos);

	    String ret = IOUtils.toString(baos.toByteArray(), "UTF-8");
	    baos.close();
	    return ret;

	} catch (

	Exception e) {
	    e.printStackTrace();
	    List<ErrorInfo> error = new ArrayList<>();
	    ErrorInfo err = new ErrorInfo();
	    err.setCause(e);
	    error.add(err);
	    throw GSException.createException(error);
	}

    }

    private void addParameter(List<DomainType> feaDtps, String parameterName, String... values) {
	DomainType rtDtp = new DomainType();
	rtDtp.setName(parameterName);
	rtDtp.setValue(Arrays.asList(values));
	feaDtps.add(rtDtp);

    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {

	return MediaType.APPLICATION_XML_TYPE;
    }
}
