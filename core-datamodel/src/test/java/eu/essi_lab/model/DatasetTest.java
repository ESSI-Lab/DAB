package eu.essi_lab.model;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import eu.essi_lab.iso.datamodel.classes.Distribution;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.xml.XMLFactories;
import eu.essi_lab.model.index.IndexedElement;
import eu.essi_lab.model.index.IndexedMetadataElement;
import eu.essi_lab.model.index.jaxb.BoundingBox;
import eu.essi_lab.model.index.jaxb.IndexesMetadata;
import eu.essi_lab.model.resource.AugmentedMetadataElement;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.ExtendedMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.HarmonizedMetadata;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.model.resource.ResourceType;
import net.opengis.iso19139.gmd.v_20060504.MDMetadataType;

public class DatasetTest {

    private static final String ORIGINAL_MD = "<original><id>FILE_ID</id><title>TITLE</title><url>http://endpoint</url><parent>PARENT_ID</parent></original>";
    private static final String SOURCE_LABEL = "SOURCE_LABEL<>!%&()=?^£";
    private static final String newTitle = "New title";
    private static final String newId = "New identifier";

    @Test
    public void marshalUnmarshalTest() {

	try {
	    Dataset dataset = createDataset();

	    // --------------------------
	    //
	    // marshal test
	    //
	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    dataset.toStream(outputStream);

	    System.out.println(outputStream);

	    // --------------------------
	    //
	    // unmarshal test 1
	    //
	    InputStream stream = new ByteArrayInputStream(outputStream.toByteArray());
	    unmarshalTest(stream, newId);

	    // --------------------------
	    //
	    // unmarshal test 2
	    //
	    stream = dataset.asStream();
	    unmarshalTest(stream, newId);

	    // --------------------------
	    //
	    // unmarshal test 3
	    //
	    Node asNode = dataset.asDocument(true);
	    GSResource gsResource = GSResource.create(asNode);
	    checkMetadataElements(gsResource, newId);

	} catch (Exception e) {

	    e.printStackTrace();

	    fail("Exception thrown");
	}
    }

    private Dataset createDataset() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
	Dataset dataset = new Dataset();

	HarmonizedMetadata hm = dataset.getHarmonizedMetadata();

	Assert.assertNotNull(hm.getAugmentedMetadataElements());
	Assert.assertNotNull(hm.getCoreMetadata());
	Assert.assertNotNull(dataset.getIndexesMetadata());

	GSSource source = new GSSource();
	source.setBrokeringStrategy(BrokeringStrategy.HARVESTED);
	source.setEndpoint("http://endpoint");
	source.setLabel(SOURCE_LABEL);
	source.setUniqueIdentifier("SOURCE_UNIQUE_ID");
	source.setVersion("SOURCE_VERSION");
	dataset.setSource(source);

	dataset.setOriginalId("ORIGINAL_ID");
	dataset.setPublicId("PUBLIC_ID");
	dataset.setPrivateId("PRIVATE_ID");

	// --------------------------
	//
	// Indexes metadata
	//

	IndexesMetadata indexesMetadata = dataset.getIndexesMetadata();

	Assert.assertFalse(indexesMetadata.hasBoundingBox());

	Assert.assertFalse(indexesMetadata.readBoundingBox().isPresent());

	indexesMetadata.write(new IndexedElement(MetadataElement.ABSTRACT.getName(), "ABSTRACT"));

	indexesMetadata.write(new IndexedElement(MetadataElement.ABSTRACT.getName(), "ABSTRACT2"));

	indexesMetadata.write(new IndexedElement(MetadataElement.TITLE.getName(), "TITLE"));

	BoundingBox boxIndexMetadata = new BoundingBox();
	boxIndexMetadata.setArea("area");
	boxIndexMetadata.setCenter("center");
	boxIndexMetadata.setNe("ne");
	boxIndexMetadata.setNw("nw");
	boxIndexMetadata.setSw("sw");
	boxIndexMetadata.setSe("se");

	indexesMetadata.write(new IndexedMetadataElement(boxIndexMetadata) {
	    @Override
	    public void defineValues(GSResource resource) {
	    }
	});

	// --------------------------
	//
	// OriginalMetadata
	//
	OriginalMetadata originalMetadata = dataset.getOriginalMetadata();

	originalMetadata.setSchemeURI("http://scheme-uri");
	dataset.setOriginalId("ORIGINAL_ID");
	originalMetadata.setMetadata(ORIGINAL_MD);

	// --------------------------
	//
	// CoreMetadata
	//
	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	coreMetadata.setTitle("TITLE");
	coreMetadata.setIdentifier("FILE_ID");
	coreMetadata.getMIMetadata().setHierarchyLevelName("dataset");
	coreMetadata.getMIMetadata().setParentIdentifier("PARENT_ID");

