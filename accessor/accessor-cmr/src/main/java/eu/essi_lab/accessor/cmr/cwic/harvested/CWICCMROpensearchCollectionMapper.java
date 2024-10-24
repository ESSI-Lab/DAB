package eu.essi_lab.accessor.cmr.cwic.harvested;

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

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import eu.essi_lab.accessor.mapper.OpensearchCollectionMapper;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author roncella
 */
public class CWICCMROpensearchCollectionMapper extends OpensearchCollectionMapper {

    public static final String SCHEMA_URI = "http://essi-lab.eu/cmr/collections/cwic";
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
    public final static String CWIC_BASE_TEMPLATE_URL = "https://cmr.earthdata.nasa.gov/opensearch/granules.atom?";

    protected Downloader downloader;

    public Downloader getDownloader() {

	if (downloader == null) {

	    downloader = new Downloader();
	    downloader.setConnectionTimeout(TimeUnit.SECONDS, 15);
	}

	return downloader;
    }

    @Override
    protected boolean skipCwic() {
	return false;
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CWICCMROpensearchCollectionMapper.SCHEMA_URI;
    }

    @Override
    protected String getGranulesBaseURL() {
	return CWIC_BASE_TEMPLATE_URL;
    }


    // @Override
    // protected int getGranulesCount(XMLDocumentReader reader) {
    //
    // int count = 0;
    // Optional<String> identifier = read(reader, IDENTIFIER_XPATH);
    //
    // if (identifier.isPresent()) {
    //
    // String url = getGranulesBaseURL() + "datasetId=" + identifier.get() +
    // "&startPage=1&count=1&isCwic=true&clientId=gs-service";
    //
    // try {
    // String result = getDownloader().downloadString(url).orElse(null);
    //
    // XMLDocumentReader rr = new XMLDocumentReader(result);
    //
    // String countString = rr.evaluateNode("//*:totalResults").getTextContent();
    //
    // count = Integer.parseInt(countString);
    //
    // return count;
    //
    // } catch (Exception e) {
    //
    // GSLoggerFactory.getLogger(OpensearchCollectionMapper.class).error("Get granules count error: {}",
    // e.getMessage());
    // }
    // }
    //
    // return count;
    //
    // }

