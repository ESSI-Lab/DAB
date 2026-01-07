package eu.essi_lab.accessor.cehq;

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

import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import eu.essi_lab.accessor.cehq.CEHQClient.CEHQProperty;
import eu.essi_lab.accessor.cehq.CEHQClient.CEHQVariable;
import eu.essi_lab.adk.timeseries.StationConnector;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
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
import net.opengis.gml.v_3_2_0.TimeIndeterminateValueType;

public class CEHQConnector extends StationConnector<CEHQConnectorSetting> {

    private static final String CEHQ_URN = "https://www.cehq.gouv.qc.ca/depot";

    /**
     * 
     */
    public static final String TYPE = "CEHQConnector";

    private static final String CEHQ_CONNECTOR_ERROR = "CEHQ_CONNECTOR_ERROR";

    private CEHQClient client;
    private List<String> identifiers = null;

    @Override
    public boolean supports(GSSource source) {
	String endpoint = source.getEndpoint();
	return endpoint.contains("www.cehq.gouv.qc.ca/hydrometrie");
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.CEHQ_URI);
	return ret;
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listTimeseries(String stationId) throws GSException {
	ListRecordsRequest request = new ListRecordsRequest();
	request.setResumptionToken(stationId);
	return listRecords(request);
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {
	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();
	init();

	if (identifiers == null) {
	    try {
		identifiers = client.getStationIdentifiers();
	    } catch (IOException e) {
		GSLoggerFactory.getLogger(getClass()).error(e);
		throw GSException.createException(//
			getClass(), //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_ERROR, //
			CEHQ_CONNECTOR_ERROR, //
			e);
	    }
	}
	String token = request.getResumptionToken();
	if (token == null) {
	    token = identifiers.get(0);
	}
	int index = 0;
	for (int i = 0; i < identifiers.size(); i++) {
	    String identifier = identifiers.get(i);
	    if (token.equals(identifier)) {
		index = i;
		break;
	    }
	}

	String stationId = identifiers.get(index);
	Map<CEHQProperty, String> properties;
	Map<CEHQVariable, SimpleEntry<Date, Date>> temporalExtents;

	try {
	    properties = client.getStationProperties(stationId);

	    temporalExtents = client.getTimeSeriesTemporalExtent(stationId);

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error(e);
	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    CEHQ_CONNECTOR_ERROR, //
		    e);
	}
	for (Entry<CEHQVariable, SimpleEntry<Date, Date>> temporalExtent : temporalExtents.entrySet()) {
	    CEHQVariable var = temporalExtent.getKey();
	    SimpleEntry<Date, Date> extent = temporalExtent.getValue();

	    Dataset dataset = new Dataset();
	    GSSource source = new GSSource();
	    source.setEndpoint(getSourceURL());
	    dataset.setSource(source);

	    CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	    CoverageDescription coverageDescription = new CoverageDescription();

	    coverageDescription.setAttributeIdentifier("urn:ca:qc:gouv:cehq:depot:variable:" + var.name());
	    coverageDescription.setAttributeTitle(var.getLabel());

	    // dataset.getExtensionHandler().setTimeInterpolation(InterpolationType.CONTINUOUS);

	    dataset.getExtensionHandler().setCountry(Country.CANADA.getShortName());

	    dataset.getExtensionHandler().setAttributeUnits(var.getUnits());

	    dataset.getExtensionHandler().setAttributeUnitsAbbreviation(var.getUnits());

	    coverageDescription.setAttributeDescription(var.getLabel());
	    coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);

	    CEHQIdentifierMangler mangler = new CEHQIdentifierMangler();
	    mangler.setPlatformIdentifier(stationId);
	    mangler.setParameterIdentifier(var.name());

	    String identifier = mangler.getMangling();
	    coreMetadata.addDistributionOnlineResource(identifier, getSourceURL(), NetProtocolWrapper.CEHQ.getCommonURN(), "download");

	    MIPlatform platform = new MIPlatform();

	    coreMetadata.getMIMetadata().addMIPlatform(platform);
	    String id = CEHQ_URN + stationId;
	    platform.setMDIdentifierCode(id);

	    ResponsibleParty datasetContact = new ResponsibleParty();
	    datasetContact.setOrganisationName("Le Centre d'expertise hydrique du Québec (CEHQ)");
	    datasetContact.setRoleCode("pointOfContact");
	    coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(datasetContact);

	    coreMetadata.getDataIdentification().setResourceIdentifier(CEHQ_URN + stationId + ":" + var.name());

	    String stationName = properties.get(CEHQProperty.STATION_NAME);
	    Citation platformCitation = new Citation();
	    platformCitation.setTitle(stationName);
	    platform.setCitation(platformCitation);

	    String beginPosition = ISO8601DateTimeUtils.getISO8601DateTime(extent.getKey());
	    Date maxDate = extent.getValue();
	    String endPosition = ISO8601DateTimeUtils.getISO8601DateTime(maxDate);
	    coreMetadata.getMIMetadata().getDataIdentification().addTemporalExtent(beginPosition, endPosition);
	    long gap = new Date().getTime() - maxDate.getTime();
	    if (gap < 1000 * 60 * 60 * 24 * 30) {
		TemporalExtent ext = dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification()
			.getTemporalExtent();
		ext.setIndeterminateBeginPosition(TimeIndeterminateValueType.NOW);
	    }

	    coreMetadata.getMIMetadata().getDataIdentification().setCitationTitle(var.getLabel() + " - " + stationName);

	    String latitude = properties.get(CEHQProperty.LAT);
	    String longitude = properties.get(CEHQProperty.LON);
	    if (latitude != null && longitude != null) {
		double lat = Double.parseDouble(latitude);
		double lon = Double.parseDouble(longitude);
		coreMetadata.addBoundingBox(lat, lon, lat, lon);
	    }

	    dataset.getPropertyHandler().setIsTimeseries(true);

	    try {
		String str = dataset.asString(true);
		OriginalMetadata record = new OriginalMetadata();
		record.setMetadata(str);
		record.setSchemeURI(CommonNameSpaceContext.GS_DATA_MODEL_SCHEMA_URI_GS_RESOURCE);
		ret.addRecord(record);
	    } catch (Exception e) {
		e.printStackTrace();
	    }

	}

	Optional<Integer> mr = getSetting().getMaxRecords();
	boolean maxNumberReached = false;
	if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent() && index > mr.get() - 1) {
	    // max record set
	    maxNumberReached = true;
	}
	if (!maxNumberReached && // max records set by user not reached
		index < (identifiers.size() - 1)) { // not last record
	    ret.setResumptionToken(identifiers.get(index + 1));
	}
	return ret;
    }

    private void init() {
	this.client = new CEHQClient();
	client.setEndpoint(getSourceURL());
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected CEHQConnectorSetting initSetting() {

	return new CEHQConnectorSetting();
    }

}