	MIPlatform miPlatform = new MIPlatform();
	Assert.assertNull(miPlatform.getDescription());
	miPlatform.setDescription("desc");
	Assert.assertEquals("desc", miPlatform.getDescription());
	coreMetadata.getMIMetadata().addMIPlatform(miPlatform);

	Distribution distribution = new Distribution();
	Online online = new Online();
	online.setDescription("DESCRIPTION");
	online.setLinkage("http://endpoint");
	distribution.addDistributionOnline(online);
	coreMetadata.getMIMetadata().setDistribution(distribution);
	hm.setCoreMetadata(coreMetadata);

	// --------------------------
	//
	// AugmentedMetadata
	//

	AugmentedMetadataElement element = new AugmentedMetadataElement();
	element.setName(MetadataElement.TITLE);
	element.setOldValue(dataset.getHarmonizedMetadata().getCoreMetadata().getTitle());
	element.setNewValue(newTitle);
	element.setUpdateTimeStamp();

	dataset.getHarmonizedMetadata().getCoreMetadata().setTitle(newTitle);
	dataset.getHarmonizedMetadata().getAugmentedMetadataElements().add(element);

	// -----------

	element = new AugmentedMetadataElement();
	element.setName(MetadataElement.IDENTIFIER);
	element.setOldValue(dataset.getHarmonizedMetadata().getCoreMetadata().getIdentifier());
	element.setNewValue(newId);
	element.setUpdateTimeStamp();

	dataset.getHarmonizedMetadata().getCoreMetadata().setIdentifier(newId);
	dataset.getHarmonizedMetadata().getAugmentedMetadataElements().add(element);

	// -----------

	String newAbstract = "New abstract";

	element = new AugmentedMetadataElement();
	element.setName(MetadataElement.ABSTRACT);
	element.setOldValue(dataset.getHarmonizedMetadata().getCoreMetadata().getAbstract());
	element.setNewValue(newAbstract);
	element.setUpdateTimeStamp();

	dataset.getHarmonizedMetadata().getCoreMetadata().setAbstract(newAbstract);
	dataset.getHarmonizedMetadata().getAugmentedMetadataElements().add(element);

	// --------------------------
	//
	// ExtendedMetadata
	//

	ExtendedMetadata extendedMetadata = dataset.getHarmonizedMetadata().getExtendedMetadata();

	extendedMetadata.add("title", "title");

	extendedMetadata.add("title", "title");

	String title = extendedMetadata.getTextContent("title");
	Assert.assertEquals("title", title);

	// creates a node to add
	DocumentBuilderFactory instance = XMLFactories.newDocumentBuilderFactory();
	instance.setNamespaceAware(true);

	DocumentBuilder builder = instance.newDocumentBuilder();
	Element el = builder.newDocument().createElementNS(CommonNameSpaceContext.GMD_NS_URI, "gmd:element");
	Element innerElement = el.getOwnerDocument().createElementNS(CommonNameSpaceContext.GMI_NS_URI, "gmi:innerElement");
	innerElement.setTextContent("innerElementText");
	el.appendChild(innerElement);

	// adds the node
	extendedMetadata.add(el);

	List<Node> list = extendedMetadata.get("//gmd:element");
	Assert.assertEquals(1, list.size());

	list = extendedMetadata.get("//gmd:element/gmi:innerElement");
	Assert.assertEquals(1, list.size());

	String textContent = extendedMetadata.getTextContent("gmi", "innerElement");
	Assert.assertEquals("innerElementText", textContent);

	// multiple elements
	extendedMetadata.add("multiple", "a1");
	extendedMetadata.add("multiple", "a2");
	extendedMetadata.add("multiple", "a3");

	List<String> multiples = extendedMetadata.getTextContents("multiple");
	Assert.assertTrue(multiples.contains("a1"));
	Assert.assertTrue(multiples.contains("a2"));
	Assert.assertTrue(multiples.contains("a3"));

