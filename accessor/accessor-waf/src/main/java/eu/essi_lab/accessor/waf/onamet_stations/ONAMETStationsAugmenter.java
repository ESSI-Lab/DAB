package eu.essi_lab.accessor.waf.onamet_stations;

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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import eu.essi_lab.access.augmenter.EasyAccessAugmenter;
import eu.essi_lab.accessor.waf.onamet_stations.ONAMETParameter.ONAMETParameterId;
import eu.essi_lab.augmenter.ResourceAugmenter;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.VerticalExtent;
import eu.essi_lab.lib.net.dirlisting.WAFClient;
import eu.essi_lab.lib.net.dirlisting.WAF_URL;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.protocols.NetProtocols;
import eu.essi_lab.lib.net.s3.S3TransferWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.netcdf.NetCDFAttribute;
import eu.essi_lab.netcdf.timeseries.H4SingleTimeSeriesWriter;
import eu.essi_lab.netcdf.timeseries.NetCDFVariable;
import eu.essi_lab.netcdf.timeseries.SimpleStation;
import ucar.ma2.DataType;

/**
 * @author Fabrizio
 */
public class ONAMETStationsAugmenter extends ResourceAugmenter<ONAMETStationsAugmenterSetting> {

    private static final String LIST_DATA_FOLDERS_ERROR = "LIST_DATA_FOLDERS_ERROR";
    private static final String LIST_DATA_FILES_ERROR = "LIST_DATA_FILES_ERROR";
    private static final String EXTRACTION_DIR_CREATION_ERROR = "EXTRACTION_DIR_CREATION_ERROR";
    private static final String NC_CREATION_ERROR = "NC_CREATION_ERROR";

    /**
     * 
     */
    private File ncFolder;
    private List<URL> dataURLs;
    private HashMap<String, String> cvsContentMap;

