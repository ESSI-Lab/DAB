package eu.essi_lab.accessor.wis;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.Dimension;
import eu.essi_lab.iso.datamodel.classes.GridSpatialRepresentation;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.LegalConstraints;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ReferenceSystem;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent.FrameValue;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Country;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.AbstractResourceMapper;
import net.opengis.gml.v_3_2_0.TimeIndeterminateValueType;
import net.opengis.iso19139.gmd.v_20060504.MDTopicCategoryCodeType;

/**
 * @author boldrini
 */
public class WISConnector extends HarvestedQueryConnector<WISConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "WISConnector";
    /**
     * 
     */
    private static final String WIS_CONNECTOR_SOURCE_NOT_FOUND_ERROR = "WIS_CONNECTOR_SOURCE_NOT_FOUND_ERROR";

    @Override
    public boolean supports(GSSource source) {
	String url = source.getEndpoint();
	if (!url.endsWith("oapi")) {

	    return false;
	}
	if (!this.getSourceURL().equals(url)) {
	    return false;
	}
	try {
	    WISClient client = new WISClient(url);
	    if (!client.getStations().isEmpty()) {
		return true;
	    }
	} catch (Exception e) {
	}

	return false;
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	Optional<GSSource> source = ConfigurationWrapper.getHarvestedSources().stream().filter(s -> this.supports(s)).findFirst();

	if (!source.isPresent()) {

	    throw GSException.createException(//
		    getClass(), //
		    "Unable to find connector source", //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    WIS_CONNECTOR_SOURCE_NOT_FOUND_ERROR);
	}

	WISClient client = new WISClient(getSourceURL());
	List<Station> stations = client.getStations();
	JSONObject metadata = client.getMetadata();
	JSONArray features = metadata.getJSONArray("features");
	JSONObject feature = features.getJSONObject(0);
	JSONObject properties = feature.getJSONObject("properties");
	String description = properties.getString("description");
	JSONArray themes = properties.getJSONArray("themes");
	String title = properties.getString("title");
	String rights = properties.optString("rights");
	JSONArray providers = new JSONArray();
	if (properties.has("providers")) {
	    providers = properties.getJSONArray("providers");
	} else if (properties.has("contacts")) {
	    providers = properties.getJSONArray("contacts");
	}

	final int GET_VARIABLES_MAX_ATTEMPTS = 2;
	final int GET_VARIABLES_ATTEMPTS_TIME = 5;// seconds

	int getVarAttempts = 0;

	GSLoggerFactory.getLogger(getClass()).info("Adding station STARTED");

	for (Station station : stations) {

	    getVarAttempts = 0;

	    HashSet<ObservedProperty> observedProperties = null;

	    while (observedProperties == null) {

		try {

		    observedProperties = client.getVariables(station.getWigosId());
		} catch (Exception ex) {

		    GSLoggerFactory.getLogger(getClass()).error("Unable to retrieve variables: " + ex.getMessage());

		    if (getVarAttempts > GET_VARIABLES_MAX_ATTEMPTS) {

			GSLoggerFactory.getLogger(getClass()).error("Max number of attempts reached, exit");
			return ret;
		    }

		    GSLoggerFactory.getLogger(getClass()).error("Waiting a while for attempt #" + getVarAttempts + "...");

		    try {
			Thread.sleep(TimeUnit.SECONDS.toMillis(GET_VARIABLES_ATTEMPTS_TIME));
		    } catch (InterruptedException e) {
		    }

		    getVarAttempts++;
		}
	    }

	    for (ObservedProperty observedProperty : observedProperties) {

		Dataset dataset = new Dataset();
		dataset.setSource(source.get());

		String stationId = station.getWigosId();
		String parameterId = observedProperty.getName();

		String platformIdentifier = stationId;
		String parameterIdentifier = "WIS:" + parameterId;
		CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

		MIMetadata miMetadata = dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();

		miMetadata.setCharacterSetCode("utf8");

		miMetadata.addHierarchyLevelScopeCodeListValue("dataset");

		MIPlatform platform = new MIPlatform();
		platform.setMDIdentifierCode(platformIdentifier);
		platform.setDescription(station.getName());
		Citation citation = new Citation();

		citation.setTitle(station.getName());
		platform.setCitation(citation);

		miMetadata.addMIPlatform(platform);

		try {
		    BigDecimal north = station.getLatitude();
		    BigDecimal east = station.getLongitude();
		    if (north.doubleValue() > 90 || north.doubleValue() < -90) {
			String warn = "Invalid latitude for station: " + platformIdentifier;
			GSLoggerFactory.getLogger(getClass()).error(warn);
		    }
		    if (east.doubleValue() > 180 || east.doubleValue() < -180) {
			String warn = "Invalid longitude for station: " + platformIdentifier;
			GSLoggerFactory.getLogger(getClass()).error(warn);
		    }
		    coreMetadata.getMIMetadata().getDataIdentification().addGeographicBoundingBox(north, east, north, east);
		} catch (Exception e) {
		    GSLoggerFactory.getLogger(getClass()).error("Unable to parse site latitude/longitude: " + e.getMessage());
		}

		for (int i = 0; i < themes.length(); i++) {
		    JSONObject theme = themes.getJSONObject(i);
		    JSONArray concepts = theme.getJSONArray("concepts");
		    Keywords keyword = new Keywords();
		    for (int j = 0; j < concepts.length(); j++) {
			String concept = "";
			JSONObject concObj = concepts.optJSONObject(j);
			if (concObj != null) {
			    concept = concObj.optString("id");
			} else {
			    concept = concepts.optString(j);
			}
			keyword.addKeyword(concept);
		    }
		    if (theme.has("scheme")) {
			String scheme = theme.getString("scheme");
			keyword.setThesaurusNameCitationTitle(scheme);
		    }
		    coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);
		}
		coreMetadata.getMIMetadata().getDataIdentification().addKeyword(station.getName());
		coreMetadata.getMIMetadata().getDataIdentification().addKeyword(observedProperty.getName());
		if (rights != null && !rights.isEmpty()) {
		    LegalConstraints lc = new LegalConstraints();
		    lc.addOtherConstraints(rights);
		    coreMetadata.getMIMetadata().getDataIdentification().addLegalConstraints(lc);
		}

		for (int i = 0; i < providers.length(); i++) {
		    JSONObject provider = providers.getJSONObject(i);
		    List<String> roles = new ArrayList<>();
		    JSONArray rolesJSON = provider.getJSONArray("roles");
		    for (int j = 0; j < rolesJSON.length(); j++) {
			JSONObject roleJSON = rolesJSON.optJSONObject(j);
			if (roleJSON != null) {
			    String roleName = roleJSON.getString("name");
			    roles.add(roleName);
			} else {
			    String roleName = rolesJSON.getString(j);
			    roles.add(roleName);
			}
		    }
		    try {
			for (String role : roles) {
			    String organization = provider.optString("organization");
			    String individual = provider.optString("name");
			    String position = provider.optString("positionName");

			    ResponsibleParty poc = new ResponsibleParty();
			    poc.setIndividualName(individual);
			    poc.setOrganisationName(organization);
			    poc.setPositionName(position);
			    poc.setRoleCode(role);
			    coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(poc);

			    String phoneAddress = null;
			    List<String> emailAddresses = new ArrayList<String>();
			    String deliveryPoint = null;
			    String city = null;
			    String administrativeArea = null;
			    String postalCode = null;
			    String country = null;
			    Online online = new Online();

			    JSONObject contactInfo = provider.optJSONObject("contactInfo");
			    if (contactInfo != null) {
				JSONObject phone = contactInfo.optJSONObject("phone");
				if (phone != null) {
				    phoneAddress = phone.optString("office");
				}
				JSONObject email = contactInfo.optJSONObject("email");
				if (email != null) {
				    String em = email.optString("office");
				    if (em != null) {
					emailAddresses.add(em);
				    }
				}
				JSONObject addressJSON = contactInfo.optJSONObject("address");
				if (addressJSON != null) {
				    JSONObject officeAddress = addressJSON.optJSONObject("office");
				    if (officeAddress != null) {
					deliveryPoint = officeAddress.optString("deliveryPoint");
					city = officeAddress.optString("city");
					administrativeArea = officeAddress.optString("administrativeArea");
					postalCode = officeAddress.optString("postalCode");
					country = officeAddress.getString("country");
				    }
				    if (addressJSON.has("onlineResource")) {
					JSONObject onlineResource = addressJSON.getJSONObject("onlineResource");

					online.setLinkage(onlineResource.getString("href"));

				    }
				}
			    } else {
				JSONArray emails = provider.optJSONArray("emails");
				if (emails != null) {
				    for (int j = 0; j < emails.length(); j++) {
					Object emailObject = emails.get(j);
					if (emailObject instanceof String) {
					    String str = (String) emailObject;
					    emailAddresses.add(str);
					} else if (emailObject instanceof JSONObject) {
					    JSONObject email = (JSONObject) emailObject;
					    String v = email.optString("value");
					    if (v != null) {
						emailAddresses.add(v);
					    }
					}
				    }
				}
				JSONArray addresses = provider.optJSONArray("addresses");
				if (addresses != null && addresses.length() > 0) {
				    JSONObject address = addresses.optJSONObject(0);
				    deliveryPoint = address.optString("deliveryPoint");
				    city = address.optString("city");
				    administrativeArea = address.optString("administrativeArea");
				    postalCode = address.optString("postalCode");
				    country = address.getString("country");
				}
			    }
			    Contact contact = new Contact();
			    contact.addPhoneVoice(phoneAddress);
			    Address address = new Address();

			    for (String email : emailAddresses) {
				address.addElectronicMailAddress(email);
			    }

			    contact.setAddress(address);
			    poc.setContactInfo(contact);

			    address.addDeliveryPoint(deliveryPoint);
			    address.setCity(city);
			    address.setAdministrativeArea(administrativeArea);

			    address.setPostalCode(postalCode);
			    address.setCountry(country);
			    contact.setOnline(online);
			    if (country != null) {
				Country c = Country.decode(country);
				if (c != null) {
				    dataset.getExtensionHandler().setCountry(c.getShortName());
				    dataset.getExtensionHandler().setCountryISO3(c.getISO3());
				} else {
				    dataset.getExtensionHandler().setCountry(country);
				}
			    }
			}
		    } catch (Exception e) {
			GSLoggerFactory.getLogger(getClass()).error(e);
		    }
		}

		ReferenceSystem referenceSystem = new ReferenceSystem();
		referenceSystem.setCode("4326");
		referenceSystem.setCodeSpace("EPSG");
		coreMetadata.getMIMetadata().addReferenceSystemInfo(referenceSystem);

		CoverageDescription coverageDescription = new CoverageDescription();

		coverageDescription.setAttributeIdentifier(parameterIdentifier);
		coverageDescription.setAttributeTitle(observedProperty.getName());
		coverageDescription.setAttributeDescription(observedProperty.getName() + " (" + observedProperty.getUnits() + ")");

		// InterpolationType interpolation = InterpolationType.TOTAL;
		// dataset.getExtensionHandler().setTimeInterpolation(interpolation);
		// dataset.getExtensionHandler().setTimeUnits(variable.getAggregationPeriodUnits());
		// dataset.getExtensionHandler().setTimeUnitsAbbreviation(variable.getAggregationPeriodUnits());
		// dataset.getExtensionHandler().setTimeSupport(variable.getAggregationPeriod().toString());
		// dataset.getExtensionHandler().setAttributeMissingValue(MISSING_VALUE);
		String unitName = observedProperty.getUnits();
		dataset.getExtensionHandler().setAttributeUnits(unitName);

		String unitAbbreviation = observedProperty.getUnits();
		dataset.getExtensionHandler().setAttributeUnitsAbbreviation(unitAbbreviation);

		coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);

		TemporalExtent temporalExtent = new TemporalExtent();

		Date beginDate = client.getObservations(station.getWigosId(), observedProperty.getName(), null, null, 1, true).get(0)
			.getDate();
		Date endDate = client.getObservations(station.getWigosId(), observedProperty.getName(), null, null, 1, false).get(0)
			.getDate();

		temporalExtent.setBeginPosition(ISO8601DateTimeUtils.getISO8601DateTime(beginDate));
		temporalExtent.setEndPosition(ISO8601DateTimeUtils.getISO8601DateTime(endDate));

		Date now = new Date();
		Date twoWeeksAgo = new Date(now.getTime() - TimeUnit.DAYS.toMillis(14));
		Date twoMonthsAgo = new Date(now.getTime() - TimeUnit.DAYS.toMillis(60));

		if (beginDate.before(twoMonthsAgo)) {
		    // not short period data
		    if (endDate.after(twoMonthsAgo)) {
			temporalExtent.setIndeterminateEndPosition(TimeIndeterminateValueType.NOW);
		    }
		} else if (beginDate.before(twoWeeksAgo)) {
		    // real time data, 1 month long period
		    temporalExtent.setBeforeNowBeginPosition(FrameValue.P1M);
		    temporalExtent.setIndeterminateEndPosition(TimeIndeterminateValueType.NOW);
		} else {
		    // real time data, 10 days long period
		    temporalExtent.setBeforeNowBeginPosition(FrameValue.P10D);
		    temporalExtent.setIndeterminateEndPosition(TimeIndeterminateValueType.NOW);
		}

		coreMetadata.getDataIdentification().addTemporalExtent(temporalExtent);

		// AbstractResourceMapper.setIndeterminatePosition(dataset);

		MDTopicCategoryCodeType topic = MDTopicCategoryCodeType.INLAND_WATERS;

		coreMetadata.getMIMetadata().getDataIdentification().addTopicCategory(topic);

		// ResponsibleParty datasetContact = new ResponsibleParty();
		// datasetContact.setOrganisationName("Dirección Nacional de Aguas (DINAGUA), Uruguay");
		// datasetContact.setRoleCode("publisher");

		// coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(datasetContact);

		coreMetadata.getMIMetadata().getDataIdentification()
			.setCitationTitle(title + ": " + station.getName() + " - " + observedProperty.getName());

		// coreMetadata.getMIMetadata().getDataIdentification().setCitationAlternateTitle(parameterDescription);

		coreMetadata.getMIMetadata().getDataIdentification().setAbstract(description + "\n\nStation: " + station.getName()
			+ "\nParameter: " + observedProperty.getName() + " Units: " + observedProperty.getUnits());

		GridSpatialRepresentation grid = new GridSpatialRepresentation();
		grid.setNumberOfDimensions(1);
		grid.setCellGeometryCode("point");
		Dimension time = new Dimension();
		time.setDimensionNameTypeCode("time");

		grid.addAxisDimension(time);
		coreMetadata.getMIMetadata().addGridSpatialRepresentation(grid);

		WISIdentifierMangler mangler = new WISIdentifierMangler();

		mangler.setWigosIdentifier(station.getWigosId());

		mangler.setVariableIdentifier(observedProperty.getName());

		String identifier = mangler.getMangling();

		coreMetadata.addDistributionOnlineResource(identifier, getSourceURL(), NetProtocolWrapper.WIS.getCommonURN(), "download");

		String resourceIdentifier = AbstractResourceMapper.generateCode(dataset, identifier);

		coreMetadata.getDataIdentification().setResourceIdentifier(resourceIdentifier);

		coreMetadata.getMIMetadata().getDistribution().getDistributionOnline().setIdentifier(resourceIdentifier);

		Online downloadOnline = coreMetadata.getOnline();

		String onlineId = downloadOnline.getIdentifier();
		if (onlineId == null) {
		    downloadOnline.setIdentifier();
		}

		downloadOnline.setIdentifier(onlineId);

		// dataset.getExtensionHandler().setCountry(Country.URUGUAY.getShortName());

		OriginalMetadata record = new OriginalMetadata();
		record.setSchemeURI(CommonNameSpaceContext.GS_DATA_MODEL_SCHEMA_URI_GS_RESOURCE);

		try {

		    record.setMetadata(dataset.asString(true));

		    ret.addRecord(record);

		} catch (Exception e) {

		    GSLoggerFactory.getLogger(getClass()).error(e);
		    GSLoggerFactory.getLogger(getClass()).error("Unable to convert current record as string: " + e.getMessage());
		}
	    }
	}

	GSLoggerFactory.getLogger(getClass()).info("Adding station ENDED");

	ret.setResumptionToken(null);
	return ret;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.GS_DATA_MODEL_SCHEMA_URI_GS_RESOURCE);
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected WISConnectorSetting initSetting() {

	return new WISConnectorSetting();
    }
}
