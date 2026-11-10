/**
 * 
 */
package eu.essi_lab.accessor.waf.trigger;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.dirlisting.WAFClient;
import eu.essi_lab.lib.net.dirlisting.WAF_URL;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.InterpolationType;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.AbstractResourceMapper;
import net.opengis.gml.v_3_2_0.TimeIndeterminateValueType;

/**
 * @author roncella
 */
public class TRIGGERWafConnector extends HarvestedQueryConnector<TRIGGERWafConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "TRIGGERWafConnector";
    public static final String WAF_TRIGGER_PROTOCOL = "WAF_TRIGGER_PROTOCOL";
    private static final String WAF_LISTING_ERROR = "WAF_LISTING_ERROR";

    private static final int STEP = 10;

    private List<URL> allFiles;
    private List<URL> runFiles;
    private static final Pattern RUN_IN_FILE_NAME = Pattern.compile("_(\\d{10})\\.json$");
    private static final Pattern CAMS_FILE_NAME = Pattern.compile("^(cams_eu_[a-zA-Z0-9._]+_web)_([A-Za-z]+)_\\d+\\.json$");
    private static final String BUCKET_NAME = "trigger-json-files";

    private Downloader downloader;

    public enum TRIGGER_INTERPOLATIONS {

	PERCENTILE_10("10", "10th Percentile", InterpolationType.STATISTICAL), PERCENTILE_25("25", "25th Percentile",
		InterpolationType.STATISTICAL), PERCENTILE_50("50", "50th Percentile", InterpolationType.STATISTICAL), PERCENTILE_75("75",
			"75th Percentile", InterpolationType.STATISTICAL), PERCENTILE_90("90", "90th Percentile",
				InterpolationType.STATISTICAL), MIN("min", "Minimum", InterpolationType.MIN), MAX("max", "Maximum",
					InterpolationType.MAX), STDEV("stdev", "Standard deviation", InterpolationType.STATISTICAL);

	private String id;
	private String label;
	private InterpolationType type;

	public String getLabel() {
	    return label;
	}

	public String getId() {
	    return id;
	}

	public InterpolationType getInterpolationType() {
	    return type;
	}

	private TRIGGER_INTERPOLATIONS(String id, String label, InterpolationType type) {
	    this.id = id;
	    this.label = label;
	    this.type = type;
	}

	public static TRIGGER_INTERPOLATIONS decode(String parameterCode) {
	    for (TRIGGER_INTERPOLATIONS var : values()) {
		if (parameterCode.equals(var.name())) {
		    return var;
		}
	    }
	    return null;

	}
    }

    public enum CAMS_TRIGGER_INTERPOLATIONS {

	PERCENTILE_0("perc_0", "0 Percentile", InterpolationType.STATISTICAL), PERCENTILE_10("perc_10", "10th Percentile",
		InterpolationType.STATISTICAL), PERCENTILE_25("perc_25", "25th Percentile", InterpolationType.STATISTICAL), PERCENTILE_50(
			"perc_50", "50th Percentile", InterpolationType.STATISTICAL), PERCENTILE_75("perc_75", "75th Percentile",
				InterpolationType.STATISTICAL), PERCENTILE_90("perc_90", "90th Percentile",
					InterpolationType.STATISTICAL), PERCENTILE_100("perc_100", "100th Percentile",
						InterpolationType.STATISTICAL);

	private String id;
	private String label;
	private InterpolationType type;

	public String getLabel() {
	    return label;
	}

	public String getId() {
	    return id;
	}

	public InterpolationType getInterpolationType() {
	    return type;
	}

	private CAMS_TRIGGER_INTERPOLATIONS(String id, String label, InterpolationType type) {
	    this.id = id;
	    this.label = label;
	    this.type = type;
	}

	public static CAMS_TRIGGER_INTERPOLATIONS decode(String parameterCode) {
	    for (CAMS_TRIGGER_INTERPOLATIONS var : values()) {
		if (parameterCode.equals(var.name())) {
		    return var;
		}
	    }
	    return null;

	}
    }

    private String username;
    private String password;

    public enum Resolution {
	HOURLY
    }

    public TRIGGERWafConnector() {

    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> response = new ListRecordsResponse<OriginalMetadata>();

	String token = request.getResumptionToken();
	int start = 0;
	if (token != null) {

	    start = Integer.valueOf(token);
	}

	Optional<Integer> mr = getSetting().getMaxRecords();
	if (username == null) {
	    username = ConfigurationWrapper.getCredentialsSetting().getTriggerWAFUser().orElse(null);
	}
	if (password == null) {
	    password = ConfigurationWrapper.getCredentialsSetting().getTriggerWAFPassword().orElse(null);
	}

	try {

	    if (allFiles == null) {

		GSLoggerFactory.getLogger(getClass()).debug("Download URLs from ECMWF WAF STARTED");
		URL listURL = new URL(getSourceURL());
		allFiles = WAFClient.listFiles(new WAF_URL(listURL), true, username, password);
		removeTiffFiles();

		if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent() && mr.get() < allFiles.size()) {

		    GSLoggerFactory.getLogger(getClass()).debug("List reduced to {} elements", mr.get());

		    allFiles = allFiles.subList(0, mr.get());

		}

		GSLoggerFactory.getLogger(getClass()).debug("Download URLs from ECMWF WAF ENDED");

		runFiles = null;
	    }

	    if (runFiles == null) {
		runFiles = filterURLsByLatestRun(allFiles);
	    }
	    List<URL> filteredURLs = runFiles;

	    if (start > filteredURLs.size()) {
		// e.g. resumption token saved by a previous harvest on a different file list
		GSLoggerFactory.getLogger(getClass()).warn("Resumption token {} beyond the {} files of the run, restarting from 0", start,
			filteredURLs.size());
		start = 0;
	    }

	    int end = start + STEP;
	    if (end > filteredURLs.size()) {
		end = filteredURLs.size();
	    } else {
		response.setResumptionToken(String.valueOf(end));
	    }

	    List<URL> subList = filteredURLs.subList(start, end);
	    GSLoggerFactory.getLogger(getClass()).debug("Downloading files [{}-{}/{}] STARTED", start, end, filteredURLs.size());

	    for (URL url : subList) {

		String fileURL = url.toExternalForm();
		String fileName = getFileName(url);
		TRIGGERWAFVariable variable = getVariable(fileName);
		if (variable == null) {
		    GSLoggerFactory.getLogger(getClass()).debug("Skipping {}: variable not supported", fileName);
		    continue;
		}

		String city = null;
		boolean isCams = isCamsFile(fileName);
		Map<String, String> interpolationMap = new HashMap<String, String>();

		if (!isCams) {
		    TRIGGER_INTERPOLATIONS[] interpolations = TRIGGER_INTERPOLATIONS.values();
		    for (TRIGGER_INTERPOLATIONS interpolation : interpolations) {
			String id = interpolation.getId();
			String label = interpolation.getLabel();
			interpolationMap.put(id, label);
		    }

		} else {
		    // other variables: e.g.cams_eu_ragweed_pollen_web
		    Matcher matcher = CAMS_FILE_NAME.matcher(fileName);
		    if (matcher.find()) {
			city = matcher.group(2);
		    }

		    CAMS_TRIGGER_INTERPOLATIONS[] interpolations = CAMS_TRIGGER_INTERPOLATIONS.values();
		    for (CAMS_TRIGGER_INTERPOLATIONS interpolation : interpolations) {
			String id = interpolation.getId();
			String label = interpolation.getLabel();
			interpolationMap.put(id, label);
		    }
		}

		String json = getDownloader().downloadOptionalString(fileURL, username, password).orElse(null);
		JSONObject jsonObject = new JSONObject(json);
		OriginalMetadata originalMetadata = null;

		for (Map.Entry<String, String> entry : interpolationMap.entrySet()) {
		    Dataset dataset = mapJSONToISO(jsonObject, entry, variable, url, isCams, city);
		    String str = dataset.asString(true);
		    originalMetadata = new OriginalMetadata();
		    originalMetadata.setMetadata(str);
		    originalMetadata.setSchemeURI(CommonNameSpaceContext.GS_DATA_MODEL_SCHEMA_URI_GS_RESOURCE);
		    response.addRecord(originalMetadata);
		}

	    }

	    GSLoggerFactory.getLogger(getClass()).debug("Downloading files [{}-{}/{}] ENDED", start, end, allFiles.size());

	    if (response.getResumptionToken() == null) {
		GSLoggerFactory.getLogger(getClass()).info("Harvest of {} files ENDED", filteredURLs.size());
		allFiles = null;
		runFiles = null;
	    }

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    WAF_LISTING_ERROR, //
		    e);
	}

	return response;
    }

    private void removeTiffFiles() {
	allFiles.removeIf(url -> url.toString().toLowerCase().contains("tiff"));
    }

    /**
     * Returns the last path segment, or an empty string for URLs without a file (e.g. https://aux.ecmwf.int/#, found in
     * the listing)
     */
    private static String getFileName(URL url) {
	String path = url.getPath();
	return path == null ? "" : path.substring(path.lastIndexOf('/') + 1);
    }

    /**
     * Simple files (e.g. utci_Bologna_2026110200.json) have 3 parts, CAMS files (e.g.
     * cams_eu_ragweed_pollen_web_Bologna_2026110200.json) more
     */
    private static boolean isCamsFile(String fileName) {
	return fileName.split("_").length != 3;
    }

    /**
     * Returns the variable of a JSON file, or null if the file is not harvested (e.g. uvindex, extended_anomaly or
     * unknown variables)
     */
    static TRIGGERWAFVariable getVariable(String fileName) {

	String varId = null;
	if (!isCamsFile(fileName)) {
	    varId = fileName.split("_")[0];
	} else {
	    Matcher matcher = CAMS_FILE_NAME.matcher(fileName);
	    if (matcher.find()) {
		varId = matcher.group(1);
	    }
	}
	return varId == null ? null : TRIGGERWAFVariable.decode(varId);
    }

    /**
     * Returns the JSON files of the most recent run (e.g. _2026110200.json). The latest run is used only if it is
     * complete, i.e. it has at least as many files as the previous run; otherwise the previous run is used. Only the
     * harvested files are counted, since the others are published at different times (e.g. the extended anomaly of a
     * run arrives the following day).
     */
    static List<URL> filterURLsByLatestRun(List<URL> urls) {

	TreeMap<String, List<URL>> byRun = new TreeMap<>();
	int skipped = 0;
	for (URL url : urls) {
	    String fileName = getFileName(url);
	    Matcher matcher = RUN_IN_FILE_NAME.matcher(fileName);
	    if (!matcher.find()) {
		continue;
	    }
	    if (getVariable(fileName) == null) {
		skipped++;
		continue;
	    }
	    byRun.computeIfAbsent(matcher.group(1), k -> new ArrayList<>()).add(url);
	}

	GSLoggerFactory.getLogger(TRIGGERWafConnector.class).info("Listed files: {}, JSON files not harvested: {}", urls.size(), skipped);
	byRun.forEach((run, files) -> GSLoggerFactory.getLogger(TRIGGERWafConnector.class).info("Run {}: {} files", run, files.size()));

	if (byRun.isEmpty()) {
	    GSLoggerFactory.getLogger(TRIGGERWafConnector.class).warn("No run found in the listed files");
	    return new ArrayList<>();
	}

	Entry<String, List<URL>> latest = byRun.lastEntry();
	Entry<String, List<URL>> previous = byRun.lowerEntry(latest.getKey());

	if (previous == null) {
	    GSLoggerFactory.getLogger(TRIGGERWafConnector.class).warn("Only run {} available, completeness not checked", latest.getKey());
	} else if (latest.getValue().size() < previous.getValue().size()) {
	    GSLoggerFactory.getLogger(TRIGGERWafConnector.class).warn("Run {} incomplete ({} of {} files), using run {}", latest.getKey(),
		    latest.getValue().size(), previous.getValue().size(), previous.getKey());
	    return previous.getValue();
	}

	GSLoggerFactory.getLogger(TRIGGERWafConnector.class).info("Using run {} ({} files)", latest.getKey(), latest.getValue().size());
	return latest.getValue();
    }

    private Dataset mapJSONToISO(JSONObject jsonObject, Entry<String, String> entry, TRIGGERWAFVariable variable, URL fileURL,
	    boolean isCams, String cityName) {
	Dataset dataset = new Dataset();
	GSSource source = new GSSource();
	source.setEndpoint(getSourceURL());
	dataset.setSource(source);

	dataset.getPropertyHandler().setIsTimeseries(true);
	BigDecimal latitude = null;
	BigDecimal longitude = null;

	String title = null;
	String date = jsonObject.optString("date");
	String time = jsonObject.optString("time");

	String dateTime = date + time;

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	Optional<Date> parsed = ISO8601DateTimeUtils.parseNotStandard2ToDate(dateTime);

	String startDate = null;
	String endDate = null;

	if (isCams) {
	    JSONObject locationObj = jsonObject.optJSONObject("location");
	    latitude = locationObj.optBigDecimal("lat", null);
	    longitude = locationObj.optBigDecimal("lon", null);
	    title = cityName;
	    if (parsed.isPresent()) {
		Date begin = parsed.get();
		Date end = addDays(begin, 4);
		startDate = ISO8601DateTimeUtils.getISO8601DateTime(begin);
		endDate = ISO8601DateTimeUtils.getISO8601DateTime(end);
	    }
	} else {
	    latitude = jsonObject.optBigDecimal("lat", null);
	    longitude = jsonObject.optBigDecimal("lon", null);
	    title = jsonObject.optString("title");
	    if (parsed.isPresent()) {
		Date begin = parsed.get();
		Date end = addDays(begin, 10);
		startDate = ISO8601DateTimeUtils.getISO8601DateTime(begin);
		endDate = ISO8601DateTimeUtils.getISO8601DateTime(end);
	    }
	}

	Resolution resolution = Resolution.HOURLY;

	TemporalExtent extent = new TemporalExtent();

	if (startDate != null) {

	    extent.setBeginPosition(startDate);

	    if (endDate != null) {
		extent.setEndPosition(endDate);
	    } else {
		extent.setIndeterminateEndPosition(TimeIndeterminateValueType.NOW);
	    }

	    /**
	     * CODE COMMENTED BELOW COULD BE USEFUL
	     * // if (dateTime.isPresent()) {
	     * // String beginTime = ISO8601DateTimeUtils.getISO8601DateTime(dateTime.get());
	     * // extent.setPosition(beginTime, startIndeterminate, false, true);
	     * // // Estimate of the data size
	     * // // only an estimate seems to be possible, as this odata service doesn't seem to support the
	     * /$count
	     * // // operator
	     * // double expectedValuesPerYears = 12.0; // 1 value every 5 minutes
	     * // double expectedValuesPerDay = expectedValuesPerHours * 24.0;
	     * // long expectedSize = TimeSeriesUtils.estimateSize(dateTime.get(), new Date(),
	     * expectedValuesPerDay);
	     * // GridSpatialRepresentation grid = new GridSpatialRepresentation();
	     * // grid.setNumberOfDimensions(1);
	     * // grid.setCellGeometryCode("point");
	     * // Dimension time = new Dimension();
	     * // time.setDimensionNameTypeCode("time");
	     * // try {
	     * // time.setDimensionSize(new BigInteger("" + expectedSize));
	     * // ExtensionHandler extensionHandler = dataset.getExtensionHandler();
	     * // extensionHandler.setDataSize(expectedSize);
	     * // } catch (Exception e) {
	     * // }
	     * // grid.addAxisDimension(time);
	     * // coreMetadata.getMIMetadata().addGridSpatialRepresentation(grid);
	     * // }
	     */

	    coreMetadata.getMIMetadata().getDataIdentification().addTemporalExtent(extent);
	}

	String variableName = variable.getLabel();
	String variableDescription = variable.getDescription();
	String variableUnit = variable.getUnit();

	coreMetadata.setTitle("Forecats of " + variableName + " ( " + entry.getValue() + ") through the station at:" + title);

	coreMetadata.setAbstract("This dataset contains " + variableDescription + " timeseries from station: " + title);

	coreMetadata.getMIMetadata().getDataIdentification().addKeyword("forecast");

	coreMetadata.getMIMetadata().getDataIdentification().setCitationPublicationDate(ISO8601DateTimeUtils.getISO8601Date(new Date()));

	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(variableName);

	if (resolution.equals(Resolution.HOURLY))
	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword("hourly");

	ExtensionHandler handler = dataset.getExtensionHandler();
	handler.setTimeResolutionDuration8601("PT1H");
	handler.setAttributeMissingValue("-9999");
	handler.setAttributeUnitsAbbreviation(variableUnit);

	coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	if (latitude != null && longitude != null) {

	    coreMetadata.addBoundingBox(latitude, longitude, latitude, longitude);

	}

	/**
	 * MIPLATFORM
	 **/

	MIPlatform platform = new MIPlatform();

	String noSpaceName = title.replaceAll(" ", ":");

	String platformIdentifier = "trigger-waf:" + noSpaceName;

	platform.setMDIdentifierCode(platformIdentifier);

	platform.setDescription(title);

	Citation platformCitation = new Citation();
	platformCitation.setTitle(title);
	platform.setCitation(platformCitation);

	coreMetadata.getMIMetadata().addMIPlatform(platform);

	/**
	 * COVERAGEDescription
	 **/

	CoverageDescription coverageDescription = new CoverageDescription();
	String varId = noSpaceName + ":" + variableName;

	coverageDescription.setAttributeIdentifier(varId);
	coverageDescription.setAttributeTitle(variableName);

	String attributeDescription = variableDescription + " Units: " + variableUnit;

	coverageDescription.setAttributeDescription(attributeDescription);
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);

	dataset.getExtensionHandler().setTimeInterpolation(entry.getValue());

	/**
	 * ONLINE
	 */
	// https://i-change.s3.amazonaws.com/Ams01_TEMP.csv
	// Online online = new Online();
	// online.setProtocol(NetProtocols.HTTP.getCommonURN());
	// String linkage = "https://i-change.s3.amazonaws.com/" + station.getName() + buildingURL;
	// online.setLinkage(linkage);
	// online.setName(variable + "@" + station.getName());
	// online.setFunctionCode("download");
	// online.setDescription(variable + " Station name: " + station.getName());
	//
	// coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(online);

	String resourceIdentifier = generateCode(dataset, variableName + ":" + entry.getKey() + ":" + noSpaceName);

	coreMetadata.getDataIdentification().setResourceIdentifier(resourceIdentifier);

	/**
	 * Linkage url to be parametized:
	 * project={public, i-change}
	 * platform = {meteotracker, acronet, smart}
	 * observation_variable = platform.getKey()
	 */
	try {

	    // String linkage = getSourceURL();

	    Online o = new Online();
	    o.setLinkage(fileURL.toExternalForm());
	    o.setFunctionCode("download");
	    o.setName(variableName + ":" + variableUnit + ":" + entry.getKey());
	    o.setIdentifier(noSpaceName + ":" + variableDescription + ":" + entry.getKey());
	    o.setProtocol(WAF_TRIGGER_PROTOCOL);
	    o.setDescription("Station name: " + title);
	    coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(o);

	} catch (Exception e) {
	    // TODO: handle exception
	}

	coreMetadata.getMIMetadata().getDistribution().getDistributionOnline().setIdentifier(resourceIdentifier);

	return dataset;
    }

    /**
     * @param gsResource
     * @param identifier
     * @return
     */
    private String generateCode(GSResource gsResource, String identifier) {

	String out = gsResource.getSource().getUniqueIdentifier() + identifier;

	try {
	    out = StringUtils.hashSHA1messageDigest(identifier);
	} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
	    GSLoggerFactory.getLogger(AbstractResourceMapper.class).error(e);
	}

	return out;
    }

    private Date addDays(Date beginDate, int days) {
	Calendar calendar = Calendar.getInstance();
	calendar.setTime(beginDate);
	calendar.add(Calendar.DAY_OF_MONTH, days);
	return calendar.getTime();
    }


    @Override
    public boolean supports(GSSource source) {
	return source.getEndpoint().contains("aux.ecmwf.int") || source.getEndpoint().contains("s3/buckets/trigger-json-files");
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return Arrays.asList(CommonNameSpaceContext.GMD_NS_URI);
    }

    @Override
    public String getType() {

	return TYPE;
    }

    public Downloader getDownloader() {
	return downloader == null ? new Downloader() : downloader;
    }

    @Override
    protected TRIGGERWafConnectorSetting initSetting() {

	return new TRIGGERWafConnectorSetting();
    }

    public static void main(String[] args) {
	List<String> filenames = Arrays.asList("cams_eu_ragweed_pollen_web_Reading_2025032400.json",
		"cams_eu_particulate_matter_2.5um_web_Bologna_2025032400.json",
		"cams_eu_particulate_matter_10um_web_Augsburg_2025032400.json", "cams_eu_ozone_web_Paris_2025032400.json",
		"cams_eu_nitrogen_dioxide_web_Berlin_2025032400.json", "cams_eu_mugwort_pollen_web_London_2025032400.json");

	Set<String> validEnums = new HashSet<>(Arrays.asList("cams_eu_ragweed_pollen_web", "cams_eu_particulate_matter_2.5um_web",
		"cams_eu_particulate_matter_10um_web", "cams_eu_ozone_web", "cams_eu_nitrogen_dioxide_web", "cams_eu_mugwort_pollen_web",
		"cams_eu_grass_pollen_web", "cams_eu_birch_pollen_web"));

	// Map to store ENUMs and their corresponding city names
	Map<String, String> extractedData = new LinkedHashMap<>();

	for (String filename : filenames) {
	    // Regex to extract ENUM name and city name
	    String pattern = "^(cams_eu_[a-zA-Z0-9._]+_web)_([A-Za-z]+)_\\d+\\.json$";
	    Pattern regex = Pattern.compile(pattern);
	    Matcher matcher = regex.matcher(filename);

	    if (matcher.find()) {
		String extractedEnum = matcher.group(1); // ENUM Name
		String cityName = matcher.group(2); // City Name

		if (validEnums.contains(extractedEnum)) {
		    extractedData.put(extractedEnum, cityName);
		}
	    }
	}

	// Output the extracted data
	System.out.println("Extracted ENUMs and City Names:");
	extractedData.forEach((enumName, city) -> System.out.println(enumName + " -> " + city));
    }

}
