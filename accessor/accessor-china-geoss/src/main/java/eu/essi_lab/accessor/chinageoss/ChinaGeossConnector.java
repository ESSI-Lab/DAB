/**
 * 
 */
package eu.essi_lab.accessor.chinageoss;

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

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.accessor.satellite.common.SatelliteConnector;
import eu.essi_lab.accessor.satellite.common.SatelliteUtils;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.HarvestingProperties;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author Fabrizio
 */
public class ChinaGeossConnector extends SatelliteConnector<ChinaGeossConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "ChinaGeossConnector";

    /**
     * 
     */
    private static final String QUERY_ENDPOINT = "http://144.76.78.204:8080/webServe/services/ChinaGeo.ChinaGeoHttpEndpoint/getCnEoMetadata?";

    private static final int SHOW_COUNT = 1000;

    private static final String CHINA_GEOSS_CONNECTOR_DOWNLOAD_ERROR = "CHINA_GEOSS_CONNECTOR_DOWNLOAD_ERROR";
    private static final String CHINA_GEOSS_CONNECTOR_PARSING_ERROR = "CHINA_GEOSS_CONNECTOR_PARSING_ERROR";

    private int addedRecords;

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> response = new ListRecordsResponse<>();

	//
	// this is the very first request (empty properties found or without end harvesting time stamp)
	// adds the sentinel collections only once
	//
//	if (request.isFirstHarvesting()) {
//
//	    GSLoggerFactory.getLogger(getClass()).info("Storing China GEOSS collections STARTED");
//	    addCollections(response);
//	    GSLoggerFactory.getLogger(getClass()).info("Storing China GEOSS collections ENDED");
//	}

	//
	// start from resumption token/recovery resumption token
	//
	String resumptionToken = request.getResumptionToken();
	int start = 0;

	if (resumptionToken != null) {

	    start = Integer.valueOf(resumptionToken);

	} else {

	    HarvestingProperties properties = request.getHarvestingProperties();

	    //
	    // incremental harvesting based on the recovery resumption token
	    //
	    if (properties != null) {

		String recoveryToken = properties.getRecoveryResumptionToken();
		if (recoveryToken != null) { // it is null if the previous harvesting ended in a single iteration
		    // so the harvester did'nt has the time to write it
		    start = Integer.valueOf(recoveryToken);

		    GSLoggerFactory.getLogger(getClass()).info("Incremental harvesting enabled starting from: " + start);
		}
	    }
	}

	String query = "pageJSON=" + StringUtils.URLEncodeUTF8("{currentResult:" + start + ",showCount:" + SHOW_COUNT + "}");

	String search = QUERY_ENDPOINT + query;

	Downloader downloader = new Downloader();

	GSLoggerFactory.getLogger(getClass()).trace("Downloading scenes STARTED");
	GSLoggerFactory.getLogger(getClass()).trace("Current query: {}", query);

	Optional<InputStream> optional = downloader.downloadOptionalStream(search);

	GSLoggerFactory.getLogger(getClass()).trace("Downloading scenes ENDED");

	if (optional.isPresent()) {

	    JSONArray jsonArray = null;
	    try {
		List<String> jsonResponse = new XMLDocumentReader(optional.get()).evaluateTextContent("//*:return/text()");

		jsonArray = new JSONArray(jsonResponse.get(0));

	    } catch (Exception e) {

		throw GSException.createException(//
			getClass(), //
			e.getMessage(), //
			null, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_ERROR, //
			CHINA_GEOSS_CONNECTOR_PARSING_ERROR); //
	    }

	    int length = jsonArray.length();

	    GSLoggerFactory.getLogger(getClass()).trace("Downloaded {} scenes", length);

	    if (length == SHOW_COUNT) {

		int nextStart = start + SHOW_COUNT;
		response.setResumptionToken(String.valueOf(nextStart));

	    } else {

		GSLoggerFactory.getLogger(getClass()).trace("No more scenes to download");
	    }

	    //
	    // creating original metadata
	    //
	    for (int i = 0; i < length; i++) {

		if (getSetting().getMaxRecords().isPresent() && addedRecords > getSetting().getMaxRecords().get()) {

		    response.setResumptionToken(null);
		    return response;
		}

		JSONObject jsonObject = jsonArray.getJSONObject(i);

		OriginalMetadata original = new OriginalMetadata();
		original.setSchemeURI(ChinaGeossMapper.CHINA_GEOSS_SCHEME_URI);

		original.setMetadata(jsonObject.toString(3));

		response.addRecord(original);

		addedRecords++;
	    }
	} else {

	    throw GSException.createException(//
		    getClass(), //
		    "Unable to download scenes", //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    CHINA_GEOSS_CONNECTOR_DOWNLOAD_ERROR); //
	}

	return response;
    }

    @Override
    protected List<GSResource> getCollections() throws Exception {

	return SatelliteUtils.getChinaGeossCollections(new GSSource(), false);
    }

    @Override
    protected String getMetadataFormat() {

	return ChinaGeossMapper.CHINA_GEOSS_SCHEME_URI;
    }

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint().startsWith("http://144.76.78.204:8080");
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected ChinaGeossConnectorSetting initSetting() {

	return new ChinaGeossConnectorSetting();
    }

}
