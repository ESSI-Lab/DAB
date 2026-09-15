/**
 *
 */
package eu.essi_lab.accessor.trigger;

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

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpHeaderUtils;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.messages.HarvestingProperties;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Harvests the daily-aggregated TRIGGER tables (<code>myair_daily</code>, <code>smartwatchhigh_daily</code>,
 * <code>smartwatchlow_daily</code>), positioning each user/variable dataset with their <code>gps_daily</code>
 * track.
 *
 * @author Roberto
 */
public class AggregatedTRIGGERConnector extends HarvestedQueryConnector<AggregatedTRIGGERConnectorSetting> {

    /**
     *
     */
    public static final String TYPE = "AggregatedTRIGGERConnector";//

    /**
     *
     */
    private int recordsCount = 0;

    private int partialNumbers = 0;

    public static final String BASE_URL = "https://trigger-io.difa.unibo.it/api/";
    private static final String MYAIR_URL = "myair_daily?";
    private static final String GPS_URL = "gps_daily?";
    private static final String SMARTWATCHHIGH_URL = "smartwatchhigh_daily?";
    private static final String SMARTWATCHLOW_URL = "smartwatchlow_daily?";

    /**
     * The daily aggregated tables have, per user, one row per day: a single request with a generous limit is
     * enough to retrieve the whole history for every user in one shot.
     */
    private static final String LARGE_LIMIT_PARAM = "limit=100000";

    private static final String USER_ID = "userId";

    Map<String, List<TRIGGERTimePosition>> latLonMap = new HashMap<>();

    public static String TRIGGER_TOKEN = null;

    private String[] SEARCH_TERMS = { MYAIR_URL, SMARTWATCHHIGH_URL, SMARTWATCHLOW_URL };
    private static int searchIndex = 0;

    public enum AGGREGATED_TRIGGER_VARIABLES {

	// MYAIR
	TEMPERATUREMEAN("Average 2m temperature", "temperature_mean", "myair", "°C"), //
	TEMPERATUREMIN("Minimum 2m temperature", "temperature_min", "myair", "°C"), //
	TEMPERATUREMAX("Maximum 2m temperature", "temperature_max", "myair", "°C"), //

	HUMIDITYMEAN("Average 2m rel. humidity", "humidity_mean", "myair", "%"), //
	HUMIDITYMIN("Minimum 2m rel. humidity", "humidity_min", "myair", "%"), //
	HUMIDITYMAX("Maximum 2m rel. humidity", "humidity_max", "myair", "%"), //

	PRESSUREMEAN("Average Pressure", "pressure_mean", "myair", "mbar"), //
	PRESSUREMIN("Minimum Pressure", "pressure_min", "myair", "mbar"), //
	PRESSUREMAX("Maximum Pressure", "pressure_max", "myair", "mbar"), //

	SOUNDMEAN("Average Sound", "sound_mean", "myair", "dB"), //
	SOUNDMIN("Minimum Sound", "sound_min", "myair", "dB"), //
	SOUNDMAX("Maximum Sound", "sound_max", "myair", "dB"), //

	LIGHTMEAN("Average Solar Radiation Index", "light_mean", "myair", "#"), //
	LIGHTMIN("Minimum Solar Radiation Index", "light_min", "myair", "#"), //
	LIGHTMAX("Maximum Solar Radiation Index", "light_max", "myair", "#"), //

	UVBMEAN("Average Ultraviolet B", "uvb_mean", "myair", "#"), //
	UVBMIN("Minimum Ultraviolet B", "uvb_min", "myair", "#"), //
	UVBMAX("Maximum Ultraviolet B", "uvb_max", "myair", "#"), //

	PM1MEAN("Average Mass Concentration PM1.0", "pm1_mean", "myair", "μg/m³"), //
	PM1MIN("Minimum Mass Concentration PM1.0", "pm1_min", "myair", "μg/m³"), //
	PM1MAX("Maximum Mass Concentration PM1.0", "pm1_max", "myair", "μg/m³"), //

	PM25MEAN("Average Mass Concentration PM2.5", "pm25_mean", "myair", "μg/m³"), //
	PM25MIN("Minimum Mass Concentration PM2.5", "pm25_min", "myair", "μg/m³"), //
	PM25MAX("Maximum Mass Concentration PM2.5", "pm25_max", "myair", "μg/m³"), //

