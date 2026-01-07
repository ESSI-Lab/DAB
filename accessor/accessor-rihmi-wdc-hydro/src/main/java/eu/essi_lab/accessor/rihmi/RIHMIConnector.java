package eu.essi_lab.accessor.rihmi;

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.w3c.dom.Node;

import eu.essi_lab.adk.timeseries.StationConnector;
import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.Dimension;
import eu.essi_lab.iso.datamodel.classes.GridSpatialRepresentation;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.Chronometer;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLNodeReader;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSProperty;
import eu.essi_lab.model.GSPropertyHandler;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Country;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.InterpolationType;
import eu.essi_lab.model.resource.OriginalMetadata;

public class RIHMIConnector extends StationConnector<RIHMIConnectorSetting> {

    public static final String TYPE = "RIHMIConnector";
    private static final String RIHMI_CONNECTOR_ERROR = "RIHMI_CONNECTOR_ERROR";

    @Override
    public boolean supports(GSSource source) {
	String url = source.getEndpoint();
	return url.contains("ws.meteo.ru") || url.contains("hydroweb.meteo.ru");
    }

    private RIHMIClient client = null;

    private List<Node> stationList = new ArrayList<Node>();

    @Override
    public ListRecordsResponse<OriginalMetadata> listTimeseries(String stationId) throws GSException {
	List<String> stations = new ArrayList<>();
	stations.add(stationId);
	return listRecords(stations);
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {
	List<String> stationIdentifiers;
	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<OriginalMetadata>();
	try {
	    if (client == null) {
		client = new RIHMIClient();
	    }

	    if (getSourceURL().contains(client.getHydrolareStationEndpoint())) {
		HttpResponse<InputStream> response = null;
		String token = request.getResumptionToken();
		int start = 0;
		if (token != null) {
		    start = Integer.valueOf(token);
		} else {
		    response = client.getDownloadResponse(getSourceURL());
		    XMLDocumentReader reader = new XMLDocumentReader(response.body());
		    Node[] nodes = reader.evaluateNodes("//*:observationMember");
		    for (Node n : nodes) {
			OriginalMetadata om = new OriginalMetadata();
			om = getOM(reader, n);
			if (om != null) {
			    ret.addRecord(om);
			}
		    }
		}

		ret.setResumptionToken(null);

		return ret;
	    }

	    stationIdentifiers = new ArrayList<>(getStationIdentifiers(getSourceURL()));
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    RIHMI_CONNECTOR_ERROR, //
		    e//
	    );
	}
	stationIdentifiers.sort(new Comparator<String>() {
	    @Override
	    public int compare(String o1, String o2) {
		return o1.compareTo(o2);
	    }
	});
	String token = request.getResumptionToken();
	if (token == null) {
	    token = stationIdentifiers.get(0);
	}
	ret = listTimeseries(token);
	for (int i = 0; i < stationIdentifiers.size(); i++) {
	    String stationIdentifier = stationIdentifiers.get(i);
	    if (token.equals(stationIdentifier)) {
		if (i != stationIdentifiers.size() - 1) {
		    ret.setResumptionToken(stationIdentifiers.get(i + 1));
		    break;
		}
	    }
	}
	return ret;

    }

