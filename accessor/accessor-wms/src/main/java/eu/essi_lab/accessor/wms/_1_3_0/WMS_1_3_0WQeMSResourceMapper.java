package eu.essi_lab.accessor.wms._1_3_0;

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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import eu.essi_lab.accessor.wms.IWMSContact;
import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.BrowseGraphic;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.Distribution;
import eu.essi_lab.iso.datamodel.classes.LegalConstraints;
import eu.essi_lab.iso.datamodel.classes.MDMetadata;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ReferenceSystem;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.wms._1_3_0.DataURL;
import eu.essi_lab.jaxb.wms._1_3_0.Dimension;
import eu.essi_lab.jaxb.wms._1_3_0.Keyword;
import eu.essi_lab.jaxb.wms._1_3_0.KeywordList;
import eu.essi_lab.jaxb.wms._1_3_0.Layer;
import eu.essi_lab.jaxb.wms._1_3_0.MetadataURL;
import eu.essi_lab.jaxb.wms._1_3_0.WMSCapabilities;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;
import eu.essi_lab.lib.net.utils.HttpConnectionUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.GSPropertyHandler;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.DatasetCollection;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.HarmonizedMetadata;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.DataDimension;
import eu.essi_lab.model.resource.data.dimension.FiniteDimension;
import eu.essi_lab.ommdk.OriginalIdentifierMapper;
import net.opengis.gml.v_3_2_0.EnvelopeType;

/**
 * @author boldrini
 */
public class WMS_1_3_0WQeMSResourceMapper extends OriginalIdentifierMapper {

    private static final String WMS_MAPPER_MAP_ERROR = "WMS_MAPPER_MAP_ERROR";

    public WMS_1_3_0WQeMSResourceMapper() {
    }

    protected Downloader downloader = new Downloader();

    public void setDownloader(Downloader downloader) {
	this.downloader = downloader;
    }

    public void setWMSDownloader(WMS_1_3_0Downloader wmsDownloader) {
	this.wmsDownloader = wmsDownloader;
    }

    protected WMS_1_3_0Downloader wmsDownloader = new WMS_1_3_0Downloader();

