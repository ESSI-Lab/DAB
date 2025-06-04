package eu.essi_lab.accessor.nextgeoss.harvested;

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

import static eu.essi_lab.iso.datamodel.classes.Identification.REVISION;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.BrowseGraphic;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.DatasetCollection;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.FileIdentifierMapper;

public class NextGEOSSCollectionMapper extends FileIdentifierMapper {

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());
    protected static final String DEFAULT_CLIENT_ID = "gs-service";
    public static final String SCHEMA_URI = "http://essi-lab.eu/nextgeoss/collections";

    public static final String NEXTGEOSS_SECOND_LEVEL_TEMPLATE = "nextgeossSecondLevel";
    
    private static final String CANT_READ_GRANULE = "Exception reading original atom granule";
    private static final String NEXTGEOSS_GRANULES_MAPPER_ORIGINAL_MD_READ_ERROR = "NEXTGEOSS_GRANULES_MAPPER_ORIGINAL_MD_READ_ERROR";
    private static final String IDENTIFIER_XPATH = "//*:identifier";
    private static final String TITLE_XPATH = "//*:title";
    private static final String REVISION_XPATH = "//*:updated";
    private static final String PUBLISHED_XPATH = "//*:published";
    private static final String ABSTRACT_XPATH = "//*:summary";
    private static final String CONTENT_XPATH = "//*:content";
    private static final String THUMBNAIL_XPATH = "//*:link[@rel='icon']/@href";
    private static final String BBOX_XPATH = "//georss:box";
    private static final String BBOX_POINT_XPATH = "//georss:point";
    private static final String BBOX_POLYGON_XPATH = "//georss:polygon";
    private static final String DIRECT_DOWNLOAD_XPATH = "//*:link[@rel='enclosure']/@href";
    private static final String DIRECT_DOWNLOAD_XPATH_TITLE = "//*:link[@rel='enclosure']/@title";
    private static final String TEMPRAL_EXTENT_XPATH = "//*:date";
    private static final String LINKS_XPATH = "//*:link";
    private static final String SHORT_NAME_XPATH = "//*:shortName";
    private static final String DATACENTER_XPATH = "//*:dataCenter";
    private static final String ARCHIVECENTER_XPATH = "//*:archiveCenter";
    private static final String ORGANIZATION_XPATH = "//*:organization";
    private static final String TAGKEY_XPATH = "//*:tagKey/text()";
    private static final String AUTHOR_NAME_XPATH = "//*:author/*:name";
    private static final String AUTHOR_EMAIL_XPATH = "//*:author/*:email";
    
    public static int COLLECTIONS_WITH_GRANULES_COUNT = 0;
    
    public final static String NEXTGEOSS_BASE_TEMPLATE_URL = "https://catalogue.nextgeoss.eu/opensearch/search.atom?";
    
    
    
    protected transient Downloader downloader;

    public Downloader getDownloader() {
	return downloader == null ? new Downloader() : downloader;
    }

    @Override
    public GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {
	
	XMLDocumentReader reader;
	try {

	    reader = new XMLDocumentReader(new ByteArrayInputStream(originalMD.getMetadata().getBytes(StandardCharsets.UTF_8)));

	    reader.setNamespaceContext(new CommonNameSpaceContext());

	} catch (SAXException | IOException e) {

	    logger.error(CANT_READ_GRANULE, e);

	    throw GSException.createException(getClass(), CANT_READ_GRANULE, null, null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, NEXTGEOSS_GRANULES_MAPPER_ORIGINAL_MD_READ_ERROR, e);

	}

	// first: check if isCwic
	Set<String> keywords = new HashSet<>();

	Optional<List<String>> tags = readListString(reader, TAGKEY_XPATH);

//	if (tags.isPresent()) {
//	    List<String> tagList = tags.get();
//	    for (String s : tagList) {
//		if (s.toLowerCase().contains("org.ceos.wgiss.cwic") && skipCwic()) {
//		    return null;
//		}
//		keywords.add(s);
//		if (s.contains("geoss_data-core")) {
//		    keywords.add("geossDataCore");
//		}
//	    }
//	}

	int count = getGranulesCount(reader);
	
	if(count > 0)
	    COLLECTIONS_WITH_GRANULES_COUNT++;

	GSResource dataset = new DatasetCollection();

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	MIMetadata miMetadata = coreMetadata.getMIMetadata();
	miMetadata.setHierarchyLevelName("series");
	miMetadata.addHierarchyLevelScopeCodeListValue("series");
	if (count == 0) {

	    // dataset.getHarmonizedMetadata().getCoreMetadata().setTitle("HAS-LOWEST-RANKING");

	    //
	    // lowest ranking
	    //
	    dataset.getPropertyHandler().setLowestRanking();
	}

	miMetadata.setLanguage("eng");

	dataset.setSource(source);

	DataIdentification dataIdentification = miMetadata.getDataIdentification();

	Optional<String> bbox = read(reader, BBOX_XPATH);

	Optional<String> bboxPoint = read(reader, BBOX_POINT_XPATH);

	if (bbox.isPresent() && (!bbox.get().isEmpty())) {

	    bbox.ifPresent(b -> {

		String[] bsplit = b.split(" ");

		if (bsplit.length != 4) {

		    logger.warn("Found unrecognized bbox {}", b);
		    return;
		}

		/**
		 * <georss:box>-90 -180 90 180</georss:box>
		 */

		dataIdentification.addGeographicBoundingBox("Collection extent", Double.valueOf(bsplit[2]), Double.valueOf(bsplit[1]),
			Double.valueOf(bsplit[0]), Double.valueOf(bsplit[3]));
	    });
	} else if (bboxPoint.isPresent() && (!bboxPoint.get().isEmpty())) {

	    bboxPoint.ifPresent(b -> {

		String[] bsplit = b.split(" ");

		if (bsplit.length != 2) {

		    logger.warn("Found unrecognized bbox {}", b);
		    return;
		}

		/**
		 * <georss:box>-90 -180 90 180</georss:box>
		 */

		dataIdentification.addGeographicBoundingBox("Collection extent", Double.valueOf(bsplit[0]), Double.valueOf(bsplit[1]),
			Double.valueOf(bsplit[0]), Double.valueOf(bsplit[1]));
	    });
	} else {
	    Optional<String> bboxPolygon = read(reader, BBOX_POLYGON_XPATH);
	    bboxPolygon.ifPresent(b -> {
		// String[] bsplit = b.split(" ");
		try {
		    String res = toBBOX(b, true);
		    double west = Double.valueOf(res.split(" ")[1]);
		    double east = Double.valueOf(res.split(" ")[3]);
		    double north = Double.valueOf(res.split(" ")[2]);
		    double south = Double.valueOf(res.split(" ")[0]);
		    dataIdentification.addGeographicBoundingBox("Collection extent", north, west, south, east);
		} catch (Exception e) {
		    GSLoggerFactory.getLogger(NextGEOSSCollectionMapper.class).error("Get Polygon Bbox Error");
		    GSLoggerFactory.getLogger(NextGEOSSCollectionMapper.class).error(e.getMessage(), e);
		}
	    });
	}
	
	
	//readString(json, PUBLISHED_KEY).ifPresent(date -> miMetadata.getDataIdentification().setCitationPublicationDate(date));
	Optional<String> published = read(reader, PUBLISHED_XPATH);
	
	published.ifPresent(dataIdentification::setCitationPublicationDate);
	Optional<String> updated = read(reader, REVISION_XPATH);

	updated.ifPresent(up -> {
	    try {
		if (!(up.isEmpty() || " ".equals(up) || "".equals(up))) {
		    String time = ISO8601DateTimeUtils.getISO8601DateTime(ISO8601DateTimeUtils.parseISO8601(up));
		    dataIdentification.addCitationDate(time, REVISION);
		}
	    } catch (Exception e) {
		logger.warn("Invalid format: \"\" {}", up, e);
	    }
	});

	String title = "Collection Title";

	Optional<String> collectionTitle = read(reader, TITLE_XPATH);
	
	if(collectionTitle.isPresent() && !collectionTitle.get().isEmpty()) {
	    dataIdentification.setCitationTitle(collectionTitle.get());
	} else {
	    dataIdentification.setCitationTitle(read(reader, IDENTIFIER_XPATH).orElse(title));
	}	

	Optional<String> abs = read(reader, CONTENT_XPATH);

	if(abs.isPresent() && !abs.get().isEmpty()) {
	    abs.ifPresent(dataIdentification::setAbstract);
	}else {
	    dataIdentification.setAbstract("Description metadata for " + dataIdentification.getCitationTitle());
	}
	
	
	/**
	 * Optional<String> download = read(reader, DIRECT_DOWNLOAD_XPATH);
	 * download.ifPresent(d -> {
	 * Online online = new Online();
	 * online.setLinkage(d);
	 * // online.setProtocol();
	 * online.setName("");
	 * miMetadata.getDistribution().addDistributionOnline(online);
	 * });
	 */

	/*
	 * KEYWORDS
	 * identifier, shortName, dataCenter, archiveCenter, organization, tags
	 */

	Optional<String> shortName = read(reader, SHORT_NAME_XPATH);
	Optional<String> dataCenter = read(reader, DATACENTER_XPATH);
	Optional<String> archiveCenter = read(reader, ARCHIVECENTER_XPATH);
	Optional<String> organization = read(reader, ORGANIZATION_XPATH);

	shortName.ifPresent(keywords::add);
	dataCenter.ifPresent(keywords::add);
	archiveCenter.ifPresent(keywords::add);
	organization.ifPresent(keywords::add);

	Optional<String> author = read(reader, AUTHOR_NAME_XPATH);

	Optional<String> mail = read(reader, AUTHOR_EMAIL_XPATH);

	ResponsibleParty responsible = null;

	if (author.isPresent()) {
	    responsible = new ResponsibleParty();
	    responsible.setRoleCode("pointOfContact");
	    responsible.setIndividualName(author.get());
	    organization.ifPresent(responsible::setOrganisationName);
	}
	Contact contact = new Contact();
	if (mail.isPresent()) {
	    contact = new Contact();
	    Address otherAddress = new Address();
	    otherAddress.addElectronicMailAddress(mail.get());
	    contact.setAddress(otherAddress);
	}

	if (responsible != null) {
	    responsible.setContactInfo(contact);
	    dataIdentification.addPointOfContact(responsible);
	}

	Optional<Node[]> downloads = readDoc(reader, LINKS_XPATH);

	downloads.ifPresent(d -> {
	    Set<String> urls = new HashSet<String>();
	    for (Node n : d) {
		NamedNodeMap attributes = n.getAttributes();
		String url = attributes.getNamedItem("href").getTextContent();
		String t = "Online resource for Collection " + read(reader, TITLE_XPATH).orElse(dataIdentification.getCitationTitle());
		if (attributes.getNamedItem("title") != null) {
		    t = attributes.getNamedItem("title").getTextContent();
		}
		if (url != null && !url.isEmpty() && !url.contains("example.com")) {
		    if (urls.contains(url))
			continue;
		    urls.add(url);
		    Online online = new Online();
		    online.setLinkage(url);
		    online.setDescription(t);
		    miMetadata.getDistribution().addDistributionOnline(online);

		}

		// possibly other keywords
		
//		if (t.contains("Product metadata")) {
//		    try {
//			String conceptXml = getDownloader().downloadString(url).orElse(null);
//
//			XMLDocumentReader readerConcept = new XMLDocumentReader(conceptXml);
//
//			//List<String> keywordsNodes = getKeywordsFromConceptXMLDocument(readerConcept);
//
//			//keywordsNodes.forEach(key -> keywords.add(key));
//
//		    } catch (Exception e) {
//			GSLoggerFactory.getLogger(NextGEOSSCollectionMapper.class).error("Get Number of Records Error");
//			GSLoggerFactory.getLogger(NextGEOSSCollectionMapper.class).error(e.getMessage(), e);
//		    }
//
//		}

	    }

	});

	keywords.forEach(k -> dataIdentification.addKeyword(k));

	Optional<String> preview = read(reader, THUMBNAIL_XPATH);

	preview.ifPresent(p -> dataIdentification.addGraphicOverview(createGraphicOverview(p)));

	Optional<String> temporalExtent = read(reader, TEMPRAL_EXTENT_XPATH);

	temporalExtent.ifPresent(t -> {

	    String[] tsplit = t.split("/");

	    if (tsplit.length != 2) {

		logger.warn("Found unrecognized time extent {}", t);
		return;
	    }

	    String start = ISO8601DateTimeUtils.getISO8601DateTime(ISO8601DateTimeUtils.parseISO8601(tsplit[0]));

	    String end = ISO8601DateTimeUtils.getISO8601DateTime(ISO8601DateTimeUtils.parseISO8601(tsplit[1]));

	    TemporalExtent tempExtent = new TemporalExtent();

	    tempExtent.setBeginPosition(start);

	    tempExtent.setEndPosition(end);

	    dataIdentification.addTemporalExtent(tempExtent);
	});

	//

	return dataset;

//	GMDResourceMapper gmdResourceMapper = new GMDResourceMapper();
//
//	FEDEOOriginalMDWrapper wrapper = new FEDEOOriginalMDWrapper();
//
//	GSResource gmiMapped = gmdResourceMapper.map(wrapper.getOriginalMetadata(originalMD), source);
//
//	try {
//
//	    enrichWithSecondLevelUrl(gmiMapped, wrapper, originalMD);
//
//	} catch (GSException e) {
//
//	    logger.warn("Unable to add FEDEO OSDD extension element to collection {}, this collection will not be expandible",
//		    gmiMapped.getPublicId());
//
//	    DefaultGSExceptionLogger.log(new DefaultGSExceptionHandler(new DefaultGSExceptionReader(e)));
//	}
//
//	return gmiMapped;

    }

