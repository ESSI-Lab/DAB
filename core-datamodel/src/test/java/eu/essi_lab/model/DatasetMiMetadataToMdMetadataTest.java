package eu.essi_lab.model;

import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.iso.datamodel.classes.Distribution;
import eu.essi_lab.iso.datamodel.classes.MDMetadata;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.HarmonizedMetadata;

public class DatasetMiMetadataToMdMetadataTest {

    private static final String ORIGINAL_MD = "<original><id>FILE_ID</id><title>TITLE</title><url>http://endpoint</url><parent>PARENT_ID</parent></original>";
    private static final String SOURCE_LABEL = "SOURCE_LABEL<>!%&()=?^£";
    private static final String newTitle = "New title";
    private static final String newId = "New identifier";

    @Test
    public void test() {

	try {

	    Dataset dataset = new Dataset();

	    HarmonizedMetadata hm = dataset.getHarmonizedMetadata();

	    CoreMetadata coreMetadata = hm.getCoreMetadata();
	    coreMetadata.setTitle("TITLE");
	    coreMetadata.setIdentifier("FILE_ID");
	    coreMetadata.getMIMetadata().setHierarchyLevelName("dataset");
	    coreMetadata.getMIMetadata().setParentIdentifier("PARENT_ID");

	    MIPlatform miPlatform = new MIPlatform();
	    Assert.assertNull(miPlatform.getDescription());
	    miPlatform.setDescription("desc");
	    Assert.assertEquals(miPlatform.getDescription(), "desc");
	    coreMetadata.getMIMetadata().addMIPlatform(miPlatform);

	    Distribution distribution = new Distribution();
	    Online online = new Online();
	    online.setDescription("DESCRIPTION");
	    online.setLinkage("http://endpoint");
	    distribution.addDistributionOnline(online);
	    coreMetadata.getMIMetadata().setDistribution(distribution);

	    hm.setCoreMetadata(coreMetadata);

	    // -------------------------------

	    MIMetadata miMetadata = dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();
	    miPlatform = miMetadata.getMIPlatform();
	    Assert.assertNotNull(miPlatform);
	    Assert.assertEquals(miMetadata.getDistribution().getDistributionOnline().getDescription(), "DESCRIPTION");

	    MDMetadata mdMetadata = dataset.getHarmonizedMetadata().getCoreMetadata().getReadOnlyMDMetadata();
	    Assert.assertEquals(mdMetadata.getDistribution().getDistributionOnline().getDescription(), "DESCRIPTION");

	    miMetadata = dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();
	    miPlatform = miMetadata.getMIPlatform();
	    Assert.assertNotNull(miPlatform);
	    Assert.assertEquals(miMetadata.getDistribution().getDistributionOnline().getDescription(), "DESCRIPTION");

	    System.out.println(mdMetadata.asString(false));

	} catch (Exception e) {

	    fail("Exception thrown");
	}
    }

}
