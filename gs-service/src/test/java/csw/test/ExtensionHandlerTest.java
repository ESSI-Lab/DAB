package csw.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.SatelliteScene;
import eu.essi_lab.model.resource.composed.ComposedElement;
import eu.essi_lab.model.resource.worldcereal.WorldCerealItem;
import eu.essi_lab.model.resource.worldcereal.WorldCerealMap;
import junit.framework.TestCase;

public class ExtensionHandlerTest {

    @Test
    public void test() {
	GSResource res = new Dataset();
	ExtensionHandler extendedMetadataHandler = res.getExtensionHandler();
	TestCase.assertTrue(extendedMetadataHandler.getOriginatorOrganisationIdentifiers().isEmpty());
	extendedMetadataHandler.addOriginatorOrganisationIdentifier("CNR");
	extendedMetadataHandler.addOriginatorOrganisationIdentifier("MARIS");
	TestCase.assertTrue(extendedMetadataHandler.getOriginatorOrganisationIdentifiers().size() == 2);
	TestCase.assertTrue(extendedMetadataHandler.getOriginatorOrganisationIdentifiers().contains("CNR"));
	TestCase.assertTrue(extendedMetadataHandler.getOriginatorOrganisationIdentifiers().contains("MARIS"));
    }

    @Test
    public void mixedTest() throws UnsupportedEncodingException, JAXBException {

	SatelliteScene scene = new SatelliteScene();
	scene.setAcquisitionType("acquisitionType");
	scene.addPolChannel("channel1");

	WorldCerealMap map = new WorldCerealMap();
	map.setWorldCerealQueryables("cropTypes,landCoverTypes,irrigationTypes");
	map.setCropTypeConfidence(98.0);
	map.setIrrigationTypeConfidence(92.0);
	map.setLcTypeConfidence(10.0);

	ComposedElement composedElement1 = MetadataElement.KEYWORD_SA.getComposeElement().get();

	composedElement1.getProperty("value").get().setValue("erggvb");
	composedElement1.getProperty("uri").get().setValue("xvfhg");
	composedElement1.getProperty("SA_uri").get().setValue("4566g");
	composedElement1.getProperty("SA_matchType").get().setValue("xzcx");

	ComposedElement composedElement2 = MetadataElement.PARAMETER_SA.getComposeElement().get();

	composedElement2.getProperty("value").get().setValue("dsad");
	composedElement2.getProperty("uri").get().setValue("czxczxcz");
	composedElement2.getProperty("SA_uri").get().setValue("asdasdasd");
	composedElement2.getProperty("SA_matchType").get().setValue("vxcxxvc");

	Dataset dataset = new Dataset();
	ExtensionHandler handler = dataset.getExtensionHandler();

	handler.setSatelliteScene(scene);
	handler.setWorldCereal(map);
	handler.addComposedElement(composedElement1);
	handler.addComposedElement(composedElement2);

	System.out.println(dataset.asString(true));

	//
	//
	//

	Optional<ComposedElement> kwd_SA = handler.getComposedElement(MetadataElement.KEYWORD_SA.getName());

	Assert.assertTrue(kwd_SA.isPresent());

	Assert.assertTrue(kwd_SA.get().getName().equals(MetadataElement.KEYWORD_SA.getName()));

	//
	//
	//

	Optional<ComposedElement> param_SA = handler.getComposedElement(MetadataElement.PARAMETER_SA.getName());

	Assert.assertTrue(param_SA.isPresent());

	Assert.assertTrue(param_SA.get().getName().equals(MetadataElement.PARAMETER_SA.getName()));
	
	//
	//
	//
	
	Assert.assertTrue(handler.getWorldCereal().isPresent());
	Assert.assertTrue(handler.getSatelliteScene().isPresent());
    }

    @Test
    public void satelliteSceneTest() throws JAXBException, ParserConfigurationException, SAXException, IOException {

	SatelliteScene scene = new SatelliteScene();
	scene.setAcquisitionType("acquisitionType");
	scene.addPolChannel("channel1");

	Assert.assertEquals("acquisitionType", scene.getAcquisitionType());
	Assert.assertEquals(1, scene.getPolChannels().size());
	Assert.assertEquals("channel1", scene.getPolChannels().get(0));

	Document doc = scene.asDocument(false);
	scene = SatelliteScene.create(doc);

	Assert.assertEquals("acquisitionType", scene.getAcquisitionType());
	Assert.assertEquals(1, scene.getPolChannels().size());
	Assert.assertEquals("channel1", scene.getPolChannels().get(0));

	scene.addPolChannel("channel2");
	scene.addPolChannel("channel3");

	Assert.assertEquals(3, scene.getPolChannels().size());
	Assert.assertEquals(true, scene.getPolChannels().stream().anyMatch(c -> c.equals("channel1")));
	Assert.assertEquals(true, scene.getPolChannels().stream().anyMatch(c -> c.equals("channel2")));
	Assert.assertEquals(true, scene.getPolChannels().stream().anyMatch(c -> c.equals("channel3")));

	Dataset dataset = new Dataset();
	ExtensionHandler handler = dataset.getExtensionHandler();

	handler.setSatelliteScene(scene);

	scene = handler.getSatelliteScene().get();

	System.out.println(dataset.asString(true));

	Assert.assertEquals("acquisitionType", scene.getAcquisitionType());

	Assert.assertEquals(3, scene.getPolChannels().size());
	Assert.assertEquals(true, scene.getPolChannels().stream().anyMatch(c -> c.equals("channel1")));
	Assert.assertEquals(true, scene.getPolChannels().stream().anyMatch(c -> c.equals("channel2")));
	Assert.assertEquals(true, scene.getPolChannels().stream().anyMatch(c -> c.equals("channel3")));

	InputStream asStream = dataset.asStream();
	dataset = Dataset.create(asStream);
	handler = dataset.getExtensionHandler();
	scene = handler.getSatelliteScene().get();

	Assert.assertEquals("acquisitionType", scene.getAcquisitionType());

	Assert.assertEquals(3, scene.getPolChannels().size());
	Assert.assertEquals(true, scene.getPolChannels().stream().anyMatch(c -> c.equals("channel1")));
	Assert.assertEquals(true, scene.getPolChannels().stream().anyMatch(c -> c.equals("channel2")));
	Assert.assertEquals(true, scene.getPolChannels().stream().anyMatch(c -> c.equals("channel3")));
    }

