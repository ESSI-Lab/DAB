package eu.essi_lab.model;

import eu.essi_lab.iso.datamodel.classes.*;
import eu.essi_lab.model.resource.*;
import org.junit.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import javax.xml.bind.*;
import javax.xml.parsers.*;
import java.io.*;
import java.util.*;

/**
 * @author Fabrizio
 */
public class DatasetEXT_OnlineTest {

    @Test
    public void getSetTest() throws JAXBException, UnsupportedEncodingException {

	EXT_Online extOnline = create();

	Dataset dataset = new Dataset();

	dataset.getHarmonizedMetadata(). //
		getCoreMetadata(). //
		getMIMetadata(). //
		getDistribution(). //
		addDistributionOnline(extOnline);

	Iterator<Online> onlines = dataset.getHarmonizedMetadata(). //
		getCoreMetadata().//
		getMIMetadata().//
		getDistribution().//
		getDistributionOnlines();

	Assert.assertFalse(onlines.hasNext());

	List<EXT_Online> extendedOnlines = dataset.getHarmonizedMetadata(). //
		getCoreMetadata().//
		getMIMetadata().//
		getDistribution().//
		getExtendedDistributionOnlines();

	Assert.assertFalse(extendedOnlines.isEmpty());

	Online online = new Online();

	online.setLinkage("onlineLinkage");

	dataset.getHarmonizedMetadata(). //
		getCoreMetadata(). //
		getMIMetadata(). //
		getDistribution(). //
		addDistributionOnline(online);

	onlines = dataset.getHarmonizedMetadata(). //
		getCoreMetadata().//
		getMIMetadata().//
		getDistribution().//
		getDistributionOnlines();

	Assert.assertTrue(onlines.hasNext());

	Optional<EXT_Online> firstExtended = dataset.getHarmonizedMetadata(). //
		getCoreMetadata().//
		getMIMetadata().//
		getDistribution().//
		getExtendedDistributionOnline();

	Assert.assertEquals("extendedLinkage", extendedOnlines.getFirst().getLinkage());

	Assert.assertEquals("extendedLinkage", firstExtended.get().getLinkage());

	Assert.assertEquals("onlineLinkage", onlines.next().getLinkage());

	System.out.println(dataset.asString(false));

    }

    @Test
    public void marshallUnmarshallTest() throws JAXBException, IOException, ParserConfigurationException, SAXException {

	EXT_Online extOnline = create();

	Dataset dataset = new Dataset();

	dataset.getHarmonizedMetadata(). //
		getCoreMetadata(). //
		getMIMetadata(). //
		getDistribution(). //
		addDistributionOnline(extOnline);

	Online online = new Online();

	online.setLinkage("onlineLinkage");

	dataset.getHarmonizedMetadata(). //
		getCoreMetadata(). //
		getMIMetadata(). //
		getDistribution(). //
		addDistributionOnline(online);

	check(dataset);

	//
	//
	//

	String asString = dataset.asString(false);  // marshall

	Assert.assertTrue(asString.contains("<gmd:CI_OnlineResource xsi:type=\"extCIOnlineResourceType\">"));

	Dataset fromString = (Dataset) Dataset.create(asString); // unmarshall

	check(fromString);

	//
	//
	//

	InputStream stream = dataset.asStream(); // marshall

	Dataset fromStream = Dataset.create(stream); // unmarshall

	check(fromStream);

	//
	//
	//

	Document asDoc = dataset.asDocument(false); // marshall

	Dataset fromDoc = (Dataset) Dataset.create(asDoc); // unmarshall

	check(fromDoc);
    }

    /**
     * @param dataset
     */
    private void check(Dataset dataset) {

	List<EXT_Online> extendedOnlines = dataset.getHarmonizedMetadata(). //
		getCoreMetadata().//
		getMIMetadata().//
		getDistribution().//
		getExtendedDistributionOnlines();

	Assert.assertFalse(extendedOnlines.isEmpty());

	Iterator<Online> onlines = dataset.getHarmonizedMetadata(). //
		getCoreMetadata().//
		getMIMetadata().//
		getDistribution().//
		getDistributionOnlines();

	Assert.assertTrue(onlines.hasNext());

	Optional<EXT_Online> firstExtended = dataset.getHarmonizedMetadata(). //
		getCoreMetadata().//
		getMIMetadata().//
		getDistribution().//
		getExtendedDistributionOnline();

	Assert.assertEquals("extendedLinkage", extendedOnlines.getFirst().getLinkage());

	Assert.assertEquals("extendedLinkage", firstExtended.get().getLinkage());

	Assert.assertEquals("onlineLinkage", onlines.next().getLinkage());
    }

    /**
     * @return
     */
    private static EXT_Online create() {

	EXT_Online extOnline = new EXT_Online();

	extOnline.setName("name");

	extOnline.setLinkage("extendedLinkage");

	Assert.assertEquals("name", extOnline.getName());

	Assert.assertEquals("extendedLinkage", extOnline.getLinkage());

	EXT_CIOnlineResourceType type = extOnline.getElementType();

	type.setLayerPk("layerPK");

	type.setLayerStyleName("layerStyleName");

	type.setLayerStyleWorkspace("layerStyleWorkspace");

	type.setQueryStringFragment("queryStringFragment");

	type.setTemporal(true);

	return extOnline;
    }
}
