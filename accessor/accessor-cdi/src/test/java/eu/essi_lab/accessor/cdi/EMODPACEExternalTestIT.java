package eu.essi_lab.accessor.cdi;

import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Optional;

import org.junit.Test;
import org.w3c.dom.Node;

import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.xml.XMLDocumentReader;

public class EMODPACEExternalTestIT {

    @Test
    public void test() throws Exception {

	String emodpaceURL = "http://222.186.3.18:8889/services/xml";
	Downloader d = new Downloader();
	Optional<InputStream> stream = d.downloadOptionalStream(emodpaceURL);

	XMLDocumentReader reader = null;

	if (stream.isPresent()) {

	    reader = new XMLDocumentReader(stream.get());

	}

	Node[] cdiUrlNodes;

	cdiUrlNodes = reader.evaluateNodes("//*:cdiUrl");

	assertTrue(cdiUrlNodes.length > 700);

	HashMap<String, SimpleEntry<Integer, String>> results = new HashMap<>();

	int idCountMD = 0;
	int idCountEML = 0;
	int resourceIdCountMD = 0;
	int resourceIdCountEML = 0;
	int titleCountMD = 0;
	int titleCountEML = 0;
	int bboxCountMD = 0;
	int bboxCountEML = 0;
	int temporalExtentCountMD = 0;
	int temporalExtentCountEML = 0;
	int keywordsCountMD = 0;
	int keywordsCountEML = 0;
	int instrumentCountMD = 0;
	int instrumentCountEML = 0;
	int organizationOriginatorCountMD = 0;
	int organizationOriginatorCountEML = 0;
	int organizationIdentifierOriginatorCountMD = 0;
	int organizationIdentifierCountEML = 0;
	int organizationContactCountMD = 0;
	int organizationContactCountEML = 0;
	int organizationIdentifierContactCountMD = 0;
	int organizationIdentifierContactCountEML = 0;
	int dateStampCountMD = 0;
	int dateStampCountEML = 0;
	int revisionCountMD = 0;
	int revisionCountEML = 0;
	int abstractCountMD = 0;
	int abstractCountEML = 0;
	int parameterCountMD = 0;
	int parameterCountEML = 0;
	int parameterIdentifierCountMD = 0;
	int parameterIdentifierCountEML = 0;
	int instrumentIdentifierCountMD = 0;
	int instrumentIdentifierCountEML = 0;
	int platformNameCountMD = 0;
	int platformNameCountEML = 0;
	int platformNameIdentifierCountMD = 0;
	int platformNameIdentifierCountEML = 0;
	int platformClassNameCountMD = 0;
	int platformClassNameCountEML = 0;
	int platformClassNameIdentifierCountMD = 0;
	int platformClassNameIdentifierCountEML = 0;
	int projectNameCountMD = 0;
	int projectNameCountEML = 0;
	int projectIdentifierCountMD = 0;
	int projectIdentifierCountEML = 0;
	int cruiseNameCountMD = 0;
	int cruiseNameCountEML = 0;
	int cruiseIdentifierCountMD = 0;
	int cruiseIdentifierCountEML = 0;
	int productNameCountMD = 0;
	int productNameCountEML = 0;
	int productIdentifierCountMD = 0;
	int productIdentifierCountEML = 0;

	for (int i = 0; i < cdiUrlNodes.length; i++) {
	    System.out.println("At " + i);
	    long time = System.currentTimeMillis();
	    Node nodeResult = cdiUrlNodes[i];
	    String targetUrl = nodeResult.getTextContent();
	    targetUrl = targetUrl.contains(" ") ? targetUrl.replaceAll(" ", "%20") : targetUrl;
	    String res = d.downloadOptionalString(targetUrl).orElse(null);

	    XMLDocumentReader r = new XMLDocumentReader(res);
	    r.setNamespaceContext(new CommonNameSpaceContext());
	    String name = r.evaluateString("local-name(/*[1])");
	    System.out.println(name);

	    SimpleEntry<Integer, String> result = results.get(name);
	    if (result == null) {
		result = new SimpleEntry(1, targetUrl);
	    } else {
		result = new SimpleEntry(result.getKey() + 1, targetUrl);
	    }
	    results.put(name, result);

	    time = System.currentTimeMillis() - time;
	    System.out.println("took " + time + " ms");
	    // GMD
	    if (name.toLowerCase().contains("metadata")) {
		// identifier
		String id = r.evaluateString("//gmd:fileIdentifier/gco:CharacterString");
		if (id != null && !id.isEmpty()) {
		    idCountMD++;
		}
		// resourceIdentifier
		String resourceId = r.evaluateString(
			"/gmd:MD_Metadata/gmd:identificationInfo/sdn:SDN_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString");
		if (resourceId != null && !resourceId.isEmpty()) {
		    resourceIdCountMD++;
		}
		// title
		String title = r.evaluateString(
			"/gmd:MD_Metadata/gmd:identificationInfo/sdn:SDN_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/*[1]");
		if (title != null && !title.isEmpty()) {
		    titleCountMD++;
		}
		// keyword
		String keyword = r.evaluateString(
			"/gmd:MD_Metadata/gmd:identificationInfo/sdn:SDN_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords[not(gmd:type) or not(contains('platform instrument',gmd:type/gmd:MD_KeywordTypeCode/@codeListValue))]/gmd:keyword/*[1]");
		if (keyword != null && !keyword.isEmpty()) {
		    keywordsCountMD++;
		}
		// bbox
		String west = r.evaluateString(
			"/gmd:MD_Metadata/gmd:identificationInfo/sdn:SDN_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude/gco:Decimal");
		String east = r.evaluateString(
			"/gmd:MD_Metadata/gmd:identificationInfo/sdn:SDN_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:eastBoundLongitude/gco:Decimal");
		String south = r.evaluateString(
			"/gmd:MD_Metadata/gmd:identificationInfo/sdn:SDN_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:southBoundLatitude/gco:Decimal");
		String north = r.evaluateString(
			"/gmd:MD_Metadata/gmd:identificationInfo/sdn:SDN_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:northBoundLatitude/gco:Decimal");
		if ((west != null && !west.isEmpty()) && (east != null && !east.isEmpty()) && (south != null && !south.isEmpty())
			&& (north != null && !north.isEmpty())) {
		    bboxCountMD++;
		}

		// time
		String start = r.evaluateString(
			"/gmd:MD_Metadata/gmd:identificationInfo/sdn:SDN_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:beginPosition");
		String end = r.evaluateString(
			"/gmd:MD_Metadata/gmd:identificationInfo/sdn:SDN_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:endPosition");

		if ((start != null && !start.isEmpty()) && (end != null && !end.isEmpty())) {
		    temporalExtentCountMD++;
		}

		// instrument
		String instrument = r.evaluateString(
			"/gmd:MD_Metadata/gmd:identificationInfo/sdn:SDN_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='instrument']/gmd:keyword/*[1]");
		if (instrument != null && !instrument.isEmpty()) {
		    instrumentCountMD++;
		}

		// datestamp
		String dateStamp = r.evaluateString("/gmd:MD_Metadata/gmd:dateStamp/gco:Date");
		if (dateStamp != null && !dateStamp.isEmpty()) {
		    dateStampCountMD++;
		}
		// revision date
		String revisionDate = r.evaluateString(
			"/gmd:MD_Metadata/gmd:identificationInfo/sdn:SDN_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='revision']/gmd:date/gco:Date");
		if (revisionDate != null && !revisionDate.isEmpty()) {
		    revisionCountMD++;
		}
		// abstract
		String abstrakt = r.evaluateString(
			"/gmd:MD_Metadata/gmd:identificationInfo/sdn:SDN_DataIdentification/gmd:abstract/gco:CharacterString");
		if (abstrakt != null && !abstrakt.isEmpty() && !abstrakt.toLowerCase().equals("null")) {
		    abstractCountMD++;
		}

		// parameter name
		String parameterName = r.evaluateString(
			"/gmd:MD_Metadata/gmd:identificationInfo/sdn:SDN_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='parameter']/gmd:keyword/*[1]");
		if (parameterName != null && !parameterName.isEmpty()) {
		    parameterCountMD++;
		}

		// parameter identifier
		String parameterIdentifier = r.evaluateString(
			"/gmd:MD_Metadata/gmd:identificationInfo/sdn:SDN_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='parameter']/gmd:keyword/*[1]/@codeListValue");
		if (parameterIdentifier != null && !parameterIdentifier.isEmpty()) {
		    parameterIdentifierCountMD++;
		}

		// instrument identifier
		String instrumentIdentifier = r.evaluateString(
			"/gmd:MD_Metadata/gmd:identificationInfo/sdn:SDN_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='instrument']/gmd:keyword/*[1]/@codeListValue");
		if (instrumentIdentifier != null && !instrumentIdentifier.isEmpty()) {
		    instrumentIdentifierCountMD++;
		}

		// platform name
		String platformName = r.evaluateString(
			"/gmd:MD_Metadata/gmd:identificationInfo/sdn:SDN_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='platform']/gmd:keyword/*[1]");
		if (platformName != null && !platformName.isEmpty()) {
		    platformNameCountMD++;
		}

		// platform identifier
		String platformIdentifier = r.evaluateString(
			"/gmd:MD_Metadata/gmd:identificationInfo/sdn:SDN_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='platform']/gmd:keyword/*[1]/@codeListValue");
		if (platformIdentifier != null && !platformIdentifier.isEmpty()) {
		    platformNameIdentifierCountMD++;
		}

		// platform_class name
		String platformClassName = r.evaluateString(
			"/gmd:MD_Metadata/gmd:identificationInfo/sdn:SDN_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='platform_class']/gmd:keyword/*[1]");
		if (platformClassName != null && !platformClassName.isEmpty()) {
		    platformClassNameCountMD++;
		}

		// platform_class identifier
		String platformClassNameIdentifier = r.evaluateString(
			"/gmd:MD_Metadata/gmd:identificationInfo/sdn:SDN_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='platform_class']/gmd:keyword/*[1]/@codeListValue");
		if (platformClassNameIdentifier != null && !platformClassNameIdentifier.isEmpty()) {
		    platformClassNameIdentifierCountMD++;
		}

		// organizationOriginator
		String organizationOriginator = r
			.evaluateString("//*:SDN_EDMOCode[../../*:role/*:CI_RoleCode/@codeListValue='originator']");
		if (organizationOriginator != null && !organizationOriginator.isEmpty()) {
		    organizationOriginatorCountMD++;
		}
		// organizationOriginatorIdentifier
		String organizationOriginatorId = r
			.evaluateString("//*:SDN_EDMOCode[../../*:role/*:CI_RoleCode/@codeListValue='originator']/@codeListValue");
		if (organizationOriginatorId != null && !organizationOriginatorId.isEmpty()) {
		    organizationIdentifierOriginatorCountMD++;
		}

		// organization pointofcontact
		String organizationContact = r
			.evaluateString("//*:SDN_EDMOCode[../../*:role/*:CI_RoleCode/@codeListValue='pointOfContact']");
		if (organizationContact != null && !organizationContact.isEmpty()) {
		    organizationContactCountMD++;
		}
		// organization pointofcontact Identifier
		String organizationContactId = r
			.evaluateString("//*:SDN_EDMOCode[../../*:role/*:CI_RoleCode/@codeListValue='pointOfContact']/@codeListValue");
		if (organizationContactId != null && !organizationContactId.isEmpty()) {
		    organizationIdentifierContactCountMD++;
		}

		// project name
		String projectName = r.evaluateString(
			"/gmd:MD_Metadata/gmd:identificationInfo/sdn:SDN_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='project']/gmd:keyword/*[1]");
		if (projectName != null && !projectName.isEmpty()) {
		    projectNameCountMD++;
		}

		// project identifier
		String projectIdentifier = r.evaluateString(
			"/gmd:MD_Metadata/gmd:identificationInfo/sdn:SDN_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='project']/gmd:keyword/*[1]/@codeListValue");
		if (projectIdentifier != null && !projectIdentifier.isEmpty()) {
		    projectIdentifierCountMD++;
		}

		// cruise name
		String cruiseName = r.evaluateString(
			"/gmd:MD_Metadata/gmd:identificationInfo/sdn:SDN_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='project']/gmd:keyword/*[1]");
		if (cruiseName != null && !cruiseName.isEmpty()) {
		    cruiseNameCountMD++;
		}

		// cruise identifier
		String cruiseIdentifier = r.evaluateString(
			"/gmd:MD_Metadata/gmd:identificationInfo/sdn:SDN_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='project']/gmd:keyword/*[1]/@codeListValue");
		if (cruiseIdentifier != null && !cruiseIdentifier.isEmpty()) {
		    cruiseIdentifierCountMD++;
		}

		// product name
		String productName = r.evaluateString(
			"/gmd:MD_Metadata/gmd:identificationInfo/sdn:SDN_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='project']/gmd:keyword/*[1]");
		if (productName != null && !productName.isEmpty()) {
		    productNameCountMD++;
		}

		// product identifier
		String productIdentifier = r.evaluateString(
			"/gmd:MD_Metadata/gmd:identificationInfo/sdn:SDN_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='project']/gmd:keyword/*[1]/@codeListValue");
		if (productIdentifier != null && !productIdentifier.isEmpty()) {
		    productIdentifierCountMD++;
		}

		// EML
	    } else if (name.toLowerCase().contains("eml")) {
		// identifier
		String emlId = r.evaluateString("/*:eml/*:dataset/*:alternateIdentifier");
		if (emlId != null && !emlId.isEmpty()) {
		    idCountEML++;
		}
		// resourceIdentifier
		// String resourceId =
		// r.evaluateString("//*:MD_Metadata/*:identificationInfo/*:MD_DataIdentification/*:citation/*:CI_Citation/*:identifier/*:MD_Identifier/*:code/*:CharacterString");
		// if (resourceId != null && !resourceId.isEmpty()) {
		// resourceIdCountEML++;
		// }
		// title
		String emlTitle = r.evaluateString("/*:eml/*:dataset/*:title");
		if (emlTitle != null && !emlTitle.isEmpty()) {
		    titleCountEML++;
		}
		// keyword
		Node[] emlKeywordNodes = r.evaluateNodes("/*:eml/*:dataset/*:keywordSet/*:keyword");
		if (emlKeywordNodes.length > 0) {
		    keywordsCountEML++;
		}
		// bbox
		String emlWest = r.evaluateString(
			"/*:eml/*:dataset/*:coverage/*:geographicCoverage[1]/*:boundingCoordinates/*:westBoundingCoordinate");
		String emlEast = r.evaluateString(
			"/*:eml/*:dataset/*:coverage/*:geographicCoverage[1]/*:boundingCoordinates/*:eastBoundingCoordinate");
		String emlNorth = r.evaluateString(
			"/*:eml/*:dataset/*:coverage/*:geographicCoverage[1]/*:boundingCoordinates/*:northBoundingCoordinate");
		String emlSouth = r.evaluateString(
			"/*:eml/*:dataset/*:coverage/*:geographicCoverage[1]/*:boundingCoordinates/*:southBoundingCoordinate");

		if ((emlWest != null && !emlWest.isEmpty()) && (emlEast != null && !emlEast.isEmpty())
			&& (emlNorth != null && !emlNorth.isEmpty()) && (emlSouth != null && !emlSouth.isEmpty())) {
		    bboxCountEML++;
		}
		// temporalExtent
		String emlStartDate = r
			.evaluateString("/*:eml/*:dataset/*:coverage/*:temporalCoverage/*:rangeOfDates/*:beginDate/*:calendarDate");
		String emlEndDate = r
			.evaluateString("/*:eml/*:dataset/*:coverage/*:temporalCoverage/*:rangeOfDates/*:endDate/*:calendarDate");

		if (emlStartDate != null && !emlStartDate.isEmpty() && !emlStartDate.contains("unknown") && emlEndDate != null
			&& !emlEndDate.isEmpty() && !emlEndDate.contains("unknown")) {
		    temporalExtentCountEML++;
		}

		// instrument

		// organization originator
		String emlOrganizationOriginator = r.evaluateString("/*:eml/*:dataset/*:creator/*:organizationName");
		if (emlOrganizationOriginator != null && !emlOrganizationOriginator.isEmpty()) {
		    organizationOriginatorCountEML++;
		}

		// organization originator Identifier

		// organization contact
		String emlOrganizationContact = r.evaluateString("/*:eml/*:dataset/*:contact/*:organizationName");
		if (emlOrganizationContact != null && !emlOrganizationContact.isEmpty()) {
		    organizationContactCountEML++;
		}

		// organization contact Identifier

		// datestamp
		String dateStamp = r.evaluateString("//*:dateStamp");
		if (dateStamp != null && !dateStamp.isEmpty()) {
		    dateStampCountEML++;
		}
		// revision date
		String emlRevisionDate = r.evaluateString("/*:eml/*:dataset/*:pubDate");
		if (emlRevisionDate != null && !emlRevisionDate.isEmpty()) {
		    revisionCountEML++;
		}

		// abstract
		String emlAbstrakt = r.evaluateString("/*:eml/*:dataset/*:abstract");
		if (emlAbstrakt != null && !emlAbstrakt.isEmpty() && emlAbstrakt.toLowerCase() != "null") {
		    abstractCountEML++;
		}

		// parameter
		String parameterNameEML = r
			.evaluateString("/*:eml/*:dataset/*:coverage/*:taxonomicCoverage/*:taxonomicClassification/*:taxonRankName");
		String parameterValueEML = r
			.evaluateString("/*:eml/*:dataset/*:coverage/*:taxonomicCoverage/*:taxonomicClassification/*:taxonRankValue");
		if (parameterNameEML != null && !parameterNameEML.isEmpty() && parameterValueEML != null && !parameterValueEML.isEmpty()) {
		    parameterCountEML++;
		}
	    }

	}

	Integer metadataCount = 0;
	Integer emlCount = 0;
	for (String root : results.keySet()) {
	    SimpleEntry<Integer, String> result = results.get(root);
	    System.out.println("Metadata records with root element " + root + ": " + result.getKey() + " Example: " + result.getValue());
	    if (root.toLowerCase().contains("metadata")) {
		metadataCount = result.getKey();
	    } else if (root.toLowerCase().contains("eml")) {
		emlCount = result.getKey();
	    }
	}

	System.out.println("MD_Metadata Identifier count: " + idCountMD + "/" + metadataCount + " . Presence(%): "
		+ (((double) idCountMD / (double) metadataCount) * 100.00));
	System.out.println("EML Identifier count: " + idCountEML + "/" + emlCount + " . Presence(%): "
		+ (((double) idCountEML / (double) emlCount) * 100.00));

	System.out.println("MD_Metadata Resource Identifier count: " + resourceIdCountMD + "/" + metadataCount + " . Presence(%): "
		+ (((double) resourceIdCountMD / (double) metadataCount) * 100.00));
	System.out.println("EML Resource Identifier count: " + resourceIdCountEML + "/" + emlCount + " . Presence(%): "
		+ (((double) resourceIdCountEML / (double) emlCount) * 100.00));

	System.out.println("MD_Metadata Keyword count: " + keywordsCountMD + "/" + metadataCount + " . Presence(%): "
		+ (((double) keywordsCountMD / (double) metadataCount) * 100.00));
	System.out.println("EML Keyword count: " + keywordsCountEML + "/" + emlCount + " . Presence(%): "
		+ (((double) keywordsCountEML / (double) emlCount) * 100.00));

	System.out.println("MD_Metadata Title count: " + titleCountMD + "/" + metadataCount + " . Presence(%): "
		+ (((double) titleCountMD / (double) metadataCount) * 100.00));
	System.out.println("EML Title count: " + titleCountEML + "/" + emlCount + " . Presence(%): "
		+ (((double) titleCountEML / (double) emlCount) * 100.00));

	System.out.println("MD_Metadata BBOX count: " + bboxCountMD + "/" + metadataCount + " . Presence(%): "
		+ (((double) bboxCountMD / (double) metadataCount) * 100.00));
	System.out.println("EML BBOX count: " + bboxCountEML + "/" + emlCount + " . Presence(%): "
		+ (((double) bboxCountEML / (double) emlCount) * 100.00));
	//
	System.out.println("MD_Metadata Temporal Extent count: " + temporalExtentCountMD + "/" + metadataCount + " . Presence(%): "
		+ (((double) temporalExtentCountMD / (double) metadataCount) * 100.00));
	System.out.println("EML Temporal Extent count: " + temporalExtentCountEML + "/" + emlCount + " . Presence(%): "
		+ (((double) temporalExtentCountEML / (double) emlCount) * 100.00));

	System.out.println("MD_Metadata Organization count: " + organizationOriginatorCountMD + "/" + metadataCount + " . Presence(%): "
		+ (((double) organizationOriginatorCountMD / (double) metadataCount) * 100.00));
	System.out.println("EML Organization count: " + organizationOriginatorCountEML + "/" + emlCount + " . Presence(%): "
		+ (((double) organizationOriginatorCountEML / (double) emlCount) * 100.00));

	System.out.println("MD_Metadata Organization identifier count: " + organizationIdentifierOriginatorCountMD + "/" + metadataCount
		+ " . Presence(%): " + (((double) organizationIdentifierOriginatorCountMD / (double) metadataCount) * 100.00));
	System.out.println("EML Organization identifier count: " + organizationIdentifierCountEML + "/" + emlCount + " . Presence(%): "
		+ (((double) organizationIdentifierCountEML / (double) emlCount) * 100.00));

	System.out.println("MD_Metadata Organization contact count: " + organizationContactCountMD + "/" + metadataCount
		+ " . Presence(%): " + (((double) organizationContactCountMD / (double) metadataCount) * 100.00));
	System.out.println("EML Organization contact count: " + organizationContactCountEML + "/" + emlCount + " . Presence(%): "
		+ (((double) organizationContactCountEML / (double) emlCount) * 100.00));

	System.out.println("MD_Metadata Organization identifier contact count: " + organizationIdentifierContactCountMD + "/"
		+ metadataCount + " . Presence(%): " + (((double) organizationIdentifierContactCountMD / (double) metadataCount) * 100.00));
	System.out.println("EML Organization identifier contact count: " + organizationIdentifierContactCountEML + "/" + emlCount
		+ " . Presence(%): " + (((double) organizationIdentifierContactCountEML / (double) emlCount) * 100.00));

	System.out.println("MD_Metadata Datestamp count: " + dateStampCountMD + "/" + metadataCount + " . Presence(%): "
		+ (((double) dateStampCountMD / (double) metadataCount) * 100.00));
	System.out.println("EML Datestamp count: " + dateStampCountEML + "/" + emlCount + " . Presence(%): "
		+ (((double) dateStampCountEML / (double) emlCount) * 100.00));

	System.out.println("MD_Metadata Revision Date count: " + revisionCountMD + "/" + metadataCount + " . Presence(%): "
		+ (((double) revisionCountMD / (double) metadataCount) * 100.00));
	System.out.println("EML Revision Date count: " + revisionCountEML + "/" + emlCount + " . Presence(%): "
		+ (((double) revisionCountEML / (double) emlCount) * 100.00));

	System.out.println("MD_Metadata Abstract count: " + abstractCountMD + "/" + metadataCount + " . Presence(%): "
		+ (((double) abstractCountMD / (double) metadataCount) * 100.00));
	System.out.println("EML Abstract count: " + abstractCountEML + "/" + emlCount + " . Presence(%): "
		+ (((double) abstractCountEML / (double) emlCount) * 100.00));

	System.out.println("MD_Metadata Parameter name count: " + parameterCountMD + "/" + metadataCount + " . Presence(%): "
		+ (((double) parameterCountMD / (double) metadataCount) * 100.00));
	System.out.println("EML Parameter name count: " + parameterCountEML + "/" + emlCount + " . Presence(%): "
		+ (((double) parameterCountEML / (double) emlCount) * 100.00));

	System.out.println("MD_Metadata Parameter identifier count: " + parameterIdentifierCountMD + "/" + metadataCount
		+ " . Presence(%): " + (((double) parameterIdentifierCountMD / (double) metadataCount) * 100.00));
	System.out.println("EML Parameter identifier count: " + parameterIdentifierCountEML + "/" + emlCount + " . Presence(%): "
		+ (((double) parameterIdentifierCountEML / (double) emlCount) * 100.00));

	System.out.println("MD_Metadata Instrument name count: " + instrumentCountMD + "/" + metadataCount + " . Presence(%): "
		+ (((double) instrumentCountMD / (double) metadataCount) * 100.00));
	System.out.println("EML Instrument name count: " + instrumentCountEML + "/" + emlCount + " . Presence(%): "
		+ (((double) instrumentCountEML / (double) emlCount) * 100.00));

	System.out.println("MD_Metadata Instrument identifier count: " + instrumentIdentifierCountMD + "/" + metadataCount
		+ " . Presence(%): " + (((double) instrumentIdentifierCountMD / (double) metadataCount) * 100.00));
	System.out.println("EML Instrument identifier count: " + instrumentIdentifierCountEML + "/" + emlCount + " . Presence(%): "
		+ (((double) instrumentIdentifierCountEML / (double) emlCount) * 100.00));

	System.out.println("MD_Metadata Platform name count: " + platformNameCountMD + "/" + metadataCount + " . Presence(%): "
		+ (((double) platformNameCountMD / (double) metadataCount) * 100.00));
	System.out.println("EML Platform name count: " + platformNameCountEML + "/" + emlCount + " . Presence(%): "
		+ (((double) platformNameCountEML / (double) emlCount) * 100.00));

	System.out.println("MD_Metadata Platform identifier count: " + platformNameIdentifierCountMD + "/" + metadataCount
		+ " . Presence(%): " + (((double) platformNameIdentifierCountMD / (double) metadataCount) * 100.00));
	System.out.println("EML Platform identifier count: " + platformNameIdentifierCountEML + "/" + emlCount + " . Presence(%): "
		+ (((double) platformNameIdentifierCountEML / (double) emlCount) * 100.00));

	System.out.println("MD_Metadata Platform_class name count: " + platformClassNameCountMD + "/" + metadataCount + " . Presence(%): "
		+ (((double) platformClassNameCountMD / (double) metadataCount) * 100.00));
	System.out.println("EML Platform_class name count: " + platformClassNameCountEML + "/" + emlCount + " . Presence(%): "
		+ (((double) platformClassNameCountEML / (double) emlCount) * 100.00));

	System.out.println("MD_Metadata Platform_class identifier count: " + platformClassNameIdentifierCountMD + "/" + metadataCount
		+ " . Presence(%): " + (((double) platformClassNameIdentifierCountMD / (double) metadataCount) * 100.00));
	System.out.println("EML Platform_class identifier count: " + platformClassNameIdentifierCountEML + "/" + emlCount
		+ " . Presence(%): " + (((double) platformClassNameIdentifierCountEML / (double) emlCount) * 100.00));

	System.out.println("MD_Metadata Project name count: " + projectNameCountMD + "/" + metadataCount + " . Presence(%): "
		+ (((double) projectNameCountMD / (double) metadataCount) * 100.00));
	System.out.println("EML Project name count: " + projectNameCountEML + "/" + emlCount + " . Presence(%): "
		+ (((double) projectNameCountEML / (double) emlCount) * 100.00));

	System.out.println("MD_Metadata Project identifier count: " + projectIdentifierCountMD + "/" + metadataCount + " . Presence(%): "
		+ (((double) projectIdentifierCountMD / (double) metadataCount) * 100.00));
	System.out.println("EML Project identifier count: " + projectIdentifierCountEML + "/" + emlCount + " . Presence(%): "
		+ (((double) projectIdentifierCountEML / (double) emlCount) * 100.00));

	System.out.println("MD_Metadata Cruise name count: " + cruiseNameCountMD + "/" + metadataCount + " . Presence(%): "
		+ (((double) cruiseNameCountMD / (double) metadataCount) * 100.00));
	System.out.println("EML Cruise name count: " + cruiseNameCountEML + "/" + emlCount + " . Presence(%): "
		+ (((double) cruiseNameCountEML / (double) emlCount) * 100.00));

	System.out.println("MD_Metadata Cruise identifier count: " + cruiseIdentifierCountMD + "/" + metadataCount + " . Presence(%): "
		+ (((double) cruiseIdentifierCountMD / (double) metadataCount) * 100.00));
	System.out.println("EML Cruise identifier count: " + cruiseIdentifierCountEML + "/" + emlCount + " . Presence(%): "
		+ (((double) cruiseIdentifierCountEML / (double) emlCount) * 100.00));

	System.out.println("MD_Metadata Product name count: " + productNameCountMD + "/" + metadataCount + " . Presence(%): "
		+ (((double) productNameCountMD / (double) metadataCount) * 100.00));
	System.out.println("EML Product name count: " + productNameCountEML + "/" + emlCount + " . Presence(%): "
		+ (((double) productNameCountEML / (double) emlCount) * 100.00));

	System.out.println("MD_Metadata Product identifier count: " + productIdentifierCountMD + "/" + metadataCount + " . Presence(%): "
		+ (((double) productIdentifierCountMD / (double) metadataCount) * 100.00));
	System.out.println("EML Product identifier count: " + productIdentifierCountEML + "/" + emlCount + " . Presence(%): "
		+ (((double) productIdentifierCountEML / (double) emlCount) * 100.00));

    }

}
