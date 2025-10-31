package eu.essi_lab.model.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import eu.essi_lab.model.resource.*;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

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

	SA_ElementWrapper wrapper1 = SA_ElementWrapper.of(MetadataElement.KEYWORD_SA);

	wrapper1.setValue("value1");
	wrapper1.setUri("uri1");
	wrapper1.setUriTitle("uri_title1");
	wrapper1.setSA_MatchType("SA_match_type1");
	wrapper1.setSA_Uri("SA_uri1");
	wrapper1.setSA_UriTitle("SA_uri_title1");

	SA_ElementWrapper wrapper2 = SA_ElementWrapper.of(MetadataElement.KEYWORD_SA);

	wrapper2.setValue("value2");
	wrapper2.setUri("uri2");
	wrapper2.setUriTitle("uri_title2");
	wrapper2.setSA_MatchType("SA_match_type2");
	wrapper2.setSA_Uri("SA_uri2");
	wrapper2.setSA_UriTitle("SA_uri_title2");

	SA_ElementWrapper wrapper3 = SA_ElementWrapper.of(MetadataElement.PARAMETER_SA);

	wrapper3.setValue("value3");
	wrapper3.setUri("uri3");
	wrapper3.setUriTitle("uri_title3");
	wrapper3.setSA_MatchType("SA_match_type3");
	wrapper3.setSA_Uri("SA_uri3");
	wrapper3.setSA_UriTitle("SA_uri_title3");

	OrganizationElementWrapper orgWrapper = OrganizationElementWrapper.get();

	orgWrapper.setHomePageURL("homePageURL");
	orgWrapper.setEmail("email");
	orgWrapper.setIndividualName("indName");
	orgWrapper.setRole("role");
	orgWrapper.setOrgName("orgName");
	orgWrapper.setIndividualURI("indURI");
	orgWrapper.setOrgURI("orgURI");

	//
	//
	//

	Dataset dataset = new Dataset();
	ExtensionHandler handler = dataset.getExtensionHandler();

	handler.setSatelliteScene(scene);
	handler.setWorldCereal(map);

	handler.addComposedElement(wrapper1.getElement());
	handler.addComposedElement(wrapper2.getElement());

	handler.addComposedElement(wrapper3.getElement());

	handler.addComposedElement(orgWrapper.getElement());

	System.out.println(dataset.asString(true));

	//
	//
	//

	ComposedElement orgComposedElement = handler.getComposedElements(MetadataElement.ORGANIZATION.getName()).getFirst();

	Assert.assertTrue(orgComposedElement.getName().equals(MetadataElement.ORGANIZATION.getName()));
	Assert.assertEquals("homePageURL", orgComposedElement.getElement().getProperty("homePageURL").get().getValue());
	Assert.assertEquals("email", orgComposedElement.getElement().getProperty("email").get().getValue());

	//
	//
	//

	List<ComposedElement> kwd_SA1 = handler.getComposedElements(MetadataElement.KEYWORD_SA.getName());

	Assert.assertFalse(kwd_SA1.isEmpty());

	ComposedElement composedElement_0 = kwd_SA1.getFirst();

	Assert.assertTrue(composedElement_0.getName().equals(MetadataElement.KEYWORD_SA.getName()));
	Assert.assertEquals("value1", composedElement_0.getElement().getProperty("value").get().getValue());
	Assert.assertEquals("uri1", composedElement_0.getElement().getProperty("uri").get().getValue());

	//
	//
	//

	List<ComposedElement> kwd_SA2 = handler.getComposedElements(MetadataElement.KEYWORD_SA.getName());

	Assert.assertFalse(kwd_SA2.isEmpty());

	ComposedElement composedElement_1 = kwd_SA1.get(1);

	Assert.assertTrue(composedElement_1.getName().equals(MetadataElement.KEYWORD_SA.getName()));
	Assert.assertEquals("value2", composedElement_1.getElement().getProperty("value").get().getValue());
	Assert.assertEquals("uri2", composedElement_1.getElement().getProperty("uri").get().getValue());

	//
	//
	//

	ComposedElement param_SA = handler.getComposedElements(MetadataElement.PARAMETER_SA.getName()).getFirst();

	Assert.assertTrue(param_SA.getName().equals(MetadataElement.PARAMETER_SA.getName()));
	Assert.assertEquals("value3", param_SA.getElement().getProperty("value").get().getValue());
	Assert.assertEquals("uri3", param_SA.getElement().getProperty("uri").get().getValue());

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
	Assert.assertEquals("channel1", scene.getPolChannels().getFirst());

	Document doc = scene.asDocument(false);
	scene = SatelliteScene.create(doc);

	Assert.assertEquals("acquisitionType", scene.getAcquisitionType());
	Assert.assertEquals(1, scene.getPolChannels().size());
	Assert.assertEquals("channel1", scene.getPolChannels().getFirst());

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

	Assert.assertEquals("watermelon", map.getCropTypes().getFirst().getLabel());
	Assert.assertEquals("1103020050", map.getCropTypes().getFirst().getCode());

	Assert.assertEquals("Fully Irrigated - surface", map.getIrrigationTypes().getFirst().getLabel());
	Assert.assertEquals("213", map.getIrrigationTypes().getFirst().getCode());

	Assert.assertEquals("mixed_cropland", map.getLandCoverTypes().getFirst().getLabel());
	Assert.assertEquals("14", map.getLandCoverTypes().getFirst().getCode());

	Document doc = map.asDocument(false);
	map = WorldCerealMap.create(doc);

	Assert.assertEquals("watermelon", map.getCropTypes().getFirst().getLabel());
	Assert.assertEquals("1103020050", map.getCropTypes().getFirst().getCode());

	Assert.assertEquals("Fully Irrigated - surface", map.getIrrigationTypes().getFirst().getLabel());
	Assert.assertEquals("213", map.getIrrigationTypes().getFirst().getCode());

	Assert.assertEquals("mixed_cropland", map.getLandCoverTypes().getFirst().getLabel());
	Assert.assertEquals("14", map.getLandCoverTypes().getFirst().getCode());

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

	Assert.assertEquals("watermelon", map.getCropTypes().getFirst().getLabel());
	Assert.assertEquals("1103020050", map.getCropTypes().getFirst().getCode());

	Assert.assertEquals("Fully Irrigated - surface", map.getIrrigationTypes().getFirst().getLabel());
	Assert.assertEquals("213", map.getIrrigationTypes().getFirst().getCode());

	Assert.assertEquals("mixed_cropland", map.getLandCoverTypes().getFirst().getLabel());
	Assert.assertEquals("14", map.getLandCoverTypes().getFirst().getCode());
    }

}