//    public void enrichWithSecondLevelUrl(GSResource gmiMapped, FEDEOOriginalMDWrapper wrapper, OriginalMetadata originalMD)
//	    throws GSException {
//
//	try {
//	    // restore original metadata
//	    // try to open collection
//
//	    XMLDocumentReader xdocReader = new XMLDocumentReader(originalMD.getMetadata());
//
//	    String remoteId = xdocReader.evaluateString("//*:link[@type='application/opensearchdescription+xml']/@href");
//
//	    if (remoteId != null && !remoteId.isEmpty()) {
//
//		String baseURL = gmiMapped.getSource().getEndpoint();
//		baseURL = baseURL.endsWith("?") ? baseURL : baseURL + "?";
//		baseURL = baseURL + NextGEOSSGranulesConnector.FEDEO_REQUEST + "parentIdentifier="
//			+ URLEncoder.encode(gmiMapped.getOriginalId().get().trim(), "UTF-8") + "&";
//
//		Downloader down = new Downloader();
//
//		String result = down.downloadString(baseURL).orElse(null);
//
//		XMLDocumentReader reader = new XMLDocumentReader(result);
//
//		int totRes = reader.evaluateNumber("//*:totalResults").intValue();
//
//		if (totRes > 0 || reader.evaluateBoolean("//*:entry")) {
//
//		    gmiMapped.getHarmonizedMetadata().getExtendedMetadata().add(NEXTGEOSS_SECOND_LEVEL_TEMPLATE,
//			    gmiMapped.getOriginalId().get().trim());
//
//		}
//
//	    }
//	    // FEDEO_COLLECTION_PREFIX
//	    // + (remoteId == null ? FEDEO_NON_QUERYABLE_COLLECTION_PREFIX + UUID.randomUUID().toString() :
//	    // xmlEncode(remoteId));
//
//	} catch (ParserConfigurationException | XPathExpressionException | SAXException | IOException e) {
//
//	    logger.warn("Unable to add FEDEO extension  element to collection {}, this collection will not be expandible",
//		    gmiMapped.getPublicId(), e);
//
//	}
//
//    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return SCHEMA_URI;
    }
    
    private Optional<List<String>> readListString(XMLDocumentReader reader, String xpath) {
   	try {

   	    return Optional.ofNullable(reader.evaluateTextContent(xpath));

   	} catch (XPathExpressionException e) {
   	    logger.warn("Can't evalueate xpath {}", xpath, e);
   	}

   	return Optional.empty();
       }

       protected Optional<String> read(XMLDocumentReader reader, String xpath) {

   	try {

   	    return Optional.ofNullable(reader.evaluateString(xpath));

   	} catch (XPathExpressionException e) {
   	    logger.warn("Can't evalueate xpath {}", xpath, e);
   	}

   	return Optional.empty();

       }

       private Optional<Node[]> readDoc(XMLDocumentReader reader, String xpath) {

   	try {

   	    return Optional.ofNullable(reader.evaluateNodes(xpath));

   	} catch (XPathExpressionException e) {
   	    logger.warn("Can't evalueate xpath {}", xpath, e);
   	}

   	return Optional.empty();

       }

       private BrowseGraphic createGraphicOverview(String url) {

   	BrowseGraphic graphic = new BrowseGraphic();

   	graphic.setFileName(url);
   	graphic.setFileType("image/png");

   	return graphic;
       }

       /**
        * @param polygon
        * @param firstIsLat
        * @return
        */
       public static String toBBOX(String polygon, boolean firstIsLat) {

   	String[] coords = polygon.split(" ");

   	Double[] lats = new Double[coords.length / 2];
   	Double[] lons = new Double[coords.length / 2];

   	for (int i = 0; i < coords.length; i++) {

   	    if (i % 2 == 0) {

   		if (firstIsLat) {
   		    lats[i / 2] = Double.valueOf(coords[i]);
   		} else {
   		    lons[i / 2] = Double.valueOf(coords[i]);
   		}
   	    } else {

   		if (!firstIsLat) {
   		    lats[i / 2] = Double.valueOf(coords[i]);
   		} else {
   		    lons[i / 2] = Double.valueOf(coords[i]);
   		}
   	    }
   	}

   	Double minLat = lats[0];
   	Double maxLat = lats[0];
   	Double minLon = lons[0];
   	Double maxLon = lons[0];

   	for (int i = 1; i < lats.length; i++) {

   	    if (lats[i] < minLat) {
   		minLat = lats[i];
   	    }
   	    if (lats[i] > maxLat) {
   		maxLat = lats[i];
   	    }

   	    if (lons[i] < minLon) {
   		minLon = lons[i];
   	    }
   	    if (lons[i] > maxLon) {
   		maxLon = lons[i];
   	    }
   	}

   	return minLat + " " + minLon + " " + maxLat + " " + maxLon;
       }
       

       private int getGranulesCount(XMLDocumentReader reader) {

   	int count = 0;
   	Optional<String> identifier = read(reader, IDENTIFIER_XPATH);

   	if (identifier.isPresent()) {

   	    String url = NEXTGEOSS_BASE_TEMPLATE_URL + "datasetId=" + identifier.get() + "&rows=1&start_index=1";

   	    try {
   		String result = getDownloader().downloadOptionalString(url).orElse(null);

   		XMLDocumentReader rr = new XMLDocumentReader(result);

   		String countString = rr.evaluateNode("//*:totalResults").getTextContent();

   		count = Integer.parseInt(countString);

   		return count;

   	    } catch (Exception e) {
   		GSLoggerFactory.getLogger(NextGEOSSCollectionMapper.class).error("Get Number of Records Error");
   		GSLoggerFactory.getLogger(NextGEOSSCollectionMapper.class).error(e.getMessage(), e);
   	    }

   	}

   	return count;

       }

       public static void main(String[] args) throws Exception {
	   String req = "http://gs-service-production.geodab.eu/gs-service/services/essi/view/geoss/opensearch/query?si=1&ct=1&sources=nextGEOSSID";
	   Downloader d = new Downloader();
	   
	   Optional<String> results = d.downloadOptionalString(req);
	   //http://gs-service-production.geodab.eu/gs-service/services/essi/view/geoss/opensearch/query?si=1&ct=10&st=&kwd=&frmt=&prot=&kwdOrBbox=&sscScore=&instrumentTitle=&platformTitle=
	   //&attributeTitle=&organisationName=&searchFields=&bbox=&rel=&tf=providerID,keyword,format,protocol&ts=&te=&targetId=&from=&until=&sources=nextGEOSSID&parents=4072dfdf-ad34-420b-ae6d-4d9b029dd7a9&subj=&rela=
	   int count = 0;
	   if(results.isPresent()) {
	       XMLDocumentReader xdoc = new XMLDocumentReader(results.get());
	       String sCount = xdoc.evaluateString("//*:totalResults");
	       count = Integer.parseInt(sCount);
	   }
	   
	   List<String> arrayIds = new ArrayList<String>();
	   if(count > 0) {
	    String urlPlatform = "http://gs-service-production.geodab.eu/gs-service/services/essi/view/geoss/opensearch/query?sources=nextGEOSSID";
	    int startIndex = 1;
	    while(startIndex < count) {
		String getRequests = urlPlatform  + "&si=" + startIndex + "&ct=10";
		String feeds = d.downloadOptionalString(getRequests).orElse(null);
		if(feeds != null) {
		    XMLDocumentReader feedDoc = new XMLDocumentReader(feeds);
		    List<Node> nodeList = feedDoc.evaluateOriginalNodesList("//*:entry/*:id");
		    for(Node n : nodeList) {
			String id = n.getTextContent();
			arrayIds.add(id);
		    }
		}
		    
		startIndex = startIndex + 10;
	    }
	       
	       
	   }
	   int granuleCount = 0;
	   for(String ids : arrayIds) {
	       String granuleRequest = "http://gs-service-production.geodab.eu/gs-service/services/essi/view/geoss/opensearch/query?si=1&ct=1&sources=nextGEOSSID&" + "parents=" + ids;
	       String granules = d.downloadOptionalString(granuleRequest).orElse(null);
	       if(granules != null) {
		   XMLDocumentReader granuleDoc = new XMLDocumentReader(granules);
		   String sGranuleCount = granuleDoc.evaluateString("//*:totalResults");
		   int partialCount = Integer.parseInt(sGranuleCount);
		   System.out.println(partialCount);
		   granuleCount = granuleCount + partialCount;
	       }
	   }
	     
	   System.out.println(arrayIds.size());
	   System.out.println(Arrays.toString(arrayIds.toArray()));
	   
       }
       
}
