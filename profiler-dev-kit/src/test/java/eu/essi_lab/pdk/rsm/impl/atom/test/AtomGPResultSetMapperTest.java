/**
 * 
 */
package eu.essi_lab.pdk.rsm.impl.atom.test;

import java.io.IOException;
import java.util.Date;

import javax.xml.bind.JAXBException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.pdk.rsm.impl.atom.AtomGPResultSetMapper;

/**
 * @author Fabrizio
 */
public class AtomGPResultSetMapperTest {

    @Test
    public void test1() throws Exception {

	GSResource resource = GSResource.create(getClass().getClassLoader().getResourceAsStream("atom-mapper-dataset-test-1.xml"));

	resource.getHarmonizedMetadata().getCoreMetadata().setAbstract("abstract");

	resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().addVerticalExtent(0, 0);
	resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().addVerticalExtent(1, 1);

	resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().addKeyword("kwd1");
	resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().addKeyword("kwd2");
	resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().addKeyword("kwd3");

	resource.getExtensionHandler().setMagnitudeLevel("5");

	XMLDocumentReader reader = map(resource);

	Assert.assertTrue(reader.evaluateString("//*:title/text()").contains("SENTINEL_2B"));
	Assert.assertTrue(reader.evaluateString("//*:id/text()").contains("a50113e0-f713-43c2-a729-184e89b9a558"));
	Assert.assertTrue(reader.evaluateString("//*:summary/text()").contains("abstract"));
	Assert.assertTrue(reader.evaluateString("//*:rights/text()").contains("geossdatacore"));

	Assert.assertTrue(reader.evaluateString("//*:category[@label=\"dataset\"]/@term").contains("hlevel"));
	Assert.assertTrue(reader.evaluateString("//*:category[@label=\"kwd1\"]/@term").contains("keywords"));
	Assert.assertTrue(reader.evaluateString("//*:category[@label=\"kwd2\"]/@term").contains("keywords"));
	Assert.assertTrue(reader.evaluateString("//*:category[@label=\"kwd3\"]/@term").contains("keywords"));

	Assert.assertTrue(reader.evaluateString("//*:category[@label=\"geoscientificInformation\"]/@term").contains("topic"));

	Assert.assertTrue(reader.evaluateString("//*:logo/text()").contains("http://tiles.geodab.eu/geodab"));

	Assert.assertTrue(reader.evaluateString("//*:orgName/text()").contains("European Commission"));
	Assert.assertTrue(reader.evaluateString("//*:role/text()").contains("originator"));

	Assert.assertTrue(reader.evaluateString("//*:verticalextent[1]/*:minimum/text()").contains("0.0"));
	Assert.assertTrue(reader.evaluateString("//*:verticalextent[1]/*:maximum/text()").contains("0.0"));

	Assert.assertTrue(reader.evaluateString("//*:verticalextent[2]/*:minimum/text()").contains("1.0"));
	Assert.assertTrue(reader.evaluateString("//*:verticalextent[2]/*:maximum/text()").contains("1.0"));

	Assert.assertTrue(reader.evaluateString("//*:box/text()").contains("46.845127953652586"));
	Assert.assertTrue(reader.evaluateString("//*:box/text()").contains("124.31188688551244"));
	Assert.assertTrue(reader.evaluateString("//*:box/text()").contains("47.84592105208886"));
	Assert.assertTrue(reader.evaluateString("//*:box/text()").contains("124.31188688551244"));

	Assert.assertTrue(reader.evaluateString("//*:dtstart/text()").contains("2020-01-02T02:51:19.024Z"));
	Assert.assertTrue(reader.evaluateString("//*:dtend/text()").contains("2020-01-02T04:15:55.000Z"));
	Assert.assertTrue(reader.evaluateString("//*:start/text()").contains("2020-01-02T02:51:19.024Z"));
	Assert.assertTrue(reader.evaluateString("//*:stop/text()").contains("2020-01-02T04:15:55.000Z"));

	Assert.assertTrue(reader.evaluateString("//*:parentID/text()").contains("satellitescene_collection_prefix_SENTINEL_2B"));
	Assert.assertTrue(reader.evaluateString("//*:harvested/text()").contains("true"));
	Assert.assertTrue(reader.evaluateString("//*:mag-level/text()").contains("5"));

	Assert.assertTrue(reader.evaluateString("//*:acquisition/*:platformid/text()").contains("2017-013A"));
	Assert.assertTrue(reader.evaluateString("//*:acquisition/*:platform/text()").contains("SENTINEL_2B"));
	Assert.assertTrue(reader.evaluateString("//*:acquisition/*:instrument/text()").contains("Multi-Spectral Instrument"));
	Assert.assertTrue(reader.evaluateString("//*:acquisition/*:instrumentOpMode/text()").contains("INS-NOBS"));
	Assert.assertTrue(reader.evaluateString("//*:acquisition/*:productType/text()").contains("S2MSI1C"));
	Assert.assertTrue(reader.evaluateString("//*:acquisition/*:cloud_cover_percentage/text()").contains("17.5957"));
	Assert.assertTrue(reader.evaluateString("//*:acquisition/*:relativeOrbit/text()").contains("132"));
	Assert.assertTrue(reader.evaluateString("//*:acquisition/*:footprint/text()").contains("MULTIPOLYGON (((125.04709758458236"));
	Assert.assertTrue(reader.evaluateString("//*:acquisition/*:footprint/text()").contains("47.01650446816934"));
	Assert.assertTrue(reader.evaluateString("//*:acquisition/*:footprint/text()").contains("125.18585337696528"));
	Assert.assertTrue(reader.evaluateString("//*:acquisition/*:footprint/text()").contains("46.845127953652586"));

	Assert.assertTrue(reader.evaluateString("//*:acquisition/*:processingbaseline/text()").contains("02.08"));
	Assert.assertTrue(reader.evaluateString("//*:acquisition/*:processinglevel/text()").contains("Level-1C"));
	Assert.assertTrue(reader.evaluateString("//*:acquisition/*:s3InstrumentIdx/text()").contains("MSI"));

	Assert.assertTrue(reader.evaluateString("//*:acquisition/*:s3Timeliness/text()").equals("A"));
	Assert.assertTrue(reader.evaluateString("//*:acquisition/*:s3ProductLevel/text()").equals("B"));
	Assert.assertTrue(reader.evaluateString("//*:acquisition/*:startOrbitNumber/text()").contains("14744"));
	Assert.assertTrue(reader.evaluateString("//*:acquisition/*:orbitdirection/text()").contains("DESCENDING"));

	Assert.assertTrue(reader.evaluateString("//*:acquisition/*:stopRelativeOrbitNumber/text()").equals("I"));
	Assert.assertTrue(reader.evaluateString("//*:acquisition/*:stopOrbitNumber/text()").equals("J"));

	Assert.assertTrue(reader.evaluateString("//*:acquisition/*:status/text()").equals("K"));

	Assert.assertTrue(reader.evaluateString("//*:satelliteCollectionQueryable/text()").equals("QUERYABLES"));

	//
	// FOLLOWING ELEMENTS ARE NOT MAPPED EVEN IF PROVIDED BY THE TEMPLATES, THEY ARE NOT USEFUL INDEED
	//

	// Assert.assertTrue(reader.evaluateString("//*:acquisition/*:sensorPolarisation/text()").equals("C"));
	// Assert.assertTrue(reader.evaluateString("//*:acquisition/*:productconsolidation/text()").equals("D"));
	// Assert.assertTrue(reader.evaluateString("//*:acquisition/*:missiondatatakeid/text()").equals("E"));
	// Assert.assertTrue(reader.evaluateString("//*:acquisition/*:productclass/text()").equals("F"));
	// Assert.assertTrue(reader.evaluateString("//*:acquisition/*:acquisitiontype/text()").equals("G"));
	// Assert.assertTrue(reader.evaluateString("//*:acquisition/*:slicenumber/text()").equals("H"));

	Assert.assertTrue(reader.evaluateBoolean("exists(//*:distributionInfo)"));

	Assert.assertTrue(reader
		.evaluateString(
			"//*:distributionInfo/*:MD_Distribution/*:transferOptions/*:MD_DigitalTransferOptions/*:transferSize/*:Real/text()")
		.equals("574.76"));

	Assert.assertTrue(reader.evaluateString("count(//*:onLine)").equals("20"));

    }