    /**
     * @param datasetInfo
     * @param sensorInfo
     * @return
     */
    static List<OriginalMetadata> create(String metadata, String uuid) {

	List<OriginalMetadata> granulesMetadata = new ArrayList<OriginalMetadata>();

	try {

	    WMSCapabilities wmsCapabilities = (WMSCapabilities) WMS_1_3_0Connector.context.createUnmarshaller()
		    .unmarshal(new ByteArrayInputStream(metadata.getBytes(StandardCharsets.UTF_8)));

	    WMS_1_3_0Capabilities capabilities = new WMS_1_3_0Capabilities(wmsCapabilities);

	    Layer last = wmsCapabilities.getCapability().getLayer();

	    boolean end = false;
	    while (!end) {
		if (!last.getLayers().isEmpty()) {
		    last = last.getLayers().get(0);
		} else {
		    end = true;
		}
	    }

	    WMS_1_3_0Layer wmsLayer = new WMS_1_3_0Layer(capabilities, last);

	    String name = wmsLayer.getName();

	    Layer layer = wmsLayer.getLayer();
	    List<String> metadataURLs = new ArrayList<String>();
	    List<String> dataURLs = new ArrayList<String>();
	    List<String> granulesURLs = new ArrayList<String>();
	    List<String> dimensionNames = new ArrayList<String>();
	    if (layer != null) {

		List<Dimension> dimensions = layer.getDimensions();
		for (Dimension d : dimensions) {
		    String dimensionName = d.getValue();
		    if (dimensionName.contains(",")) {
			String[] splittedDimesion = dimensionName.split(",");
			for (int i = 0; i < splittedDimesion.length; i++) {
			    dimensionNames.add(splittedDimesion[i]);
			}
		    }
		}

		List<MetadataURL> wmsMetadataURLs = layer.getMetadataURLs();
		for (MetadataURL mdURL : wmsMetadataURLs) {
		    metadataURLs.add(mdURL.getOnlineResource().getHref());
		}
		List<DataURL> wmsDataURLs = layer.getDataURLs();
		for (DataURL dURL : wmsDataURLs) {
		    dataURLs.add(dURL.getOnlineResource().getHref());
		}

		for (String str : metadataURLs) {
		    for (String s : dimensionNames) {
			String granuleURL = str.replace("$(GRANULE)", s);
			granulesURLs.add(granuleURL);
		    }
		}
		// if (metadataURL != null && metadataURL.contains("$(GRANULE)")) {
		// metadataURL = metadataURL.replace("$(GRANULE)", dimensionName);
		// }
		// if (dataURL != null && dataURL.contains("$(GRANULE)")) {
		// dataURL = dataURL.replace("$(GRANULE)", dimensionName);
		// }

	    }

	    Downloader d = new Downloader();
	    int i = 0;
	    for (String url : granulesURLs) {
		String granule = dimensionNames.get(i);
		i++;
		// get iso metadata
		HttpResponse<InputStream> response = d.downloadResponse(url);
		if (response == null || response.statusCode() != 200) {
		    String msg = "Unable to retrieve URL: " + url;
		    continue;
		}

		MDMetadata mdMetadata = new MDMetadata(response.body());
		mdMetadata.setParentIdentifier(uuid);
		String wmsHref = wmsCapabilities.getService().getOnlineResource().getHref();
		if (wmsHref.endsWith("geoserver/")) {
		    wmsHref += "ows?DIM_GRANULE=" + granule;
		    Online datOnline = new Online();
		    datOnline.setProtocol(NetProtocolWrapper.WMS_1_3_0.getCommonURN());
		    datOnline.setLinkage(wmsHref);
		    datOnline.setName(name);
		    datOnline.setFunctionCode("download");
		    String uuid2 = UUID.randomUUID().toString();
		    datOnline.setIdentifier(uuid2);
		    mdMetadata.getDistribution().addDistributionOnline(datOnline);

		    // preview
		    WMS_1_3_0Downloader wmsDownloader = new WMS_1_3_0Downloader();
		    GSResource ret = new Dataset();
		    MIMetadata miMetadata = new MIMetadata(mdMetadata.getElementType());
		    ret.getHarmonizedMetadata().getCoreMetadata().setMIMetadata(miMetadata);
		    wmsDownloader.setOnlineResource(ret, uuid2);
		    // preset the capabilities document in the downloader connector, as it is no need to re-download
		    // the
		    // capabilities document to generate the preview link
		    WMS_1_3_0Connector downloaderConnector = wmsDownloader.getConnector();
		    downloaderConnector.setCapabilities(capabilities);
		    DataDescriptor[] previewDescriptors = new DataDescriptor[] {};

		    try {
			previewDescriptors = wmsDownloader.getPreviewRemoteDescriptors().toArray(new DataDescriptor[] {});
		    } catch (Exception e) {

			GSLoggerFactory.getLogger(WMS_1_3_0WQeMSResourceMapper.class)
				.warn("Can't get previewe descriptors: " + e.getMessage(), e);
		    }

		    if (previewDescriptors.length > 0) {

			Arrays.sort(previewDescriptors, (o1, o2) -> {

			    Integer p1 = getStaticPoints(o1);
			    Integer p2 = getStaticPoints(o2);

			    return p1.compareTo(p2);
			});

			DataDescriptor previewDescriptor = previewDescriptors[0];

			// fixTime(previewDescriptor);

			URL previewUrl = wmsDownloader.getImageURL(previewDescriptor, wmsHref + "&");

			BrowseGraphic graphic = new BrowseGraphic();
			graphic.setFileDescription(previewUrl.getPath());
			graphic.setFileName(previewUrl.toString());
			graphic.setFileType("image/png");

			mdMetadata.getDataIdentification().addGraphicOverview(graphic);
		    }

		}

		// tiff data seems to be already present in metadata granules
		// if (url.endsWith(".xml")) {
		//
		// String dataURL = url.replace(".xml", ".tif");
		// Online datOnline = new Online();
		// datOnline.setProtocol(new HTTPProtocol().getCommonURN());
		// datOnline.setLinkage(dataURL);
		// datOnline.setName("Data URL");
		// datOnline.setFunctionCode("download");
		// datOnline.setIdentifier(uuid);
		// mdMetadata.getDistribution().addDistributionOnline(datOnline);
		//
		// }
		String mdMetadataString = mdMetadata.asString(true);
		OriginalMetadata om = new OriginalMetadata();
		om.setMetadata(mdMetadataString);
		om.setSchemeURI(CommonNameSpaceContext.GMD_NS_URI);
		granulesMetadata.add(om);
		// MDMetadata mdMetadata = new MDMetadata(optionalResponse.get());
		// miMetadata = new MIMetadata(mdMetadata.getElementType());
	    }

	    return granulesMetadata;

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(WMS_1_3_0WQeMSResourceMapper.class).error(ex.getMessage(), ex);
	}

	return granulesMetadata;
    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	try {

	    String id = resource.getPublicId();
	    return id;

	    // WMSCapabilities wmsCapabilities = (WMSCapabilities) WMS_1_3_0Connector.context.createUnmarshaller()
	    // .unmarshal(new ByteArrayInputStream(//
	    // resource.getOriginalMetadata().//
	    // getMetadata().//
	    // getBytes(StandardCharsets.UTF_8)));
	    //
	    // WMS_1_3_0Capabilities capabilities = new WMS_1_3_0Capabilities(wmsCapabilities);
	    //
	    // Layer last = wmsCapabilities.getCapability().getLayer();
	    //
	    // boolean end = false;
	    // while (!end) {
	    // if (!last.getLayers().isEmpty()) {
	    // last = last.getLayers().get(0);
	    // } else {
	    // end = true;
	    // }
	    // }
	    //
	    // WMS_1_3_0Layer wmsLayer = new WMS_1_3_0Layer(capabilities, last);
	    //
	    // return wmsLayer.getName();

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);
	}

