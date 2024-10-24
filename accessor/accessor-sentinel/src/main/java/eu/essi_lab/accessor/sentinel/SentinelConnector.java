/**
 * 
 */
package eu.essi_lab.accessor.sentinel;

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

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.w3c.dom.Node;

import eu.essi_lab.accessor.satellite.common.SatelliteConnector;
import eu.essi_lab.accessor.satellite.common.SatelliteUtils;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLNodeReader;
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
public class SentinelConnector extends SatelliteConnector<SentinelConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "SentinelConnector";

    private static final int MAX_ROWS = 100;

    private static final int TIME_BACK = 0; // 0 hours

    private static final String SENTINEL_CONNECTOR_ERROR = "SENTINEL_CONNECTOR_ERROR";

    private static final String SENTINEL_CONNECTOR_DOWNLOAD_ERROR = "SENTINEL_CONNECTOR_DOWNLOAD_ERROR";

    private int totalResults;
    private int addedRecords;

    private Date startDate;

    /**
     * @param startDate
     */
    public void setStartDate(Date startDate) {

	this.startDate = startDate;
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> response = new ListRecordsResponse<>();

	HarvestingProperties properties = request.getHarvestingProperties();

	//
	// this is the very first request (empty properties found or without end harvesting time stamp)
	// adds the sentinel collections only once
	//
	if (request.isFirstHarvesting()) {

	    GSLoggerFactory.getLogger(getClass()).info("Storing Sentinel collections STARTED");
	    addCollections(response);
	    GSLoggerFactory.getLogger(getClass()).info("Storing Sentinel collections ENDED");
	}

	//
	// creates the start query, from the init data to now
	//
	String endDate = ISO8601DateTimeUtils.getISO8601DateTime();

	if (startDate == null) {
	    endDate = ISO8601DateTimeUtils.getISO8601DateTime(new Date());
	} else {
	    endDate = ISO8601DateTimeUtils.getISO8601DateTime(startDate);
	}

	String query = createQueryPart(getInitialDate(), endDate, "1");
	String resumptionToken = request.getResumptionToken();

	//
	// incremental harvesting
	//
	if (properties != null && !properties.isEmpty() && !request.isRecovered() && resumptionToken == null) {

	    String timestamp = properties.getEndHarvestingTimestamp();
	    @SuppressWarnings("deprecation")
	    long time = ISO8601DateTimeUtils.parseISO8601(timestamp).getTime();
	    time -= TIME_BACK;
	    String iso8601DateTime = ISO8601DateTimeUtils.getISO8601DateTime(new Date(time));

	    query = createQueryPart(iso8601DateTime, endDate, "1");

	    GSLoggerFactory.getLogger(getClass()).info("Incremental harvesting enabled starting from: " + iso8601DateTime);
	}
	//
	// successive query or recovery
	//
	else if (resumptionToken != null) {

	    query = resumptionToken;
	}

	String user = ConfigurationWrapper.getCredentialsSetting().getSentinelUser().orElse(null);
	String password = ConfigurationWrapper.getCredentialsSetting().getSentinelPassword().orElse(null);

	String search = "https://" + user + ":" + password + "@scihub.copernicus.eu/dhus/search?" + query;

	Downloader downloader = new Downloader();

	GSLoggerFactory.getLogger(getClass()).trace("Downloading scenes STARTED");
	GSLoggerFactory.getLogger(getClass()).trace("Current query: {}", query);

	Optional<InputStream> optional = downloader.downloadOptionalStream(search);

	GSLoggerFactory.getLogger(getClass()).trace("Downloading scenes ENDED");

	if (optional.isPresent()) {

	    try {

		InputStream stream = optional.get();
		XMLDocumentReader reader = new XMLDocumentReader(stream);

		totalResults = Integer.valueOf(reader.evaluateString("//*:feed/*:totalResults"));
		if (request.isFirst()) {

		    GSLoggerFactory.getLogger(getClass()).info("Found " + totalResults + " scenes to harvest");
		}

		//
		// adds the original
		//
		Node[] entries = reader.evaluateNodes("//*:entry");

		for (int i = 0; i < entries.length; i++) {

		    if (getSetting().getMaxRecords().isPresent() && addedRecords > getSetting().getMaxRecords().get()) {

			response.setResumptionToken(null);
			return response;
		    }

		    Node entry = entries[i];

		    OriginalMetadata original = new OriginalMetadata();
		    original.setSchemeURI(SentinelMapper.SENTINEL_SCHEME_URI);

		    String entryString = XMLNodeReader.asString(entry, true);
		    original.setMetadata(entryString);

		    response.addRecord(original);

		    addedRecords++;
		}

		//
		// handles the resumption token
		//
		int startIndex = Integer.valueOf(reader.evaluateString("//*:feed/*:startIndex"));

		if (entries.length > 0) {

		    if (startIndex - MAX_ROWS > 0) {
			GSLoggerFactory.getLogger(getClass()).debug("Handling scenes [{}-{}/{}] ENDED", (startIndex - MAX_ROWS), startIndex,
				totalResults);
		    }

		    int nextIndex = startIndex + MAX_ROWS;
		    query = updateQueryPart(query, startIndex, nextIndex);

		    GSLoggerFactory.getLogger(getClass()).debug("Handling scenes [{}-{}/{}] STARTED", startIndex, nextIndex, totalResults);

		} else {

		    GSLoggerFactory.getLogger(getClass()).info("No more scenes found, process ENDED");

		    query = null;
		}

		response.setResumptionToken(query);

	    } catch (Exception ex) {

		throw GSException.createException(//
			getClass(), //
			ex.getMessage(), //
			null, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_ERROR, //
			SENTINEL_CONNECTOR_ERROR); //

	    }
	} else {

	    throw GSException.createException(//
		    getClass(), //
		    "Unable to download scenes", //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    SENTINEL_CONNECTOR_DOWNLOAD_ERROR); //
	}

	return response;
    }

    /**
     * @param startDate
     * @param endDate
     * @param startRow
     * @return
     */
    private String createQueryPart(String startDate, String endDate, String startRow) {

	return "q=ingestiondate%3A%5B%20" + startDate + "%20TO%20" + endDate

		+ "%5D%20AND%20(filename%3AS1A_*%20OR%20filename%3AS1B_*%20OR%20filename%3AS2A_*%20OR%20filename%3AS2B_*%20OR%20filename%3AS3A_*)&rows=100&start="

		// + "%5D%20AND%20(filename%3AS1A_*%20OR%20filename%3AS1B_*)&rows=100&start="

		// + "%5D%20AND%20(filename%3AS2A_*%20OR%20filename%3AS2B_*)&rows=100&start="

		// + "%5D%20AND%20(filename%3AS3A_*)&rows=100&start="

		+ startRow;
    }

    /**
     * @param query
     * @param startRow
     * @param endRow
     * @return
     */
    private String updateQueryPart(String query, int startRow, int endRow) {

	return query.replace("start=" + String.valueOf(startRow), "start=" + String.valueOf(endRow));
    }

    /**
     * @return
     */
    private String getInitialDate() {

	return "2014-01-01T00:00:000Z";
    }

    @Override
    protected List<GSResource> getCollections() throws Exception {

	return SatelliteUtils.getSentinelCollections(new GSSource(), false);
    }

    @Override
    protected String getMetadataFormat() {

	return SentinelMapper.SENTINEL_SCHEME_URI;
    }

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint().startsWith("https://scihub.copernicus.eu/dhus/search");
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected SentinelConnectorSetting initSetting() {

	return new SentinelConnectorSetting();
    }
}
