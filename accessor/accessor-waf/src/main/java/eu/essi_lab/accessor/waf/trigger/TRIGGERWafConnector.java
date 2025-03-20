/**
 * 
 */
package eu.essi_lab.accessor.waf.trigger;

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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.IOUtils;
import org.geotools.imageio.netcdf.GeoToolsNetCDFReader;
import org.geotools.imageio.netcdf.utilities.NetCDFCRSUtilities;
import org.json.JSONObject;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.xml.sax.SAXException;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.DownloadSetting;
import eu.essi_lab.cfga.gs.setting.S3StorageSetting;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.Dimension;
import eu.essi_lab.iso.datamodel.classes.GridSpatialRepresentation;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.iso.datamodel.classes.VerticalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.dirlisting.WAFClient;
import eu.essi_lab.lib.net.dirlisting.WAF_URL;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.s3.S3TransferWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.utils.IterationLogger;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
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
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.Datum;
import eu.essi_lab.model.resource.data.DimensionType;
import eu.essi_lab.model.resource.data.Unit;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.netcdf.timeseries.NetCDFUtils;
import eu.essi_lab.ommdk.AbstractResourceMapper;
import net.opengis.gml.v_3_2_0.TimeIndeterminateValueType;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dataset.NetcdfDataset;

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

    private static final int STEP = 50;

    private List<URL> allFiles;
    private static final String BUCKET_NAME = "trigger-json-files";
    private static final int TARGET_COUNT = 2888086;
    private static final Integer PAGE_SIZE = 100;
    private int recordsCount;

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

		if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent() && mr.get() < allFiles.size()) {

		    GSLoggerFactory.getLogger(getClass()).debug("List reduced to {} elements", mr.get());

		    allFiles = allFiles.subList(0, mr.get());

		}

		GSLoggerFactory.getLogger(getClass()).debug("Download URLs from ECMWF WAF ENDED");

	    }

	    List<URL> filteredURLs = filterURLsByToday(allFiles, 0);
	    if (filteredURLs == null || filteredURLs.isEmpty()) {
		filteredURLs = filterURLsByToday(allFiles, -1);

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
		String name = url.getFile();

		String[] parts = name.split("/");
		String fileName = parts[parts.length - 1];
		String[] splittedName = fileName.split("_");
		// check splitted names - for now we take only the simple files
		String varId = null;
		String city = null;
		String date = null;

		if (splittedName.length == 3) {

		    varId = splittedName[0];
		    TRIGGERWAFVariable variable = TRIGGERWAFVariable.decode(varId);
		    if (variable == null) {
			continue;
		    }

		    String json = getDownloader().downloadOptionalString(url.toExternalForm(), username, password).orElse(null);
		    JSONObject jsonObject = new JSONObject(json);
		    OriginalMetadata originalMetadata = null;
		    TRIGGER_INTERPOLATIONS[] interpolations = TRIGGER_INTERPOLATIONS.values();
		    for (TRIGGER_INTERPOLATIONS interpolation : interpolations) {
			Dataset dataset = mapJSONToISO(jsonObject, interpolation, variable, url);
			String str = dataset.asString(true);
			originalMetadata = new OriginalMetadata();
			originalMetadata.setMetadata(str);
			originalMetadata.setSchemeURI(CommonNameSpaceContext.GS_DATA_MODEL_SCHEMA_URI_GS_RESOURCE);
			response.addRecord(originalMetadata);

		    }

		}

	    }

	    GSLoggerFactory.getLogger(getClass()).debug("Downloading files [{}-{}/{}] ENDED", start, end, allFiles.size());
	    
	    if (response.getResumptionToken() == null) {
		allFiles = null;
		filteredURLs = null;
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

    private List<URL> filterURLsByToday(List<URL> urls, int i) {
	String todayDateString = getTodayDateString(i);
	return urls.stream().filter(url -> url.toString().contains(todayDateString)).collect(Collectors.toList());
    }

    private String getTodayDateString(int i) {
	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	Date d;
	// today date
	if (i == 0) {
	    d = new Date();
	} else {
	    // yesterday date
	    d = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
	}

	return sdf.format(d); // Returns today's date in "yyyyMMdd" format

    }

    private Dataset mapJSONToISO(JSONObject jsonObject, TRIGGER_INTERPOLATIONS interpolation, TRIGGERWAFVariable variable, URL fileURL) {
	Dataset dataset = new Dataset();
	GSSource source = new GSSource();
	source.setEndpoint(getSourceURL());
	dataset.setSource(source);

	dataset.getPropertyHandler().setIsTimeseries(true);

	BigDecimal latitude = jsonObject.optBigDecimal("lat", null);
	BigDecimal longitude = jsonObject.optBigDecimal("lon", null);

	String title = jsonObject.optString("title");
	String date = jsonObject.optString("date");
	String time = jsonObject.optString("time");

	String dateTime = date + time;

	Resolution resolution = Resolution.HOURLY;

	// TEMPORAL EXTENT
	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	TemporalExtent extent = new TemporalExtent();

	Optional<Date> parsed = ISO8601DateTimeUtils.parseNotStandard2ToDate(dateTime);

	String startDate = null;
	String endDate = null;
	if (parsed.isPresent()) {
	    Date begin = parsed.get();
	    Date end = addDays(begin, 10);
	    startDate = ISO8601DateTimeUtils.getISO8601DateTime(begin);
	    endDate = ISO8601DateTimeUtils.getISO8601DateTime(end);
	}

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

	coreMetadata.setTitle("Forecats of " + variableName + " through the station at:" + title);

	coreMetadata.setAbstract("This dataset contains " + variableDescription + " timeseries from station: " + title);

	coreMetadata.getMIMetadata().getDataIdentification().addKeyword("forecast");

	coreMetadata.getMIMetadata().getDataIdentification().setCitationPublicationDate(ISO8601DateTimeUtils.getISO8601Date(new Date()));

	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(variableName);

	if (resolution.equals(Resolution.HOURLY))
	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword("hourly");

	ExtensionHandler handler = dataset.getExtensionHandler();
	handler.setTimeUnits("h");
	handler.setTimeResolution("1");
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
	String varId = noSpaceName + ":" + variableName + ":" + interpolation.getId();

	coverageDescription.setAttributeIdentifier(varId);
	coverageDescription.setAttributeTitle(variableName + " (" + interpolation.getLabel() + ")");

	String attributeDescription = variableDescription + " Units: " + variableUnit;

	coverageDescription.setAttributeDescription(attributeDescription);
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);

	dataset.getExtensionHandler().setTimeInterpolation(interpolation.getInterpolationType());

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

	String resourceIdentifier = generateCode(dataset, variableName + ":" + interpolation.getId() + ":" + noSpaceName);

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
	    o.setName(variableName + ":" + variableUnit + ":" + interpolation.getId());
	    o.setIdentifier(noSpaceName + ":" + variableDescription + ":" + interpolation.getId());
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

    public static File getLocalCopy(String url, String filename) throws Exception {
	String u = url + filename;
	Downloader down = new Downloader();
	Optional<InputStream> netCDF = down.downloadOptionalStream(u);

	if (netCDF.isPresent()) {

	    // coverageDescription.setAttributeIdentifier("urn:ca:qc:gouv:cehq:depot:variable:" +
	    // var.name());
	    // coverageDescription.setAttributeTitle(var.getLabel());
	    String tmpDirsLocation = System.getProperty("java.io.tmpdir");
	    File tmpFile = new File(tmpDirsLocation, "netcdf-connector-" + filename);
	    if (tmpFile.exists()) {
		// already downloaded
		return tmpFile;
	    }
	    FileOutputStream fos = new FileOutputStream(tmpFile);
	    IOUtils.copy(netCDF.get(), fos);
	    fos.close();
	    return tmpFile;
	}
	return null;

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
}
