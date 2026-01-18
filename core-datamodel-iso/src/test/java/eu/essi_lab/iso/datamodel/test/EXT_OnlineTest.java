package eu.essi_lab.iso.datamodel.test;

import eu.essi_lab.iso.datamodel.classes.*;
import org.junit.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import javax.xml.bind.*;
import javax.xml.parsers.*;
import java.io.*;

/**
 * @author Fabrizio
 */
public class EXT_OnlineTest {

    @Test
    public void initTest() {

	EXT_Online extOnline = new EXT_Online();

	Assert.assertNull(extOnline.getLinkage());

	Assert.assertNull(extOnline.getName());

	EXT_CIOnlineResourceType type = extOnline.getElementType();

	Assert.assertNull(type.getLayerPk());

	Assert.assertNull(type.getLayerStyleName());

	Assert.assertNull(type.getLayerStyleWorkspace());

	Assert.assertNull(type.getQueryStringFragment());

	Assert.assertFalse(type.isTemporal());
    }

    @Test
    public void setGetTest() {

	EXT_Online extOnline = create();

	Assert.assertEquals("name", extOnline.getName());

	Assert.assertEquals("linkage", extOnline.getLinkage());

	EXT_CIOnlineResourceType type = extOnline.getElementType();

	Assert.assertEquals("layerPK", type.getLayerPk());

	Assert.assertEquals("layerStyleName", type.getLayerStyleName());

	Assert.assertEquals("layerStyleWorkspace", type.getLayerStyleWorkspace());

	Assert.assertEquals("queryStringFragment", type.getQueryStringFragment());

	Assert.assertTrue(type.isTemporal());
    }

    @Test
    public void marshallUnmarshallTest() throws JAXBException, IOException, ParserConfigurationException, SAXException {

	EXT_Online extOnline = create();

	Assert.assertEquals("name", extOnline.getName());

	Assert.assertEquals("linkage", extOnline.getLinkage());

	//
	//
	//

	String string = extOnline.asString(false); // marshall

	System.out.println(string);

	Assert.assertTrue(string.contains("<gmd:EXT_CIOnlineResource"));

	//
	//
	//

	InputStream stream = extOnline.asStream();  // marshall

	EXT_Online fromStream = EXT_Online.create(stream);  // unmarshall

	check(fromStream, extOnline);

	//
	//
	//

	Document doc = extOnline.asDocument(false); // marshall

	EXT_Online fromDoc = EXT_Online.create(doc); // unmarshall

	check(fromDoc, extOnline);
    }

    /**
     * @param original
     * @param marshalled
     */
    private void check(EXT_Online original, EXT_Online marshalled) {

	Assert.assertEquals(original.getName(), marshalled.getName());

	Assert.assertEquals(original.getLinkage(), marshalled.getLinkage());

	Assert.assertEquals(original.getElementType().getLayerPk(), marshalled.getElementType().getLayerPk());

	Assert.assertEquals(original.getElementType().getLayerStyleName(), marshalled.getElementType().getLayerStyleName());

	Assert.assertEquals(original.getElementType().getLayerStyleWorkspace(), marshalled.getElementType().getLayerStyleWorkspace());

	Assert.assertEquals(original.getElementType().getQueryStringFragment(), marshalled.getElementType().getQueryStringFragment());

	Assert.assertEquals(original.getElementType().isTemporal(), marshalled.getElementType().isTemporal());

	Assert.assertEquals(original.getElementType(), marshalled.getElementType());
    }

    /**
     * @return
     */
    private static EXT_Online create() {

	EXT_Online extOnline = new EXT_Online();

	extOnline.setName("name");

	extOnline.setLinkage("linkage");

	Assert.assertEquals("name", extOnline.getName());

	Assert.assertEquals("linkage", extOnline.getLinkage());

	EXT_CIOnlineResourceType type = extOnline.getElementType();

	type.setLayerPk("layerPK");

	type.setLayerStyleName("layerStyleName");

	type.setLayerStyleWorkspace("layerStyleWorkspace");

	type.setQueryStringFragment("queryStringFragment");

	type.setTemporal(true);

	return extOnline;
    }

}
