/**
 * 
 */
package eu.essi_lab.accessor.sentinel;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;

import eu.essi_lab.accessor.satellite.common.SatelliteUtils;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.iso.datamodel.classes.BrowseGraphic;
import eu.essi_lab.iso.datamodel.classes.MIInstrument;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodWithBody;
import eu.essi_lab.lib.net.protocols.impl.HTTPProtocol;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.model.resource.SatelliteScene;
import eu.essi_lab.ommdk.OriginalIdentifierMapper;

/**
 * @author Fabrizio
 */
public class SentinelMapper extends OriginalIdentifierMapper {

    public static final String SENTINEL_SCHEME_URI = "sentinel-scheme-uri";
    private static final String SENTINEL_MAPPER_ERROR = "SENTINEL_MAPPER_ERROR";

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	try {

	    XMLDocumentReader reader = new XMLDocumentReader(resource.getOriginalMetadata().getMetadata());

	    return reader.evaluateString("//*[local-name()='entry']/*[local-name()='str'][@name='identifier']");

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);
	}

	return null;
    }

    @Override
    public GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	Dataset dataset = new Dataset();
	dataset.setSource(source);

	//
	// set the GDC tag
	//
	dataset.getPropertyHandler().setIsGDC(true);

	try {

	    XMLDocumentReader reader = new XMLDocumentReader(originalMD.getMetadata());

	    MIMetadata miMetadata = dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();
	    miMetadata.setHierarchyLevelName("dataset");
	    miMetadata.setLanguage("en");

	    //
	    // identifier
	    //
	    String id = reader.evaluateString("//*[local-name()='entry']/*[local-name()='str'][@name='identifier']");

	    //
	    // bbox
	    //

	    String polygon = reader.evaluateString("//*[local-name()='entry']/*[local-name()='str'][@name='footprint']");
	    boolean firstIsLat = false;

	    if (checkString(polygon)) {

		polygon = polygon.replace("MULTIPOLYGON", "").//
			replace("POLYGON", "").//
			replace("(((", "").//
			replace("((", "").//
			replace(")))", "").//
			replace("))", "").//
			replace(",", " ").//
			replace("  ", " ").//
			trim();
	    } else {

		XMLDocumentReader polxml = new XMLDocumentReader(
			reader.evaluateString("//*[local-name()='entry']/*[local-name()='str'][@name='gmlfootprint']"));

		polygon = polxml.evaluateString("//*[local-name()='coordinates']");
		polygon = polygon.replace(",", " ").trim();

		firstIsLat = true;
	    }

	    try {

		String bbox = SatelliteUtils.toBBOX(polygon, firstIsLat);

		double west = Double.valueOf(bbox.split(" ")[1]);
		double east = Double.valueOf(bbox.split(" ")[3]);
		double north = Double.valueOf(bbox.split(" ")[2]);
		double south = Double.valueOf(bbox.split(" ")[0]);

		dataset.getHarmonizedMetadata().getCoreMetadata().addBoundingBox(north, west, south, east);

	    } catch (NumberFormatException ex) {
		GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);
	    }

	    //
	    // time
	    //
	    String beginPosition = reader.evaluateString("//*[local-name()='entry']/*[local-name()='date'][@name='beginposition']");

	    String type = id.substring(1, 3);
	    String endPosition;
	    if (type != null && (type.contains("2A") || type.contains("2B") || type.contains("2"))) {
		endPosition = extractTemporalEnd(id);
	    } else {
		endPosition = reader.evaluateString("//*[local-name()='entry']/*[local-name()='date'][@name='endposition']");
	    }

	    dataset.getHarmonizedMetadata().getCoreMetadata().addTemporalExtent(beginPosition, endPosition);

	    //
	    // parent identifier
	    //
	    String parentId = SatelliteUtils.getSentinelPlatformIndentifier(id);
	    miMetadata.setParentIdentifier(parentId);

	    //
	    // title
	    //
	    String pubDate = reader.evaluateString("//*[local-name()='entry']/*[local-name()='date'][@name='ingestiondate']");
	    String platformName = "SENTINEL_" + id.substring(1, 3);

	    String title = platformName + " " + //
		    (checkString(pubDate) ? "- Day " + pubDate + " " : "") + //
		    "(" + id + ")";

	    dataset.getHarmonizedMetadata().getCoreMetadata().setTitle(title);

	    //
	    // organization name
	    //
	    ResponsibleParty contact = new ResponsibleParty();
	    contact.setRoleCode("originator");
	    contact.setOrganisationName("European Commission");

	    miMetadata.addContact(contact);
	    miMetadata.getDataIdentification().addCitationResponsibleParty(contact);

	    //
	    // publication date
	    //
	    miMetadata.getDataIdentification().setCitationPublicationDate(pubDate);

	    //
	    // cloud cover percentage
	    //

	    String ccp = reader.evaluateString("//*[local-name()='entry']/*[local-name()='double'][@name='cloudcoverpercentage']");
	    if (checkString(ccp)) {

		miMetadata.addCloudCoverPercentage(Double.valueOf(ccp));
	    }

	    //
	    // sensor identifier
	    //
	    String sensorId = reader.evaluateString("//*[local-name()='entry']/*[local-name()='str'][@name='instrumentshortname']");

	    MIInstrument miInstrument = new MIInstrument();
	    miInstrument.setMDIdentifierTypeCode(sensorId);
	    miMetadata.addMIInstrument(miInstrument);

	    //
	    // sensor title
	    //
	    String sensorName = reader.evaluateString("//*[local-name()='entry']/*[local-name()='str'][@name='instrumentname']");
	    miInstrument.setTitle(sensorName);

	    //
	    // sensor description
	    //
	    String sensorDesc = "";

	    String sensorOpMode = reader.evaluateString("//*[local-name()='entry']/*[local-name()='str'][@name='sensoroperationalmode']");
	    String sensorSwath = reader.evaluateString("//*[local-name()='entry']/*[local-name()='str'][@name='swathidentifier']");

	    if (checkString(sensorOpMode)) {

		sensorDesc += "Instrument Operational Mode: " + sensorOpMode;
	    }

	    if (checkString(sensorSwath)) {

		sensorDesc += (sensorDesc.equalsIgnoreCase("") ? "" : " -- ") + "Instrument Swath: " + sensorSwath;
	    }

	    if (checkString(sensorDesc)) {
		miInstrument.setDescription(sensorDesc);
	    }

	    //
	    // platform desc and id
	    //
	    String platformId = reader.evaluateString("//*[local-name()='entry']/*[local-name()='str'][@name='platformidentifier']");

	    MIPlatform miPlatform = new MIPlatform();
	    miPlatform.setMDIdentifierCode(platformId);
	    miPlatform.setDescription(platformName);
	    miMetadata.addMIPlatform(miPlatform);

	    //
	    // links
	    //
	    String size = reader.evaluateString("//*[local-name()='entry']/*[local-name()='str'][@name='size']");
	    String alternative = reader.evaluateString("//*[local-name()='entry']/*[local-name()='link'][@rel='alternative']/@href");
	    String link = reader.evaluateString("//*[local-name()='entry']/*[local-name()='link']/@href");

	    addLink(miMetadata, link, false, size);
	    addLink(miMetadata, alternative, true, size);

	    // ------------------------
	    //
	    // SatelliteScene extension
	    //
	    //
	    SatelliteScene satelliteScene = new SatelliteScene();

	    //
	    // thumbnail
	    //
	    try {
		handleThumbnail(reader, satelliteScene, id, miMetadata);
	    } catch (Exception ex) {

		GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);
	    }

	    // this is used by the AtomGPResultSetMapper
	    satelliteScene.setOrigin("sentinel");

	    satelliteScene.setPlatid(platformId);

	    String relOrbit = reader.evaluateString("//*[local-name()='entry']/*[local-name()='int'][@name='relativeorbitnumber']");
	    if (checkString(relOrbit)) {
		satelliteScene.setRelativeOrbit(Integer.valueOf(relOrbit));
	    }

	    String productType = reader.evaluateString("//*[local-name()='entry']/*[local-name()='str'][@name='producttype']");
	    if (checkString(productType)) {
		satelliteScene.setProductType(productType);
	    }

	    if (checkString(ccp)) {
		satelliteScene.setCloudCoverPercentage(ccp);
	    }

	    if (checkString(sensorSwath)) {
		satelliteScene.setSensorSwath(sensorSwath);
	    }

	    if (checkString(sensorOpMode)) {
		satelliteScene.setSensorOpMode(sensorOpMode);
	    }

	    String sensorPolarization = reader.evaluateString("//*[local-name()='entry']/*[local-name()='str'][@name='polarisationmode']");
	    if (checkString(sensorPolarization)) {
		sensorPolarization = sensorPolarization.replace(" ", "").replace(",", "");
		satelliteScene.setSarPolCh(sensorPolarization);
	    }

	    String footprint = reader.evaluateString("//*[local-name()='entry']/*[local-name()='str'][@name='footprint']");
	    if (checkString(footprint)) {
		satelliteScene.setFootprint(footprint);
	    }

	    String productconsolidation = reader
		    .evaluateString("//*[local-name()='entry']/*[local-name()='str'][@name='productconsolidation']");
	    if (checkString(productconsolidation)) {
		satelliteScene.setProductConsolidation(productconsolidation);
	    }

	    String stopRelativeOrbitNumber = reader
		    .evaluateString("//*[local-name()='entry']/*[local-name()='int'][@name='lastrelativeorbitnumber']");

	    if (checkString(stopRelativeOrbitNumber)) {
		satelliteScene.setStopRelativeOrbitNumber(stopRelativeOrbitNumber);
	    }

	    String startOrbitNumber = reader.evaluateString("//*[local-name()='entry']/*[local-name()='int'][@name='orbitnumber']");
	    if (checkString(startOrbitNumber)) {
		satelliteScene.setStartOrbitNumber(startOrbitNumber);
	    }

	    String stopOrbitNumber = reader.evaluateString("//*[local-name()='entry']/*[local-name()='int'][@name='lastorbitnumber']");
	    if (checkString(stopOrbitNumber)) {
		satelliteScene.setStopOrbitNumber(stopOrbitNumber);
	    }

	    String orbitdirection = reader.evaluateString("//*[local-name()='entry']/*[local-name()='str'][@name='orbitdirection']");
	    if (checkString(orbitdirection)) {
		satelliteScene.setOrbitDirection(orbitdirection);
	    }

	    String productclass = reader.evaluateString("//*[local-name()='entry']/*[local-name()='str'][@name='productclass']");
	    if (checkString(productclass)) {
		satelliteScene.setProductClass(productclass);
	    }

	    String acquisitiontype = reader.evaluateString("//*[local-name()='entry']/*[local-name()='str'][@name='acquisitiontype']");
	    if (checkString(acquisitiontype)) {
		satelliteScene.setAcquisitionType(acquisitiontype);
	    }

	    String slicenumber = reader.evaluateString("//*[local-name()='entry']/*[local-name()='int'][@name='slicenumber']");
	    if (checkString(slicenumber)) {
		satelliteScene.setSliceNumber(slicenumber);
	    }

	    String missiondatatakeid = reader.evaluateString("//*[local-name()='entry']/*[local-name()='str'][@name='missiondatatakeid']");
	    if (checkString(missiondatatakeid)) {
		satelliteScene.setMissionDatatakeid(missiondatatakeid);
	    }

	    String status = reader.evaluateString("//*[local-name()='entry']/*[local-name()='str'][@name='status']");
	    if (checkString(status)) {
		satelliteScene.setStatus(status);
	    }

	    String processingbaseline = reader
		    .evaluateString("//*[local-name()='entry']/*[local-name()='str'][@name='processingbaseline']");
	    if (checkString(processingbaseline)) {
		satelliteScene.setProcessingBaseline(processingbaseline);
	    }

	    String processinglevel = reader.evaluateString("//*[local-name()='entry']/*[local-name()='str'][@name='processinglevel']");
	    if (checkString(processinglevel)) {
		satelliteScene.setProcessingLevel(processinglevel);
	    }

	    String dusId = reader.evaluateString("//*[local-name()='entry']/*[local-name()='str'][@name='uuid']");
	    if (checkString(dusId)) {
		satelliteScene.setDusId(dusId);
	    }

	    String s3ProductLevel = reader.evaluateString("//*[local-name()='entry']/*[local-name()='str'][@name='productlevel']");
	    if (checkString(s3ProductLevel)) {
		satelliteScene.setS3ProductLevel(s3ProductLevel);
	    }

	    String s3Timeliness = reader.evaluateString("//*[local-name()='entry']/*[local-name()='str'][@name='timeliness']");
	    if (checkString(s3Timeliness)) {
		satelliteScene.setS3Timeliness(s3Timeliness);
	    }

	    String productFormat = reader.evaluateString("//*[local-name()='entry']/*[local-name()='str'][@name='format']");
	    if (checkString(productFormat)) {
		satelliteScene.setProductFormat(productFormat);
	    }

	    if (checkString(sensorId)) {
		satelliteScene.setS3InstrumentIdx(sensorId);
	    }

	    //
	    // complex links for loading preview
	    addComplexLink(miMetadata, id, satelliteScene);

	    //

	    dataset.getExtensionHandler().setSatelliteScene(satelliteScene);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    SENTINEL_MAPPER_ERROR, e);

	}

	return dataset;
    }

    private String extractTemporalEnd(String id) throws ParseException {
	String time = id.substring(id.lastIndexOf("_") + 1);
	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
	sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
	Date date = sdf.parse(time);
	SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.000Z'");
	sdf2.setTimeZone(TimeZone.getTimeZone("UTC"));
	return sdf2.format(date);
    }

    /**
     * @param reader
     * @param satelliteScene
     * @param id
     * @param miMetadata
     * @throws Exception
     */
    public boolean handleThumbnail(XMLDocumentReader reader, SatelliteScene satelliteScene, String id, MIMetadata miMetadata)
	    throws Exception {

	String quickLook = reader.evaluateString("//*[local-name()='entry']/*[local-name()='link'][@rel='icon']/@href");

	if (quickLook == null || quickLook.isEmpty()) {

	    // GSLoggerFactory.getLogger(getClass()).trace("No quick look available, skip");
	    return false;
	}

	// GSLoggerFactory.getLogger(getClass()).trace("Quick look to handle: " + quickLook);

	String user = ConfigurationWrapper.getCredentialsSetting().getSentinelUser().orElse(null);
	String password = ConfigurationWrapper.getCredentialsSetting().getSentinelPassword().orElse(null);

	quickLook = quickLook.replace("https://scihub.copernicus.eu", "https://" + user + ":" + password + "@scihub.copernicus.eu");

	// GSLoggerFactory.getLogger(getClass()).trace("Quick look with credentials: " + quickLook);

	Downloader pReq = new Downloader();

	Optional<InputStream> optional = Optional.empty();

	try {
	    optional = pReq.downloadOptionalStream(quickLook);

	} catch (IllegalArgumentException ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage());
	    GSLoggerFactory.getLogger(getClass()).error("Invalid quicklook: " + quickLook);
	}

	if (optional.isPresent()) {

	    String putBase = "http://tiles.geodab.eu/geodab-tiles-nt/tile/thumbnail-";
	    String putRequ = putBase + id + "/0/0/0";

	    ClonableInputStream cis = new ClonableInputStream(optional.get());
	    boolean isImage = false;

	    if (IOStreamUtils.isPNG(cis.clone())) {

		putRequ = putRequ + ".png";
		isImage = true;
	    }

	    if (IOStreamUtils.isJPEG(cis.clone())) {

		putRequ = putRequ + ".jpg";
		isImage = true;
	    }

	    if (isImage) {

		// GSLoggerFactory.getLogger(getClass()).trace("Thumbnail uploading STARTED");
		// GSLoggerFactory.getLogger(getClass()).trace("Current upload url: {}", putRequ);

		// HttpPut putRequest = new HttpPut();
		// putRequest.setURI(new URI(putRequ));
		// putRequest.setEntity(new InputStreamEntity(cis.clone()));

		Downloader executor = new Downloader();
 
		HttpRequest putRequest = HttpRequestUtils.build(MethodWithBody.PUT, putRequ, cis.clone());

		HttpResponse<InputStream> response = executor.downloadResponse(putRequest);

		int statusCode = response.statusCode();
		if (statusCode == 200) {

		    satelliteScene.setThumbnailURL(putRequ);

		    BrowseGraphic browseGraphic = new BrowseGraphic();
		    browseGraphic.setFileName(putRequ);

		    browseGraphic.setFileDescription("Pictorial preview of the dataset");

		    miMetadata.getDataIdentification().addGraphicOverview(browseGraphic);

		    // GSLoggerFactory.getLogger(getClass()).trace("Thumbnail uploading ENDED. Thumbnail correctly
		    // set");

		    return true;

		} else {

		    // GSLoggerFactory.getLogger(getClass()).error("Unable to upload thumbnail : " +
		    // response.getStatusLine());
		}
	    } else {

		// GSLoggerFactory.getLogger(getClass()).trace("Quick look not available: " + quickLook);
	    }
	} else {

	    // GSLoggerFactory.getLogger(getClass()).trace("Quick look not available: " + quickLook);
	}

	// GSLoggerFactory.getLogger(getClass()).trace("Thumbnail uploading ENDED. Thumbnail not set");

	return false;
    }

    /**
     * @param mi_Metadata
     * @param link
     * @param alternative
     * @param size
     */
    private void addLink(MIMetadata mi_Metadata, String link, boolean alternative, String size) {

	Online onLine = new Online();
	onLine.setLinkage(link);
	onLine.setProtocol(new HTTPProtocol().getCommonURN());
	onLine.setName("Product");
	onLine.setFunctionCode("download");
	if (alternative) {
	    onLine.setDescription("Alternative link");
	}

	if (checkString(size)) {
	    String megaBytes = SatelliteUtils.toMegaBytes(size);
	    mi_Metadata.getDistribution().addDistributionOnline(onLine, Double.valueOf(megaBytes));
	} else {
	    mi_Metadata.getDistribution().addDistributionOnline(onLine);
	}
    }

    /**
     * @param mi_Metadata
     * @param link
     */
    public void addComplexLink(MIMetadata mi_Metadata, String identifier, SatelliteScene scene) {

	// String baseUrl = "http://gs-service-production.geodab.eu/gs-service/services/essi/wms?";
	String check = identifier.substring(1, 3);
	if (check != null && (check.contains("2A") || check.contains("2B"))) {
	    String link = "http://services.sentinel-hub.com/";

	    for (SatelliteLayers v : SatelliteLayers.values()) {
		Online onLine = new Online();
		onLine.setLinkage(link);
		onLine.setName(v.toString() + "@" + identifier);
		onLine.setProtocol(CommonNameSpaceContext.SENTINEL2_URI);
		onLine.setFunctionCode("download");
		mi_Metadata.getDistribution().addDistributionOnline(onLine);
	    }

	}
    }

    /**
     * @param s
     * @return
     */
    private boolean checkString(String s) {

	return s != null && !s.isEmpty();
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return SENTINEL_SCHEME_URI;
    }
}