	PM10MEAN("Average Mass Concentration PM10", "pm10_mean", "myair", "μg/m³"), //
	PM10MIN("Minimum Mass Concentration PM10", "pm10_min", "myair", "μg/m³"), //
	PM10MAX("Maximum Mass Concentration PM10", "pm10_max", "myair", "μg/m³"), //

	PC03MEAN("Average Number of particulate concentration PC0.3", "pc03_mean", "myair", "#/cm³"), //
	PC03MIN("Minimum Number of particulate concentration PC0.3", "pc03_min", "myair", "#/cm³"), //
	PC03MAX("Maximum Number of particulate concentration PC0.3", "pc03_max", "myair", "#/cm³"), //

	PC05MEAN("Average Number of particulate concentration PM0.5", "pc05_mean", "myair", "#/cm³"), //
	PC05MIN("Minimum Number of particulate concentration PM0.5", "pc05_min", "myair", "#/cm³"), //
	PC05MAX("Maximum Number of particulate concentration PM0.5", "pc05_max", "myair", "#/cm³"), //

	PC1MEAN("Average Number of particulate concentration PC1.0", "pc1_mean", "myair", "#/cm³"), //
	PC1MIN("Minimum Number of particulate concentration PC1.0", "pc1_min", "myair", "#/cm³"), //
	PC1MAX("Maximum Number of particulate concentration PC1.0", "pc1_max", "myair", "#/cm³"), //

	PC25MEAN("Average Number of particulate concentration PC2.5", "pc25_mean", "myair", "#/cm³"), //
	PC25MIN("Minimum Number of particulate concentration PC2.5", "pc25_min", "myair", "#/cm³"), //
	PC25MAX("Maximum Number of particulate concentration PC2.5", "pc25_max", "myair", "#/cm³"), //

	PC5MEAN("Average Number of particulate concentration PC5.0", "pc5_mean", "myair", "#/cm³"), //
	PC5MIN("Minimum Number of particulate concentration PC5.0", "pc5_min", "myair", "#/cm³"), //
	PC5MAX("Maximum Number of particulate concentration PC5.0", "pc5_max", "myair", "#/cm³"), //

	PC10MEAN("Average Number of particulate concentration PC10", "pc10_mean", "myair", "#/cm³"), //
	PC10MIN("Minimum Number of particulate concentration PC10", "pc10_min", "myair", "#/cm³"), //
	PC10MAX("Maximum Number of particulate concentration PC10", "pc10_max", "myair", "#/cm³"), //

	// SMARTWATCHLOW
	BPHIGHMEAN("Average Systolic Blood Pressure", "bphigh_mean", "smartwatchlow", "mmHg"), //
	BPHIGHMIN("Minimum Systolic Blood Pressure", "bphigh_min", "smartwatchlow", "mmHg"), //
	BPHIGHMAX("Maximum Systolic Blood Pressure", "bphigh_max", "smartwatchlow", "mmHg"), //

	BPLOWMEAN("Average Diastolic Blood Pressure", "bplow_mean", "smartwatchlow", "mmHg"), //
	BPLOWMIN("Minimum Diastolic Blood Pressure", "bplow_min", "smartwatchlow", "mmHg"), //
	BPLOWMAX("Maximum Diastolic Blood Pressure", "bplow_max", "smartwatchlow", "mmHg"), //

	BODYTEMPMEAN("Average Body Temperature", "bodytemp_mean", "smartwatchlow", "°C"), //
	BODYTEMPMIN("Minimum Body Temperature", "bodytemp_min", "smartwatchlow", "°C"), //
	BODYTEMPMAX("Maximum Body Temperature", "bodytemp_max", "smartwatchlow", "°C"), //

	SKINTEMPMEAN("Average Skin Temperature", "skintemp_mean", "smartwatchlow", "°C"), //
	SKINTEMPMIN("Minimum Skin Temperature", "skintemp_min", "smartwatchlow", "°C"), //
	SKINTEMPMAX("Maximum Skin Temperature", "skintemp_max", "smartwatchlow", "°C"), //

	// SMARTWATCHHIGH
	HEARTRATEMEAN("Average Heart Rate", "heartrate_mean", "smartwatchhigh", "BPM"), //
	HEARTRATEMIN("Minimum Heart Rate", "heartrate_min", "smartwatchhigh", "BPM"), //
	HEARTRATEMAX("Maximum Heart Rate", "heartrate_max", "smartwatchhigh", "BPM"), //

