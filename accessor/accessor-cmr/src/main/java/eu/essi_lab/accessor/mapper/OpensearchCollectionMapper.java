package eu.essi_lab.accessor.mapper;

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

import static eu.essi_lab.iso.datamodel.classes.Identification.REVISION;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import eu.essi_lab.accessor.cmr.harvested.CMRIDNOpensearchCollectionMapper;
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

/**
 * @author ilsanto
 */
public abstract class OpensearchCollectionMapper extends FileIdentifierMapper {

    // public static final String SCHEMA_URI = "http://essi-lab.eu/cmr/collections/cwic";
    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    private static final String CANT_READ_GRANULE = "Exception reading original atom granule";
    private static final String CMRGRANULES_MAPPER_ORIGINAL_MD_READ_ERROR = "CMRGRANULES_MAPPER_ORIGINAL_MD_READ_ERROR";
    private static final String IDENTIFIER_XPATH = "//*:identifier";
    private static final String TITLE_XPATH = "//*:title";
    private static final String REVISION_XPATH = "//*:updated";
    private static final String ABSTRACT_XPATH = "//*:summary";
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
    private static final String VERSION_XPATH = "//*:versionId";

    public static int COLLECTIONS_WITH_GRANULES_COUNT = 0;

    
    protected transient Downloader downloader;

    public Downloader getDownloader() {
	return downloader == null ? new Downloader() : downloader;
    }

    // Sub-classes (IDn and CWIC) must implement
    protected abstract boolean skipCwic();

    // Sub-classes (IDn and CWIC) must implement
    protected abstract String getGranulesBaseURL();

    /**
     * @param reader
     * @return
     */
    protected int getGranulesCount(XMLDocumentReader reader) {

	int count = 0;
	Optional<String> dataCenter = read(reader, DATACENTER_XPATH);
	Optional<String> shortName = read(reader, SHORT_NAME_XPATH);
	Optional<String> version = read(reader, VERSION_XPATH);

	if (dataCenter.isPresent()) {

	    String url = getGranulesBaseURL() + "dataCenter=" + dataCenter.get();

	    if (shortName.isPresent()) {

		try {
		    String encodedName = URLEncoder.encode(shortName.get(), "UTF-8");
		    url = url + "&shortName=" + encodedName;

		} catch (UnsupportedEncodingException e) {
		}
	    }

	    // if(version.isPresent()) {
	    // if(version.get().contains(" ") || version.get().contains("Not provided") ||
	    // version.get().contains("Latest Version")) {
	    // url = url + "&versionId=";
	    // } else {
	    // url = url + "&versionId=" + version.get();
	    // }
	    // }
	    url = url + "&offset=0&numberOfResults=1&clientId=gs-service";

	    try {
		String result = getDownloader().downloadOptionalString(url).orElse(null);

		XMLDocumentReader rr = new XMLDocumentReader(result);

		String countString = rr.evaluateNode("//*:totalResults").getTextContent();

		count = Integer.parseInt(countString);

		return count;

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(CMRIDNOpensearchCollectionMapper.class).error("Get granules count error: {}", e.getMessage());
	    }
	}

	return count;
    }

