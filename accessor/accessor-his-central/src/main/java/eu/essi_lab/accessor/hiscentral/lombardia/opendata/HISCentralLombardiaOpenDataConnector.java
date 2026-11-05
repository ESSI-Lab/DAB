package eu.essi_lab.accessor.hiscentral.lombardia.opendata;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * Harvests the ARPA Lombardia hydro-nivo-meteorological network from the Regione Lombardia open data portal: one
 * record per sensor and {@link LombardiaOpenDataProperty}. When a WFS endpoint is configured, the rating curves of
 * the river level sensors are harvested as additional records, and the hydrometric zeros are added to the river
 * level series.
 *
 * @author boldrini
 */
public class HISCentralLombardiaOpenDataConnector extends HarvestedQueryConnector<HISCentralLombardiaOpenDataConnectorSetting> {

    public static final String TYPE = "HISCentralLombardiaOpenDataConnector";

    /**
     * Record types
     */
    static final String TIME_SERIES = "time-series";
    static final String RATING_CURVE = "rating-curve";

    /**
     * Number of days of recent real time data used to find the active series and their time step
     */
    static final int RECENT_DAYS = 2;

    private static final String LOMBARDIA_OPEN_DATA_CONNECTOR_ERROR = "LOMBARDIA_OPEN_DATA_CONNECTOR_ERROR";

    private final Logger logger = GSLoggerFactory.getLogger(getClass());

    private final List<String> recordCache = new ArrayList<>();
    private int partialNumbers;

    @Override
    public boolean supports(GSSource source) {
	String endpoint = source.getEndpoint();
	return endpoint != null && endpoint.contains("dati.lombardia.it");
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> response = new ListRecordsResponse<>();

	if (recordCache.isEmpty()) {
	    fillRecordCache();
	}

	int index = 0;
	if (request.getResumptionToken() != null) {
	    try {
		index = Integer.parseInt(request.getResumptionToken());
	    } catch (NumberFormatException e) {
		logger.warn("Invalid resumption token '{}', restarting from 0", request.getResumptionToken());
	    }
	}

	if (index >= recordCache.size()) {
	    response.setResumptionToken(null);
	    partialNumbers = 0;
	    return response;
	}

	Optional<Integer> maxRecords = getSetting().getMaxRecords();
	int remainingBudget = getSetting().isMaxRecordsUnlimited() || !maxRecords.isPresent() ? Integer.MAX_VALUE
		: Math.max(0, maxRecords.get() - partialNumbers);

	int pageSize = Math.max(1, getSetting().getPageSize());
	int emitted = 0;
	while (index < recordCache.size() && emitted < pageSize && remainingBudget > 0) {
	    OriginalMetadata metadata = new OriginalMetadata();
	    metadata.setSchemeURI(CommonNameSpaceContext.HISCENTRAL_LOMBARDIA_OPEN_DATA_NS_URI);
	    metadata.setMetadata(recordCache.get(index));
	    response.addRecord(metadata);
	    index++;
	    emitted++;
	    partialNumbers++;
	    remainingBudget--;
	}

	if (index < recordCache.size() && remainingBudget > 0) {
	    response.setResumptionToken(String.valueOf(index));
	} else {
	    response.setResumptionToken(null);
	    partialNumbers = 0;
	    recordCache.clear();
	}

	return response;
    }