	return null;
    }

    @Override
    public GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	try {

	    WMSCapabilities capabilities = (WMSCapabilities) WMS_1_3_0Connector.context.createUnmarshaller()
		    .unmarshal(new ByteArrayInputStream(originalMD.getMetadata().getBytes(StandardCharsets.UTF_8)));

	    String endpoint = source.getEndpoint();
	    GSPropertyHandler addInfo = originalMD.getAdditionalInfo();
	    String uuid = addInfo.get("parentId", String.class);
	    GSResource resource = mapResource(capabilities, endpoint, uuid);
	    resource.setSource(source);
	    if (resource.getHarmonizedMetadata().getCoreMetadata().getTitle().isEmpty() || resource.getHarmonizedMetadata()
		    .getCoreMetadata().getTitle().toLowerCase().contains("wqems geoserver web map service"))
		return null;

	    return resource;

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).warn("Exception mapping layer: " + e.getMessage(), e);
	    throw GSException.createException( //
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_FATAL, //
		    WMS_MAPPER_MAP_ERROR, //
		    e);
	}

    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return CommonNameSpaceContext.WMS_1_3_0_QWeMS_NS_URI;
    }

    private GSResource mapResource(WMSCapabilities wmsCapabilities, String sourceEndpoint, String parentId) throws Exception {

	MIMetadata metadata = new MIMetadata();

	WMS_1_3_0Capabilities capabilities = new WMS_1_3_0Capabilities(wmsCapabilities);

	Layer last = wmsCapabilities.getCapability().getLayer();

	boolean end = false;
	while (!end) {
	    if (!last.getLayers().isEmpty()) {
		last = last.getLayers().get(0);
	    } else {
		end = true;
	    }
	}

	WMS_1_3_0Layer wmsLayer = new WMS_1_3_0Layer(capabilities, last);

	GSResource ret = null;

	String name = wmsLayer.getName();

	ret = new DatasetCollection();
	// String parentId = UUID.randomUUID().toString();
	ret.setPublicId(parentId);
	ret.setPrivateId(parentId);
	ret.setOriginalId(parentId);

	Layer layer = wmsLayer.getLayer();
	String metadataURL = null;
	String dataURL = null;
	MIMetadata miMetadata = null;
	// if (layer != null) {
	// List<String> dimensionNames = new ArrayList<String>();
	// List<Dimension> dimensions = layer.getDimensions();
	// for (Dimension d : dimensions) {
	// String dimensionName = d.getValue();
	// dimensionNames.add(dimensionName);
	// }
	//
	// for (String s : dimensionNames) {
	//
	// List<MetadataURL> metadataURLs = layer.getMetadataURLs();
	// for (MetadataURL mdURL : metadataURLs) {
	// metadataURL = mdURL.getOnlineResource().getHref();
	// }
	// List<DataURL> dataURLs = layer.getDataURLs();
	// for (DataURL dURL : dataURLs) {
	// dataURL = dURL.getOnlineResource().getHref();
	// }
	//
	// if (metadataURL != null && metadataURL.contains("$(GRANULE)")) {
	// metadataURL = metadataURL.replace("$(GRANULE)", dimensionName);
	// }
	// if (dataURL != null && dataURL.contains("$(GRANULE)")) {
	// dataURL = dataURL.replace("$(GRANULE)", dimensionName);
	// }
	//
	// }
	//
	// }

	// if(metadataURL != null) {
	// //get iso metadata
	// Optional<InputStream> optionalResponse = downloader.downloadStream(metadataURL);
	// if(optionalResponse.isPresent()) {
	// //XMLDocumentReader xdoc = new XMLDocumentReader(optionalResponse.get());
	// MDMetadata mdMetadata = new MDMetadata(optionalResponse.get());
	// miMetadata = new MIMetadata(mdMetadata.getElementType());
	// }
	// }
	//
	// if(miMetadata != null) {
	// CoreMetadata coreMetadata = ret.getHarmonizedMetadata().getCoreMetadata();
	// coreMetadata.setMIMetadata(miMetadata);
	// return ret;
	// }

	HarmonizedMetadata harmonizedMetadata = ret.getHarmonizedMetadata();
	CoreMetadata coreMetadata = harmonizedMetadata.getCoreMetadata();

	if (ret instanceof DatasetCollection) {
	    metadata.addHierarchyLevelScopeCodeListValue("series");

	}else{
	    metadata.addHierarchyLevelScopeCodeListValue("dataset");
	}
	coreMetadata.setMIMetadata(metadata);

	coreMetadata.setIdentifier(parentId);

	IWMSContact contactInformation = capabilities.getContactInformation();

	ExtensionHandler extendedMetadataHandler = ret.getExtensionHandler();

	ResponsibleParty contact = new ResponsibleParty();
	if (contactInformation != null) {
	    contact.setIndividualName(contactInformation.getContactIndividualName());
	    contact.setPositionName(contactInformation.getContactPositionName());
	    contact.setOrganisationName(contactInformation.getOrganisationName());

	    if (contactInformation.getOrganisationName() != null) {

		extendedMetadataHandler.addOriginatorOrganisationDescription(contactInformation.getOrganisationName());
	    }

	    Address iaddress = new Address();
	    iaddress.addDeliveryPoint(contactInformation.getDeliveryPoint());
	    iaddress.setAdministrativeArea(contactInformation.getAdministrativeArea());
	    iaddress.setCity(contactInformation.getCity());
	    iaddress.setPostalCode(contactInformation.getPostCode());
	    iaddress.setCountry(contactInformation.getCountry());
	    iaddress.addElectronicMailAddress(contactInformation.getMailAddress());
	    Contact contactInfo = new Contact();
	    contactInfo.setAddress(iaddress);
	    contact.setContactInfo(contactInfo);
	    contactInfo.addPhoneVoice(contactInformation.getPhone());
	    contactInfo.addPhoneFax(contactInformation.getFax());
	    String site = capabilities.getServiceOnlineResource();
	    if (site != null) {
		Online online = new Online();
		online.setLinkage(site);
		contactInfo.setOnline(online);
	    }

	}

	metadata.addContact(contact);

	List<String> crses = wmsLayer.getCRS();
	if (crses != null && !crses.isEmpty()) {
	    for (String crs : crses) {
		ReferenceSystem ref = new ReferenceSystem();
		ref.setCode(crs);
		metadata.addReferenceSystemInfo(ref);
	    }
	} else {
	    if (name != null) {
		GSLoggerFactory.getLogger(getClass()).warn("No CRS specified for this layer: {}", wmsLayer.toString());
	    }
	}
	// wmsLayer.getLayer().getDataURLs()
	// wmsLayer.getLayer().getDimensions()
	// wmsLayer.getLayer().getMetadataURLs()
	DataIdentification identification = new DataIdentification();

	String id;
	if (name == null || name.isEmpty()) {
	    id = UUID.randomUUID().toString();
	} else {
	    id = name;
	}

	identification.setResourceIdentifier(id);

	metadata.addDataIdentification(identification);

	identification.setCitationTitle(wmsLayer.getTitle());

	// ABSTRACT
	String abs = wmsLayer.getAbstract();
	if (abs != null) {
	    identification.setAbstract(abs);
	}

	// KEYWORD
	HashSet<String> finalKeywords = new HashSet<>();
	List<String> keywords = wmsLayer.getKeywords();
	if (keywords != null && !keywords.isEmpty()) {
	    for (String keyword : keywords) {
		finalKeywords.add(keyword);
	    }
	} else {
	    KeywordList keywordsList = capabilities.getCapabilities().getService().getKeywordList();
	    if (keywordsList != null) {
		List<Keyword> ks = keywordsList.getKeywords();
		if (ks != null) {
		    for (Keyword k : ks) {
			finalKeywords.add(k.getValue());
		    }
		}
	    }
	}
	List<String> styles = wmsLayer.getStyleTitles();
	if (styles != null) {
	    for (String style : styles) {
		finalKeywords.add(style);
	    }
	}
	finalKeywords.remove("default");
	finalKeywords.remove("DEFAULT");
	finalKeywords.remove("Default");
	for (String keyword : finalKeywords) {
	    identification.addKeyword(keyword);
	}

	// CONSTRAINTS
	String fees = capabilities.getFees();
	if (fees != null) {
	    identification.setSupplementalInformation("Fees: " + fees);
	}
	String constraints = capabilities.getAccessConstraints();
	if (constraints != null) {
	    identification.addAccessConstraint("otherRestrictions");
	    LegalConstraints legalConstraints = new LegalConstraints();
	    legalConstraints.addOtherConstraints("Access constraints: " + constraints);
	    identification.addLegalConstraints(legalConstraints);
	}

	// BOUNDING BOX
	EnvelopeType envelope84 = wmsLayer.getEnvelope(CRS.OGC_84().getIdentifier());
	if (envelope84 != null) {
	    double west = envelope84.getLowerCorner().getValue().get(0);
	    double south = envelope84.getLowerCorner().getValue().get(1);
	    double east = envelope84.getUpperCorner().getValue().get(0);
	    double north = envelope84.getUpperCorner().getValue().get(1);
	    identification.addGeographicBoundingBox(north, west, south, east);
	} else {
	    EnvelopeType envelope4326 = wmsLayer.getEnvelope(CRS.EPSG_4326().getIdentifier());
	    if (envelope4326 != null) {
		double south = envelope4326.getLowerCorner().getValue().get(0);
		double west = envelope4326.getLowerCorner().getValue().get(1);
		double north = envelope4326.getUpperCorner().getValue().get(0);
		double east = envelope4326.getUpperCorner().getValue().get(1);
		identification.addGeographicBoundingBox(north, west, south, east);
	    }
	}

	// TIME
	Optional<String> timeDimension = wmsLayer.getDimensionAxis("time");

	if (timeDimension.isPresent()) {
	    String tDim = timeDimension.get().trim();

	    String beginPosition;
	    String endPosition;

	    if (tDim.contains(",")) {
		String[] split = tDim.split(",");
		beginPosition = split[0].trim();
		endPosition = split[split.length - 1].trim();
	    } else {
		beginPosition = tDim;
		endPosition = tDim;
	    }
	    if (beginPosition.contains("/")) {
		beginPosition = beginPosition.split("/")[0].trim();
	    }
	    if (endPosition.contains("/")) {
		endPosition = endPosition.split("/")[1].trim();
	    }

	    Optional<Date> beginDateOpt = ISO8601DateTimeUtils.parseISO8601ToDate(beginPosition);
	    Optional<Date> endDateOpt = ISO8601DateTimeUtils.parseISO8601ToDate(endPosition);

	    if (beginDateOpt.isPresent() && endDateOpt.isPresent())

		identification.addTemporalExtent(ISO8601DateTimeUtils.getISO8601DateTime(beginDateOpt.get()),
			ISO8601DateTimeUtils.getISO8601DateTime(endDateOpt.get()));

	} else {

	    if (layer != null) {
		List<Dimension> dimensions = layer.getDimensions();
		for (Dimension d : dimensions) {
		    String dimensionName = d.getValue();
		    if (dimensionName.contains(",")) {
			String[] splittedDimesion = dimensionName.split(",");
			if (splittedDimesion != null && splittedDimesion.length > 1) {
			    String startTime = splittedDimesion[0].split("_")[0];
			    String endTime = splittedDimesion[splittedDimesion.length - 1].split("_")[0];

			    Optional<Date> startDateTime = null;
			    Optional<Date> endDateTime = null;

			    startDateTime = ISO8601DateTimeUtils.parseNotStandardToDate(startTime);
			    endDateTime = ISO8601DateTimeUtils.parseNotStandardToDate(endTime);

			    if (startDateTime.isPresent() && endDateTime.isPresent()) {
				Date beginPosition = startDateTime.get();
				Date endPosition = endDateTime.get();
				identification.addTemporalExtent(ISO8601DateTimeUtils.getISO8601DateTime(beginPosition),
					ISO8601DateTimeUtils.getISO8601DateTime(endPosition));
			    }
			}

		    } else {
			// start time only
			String startTime = dimensionName.split("_")[0];
			Optional<Date> startDateTime = null;
			startDateTime = ISO8601DateTimeUtils.parseNotStandardToDate(startTime);

			if (startDateTime.isPresent()) {
			    Date beginPosition = startDateTime.get();
			    identification.addTemporalExtent(ISO8601DateTimeUtils.getISO8601DateTime(beginPosition),
				    ISO8601DateTimeUtils.getISO8601DateTime(beginPosition));

			}
		    }
		}
	    }
	}

	if (name != null) {

	    // GRAPHIC OVERVIEW

	    String uuid = UUID.randomUUID().toString();
	    Online online = new Online();
	    String mapURL = capabilities.getGetMapOnlineResource();

	    boolean useSourceEndpoint = false;
	    try {
		new URL(mapURL);
	    } catch (Exception e) {
		useSourceEndpoint = true;
	    }

	    if (mapURL.contains("localhost")) {
		useSourceEndpoint = true;
	    }

	    if (!useSourceEndpoint) {
		Optional<Integer> integer = HttpConnectionUtils.getOptionalResponseCode(mapURL);
		if (integer.isPresent()) {
		    String status = "" + integer;
		    // in case of client error, such as 404 not found
		    // we try to recover using the source endpoint
		    if (status.startsWith("4")) {
			useSourceEndpoint = true;
		    }
		} else {
		    // or even if no response provided
		    useSourceEndpoint = true;
		}
	    }
	    if (useSourceEndpoint) {
		mapURL = sourceEndpoint;
	    }

	    if (!mapURL.endsWith("?") && !mapURL.endsWith("&")) {

		if (!mapURL.contains("?")) {
		    mapURL = mapURL + "?";
		} else {
		    mapURL = mapURL + "&";
		}
	    }

	    online.setProtocol(NetProtocolWrapper.WMS_1_3_0.getCommonURN());
	    online.setLinkage(mapURL);
	    online.setName(wmsLayer.getName());
	    online.setFunctionCode("download");
	    online.setIdentifier(uuid);

	    Distribution distribution = new Distribution();
	    distribution.addDistributionOnline(online);
	    metadata.setDistribution(distribution);

	    if (wmsDownloader != null) {
		wmsDownloader.setOnlineResource(ret, uuid);
		// preset the capabilities document in the downloader connector, as it is no need to re-download the
		// capabilities document to generate the preview link
		WMS_1_3_0Connector downloaderConnector = wmsDownloader.getConnector();
		downloaderConnector.setCapabilities(capabilities);
		DataDescriptor[] previewDescriptors = new DataDescriptor[] {};

		try {
		    previewDescriptors = wmsDownloader.getPreviewRemoteDescriptors().toArray(new DataDescriptor[] {});
		} catch (Exception e) {

		    GSLoggerFactory.getLogger(getClass()).warn("Can't get previewe descriptors: " + e.getMessage(), e);
		}

		if (previewDescriptors.length > 0) {

		    Arrays.sort(previewDescriptors, (o1, o2) -> {

			Integer p1 = getPoints(o1);
			Integer p2 = getPoints(o2);

			return p1.compareTo(p2);
		    });

		    DataDescriptor previewDescriptor = previewDescriptors[0];

		    fixTime(previewDescriptor);

		    URL url = wmsDownloader.getImageURL(previewDescriptor, mapURL);

		    metadata.getDataIdentification().addGraphicOverview(getGraphicOverview(url));
		}
	    }
	}

	return ret;

    }

    private void fixTime(DataDescriptor desc) {

	DataDimension dataDimension = desc.getTemporalDimension();
	if (dataDimension != null) {
	    if (dataDimension instanceof ContinueDimension) {
		ContinueDimension sizedDimension = dataDimension.getContinueDimension();
		sizedDimension.setLower(sizedDimension.getUpper());
	    } else {
		FiniteDimension discreteDimension = dataDimension.getFiniteDimension();
		int size = discreteDimension.getPoints().size();
		List<String> subList = discreteDimension.getPoints().subList(size - 1, size);
		discreteDimension.setPoints(subList);
	    }
	}
    }

    protected Integer getPoints(DataDescriptor o1) {
	CRS crs1 = o1.getCRS();
	DataFormat format1 = o1.getDataFormat();
	if (crs1.equals(CRS.EPSG_4326()) || crs1.equals(CRS.OGC_84())) {
	    if (format1.equals(DataFormat.IMAGE_PNG())) {
		return -6;
	    }
	    if (format1.equals(DataFormat.IMAGE_JPG())) {
		return -5;
	    }
	    if (format1.equals(DataFormat.IMAGE_GEOTIFF())) {
		return -4;
	    }
	    return -3;
	} else {
	    if (format1.equals(DataFormat.IMAGE_PNG())) {
		return 1;
	    }
	    if (format1.equals(DataFormat.IMAGE_JPG())) {
		return 2;
	    }
	    if (format1.equals(DataFormat.IMAGE_GEOTIFF())) {
		return 3;
	    }
	    return 4;
	}
    }

    static Integer getStaticPoints(DataDescriptor o1) {
	CRS crs1 = o1.getCRS();
	DataFormat format1 = o1.getDataFormat();
	if (crs1.equals(CRS.EPSG_4326()) || crs1.equals(CRS.OGC_84())) {
	    if (format1.equals(DataFormat.IMAGE_PNG())) {
		return -6;
	    }
	    if (format1.equals(DataFormat.IMAGE_JPG())) {
		return -5;
	    }
	    if (format1.equals(DataFormat.IMAGE_GEOTIFF())) {
		return -4;
	    }
	    return -3;
	} else {
	    if (format1.equals(DataFormat.IMAGE_PNG())) {
		return 1;
	    }
	    if (format1.equals(DataFormat.IMAGE_JPG())) {
		return 2;
	    }
	    if (format1.equals(DataFormat.IMAGE_GEOTIFF())) {
		return 3;
	    }
	    return 4;
	}
    }

    private BrowseGraphic getGraphicOverview(URL url) {
	BrowseGraphic graphic = new BrowseGraphic();
	graphic.setFileDescription(url.getPath());
	graphic.setFileName(url.toString());
	graphic.setFileType("image/png");
	return graphic;
    }

}
