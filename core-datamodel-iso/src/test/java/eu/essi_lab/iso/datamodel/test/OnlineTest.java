package eu.essi_lab.iso.datamodel.test;

import org.junit.Assert;

import eu.essi_lab.iso.datamodel.MetadataTest;
import eu.essi_lab.iso.datamodel.classes.Online;
import net.opengis.iso19139.gmd.v_20060504.CIOnlineResourceType;

public class OnlineTest extends MetadataTest<Online, CIOnlineResourceType> {

    public OnlineTest() {
	super(Online.class, CIOnlineResourceType.class);
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
	Assert.assertEquals(online.getName(), "name");
	Assert.assertEquals(online.getProtocol(), "protocol");
	Assert.assertEquals(online.getDescription(), "desc");
	Assert.assertEquals(online.getLinkage(), "link");
	Assert.assertEquals(online.getFunctionCode(), "download");
	Assert.assertEquals(online.getIdentifier(), "id");

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