    private void fillRecordCache() throws GSException {

	LombardiaOpenDataClient client = new LombardiaOpenDataClient(getSourceURL(), getSetting().getAppToken());

	List<JSONObject> sensors;
	try {
	    sensors = client.getSensors();
	} catch (IOException e) {
	    logger.error("Unable to retrieve the Lombardia sensor registry", e);
	    throw GSException.createException(getClass(), "Unable to retrieve the Lombardia sensor registry: " + e.getMessage(), null,
		    ErrorInfo.ERRORTYPE_SERVICE, ErrorInfo.SEVERITY_ERROR, LOMBARDIA_OPEN_DATA_CONNECTOR_ERROR);
	}
	logger.info("Retrieved {} sensors", sensors.size());

	// sensor|operator -> number of real time observations of the latest days
	Map<String, Integer> recentCounts = null;
	try {
	    recentCounts = new HashMap<>();
	    for (JSONObject count : client.getRecentObservationCounts(RECENT_DAYS)) {
		recentCounts.put(count.getString("idsensore") + "|" + count.getString("idoperatore"), count.optInt("count"));
	    }
	} catch (IOException e) {
	    logger.warn("Unable to retrieve the recent observation counts, all series will be harvested: {}", e.getMessage());
	    recentCounts = null;
	}

	Map<String, List<JSONObject>> ratingCurves = new LinkedHashMap<>();
	Map<String, JSONObject> hydrometricZeros = new HashMap<>();
	String wfsEndpoint = getSetting().getWFSEndpoint();
	if (wfsEndpoint != null && !wfsEndpoint.isBlank()) {
	    LombardiaWFSClient wfsClient = new LombardiaWFSClient(wfsEndpoint);
	    try {
		for (JSONObject piece : wfsClient.getRatingCurveFeatures()) {
		    String sensorId = piece.get(LombardiaWFSClient.RATING_CURVES_SENSOR_FIELD).toString();
		    ratingCurves.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(piece);
		}
		logger.info("Retrieved rating curves of {} sensors", ratingCurves.size());
	    } catch (Exception e) {
		logger.error("Unable to retrieve the rating curves from {}: {}", wfsEndpoint, e.getMessage());
	    }
	    try {
		for (JSONObject zero : wfsClient.getHydrometricZeroFeatures()) {
		    String document = zero.optString("download", null);
		    if (document != null) {
			Optional<BigDecimal> value = LombardiaWFSClient.readHydrometricZero(document);
			value.ifPresent(v -> zero.put(HISCentralLombardiaOpenDataMapper.HYDROMETRIC_ZERO, v.toPlainString()));
		    }
		    hydrometricZeros.put(zero.get(LombardiaWFSClient.HYDROMETRIC_ZEROS_SENSOR_FIELD).toString(), zero);
		}
		logger.info("Retrieved hydrometric zeros of {} sensors", hydrometricZeros.size());
	    } catch (Exception e) {
		logger.error("Unable to retrieve the hydrometric zeros from {}: {}", wfsEndpoint, e.getMessage());
	    }
	}

	// rivers are published for the river level sensors, they are extended to the other sensors of their stations
	Map<String, String> sensorStations = new HashMap<>();
	for (JSONObject sensor : sensors) {
	    sensorStations.put(sensor.getString("idsensore"), sensor.optString("idstazione"));
	}
	Map<String, String> stationRivers = new HashMap<>();
	for (Map.Entry<String, List<JSONObject>> entry : ratingCurves.entrySet()) {
	    putRiver(stationRivers, sensorStations.get(entry.getKey()), entry.getValue().get(0).optString("Fiume"));
	}
	for (Map.Entry<String, JSONObject> entry : hydrometricZeros.entrySet()) {
	    putRiver(stationRivers, sensorStations.get(entry.getKey()), entry.getValue().optString("Fiume"));
	}

	Set<String> sensorsWithRecentData = new HashSet<>();
	if (recentCounts != null) {
	    recentCounts.keySet().forEach(k -> sensorsWithRecentData.add(k.substring(0, k.indexOf('|'))));
	}

	for (JSONObject sensor : sensors) {

	    String sensorId = sensor.getString("idsensore");
	    String river = stationRivers.get(sensor.optString("idstazione"));

	    for (LombardiaOpenDataProperty property : LombardiaOpenDataProperty.fromTipologia(sensor.optString("tipologia"))) {

		Integer recentCount = recentCounts == null ? null : recentCounts.get(sensorId + "|" + property.getOperator());

		// series not updated in real time are kept only if the archives can serve them
		if (recentCounts != null && recentCount == null
			&& (sensorsWithRecentData.contains(sensorId) || property.getArchives().isEmpty())) {
		    continue;
		}

		JSONObject record = new JSONObject();
		record.put("type", TIME_SERIES);
		record.put("property", property.name());
		record.put("sensor", sensor);
		if (recentCount != null) {
		    record.put("recentCount", recentCount);
		    record.put("recentDays", RECENT_DAYS);
		}
		if (river != null) {
		    record.put("river", river);
		}
		if (property == LombardiaOpenDataProperty.LIVELLO_IDROMETRICO && hydrometricZeros.containsKey(sensorId)) {
		    record.put("hydrometricZero", hydrometricZeros.get(sensorId));
		}
		recordCache.add(record.toString());
	    }
	}

	Map<String, JSONObject> sensorsById = new HashMap<>();
	sensors.forEach(s -> sensorsById.put(s.getString("idsensore"), s));

	for (Map.Entry<String, List<JSONObject>> entry : ratingCurves.entrySet()) {

	    String sensorId = entry.getKey();
	    JSONObject record = new JSONObject();
	    record.put("type", RATING_CURVE);
	    record.put("sensorId", sensorId);
	    record.put("wfsEndpoint", wfsEndpoint);
	    record.put("ratingCurves", new JSONArray(entry.getValue()));
	    // some rating curves belong to gauges which are not in the open data registry
	    JSONObject sensor = sensorsById.get(sensorId);
	    if (sensor != null) {
		record.put("sensor", sensor);
	    }
	    String river = entry.getValue().get(0).optString("Fiume", null);
	    if (river != null) {
		record.put("river", river);
	    }
	    if (hydrometricZeros.containsKey(sensorId)) {
		record.put("hydrometricZero", hydrometricZeros.get(sensorId));
	    }
	    recordCache.add(record.toString());
	}

	logger.info("Lombardia open data connector cache filled with {} records", recordCache.size());
    }

    private static void putRiver(Map<String, String> stationRivers, String stationId, String river) {
	if (stationId != null && river != null && !river.isBlank() && !river.equals("-")) {
	    stationRivers.putIfAbsent(stationId, river.trim());
	}
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.HISCENTRAL_LOMBARDIA_OPEN_DATA_NS_URI);
	return ret;
    }

    @Override
    public String getType() {
	return TYPE;
    }

    @Override
    protected HISCentralLombardiaOpenDataConnectorSetting initSetting() {
	return new HISCentralLombardiaOpenDataConnectorSetting();
    }
}
