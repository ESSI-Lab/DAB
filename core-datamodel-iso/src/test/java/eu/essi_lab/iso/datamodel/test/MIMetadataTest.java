package eu.essi_lab.iso.datamodel.test;

import java.util.Iterator;

import org.junit.Assert;

import eu.essi_lab.iso.datamodel.MetadataTest;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.Distribution;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.jaxb.iso19139_2.gmi.v_1_0.MIMetadataType;
import net.opengis.iso19139.gmd.v_20060504.MDMetadataType;

public class MIMetadataTest extends MetadataTest<MIMetadata, MDMetadataType> {

    public MIMetadataTest() {
	super(MIMetadata.class, MIMetadataType.class);
    }

    @Override
    public void setProperties(MIMetadata miMetadata) {

	MIPlatform miPlatform = new MIPlatform();
	miPlatform.setDescription("platformDesc");
	miMetadata.addMIPlatform(miPlatform);

	miMetadata.setCharacterSetCode("characterSetCode");

	miMetadata.setDateStampAsDate("DATE");
	
	miMetadata.addCloudCoverPercentage(5);
	miMetadata.addCloudCoverPercentage(8);

	// TODO
	// mdMetadata.setDateStampAsDateTime(calendar);

	miMetadata.setFileIdentifier("ID");

	miMetadata.addHierarchyLevelScopeCodeListValue("dataset");

	miMetadata.setHierarchyLevelName("name");

	miMetadata.setLanguage("language");

	miMetadata.setMetadataStandardName("name");

	miMetadata.setMetadataStandardVersion("version");

	miMetadata.setParentIdentifier("ID");

	// --------------------------------
	//
	// DataIdentification
	//
	DataIdentification dataIdentification1 = new DataIdentification();
	dataIdentification1.setCitationTitle("TITLE1");
	miMetadata.addDataIdentification(dataIdentification1);

	DataIdentification dataIdentification2 = new DataIdentification();
	dataIdentification2.setCitationTitle("TITLE2");
	miMetadata.addDataIdentification(dataIdentification2);

	DataIdentification dataIdentification3 = new DataIdentification();
	dataIdentification3.setCitationTitle("TITLE3");
	miMetadata.addDataIdentification(dataIdentification3);

	// --------------------------------
	//
	// Distribution
	//
	Distribution distribution = new Distribution();
	Online online = new Online();
	online.setDescription("desc");
	distribution.addDistributionOnline(online);
	miMetadata.setDistribution(distribution);

    }

    @Override
    public void checkProperties(MIMetadata miMetadata) {
	
	MIPlatform miPlatform = miMetadata.getMIPlatform();
	Assert.assertEquals(miPlatform.getDescription(), "platformDesc");
	
	String type = miMetadata.getCharacterSetCode();
	Assert.assertEquals("characterSetCode", type);

	Assert.assertEquals(miMetadata.getDateStamp(), "DATE");

	Assert.assertEquals(miMetadata.getFileIdentifier(), "ID");
	
	Assert.assertEquals(2, miMetadata.getCloudCoverPercentageList().size());
	
	Assert.assertTrue(miMetadata.getCloudCoverPercentageList().stream().anyMatch(p -> p.equals(5.0)));
	Assert.assertTrue(miMetadata.getCloudCoverPercentageList().stream().anyMatch(p -> p.equals(8.0)));

	String level = miMetadata.getHierarchyLevelScopeCodeListValue();
	Assert.assertEquals("dataset", level);

	Assert.assertEquals(miMetadata.getHierarchyLevelName(), "name");

	Iterator<String> hierarchyLevels = miMetadata.getHierarchyLevelScopeCodeListValues();
	String next2 = hierarchyLevels.next();
	Assert.assertEquals("dataset", next2);

	Assert.assertEquals(miMetadata.getLanguage(), "language");

	Assert.assertEquals(miMetadata.getMetadataStandardName(), "name");

	Assert.assertEquals(miMetadata.getMetadataStandardVersion(), "version");

	Assert.assertEquals(miMetadata.getParentIdentifier(), "ID");

	// --------------------------------
	//
	// DataIdentification
	//
	Iterator<DataIdentification> dataIdentifications = miMetadata.getDataIdentifications();

	DataIdentification next = dataIdentifications.next();
	Assert.assertEquals(next.getCitationTitle(), "TITLE1");

	next = dataIdentifications.next();
	Assert.assertEquals(next.getCitationTitle(), "TITLE2");

	next = dataIdentifications.next();
	Assert.assertEquals(next.getCitationTitle(), "TITLE3");

	// --------------------------------
	//
	// Distribution
	//
	Distribution distribution = miMetadata.getDistribution();
	Assert.assertEquals(distribution.getDistributionOnlines().next().getDescription(), "desc");
    }

    @Override
    public void clearProperties(MIMetadata miMetadata) {
	miMetadata.setCharacterSetCode(null);

	miMetadata.setDateStampAsDate(null);

	miMetadata.setFileIdentifier(null);

	miMetadata.clearHierarchyLevels();

	miMetadata.setHierarchyLevelName(null);

	miMetadata.setLanguage(null);

	miMetadata.setMetadataStandardName(null);

	miMetadata.setMetadataStandardVersion(null);

	miMetadata.setParentIdentifier(null);

	// --------------------------------
	//
	// DataIdentification
	//
	miMetadata.clearDataIdentifications();

	// --------------------------------
	//
	// Distribution
	//

	miMetadata.setDistribution(null);

    }

    @Override
    public void checkNullProperties(MIMetadata miMetadata) {
	Assert.assertNull(miMetadata.getCharacterSetCode());
	Assert.assertNull(miMetadata.getDateStamp());
	Assert.assertNull(miMetadata.getDateTimeStamp());
	Assert.assertNull(miMetadata.getFileIdentifier());
	Assert.assertNull(miMetadata.getHierarchyLevelScopeCodeListValue());
	Assert.assertNull(miMetadata.getHierarchyLevelName());
	Assert.assertNotNull(miMetadata.getHierarchyLevelScopeCodeListValues());
	Assert.assertNull(miMetadata.getLanguage());
	Assert.assertNull(miMetadata.getMetadataStandardName());
	Assert.assertNull(miMetadata.getMetadataStandardVersion());
	Assert.assertNull(miMetadata.getParentIdentifier());
	Assert.assertNull(miMetadata.getDataIdentification());
	Assert.assertNotNull(miMetadata.getDataIdentifications());
	Assert.assertNull(miMetadata.getDistribution());
	Assert.assertFalse(miMetadata.getMIInstruments().hasNext());
    }

}