    @Test
    public void test2() throws JAXBException, GSException, SAXException, IOException, XPathExpressionException {

	//
	// time series report
	//

	GSResource resource = GSResource.create(getClass().getClassLoader().getResourceAsStream("atom-mapper-dataset-test-2.xml"));

	XMLDocumentReader reader = map(resource);

	Assert.assertTrue(reader.evaluateString("//*:title/text()").contains("ALFREDO CHAVES - PRESSAO ATMOSFERICA"));
	Assert.assertTrue(reader.evaluateString("//*:id/text()").contains("6d6b470d-3d01-4f58-a5c0-b27c1bbbe0ef"));
	Assert.assertTrue(reader.evaluateString("//*:content/text()").contains("JSON"));

	Assert.assertTrue(reader.evaluateString("//*:summary/text()").contains("Acquisition made at station: ALFREDO CHAVES"));

	Assert.assertTrue(reader.evaluateString("//*:category[@label=\"dataset\"]/@term").contains("hlevel"));
	Assert.assertTrue(reader.evaluateString("//*:category[@label=\"Automatica\"]/@term").contains("keywords"));
	Assert.assertTrue(reader.evaluateString("//*:category[@label=\"Operante\"]/@term").contains("keywords"));
	Assert.assertTrue(reader.evaluateString("//*:category[@label=\"ES\"]/@term").contains("keywords"));

	Assert.assertTrue(reader.evaluateString("//*:category[@label=\"inlandWaters\"]/@term").contains("topic"));

	Assert.assertTrue(reader.evaluateString("//*:orgName/text()").contains("National Metereology Institute of Brazil"));

	Assert.assertTrue(reader.evaluateString("//*:verticalextent/*:minimum/text()").contains("14.19"));
	Assert.assertTrue(reader.evaluateString("//*:verticalextent/*:maximum/text()").contains("14.19"));

	Assert.assertTrue(reader.evaluateString("//*:box/text()").contains("-20.636526"));
	Assert.assertTrue(reader.evaluateString("//*:box/text()").contains("-40.741818"));

	Assert.assertTrue(reader.evaluateString("//*:dtstart/text()").contains("2006-11-03T00:00:00Z"));
	Assert.assertEquals((double) (new Date().getTime()),
		(double) (ISO8601DateTimeUtils.parseISO8601ToDate(reader.evaluateString("//*:dtend/text()")).get().getTime()), 10000);
	Assert.assertTrue(reader.evaluateString("//*:start/text()").contains("2006-11-03T00:00:00Z"));
	Assert.assertEquals((double) (new Date().getTime()),
		(double) (ISO8601DateTimeUtils.parseISO8601ToDate(reader.evaluateString("//*:stop/text()")).get().getTime()), 10000);

	Assert.assertTrue(reader.evaluateString("//*:harvested/text()").contains("true"));

	Assert.assertTrue(reader.evaluateBoolean("exists(//*:distributionInfo)"));

	Assert.assertTrue(reader
		.evaluateString("//*:distributionInfo/*:MD_Distribution/*:distributionFormat/*:MD_Format/*:name/*:CharacterString/text()")
		.equals("JSON"));

	Assert.assertTrue(reader.evaluateString("count(//*:onLine)").equals("2"));

    }

