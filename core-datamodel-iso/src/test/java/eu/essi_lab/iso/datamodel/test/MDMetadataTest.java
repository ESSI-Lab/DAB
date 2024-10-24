package eu.essi_lab.iso.datamodel.test;

import static org.junit.Assert.fail;

import java.math.BigInteger;
import java.util.Iterator;

import org.junit.Assert;

import eu.essi_lab.iso.datamodel.MetadataTest;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.Dimension;
import eu.essi_lab.iso.datamodel.classes.Distribution;
import eu.essi_lab.iso.datamodel.classes.GridSpatialRepresentation;
import eu.essi_lab.iso.datamodel.classes.MDMetadata;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ReferenceSystem;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import net.opengis.iso19139.gmd.v_20060504.MDMetadataType;

public class MDMetadataTest extends MetadataTest<MDMetadata, MDMetadataType> {

    public MDMetadataTest() {
	super(MDMetadata.class, MDMetadataType.class);
    }

    @Override
    public void setProperties(MDMetadata mdMetadata) {

	mdMetadata.setCharacterSetCode("characterSet");

	mdMetadata.setDateStampAsDate("DATE");

	mdMetadata.setFileIdentifier("ID");

	mdMetadata.setHierarchyLevelName("name");

	mdMetadata.addHierarchyLevelScopeCodeListValue("dataset");

	mdMetadata.setLanguage("language");

	mdMetadata.setMetadataStandardName("name");

	mdMetadata.setMetadataStandardVersion("version");

	mdMetadata.setParentIdentifier("ID");

	mdMetadata.addAggregatedResourceIdentifier("AGGREGATED_RES_ID_1");
	mdMetadata.addAggregatedResourceIdentifier("AGGREGATED_RES_ID_2");

	// --------------------------------
	//
	// DataIdentification
	//
	DataIdentification dataIdentification1 = new DataIdentification();
	dataIdentification1.setCitationTitle("TITLE1");
	mdMetadata.addDataIdentification(dataIdentification1);

	DataIdentification dataIdentification2 = new DataIdentification();
	dataIdentification2.setCitationTitle("TITLE2");
	mdMetadata.addDataIdentification(dataIdentification2);

	DataIdentification dataIdentification3 = new DataIdentification();
	dataIdentification3.setCitationTitle("TITLE3");
	mdMetadata.addDataIdentification(dataIdentification3);

	// --------------------------------
	//
	// Distribution
	//
	Distribution distribution = new Distribution();
	Online online = new Online();
	online.setDescription("desc");
	distribution.addDistributionOnline(online);
	mdMetadata.setDistribution(distribution);

	// contact

	ResponsibleParty party = new ResponsibleParty();

	party.setIndividualName("My Contact");

	mdMetadata.addContact(party);

	ReferenceSystem referenceSystem = new ReferenceSystem();
	referenceSystem.setCode("ref-id");
	mdMetadata.addReferenceSystemInfo(referenceSystem);

	GridSpatialRepresentation grid = new GridSpatialRepresentation();
	Dimension dim = new Dimension();
	dim.setDimensionSize(BigInteger.TEN);
	grid.addAxisDimension(dim);
	mdMetadata.addGridSpatialRepresentation(grid);

    }

