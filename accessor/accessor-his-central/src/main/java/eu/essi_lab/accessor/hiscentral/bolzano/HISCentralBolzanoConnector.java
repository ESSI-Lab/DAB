package eu.essi_lab.accessor.hiscentral.bolzano;

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
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author Roberto
 */
public class HISCentralBolzanoConnector extends HarvestedQueryConnector<HISCentralBolzanoConnectorSetting> {

    /**
     * 
     */
    static final String TYPE = "HISCentralBolzanoConnector";

    /**
     * 
     */
    public HISCentralBolzanoConnector() {

    }

    /**
     * BASE_URL= http://daten.buergernetz.bz.it/services/meteo/v1/
     * 
     * GET STATIONS: http://daten.buergernetz.bz.it/services/meteo/v1/stations?coord_sys=EPSG:3857
     * GET SENSORS: http://daten.buergernetz.bz.it/services/meteo/v1/sensors?station_code=19850PG
     * GET DATA FROM TO:
     * http://daten.buergernetz.bz.it/services/meteo/v1/timeseries?station_code=19850PG&sensor_code=Q&date_from=20160114&date_to=20160214
     */

    static final String STATIONS_URL = "stations?coord_sys=EPSG:3857";

    /**
     * 
     */
    static final String SENSOR_URL = "sensors?station_code=";

    public static final String BASE_URL = "http://daten.buergernetz.bz.it/services/meteo/v1/";

    private static final String HIS_CENTRAL_BOLZANO_CONNECTOR_DOWNLOAD_ERROR = "HIS_CENTRAL_BOLZANO_CONNECTOR_DOWNLOAD_ERROR";

    private Downloader downloaader = new Downloader();

    /**
     * 
     */

    private int maxRecords;

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	String page = "1";

	if (request.getResumptionToken() != null) {

	    page = request.getResumptionToken();
	}

	// add authorization token
	String baseUrl = getSourceURL();

	JSONObject stationsObject = getStationsList();

	if (stationsObject != null) {

	    JSONArray array = stationsObject.optJSONArray("features");
	    // JSONArray array = new JSONArray(IOStreamUtils.asUTF8String(stream.clone()));
	    if (array == null) {
		GSLoggerFactory.getLogger(getClass()).info("ERROR getting items.");
	    } else {
		maxRecords = array.length();
		getSetting().getMaxRecords().ifPresent(v -> maxRecords = v);
		for (int i = 0; i < maxRecords; i++) {
		    JSONObject properties = null;
		    JSONObject datasetMetadata = array.getJSONObject(i);
		    if (datasetMetadata != null) {
			properties = datasetMetadata.optJSONObject("properties");
			if (properties != null) {
			    String id = properties.optString("SCODE");
			    if (id != null && !id.isEmpty()) {
				JSONArray measuresArray = getSensors(id);
				if (measuresArray != null) {
				    for (int j = 0; j < measuresArray.length(); j++) {
					JSONObject sensorInfo = measuresArray.getJSONObject(j);
					ret.addRecord(HISCentralBolzanoMapper.create(datasetMetadata, sensorInfo));
				    }
				}
			    }
			}

		    }
		}
	    }

	}

	return ret;
    }

    private JSONArray getSensors(String id) throws GSException {
	String url = getSourceURL() + SENSOR_URL + id;
	GSLoggerFactory.getLogger(getClass()).info("Getting " + url);

	try {
	    Optional<String> response = downloaader.downloadOptionalString(url);

	    if (response.isPresent()) {
		JSONArray obj = new JSONArray(response.get());
		return obj;
	    }

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("Unable to retrieve " + url);
	}
	return null;
    }

    private JSONObject getStationsList() throws GSException {
	String url = getSourceURL() + STATIONS_URL;
	GSLoggerFactory.getLogger(getClass()).info("Getting " + url);
	try {
	    Optional<String> response = downloaader.downloadOptionalString(url);

	    if (response.isPresent()) {
		JSONObject obj = new JSONObject(response.get());
		return obj;
	    }

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("Unable to retrieve " + url);

	}
	return null;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.HISCENTRAL_BOLZANO_NS_URI);
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    public boolean supports(GSSource source) {
	String endpoint = source.getEndpoint();
	return endpoint.contains("daten.buergernetz.bz.it");
    }

    @Override
    protected HISCentralBolzanoConnectorSetting initSetting() {

	return new HISCentralBolzanoConnectorSetting();
    }
}