	return dataset;
    }

    @Test
    public void unmarshalTest() throws XPathExpressionException {

	unmarshalTest(DatasetTest.class.getClassLoader().getResourceAsStream("dataset1.xml"), "PUBLIC_ID");
    }

    private void checkMetadataElements(GSResource dataset, String newId) throws XPathExpressionException {

	GSSource source = dataset.getSource();
	Assert.assertNotNull(source);

	ResourceType resourceType = dataset.getResourceType();
	Assert.assertEquals(ResourceType.DATASET, resourceType);

	// --------------------------
	//
	// OriginalMetadata
	//
	OriginalMetadata originalMetadata = dataset.getOriginalMetadata();

	String originalMetadataString = originalMetadata.getMetadata();
	Assert.assertEquals(ORIGINAL_MD, originalMetadataString);

	String schemeURI = originalMetadata.getSchemeURI();
	Assert.assertEquals("http://scheme-uri", schemeURI);

	String originalId1 = dataset.getOriginalId().get();
	Assert.assertEquals("ORIGINAL_ID", originalId1);

	BrokeringStrategy brokeringStrategy = source.getBrokeringStrategy();
	Assert.assertEquals(brokeringStrategy, BrokeringStrategy.HARVESTED);

	String endpoint = source.getEndpoint();
	Assert.assertEquals("http://endpoint", endpoint);

	String label = source.getLabel();
	Assert.assertEquals(SOURCE_LABEL, label);

	String uniqueIdentifier = source.getUniqueIdentifier();
	Assert.assertEquals("SOURCE_UNIQUE_ID", uniqueIdentifier);

	String version = source.getVersion();
	Assert.assertEquals("SOURCE_VERSION", version);

	// --------------------------
	//
	// HarmonizedMetadata
	//
	HarmonizedMetadata harmonizedMetadata = dataset.getHarmonizedMetadata();
	Assert.assertNotNull(harmonizedMetadata);

	//
	// IndexesMetadata
	//
	IndexesMetadata indexesMetadata = dataset.getIndexesMetadata();

	Assert.assertEquals(2, indexesMetadata.read(MetadataElement.ABSTRACT).size());

	Assert.assertEquals("ABSTRACT", indexesMetadata.read(MetadataElement.ABSTRACT).get(0));
	Assert.assertEquals("ABSTRACT2", indexesMetadata.read(MetadataElement.ABSTRACT).get(1));

	Assert.assertEquals("TITLE", indexesMetadata.read(MetadataElement.TITLE).get(0));

	Assert.assertTrue(indexesMetadata.hasBoundingBox());

	Optional<BoundingBox> bbox = indexesMetadata.readBoundingBox();
	Assert.assertEquals("center", bbox.get().getCenter());

	List<String> values = indexesMetadata.read(MetadataElement.ABSTRACT);
	Assert.assertEquals("ABSTRACT", values.get(0));
	Assert.assertEquals("ABSTRACT2", values.get(1));

	String originalId2 = dataset.getOriginalId().get();
	Assert.assertEquals("ORIGINAL_ID", originalId2);

	String publicId = dataset.getPublicId();
	Assert.assertEquals(newId, publicId);

	String privateId = dataset.getPrivateId();
	Assert.assertEquals("PRIVATE_ID", privateId);

	brokeringStrategy = source.getBrokeringStrategy();
	Assert.assertEquals(BrokeringStrategy.HARVESTED, brokeringStrategy);

	endpoint = source.getEndpoint();
	Assert.assertEquals("http://endpoint", endpoint);

	label = source.getLabel();
	Assert.assertEquals(SOURCE_LABEL, label);

	uniqueIdentifier = source.getUniqueIdentifier();
	Assert.assertEquals("SOURCE_UNIQUE_ID", uniqueIdentifier);

	version = source.getVersion();
	Assert.assertEquals("SOURCE_VERSION", version);

	// --------------------------
	//
	// AugmentedMetadata
	//

	List<AugmentedMetadataElement> elements = harmonizedMetadata.getAugmentedMetadataElements();
	Assert.assertEquals(3, elements.size());

	Assert.assertEquals("TITLE", elements.get(0).getOldValue());
	Assert.assertEquals(newTitle, elements.get(0).getNewValue());

	// --------------------------
	//
	// CoreMetadata
	//
	CoreMetadata coreMetadata = harmonizedMetadata.getCoreMetadata();
	Assert.assertNotNull(coreMetadata);

	MIMetadata mdMetadata = coreMetadata.getMIMetadata();
	Assert.assertNotNull(mdMetadata);

	MDMetadataType elementType = mdMetadata.getElementType();
	Assert.assertNotNull(elementType);

	String citationTitle = mdMetadata.getDataIdentification().getCitationTitle();
	Assert.assertEquals(newTitle, citationTitle);

	String title = coreMetadata.getTitle();
	Assert.assertEquals(newTitle, title);

	// --------------------------
	//
	// ExtendedMetadata
	//
	ExtendedMetadata extendedMetadata = harmonizedMetadata.getExtendedMetadata();

	title = extendedMetadata.getTextContent("title");
	Assert.assertEquals("title", title);

	String text = extendedMetadata.get("//gmi:innerElement").get(0).getTextContent();
	Assert.assertEquals("innerElementText", text);
    }

    private void unmarshalTest(InputStream stream, String identifier) throws XPathExpressionException {

	try {
	    Dataset dataset = (Dataset) GSResource.create(stream);

	    checkMetadataElements(dataset, identifier);

	} catch (JAXBException e) {

	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

}