	// sleeprate has no _mean/_min/_max in the aggregated API: it's exposed as per-stage counts
	// (sleeprate_1_n..sleeprate_4_n), which don't fit this scalar-variable model. Left out for now.

	OXYGENSMEAN("Average Oxygen", "oxygens_mean", "smartwatchhigh", "%"), //
	OXYGENSMIN("Minimum Oxygen", "oxygens_min", "smartwatchhigh", "%"), //
	OXYGENSMAX("Maximum Oxygen", "oxygens_max", "smartwatchhigh", "%"), //

	BREATHRATEMEAN("Average Breaths Rate", "breathrate_mean", "smartwatchhigh", "BPM"), //
	BREATHRATEMIN("Minimum Breaths Rate", "breathrate_min", "smartwatchhigh", "BPM"), //
	BREATHRATEMAX("Maximum Breaths Rate", "breathrate_max", "smartwatchhigh", "BPM");

	private String label;
	private String jsonField;
	private String table;
	private String units;

	public String getLabel() {
	    return label;
	}

	/**
	 * @return the literal field name of this variable in the TRIGGER API JSON response, e.g. {@code pm1_mean}
	 */
	public String getJsonField() {
	    return jsonField;
	}

	/**
	 * @return the aggregated table this variable belongs to ({@code myair}/{@code smartwatchlow}/{@code smartwatchhigh})
	 */
	public String getTable() {
	    return table;
	}

	public String getUnits() {
	    return units;
	}

	private AGGREGATED_TRIGGER_VARIABLES(String label, String jsonField, String table, String units) {
	    this.label = label;
	    this.jsonField = jsonField;
	    this.table = table;
	    this.units = units;
	}

	public static AGGREGATED_TRIGGER_VARIABLES decode(String parameterCode) {
	    for (AGGREGATED_TRIGGER_VARIABLES var : values()) {
		if (parameterCode.equals(var.name())) {
		    return var;
		}
	    }
	    return null;

	}
    }

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint().contains("trigger-io.difa.unibo.it");
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> response = new ListRecordsResponse<>();

	if (latLonMap.isEmpty()) {

	    String resp = getResponse(BASE_URL + GPS_URL + LARGE_LIMIT_PARAM);
	    getGPSValues(resp);
	}

	String token = request.getResumptionToken();
	int start = 0;
	if (token != null) {
	    start = Integer.valueOf(token);
	}