    @Override
    public Optional<GSResource> augment(GSResource resource) throws GSException {

	//
	// get the URLs of all data files, only once
	//
	if (dataURLs == null) {

	    cvsContentMap = new HashMap<>();

	    GSLoggerFactory.getLogger(getClass()).debug("List data folders STARTED");

	    String sourceEndpoint = resource.getSource().getEndpoint();

	    List<String> listDataFolders = listDataFolders(sourceEndpoint); // ordered list of data folders URL

	    //
	    // TO REMOVE
	    //

	    // listDataFolders = listDataFolders.stream().filter(f ->
	    // f.contains("2021")).collect(Collectors.toList()).subList(0, 5);

	    //
	    // TO REMOVE
	    //

	    GSLoggerFactory.getLogger(getClass()).debug("Found {} data folders", listDataFolders.size());

	    GSLoggerFactory.getLogger(getClass()).debug("List data folders ENDED");

	    String startDataFolder = listDataFolders.get(0);

	    GSLoggerFactory.getLogger(getClass()).debug("List data URLs STARTED");

	    dataURLs = listDataFiles(startDataFolder, listDataFolders);

	    GSLoggerFactory.getLogger(getClass()).debug("Found {} data URLs", dataURLs.size());

	    GSLoggerFactory.getLogger(getClass()).debug("List data URLs ENDED");

	    //
	    // creates the NC folder
	    //

	    Optional<String> ncPath = getSetting().getNCPath();

	    ncFolder = new File(IOStreamUtils.getUserTempDirectory() + File.separator + "onamet-stations-nc");

	    if (ncPath.isPresent()) {

		ncFolder = new File(ncPath.get());
	    }

	    if (!ncFolder.exists()) {

		GSLoggerFactory.getLogger(getClass()).debug("NC folder creation STARTED");

		boolean mkdirs = ncFolder.mkdirs();

		if (!mkdirs) {

		    GSLoggerFactory.getLogger(getClass()).error("Unable to create extraction dir {}", ncPath.get());

		    throw GSException.createException(//
			    getClass(), //
			    "Unable to create extraction dir" + ncPath.get(), //
			    null, //
			    ErrorInfo.ERRORTYPE_INTERNAL, //
			    ErrorInfo.SEVERITY_ERROR, //
			    EXTRACTION_DIR_CREATION_ERROR);
		}

		GSLoggerFactory.getLogger(getClass()).debug("NC folder creation ENDED");
	    }
	}

	//
	// 1) groups the URLs by station Id
	//
	HashMap<String, List<URL>> stationToURLMap = new HashMap<String, List<URL>>();

	for (URL url : dataURLs) {

	    // --------------------------------------day.ye
	    // https://onamet.gov.do/ema/2022/019/486019.22.cvs
	    // --------------------------year/day/sta

	    String stationId = findStationId(url);

	    List<URL> list = stationToURLMap.get(stationId);
	    if (list == null) {
		list = new ArrayList<>();
		stationToURLMap.put(stationId, list);
	    }

	    list.add(url);
	}

	//
	// 2) creates a NC file for each station/parameter and stores them in the nc folder
	//

	//
	// get all the URLs of the station related to this metadata
	// each station URL is related to a particular temporal period
	//
	OriginalMetadata originalMetadata = resource.getOriginalMetadata();

	String stationId = ONAMETStationsMapper.readStationId(originalMetadata);
	ONAMETParameterId parameter = ONAMETStationsMapper.readParameterId(originalMetadata);

	List<URL> stationURLs = stationToURLMap.get(stationId);

	if (stationURLs == null) { // it happens only if the URLs list is reduced for test purposes

	    return Optional.empty();
	}

	//
	// the list with all the values of all the availble temporal period
	// of the metadata parameter related to the metadata stationId
	// e.g: the values of parameter PE from the station 102 from 2019 to 2022
	//
	List<SimpleEntry<String, Double>> allParameterValues = new ArrayList<>();

	for (URL url : stationURLs) {

	    //
	    // get the values of the parameter related to this metadata
	    //

	    String fileName = url.toString().substring(url.toString().lastIndexOf("/") + 1, url.toString().length());

	    String csvFileContent = cvsContentMap.get(fileName);

	    if (csvFileContent == null) {

		Downloader downloader = new Downloader();
		Optional<String> csvFile = downloader.downloadOptionalString(url.toString());

		csvFileContent = csvFile.get();

		cvsContentMap.put(fileName, csvFileContent);
	    }

	    OMAMETStationCVSFile file = new OMAMETStationCVSFile(csvFileContent);

	    List<SimpleEntry<String, Double>> values = file.getParameterValues(parameter);

	    allParameterValues.addAll(values);
	}

	int origSize = allParameterValues.size();

	allParameterValues = allParameterValues.stream().distinct().collect(Collectors.toList());

	int distSize = allParameterValues.size();

	if (origSize > distSize) {

	    GSLoggerFactory.getLogger(getClass()).warn("Duplicate time/value pairs found");
	    GSLoggerFactory.getLogger(getClass()).warn("Original size: {}", origSize);
	    GSLoggerFactory.getLogger(getClass()).warn("Distinct size: {}", distSize);
	}

	//
	// creates the NC file using the parameter and its values
	//

	File ncFile = new File(ncFolder, stationId + "_" + parameter.name() + ".nc");

	try {
	    createNCFile(ncFile, resource, parameter, allParameterValues);
	} catch (IOException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    NC_CREATION_ERROR);
	}

	//
	// set the temporal extent
	//
	allParameterValues.sort((v1, v2) -> v1.getKey().compareTo(v2.getKey()));

	String minDateTime = allParameterValues.get(0).getKey();
	String maxDateTime = allParameterValues.get(allParameterValues.size() - 1).getKey();

	resource.getHarmonizedMetadata().getCoreMetadata().addTemporalExtent(minDateTime, maxDateTime);

	//
	// set the THREDDS HTTP NC link to the metadata
	//

	CoreMetadata coreMetadata = resource.getHarmonizedMetadata().getCoreMetadata();

	String threddsURL = getSetting().getTHREDDSUrl();

	String subFolder = getSetting().getTHREDDSDataSubFolder().map(f -> f + "/").orElse("");

	coreMetadata.addDistributionOnlineResource(//
		ncFile.getName(), //
		threddsURL + "fileServer/data/all/" + subFolder + ncFile.getName(), //
		NetProtocols.HTTP.getCommonURN(), //
		"download");

	coreMetadata.addDistributionOnlineResource(//
		ncFile.getName(), //
		"https://thredds-data.s3.amazonaws.com/onametStations/" + ncFile.getName(), //
		NetProtocols.HTTP.getCommonURN(), //
		"download");

	//
	// uploads to S3
	//

	if (getSetting().getS3BucketName().isPresent()) {

	    uploadToS3(ncFile, getSetting());
	}

	//
	//
	//

	EasyAccessAugmenter easyAccessAugmenter = new EasyAccessAugmenter();

