package eu.essi_lab.accessor.cmr;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import eu.essi_lab.accessor.cmr.cwic.distributed.CWICGranulesTemplate;
import eu.essi_lab.accessor.cmr.distributed.CMRGranulesTemplate;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author ilsanto
 */
public class CWICGranulesTemplateTest {

    @Test
    public void test() {

	String url = "https://cmr.earthdata.nasa.gov/opensearch/granules.atom?datasetId={echo:datasetId?}&"
		+ "shortName=12-hourly_interpolated_surface_velocity_from_buoys&versionId={echo:versionId?}&dataCenter=SCIOPS&"
		+ "boundingBox={geo:box?}&geometry={geo:geometry?}&placeName={geo:name?}&startTime={time:start?}&endTime={time:end?}&"
		+ "cursor={os:startPage?}&numberOfResults={os:count?}&offset={os:startIndex?}&uid={geo:uid?}&"
		+ "parentIdentifier={eo:parentIdentifier?}&clientId=gs-service";

	CWICGranulesTemplate template = new CWICGranulesTemplate(url);

	template.setCount(10);
	template.setStart(0);

	Assert.assertEquals(
		"https://cmr.earthdata.nasa.gov/opensearch/granules.atom?shortName=12-hourly_interpolated_surface_velocity_from_buoys&dataCenter=SCIOPS&clientId=gs-service&numberOfResults=10&offset=0&",
		template.getRequestURL());

    }

    @Test
    public void test2() {

	String url = "https://cmr.earthdata.nasa.gov/opensearch/granules.atom?datasetId={echo:datasetId?}&"
		+ "shortName=12-hourly_interpolated_surface_velocity_from_buoys&versionId={echo:versionId?}&dataCenter=SCIOPS&"
		+ "boundingBox={geo:box?}&geometry={geo:geometry?}&placeName={geo:name?}&startTime={time:start?}&endTime={time:end?}&"
		+ "cursor={os:startPage?}&numberOfResults={os:count?}&offset={os:startIndex?}&uid={geo:uid?}&"
		+ "parentIdentifier={eo:parentIdentifier?}&clientId=gs-service";

	CWICGranulesTemplate template = new CWICGranulesTemplate(url);

	Assert.assertEquals(
		"https://cmr.earthdata.nasa.gov/opensearch/granules.atom?shortName=12-hourly_interpolated_surface_velocity_from_buoys&dataCenter=SCIOPS&clientId=gs-service&",
		template.getRequestURL());

    }

    @Test
    public void test3() {

	String url = "https://cwic.wgiss.ceos.org/opensearch/granules.atom?datasetId=C1243477378-GES_DISC&startIndex={startIndex?}&count={count?}&timeStart={time:start}&timeEnd={time:end}&geoBox={geo:box}&clientId=gs-service";

	CWICGranulesTemplate template = new CWICGranulesTemplate(url);

	template.setCount(1);
	template.setStart(1);

	Assert.assertEquals(
		"https://cwic.wgiss.ceos.org/opensearch/granules.atom?datasetId=C1243477378-GES_DISC&clientId=gs-service&count=1&startIndex=1&",
		template.getRequestURL());

    }

    @Test
    public void test4() {

	String url = "https://cwic.wgiss.ceos.org/opensearch/granules.atom?datasetId={datasetId?}&startIndex={startIndex?}&count={count?}&timeStart={time:start}&timeEnd={time:end}&geoBox={geo:box}&clientId=gs-service";
	// "datasetId=C1243477378-GES_DISC&startIndex={startIndex?}&count={count?}&timeStart={time:start}&timeEnd={time:end}&geoBox={geo:box}&clientId=gs-service";

	CWICGranulesTemplate template = new CWICGranulesTemplate(url);

	template.setDatasetId("C1243477378-GES_DISC");
	template.setCount(1);
	template.setStart(1);

	Assert.assertEquals(
		"https://cwic.wgiss.ceos.org/opensearch/granules.atom?clientId=gs-service&datasetId=C1243477378-GES_DISC&count=1&startIndex=1&",
		template.getRequestURL());

    }

