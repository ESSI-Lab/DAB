/**
 *
 */
package eu.essi_lab.accessor.landsat8;

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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import eu.essi_lab.accessor.satellite.common.SatelliteConnector;
import eu.essi_lab.accessor.satellite.common.SatelliteUtils;
import eu.essi_lab.lib.net.dirlisting.HREFGrabberClient;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.zip.GZIPUnzipper;
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
public class Landsat8Connector extends SatelliteConnector<Landsat8ConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "Landsat8Connector";

    private static final String LANDSAT_8_CONNECTOR_SCENE_LIST_DOWNLOAD_ERROR = "LANDSAT_8_CONNECTOR_SCENE_LIST_DOWNLOAD_ERROR";
    private static final String LANDSAT_8_CONNECTOR_IO_ERROR = "LANDSAT_8_CONNECTOR_IO_ERROR";

    private static final int STEP = 100;

    private static final String FIRST_ZIP_URL = "http://landsat-pds.s3.amazonaws.com/scene_list.gz";

    private static final String SECOND_ZIP_URL = "http://landsat-pds.s3.amazonaws.com/c1/L8/scene_list.gz";

    private List<File> zipList;

    private List<Boolean> completedList;

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> response = new ListRecordsResponse<>();

	if (request.isFirst()) {

	    completedList = new ArrayList<>();
	    completedList.add(false);
	    completedList.add(false);
	    zipList = new ArrayList<>();
	}

	//
	// this is the very first request (empty properties found or without end harvesting time stamp)
	// adds the landsat collections only once
	//
	if (request.isFirstHarvesting()) {

	    GSLoggerFactory.getLogger(getClass()).info("Storing Landsat collection STARTED");
	    addCollections(response);
	    GSLoggerFactory.getLogger(getClass()).info("Storing Landsat collection ENDED");
	}

	GSLoggerFactory.getLogger(getClass()).info("Handling first ZIP file STARTED");
	response = handleZip(FIRST_ZIP_URL, request, response, 0);
	GSLoggerFactory.getLogger(getClass()).info("Handling first ZIP file ENDED");

	GSLoggerFactory.getLogger(getClass()).info("Handling second ZIP file STARTED");
	response = handleZip(SECOND_ZIP_URL, request, response, 1);
	GSLoggerFactory.getLogger(getClass()).info("Handling second ZIP file ENDED");

	return response;
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
		    LANDSAT_8_CONNECTOR_IO_ERROR //
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

	GSLoggerFactory.getLogger(getClass()).debug("Landsat8 scenes list download STARTED");

	Downloader downloader = new Downloader();
	Optional<InputStream> optional = downloader.downloadOptionalStream(zipUrl);

	if (optional.isPresent()) {

	    File file = null;

	    InputStream stream = optional.get();

	    GZIPUnzipper unzipper = new GZIPUnzipper(stream);
	    file = unzipper.unzip();

	    zipList.add(file);

	    int count = countLines(file);

	    GSLoggerFactory.getLogger(getClass()).debug("Landsat8 scenes list download ENDED");
	    GSLoggerFactory.getLogger(getClass()).debug("Total number of landsat8 scenes: {}", count);

	} else {

	    throw GSException.createException(//
		    getClass(), //
		    "Error occurred, unable to download scenes list", //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    LANDSAT_8_CONNECTOR_SCENE_LIST_DOWNLOAD_ERROR //
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

	collect = Files
		.lines(//
			Paths.get(zipFile.getAbsolutePath()))
		.//
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
		    String parentId = "PARENTID = USGS_LANDSAT_8\n";
		    String link = "TXTLINK = " + txtLink + "\n";
		    metadata += parentId;
		    metadata += link;

		    OriginalMetadata original = new OriginalMetadata();
		    original.setMetadata(metadata);
		    original.setSchemeURI(Landsat8Mapper.LANDSAT_8_SCHEME_URI);

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

	return Arrays.asList(SatelliteUtils.getLandsatCollection(new GSSource(), false));
    }

    @Override
    protected String getMetadataFormat() {

	return Landsat8Mapper.LANDSAT_8_SCHEME_URI;
    }

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint().startsWith("http://landsat-pds.s3.amazonaws.com/scene_list.gz");
    }

    public static void main(String[] args) throws IOException {

	int step = 10;

	for (int start = 0; start < 30;) {

	    List<String> sceneLinks = getHTMLSceneLinks(//
		    start, //
		    start + step, //
		    new File("C:\\Users\\Fabrizio\\Desktop\\scene_list_DEFAULT"));

	    for (String string : sceneLinks) {

		System.out.println(string);
	    }

	    start = start + step;
	}

    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected Landsat8ConnectorSetting initSetting() {

	return new Landsat8ConnectorSetting();
    }
}