	return easyAccessAugmenter.augment(resource);
    }

    /**
     * @param ncFile
     * @param resource
     * @param var
     * @param variableValues
     * @throws IOException
     */
    @SuppressWarnings("deprecation")
    private void createNCFile(//
	    File ncFile, //
	    GSResource resource, //
	    ONAMETParameterId var, //
	    List<SimpleEntry<String, Double>> variableValues) throws IOException {

	SimpleStation station = new SimpleStation();

	GeographicBoundingBox boundingBox = resource.getHarmonizedMetadata().getCoreMetadata().getBoundingBox();

	station.setLatitude(boundingBox.getSouth());
	station.setLongitude(boundingBox.getWest());

	VerticalExtent verticalExtent = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification()
		.getVerticalExtent();

	if (verticalExtent != null) {

	    Double minimumValue = verticalExtent.getMinimumValue();
	    station.setAltitude(minimumValue);
	}

	String title = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getMIPlatform().getCitation().getTitle();
	station.setName(title);

	station.setIdentifier(resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getMIPlatform().getMDIdentifierCode());

	//
	//
	//
	List<Long> timeValues = new ArrayList<>();

	NetCDFVariable<Long> timeVariable = new NetCDFVariable<Long>("time", timeValues, "milliseconds since 1970-01-01 00:00:00",
		DataType.LONG);

	variableValues.forEach(v -> timeValues.add(ISO8601DateTimeUtils.parseISO8601(v.getKey()).getTime()));

	//
	//
	//

	List<Double> acquisitions = new ArrayList<>();

	String units = resource.getExtensionHandler().getAttributeUnits().get();

	NetCDFVariable<Double> mainVariable = new NetCDFVariable<Double>(var.name(), acquisitions, units, DataType.DOUBLE);

	mainVariable.setLongName(var.getName());

	mainVariable.addAttribute(NetCDFAttribute.WML_UNIT_NAME.getNetCDFName(), units);
	mainVariable.addAttribute(NetCDFAttribute.WML_UNIT_ABBREVIATION.getNetCDFName(), units);

	mainVariable.setMissingValue(Double.NaN); // in any case we set missing value to NaN, it is more accurate

	variableValues.forEach(v -> acquisitions.add(v.getValue()));

	//
	//
	//

	H4SingleTimeSeriesWriter writer = new H4SingleTimeSeriesWriter(ncFile.getAbsolutePath());

	writer.write(station, timeVariable, mainVariable);
    }

    /**
     * @param url
     * @return
     */
    private String findStationId(URL url) {

	String cvs = url.toString().substring(url.toString().lastIndexOf("/") + 1, url.toString().length());

	String stationId = cvs.substring(0, cvs.indexOf(".") - 3);

	return stationId;
    }

    /**
     * @param filesPaths
     * @param setting
     */
    private void uploadToS3(File file, ONAMETStationsAugmenterSetting setting) {

	GSLoggerFactory.getLogger(getClass()).debug("Uploading nc file {} to the S3 bucket {} STARTED", file,
		setting.getS3BucketName().get());

	S3TransferWrapper manager = new S3TransferWrapper();
	manager.setAccessKey(setting.getS3AccessKey().get());
	manager.setSecretKey(setting.getS3SecretKey().get());

	manager.setACLPublicRead(true);

	String bucketName = setting.getS3BucketName().get();

	if (bucketName.contains("/")) {

	    String bucket = bucketName.substring(0, bucketName.indexOf("/"));
	    String folder = bucketName.substring(bucketName.indexOf("/") + 1, bucketName.length());

	    manager.uploadFile(file.getAbsolutePath(), bucket, folder + "/" + file.getName());

	} else {

	    manager.uploadFile(file.getAbsolutePath(), bucketName);
	}

	manager.uploadFile(file.getAbsolutePath(), setting.getS3BucketName().get());

	GSLoggerFactory.getLogger(getClass()).debug("Uploading nc file {} to the S3 bucket {} STARTED", file,
		setting.getS3BucketName().get());
    }

    /**
     * @param sourceUrl
     * @return
     * @throws GSException
     */
    private List<String> listDataFolders(String sourceUrl) throws GSException {

	String rootURL = sourceUrl.endsWith("/") ? sourceUrl + ONAMETStationsConnector.ROOT_PATH
		: sourceUrl + "/" + ONAMETStationsConnector.ROOT_PATH;

	try {

	    GSLoggerFactory.getLogger(getClass()).debug("Retrieving root data folders STARTED");

	    WAFClient client = new WAFClient(new URL(rootURL));

	    client.setUseAbsolutePathReference(true);

	    List<String> rootFolders = client.//
		    listFolders(u -> !u.toString().contains("metadatos")).//
		    stream().//
		    map(w -> w.getURL().toString()).//
		    collect(Collectors.toList()); //

	    GSLoggerFactory.getLogger(getClass()).debug("Found {} root data folders", rootFolders.size());

	    GSLoggerFactory.getLogger(getClass()).debug("Retrieving root data folders ENDED");

	    List<String> listFolders = new ArrayList<>();

	    for (String folder : rootFolders) {

		GSLoggerFactory.getLogger(getClass()).debug("Retrieving data folders of root folder {} STARTED", folder);

		List<String> list = WAFClient.listFolders(new WAF_URL(new URL(folder)), true).//
			stream().//
			map(w -> w.getURL().toString()).//
			collect(Collectors.toList());

		GSLoggerFactory.getLogger(getClass()).debug("Found {} data folders", list.size());

		GSLoggerFactory.getLogger(getClass()).debug("Retrieving data folders of root folder {} ENDED", folder);

		listFolders.addAll(list);
	    }

	    // they must be sorted!
	    listFolders.sort(String::compareTo);

	    return listFolders;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    LIST_DATA_FOLDERS_ERROR, //
		    e);
	}
    }

    /**
     * @param request
     * @param startDataFolder
     * @param listDataFolders
     * @return
     * @throws GSException
     */
    private List<URL> listDataFiles(String startDataFolder, List<String> listDataFolders) throws GSException {

	int index = listDataFolders.indexOf(startDataFolder);

	listDataFolders = listDataFolders.subList(index, listDataFolders.size());

	if (listDataFolders.isEmpty()) {

	    GSLoggerFactory.getLogger(getClass()).debug("No new data folder to proceess");

	    return new ArrayList<>();
	}

	GSLoggerFactory.getLogger(getClass()).debug("Number of data folders to process: {}", listDataFolders.size());

	try {

	    List<URL> listFiles = new ArrayList<URL>();

	    // String lastDataFolder = null;

	    for (String dataFolder : listDataFolders) {

		GSLoggerFactory.getLogger(getClass()).debug("Listing files of data folder {} STARTED", dataFolder);

		WAFClient client = new WAFClient(new URL(dataFolder));

		client.setUseAbsolutePathReference(true);

		List<URL> list = client.listFiles();

		GSLoggerFactory.getLogger(getClass()).debug("Found {} '.csv' files", list.size());

		listFiles.addAll(list);

		// lastDataFolder = dataFolder;

		GSLoggerFactory.getLogger(getClass()).debug("Listing files of data folder {} ENDED", dataFolder);
	    }

	    GSLoggerFactory.getLogger(getClass()).debug("Total data files to download: {} ENDED", listFiles.size());

	    //
	    //
	    //

	    // if (request.getHarvestingProperties() != null) {
	    //
	    // GSLoggerFactory.getLogger(getClass()).debug("Updating properties with last processed folder: {}",
	    // lastDataFolder);
	    //
	    // request.getHarvestingProperties().put(LAST_PROCESSED_FOLDER_PROPERTY_KEY, lastDataFolder);
	    // }

	    //
	    //
	    //

	    return listFiles;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    LIST_DATA_FILES_ERROR, //
		    e);
	}
    }

    /**
     * @param request
     * @param listDataFolders
     * @return
     */
    // private String computeStartDataFolder(ListRecordsRequest request, List<String> listDataFolders) {
    //
    // String startDataFolder = null;
    //
    // HarvestingProperties properties = request.getHarvestingProperties();
    //
    // if (properties != null) {
    //
    // String lastProcessedFolder = properties.getProperty(LAST_PROCESSED_FOLDER_PROPERTY_KEY);
    // if (lastProcessedFolder != null) {
    //
    // try {
    // startDataFolder = URLDecoder.decode(lastProcessedFolder, "UTF-8");
    // } catch (UnsupportedEncodingException e) {
    // }
    //
    // GSLoggerFactory.getLogger(getClass()).debug("Start data folder retrieved from properties: {}", startDataFolder);
    // }
    // }
    //
    // if (startDataFolder == null) {
    //
    // startDataFolder = listDataFolders.get(0);
    //
    // GSLoggerFactory.getLogger(getClass()).debug("Using default start data folder: {}", startDataFolder);
    // }
    //
    // return startDataFolder;
    // }

    @Override
    public String getType() {

	return "ONAMETStationsAugmenter";
    }

    @Override
    protected ONAMETStationsAugmenterSetting initSetting() {

	return new ONAMETStationsAugmenterSetting();
    }

    @Override
    protected String initName() {

	return "ONAMET Stations Augmenter";
    }
}
