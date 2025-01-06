package eu.essi_lab.accessor.polytope;

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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.xml.xpath.XPathExpressionException;

import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.xml.sax.SAXException;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author roncella
 */
public class PolytopeConnector extends HarvestedQueryConnector<PolytopeConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "PolytopeConnector";

    /**
     * SERVICE ENDPOINT: https://polytope.ecmwf.int/api/v1/
     * COLLECTIONS: https://polytope.ecmwf.int/api/v1/collections
     * 25 columns (first column (0) empty)
     * ;obstype@body;codetype@body;entryno@body;varno@body;date@hdr;time@hdr;stalt@hdr;statid@hdr;obstype@hdr;codetype@hdr;source@hdr;
     * groupid@hdr;reportype@hdr;class@desc;type@desc;stream@desc;expver@desc;levtype@desc;andate@desc;antime@desc;lat@hdr;lon@hdr;obsvalue@body;stationid@hdr,
     * column0: number of measures per day (cumulative)
     * column4: varno (identifier of variable)
     * column19: andate (date of measure)
     * column20: antime (time of measure)
     * column21: lat (station latitude)
     * column22: lon (station longitude)
     * column23: obsvalue (value of variable)
     * column24: stationid (Name of station)
     */
    private static final String POLYTOPE_STATION_ERROR = "Unable to find stations URL";
    private static final String POLYTOPE_PARSING_STATION_ERROR = "POLYTOPE_PARSING_STATION_ERROR";
    private static final String POLYTOPE_STATIONS_URL_NOT_FOUND_ERROR = "POLYTOPE_STATIONS_URL_NOT_FOUND_ERROR";

    private Downloader downloader;

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    private int partialNumbers;

    public PolytopeConnector() {

	this.downloader = new Downloader();

    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest listRecords) throws GSException {

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	// post requests with Polytope Rest API to get data
	// e.g. endpoint: https://polytope.ecmwf.int/api/v1/requests/i-change
	// Authorization header: EmailKey username:psw
	//
	// currently we read the csv file downloaded from python code

	int count = 0;

	String token = listRecords.getResumptionToken();
	int start = 0;
	if (token != null) {

	    start = Integer.valueOf(token);
	}

	Optional<Integer> mr = getSetting().getMaxRecords();
	boolean maxNumberReached = false;
	if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent() && start > mr.get() - 1) {
	    // max record set
	    maxNumberReached = true;
	}

	Optional<InputStream> stream = downloader.downloadOptionalStream(getSourceURL());

	if (stream.isPresent()) {

	    InputStream inputStream = stream.get();
	    final String url = getSourceURL().endsWith("/") ? getSourceURL() : getSourceURL() + "/";

	    try {
		XMLDocumentReader reader = new XMLDocumentReader(inputStream);

		Arrays.asList(reader.evaluateNodes("//*:Key/text()")).//
			stream().//

			map(n -> {

			    OriginalMetadata originalMetadata = null;

			    String u = url + n.getNodeValue();
			    Downloader down = new Downloader();
			    Optional<InputStream> csv = down.downloadOptionalStream(u);

			    if (csv.isPresent()) {

				try {
				    String meta = IOStreamUtils.asUTF8String(csv.get());

				    originalMetadata = new OriginalMetadata();
				    originalMetadata.setMetadata(meta);
				    originalMetadata.setSchemeURI(CommonNameSpaceContext.POLYTOPE);
				    if(u.contains("meteotracker")) {
					originalMetadata.setSchemeURI(CommonNameSpaceContext.POLYTOPE_METEOTRACKER);
				    }

				} catch (IOException e) {

				    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
				}
			    }

			    return originalMetadata;
			}).//
			filter(Objects::nonNull).//
			forEach(o -> ret.addRecord(o));

	    } catch (SAXException | IOException | XPathExpressionException e) {

		GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	    }
	}
	// List<InputStream> datasets = PolytopeUtils.getAmsterdamDatasets();
	//
	// if (!maxNumberReached) {
	//
	// try {
	//
	// for (InputStream is : datasets) {
	//
	// String originalMetadata = IOUtils.toString(is, StandardCharsets.UTF_8.name());
	//
	// OriginalMetadata metadata = new OriginalMetadata();
	// metadata.setSchemeURI(CommonNameSpaceContext.POLYTOPE);
	// metadata.setMetadata(originalMetadata);
	// ret.addRecord(metadata);
	// partialNumbers++;
	// count++;
	// if (is != null)
	// is.close();
	//
	// }
	// } catch (Exception e) {
	// throw GSException.createException(//
	// this.getClass(), e.getMessage(), null, //
	// ErrorInfo.ERRORTYPE_SERVICE, //
	// ErrorInfo.SEVERITY_ERROR, //
	// POLYTOPE_PARSING_STATION_ERROR);
	//
	// }
	// } else {
	// ret.setResumptionToken(null);
	//
	// logger.debug("Added Collection records: {} . TOTAL STATION SIZE: {}", partialNumbers, datasets.size());
	// partialNumbers = 0;
	// return ret;
	// }

	return ret;

    }

    public Downloader getDownloader() {
	return downloader == null ? new Downloader() : downloader;
    }

    public void setDownloader(Downloader downloader) {

	this.downloader = downloader;
    }

    /**
     * @return
     * @throws GSException
     */
    public Optional<File> findStations() throws GSException {

	// TODO: to be implemented
	//
	// logger.trace("Stations URL finding STARTED");
	//
	// String polytopeUrl = getSourceURL();
	//
	// logger.trace("Polytope URL: {}", polytopeUrl);
	//
	// String url = addCredentialsInRequests(polytopeUrl);
	//
	// File metadataFile = null;
	//
	// try {
	// metadataFile = downloader.downloadStream(url, POLYTOPE_URL_METADATA_PATH);
	// } catch (Exception e) {
	// throw GSException.createException(//
	// this.getClass(), e.getMessage(), null, //
	// ErrorInfo.ERRORTYPE_SERVICE, //
	// ErrorInfo.SEVERITY_ERROR, //
	// POLYTOPE_PARSING_STATION_ERROR);
	// }
	//
	// return Optional.of(metadataFile);
	return null;
    }

    /**
     * @param stationsURL
     * @throws GSException
     * @throws IOException
     * @throws ClientProtocolException
     */
    private Map<String, PolytopeStation> parseStations(File stations) throws GSException {

	// TODO: to be implemented
//	Map<String, PolytopeStation> stationMap = new HashMap<>();
//
//	try (BufferedReader bfReader = new BufferedReader(new FileReader(stations))) {
//
//	    String temp = bfReader.readLine(); // skip header line
//
//	    logger.trace("Reading header line ENDED");
//
//	    logger.trace("Executing while STARTED");
//
//	    while ((temp = bfReader.readLine()) != null) {
//
//		if (!temp.equals("")) {
//
//		    String[] split = temp.split(",", -1);
//		    // 0 STATION CODE
//		    String stationCode = split[0].replace("\"", "");
//		    // 1 WBAN
//		    // 2 STATION NAME - name of the station that collected thunder count
//		    String stationName = split[2].replace("\"", "");
//		    // 3 - CTRY - is the country of the station
//		    String stationCountry = split[3].replace("\"", "");
//		    // 4 - STATE -
//		    String stationState = split[4].replace("\"", "");
//		    // 5 - ICAO -
//		    String stationIcao = split[5].replace("\"", "");
//		    // 6 - LAT -
//		    String stationLat = split[6].replace("\"", "");
//		    // 7 - LON -
//		    String stationLon = split[7].replace("\"", "");
//		    // 8 - ELEV (M) -
//		    String stationElevation = split[8].replace("\"", "");
//		    // 9 - BEGIN -
//		    String stationBegin = split[9].replace("\"", "");
//		    // 10 - END -
//		    String stationEnd = split[10].replace("\"", "");
//		    String startTime = split[11].replace("\"", "");
//		    String endTime = split[12].replace("\"", "");
//
//		    PolytopeStation station = new PolytopeStation(stationCode, stationName, stationLat, stationLon, stationElevation,
//			    stationBegin, stationEnd, startTime, endTime);
//		    if (stationCountry != null && !stationCountry.equals("")) {
//			station.setCountry(stationCountry);
//		    } else {
//			station.setCountry("");
//		    }
//		    if (stationIcao != null && !stationIcao.equals("")) {
//			station.setIcao(stationIcao);
//		    } else {
//			station.setIcao("");
//		    }
//		    if (stationState != null && !stationState.equals("")) {
//			station.setState(stationState);
//		    } else {
//			station.setState("");
//		    }
//
//		    // add station to the map
//		    if (!stationMap.containsKey(stationCode)) {
//			stationMap.put(stationCode, station);
//		    }
//		}
//	    }
//
//	    return stationMap;
//
//	} catch (Exception e) {
//
//	    throw GSException.createException(//
//		    this.getClass(), //
//		    POLYTOPE_STATION_ERROR, //
//		    null, //
//		    ErrorInfo.ERRORTYPE_SERVICE, //
//		    ErrorInfo.SEVERITY_ERROR, //
//		    POLYTOPE_STATIONS_URL_NOT_FOUND_ERROR);
//	}
	return null;
    }

    /**
     * @param station
     * @param name
     * @return
     */
    private String createOriginalMetadata(PolytopeStation station, String txtname) {

	logger.trace("Creating metadata results from {} STARTED", station.getName());

	String endpoint = getSourceURL() + "/" + txtname;

	String metadataRecord = createRecord(station, endpoint);

	logger.trace("Creating metadata results from {} ENDED", station.getName());

	return metadataRecord;

    }

    private String createRecord(PolytopeStation station, String txtname) {

	StringBuilder sb = new StringBuilder();

	sb.append(station.getStationCode());
	sb.append(",");
	sb.append(station.getName());
	sb.append(",");
	sb.append(station.getMinLat());
	sb.append(",");
	sb.append(station.getMinLon());
	sb.append(",");
	sb.append(station.getMaxLat());
	sb.append(",");
	sb.append(station.getMaxLon());
	sb.append(",");
	sb.append(station.getMinElevation());
	sb.append(",");
	sb.append(station.getMaxElevation());
	sb.append(",");
	sb.append(station.getStartDateTime());
	sb.append(",");
	sb.append(station.getEndDateTime());
	sb.append(",");
	sb.append(station.getIcao());
	sb.append(",");
	sb.append(station.getState());
	sb.append(",");
	sb.append(station.getCountry());
	sb.append(",");
	sb.append(getSourceURL());
	sb.append(",");

	sb.append(txtname);

	return sb.toString();
    }

    @Override
    public List<String> listMetadataFormats() {
	List<String> toret = new ArrayList<>();
	toret.add(CommonNameSpaceContext.POLYTOPE);
	toret.add(CommonNameSpaceContext.POLYTOPE_METEOTRACKER);
	return toret;
    }

    @Override
    public String getSourceURL() {
        return "https://s3.amazonaws.com/i-change/";
    }
    
    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint().startsWith("https://s3.amazonaws.com/i-change") || source.getEndpoint().contains("polytope.ecmwf.int");

	// TODO: to be implemented
	// String baseEndpoint = source.getEndpoint();
	//
	// if (!baseEndpoint.endsWith("/")) {
	// baseEndpoint += "/";
	// }
	//
	// String url = baseEndpoint + "collections";
	//
	// Map<String, String> headers = new HashMap<String, String>();
	// headers.put("Authorization", "EmailKey " + getSetting().getPolytopeUsername() + ":" +
	// getSetting().getPolytopePassword());
	//
	// Downloader d = getDownloader();
	// d.setRequestHeaders(headers);
	//
	// try {
	//
	// return d.checkConnectivity(url);
	//
	// } catch (Exception e) {
	// // any exception during download or during XML parsing
	// logger.warn("Exception during connection to FTP remote service", e);
	// }
	// return false;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected PolytopeConnectorSetting initSetting() {

	return new PolytopeConnectorSetting();
    }
}
