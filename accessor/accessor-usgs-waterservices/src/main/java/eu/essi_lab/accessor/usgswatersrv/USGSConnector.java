package eu.essi_lab.accessor.usgswatersrv;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import eu.essi_lab.adk.timeseries.StationConnector;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author boldrini
 * @author Fabrizio
 */
public class USGSConnector extends StationConnector<USGSConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "USGSConnector";

    public static final String USGS_SITE_METADATA = "USGS_SITE_METADATA";
    public static boolean LIMITED_HARVESTING = false;
    public static boolean LIMITED_HARVESTING_DISCHARGE_TEMPERATURE_LEVEL = true;
    public static boolean LIMITED_HARVESTING_IV_UV_RT_DV = true;
    public static String[] LIMITED_HARVESTING_STATISTICS_CODE = new String[] { "00003", "00011" };
    private String siteURL;

    private USGSCounties counties = null;

    @Override
    public boolean supports(GSSource source) {
	String endpoint = source.getEndpoint();
	if (endpoint.contains("usgs.gov")) {
	    return true;
	} else {
	    return false;
	}
    }

    @Override
    public String getSourceURL() {
	return siteURL;
    }

    @Override
    public void setSourceURL(String sourceURL) {
	if (!sourceURL.endsWith("?")) {
	    sourceURL = sourceURL + "?";
	}
	this.siteURL = sourceURL;

    }

    /**
     * Lists records with a given station id
     */
    @Override
    public ListRecordsResponse<OriginalMetadata> listTimeseries(String stationId) throws GSException {
	USGSClient client = getBasicClient();

	client.setStationCode(stationId);

	ListRecordsResponse<OriginalMetadata> ret = listRecords(client);

	return ret;
    }

    /**
     * Lists all records, county by county
     */
    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {
	USGSClient client = getBasicClient();

	String token = request.getResumptionToken();
	if (token == null) {

	    token = counties.getNextKey(null);
	}

	USGSCounty county = counties.getCounty(token);

	GSLoggerFactory.getLogger(getClass()).info("Getting series (state/county code:" + token + ") from state: " + county.getStateCode()
		+ " county: " + county.getCountyName());

	client.setCountyCode(county.getStateCode() + county.getCountyCode());

	ListRecordsResponse<OriginalMetadata> ret = listRecords(client);

	String nextToken = counties.getNextKey(token);
	ret.setResumptionToken(nextToken);

	return ret;
    }

    private USGSClient getBasicClient() throws GSException {
	USGSClient client = new USGSClient(siteURL);
	if (LIMITED_HARVESTING_DISCHARGE_TEMPERATURE_LEVEL) {
	    client.setRequiredParameter("00010,00020,00060,00061,00065"); // Temperature (water), Temperature (air),
									  // Discharge (mean), Discharge
									  // (instantaneous), Gage height
	}
	if (LIMITED_HARVESTING_IV_UV_RT_DV) {
	    client.setRequiredDataType("iv,dv");
	}
	if (counties == null) {
	    USGSCounties myCounties = client.getCounties();
	    if (LIMITED_HARVESTING) {
		myCounties.reduceCounties();
	    }
	    counties = myCounties;
	}

	return client;
    }

    public ListRecordsResponse<OriginalMetadata> listRecords(USGSClient client) throws GSException {

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	try {

	    // gets information about temporal range of available series
	    List<String> seriesMetadatas = client.getSeriesInfo();
	    // gets supplemental information about available sites, such as time zone
	    List<String> siteMetadatas = client.getSitesInfoExpanded();

	    HashMap<String, String> siteToMetadata = new HashMap<>();
	    for (String expandedMetadata : siteMetadatas) {
		USGSMetadata metadata = new USGSMetadata(expandedMetadata);
		String siteNumber = metadata.getSiteNumber();
		siteToMetadata.put(siteNumber, expandedMetadata);
	    }

	    int size = seriesMetadatas.size();

	    GSLoggerFactory.getLogger(this.getClass()).info("Returning " + size + " series (" + siteMetadatas.size() + " sites)");

	    for (int i = 0; i < size; i++) {
		String metadata = seriesMetadatas.get(i);
		OriginalMetadata metadataRecord = new OriginalMetadata();
		metadataRecord.setSchemeURI(USGS_SITE_METADATA);
		USGSMetadata seriesMetadata = new USGSMetadata(metadata);
		String parameterCode = seriesMetadata.getParameterCode();
		String timeSeriesId = seriesMetadata.getTimeSeriesId();
		String dataTypeCode = seriesMetadata.get("data_type_cd");
		String statisticsCode = seriesMetadata.get("stat_cd");

		if (LIMITED_HARVESTING_IV_UV_RT_DV) {
		    if (dataTypeCode == null) {
			GSLoggerFactory.getLogger(getClass()).warn("Data type not present");
			continue;
		    }
		    if (!dataTypeCode.equals("iv") && !dataTypeCode.equals("rt") && !dataTypeCode.equals("uv")
			    && !dataTypeCode.equals("dv")) {
			GSLoggerFactory.getLogger(getClass()).warn("Not iv, rt, uv or dv data types: {} {} {}", dataTypeCode, parameterCode,
				statisticsCode);
			continue;
		    }
		}
		if (dataTypeCode.equals("iv") || dataTypeCode.equals("uv") || dataTypeCode.equals("rt")) {
		    if (statisticsCode != null && statisticsCode.isEmpty()) {
			statisticsCode = "00011"; // for these data types we can infer that it is instantaneous
		    }
		}
		if (LIMITED_HARVESTING_STATISTICS_CODE != null && LIMITED_HARVESTING_STATISTICS_CODE.length > 0) {
		    if (statisticsCode == null) {
			GSLoggerFactory.getLogger(getClass()).warn("Statistics code not present: {} {} {}", dataTypeCode, parameterCode,
				statisticsCode);
			continue;
		    }
		    boolean found = false;
		    for (String allowedStatisticsCode : LIMITED_HARVESTING_STATISTICS_CODE) {
			if (statisticsCode.equals(allowedStatisticsCode)) {
			    found = true;
			    break;
			}
		    }
		    if (!found) {
			GSLoggerFactory.getLogger(getClass()).warn("Statistics code not allowed: {} {} {}", dataTypeCode, parameterCode,
				statisticsCode);
			continue;
		    }
		}

		if (LIMITED_HARVESTING_DISCHARGE_TEMPERATURE_LEVEL) {
		    if (parameterCode == null) {
			GSLoggerFactory.getLogger(getClass()).warn("Parameter code not present");
			continue;
		    }
		    if (!parameterCode.equals("00010")//
			    && !parameterCode.equals("00020")//
			    && !parameterCode.equals("00060")//
			    && !parameterCode.equals("00061")//
			    && !parameterCode.equals("00065")//
		    ) {
			GSLoggerFactory.getLogger(getClass()).warn("Not discharge/temperature/level: {} {} {}", dataTypeCode, parameterCode,
				statisticsCode);
			continue;
		    }
		}

		GSLoggerFactory.getLogger(getClass()).info("Approved for harvesting: {} {} {}", dataTypeCode, parameterCode,
			statisticsCode);

		if (timeSeriesId == null || timeSeriesId.trim().equals("") || timeSeriesId.trim().equals("0")) {

		    // GSLoggerFactory.getLogger(USGSMapper.class).trace("Not a time series... probably manual
		    // measurement");

		    continue;

		}

		String siteNumber = seriesMetadata.getSiteNumber();
		String siteMetadata = siteToMetadata.get(siteNumber);
		if (siteMetadata == null) {
		    GSLoggerFactory.getLogger(getClass()).error("Site not present: " + siteNumber);
		    continue;
		}

		metadataRecord.setMetadata(metadata + siteMetadata);
		ret.addRecord(metadataRecord);
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}

	return ret;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	return Arrays.asList(USGS_SITE_METADATA);
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected USGSConnectorSetting initSetting() {

	return new USGSConnectorSetting();
    }

}