	Optional<Integer> mr = getSetting().getMaxRecords();
	boolean maxNumberReached = false;
	if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent() && recordsCount > mr.get() - 1) {
	    // max record set
	    maxNumberReached = true;
	}

	if (!maxNumberReached) {

	    HarvestingProperties properties = request.getHarvestingProperties();

	    try {

		String queryPath = SEARCH_TERMS[searchIndex];

		String url = BASE_URL + queryPath + LARGE_LIMIT_PARAM;

		GSLoggerFactory.getLogger(getClass()).info("Getting " + url);

		String queryRes = getResponse(url);

		if (queryRes != null) {

		    JSONArray dataArray = new JSONArray(queryRes);

		    Set<String> processedUsers = new HashSet<>();
		    for (int i = 0; i < dataArray.length(); i++) {
			JSONObject obj = dataArray.getJSONObject(i);

			String userId = obj.optString(USER_ID, null);

			if (userId == null) {
			    continue;
			}

			if (!latLonMap.containsKey(userId)) {
			    GSLoggerFactory.getLogger(getClass()).warn("No coordinates found for userID: {} ", userId);
			    continue;
			}

			if (!processedUsers.add(userId)) {
			    continue;
			}

			for (AGGREGATED_TRIGGER_VARIABLES var : AGGREGATED_TRIGGER_VARIABLES.values()) {

			    if (!queryPath.equals(var.getTable() + "_daily?")) {
				continue;
			    }

			    String field = var.getJsonField();

			    if (!obj.has(field) || obj.isNull(field)) {
				continue;
			    }

			    response.addRecord(AggregatedTRIGGERMapper.create(obj, var.name(), queryPath, latLonMap.get(userId)));
			    partialNumbers++;
			    recordsCount++;

			}

		    }

		}

		GSLoggerFactory.getLogger(getClass()).debug("Current records count: {}", recordsCount);

		// try again, probably the token is expired
		if (TRIGGER_TOKEN == null) {
		    response.setResumptionToken(String.valueOf(Integer.valueOf(start)));
		    return response;
		}

		// use the next search term
		searchIndex++;
		if (searchIndex < SEARCH_TERMS.length) {
		    response.setResumptionToken(String.valueOf(searchIndex));
		} else {
		    response.setResumptionToken(null);
		    TRIGGER_TOKEN = null;
		    latLonMap = new HashMap<>();
		    searchIndex = 0;
		}

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
		TRIGGER_TOKEN = null;
		getBearerToken();
	    }

	} else {
	    response.setResumptionToken(null);
	    TRIGGER_TOKEN = null;
	    latLonMap = new HashMap<>();
	    GSLoggerFactory.getLogger(AggregatedTRIGGERConnector.class)
		    .debug("Added Collection records: {} . TOTAL STATION SIZE: {}", partialNumbers, recordsCount);
	    return response;
	}

	return response;
    }

    private void getGPSValues(String response) {

	try {

	    if (response != null) {

		JSONArray dataArray = new JSONArray(response);

		for (int i = 0; i < dataArray.length(); i++) {
		    JSONObject obj = dataArray.getJSONObject(i);

		    String userId = obj.optString(USER_ID, null);
		    if (userId == null) {
			continue;
		    }

		    LocalDateTime dateTime = LocalDateTime.parse(obj.getString("date") + "T00:00:00");

		    double lat = obj.optDouble("latitude_mean", Double.NaN);
		    double lon = obj.optDouble("longitude_mean", Double.NaN);

		    if (Double.isNaN(lat) || Double.isNaN(lon))
			continue;

		    TRIGGERTimePosition position = new TRIGGERTimePosition(lon, lat, dateTime);

		    latLonMap.computeIfAbsent(userId, k -> new ArrayList<>()).add(position);
		}

		// order by date
		for (List<TRIGGERTimePosition> list : latLonMap.values()) {
		    list.sort(Comparator.comparing(TRIGGERTimePosition::getDateTime));
		}
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    public static String getBearerToken() {

	GSLoggerFactory.getLogger(AggregatedTRIGGERConnector.class).info("Getting TOKEN from TRIGGER service");

	TRIGGERClient client = new TRIGGERClient(BASE_URL);
	client.setUser(ConfigurationWrapper.getCredentialsSetting().getTriggerUser().orElse(null));
	client.setPassword(ConfigurationWrapper.getCredentialsSetting().getTriggerPassword().orElse(null));
	String token = null;

	try {
	    token = client.getToken();
	} catch (GSException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(AggregatedTRIGGERConnector.class).info("ERROR getting BEARER TOKEN: " + e.getMessage());
	    return null;
	}
	return token;
    }

    public static String getResponse(String url) {

	GSLoggerFactory.getLogger(AggregatedTRIGGERConnector.class).info("Getting Data from TRIGGER service");

	String response = null;

	if (TRIGGER_TOKEN == null) {
	    TRIGGER_TOKEN = getBearerToken();
	}

	try {

	    HttpResponse<InputStream> triggerResponse = new Downloader().downloadResponse(//
		    url.trim(), //
		    HttpHeaderUtils.build("token", TRIGGER_TOKEN));

	    int statusCode = triggerResponse.statusCode();
	    if (statusCode > 400) {
		// token expired - refresh token
		TRIGGER_TOKEN = getBearerToken();

		triggerResponse = new Downloader().downloadResponse(//
			url.trim(), //
			HttpHeaderUtils.build("token", TRIGGER_TOKEN));

	    }

	    InputStream stream = triggerResponse.body();
	    GSLoggerFactory.getLogger(AggregatedTRIGGERConnector.class).info("Got " + url);

	    if (stream != null) {

		ClonableInputStream clone = new ClonableInputStream(stream);
		response = IOStreamUtils.asUTF8String(clone.clone());
		stream.close();
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}

	return response;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.AGGREGATED_TRIGGER);
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected AggregatedTRIGGERConnectorSetting initSetting() {

	return new AggregatedTRIGGERConnectorSetting();
    }

    @Override
    public boolean supportsIncrementalHarvesting() throws GSException {

	return false;
    }
}