    @Override
    public void checkProperties(MDMetadata mdMetadata) {

	try {
	    String textContent = mdMetadata.getTextContent();
	    Assert.assertNotNull(textContent);
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}

	String type = mdMetadata.getCharacterSetCode();
	Assert.assertEquals("characterSet", type);

	Assert.assertEquals(mdMetadata.getDateStamp(), "DATE");

	Assert.assertEquals(mdMetadata.getFileIdentifier(), "ID");

	String level = mdMetadata.getHierarchyLevelScopeCodeListValue();
	Assert.assertEquals("dataset", level);

	Assert.assertEquals(mdMetadata.getHierarchyLevelName(), "name");

	Assert.assertEquals(mdMetadata.getLanguage(), "language");

	Assert.assertEquals(mdMetadata.getMetadataStandardName(), "name");

	Assert.assertEquals(mdMetadata.getMetadataStandardVersion(), "version");

	Assert.assertEquals(mdMetadata.getParentIdentifier(), "ID");

	Iterator<String> titles = mdMetadata.getAggregatedResourcesIdentifiers();
	String aggTitle1 = titles.next();
	Assert.assertEquals("AGGREGATED_RES_ID_1", aggTitle1);

	String aggTitle2 = titles.next();
	Assert.assertEquals("AGGREGATED_RES_ID_2", aggTitle2);

	// --------------------------------
	//
	// DataIdentification
	//

	// the first data id is created when the aggregated resources titles are added
	// so here we expect the null value since no title is set in this data id
	Assert.assertEquals(null, mdMetadata.getDataIdentification().getCitationTitle());
	Iterator<DataIdentification> dataIdentifications = mdMetadata.getDataIdentifications();

	DataIdentification next = dataIdentifications.next();
	Assert.assertEquals(null, next.getCitationTitle());

	next = dataIdentifications.next();
	Assert.assertEquals("TITLE1", next.getCitationTitle());

	next = dataIdentifications.next();
	Assert.assertEquals("TITLE2", next.getCitationTitle());

	next = dataIdentifications.next();
	Assert.assertEquals("TITLE3", next.getCitationTitle());

	// --------------------------------
	//
	// Distribution
	//
	Distribution distribution = mdMetadata.getDistribution();
	Assert.assertEquals(distribution.getDistributionOnlines().next().getDescription(), "desc");

	// contact
	Assert.assertEquals("My Contact", mdMetadata.getContacts().next().getIndividualName());

	Assert.assertEquals("ref-id", mdMetadata.getReferenceSystemInfos().next().getCode());

	Assert.assertEquals(BigInteger.TEN, mdMetadata.getGridSpatialRepresentation().getAxisDimension().getDimensionSize());

    }

    @Override
    public void clearProperties(MDMetadata mdMetadata) {
	mdMetadata.setCharacterSetCode(null);

	mdMetadata.setDateStampAsDate(null);

	mdMetadata.setFileIdentifier(null);

	mdMetadata.clearHierarchyLevels();

	mdMetadata.setHierarchyLevelName(null);

	mdMetadata.setLanguage(null);

	mdMetadata.setMetadataStandardName(null);

	mdMetadata.setMetadataStandardVersion(null);

	mdMetadata.setParentIdentifier(null);

	// --------------------------------
	//
	// DataIdentification
	//

	mdMetadata.clearDataIdentifications();

	// --------------------------------
	//
	// Distribution
	//
	mdMetadata.setDistribution(null);

	mdMetadata.clearContacts();

	mdMetadata.clearReferenceSystemInfos();

	mdMetadata.clearGridSpatialRepresentations();

    }

    @Override
    public void checkNullProperties(MDMetadata mdMetadata) {
	Assert.assertNull(mdMetadata.getCharacterSetCode());
	Assert.assertNull(mdMetadata.getDateStamp());
	Assert.assertNull(mdMetadata.getDateTimeStamp());
	Assert.assertNull(mdMetadata.getFileIdentifier());
	Assert.assertNull(mdMetadata.getHierarchyLevelScopeCodeListValue());
	Assert.assertNull(mdMetadata.getHierarchyLevelName());
	Assert.assertNotNull(mdMetadata.getHierarchyLevelScopeCodeListValues());
	Assert.assertNull(mdMetadata.getLanguage());
	Assert.assertNull(mdMetadata.getMetadataStandardName());
	Assert.assertNull(mdMetadata.getMetadataStandardVersion());
	Assert.assertNull(mdMetadata.getParentIdentifier());
	Assert.assertNull(mdMetadata.getDataIdentification());
	Assert.assertNotNull(mdMetadata.getDataIdentifications());
	Assert.assertNull(mdMetadata.getDistribution());
	Assert.assertFalse(mdMetadata.getContacts().hasNext());
	Assert.assertFalse(mdMetadata.getReferenceSystemInfos().hasNext());
	Assert.assertNotNull(mdMetadata.getAggregatedResourcesIdentifiers());
	Assert.assertNull(mdMetadata.getGridSpatialRepresentation());
    }
}
