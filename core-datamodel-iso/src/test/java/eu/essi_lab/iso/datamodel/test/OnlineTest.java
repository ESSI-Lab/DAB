package eu.essi_lab.iso.datamodel.test;

import eu.essi_lab.iso.datamodel.*;
import eu.essi_lab.iso.datamodel.classes.*;
import net.opengis.iso19139.gmd.v_20060504.*;
import org.junit.*;

public class OnlineTest extends MetadataTest<Online, CIOnlineResourceType> {

    public OnlineTest() {
	super(Online.class, CIOnlineResourceType.class);
    }

    @Test
    public void anchorTest() {

	Online online = new Online();

	online.setDescriptionGmxAnchor("descriptionGmxAnchor");

	online.setProtocolAnchor("protocolHrefGmxAnchor", "protocolValueGmxAnchor");

	Assert.assertEquals("descriptionGmxAnchor", online.getDescriptionGmxAnchor());

	Assert.assertEquals("protocolHrefGmxAnchor", online.getProtocolGmxAnchor());

	Assert.assertEquals("protocolValueGmxAnchor", online.getProtocolValueGmxAnchor());
    }

    @Override
    public void setProperties(Online online) {
	online.setName("name");
	online.setProtocol("protocol");
	online.setDescription("desc");
	online.setLinkage("link");
	online.setFunctionCode("download");
	online.setIdentifier("id");

    }

    @Override
    public void checkProperties(Online online) {
	Assert.assertEquals("name", online.getName());
	Assert.assertEquals("protocol", online.getProtocol());
	Assert.assertEquals("desc", online.getDescription());
	Assert.assertEquals("link", online.getLinkage());
	Assert.assertEquals("download", online.getFunctionCode());
	Assert.assertEquals("id", online.getIdentifier());

    }

    @Override
    public void clearProperties(Online online) {
	online.setName(null);
	online.setProtocol(null);
	online.setDescription(null);
	online.setLinkage(null);
	online.setFunctionCode(null);
	online.setIdentifier(null);

    }

    @Override
    public void checkNullProperties(Online online) {
	Assert.assertNull(online.getName());
	Assert.assertNull(online.getProtocol());
	Assert.assertNull(online.getDescription());
	Assert.assertNull(online.getLinkage());
	Assert.assertNull(online.getDescriptionGmxAnchor());
	Assert.assertNull(online.getFunctionCode());
	Assert.assertNull(online.getIdentifier());

    }

}