    private OriginalMetadata getOM(XMLDocumentReader reader, Node row) {
	OriginalMetadata record = new OriginalMetadata();
	try {
	    Dataset dataset = new Dataset();
	    GSSource source = new GSSource();
	    source.setEndpoint(getSourceURL());
	    dataset.setSource(source);
	    // String shortName = reader.evaluateString(row, "*:td[@class='shortname']/*:a");
	    String from = reader.evaluateString(row, "*:OM_Observation/*:phenomenonTime/*:TimePeriod/*:beginPosition");
	    String to = reader.evaluateString(row, "*:OM_Observation/*:phenomenonTime/*:TimePeriod/*:endPosition");
	    String pos = reader.evaluateString(row, "*:OM_Observation/*:featureOfInterest/*:MonitoringPoint/*:shape/*:Point/*:pos");
	    String[] split = new String[2];
	    split = pos.split(" ");

	    String start = null;
	    String end = null;
	    if (from != null && !from.isEmpty()) {
		start = getIsoDateFromYear(from);
	    }

	    if (to != null && !to.isEmpty()) {
		end = getIsoDateFromYear(to);
	    }

	    String stationId = reader.evaluateString(row, "*:OM_Observation/*:featureOfInterest/*:MonitoringPoint/*:identifier");

	    RIHMIMetadata rm = new RIHMIMetadata();
	    rm.setStationId(stationId);
	    dataset.setOriginalId("Hydrolare:" + stationId);

	    if (split.length > 1 && split[0] != null) {
		rm.setLatitude(new BigDecimal(split[0]));
		rm.setLongitude(new BigDecimal(split[1]));
	    }

	    rm.setParameterId("RIHMI:WaterLevel");
	    rm.setParameterName("Water Level");

	    // if (url.contains(client.getAralWaterLevelEndpoint()) ||
	    // url.contains(client.getMoldovaWaterLevelEndpoint())) {

	    // } else if (url.contains(client.getAralDischargeEndpoint()) ||
	    // url.contains(client.getMoldovaDischargeEndpoint())
	    // || url.contains(client.getHistoricalEndpoint()) || url.contains(client.getRealTimeEndpoint())) {
	    // rm.setParameterId("RIHMI:Discharge");
	    // rm.setParameterName("Discharge");
	    // } else if (url.contains(client.getAralWaterTemperatureEndpoint())
	    // || url.contains(client.getMoldovaWaterTemperatureEndpoint())) {
	    // rm.setParameterId("RIHMI:WaterTemperature");
	    // rm.setParameterName("Water Temperature");
	    // }

	    String stationName = reader.evaluateString(row,
		    "*:OM_Observation/*:featureOfInterest//*:NamedValue[*:name/@*:title='station name']/*:value/*:CharacterString/text()");

	    String name = reader.evaluateString(row,
		    "*:OM_Observation/*:featureOfInterest/*:MonitoringPoint[1]/*:parameter[1]/*:NamedValue[1]/*:value[1]/*:CharacterString[1]");
	    rm.setStationName(name);

	    String units = reader.evaluateString(row,
		    "*:OM_Observation/*:result/*:MeasurementTimeseries[1]/*:defaultPointMetadata[1]/*:DefaultTVPMeasurementMetadata[1]/*:uom[1]/@code");
	    rm.setUnits(units);

	    String interpolation = reader.evaluateString(row,
		    "*:OM_Observation/*:result/*:MeasurementTimeseries[1]/*:defaultPointMetadata[1]/*:DefaultTVPMeasurementMetadata[1]/*:interpolationType[1]/@*:href");

	    if (interpolation != null && !interpolation.isEmpty()) {
		if (interpolation.equals("http://www.opengis.net/def/waterml/2.0/interpolationType/AverageSucc")) {
		    rm.setInterpolation(InterpolationType.AVERAGE_SUCC);
		} else {
		    GSLoggerFactory.getLogger(getClass()).error("Interpolation not recognized: {}", interpolation);
		}
	    }

	    String aggregationDuration = reader.evaluateString(row,
		    "*:OM_Observation/*:result/*:MeasurementTimeseries[1]/*:defaultPointMetadata[1]/*:DefaultTVPMeasurementMetadata[1]/*:aggregationDuration[1]");
	    if (aggregationDuration != null && !aggregationDuration.isEmpty()) {
		rm.setAggregationDuration(aggregationDuration);
	    }

	    // ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    // rm.marshal(baos);
	    // String str = new String(baos.toByteArray());
	    // try {
	    // baos.close();
	    // } catch (IOException e) {
	    // e.printStackTrace();
	    // }

	    String orgNamePub = reader.evaluateString(row,
		    "*:OM_Observation/*:metadata/*:ObservationMetadata/*:contact[*:CI_ResponsibleParty/*:role/*:CI_RoleCode/@codeListValue='publisher']/*:CI_ResponsibleParty/*:organisationName/*:CharacterString/text()");
	    String emailPub = reader.evaluateString(row,
		    "*:OM_Observation/*:metadata/*:ObservationMetadata/*:contact[*:CI_ResponsibleParty/*:role/*:CI_RoleCode/@codeListValue='publisher']//*:electronicMailAddress/*:CharacterString/text()");

	    String orgNameOriginator = reader.evaluateString(row,
		    "*:OM_Observation/*:metadata/*:ObservationMetadata/*:contact[*:CI_ResponsibleParty/*:role/*:CI_RoleCode/@codeListValue='originator']/*:CI_ResponsibleParty/*:organisationName/*:CharacterString/text()");
	    String emailOriginator = reader.evaluateString(row,
		    "*:OM_Observation/*:metadata/*:ObservationMetadata/*:contact[*:CI_ResponsibleParty/*:role/*:CI_RoleCode/@codeListValue='originator']//*:electronicMailAddress/*:CharacterString/text()");

	    String country = reader.evaluateString(row,
		    "*:OM_Observation/*:featureOfInterest//*:NamedValue[*:name/@*:title='country']/*:value/*:CharacterString/text()");

	    CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	    // TIME
	    if (start != null && end != null) {
		TemporalExtent extent = new TemporalExtent();
		extent.setBeginPosition(start);
		extent.setEndPosition(end);
		coreMetadata.getMIMetadata().getDataIdentification().addTemporalExtent(extent);
		GridSpatialRepresentation grid = new GridSpatialRepresentation();
		grid.setNumberOfDimensions(1);
		grid.setCellGeometryCode("point");
		Dimension time = new Dimension();
		time.setDimensionNameTypeCode("time");
	    }

	    String interpolationString = "";
	    if (interpolation != null && !interpolation.isEmpty()) {
		if (interpolation.equals(InterpolationType.AVERAGE_SUCC)) {
		    interpolationString = "(" + aggregationDuration + " average)";
		} else {
		    interpolationString = "(" + aggregationDuration + " " + interpolation + ")";
		}
	    }

	    coreMetadata.setTitle((rm.getParameterName() + " acquisitions " + interpolationString + " at station " + stationName));
	    coreMetadata.setAbstract("This dataset contains a hydrology time series of a specific variable (" + rm.getParameterName()
		    + ") acquired by RIHMI-WDC station " + stationName);

	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword(stationName);

	    coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");
	    // BBOX
	    if (rm.getLatitude() != null && rm.getLongitude() != null) {
		coreMetadata.addBoundingBox(rm.getLatitude(), rm.getLongitude(), rm.getLatitude(), rm.getLongitude());
	    } else {
		return null;
	    }

	    // RESPONSIBLES
	    if (orgNameOriginator != null && !orgNameOriginator.isEmpty()) {
		ResponsibleParty originatorContact = new ResponsibleParty();

		originatorContact.setOrganisationName(orgNameOriginator);
		originatorContact.setRoleCode("originator");
		if (emailOriginator != null && !emailOriginator.isEmpty()) {
		    Contact contact = new Contact();
		    Address address = new Address();
		    address.addElectronicMailAddress(emailOriginator);
		    contact.setAddress(address);
		    originatorContact.setContactInfo(contact);
		}
		coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(originatorContact);
	    }

	    if (orgNamePub != null && !orgNamePub.isEmpty()) {
		ResponsibleParty publisherContact = new ResponsibleParty();

		publisherContact.setOrganisationName(orgNamePub);
		publisherContact.setRoleCode("publisher");
		if (emailPub != null && !emailPub.isEmpty()) {
		    Contact contact = new Contact();
		    Address address = new Address();
		    address.addElectronicMailAddress(emailPub);
		    contact.setAddress(address);
		    publisherContact.setContactInfo(contact);
		}
		coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(publisherContact);
	    }

	    ResponsibleParty creatorContact = new ResponsibleParty();

	    creatorContact.setOrganisationName("RIHMI-WDC");
	    creatorContact.setRoleCode("pointOfContact");
	    Contact contact = new Contact();
	    Address address = new Address();
	    address.addElectronicMailAddress("shevchen2007@yandex.ru");
	    contact.setAddress(address);
	    creatorContact.setContactInfo(contact);
	    coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(creatorContact);

	    /**
	     * MIPLATFORM
	     **/

	    MIPlatform platform = new MIPlatform();

	    platform.setMDIdentifierCode("urn:rihmi:station:" + stationId);

	    platform.setDescription(stationName);

	    Citation platformCitation = new Citation();
	    platformCitation.setTitle(stationName);
	    platform.setCitation(platformCitation);

	    coreMetadata.getMIMetadata().addMIPlatform(platform);

	    /**
	     * COVERAGEDescription
	     **/

	    CoverageDescription coverageDescription = new CoverageDescription();

	    coverageDescription.setAttributeIdentifier("urn:ru:meteo:ws:variable:" + rm.getParameterName() + interpolationString);
	    coverageDescription.setAttributeTitle(rm.getParameterName());

	    String attributeDescription = rm.getParameterName() + " Units: " + units;

	    coverageDescription.setAttributeDescription(attributeDescription);
	    coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);

	    if (interpolation != null && !interpolation.isEmpty()) {
		dataset.getExtensionHandler().setTimeInterpolation(interpolation);
	    }
	    if (aggregationDuration != null && !aggregationDuration.isEmpty()) {
		if (aggregationDuration.equals("P1M")) {
		    dataset.getExtensionHandler().setTimeUnits("month");
		    dataset.getExtensionHandler().setTimeSupport("1");
		} else if (aggregationDuration.equals("P1D")) {
		    dataset.getExtensionHandler().setTimeUnits("day");
		    dataset.getExtensionHandler().setTimeSupport("1");
		} else {
		    GSLoggerFactory.getLogger(getClass()).error("Unrecognized aggregation duration: {}", aggregationDuration);
		}
	    }
	    
	    dataset.getExtensionHandler().setAttributeUnits(units);
	    dataset.getExtensionHandler().setAttributeUnitsAbbreviation(units);

	    // COUNTRY
	    if (country != null && !country.isEmpty()) {
		Country c = Country.decode(country);
		if (c != null)
		    dataset.getExtensionHandler().setCountry(c.getShortName());
	    }
	    dataset.getPropertyHandler().setIsTimeseries(true);
	    String str = dataset.asString(true);
	    record.setMetadata(str);
	    record.setSchemeURI(CommonNameSpaceContext.GS_DATA_MODEL_SCHEMA_URI_GS_RESOURCE);

	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    return null;
	}
	
