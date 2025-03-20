/**
 *
 */
package eu.essi_lab.accessor.prisma;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import eu.essi_lab.accessor.satellite.common.SatelliteConnector;
import eu.essi_lab.accessor.satellite.common.SatelliteUtils;
import eu.essi_lab.lib.net.dirlisting.HREFGrabberClient;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.utils.WebConnector;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.utils.zip.GZIPUnzipper;
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
public class PRISMAConnector extends SatelliteConnector<PRISMAConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "PRISMAConnector";

    private static final String PRISMA_CONNECTOR_SCENE_LIST_DOWNLOAD_ERROR = "PRISMA_CONNECTOR_SCENE_LIST_DOWNLOAD_ERROR";
    private static final String PRISMA_CONNECTOR_IO_ERROR = "PRISMA_CONNECTOR_IO_ERROR";

    private static final int STEP = 100;

    private List<String> prismaYear;
    // private Map<String, Map<String, List<String>>> prismaMap;
    private List<File> zipList;
    private List<Boolean> completedList;
    private Downloader downloader;
    private WebConnector webConnector;

    private int totalResults;

    public PRISMAConnector() {

	this.downloader = new Downloader();
	this.webConnector = new WebConnector();
	// this.prismaMap = new HashMap<String, Map<String, List<String>>>();
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> response = new ListRecordsResponse<>();

	//
	// this is the very first request (empty properties found or without end harvesting time stamp)
	// adds the landsat collections only once
	//
	if (request.isFirstHarvesting()) {

	    GSLoggerFactory.getLogger(getClass()).info("Storing PRISMA collection STARTED");
	    addCollections(response);
	    GSLoggerFactory.getLogger(getClass()).info("Storing PRISMA collection ENDED");
	}
	// resumption token is the Year data url
	String resumptionToken = request.getResumptionToken();
	int start = 0;
	// token=datestamp
	if (resumptionToken != null) {

	    start = Integer.valueOf(resumptionToken);
	}

	HarvestingProperties properties = request.getHarvestingProperties();

	Date endDate = null;

	if (properties != null && !properties.isEmpty() && !request.isRecovered()) {

	    String timestamp = properties.getEndHarvestingTimestamp();
	    if (timestamp != null) {
		@SuppressWarnings("deprecation")
		long time = ISO8601DateTimeUtils.parseISO8601(timestamp).getTime();
		String iso8601DateTime = ISO8601DateTimeUtils.getISO8601DateTime(new Date(time));
		endDate = new Date(time);
		GSLoggerFactory.getLogger(getClass()).info("Incremental harvesting enabled starting from: " + iso8601DateTime);
	    }
	}
	// fix if the schedule restart from the beginning due to new deploy
	if (endDate == null && resumptionToken != null) {
	    if (properties != null && !properties.isEmpty()) {
		String timestamp = properties.getEndHarvestingTimestamp();
		if (timestamp != null) {
		    @SuppressWarnings("deprecation")
		    long time = ISO8601DateTimeUtils.parseISO8601(timestamp).getTime();
		    String iso8601DateTime = ISO8601DateTimeUtils.getISO8601DateTime(new Date(time));
		    endDate = new Date(time);
		    GSLoggerFactory.getLogger(getClass()).info("Incremental harvesting enabled starting from: " + iso8601DateTime);
		} else {
		    Date today = new Date();
		    endDate = new Date(today.getTime() - 24 * 60 * 60 * 1000);
		}
	    } else {
		Date today = new Date();
		endDate = new Date(today.getTime() - 24 * 60 * 60 * 1000);
	    }
	    start = 0;
	}
	GSLoggerFactory.getLogger(getClass()).info("Handling YEAR directory STARTED");

	String yearURL = null;
	if (prismaYear == null) {
	    prismaYear = webConnector.getHrefs(getSourceURL(), endDate);

	}
	yearURL = prismaYear.get(start);
	start++;
	// response = handleZip(FIRST_ZIP_URL, request, response, 0);
	GSLoggerFactory.getLogger(getClass()).info("Handling YEAR directory ENDED");

	GSLoggerFactory.getLogger(getClass()).info("Number of YEARS found {}.", prismaYear.size());

	// first year
	// yearURL = (yearURL != null) ? yearURL : resumptionToken;
	Map<String, Map<String, List<String>>> prismaMap = new HashMap<String, Map<String, List<String>>>();
	if (yearURL != null) {
	    List<String> months = webConnector.getHrefs(yearURL, endDate);
	    Map<String, List<String>> mapMonthDays = new HashMap<String, List<String>>();
	    for (String m : months) {
		List<String> days = webConnector.getHrefs(m, endDate);
		mapMonthDays.put(m, days);
	    }

	    prismaMap.put(yearURL, mapMonthDays);
	} else {
	    // never happens in theory
	    GSLoggerFactory.getLogger(getClass()).info("-----THIS SHOULD NEVER HAPPEN ERROR------");
	}

	// now prismaMap contains all months and days per year

	GSLoggerFactory.getLogger(getClass()).info("Searching records from the day folders STARTED");
	int addedRecords = 0;
	for (Map.Entry<String, Map<String, List<String>>> entry : prismaMap.entrySet()) {

	    Map<String, List<String>> monthDays = entry.getValue();

	    for (Map.Entry<String, List<String>> e : monthDays.entrySet()) {
		List<String> days = e.getValue();

		for (String d : days) {
		    List<String> omList = getRecords(d);
		    for (String s : omList) {

			OriginalMetadata metadata = new OriginalMetadata();
			metadata.setSchemeURI(PRISMAMapper.PRISMA_SCHEME_URI);
			metadata.setMetadata(s);
			response.addRecord(metadata);
			addedRecords++;
			totalResults++;
		    }

		}
	    }
	}
	GSLoggerFactory.getLogger(getClass()).debug("Handling scenes [{}] ENDED", addedRecords);
	if (prismaYear.size() > start) {
	    response.setResumptionToken(String.valueOf(start));
	} else {
	    GSLoggerFactory.getLogger(getClass()).debug("Handling ALL scenes [{}] ENDED", totalResults);
	    prismaYear = null;
	    response.setResumptionToken(null);
	}

	// response = handleZip(SECOND_ZIP_URL, request, response, 1);
	GSLoggerFactory.getLogger(getClass()).info("Searching records from the day folders ENDED");

	return response;
    }

    private List<String> getRecords(String d) throws GSException {
	List<String> listStringRecords = new ArrayList<String>();
	List<String> listRecord = webConnector.getHrefs(d, null);
	for (String csvFile : listRecord) {
	    if (csvFile.endsWith(".csv")) {
		List<String> listScenes = getOriginalMetadata(csvFile);
		for (String s : listScenes) {
		    listStringRecords.add(s);
		}
	    }
	}
	return listStringRecords;
    }

    private List<String> getOriginalMetadata(String csvUrl) throws GSException {
	List<String> ret = new ArrayList<String>();
	Optional<InputStream> inputStream = downloader.downloadOptionalStream(csvUrl);

	if (inputStream.isPresent()) {
	    try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream.get()))) {
		String temp = br.readLine(); // skip header line
		// String[] headersSplit = new String[] {};
		String separator = "\\|";
		StringBuilder sb = new StringBuilder();
		while ((temp = br.readLine()) != null) {

		    if (!temp.equals("")) {
			String[] dataSplit = temp.split(separator);

			String join = String.join("|", dataSplit);

			//
			// 0 FILENAME CODE
			String fileName = dataSplit[0];
			// 1 Validity Start
			String startDate = dataSplit[1];
			// 2 Validity Stop
			String endDate = dataSplit[2];
			// 3 Polygon
			String polygon = dataSplit[3];
			// 4 Cloud %
			String cloudCoverage = dataSplit[4];
			// 5 Metadata Point of Contact Organization Name
			String metadataOrganizationName = dataSplit[5];
			// 6 Metadata Point of Contact Organization Mail
			String metadataOrganizationMail = dataSplit[6];
			// 7 Metadata Date Stamp
			String dateStamp = dataSplit[7];
			// 8 Coordinate Reference System (CRS)
			String crs = dataSplit[8];
			// 9 Title
			String title = dataSplit[9];
			// 10 Dataset Identifier
			String identifier = dataSplit[10];
			// 11 Abstract
			String abstrakt = dataSplit[11];
			// 12 Dataset Creator Organization Name
			String creatorOrganizationName = dataSplit[12];
			// 13 Dataset Creator Organization Mail
			String creatorOrganizationMail = dataSplit[13];
			// 14 Dataset Spatial Resolution
			String spatialResolution = dataSplit[14];
			// 15 Dataset Spatial Units
			String spatialUnit = dataSplit[15];
			// 16 Limitations on Public Access
			String limitations = dataSplit[16];
			// 17 Conditions applying to Access and Use
			String conditions = dataSplit[17];
			// 18 Distribution Format
			String distributionFormat = dataSplit[18];
			// 19 Distribution link
			String distributionLink = dataSplit[19];
			// 20 Lineage
			String lineage = dataSplit[20];
			// 21 Graphic Overview
			String overview = dataSplit[21];
			// 22 Keywords
			String keywords = dataSplit[22];
			// 23 Measured Attribute (parameter) Name
			String parameter = dataSplit[23];
			// 24 Band Bound Min
			String bandMin = dataSplit[24];
			// 25 Band Bound Max
			String bandMax = dataSplit[25];
			// 26 Instrument Name
			String instrumentName = dataSplit[26];
			// 27 Instrument Identifier
			String instrumentIdentifier = dataSplit[27];
			// 28 Instrument Type
			String instrumentType = dataSplit[28];
			// 29 Platform Name
			String platformName = dataSplit[29];
			// 30Platform Identifier
			String platformIdentifier = dataSplit[30];
			// 31 Sensor Name
			String sensorName = dataSplit[31];
			// 32 Sensor Identifier
			String sensorIdentifier = dataSplit[32];
			// 33 Sensor Type
			String sensorType = dataSplit[33];

			PRISMAScene scene = new PRISMAScene();

			scene.setAbstrakt(abstrakt);
			scene.setBandMax(bandMax);
			scene.setBandMin(bandMin);
			scene.setCloudCoverage(cloudCoverage);
			scene.setConditions(conditions);
			scene.setCreatorOrganizationMail(creatorOrganizationMail);
			scene.setCreatorOrganizationName(creatorOrganizationName);
			scene.setCrs(crs);
			scene.setDateStamp(dateStamp);
			scene.setDistributionFormat(distributionFormat);
			scene.setDistributionLink(distributionLink);
			scene.setEndDate(endDate);
			scene.setFileName(fileName);
			scene.setIdentifier(identifier);
			scene.setInstrumentIdentifier(instrumentIdentifier);
			scene.setInstrumentName(instrumentName);
			scene.setInstrumentType(instrumentType);
			scene.setKeywords(keywords);
			scene.setLimitations(limitations);
			scene.setLineage(lineage);
			scene.setMetadataOrganizationMail(metadataOrganizationMail);
			scene.setMetadataOrganizationName(metadataOrganizationName);
			scene.setOverview(overview);
			scene.setParameter(parameter);
			scene.setPlatformIdentifier(platformIdentifier);
			scene.setPlatformName(platformName);
			scene.setPolygon(polygon);
			scene.setSensorIdentifier(sensorIdentifier);
			scene.setSensorName(sensorName);
			scene.setSensorType(sensorType);
			scene.setSpatialResolution(spatialResolution);
			scene.setSpatialUnit(spatialUnit);
			scene.setStartDate(startDate);
			scene.setTitle(title);

			ret.add(join);
			// if (headersSplit.length == dataSplit.length) {
			// HashMap<String, String> values = new HashMap<>();
			// for (int i = 0; i < dataSplit.length; i++) {
			// String header = headersSplit[i];
			// String data = dataSplit[i];
			// values.put(header, data);
			// }
			// String mainCode = "";
			//
			// }
			// }
		    }
		}

		return ret;

	    } catch (Exception e) {

		throw GSException.createException(//
			this.getClass(), //
			PRISMA_CONNECTOR_IO_ERROR, //
			null, //
			ErrorInfo.ERRORTYPE_SERVICE, //
			ErrorInfo.SEVERITY_ERROR, //
			PRISMA_CONNECTOR_SCENE_LIST_DOWNLOAD_ERROR);
	    }

	}

	return null;

    }

    /**
     * @param zipUrl
     * @param response
     * @param request
     * @param sceneLinks
     * @param first
     * @return
     */
    private ListRecordsResponse<OriginalMetadata> handleZip(//
	    String zipUrl, //
	    ListRecordsRequest request, //
	    ListRecordsResponse<OriginalMetadata> response, //
	    int zipIndex) throws GSException {

	int start = 0;
	int toIndex = STEP;

	if (completedList.get(zipIndex)) {

	    GSLoggerFactory.getLogger(getClass()).info("No new scene to harvest, ZIP " + zipIndex + " is completed");
	    return response;
	}

	try {

	    if (request.isFirst()) {

		//
		// retrieves the scene list
		//
		downloadZipFile(zipUrl, zipIndex);

		int count = countLines(zipList.get(zipIndex));

		//
		// incremental harvesting and/or recovery
		//
		if (request.getHarvestingProperties() != null && request.getHarvestingProperties().getRecoveryResumptionToken() != null) {

		    start = readResumptionToken(request.getHarvestingProperties().getRecoveryResumptionToken(), zipIndex);
		    toIndex = start + STEP;

		    if (start >= count) {

			completedList.set(zipIndex, true);
			deleteZIPFile(zipIndex);

			GSLoggerFactory.getLogger(getClass()).info("No new scene to harvest, ZIP " + zipIndex + " is completed");
			return response;
		    }
		}
	    }

	    if (request.getResumptionToken() != null) {
		//
		// next iteration or recovery
		//
		start = readResumptionToken(request.getResumptionToken(), zipIndex);
		toIndex = start + STEP;
	    }

	    int count = countLines(zipList.get(zipIndex));

	    //
	    // handles the to index and the resumption token
	    //
	    if (toIndex >= count) {

		// no resumption token is set for this ZIP
		toIndex = count;
		completedList.set(zipIndex, true);

		GSLoggerFactory.getLogger(getClass()).info("Last iteration for ZIP " + zipIndex);

	    } else {

		String token = updateResumptionToken(request, response, toIndex, zipIndex);
		response.setResumptionToken(token);

		GSLoggerFactory.getLogger(getClass()).debug("Response resumption token updated: {}", token);
	    }

	    if (toIndex > start) {

		List<String> currentScenesLinks = getHTMLSceneLinks(start, toIndex, zipList.get(zipIndex));
		GSLoggerFactory.getLogger(getClass()).debug("Handling scenes [{}-{}/{}] STARTED", start, toIndex, count);

		for (String sceneLink : currentScenesLinks) {

		    addOriginalMetadata(sceneLink, response);
		}

		GSLoggerFactory.getLogger(getClass()).debug("Handling scenes [{}-{}/{}] ENDED", start, toIndex, count);

		if (completedList.get(zipIndex)) {

		    deleteZIPFile(zipIndex);
		}

	    } else {

		GSLoggerFactory.getLogger(getClass()).info("No new scene to harvest, ZIP " + zipIndex + " is completed");
	    }

	} catch (GSException ex) {

	    throw ex;

	} catch (IOException ex) {

	    throw GSException.createException(//
		    getClass(), //
		    ex.getMessage(), null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    PRISMA_CONNECTOR_IO_ERROR //
	    );
	}

	return response;
    }

    /**
     * @param zipIndex
     */
    private void deleteZIPFile(int zipIndex) {

	File file = zipList.get(zipIndex);
	try {
	    if (file.exists()) {
		GSLoggerFactory.getLogger(getClass()).debug("Deleting ZIP file {} STARTED", zipIndex);

		file.delete();
		file.deleteOnExit();

		GSLoggerFactory.getLogger(getClass()).debug("Deleting ZIP file {} DONE", zipIndex);

	    }
	} catch (Exception ex) {
	    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);
	}
    }

    /**
     * @param token
     * @param toIndex
     * @param firstZIP
     * @return
     */
    private String updateResumptionToken(//
	    ListRecordsRequest request, //
	    ListRecordsResponse<OriginalMetadata> response, //
	    int toIndex, //
	    int zipIndex) {

	if (request.isFirstHarvesting()) {

	    if (zipIndex == 1) {
		return response.getResumptionToken() + "_" + String.valueOf(toIndex);
	    }

	    return String.valueOf(toIndex);
	}

	String responseToken = response.getResumptionToken();
	// this happens when ZIP0 ends before ZIP1
	if (responseToken == null) {
	    responseToken = request.getResumptionToken();
	}

	String[] split = null;
	// this happens during the first request with ZIP0 after a crash,
	// or ZIP0 in the first selective harvesting request
	if (responseToken == null) {
	    String recoveryToken = request.getHarvestingProperties().getRecoveryResumptionToken();
	    split = recoveryToken.split("_");
	} else {
	    split = zipIndex == 0 ? request.getResumptionToken().split("_") : responseToken.split("_");
	}

	if (split.length == 1) {
	    return String.valueOf(toIndex);
	}

	String firstToken = split[0];
	String secondToken = split[1];

	if (zipIndex == 0) {
	    return String.valueOf(toIndex) + "_" + secondToken;
	}

	return firstToken + "_" + String.valueOf(toIndex);
    }

    /**
     * @param token
     * @param firstZIP
     * @return
     */
    private int readResumptionToken(String token, int zipIndex) {

	String[] split = token.split("_");
	if (split.length == 1) {
	    return Integer.valueOf(token);
	}

	return Integer.valueOf(split[zipIndex]);
    }

    /**
     * @param zipUrl
     * @param sceneLinks
     * @throws GSException
     * @throws IOException
     */
    private void downloadZipFile(String zipUrl, int zipIndex) throws GSException, IOException {

	GSLoggerFactory.getLogger(getClass()).debug("PRISMA scenes list download STARTED");

	Downloader downloader = new Downloader();
	Optional<InputStream> optional = downloader.downloadOptionalStream(zipUrl);

	if (optional.isPresent()) {

	    File file = null;

	    InputStream stream = optional.get();

	    GZIPUnzipper unzipper = new GZIPUnzipper(stream);
	    file = unzipper.unzip();

	    zipList.add(file);

	    int count = countLines(file);

	    GSLoggerFactory.getLogger(getClass()).debug("PRISMA scenes list download ENDED");
	    GSLoggerFactory.getLogger(getClass()).debug("Total number of PRISMA scenes: {}", count);

	} else {

	    throw GSException.createException(//
		    getClass(), //
		    "Error occurred, unable to download scenes list", //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    PRISMA_CONNECTOR_SCENE_LIST_DOWNLOAD_ERROR //
	    );
	}
    }

    /**
     * @param zipFile
     * @return
     * @throws IOException
     */
    private int countLines(File zipFile) throws IOException {

	return (int) (Files.lines(//
		Paths.get(zipFile.getAbsolutePath())).count());
    }

    /**
     * @param start
     * @param end
     * @param zipFile
     * @return
     * @throws IOException
     */
    private static List<String> getHTMLSceneLinks(int start, int end, File zipFile) throws IOException {

	// this is to skip the first line with the headers
	start = start + 1;
	end = end + 1;

	List<String> collect = new ArrayList<>();

	collect = Files.lines(//
		Paths.get(zipFile.getAbsolutePath())).//
		limit(end).//
		skip(start).//
		map(s -> s.substring(s.indexOf("https"), s.length())).//
		collect(Collectors.toList());

	return collect;
    }

    /**
     * @param response
     * @throws GSException
     */
    private void addOriginalMetadata(String sceneLink, ListRecordsResponse<OriginalMetadata> response) {

	try {

	    HREFGrabberClient client = new HREFGrabberClient(new URL(sceneLink));
	    Optional<String> first = client.grabLinks().//
		    stream().//
		    filter(l -> l.endsWith("MTL.txt")).//
		    map(l -> l.replace("index.html/", "")).//
		    findFirst();

	    if (first.isPresent()) {

		String txtLink = first.get();

		Downloader downloader = new Downloader();
		Optional<String> optional = downloader.downloadOptionalString(txtLink);

		if (optional.isPresent()) {

		    String metadata = optional.get();
		    String parentId = "PARENTID = ASI_PRISMA\n";
		    String link = "TXTLINK = " + txtLink + "\n";
		    metadata += parentId;
		    metadata += link;

		    OriginalMetadata original = new OriginalMetadata();
		    original.setMetadata(metadata);
		    original.setSchemeURI(PRISMAMapper.PRISMA_SCHEME_URI);

		    response.addRecord(original);

		    return;
		}
	    } else {
		GSLoggerFactory.getLogger(getClass()).error("Unable to find original metadata file from {}", sceneLink);
	    }
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    @Override
    protected List<GSResource> getCollections() throws Exception {

	return Arrays.asList(SatelliteUtils.getPrismaCollection(new GSSource(), false));
    }

    @Override
    protected String getMetadataFormat() {

	return PRISMAMapper.PRISMA_SCHEME_URI;
    }

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint().contains("90.147.167.250");
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected PRISMAConnectorSetting initSetting() {

	return new PRISMAConnectorSetting();
    }

    @Override
    public boolean supportsIncrementalHarvesting() throws GSException {
	// TODO Auto-generated method stub
	return true;
    }

  
}
