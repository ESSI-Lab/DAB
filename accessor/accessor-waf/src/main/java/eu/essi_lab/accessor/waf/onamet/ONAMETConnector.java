package eu.essi_lab.accessor.waf.onamet;

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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.lib.net.dirlisting.WAFClient;
import eu.essi_lab.lib.net.dirlisting.WAF_URL;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.s3.S3TransferWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.utils.TarExtractor;
import eu.essi_lab.messages.HarvestingProperties;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.netcdf.NetCDFCRSConverter;

/**
 * @author Fabrizio
 */
public class ONAMETConnector extends HarvestedQueryConnector<ONAMETConnectorSetting> {

    /**
     * 
     */
    private static DecimalFormat decimalFormat;

    static {

	decimalFormat = new DecimalFormat();
	decimalFormat.setGroupingSize(3);
	decimalFormat.setGroupingUsed(true);

	DecimalFormatSymbols symbols = new DecimalFormatSymbols();
	symbols.setGroupingSeparator('.');
	symbols.setDecimalSeparator(',');

	decimalFormat.setDecimalFormatSymbols(symbols);
    }

    /**
     * 
     */
    static final String TYPE = "ONAMETConnector";

    /**
     * 
     */
    private static final String NC_LIST_FILES_ERROR = "NC_LIST_FILES_ERROR";
    private static final String LIST_FOLDERS_ERROR = "LIST_FOLDERS_ERROR";
    private static final String MULTIPLE_TAR_GZ_FOUND_ERROR = "MULTIPLE_TAR_GZ_FOUND_ERROR";
    private static final String ORIGINAL_MD_FROM_DIR_LISTING_CREATION_ERROR = "ORIGINAL_MD_FROM_DIR_LISTING_CREATION_ERROR";
    private static final String EXTRACTION_DIR_CREATION_ERROR = "EXTRACTION_DIR_CREATION_ERROR";
    private static final String EXTRACTION_TIMEOUT_ERROR = "EXTRACTION_TIMEOUT_ERROR";
    private static final String UNABLE_TO_DOWNLOAD_TAR_GIZ_ERROR = "UNABLE_TO_DOWNLOAD_TAR_GIZ_ERROR";
    private static final String ORIGINAL_METADATA_CREATION_ERROR = "ORIGINAL_METADATA_CREATION_ERROR";

    /**
     * To use for test purpose, file MUST be closed in prod.env.
     */
    public static boolean closeTarGz = true;

    /**
     * 
     */
    private List<String> foldersURLList;

    /**
     * 
     */
    private int recordsCount;

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> response = new ListRecordsResponse<>();

	//
	// [1/6] lists all the folders of the source URL
	//
	GSLoggerFactory.getLogger(getClass()).debug("[1/6] Listing d0x/ folders STARTED");

	if (foldersURLList == null) {
	    foldersURLList = getFoldersURLList(getSourceURL());
	}

	GSLoggerFactory.getLogger(getClass()).debug("[1/6] Listing d0x/ folders ENDED");

	//
	// [2/6] get the URL of the directory to process
	//
	GSLoggerFactory.getLogger(getClass()).debug("[2/6] Getting current WAF URL STARTED");

	Optional<String> currentWAF_URL = getCurrentWAF_URL(request, foldersURLList);

	if (!currentWAF_URL.isPresent()) {

	    return response;
	}

	int dirURLIndex = foldersURLList.indexOf(currentWAF_URL.get());

	GSLoggerFactory.getLogger(getClass()).debug("[2/6] Getting current WAF URL ENDED");

	GSLoggerFactory.getLogger(getClass()).debug("Processing of directory [{}/{}] STARTED", (dirURLIndex + 1), foldersURLList.size());

	//
	// [3/6] get the URL of the tar.gz files of the current folder
	//
	GSLoggerFactory.getLogger(getClass()).debug("[3/6] Getting tar.gz URL STARTED");

	Optional<URL> tarGZ_URL = getTarGZ_URL(currentWAF_URL.get());