    @Test
    public void test3() throws JAXBException, GSException, SAXException, IOException, XPathExpressionException {

	//
	// landsat extensions
	//

	GSResource resource = GSResource.create(getClass().getClassLoader().getResourceAsStream("landsat-dataset.xml"));

	XMLDocumentReader reader = map(resource);

	Assert.assertTrue(reader.evaluateString("//*:acquisition/*:platformid/text()").contains("LANDSAT_8"));
	Assert.assertTrue(reader.evaluateString("//*:acquisition/*:platform/text()").contains("Platform LANDSAT_8"));

	Assert.assertTrue(
		reader.evaluateString("//*:acquisition/*:instrument/text()").contains("Operational Land Imager Thermal Infrared Sensor"));

	Assert.assertTrue(reader.evaluateString("//*:acquisition/*:productType/text()").contains("L1TP"));
	Assert.assertTrue(reader.evaluateString("//*:acquisition/*:cloud_cover_percentage/text()").contains("15.46"));
	Assert.assertTrue(reader.evaluateString("//*:acquisition/*:row/text()").contains("75"));
	Assert.assertTrue(reader.evaluateString("//*:acquisition/*:path/text()").contains("46"));

	Assert.assertTrue(reader.evaluateString("//*:satelliteCollectionQueryable/text()")
		.equals("prodType,cloudcp,pubDatefrom,pubDateuntil,row,path"));
    }

    /**
     * @param resource
     * @return
     * @throws GSException
     * @throws IOException
     * @throws SAXException
     */
    private XMLDocumentReader map(GSResource resource) throws GSException, SAXException, IOException {

	AtomGPResultSetMapper mapper = new AtomGPResultSetMapper();

	DiscoveryMessage message = createMessage();

	String mapped = mapper.map(message, resource);

	System.out.println(mapped);

	return new XMLDocumentReader(mapped);
    }

    /**
     * @return
     */
    private DiscoveryMessage createMessage() {

	DiscoveryMessage message = new DiscoveryMessage();

	WebRequest request = WebRequest.createGET("http://localhost");

	message.setWebRequest(request);

	return message;
    }
}