    // @Override
    // void enrichWithSecondLevelUrl(GSResource gmiMapped, CMROriginalMDWrapper wrapper, OriginalMetadata originalMD)
    // throws GSException {
    //
    // try {
    //
    // CWICCMRCollectionAtomEntry cmrCollectionAtomEntry = new CWICCMRCollectionAtomEntry(wrapper.getUrl(originalMD));
    //
    // InputStream stream = cmrCollectionAtomEntry.getCollectionAtom(gmiMapped.getOriginalId().get());
    //
    // CWICCMRCollectionEntryParser cmrCollectionEntryParser = new CWICCMRCollectionEntryParser(stream);
    //
    // String url = cmrCollectionEntryParser.getSecondLevelOpenSearchDD(DEFAULT_CLIENT_ID);
    //
    // CWICCMRSecondLevelOSDDResolver resolver = new CWICCMRSecondLevelOSDDResolver(url);
    //
    // String baseSecondLevelURL = resolver.getSearchBaseUrl();
    //
    // gmiMapped.getHarmonizedMetadata().getExtendedMetadata().add(CWIC_SECOND_LEVEL_TEMPLATE, baseSecondLevelURL);
    //
    // } catch (ParserConfigurationException | XPathExpressionException | SAXException | IOException e) {
    //
    // logger.warn("Unable to add CMR OSDD extension element to collection {}, this collection will not be expandible",
    // gmiMapped.getPublicId(), e);
    //
    // }
    // }
    //
    // @Override
    // protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {
    // // TODO Auto-generated method stub
    // // return super.execMapping(originalMD, source);
    //
    // XMLDocumentReader reader;
    // try {
    //
    // reader = new XMLDocumentReader(new
    // ByteArrayInputStream(originalMD.getMetadata().getBytes(StandardCharsets.UTF_8)));
    //
    // reader.setNamespaceContext(new CommonNameSpaceContext());
    //
    // } catch (SAXException | IOException e) {
    //
    // logger.error(CANT_READ_GRANULE, e);
    //
    // throw GSException.createException(getClass(), CANT_READ_GRANULE, null, null, ErrorInfo.ERRORTYPE_INTERNAL,
    // ErrorInfo.SEVERITY_ERROR, CMRGRANULES_MAPPER_ORIGINAL_MD_READ_ERROR, e);
    //
    // }
    //
    // Optional<String> identifier = read(reader, IDENTIFIER_XPATH);
    //
    // int count = 0;
    // if (identifier.isPresent()) {
    //
    // String url = CWIC_BASE_TEMPLATE_URL + "datasetId=" + identifier.get() +
    // "&startPage=1&count=1&isCwic=true&clientId=gs-service";
    //
    // try {
    // String result = getDownloader().downloadString(url).orElse(null);
    //
    // XMLDocumentReader rr = new XMLDocumentReader(result);
    //
    // String countString = rr.evaluateNode("//*:totalResults").getTextContent();
    //
    // count = Integer.parseInt(countString);
    //
    // } catch (Exception e) {
    // GSLoggerFactory.getLogger(CWICCMROpensearchCollectionMapper.class).error("Get Number of Records Error");
    // GSLoggerFactory.getLogger(CWICCMROpensearchCollectionMapper.class).error(e.getMessage(), e);
    // }
    //
    // }
    //
    // GSResource dataset = new DatasetCollection();
    //
    // CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
    // MIMetadata miMetadata = coreMetadata.getMIMetadata();
    // miMetadata.setHierarchyLevelName("series");
    // miMetadata.addHierarchyLevelScopeCodeListValue("series");
    // if (count == 0) {
    //
    // // dataset.getHarmonizedMetadata().getCoreMetadata().setTitle("HAS-LOWEST-RANKING");
    //
    // //
    // // lowest ranking
    // //
    // dataset.getPropertyHandler().setLowestRanking();
    // }
    //
    // miMetadata.setLanguage("eng");
    //
    // dataset.setSource(source);
    //
    // DataIdentification dataIdentification = miMetadata.getDataIdentification();
    //
    // Optional<String> bbox = read(reader, BBOX_XPATH);
    //
    // if (bbox.isPresent()) {
    //
    // bbox.ifPresent(b -> {
    //
    // String[] bsplit = b.split(" ");
    //
    // if (bsplit.length != 4) {
    //
    // logger.warn("Found unrecognized bbox {}", b);
    // return;
    // }
    //
    // /**
    // * <georss:box>-90 -180 90 180</georss:box>
    // */
    //
    // dataIdentification.addGeographicBoundingBox("Collection extent", Double.valueOf(bsplit[2]),
    // Double.valueOf(bsplit[1]),
    // Double.valueOf(bsplit[0]), Double.valueOf(bsplit[3]));
    // });
    // } else {
    // Optional<String> bboxpoint = read(reader, BBOX_POINT_XPATH);
    // bboxpoint.ifPresent(b -> {
    //
    // String[] bsplit = b.split(" ");
    //
    // if (bsplit.length != 2) {
    //
    // logger.warn("Found unrecognized bbox {}", b);
    // return;
    // }
    //
    // /**
    // * <georss:box>-90 -180 90 180</georss:box>
    // */
    //
    // dataIdentification.addGeographicBoundingBox("Collection extent", Double.valueOf(bsplit[0]),
    // Double.valueOf(bsplit[1]),
    // Double.valueOf(bsplit[0]), Double.valueOf(bsplit[1]));
    // });
    // }
    //
    // Optional<String> updated = read(reader, REVISION_XPATH);
    //
    // updated.ifPresent(up -> {
    // try {
    // String time = ISO8601DateTimeUtils.getISO8601DateTime(ISO8601DateTimeUtils.parseISO8601(up));
    // dataIdentification.addCitationDate(time, REVISION);
    // } catch (Exception e) {
    // logger.warn("Invalid format: \"\" {}", up, e);
    // }
    // });
    //
    // String title = "Collection title";
    //
    // dataIdentification.setCitationTitle(read(reader, TITLE_XPATH).orElse(title));
    //
    // Optional<String> abs = read(reader, ABSTRACT_XPATH);
    //
    // abs.ifPresent(dataIdentification::setAbstract);
    // /**
    // * Optional<String> download = read(reader, DIRECT_DOWNLOAD_XPATH);
    // * download.ifPresent(d -> {
    // * Online online = new Online();
    // * online.setLinkage(d);
    // * // online.setProtocol();
    // * online.setName("");
    // * miMetadata.getDistribution().addDistributionOnline(online);
    // * });
    // */
    //
    // /*
    // * KEYWORDS
    // * identifier, shortName, dataCenter, archiveCenter, organization, tags
    // */
    //
    // Optional<String> shortName = read(reader, SHORT_NAME_XPATH);
    // Optional<String> dataCenter = read(reader, DATACENTER_XPATH);
    // Optional<String> archiveCenter = read(reader, ARCHIVECENTER_XPATH);
    // Optional<String> organization = read(reader, ORGANIZATION_XPATH);
    // Optional<List<String>> tags = readListString(reader, TAGKEY_XPATH);
    //
    // Set<String> keywords = new HashSet<>();
    //
    // shortName.ifPresent(keywords::add);
    // dataCenter.ifPresent(keywords::add);
    // archiveCenter.ifPresent(keywords::add);
    // organization.ifPresent(keywords::add);
    //
    // tags.ifPresent(t -> {
    // for (String s : t) {
    // keywords.add(s);
    // if (s.contains("geoss_data-core")) {
    // keywords.add("geossDataCore");
    // }
    // }
    // });
    //
    // Optional<String> author = read(reader, AUTHOR_NAME_XPATH);
    //
    // Optional<String> mail = read(reader, AUTHOR_EMAIL_XPATH);
    //
    // ResponsibleParty responsible = null;
    //
    // if (author.isPresent()) {
    // responsible = new ResponsibleParty();
    // responsible.setRoleCode("pointOfContact");
    // responsible.setIndividualName(author.get());
    // organization.ifPresent(responsible::setOrganisationName);
    // }
    // Contact contact = new Contact();
    // if (mail.isPresent()) {
    // contact = new Contact();
    // Address otherAddress = new Address();
    // otherAddress.addElectronicMailAddress(mail.get());
    // contact.setAddress(otherAddress);
    // }
    //
    // if (responsible != null) {
    // responsible.setContactInfo(contact);
    // dataIdentification.addPointOfContact(responsible);
    // }
    //
    // Optional<Node[]> downloads = readDoc(reader, LINKS_XPATH);
    //
    // downloads.ifPresent(d -> {
    // Set<String> urls = new HashSet<String>();
    // for (Node n : d) {
    // NamedNodeMap attributes = n.getAttributes();
    // String url = attributes.getNamedItem("href").getTextContent();
    // String t = "Online resource for Collection " + read(reader, TITLE_XPATH).orElse(title);
    // if (attributes.getNamedItem("title") != null) {
    // t = attributes.getNamedItem("title").getTextContent();
    // }
    // if (url != null && !url.isEmpty()) {
    // if (urls.contains(url))
    // continue;
    // urls.add(url);
    // Online online = new Online();
    // online.setLinkage(url);
    // online.setDescription(t);
    // miMetadata.getDistribution().addDistributionOnline(online);
    //
    // }
    //
    // // possibly other keywords
    //
    // if (t.contains("Product metadata")) {
    // try {
    // String conceptXml = getDownloader().downloadString(url).orElse(null);
    //
    // XMLDocumentReader readerConcept = new XMLDocumentReader(conceptXml);
    //
    // List<String> keywordsNodes = getKeywordsFromConceptXMLDocument(readerConcept);
    //
    // keywordsNodes.forEach(key -> keywords.add(key));
    //
    // } catch (Exception e) {
    // GSLoggerFactory.getLogger(CWICCMROpensearchCollectionMapper.class).error("Get Number of Records Error");
    // GSLoggerFactory.getLogger(CWICCMROpensearchCollectionMapper.class).error(e.getMessage(), e);
    // }
    //
    // }
    //
    // }
    //
    // });
    //
    // keywords.forEach(k -> dataIdentification.addKeyword(k));
    //
    // Optional<String> preview = read(reader, THUMBNAIL_XPATH);
    //
    // preview.ifPresent(p -> dataIdentification.addGraphicOverview(createGraphicOverview(p)));
    //
    // Optional<String> temporalExtent = read(reader, TEMPRAL_EXTENT_XPATH);
    //
    // temporalExtent.ifPresent(t -> {
    //
    // String[] tsplit = t.split("/");
    //
    // if (tsplit.length != 2) {
    //
    // logger.warn("Found unrecognized time extent {}", t);
    // return;
    // }
    //
    // String start = ISO8601DateTimeUtils.getISO8601DateTime(ISO8601DateTimeUtils.parseISO8601(tsplit[0]));
    //
    // String end = ISO8601DateTimeUtils.getISO8601DateTime(ISO8601DateTimeUtils.parseISO8601(tsplit[1]));
    //
    // TemporalExtent tempExtent = new TemporalExtent();
    //
    // tempExtent.setBeginPosition(start);
    //
    // tempExtent.setEndPosition(end);
    //
    // dataIdentification.addTemporalExtent(tempExtent);
    // });
    //
    // //
    //
    // return dataset;
    //
    // }
    //
    // private List<String> getKeywordsFromConceptXMLDocument(XMLDocumentReader readerConcept) {
    // List<String> keywords = new ArrayList<String>();
    //
    // try {
    // Node[] nodes = readerConcept.evaluateNodes("//*:ScienceKeyword");
    // for (Node keywordNode : nodes) {
    // NodeList listNode = keywordNode.getChildNodes();
    // String keyword = "";
    //
    // for (int j = 0; j < listNode.getLength(); j++) {
    //
    // Node element = listNode.item(j);
    //
    // if (element.getNodeType() == Node.ELEMENT_NODE) {
    // String text = element.getTextContent().replaceAll("[\\n]", "").trim() + " > ";
    // keyword = keyword + text;
    //
    // }
    // }
    // if (!keyword.isEmpty()) {
    // keyword = keyword.substring(0, keyword.length() - 3);
    // keywords.add(keyword);
    // }
    // }
    //
    // if (keywords.isEmpty()) {
    // Node[] alternativeNodes = readerConcept.evaluateNodes("//*:Science_Keywords");
    // for (Node keywordNode : alternativeNodes) {
    // NodeList listNode = keywordNode.getChildNodes();
    // String keyword = "";
    //
    // for (int j = 0; j < listNode.getLength(); j++) {
    //
    // Node element = listNode.item(j);
    //
    // if (element.getNodeType() == Node.ELEMENT_NODE) {
    // String text = element.getTextContent().replaceAll("[\\n]", "").trim() + " > ";
    // keyword = keyword + text;
    //
    // }
    // }
    // if (!keyword.isEmpty()) {
    // keyword = keyword.substring(0, keyword.length() - 3);
    // keywords.add(keyword);
    // }
    // }
    // }
    //
    // Optional<List<String>> isoTopicCategory = readListString(readerConcept, "//*:ISO_Topic_Category");
    // Optional<List<String>> ancillaryKeyword = readListString(readerConcept, "//*:Ancillary_Keyword");
    //
    // isoTopicCategory.ifPresent(topic -> {
    // for (String s : topic) {
    // keywords.add(s);
    // }
    // });
    //
    // ancillaryKeyword.ifPresent(ancillary -> {
    // for (String s : ancillary) {
    // keywords.add(s);
    // }
    // });
    //
    // } catch (Exception e) {
    // GSLoggerFactory.getLogger(CWICCMROpensearchCollectionMapper.class).error("Get Number of Records Error");
    // GSLoggerFactory.getLogger(CWICCMROpensearchCollectionMapper.class).error(e.getMessage(), e);
    // }
    // return keywords;
    // }
    //
    // private Optional<List<String>> readListString(XMLDocumentReader reader, String xpath) {
    // try {
    //
    // return Optional.ofNullable(reader.evaluateTextContent(xpath));
    //
    // } catch (XPathExpressionException e) {
    // logger.warn("Can't evalueate xpath {}", xpath, e);
    // }
    //
    // return Optional.empty();
    // }
    //
    // private Optional<String> read(XMLDocumentReader reader, String xpath) {
    //
    // try {
    //
    // return Optional.ofNullable(reader.evaluateString(xpath));
    //
    // } catch (XPathExpressionException e) {
    // logger.warn("Can't evalueate xpath {}", xpath, e);
    // }
    //
    // return Optional.empty();
    //
    // }
    //
    // private Optional<Node[]> readDoc(XMLDocumentReader reader, String xpath) {
    //
    // try {
    //
    // return Optional.ofNullable(reader.evaluateNodes(xpath));
    //
    // } catch (XPathExpressionException e) {
    // logger.warn("Can't evalueate xpath {}", xpath, e);
    // }
    //
    // return Optional.empty();
    //
    // }
    //
    // private BrowseGraphic createGraphicOverview(String url) {
    //
    // BrowseGraphic graphic = new BrowseGraphic();
    //
    // graphic.setFileName(url);
    // graphic.setFileType("image/png");
    //
    // return graphic;
    // }
    //
}