    @Override

    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {
	// TODO Auto-generated method stub
	// return super.execMapping(originalMD, source);

	XMLDocumentReader reader;
	try {

	    reader = new XMLDocumentReader(new ByteArrayInputStream(originalMD.getMetadata().getBytes(StandardCharsets.UTF_8)));

	    reader.setNamespaceContext(new CommonNameSpaceContext());

	} catch (SAXException | IOException e) {

	    logger.error(CANT_READ_GRANULE, e);

	    throw GSException.createException(getClass(), CANT_READ_GRANULE, null, null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, CMRGRANULES_MAPPER_ORIGINAL_MD_READ_ERROR, e);

	}

	// first: check if isCwic
	Set<String> keywords = new HashSet<>();

	Optional<List<String>> tags = readListString(reader, TAGKEY_XPATH);

	if (tags.isPresent()) {
	    List<String> tagList = tags.get();
	    for (String s : tagList) {
		if (s.toLowerCase().contains("org.ceos.wgiss.cwic") && skipCwic()) {
		    return null;
		}
		keywords.add(s);
		if (s.contains("geoss_data-core")) {
		    keywords.add("geossDataCore");
		}
	    }
	}

	int count = getGranulesCount(reader);

	if (count > 0)
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
		    GSLoggerFactory.getLogger(OpensearchCollectionMapper.class).error("Get Polygon Bbox Error: {}", e.getMessage());
		}
	    });
	}

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

	if (collectionTitle.isPresent() && !collectionTitle.get().isEmpty()) {
	    dataIdentification.setCitationTitle(collectionTitle.get());
	} else {
	    dataIdentification.setCitationTitle(read(reader, IDENTIFIER_XPATH).orElse(title));
	}

	Optional<String> abs = read(reader, ABSTRACT_XPATH);

	if (abs.isPresent() && !abs.get().isEmpty()) {
	    abs.ifPresent(dataIdentification::setAbstract);
	} else {
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
		if (url != null && !url.isEmpty()) {
		    if (urls.contains(url))
			continue;
		    urls.add(url);
		    Online online = new Online();
		    online.setLinkage(url);
		    online.setDescription(t);
		    miMetadata.getDistribution().addDistributionOnline(online);

		}

		// possibly other keywords

		if (t.contains("Product metadata")) {
		    try {
			String conceptXml = getDownloader().downloadOptionalString(url).orElse(null);

			XMLDocumentReader readerConcept = new XMLDocumentReader(conceptXml);

			List<String> keywordsNodes = getKeywordsFromConceptXMLDocument(readerConcept);

			keywordsNodes.forEach(key -> keywords.add(key));

		    } catch (Exception e) {
			GSLoggerFactory.getLogger(OpensearchCollectionMapper.class).error("Get concepts XML error: {}", e.getMessage());
		    }
		}
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

    }

    private List<String> getKeywordsFromConceptXMLDocument(XMLDocumentReader readerConcept) {
	List<String> keywords = new ArrayList<String>();

	try {
	    Node[] nodes = readerConcept.evaluateNodes("//*:ScienceKeyword");
	    for (Node keywordNode : nodes) {
		NodeList listNode = keywordNode.getChildNodes();
		String keyword = "";

		for (int j = 0; j < listNode.getLength(); j++) {

		    Node element = listNode.item(j);

		    if (element.getNodeType() == Node.ELEMENT_NODE) {
			String text = element.getTextContent().replaceAll("[\\n]", "").trim() + " > ";
			keyword = keyword + text;

		    }
		}
		if (!keyword.isEmpty()) {
		    keyword = keyword.substring(0, keyword.length() - 3);
		    keywords.add(keyword);
		}
	    }

	    if (keywords.isEmpty()) {
		Node[] alternativeNodes = readerConcept.evaluateNodes("//*:Science_Keywords");
		for (Node keywordNode : alternativeNodes) {
		    NodeList listNode = keywordNode.getChildNodes();
		    String keyword = "";

		    for (int j = 0; j < listNode.getLength(); j++) {

			Node element = listNode.item(j);

			if (element.getNodeType() == Node.ELEMENT_NODE) {
			    String text = element.getTextContent().replaceAll("[\\n]", "").trim() + " > ";
			    keyword = keyword + text;

			}
		    }
		    if (!keyword.isEmpty()) {
			keyword = keyword.substring(0, keyword.length() - 3);
			keywords.add(keyword);
		    }
		}
	    }

	    if (keywords.isEmpty()) {
		Node[] alternativeNodes = readerConcept.evaluateNodes("//*:keyword");
		for (Node keywordNode : alternativeNodes) {
		    NodeList listNode = keywordNode.getChildNodes();
		    String keyword = "";

		    for (int j = 0; j < listNode.getLength(); j++) {

			Node element = listNode.item(j);

			if (element.getNodeType() == Node.ELEMENT_NODE) {
			    keyword = element.getTextContent();

			}
		    }
		    if (!keyword.isEmpty()) {
			keywords.add(keyword);
		    }
		}
	    }

	    Optional<List<String>> isoTopicCategory = readListString(readerConcept, "//*:ISO_Topic_Category");
	    Optional<List<String>> ancillaryKeyword = readListString(readerConcept, "//*:Ancillary_Keyword");

	    isoTopicCategory.ifPresent(topic -> {
		for (String s : topic) {
		    keywords.add(s);
		}
	    });

	    ancillaryKeyword.ifPresent(ancillary -> {
		for (String s : ancillary) {
		    keywords.add(s);
		}
	    });

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(OpensearchCollectionMapper.class).error("Get keywords from concept XML error: {}", e.getMessage());
	}
	return keywords;
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

    public static void main(String[] args) throws Exception {
	String url = "https://cmr.earthdata.nasa.gov/search/concepts/C179014694-NSIDC_ECS.xml";
	Downloader d = new Downloader();
	List<String> keywords = new ArrayList<String>();
	Optional<String> res = d.downloadOptionalString(url);
	if (res.isPresent()) {
	    XMLDocumentReader xdoc = new XMLDocumentReader(res.get());
	    Node[] alternativeNodes = xdoc.evaluateNodes("//*:keyword");
	    for (Node keywordNode : alternativeNodes) {
		NodeList listNode = keywordNode.getChildNodes();
		String keyword = "";

		for (int j = 0; j < listNode.getLength(); j++) {

		    Node element = listNode.item(j);

		    if (element.getNodeType() == Node.ELEMENT_NODE) {
			keyword = element.getTextContent();
		    }
		}
		if (!keyword.isEmpty()) {
		    keywords.add(keyword);
		}
	    }
	}

    }

}