	GSLoggerFactory.getLogger(getClass()).debug("[3/6] Getting tar.gz URL ENDED");

	//
	// [4/6] creates the original metadata from the NetCDF files extracted or downloaded
	//

	GSLoggerFactory.getLogger(getClass()).debug("[4/6] Original metadata creation STARTED");

	String metadata = null;
	try {
	    metadata = createOriginalMetadata(tarGZ_URL, currentWAF_URL.get());

	}

	catch (TimeoutException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage());

	    throw GSException.createException(//
		    ONAMETConnector.class, //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    EXTRACTION_TIMEOUT_ERROR, //
		    e);

	} catch (Exception ex) {

	    if (ex instanceof GSException) {

		throw (GSException) ex;

	    } else {

		throw GSException.createException(//
			ONAMETConnector.class, //
			ex.getMessage(), //
			null, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_ERROR, //
			ORIGINAL_METADATA_CREATION_ERROR, //
			ex);

	    }
	}

	Optional<Integer> maxRecords = getSetting().getMaxRecords();

	boolean maxRecordsReached = false;

	OriginalMetadata original = new OriginalMetadata();
	original.setSchemeURI(ONAMETMapper.ONAMET_METADATA_SCHEMA);
	original.setMetadata(metadata);

	response.addRecord(original);

	recordsCount++;

	if (maxRecords.isPresent() && recordsCount == maxRecords.get()) {

	    GSLoggerFactory.getLogger(getClass()).debug("Reached {} max records, exit", maxRecords.get());

	    GSLoggerFactory.getLogger(getClass()).debug("Processing of directory [{}/{}] ENDED", (dirURLIndex + 1), foldersURLList.size());

	    maxRecordsReached = true;
	}

	GSLoggerFactory.getLogger(getClass()).debug("[4/6] Original metadata creation ENDED");

	//
	// [5/6] get the next directory URL (if available)
	//
	GSLoggerFactory.getLogger(getClass()).debug("[5/6] Get next directory URL STARTED");

	if (!maxRecordsReached) {

	    Optional<String> nextDirectoryURL = getNextDirectoryURL(dirURLIndex, foldersURLList);

	    if (nextDirectoryURL.isPresent()) {

		response.setResumptionToken(nextDirectoryURL.get());
	    }
	} else {

	    GSLoggerFactory.getLogger(getClass()).debug("Reached {} max records, no resumption token to set", maxRecords.get());
	}

	GSLoggerFactory.getLogger(getClass()).debug("[5/6] Get next directory URL ENDED");

	//
	// [6/6] storing to s3
	//
	GSLoggerFactory.getLogger(getClass()).debug("[6/6] Uploading {} nc file to the S3 bucket {} STARTED",
		getSetting().getS3BucketName().get());

	if (getSetting().getS3BucketName().isPresent()) {

	    File file = new File(ONAMETMapper.readNcFilePath(metadata));

	    uploadToS3(file, getSetting());

	} else {

	    GSLoggerFactory.getLogger(getClass()).debug("S3 bucket name not provided");
	}

	GSLoggerFactory.getLogger(getClass()).debug("[6/6] Uploading {} nc file to the S3 bucket {} ENDED",
		getSetting().getS3BucketName().get());

	GSLoggerFactory.getLogger(getClass()).debug("Processing of directory [{}/{}] ENDED", (dirURLIndex + 1), foldersURLList.size());

	return response;
    }

    /**
     * @return
     * @throws GSException
     */
    static List<String> getFoldersURLList(String sourceURL) throws GSException {

	List<String> foldersURLList = new ArrayList<>();
	try {

	    WAFClient client = new WAFClient(new URL(sourceURL));

	    GSLoggerFactory.getLogger(ONAMETConnector.class).debug("Listing root folders STARTED");

	    List<WAF_URL> rootFolders = client.listFolders();

	    GSLoggerFactory.getLogger(ONAMETConnector.class).debug("Root folders found: {}", rootFolders.size());

	    GSLoggerFactory.getLogger(ONAMETConnector.class).debug("Listing root folders ENDED");

	    GSLoggerFactory.getLogger(ONAMETConnector.class).debug("Creating d0x folders STARTED");

	    for (WAF_URL rootDirUrl : rootFolders) {

		foldersURLList.addAll(//

			WAFClient
				.listFolders(//
					rootDirUrl,
					url -> url.toString().endsWith("d01/") || //
						url.toString().endsWith("d02/") || //
						url.toString().endsWith("d03/"))
				.//
				stream().//
				map(d -> d.getURL().toString()).//
				collect(Collectors.toList()));
	    }

	    //
	    // this is the case when the source URL is not http://186.149.199.244/ftp/ but it refers directly
	    // to a root directory like http://186.149.199.244/ftp/2021112612/
	    //
	    if (foldersURLList.isEmpty()) {

		foldersURLList = rootFolders.stream().map(u -> u.getURL().toString()).collect(Collectors.toList());
	    }

	    foldersURLList = foldersURLList.//
		    stream().//
		    //
		    // folders URL can be lexicographically compared. this is very useful beacuse it allows
		    // to determinate the new folders
		    //
		    sorted((d1, d2) -> d1.compareTo(d2)).//
		    collect(Collectors.toList());

	    GSLoggerFactory.getLogger(ONAMETConnector.class).debug("Creating d0x folders ENDED");

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(ONAMETConnector.class).error(e.getMessage(), e);

	    throw GSException.createException(//
		    ONAMETConnector.class, //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    LIST_FOLDERS_ERROR, //
		    e);
	}

	return foldersURLList;
    }

    /**
     * @param request
     * @param foldersURLList
     * @return
     */
    private Optional<String> getCurrentWAF_URL(ListRecordsRequest request, List<String> foldersURLList) {

	String currentDirectoryURL = null;

	Optional<String> startFolderUrl = getSetting().getStartFolderUrl();
	if (startFolderUrl.isPresent() && request.isFirst()) {

	    GSLoggerFactory.getLogger(getClass()).debug("Current directory URL is manually configured for the first harvesting request");

	    currentDirectoryURL = startFolderUrl.get();

	} else {

	    //
	    // the resumption token is the encoded URL of the d01/, d02/, d03/ directory to processes
	    //
	    currentDirectoryURL = request.getResumptionToken();

	    if (currentDirectoryURL == null) {

		HarvestingProperties harvestingProperties = request.getHarvestingProperties();
		if (harvestingProperties != null) {

		    //
		    // in the recovery token there is the URL of the last processed directory,
		    // so we must start from the next directory if available
		    //
		    String recoveryResumptionToken = harvestingProperties.getRecoveryResumptionToken();

		    if (recoveryResumptionToken != null) {

			recoveryResumptionToken = decodeURL(recoveryResumptionToken);

			GSLoggerFactory.getLogger(getClass()).debug("Recovery token: {}", recoveryResumptionToken);

			//
			// it is possible that the last processed WAF in the previous harvesting
			// has been removed
			//
			if (!foldersURLList.contains(recoveryResumptionToken)) {

			    GSLoggerFactory.getLogger(getClass()).debug("Recovery token refers to a missing WAF, adding to the list");

			    foldersURLList.add(recoveryResumptionToken);

			    foldersURLList.sort(String::compareTo);
			}

			int index = foldersURLList.indexOf(recoveryResumptionToken);
			if (index == foldersURLList.size() - 1) {

			    GSLoggerFactory.getLogger(getClass()).debug("No new directory to process, exit");
			    return Optional.empty();
			}

			GSLoggerFactory.getLogger(getClass()).debug("Current directory URL computed from recovery resumption token");

			currentDirectoryURL = foldersURLList.get(index + 1);
		    }
		}
	    } else {

		GSLoggerFactory.getLogger(getClass()).debug("Current directory URL taken from resumption token");
	    }

	    if (currentDirectoryURL == null) {

		// per default the first directory URL is used
		currentDirectoryURL = foldersURLList.get(0);

		GSLoggerFactory.getLogger(getClass()).debug("Using default current directory URL from list at index 0");
	    }
	}

	currentDirectoryURL = decodeURL(currentDirectoryURL);

	GSLoggerFactory.getLogger(getClass()).debug("Current directory URL: " + currentDirectoryURL);

	return Optional.of(currentDirectoryURL);
    }

    /**
     * @param url
     * @return
     */
    private String decodeURL(String url) {

	try {
	    return URLDecoder.decode(url, "UTF-8");
	} catch (UnsupportedEncodingException e) {
	}
	// no way
	return null;
    }

    /**
     * @param currentDirectoryURL
     * @return
     * @throws GSException
     */
    private Optional<URL> getTarGZ_URL(String currentDirectoryURL) throws GSException {

	try {

	    GSLoggerFactory.getLogger(getClass()).debug("Listing tar.gz files of directory {} STARTED", currentDirectoryURL);

	    URL dirURL = new URL(currentDirectoryURL);

	    List<URL> tarGZURLList = WAFClient.listFiles(//
		    new WAF_URL(dirURL), //
		    url -> url.toString().endsWith("tar.gz"));

	    GSLoggerFactory.getLogger(getClass()).debug("tar.gz files found: {}", tarGZURLList.size());

	    if (tarGZURLList.isEmpty()) {

		return Optional.empty();
	    }

	    if (tarGZURLList.size() > 1) {

		// it should be exactly one
		GSLoggerFactory.getLogger(getClass()).error("More than one tar.gz files found!");

		throw GSException.createException(//
			getClass(), //
			"More than one tar.gz files found!", //
			null, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_ERROR, //
			MULTIPLE_TAR_GZ_FOUND_ERROR //
		);
	    }

	    GSLoggerFactory.getLogger(getClass()).debug("tar.gz URL: {}", tarGZURLList.get(0));

	    GSLoggerFactory.getLogger(getClass()).debug("Listing tar.gz files of directory {} ENDED", currentDirectoryURL);

	    return Optional.of(tarGZURLList.get(0));

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    NC_LIST_FILES_ERROR, //
		    e);
	}
    }

    /**
     * If <code>targGzURL</code> is present, extracts in the extraction direcory the compressed NetCDF files and
     * for each file, creates an original metadata using
     * {@link ONAMETMapper#createOriginalMetadata(String, String, String)}.<br>
     * If <code>targGzURL</code> is not present, we assume that nc files are directly stored in the given
     * <code>currentDirectoryURL</code>
     * so the original metadata are created from the listed nc files
     * 
     * @param targGzURL
     * @param currentDirectoryURL
     * @return
     * @throws Exception
     * @throws Exception
     */
    private String createOriginalMetadata(Optional<URL> targGzURL, String currentDirectoryURL) throws Exception {

	Optional<String> extractionTarget = getSetting().getExtractionPath();

	File extractionDir = new File(IOStreamUtils.getUserTempDirectory() + File.separator + "onamet-nc");

	if (extractionTarget.isPresent()) {

	    extractionDir = new File(extractionTarget.get());
	}

	if (!extractionDir.exists()) {

	    boolean mkdirs = extractionDir.mkdirs();

	    if (!mkdirs) {

		GSLoggerFactory.getLogger(getClass()).error("Unable to create extraction dir {}", extractionTarget.get());

		throw GSException.createException(//
			getClass(), //
			"Unable to create extraction dir" + extractionTarget.get(), //
			null, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_ERROR, //
			EXTRACTION_DIR_CREATION_ERROR);
	    }
	}

	GSLoggerFactory.getLogger(getClass()).debug("Extraction dir: {}", extractionDir);

	int extractionDirSize = extractionDir.listFiles().length;

	GSLoggerFactory.getLogger(getClass()).debug("Extraction dir contains {} files", extractionDirSize);

	if (extractionDirSize > 0) {

	    GSLoggerFactory.getLogger(getClass()).warn("Extraction dir not emtpy, size is {}", extractionDirSize);
	    Arrays.asList(extractionDir.listFiles()).forEach(f -> {

		GSLoggerFactory.getLogger(getClass()).warn("Deleting file {} STARTED", f.getAbsolutePath());
		boolean deleted = f.delete();
		if (!deleted) {
		    GSLoggerFactory.getLogger(getClass()).warn("Unable to delete file {}", f.getAbsolutePath());
		}
		GSLoggerFactory.getLogger(getClass()).warn("Deleting file {} ENDED", f.getAbsolutePath());
	    });
	}

	GSLoggerFactory.getLogger(getClass()).debug("Total MB: " + decimalFormat.format(extractionDir.getTotalSpace() / (1024 * 1024)));
	GSLoggerFactory.getLogger(getClass()).debug("Usable MB: " + decimalFormat.format(extractionDir.getUsableSpace() / (1024 * 1024)));
	GSLoggerFactory.getLogger(getClass()).debug("Free MB: " + decimalFormat.format(extractionDir.getFreeSpace() / (1024 * 1024)));

	Integer maxEntries = getSetting().getMaxProcessedEntries().orElse(-1);

	String currentWAFPath = getDirectoryPath(currentDirectoryURL);

	if (targGzURL.isPresent()) {

	    extractFromTarGZ(currentWAFPath, targGzURL, extractionDir, maxEntries);

	} else {

	    downloadFromWAF(currentDirectoryURL, currentWAFPath, extractionDir, maxEntries);
	}

	GSLoggerFactory.getLogger(getClass()).debug("Original metadata creation STARTED");

	//
	// merging
	//

	File mergedOutput = new File(extractionDir, currentWAFPath + ".merged.nc");

	GSLoggerFactory.getLogger(getClass()).debug("Merging files STARTED");

	mergeFiles(extractionDir, mergedOutput);

	GSLoggerFactory.getLogger(getClass()).debug("Merged file: {}", mergedOutput.getAbsolutePath());

	GSLoggerFactory.getLogger(getClass()).debug("Merging files ENDED");

	//
	// normalization
	//

	File normalizedOutput = new File(extractionDir, currentWAFPath + ".nc");

	GSLoggerFactory.getLogger(getClass()).debug("Normalization files STARTED");

	ONAMETNormalizer.normalize(mergedOutput, normalizedOutput, true);

	GSLoggerFactory.getLogger(getClass()).debug("Normalized file: {}", normalizedOutput.getAbsolutePath());

	GSLoggerFactory.getLogger(getClass()).debug("Normalization files ENDED");

	//
	//
	//

	String metadata = ONAMETMapper.createOriginalMetadata(//
		normalizedOutput.getAbsolutePath(), //
		getSetting().getTHREDDSUrl(), //
		getSetting().getTHREDDSDataSubFolder().orElse(""), //
		currentWAFPath, //
		extractionDir.getAbsolutePath());

	GSLoggerFactory.getLogger(getClass()).debug("Original metadata creation ENDED");

	return metadata;
    }

    /**
     * @param currentDirectoryURL
     * @param extractionDir
     * @param maxEntries
     * @return
     * @throws GSException
     */
    private void downloadFromWAF(//
	    String currentDirectoryURL, //
	    String currentWAFPath, //
	    File extractionDir, //
	    int maxEntries) throws GSException {

	GSLoggerFactory.getLogger(getClass()).debug("No tar.gz found, nc files are included in the directory");

	try {

	    GSLoggerFactory.getLogger(getClass()).debug("Listing of nc files in the directory {} STARTED", currentDirectoryURL);

	    WAFClient client = new WAFClient(new URL(currentDirectoryURL));

	    List<URL> listURLs = client.listFiles(fileUrl -> fileUrl.toString().contains("wrfout") && fileUrl.toString().endsWith("00"));

	    GSLoggerFactory.getLogger(getClass()).debug("Found {} nc files", listURLs.size());

	    GSLoggerFactory.getLogger(getClass()).debug("Listing of nc files in the directory {} ENDED", currentDirectoryURL);

	    for (int i = 0; i < listURLs.size(); i++) {

		GSLoggerFactory.getLogger(getClass()).debug("Downloading nc file [{}/{}] STARTED", (i + 1), listURLs.size());

		URL fileUrl = listURLs.get(i);

		GSLoggerFactory.getLogger(getClass()).debug("Downloading stream of nc file {} STARTED", fileUrl.toString());

		Downloader downloader = new Downloader();

		Optional<InputStream> stream = downloader.downloadOptionalStream(fileUrl.toExternalForm());

		GSLoggerFactory.getLogger(getClass()).debug("Downloading stream of nc file {} ENDED", fileUrl.toString());

		if (!stream.isPresent()) {

		    GSLoggerFactory.getLogger(getClass()).error("Unable to download nc file  at {}", fileUrl.toString());
		    continue;
		}

		String fileName = fileUrl.toString().substring(fileUrl.toString().lastIndexOf("/") + 1, fileUrl.toString().length());

		fileName = createFileName(currentWAFPath, fileName);

		File file = new File(extractionDir + File.separator + fileName);

		GSLoggerFactory.getLogger(getClass()).debug("Copy of nc file to {} STARTED", file.toString());

		FileUtils.copyToFile(stream.get(), file);

		GSLoggerFactory.getLogger(getClass()).debug("Copy of nc file to {} ENDED", file.toString());

		GSLoggerFactory.getLogger(getClass()).debug("Downloading nc file [{}/{}] ENDED", (i + 1), listURLs.size());

		if (maxEntries > 0 && (i + 1) == maxEntries) {

		    GSLoggerFactory.getLogger(getClass()).debug("Maximum number of {} nc files to process reached, exit", maxEntries);
		    break;
		}
	    }

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    ORIGINAL_MD_FROM_DIR_LISTING_CREATION_ERROR, //
		    e);
	}
    }

    /**
     * @param targGzURL
     * @param extractionDir
     * @param currentDirectoryURL
     * @param maxEntries
     * @return
     * @throws TimeoutException
     * @throws GSException
     */
    private void extractFromTarGZ(//
	    String currentWAFPath, //
	    Optional<URL> targGzURL, //
	    File extractionDir, //
	    int maxEntries) throws TimeoutException, GSException {

	GSLoggerFactory.getLogger(getClass()).debug("Downloading of tar.gz at {} STARTED", targGzURL.get().toExternalForm());

	Downloader downloader = new Downloader();

	Optional<InputStream> stream = downloader.downloadOptionalStream(targGzURL.get().toExternalForm());

	GSLoggerFactory.getLogger(getClass()).debug("Downloading of tar.gz at {} ENDED", targGzURL.get().toExternalForm());

	if (!stream.isPresent()) {

	    GSLoggerFactory.getLogger(getClass()).error("Unable to download targ.gz at {}", targGzURL.get().toExternalForm());

	    throw GSException.createException(//
		    getClass(), //
		    "Unable to download targ.gz at {}" + targGzURL.get().toExternalForm(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    UNABLE_TO_DOWNLOAD_TAR_GIZ_ERROR //
	    );
	}

	//
	//
	//

	GSLoggerFactory.getLogger(getClass()).debug("Extraction of tar.gz at {} STARTED", targGzURL.get().toExternalForm());

	TarExtractor tarExtractor = new TarExtractor(maxEntries, entryName -> createFileName(currentWAFPath, entryName));

	List<File> extractedFiles = null;
	try {

	    Optional<Integer> extractionTimeout = getSetting().getExtractionTimeout();

	    if (extractionTimeout.isPresent()) {

		GSLoggerFactory.getLogger(getClass()).debug("Extraction timeout: {} minutes", extractionTimeout.get());

		tarExtractor.setTimeOut(TimeUnit.MINUTES, extractionTimeout.get());

	    } else {

		GSLoggerFactory.getLogger(getClass()).debug("No extraction timeout set");
	    }

	    extractedFiles = tarExtractor.extract(stream.get(), extractionDir, closeTarGz);

	} catch (TimeoutException e) {

	    throw e;
	}

	GSLoggerFactory.getLogger(getClass()).debug("Extracted entries: {} ", extractedFiles.size());

	GSLoggerFactory.getLogger(getClass()).debug("Extraction of tar.gz at {} ENDED", targGzURL.get().toExternalForm());
    }

    /**
     * @param path
     * @param fileName
     * @return
     */
    private String createFileName(String path, String fileName) {

	return path + fileName.replace(":", "_") + ".nc";
    }

    /**
     * @param currentDirectoryURL
     * @return
     */
    private String getDirectoryPath(String currentDirectoryURL) {

	try {
	    String path = new URL(currentDirectoryURL).getPath();
	    path = path.replace("/ftp/", "");
	    path = path.replace("/", "_");

	    return path;
	} catch (MalformedURLException e) {
	}

	return String.valueOf(System.currentTimeMillis());
    }

    /**
     * @param dirURLIndex
     * @param foldersURLList
     * @return
     */
    private Optional<String> getNextDirectoryURL(int dirURLIndex, List<String> foldersURLList) {

	if (dirURLIndex < foldersURLList.size() - 1) {

	    String nextDirectory = foldersURLList.get(++dirURLIndex);

	    GSLoggerFactory.getLogger(getClass()).debug("Next directory to process: {}", nextDirectory);

	    try {
		nextDirectory = URLEncoder.encode(nextDirectory, "UTF-8");
	    } catch (UnsupportedEncodingException e) {
	    }

	    return Optional.of(nextDirectory);

	} else {

	    GSLoggerFactory.getLogger(getClass()).debug("No more folders to process");
	}

	return Optional.empty();
    }

    /**
     * @param file
     * @param setting
     */
    private void uploadToS3(File file, ONAMETConnectorSetting setting) {

	S3TransferWrapper manager = new S3TransferWrapper();
	manager.setAccessKey(setting.getS3AccessKey().get());
	manager.setSecretKey(setting.getS3SecretKey().get());

	manager.uploadFile(file.getAbsolutePath(), setting.getS3BucketName().get());
    }

    /**
     * @param extractionDir
     * @param mergedOutput
     * @throws IOException
     * @throws InterruptedException
     */
    private void mergeFiles(File extractionDir, File mergedOutput) throws IOException, InterruptedException {

	List<File> files = Arrays.asList(extractionDir.listFiles());

	GSLoggerFactory.getLogger(getClass()).debug("Files to merge: {}", files.size());

	files.forEach(f -> GSLoggerFactory.getLogger(getClass()).debug(f.getAbsolutePath()));

	// -W Disable Warnings, required in order to execute the command through Java process
	executeWithRuntime("cdo -W mergetime *.nc " + mergedOutput.getName(), extractionDir);

	Arrays.asList(extractionDir.listFiles()).forEach(f -> {

	    if (!f.getName().equals(mergedOutput.getName())) {

		f.delete();
	    }
	});
    }

    /**
     * @param command
     * @param workingDir
     * @throws IOException
     * @throws InterruptedException
     */
    private void executeWithRuntime(String command, File workingDir) throws IOException, InterruptedException {

	Runtime rt = Runtime.getRuntime();

	Process ps = rt.exec(command, null, workingDir);

	int exitVal = ps.waitFor();

	if (exitVal > 0) {

	    GSLoggerFactory.getLogger(NetCDFCRSConverter.class).error(IOStreamUtils.asUTF8String(ps.getErrorStream()));
	}
    }

    @Override
    public boolean supportsIncrementalHarvesting() throws GSException {

	return true;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint().contains("186.149.199.244/ftp");
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return Arrays.asList(ONAMETMapper.ONAMET_METADATA_SCHEMA);
    }

    @Override
    protected ONAMETConnectorSetting initSetting() {

	return new ONAMETConnectorSetting();
    }
}