	return record;

    }

    private Set<String> stationIdentifiers = null;

    private Set<String> getStationIdentifiers(String linkage) throws Exception {

	if (client == null) {
	    client = new RIHMIClient();
	}

	if (stationIdentifiers != null) {
	    return stationIdentifiers;
	}
	stationIdentifiers = client.getStationIdentifiers(linkage);
	return stationIdentifiers;
    }

    public ListRecordsResponse<OriginalMetadata> listRecords(List<String> stationIdentifiers) throws GSException {

	try {
	    if (client == null) {
		client = new RIHMIClient();
	    }

	    ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<OriginalMetadata>();

	    for (String stationId : stationIdentifiers) {

		if (stationId.length() == 4) {
		    stationId = "0" + stationId;
		    GSLoggerFactory.getLogger(getClass()).error("modified {}", stationId);
		}

		List<String> downloadUrls = new ArrayList<>();
		String sourceURL = getSourceURL();

		if (sourceURL.contains(client.getAralStationEndpoint())) {
		    // water level
		    downloadUrls.add(getRealtimeDownloadUrl(client.getAralWaterLevelEndpoint(), stationId));
		    // discharges
		    downloadUrls.add(getRealtimeDownloadUrl(client.getAralDischargeEndpoint(), stationId));
		    // water temperature
		    downloadUrls.add(getRealtimeDownloadUrl(client.getAralWaterTemperatureEndpoint(), stationId));

		} else if (sourceURL.contains(client.getMoldovaStationEndpoint())) {
		    // water level
		    downloadUrls.add(getRealtimeDownloadUrl(client.getMoldovaWaterLevelEndpoint(), stationId));
		    // discharges
		    downloadUrls.add(getRealtimeDownloadUrl(client.getMoldovaDischargeEndpoint(), stationId));
		    // water temperature
		    downloadUrls.add(getRealtimeDownloadUrl(client.getMoldovaWaterTemperatureEndpoint(), stationId));
		} else {
		    // real time download url
		    downloadUrls.add(getRealtimeDownloadUrl(getSourceURL(), stationId));
		    // historical download url
		    downloadUrls.add(client.getHistoricalDownloadUrl(stationId));
		}

		int count = 0;
		for (String url : downloadUrls) {
		    count++;
		    OriginalMetadata metadataRecord = new OriginalMetadata();

		    metadataRecord.setSchemeURI(CommonNameSpaceContext.RIHMI_URI);

		    HttpResponse<InputStream> response;
		    try {
			response = client.getDownloadResponse(url);
		    } catch (Exception e) {
			GSLoggerFactory.getLogger(getClass()).error("Error ({}) while downloading from station {} Reference URL: {}",
				e.getMessage(), stationId, url);
			continue;
		    }

		    Integer status = response.statusCode();

		    if (status != 200) {
			GSLoggerFactory.getLogger(getClass())
				.error("Error (HTTP code {}) while downloading from station {} Reference URL: {}", status, stationId, url);
			continue;
		    }

		    XMLDocumentReader reader = new XMLDocumentReader(response.body());

		    if (reader.asString().contains("Internal Server Error")) {
			GSLoggerFactory.getLogger(getClass())
				.error("Error (HTTP code {}) while downloading from station {} Reference URL: {}", status, stationId, url);
			continue;
		    }

		    String from = normalizeTime(reader.evaluateString("//*:TimePeriod/*:beginPosition"));
		    if (from == null || from.isEmpty()) {
			from = reader.evaluateString("//*:MeasurementTVP[1]/*:time[1]");
		    }

		    String to = normalizeTime(reader.evaluateString("//*:TimePeriod/*:endPosition"));
		    if (to == null || to.isEmpty()) {
			to = ISO8601DateTimeUtils.getISO8601DateTime();
		    }

		    RIHMIMetadata rm = new RIHMIMetadata();
		    if (from == null || from.isEmpty()) {
			GSLoggerFactory.getLogger(getClass()).info("No data while downloading from station {} Reference URL: {}", stationId,
				url);
			GSLoggerFactory.getLogger(getClass()).info("Try again with larger date range");
			String[] splittedURL = url.split("\\?");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

			String dateFromStr = "2020-01-01 00:00";
			String dateFrom = "";
			String dateTo = URLEncoder.encode(sdf.format(new Date()), "UTF-8");
			try {
			    // Parse the string to Date
			    Date fixedDate = sdf.parse(dateFromStr);

			    // Format it back (ensures correct timezone formatting)
			    String formattedDate = sdf.format(fixedDate);

			    // URL encode the formatted date
			    dateFrom = URLEncoder.encode(formattedDate, "UTF-8");
			} catch (Exception e) {
			    e.printStackTrace();
			}
			String newURL = splittedURL[0] + "?dateFrom=" + dateFrom + "&dateTo=" + dateTo + "&index=" + stationId;

			try {
			    response = client.getDownloadResponse(newURL);
			} catch (Exception e) {
			    GSLoggerFactory.getLogger(getClass()).error("Error ({}) while downloading from station {} Reference URL: {}",
				    e.getMessage(), stationId, newURL);
			    continue;
			}

			status = response.statusCode();

			if (status != 200) {
			    GSLoggerFactory.getLogger(getClass()).error(
				    "Error (HTTP code {}) while downloading from station {} Reference URL: {}", status, stationId, url);
			    continue;
			}

			reader = new XMLDocumentReader(response.body());

			if (reader.asString().contains("Internal Server Error")) {
			    GSLoggerFactory.getLogger(getClass()).error(
				    "Error (HTTP code {}) while downloading from station {} Reference URL: {}", status, stationId, url);
			    continue;
			}

			from = normalizeTime(reader.evaluateString("//*:TimePeriod/*:beginPosition"));
			if (from == null || from.isEmpty()) {
			    from = reader.evaluateString("//*:MeasurementTVP[1]/*:time[1]");
			}

			to = normalizeTime(reader.evaluateString("//*:TimePeriod/*:endPosition"));
			if (to == null || to.isEmpty()) {
			    to = ISO8601DateTimeUtils.getISO8601DateTime();
			}

			if (from == null || from.isEmpty()) {
			    if (count == 3) {
				ret.setResumptionToken(null);
				return ret;
			    } else {
				continue;
			    }
			}

		    }
		    ISO8601DateTimeUtils.parseISO8601ToDate(from).ifPresent(d -> rm.setBegin(d));
		    ISO8601DateTimeUtils.parseISO8601ToDate(to).ifPresent(d -> rm.setEnd(d));

		    String pos = reader.evaluateString("//*:shape/*:Point/*:pos");
		    String[] split = new String[2];
		    if (sourceURL.contains(client.getAralStationEndpoint())) {
			String[] splittedPos = pos.split(", ");
			if (splittedPos != null && splittedPos.length > 1) {
			    split[0] = splittedPos[0].replace(",", ".");
			    split[1] = splittedPos[1].replace(",", ".");
			}
		    } else {
			pos = pos.replace(",", "");
			split = pos.split(" ");
		    }

		    if (split.length > 1 && split[0] != null) {
			rm.setLatitude(new BigDecimal(split[0]));
			rm.setLongitude(new BigDecimal(split[1]));
		    }
		    if (url.contains(client.getAralWaterLevelEndpoint()) || url.contains(client.getMoldovaWaterLevelEndpoint())) {
			rm.setParameterId("RIHMI:WaterLevel");
			rm.setParameterName("Water Level");
		    } else if (url.contains(client.getAralDischargeEndpoint()) || url.contains(client.getMoldovaDischargeEndpoint())
			    || url.contains(client.getHistoricalEndpoint()) || url.contains(client.getRealTimeEndpoint())) {
			rm.setParameterId("RIHMI:Discharge");
			rm.setParameterName("Discharge");
		    } else if (url.contains(client.getAralWaterTemperatureEndpoint())
			    || url.contains(client.getMoldovaWaterTemperatureEndpoint())) {
			rm.setParameterId("RIHMI:WaterTemperature");
			rm.setParameterName("Water Temperature");
		    }
		    rm.setStationId(stationId);
		    String name = reader
			    .evaluateString("//*:MonitoringPoint[1]/*:parameter[1]/*:NamedValue[1]/*:value[1]/*:CharacterString[1]");
		    rm.setStationName(name);

		    String units = reader.evaluateString(
			    "//*:MeasurementTimeseries[1]/*:defaultPointMetadata[1]/*:DefaultTVPMeasurementMetadata[1]/*:uom[1]/@code");
		    rm.setUnits(units);

		    String interpolation = reader.evaluateString(
			    "//*:MeasurementTimeseries[1]/*:defaultPointMetadata[1]/*:DefaultTVPMeasurementMetadata[1]/*:interpolationType[1]/@*:href");

		    if (interpolation != null && !interpolation.isEmpty()) {
			if (interpolation.equals("http://www.opengis.net/def/waterml/2.0/interpolationType/AverageSucc")) {
			    rm.setInterpolation(InterpolationType.AVERAGE_SUCC);
			} else {
			    GSLoggerFactory.getLogger(getClass()).error("Interpolation not recognized: {}", interpolation);
			}
		    } else if (sourceURL.contains(client.getMoldovaStationEndpoint())
			    || sourceURL.contains(client.getAralStationEndpoint())) {
			rm.setInterpolation(InterpolationType.DISCONTINUOUS);
		    }

		    String aggregationDuration = reader.evaluateString(
			    "//*:MeasurementTimeseries[1]/*:defaultPointMetadata[1]/*:DefaultTVPMeasurementMetadata[1]/*:aggregationDuration[1]");
		    if (aggregationDuration != null && !aggregationDuration.isEmpty()) {
			rm.setAggregationDuration(aggregationDuration);
		    } else if (sourceURL.contains(client.getMoldovaStationEndpoint())
			    || sourceURL.contains(client.getAralStationEndpoint())) {
			rm.setAggregationDuration("P1D");
		    }

		    ByteArrayOutputStream baos = new ByteArrayOutputStream();
		    rm.marshal(baos);
		    String str = new String(baos.toByteArray());
		    try {
			baos.close();
		    } catch (IOException e) {
			e.printStackTrace();
		    }

		    metadataRecord.setMetadata(str);

		    // GSPropertyHandler handler = GSPropertyHandler.of(new GSProperty<Boolean>("isAral", isAral));
		    if (sourceURL.contains(client.getAralStationEndpoint()) || sourceURL.contains(client.getMoldovaStationEndpoint())) {
			GSPropertyHandler handler = GSPropertyHandler.of(new GSProperty<String>("downloadLink", url));
			metadataRecord.setAdditionalInfo(handler);
		    }

		    ret.addRecord(metadataRecord);

		}
	    }

	    ret.setResumptionToken(null);

	    return ret;
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    RIHMI_CONNECTOR_ERROR, //
		    e//
	    );
	}
    }

    private String normalizeTime(String time) {
	if (time == null) {
	    return null;
	}
	time = time.trim();
	if (time.isEmpty()) {
	    return null;
	}
	if (time.endsWith("Z")) {
	    return time.replace(" ", "");
	} else {
	    return time.replace(" ", "") + "Z";
	}
    }

    private String getIsoDateFromYear(String yearStr) {
	// Convert year string to integer
	int year = Integer.parseInt(yearStr);

	// Prepare a Calendar for UTC timezone
	Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	calendar.clear(); // Reset fields
	calendar.set(Calendar.YEAR, year); // Set only the year -> 1 Jan 00:00:00 UTC
	Date date = calendar.getTime();

	// Format to ISO 8601
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
	return sdf.format(date);
    }

    public static String getRealtimeDownloadUrl(String url, String stationId) {
	if (url.contains("?")) {
	    url = url.substring(0, url.indexOf("?"));
	}
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm");// 2020-01-02T06:06:30.000+0000
	sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
	String dateFrom = sdf.format(new Date(new Date().getTime() - 1000 * 60 * 60 * 24 * 180l));
	try {
	    dateFrom = URLEncoder.encode(dateFrom, "UTF-8");
	} catch (UnsupportedEncodingException e) {
	    e.printStackTrace();
	}
	String dateTo = sdf.format(new Date());
	try {
	    dateTo = URLEncoder.encode(dateTo, "UTF-8");
	} catch (UnsupportedEncodingException e) {
	    e.printStackTrace();
	}
	url = url + "?dateFrom=" + dateFrom + "&dateTo=" + dateTo + "&index=" + stationId;
	return url;
    }

    public static String extractStationId(String url) {
	String ret = "70801"; // by default
	if (url.contains("index=")) {
	    ret = url.substring(url.indexOf("index="));
	    ret = ret.substring(ret.indexOf("=") + 1);
	    if (ret.contains("&")) {
		ret = ret.substring(0, ret.indexOf("&"));
	    }
	}
	return ret;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<String>();
	ret.add(CommonNameSpaceContext.RIHMI_URI);
	ret.add(CommonNameSpaceContext.GS_DATA_MODEL_SCHEMA_URI_GS_RESOURCE);
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected RIHMIConnectorSetting initSetting() {

	return new RIHMIConnectorSetting();
    }

}
