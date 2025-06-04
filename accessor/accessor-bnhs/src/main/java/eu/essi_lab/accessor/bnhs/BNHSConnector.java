package eu.essi_lab.accessor.bnhs;

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

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.accessor.canada.CANADAMSCConnector;
import eu.essi_lab.accessor.canada.CANADAMSCMapper;
import eu.essi_lab.accessor.cehq.CEHQConnector;
import eu.essi_lab.accessor.imo.IMOConnector;
import eu.essi_lab.accessor.imo.IMOMapper;
import eu.essi_lab.accessor.nve.NVEConnector;
import eu.essi_lab.accessor.nve.NVEMapper;
import eu.essi_lab.accessor.odatahidro.ODataHidrologyConnector;
import eu.essi_lab.accessor.odatahidro.ODataHidrologyMapper;
import eu.essi_lab.accessor.rihmi.RIHMIConnector;
import eu.essi_lab.accessor.rihmi.RIHMIMapper;
import eu.essi_lab.accessor.usgswatersrv.USGSConnector;
import eu.essi_lab.accessor.usgswatersrv.USGSMapper;
import eu.essi_lab.adk.timeseries.StationConnector;
import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.MIInstrument;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.VerticalCRS;
import eu.essi_lab.iso.datamodel.classes.VerticalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.BNHSProperty;
import eu.essi_lab.model.resource.BNHSPropertyReader;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.InterpolationType;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.AbstractResourceMapper;
import eu.essi_lab.ommdk.GSResourceMapper;

