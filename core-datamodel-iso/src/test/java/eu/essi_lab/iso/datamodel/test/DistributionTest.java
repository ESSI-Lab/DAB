package eu.essi_lab.iso.datamodel.test;

import java.util.Iterator;

import org.junit.Assert;

import com.google.common.collect.Lists;

import eu.essi_lab.iso.datamodel.MetadataTest;
import eu.essi_lab.iso.datamodel.classes.Distribution;
import eu.essi_lab.iso.datamodel.classes.Format;
import eu.essi_lab.iso.datamodel.classes.Online;
import net.opengis.iso19139.gmd.v_20060504.MDDistributionType;

public class DistributionTest extends MetadataTest<Distribution, MDDistributionType> {

    public DistributionTest() {
	super(Distribution.class, MDDistributionType.class);
    }

    @Override
    public void setProperties(Distribution distribution) {

	// ------------
	//
	// formats
	//
	//
	Format format = new Format();
	format.setName("name1");
	format.setVersion("version1");
	distribution.addFormat(format);

	Format format2 = new Format();
	format2.setName("name2");
	format2.setVersion("version2");
	distribution.addFormat(format2);

	// ------------
	//
	// onlines
	//
	//
	Online online = new Online();
	online.setIdentifier("id1");
	online.setName("name");
	online.setProtocol("protocol");
	online.setDescription("desc");
	online.setLinkage("link");
	distribution.addDistributionOnline(online);

	Online online2 = new Online();
	online2.setIdentifier("id2");
	online2.setName("name2");
	online2.setProtocol("protocol2");
	online2.setDescription("desc2");
	online2.setLinkage("link2");
	distribution.addDistributionOnline(online2, 1000.0);

	// -------------------
	//
	// distributor onlines
	//
	//
	distribution.addDistributorOnline(online);

    }

    @Override
    public void checkProperties(Distribution distribution) {
	// ------------
	//
	// formats
	//
	//

	Iterator<Format> formats = distribution.getFormats();

	Format next = formats.next();
	Assert.assertEquals(next.getName(), "name1");
	Assert.assertEquals(next.getVersion(), "version1");

	next = formats.next();
	Assert.assertEquals(next.getName(), "name2");
	Assert.assertEquals(next.getVersion(), "version2");

	Format format3 = distribution.getFormat();
	Assert.assertEquals(format3.getName(), "name1");
	Assert.assertEquals(format3.getVersion(), "version1");

	// ------------
	//
	// onlines
	//
	//

	Iterator<Online> onlines = distribution.getDistributionOnlines();
	Online next2 = onlines.next();
	Assert.assertEquals(next2.getIdentifier(), "id1");
	Assert.assertEquals(next2.getName(), "name");
	Assert.assertEquals(next2.getDescription(), "desc");
	Assert.assertEquals(next2.getLinkage(), "link");
	Assert.assertEquals(next2.getProtocol(), "protocol");

	Online next3 = onlines.next();
	Assert.assertEquals(next3.getIdentifier(), "id2");
	Assert.assertEquals(next3.getName(), "name2");
	Assert.assertEquals(next3.getDescription(), "desc2");
	Assert.assertEquals(next3.getLinkage(), "link2");
	Assert.assertEquals(next3.getProtocol(), "protocol2");
	
	next3 = distribution.getDistributionOnline("id2");
	Assert.assertEquals(next3.getIdentifier(), "id2");
	Assert.assertEquals(next3.getName(), "name2");
	Assert.assertEquals(next3.getDescription(), "desc2");
	Assert.assertEquals(next3.getLinkage(), "link2");
	Assert.assertEquals(next3.getProtocol(), "protocol2");

	Online online3 = distribution.getDistributionOnlines().next();

	Assert.assertEquals(online3.getName(), "name");
	Assert.assertEquals(online3.getDescription(), "desc");
	Assert.assertEquals(online3.getLinkage(), "link");
	Assert.assertEquals(online3.getProtocol(), "protocol");
	
	Online next4 = distribution.getDistributionOnline();
	Assert.assertEquals(next4.getName(), "name");
	Assert.assertEquals(next4.getDescription(), "desc");
	Assert.assertEquals(next4.getLinkage(), "link");
	Assert.assertEquals(next4.getProtocol(), "protocol");

	Assert.assertEquals(2, Lists.newArrayList(distribution.getDistributionTransferOptions()).size());

	// -------------------
	//
	// distributor onlines
	//
	//

	Iterator<Online> distributorOnlines = distribution.getDistributorOnlines();
	next4 = distributorOnlines.next();
	Assert.assertEquals(next4.getName(), "name");
	Assert.assertEquals(next4.getDescription(), "desc");
	Assert.assertEquals(next4.getLinkage(), "link");
	Assert.assertEquals(next4.getProtocol(), "protocol");

	Assert.assertFalse(distributorOnlines.hasNext());
	
	Assert.assertEquals(1, Lists.newArrayList(distribution.getDistributorTransferOptions()).size());

    }

    @Override
    public void clearProperties(Distribution distribution) {
	// ------------
	//
	// formats
	//
	//

	distribution.clearFormats();

	// ------------
	//
	// onlines
	//
	//

	distribution.clearDistributionOnlines();

	// -------------------
	//
	// distributor onlines
	//
	//

	distribution.clearDistributorOnlines();

    }

    @Override
    public void checkNullProperties(Distribution distribution) {
	// ------------
	//
	// formats
	//
	//

	Iterator<Format> formats = distribution.getFormats();

	Assert.assertEquals(formats.hasNext(), false);

	Assert.assertNull(distribution.getFormat());

	// ------------
	//
	// onlines
	//
	//

	Assert.assertEquals(distribution.getDistributionOnlines().hasNext(), false);

	Assert.assertNull(distribution.getDistributionOnline());

	// -------------------
	//
	// distributor onlines
	//
	//

	Iterator<Online> distributorOnlines = distribution.getDistributorOnlines();
	Assert.assertEquals(distributorOnlines.hasNext(), false);

    }
}