    @Test
    public void testFormOriginalMetadata() throws SAXException, IOException, TransformerException, XPathExpressionException {

	String url = "https://cwic.wgiss.ceos.org/opensearch/granules.atom?datasetId={datasetId?}&startIndex={startIndex?}&count={count?}&timeStart={time:start}&timeEnd={time:end}&geoBox={geo:box}&clientId=gs-service";
	// "datasetId=C1243477378-GES_DISC&startIndex={startIndex?}&count={count?}&timeStart={time:start}&timeEnd={time:end}&geoBox={geo:box}&clientId=gs-service";

	CWICGranulesTemplate template = new CWICGranulesTemplate(url);

	InputStream stream = CWICGranulesTemplateTest.class.getClassLoader()
		.getResourceAsStream("eu/essi_lab/accessor/cmr/test/originalMetadata.xml");

	XMLDocumentReader reader = new XMLDocumentReader(stream);

	OriginalMetadata originalMetadata = new OriginalMetadata();

	String id = reader.evaluateString("//*:fileIdentifier").trim();

	String metadata = XMLDocumentReader.asString(reader.getDocument());

	originalMetadata.setMetadata(metadata);

	template.setDatasetId(id);
	template.setCount(10);
	template.setStart(1);

	Assert.assertEquals(
		"https://cwic.wgiss.ceos.org/opensearch/granules.atom?clientId=gs-service&datasetId=C1000000725-LARC_ASDC&count=10&startIndex=1&",
		template.getRequestURL());

    }

    @Test
    public void testCMRGranulesTemplate() throws SAXException, IOException, TransformerException, XPathExpressionException {

	String url = "https://cmr.earthdata.nasa.gov/opensearch/granules.atom?dataCenter={dataCenter?}&shortName={shortName?}&offset={os:startIndex?}&numberOfResults={os:count?}&startTime={time:start}&endTime={time:end}&boundingBox={geo:box}&clientId=gs-service";
	// "datasetId=C1243477378-GES_DISC&startIndex={startIndex?}&count={count?}&timeStart={time:start}&timeEnd={time:end}&geoBox={geo:box}&clientId=gs-service";

	CMRGranulesTemplate template = new CMRGranulesTemplate(url);

	System.out.println(template.getRequestURL());

	String dataCenter = "NSIDC_ECS";
	String shortName = "ABLVIS0";

	template.setDataCenter(dataCenter);
	template.setShortName(shortName);
	template.setCount(10);
	template.setStart(0);

	Assert.assertEquals(
		"https://cmr.earthdata.nasa.gov/opensearch/granules.atom?clientId=gs-service&dataCenter=NSIDC_ECS&shortName=ABLVIS0&numberOfResults=10&offset=0&",
		template.getRequestURL());

	// InputStream stream = CWICGranulesTemplateTest.class.getClassLoader().getResourceAsStream(
	// "eu/essi_lab/accessor/cmr/test/originalMetadata.xml");
	//
	// XMLDocumentReader reader = new XMLDocumentReader(stream);
	//
	// OriginalMetadata originalMetadata = new OriginalMetadata();
	//
	// String id = reader.evaluateString("//*:fileIdentifier").trim();
	//
	// String metadata = XMLDocumentReader.asString(reader.getDocument());
	//
	// originalMetadata.setMetadata(metadata);
	//
	// template.setDatasetId(id);
	// template.setCount(10);
	// template.setStart(1);
	//
	// Assert.assertEquals(
	// "https://cwic.wgiss.ceos.org/opensearch/granules.atom?clientId=gs-service&datasetId=C1000000725-LARC_ASDC&count=10&startIndex=1&",
	// template.getRequestURL());

    }

}