public class BNHSConnector extends HarvestedQueryConnector<BNHSConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "BNHSConnector";

    private BNHSClient client = null;

    @Override
    public boolean supports(GSSource source) {
	String endpoint = source.getEndpoint();
	try {
	    if (endpoint.contains("google")) {
		BNHSClient client = new BNHSClient(new URL(endpoint));
		Integer size = client.getRowSize();
		return size > 10;
	    }
	} catch (Exception e) {
	}
	return false;

    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	initClient();

	String[] keys = client.getKeys().toArray(new String[] {});
	Arrays.sort(keys, new Comparator<String>() {

	    @Override
	    public int compare(String k1, String k2) {
		Integer l1 = k1 == null ? 0 : k1.length();
		Integer l2 = k2 == null ? 0 : k2.length();
		if (l1.equals(l2)) {
		    return k1.compareTo(k2);
		}
		return l1.compareTo(l2);
	    }
	});

	String token = request.getResumptionToken();
	if (token == null) {
	    token = keys[0];
	}

	int row = 0;
	for (int i = 0; i < keys.length; i++) {
	    if (token.equals(keys[i])) {
		row = i;
		break;
	    }
	}

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	boolean stopRequested = getSetting().isMaxRecordsUnlimited() ? false : (row + 1) >= getSetting().getMaxRecords().get();

	if (row < keys.length) {

	    String key = keys[row];

	    String latitudeString = client.getValue(key, BNHSProperty.LATITUDE);
	    double lat = Double.parseDouble(latitudeString);
	    String longitudeString = client.getValue(key, BNHSProperty.LONGITUDE);
	    double lon = Double.parseDouble(longitudeString);
	    String stationId = client.getValue(key, BNHSProperty.STATION_ID);
	    String realtimeLink = client.getValue(key, BNHSProperty.SCRIPT_LINK_FOR_REAL_TIME_DATA);
	    String realtimeNotes = client.getValue(key, BNHSProperty.NOTES_FOR_REAL_TIME_DATA);
	    String institute = client.getValue(key, BNHSProperty.INSTITUTE);

	    StationConnector connector = null;
	    AbstractResourceMapper mapper = null;
	    GSSource source = new GSSource();

	    List<String> headers = client.getRow(0);
	    List<String> values = client.getRowByKey(key);
	    String bnhsInfo = "";
	    for (int i = 0; i < headers.size(); i++) {
		String header = headers.get(i);
		String value = i < values.size() ? values.get(i) : "";
		bnhsInfo += header + BNHSPropertyReader.SEPARATOR + value + BNHSPropertyReader.SEPARATOR;
	    }

	    ListRecordsResponse<OriginalMetadata> timeSeries = new ListRecordsResponse<OriginalMetadata>();

	    if (realtimeLink != null) {

		// CANADA -> USGS
		if (realtimeLink.isEmpty() && realtimeNotes.contains("Realtime available through USGS")) { //
		    realtimeLink = "https://waterservices.usgs.gov/nwis/site/";
		    connector = new USGSConnector();
		    mapper = new USGSMapper();
		    switch (stationId) {
		    case "05PA012": // Environment Canada code
			stationId = "05127500"; // USGS code
			break;
		    case "05PC018":
			stationId = "05133500"; // USGS code
			break;
		    default:
			break;
		    }

		    // CANADA
		} else if (realtimeLink.contains("http://dd.weather.gc.ca/hydrometric/") || //
			realtimeLink.contains("https://wateroffice.ec.gc.ca/report/real_time_e.html") || //
			institute.equals("Water Survey of Canada")) {
		    realtimeLink = "http://dd.weather.gc.ca/hydrometric/csv/";
		    connector = new CANADAMSCConnector();
		    mapper = new CANADAMSCMapper();

		    // RUSSIA
		} else if (realtimeLink.contains("http://ws.meteo.ru/hydro/rest/GetHydroDischargesRF/xml")) {
		    connector = new RIHMIConnector();
		    mapper = new RIHMIMapper();

		    // FINLAND
		} else if (realtimeLink.contains("http://wwwi3.ymparisto.fi/")) {
		    realtimeLink = "http://rajapinnat.ymparisto.fi/api/Hydrologiarajapinta/1.0";
		    connector = new ODataHidrologyConnector();
		    mapper = new ODataHidrologyMapper();

		    // USA
		} else if (realtimeLink.contains("waterdata.usgs.gov") || realtimeLink.contains("waterservices.usgs.gov")) {
		    if (stationId.length() == 7) {
			stationId = "0" + stationId;
		    }
		    realtimeLink = "https://waterservices.usgs.gov/nwis/site/";
		    connector = new USGSConnector();
		    mapper = new USGSMapper();

		    // NORWAY
		} else if (realtimeLink.contains("hydapi.nve.no")) {
		    realtimeLink = "https://hydapi.nve.no/api/v1";
		    connector = new NVEConnector();
		    mapper = new NVEMapper();

		    // ICELAND
		} else if (realtimeLink.contains("customer.vedur.is")) {
		    realtimeLink = "https://customer.vedur.is/HYCOS/";
		    connector = new IMOConnector();
		    mapper = new IMOMapper();

		    // QUEBEC
		} else if (realtimeLink.contains("http://www.cehq.gouv.qc.ca/suivihydro/graphique.asp?NoStation=")) {
		    stationId = realtimeLink.substring(realtimeLink.lastIndexOf("=") + 1);
		    realtimeLink = "https://www.cehq.gouv.qc.ca/hydrometrie/historique_donnees/default.asp";
		    connector = new CEHQConnector();
		    mapper = new GSResourceMapper();
		}

		if (connector != null) {
		    connector.setSourceURL(realtimeLink);
		    source.setEndpoint(realtimeLink);
		    try {
			timeSeries = connector.listTimeseries(stationId);
			if (timeSeries.getRecordsAsList().isEmpty()) {
			    connector = null;
			}
		    } catch (Exception e) {
			e.printStackTrace();
			connector = null;
		    }

		}
	    }
	    if (connector == null) {
		// Unrecognized type station
		source.setEndpoint(getSourceURL());
		timeSeries = new ListRecordsResponse<>();
		OriginalMetadata originalMD = new OriginalMetadata();
		originalMD.setSchemeURI("BNHS");
		originalMD.setMetadata(bnhsInfo);
		timeSeries.addRecord(originalMD);
		mapper = new BNHSMapper();
	    }

	    for (OriginalMetadata time : timeSeries.getRecordsAsList()) {
		GSResource mapped = mapper.map(time, source);
		mapped.getExtensionHandler().setBNHSInfo(bnhsInfo);
		Double latitude = null;
		Double longitude = null;

		String status = null;

		for (BNHSProperty property : BNHSProperty.values()) {
		    Optional<String> value = BNHSPropertyReader.readProperty(mapped, property);
		    if (value.isPresent()) {
			if (property.equals(BNHSProperty.STATUS)) {
			    status = value.get();
			}
			MetadataElement target = property.getElement();
			if (target != null) {
			    mapped.getExtensionHandler().setBNHSProperty(property, value.get());
			    try {
				VerticalExtent verticalExtent = null;
				switch (property) {
				case COUNTRY:
				    mapped.getExtensionHandler().setCountry(value.get());
				    break;
				case LATITUDE:
				    latitude = Double.parseDouble(value.get());
				    break;
				case LONGITUDE:
				    longitude = Double.parseDouble(value.get());
				    break;
				case EQUIPMENT:
				    MIInstrument instrument = new MIInstrument();
				    instrument.setTitle(value.get());
				    mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().addMIInstrument(instrument);
				    break;
				case DATUM_ALTITUDE:
				    verticalExtent = mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
					    .getDataIdentification().getVerticalExtent();
				    if (verticalExtent == null) {
					verticalExtent = new VerticalExtent();
					mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification()
						.addVerticalExtent(verticalExtent);
				    }
				    verticalExtent.setMinimumValue(Double.parseDouble(value.get()));
				    verticalExtent.setMaximumValue(Double.parseDouble(value.get()));
				    break;
				case DATUM_NAME:
				    verticalExtent = mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
					    .getDataIdentification().getVerticalExtent();
				    if (verticalExtent == null) {
					verticalExtent = new VerticalExtent();
					mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification()
						.addVerticalExtent(verticalExtent);
				    }
				    VerticalCRS verticalCRS = new VerticalCRS();
				    verticalCRS.setId(value.get());
				    verticalExtent.setVerticalCRS(verticalCRS);
				    break;
				case INSTITUTE:
				    mapped.getExtensionHandler().addOriginatorOrganisationDescription(value.get());
				    ResponsibleParty contact = mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
					    .getDataIdentification().getPointOfContact();
				    if (contact == null) {
					contact = new ResponsibleParty();
					mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification()
						.addPointOfContact(contact);
				    }
				    contact.setOrganisationName(value.get());
				    break;
				case STATION_NAME:
				    MIPlatform platform = mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getMIPlatform();
				    if (platform == null) {
					platform = new MIPlatform();
					mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().addMIPlatform(platform);
				    }
				    platform.getCitation().setTitle(value.get());
				    break;
				default:
				    break;
				}
			    } catch (Exception e) {
				e.printStackTrace();
			    }
			}
		    }
		}
		if (latitude != null && longitude != null) {
		    GeographicBoundingBox bbox = mapped.getHarmonizedMetadata().getCoreMetadata().getDataIdentification()
			    .getGeographicBoundingBox();
		    if (bbox == null) {
			mapped.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().addGeographicBoundingBox(latitude,
				longitude, latitude, longitude);
		    }
		}

		if (status != null && connector != null && connector instanceof USGSConnector) {
		    Optional<InterpolationType> interpolation = mapped.getExtensionHandler().getTimeInterpolation();
		    if (!interpolation.isPresent()) {
			continue;
		    }
		    switch (status) {
		    case "A": // active
			if (!interpolation.get().equals(InterpolationType.CONTINUOUS)) { // as per discussion with Jeff
											 // and Igor, 3 March 2021
			    continue;
			}
			break;
		    case "N": // no longer active
			if (interpolation.get().equals(InterpolationType.AVERAGE)) { // as per discussion with Jeff and
										     // Igor, 3 March 2021
			    // accepted
			} else {
			    // the series is skipped
			    continue;
			}
			break;
		    default:
			GSLoggerFactory.getLogger(getClass()).error("Unrecognized status: {}", status);
			break;
		    }
		}

		String str;
		try {
		    str = mapped.asString(true);
		    OriginalMetadata record = new OriginalMetadata();
		    record.setMetadata(str);
		    record.setSchemeURI(CommonNameSpaceContext.GS_DATA_MODEL_SCHEMA_URI_GS_RESOURCE);
		    ret.addRecord(record);
		} catch (Exception e) {
		    e.printStackTrace();
		}

	    }
	    if ((row + 1) < keys.length) {
		token = keys[row + 1];
	    } else {
		token = null;
	    }
	} else

	{
	    token = null;
	}

	if (stopRequested) {
	    token = null;
	}

	ret.setResumptionToken(token);

	return ret;

    }

    private void initClient() {
	try {
	    if (client == null) {
		client = new BNHSClient(new URL(getSourceURL()));
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.BNHS_URI);
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected BNHSConnectorSetting initSetting() {

	return new BNHSConnectorSetting();
    }

}
