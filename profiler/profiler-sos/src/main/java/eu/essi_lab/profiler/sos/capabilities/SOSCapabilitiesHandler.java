/**
 * 
 */
package eu.essi_lab.profiler.sos.capabilities;

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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;

import org.apache.commons.io.IOUtils;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.jaxb.sos._2_0.CapabilitiesType;
import eu.essi_lab.jaxb.sos._2_0.CapabilitiesType.Contents;
import eu.essi_lab.jaxb.sos._2_0.ContentsType;
import eu.essi_lab.jaxb.sos._2_0.ObservationOfferingType;
import eu.essi_lab.jaxb.sos._2_0.ObservationOfferingType.ObservedArea;
import eu.essi_lab.jaxb.sos._2_0.ObservationOfferingType.PhenomenonTime;
import eu.essi_lab.jaxb.sos._2_0.fes._2.ConformanceType;
import eu.essi_lab.jaxb.sos._2_0.fes._2.FilterCapabilities;
import eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.DirectPositionType;
import eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.EnvelopeType;
import eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.ObjectFactory;
import eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.TimeIndeterminateValueType;
import eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.TimePeriodType;
import eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.TimePositionType;
import eu.essi_lab.jaxb.sos._2_0.ows_1.CodeType;
import eu.essi_lab.jaxb.sos._2_0.ows_1.ContactType;
import eu.essi_lab.jaxb.sos._2_0.ows_1.DomainType;
import eu.essi_lab.jaxb.sos._2_0.ows_1.LanguageStringType;
import eu.essi_lab.jaxb.sos._2_0.ows_1.OnlineResourceType;
import eu.essi_lab.jaxb.sos._2_0.ows_1.ResponsiblePartySubsetType;
import eu.essi_lab.jaxb.sos._2_0.ows_1.ServiceIdentification;
import eu.essi_lab.jaxb.sos._2_0.ows_1.ServiceProvider;
import eu.essi_lab.jaxb.sos._2_0.ows_1.TelephoneType;
import eu.essi_lab.jaxb.sos._2_0.ows_1.ValueType;
import eu.essi_lab.jaxb.sos._2_0.swes_2.AbstractContentsType.Offering;
import eu.essi_lab.jaxb.sos._2_0.swes_2.AbstractOfferingType;
import eu.essi_lab.jaxb.sos.factory.JAXBSOS;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
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
import eu.essi_lab.messages.stats.ComputationResult;
import eu.essi_lab.messages.stats.ResponseItem;
import eu.essi_lab.messages.stats.StatisticsMessage;
import eu.essi_lab.messages.stats.StatisticsResponse;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.index.jaxb.CardinalValues;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.pdk.handler.DefaultRequestHandler;
import eu.essi_lab.pdk.wrt.WebRequestTransformer;
import eu.essi_lab.profiler.sos.SOSUtils;
import eu.essi_lab.request.executor.IDiscoveryExecutor;
import eu.essi_lab.request.executor.IStatisticsExecutor;

/**
 * @author boldrini
 */
public class SOSCapabilitiesHandler extends DefaultRequestHandler {

    /**
     * 
     */
    private static final String SOS_CAPABILITIES_HANDLER_ERROR = "SOS_CAPABILITIES_HANDLER_ERROR";

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	ValidationMessage ret = new ValidationMessage();
	try {
	    new GetCapabilitiesRequest(request);
	    ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	} catch (Exception e) {
	    ret.setResult(ValidationResult.VALIDATION_FAILED);
	}