    @Test
    public void worldCerealTest() throws JAXBException, ParserConfigurationException, SAXException, IOException {

	WorldCerealMap map = new WorldCerealMap();
	map.setWorldCerealQueryables("cropTypes,landCoverTypes,irrigationTypes");
	map.setCropTypeConfidence(98.0);
	map.setIrrigationTypeConfidence(92.0);
	map.setLcTypeConfidence(10.0);
	List<WorldCerealItem> cropList = new ArrayList<WorldCerealItem>();
	List<WorldCerealItem> irrList = new ArrayList<WorldCerealItem>();
	List<WorldCerealItem> lcList = new ArrayList<WorldCerealItem>();
	WorldCerealItem ctItem = new WorldCerealItem();
	ctItem.setLabel("watermelon");
	ctItem.setCode("1103020050");
	cropList.add(ctItem);
	WorldCerealItem irrItem = new WorldCerealItem();
	irrItem.setLabel("Fully Irrigated - surface");
	irrItem.setCode("213");
	irrList.add(irrItem);
	WorldCerealItem lcItem = new WorldCerealItem();
	lcItem.setLabel("mixed_cropland");
	lcItem.setCode("14");
	lcList.add(lcItem);
	map.setCropTypes(cropList);
	map.setIrrigationTypes(irrList);
	map.setLandCoverTypes(lcList);

	Assert.assertEquals("watermelon", map.getCropTypes().get(0).getLabel());
	Assert.assertEquals("1103020050", map.getCropTypes().get(0).getCode());

	Assert.assertEquals("Fully Irrigated - surface", map.getIrrigationTypes().get(0).getLabel());
	Assert.assertEquals("213", map.getIrrigationTypes().get(0).getCode());

	Assert.assertEquals("mixed_cropland", map.getLandCoverTypes().get(0).getLabel());
	Assert.assertEquals("14", map.getLandCoverTypes().get(0).getCode());

	Document doc = map.asDocument(false);
	map = WorldCerealMap.create(doc);

	Assert.assertEquals("watermelon", map.getCropTypes().get(0).getLabel());
	Assert.assertEquals("1103020050", map.getCropTypes().get(0).getCode());

	Assert.assertEquals("Fully Irrigated - surface", map.getIrrigationTypes().get(0).getLabel());
	Assert.assertEquals("213", map.getIrrigationTypes().get(0).getCode());

	Assert.assertEquals("mixed_cropland", map.getLandCoverTypes().get(0).getLabel());
	Assert.assertEquals("14", map.getLandCoverTypes().get(0).getCode());

	Assert.assertEquals("cropTypes,landCoverTypes,irrigationTypes", map.getWorldCerealQueryables());

	Assert.assertEquals("98.0", String.valueOf(map.getCropTypeConfidence()));
	Assert.assertEquals("92.0", String.valueOf(map.getIrrigationTypeConfidence()));
	Assert.assertEquals("10.0", String.valueOf(map.getLcTypeConfidence()));

	Dataset dataset = new Dataset();
	ExtensionHandler handler = dataset.getExtensionHandler();

	handler.setWorldCereal(map);

	map = handler.getWorldCereal().get();

	System.out.println(dataset.asString(true));

	Assert.assertEquals("cropTypes,landCoverTypes,irrigationTypes", map.getWorldCerealQueryables());

	InputStream asStream = dataset.asStream();
	dataset = Dataset.create(asStream);
	handler = dataset.getExtensionHandler();
	map = handler.getWorldCereal().get();

	Assert.assertEquals("cropTypes,landCoverTypes,irrigationTypes", map.getWorldCerealQueryables());

	Assert.assertEquals("98.0", String.valueOf(map.getCropTypeConfidence()));
	Assert.assertEquals("92.0", String.valueOf(map.getIrrigationTypeConfidence()));
	Assert.assertEquals("10.0", String.valueOf(map.getLcTypeConfidence()));

	Assert.assertEquals("watermelon", map.getCropTypes().get(0).getLabel());
	Assert.assertEquals("1103020050", map.getCropTypes().get(0).getCode());

	Assert.assertEquals("Fully Irrigated - surface", map.getIrrigationTypes().get(0).getLabel());
	Assert.assertEquals("213", map.getIrrigationTypes().get(0).getCode());

	Assert.assertEquals("mixed_cropland", map.getLandCoverTypes().get(0).getLabel());
	Assert.assertEquals("14", map.getLandCoverTypes().get(0).getCode());
    }

}