	return ret;
    }

    @Override
    public String getStringResponse(WebRequest webRequest) throws GSException {

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

	    // set the view
	    Optional<String> viewId = webRequest.extractViewId();
	    if (viewId.isPresent()) {

		WebRequestTransformer.setView(//
			viewId.get(), //
			statisticsMessage.getDataBaseURI(), //
			statisticsMessage);
	    }

	    // set the user bond
	    statisticsMessage.setUserBond(andBond);

	    // groups by source id
	    statisticsMessage.groupBy(MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER);

	    // pagination works with grouped results. in this case there is one result item for each source.
	    // in order to be sure to get all the items in the same statistics response,
	    // we set the count equals to number of sources
	    Page page = new Page();
	    page.setStart(1);
	    page.setSize(1000);

	    statisticsMessage.setPage(page);

	    // computes union of bboxes
	    statisticsMessage.computeBboxUnion();
	    String now = ISO8601DateTimeUtils.getISO8601DateTime();
	    statisticsMessage.computeMin(Arrays.asList(MetadataElement.TEMP_EXTENT_BEGIN));
	    statisticsMessage.computeMax(Arrays.asList(MetadataElement.TEMP_EXTENT_END));

	    // computes count distinct of 2 queryables
	    statisticsMessage.countDistinct(//
		    Arrays.asList(//
			    MetadataElement.ONLINE_ID, //
			    MetadataElement.UNIQUE_PLATFORM_IDENTIFIER));

	    statisticsMessage.computeSum(Arrays.asList(MetadataElement.DATA_SIZE));

	    ServiceLoader<IStatisticsExecutor> loader = ServiceLoader.load(IStatisticsExecutor.class);
	    IStatisticsExecutor executor = loader.iterator().next();

	    StatisticsResponse response = executor.compute(statisticsMessage);

	    List<ResponseItem> items = response.getItems();

	    CapabilitiesType capabilities = new CapabilitiesType();

	    capabilities.setVersion("2.0.0");

	    // SERVICE IDENTIFICATION

	    ServiceIdentification serviceIdentification = new ServiceIdentification();

	    serviceIdentification.getTitle().addAll(getEnglishTextList("GI-suite brokering service Sensor Observation Service"));
	    serviceIdentification.getAbstract().addAll(getEnglishTextList("GI-suite brokering service Sensor Observation Service"));
	    CodeType serviceType = new CodeType();
	    serviceType.setCodeSpace("http://opengeospatial.net");
	    serviceType.setValue("OGC:SOS");
	    serviceIdentification.setServiceType(serviceType);
	    serviceIdentification.getServiceTypeVersion().addAll(Arrays.asList(new String[] { "2.0.0" }));
	    serviceIdentification.getProfile()
		    .addAll(Arrays.asList(new String[] { //
			    "http://www.opengis.net/spec/SOS/2.0/conf/core", //
			    "http://www.opengis.net/spec/SOS_application-profile_hydrology/1.0/req/hydrosos"//
	    }));
	    serviceIdentification.setFees("NONE");
	    serviceIdentification.getAccessConstraints().addAll(Arrays.asList(new String[] { "NONE" }));

	    capabilities.setServiceIdentification(serviceIdentification);

	    // SERVICE PROVIDER

	    ServiceProvider serviceProvider = new ServiceProvider();
	    serviceProvider.setProviderName("National Research Council of Italy (CNR) ESSI-Lab");
	    OnlineResourceType onlineResource = new OnlineResourceType();
	    onlineResource.setHref("http://essi-lab.eu");
	    serviceProvider.setProviderSite(onlineResource);
	    ResponsiblePartySubsetType responsibleParty = new ResponsiblePartySubsetType();
	    responsibleParty.setIndividualName("Paolo Mazzetti");
	    CodeType roleCode = new CodeType();
	    roleCode.setValue("Head of the Division of Florence of CNR-IIA");
	    responsibleParty.setRole(roleCode);
	    ContactType contact = new ContactType();
	    TelephoneType phone = new TelephoneType();
	    phone.getVoice().addAll(Arrays.asList(new String[] { "+390555226591" }));
	    contact.setPhone(phone);
	    responsibleParty.setContactInfo(contact);
	    serviceProvider.setServiceContact(responsibleParty);// note: without service contact Helgoland will hang!
	    capabilities.setServiceProvider(serviceProvider);

	    // FILTER CAPABILITIES

	    FilterCapabilities filterCapabilities = new FilterCapabilities();

	    ConformanceType conformance = new ConformanceType();
	    List<DomainType> constraints = new ArrayList<>();
	    constraints.add(getConstraint("ImplementsQuery", false));
	    conformance.getConstraint().addAll(constraints);
	    filterCapabilities.setConformance(conformance);

	    eu.essi_lab.jaxb.sos._2_0.CapabilitiesType.FilterCapabilities filterCaps = new eu.essi_lab.jaxb.sos._2_0.CapabilitiesType.FilterCapabilities();
	    filterCaps.setFilterCapabilities(filterCapabilities);
	    capabilities.setFilterCapabilities(filterCaps);

	    // CONTENTS

	    Contents contents = new Contents();

	    ContentsType contentsType = new ContentsType();
	    contentsType.getResponseFormat().add("http://www.opengis.net/om/2.0");
	    contentsType.getObservationType().add("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
	    contents.setContents(contentsType);

	    capabilities.setContents(contents);

	    List<Offering> offerings = new ArrayList<>();

	    ServiceLoader<IDiscoveryExecutor> discoveryLoader = ServiceLoader.load(IDiscoveryExecutor.class);
	    IDiscoveryExecutor discoveryExecutor = discoveryLoader.iterator().next();

	    DiscoveryMessage message = new DiscoveryMessage();

	    message.getResourceSelector().setIndexesPolicy(IndexesPolicy.NONE);
	    message.getResourceSelector().setSubset(ResourceSubset.FULL);

	    message.setPage(new Page(1, 1));

	    message.setSources(ConfigurationWrapper.getHarvestedSources());
	    message.setDataBaseURI(ConfigurationWrapper.getStorageInfo());

	    // set the view
	    if (viewId.isPresent()) {

		WebRequestTransformer.setView(//
			viewId.get(), //
			message.getDataBaseURI(), //
			message);
	    }

	    for (ResponseItem responseItem : items) {

		String uniqueAttributeId = responseItem.getGroupedBy().get();

		message.setUserBond(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER,
			uniqueAttributeId));

		ResultSet<GSResource> discoveryResponse = discoveryExecutor.retrieve(message);
		List<GSResource> results = discoveryResponse.getResultsList();
		if (results.isEmpty()) {
		    continue;
		}
		GSResource resource = results.get(0);

		Offering offering = new Offering();

		ObservationOfferingType observationOffering = new ObservationOfferingType();
		JAXBElement<? extends AbstractOfferingType> jaxbOffering = JAXBSOS.getInstance().getFactory()
			.createObservationOffering(observationOffering);
		offering.setAbstractOffering(jaxbOffering);

		String attributeName = uniqueAttributeId;
		try {
		    attributeName = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getCoverageDescription()
			    .getAttributeTitle();
		} catch (Exception e) {
		}

		String siteCount = responseItem.getCountDistinct(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER).get().getValue();

		String timeSeriesCount = responseItem.getCountDistinct(MetadataElement.ONLINE_ID).get().getValue();

		String valueCount = responseItem.getSum(MetadataElement.DATA_SIZE).get().getValue();
		if (valueCount == null || valueCount.isEmpty() || valueCount.equals("0")) {
		    valueCount = "100";
		}

		String offeringId = SOSUtils.createOfferingId(uniqueAttributeId);
		observationOffering.setIdentifier(offeringId);
		Collection<? extends eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.CodeType> codes = getGMLCodes("Offering of: " + attributeName);
		observationOffering.getName().addAll(codes);

		observationOffering.setDescription("Collection of homogeneous time series on the observed parameter: " + attributeName
			+ " Sites #: " + siteCount + " Time series #" + timeSeriesCount + " Values #: " + valueCount);

		String procedureId = SOSUtils.createProcedureId(uniqueAttributeId);
		observationOffering.setProcedure(procedureId);
		observationOffering.getObservableProperty().addAll(Arrays.asList(new String[] { attributeName }));

		ObservedArea observedArea = new ObservedArea();
		ObjectFactory gmlFactory = new ObjectFactory();
		EnvelopeType envelope = new EnvelopeType();
		JAXBElement<EnvelopeType> jaxbEnvelope = gmlFactory.createEnvelope(envelope);
		observedArea.setEnvelope(jaxbEnvelope);
		observationOffering.setObservedArea(observedArea);

		CardinalValues cardinalValues = responseItem.getBBoxUnion().getCardinalValues().get();

		Double east = Double.parseDouble(cardinalValues.getEast());
		Double north = Double.parseDouble(cardinalValues.getNorth());
		Double west = Double.parseDouble(cardinalValues.getWest());
		Double south = Double.parseDouble(cardinalValues.getSouth());
		DirectPositionType lower = new DirectPositionType();
		lower.getValue().addAll(Arrays.asList(new Double[] { south, west }));
		envelope.setLowerCorner(lower);
		DirectPositionType upper = new DirectPositionType();
		upper.getValue().addAll(Arrays.asList(new Double[] { north, east }));
		envelope.setUpperCorner(upper);
		envelope.setSrsName("http://www.opengis.net/def/crs/EPSG/0/4326");

		Optional<ComputationResult> begin = responseItem.getMin(MetadataElement.TEMP_EXTENT_BEGIN);
		// String beginTime = "";
		// String endTime = "";
		TimePositionType tp1 = new TimePositionType();
		TimePositionType tp2 = new TimePositionType();
		if (begin.isPresent()) {
		    tp1.getValue().addAll(Arrays.asList(new String[] { begin.get().getValue() }));
		}
		Optional<ComputationResult> end = responseItem.getMax(MetadataElement.TEMP_EXTENT_END);

		if (end.isPresent()) {
		    String value = end.get().getValue();
		    if (value != null && !value.equals("")) {
			tp2.getValue().addAll(Arrays.asList(new String[] { value }));
		    } else {
			tp2.setIndeterminatePosition(TimeIndeterminateValueType.NOW);
		    }
		} else {
		    tp2.setIndeterminatePosition(TimeIndeterminateValueType.NOW);
		}
		PhenomenonTime time = new PhenomenonTime();
		TimePeriodType timePeriod = new TimePeriodType();

		timePeriod.setBeginPosition(tp1);
		timePeriod.setEndPosition(tp2);
		time.setTimePeriod(timePeriod);
		observationOffering.setPhenomenonTime(time);

		offerings.add(offering);
	    }
	    contentsType.getOffering().addAll(offerings);

	    // OTHER

	    JAXBElement<CapabilitiesType> jaxbElement = JAXBSOS.getInstance().getFactory().createCapabilities(capabilities);
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    JAXBSOS.getInstance().marshal(jaxbElement, baos);
	    String ret = IOUtils.toString(baos.toByteArray(), "UTF-8");
	    baos.close();
	    return ret;

	} catch (Exception e) {

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    ErrorInfo.ERRORTYPE_CLIENT, //
		    ErrorInfo.SEVERITY_ERROR, //
		    SOS_CAPABILITIES_HANDLER_ERROR);
	}

    }

    private List<eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.CodeType> getGMLCodes(String value) {
	List<eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.CodeType> ret = new ArrayList<>();
	eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.CodeType code = new eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.CodeType();
	code.setValue(value);
	ret.add(code);
	return ret;
    }

    private DomainType getConstraint(String name, boolean supported) {
	DomainType ret = new DomainType();
	ret.setName(name);
	ValueType value = new ValueType();
	value.setValue("" + supported);
	ret.setDefaultValue(value);
	return ret;
    }

    private List<LanguageStringType> getEnglishTextList(String value) {
	List<LanguageStringType> titles = new ArrayList<>();
	LanguageStringType title = new LanguageStringType();
	title.setLang("eng");
	title.setValue(value);
	titles.add(title);
	return titles;
    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {

	return MediaType.APPLICATION_XML_TYPE;
    }
}
