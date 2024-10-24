package eu.essi_lab.model;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.iso.datamodel.classes.Format;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;

public class CoreMetadataTest {

    @Test
    public void test() {

	CoreMetadata coreMetadata = new Dataset().getHarmonizedMetadata().getCoreMetadata();
	coreMetadata.setTitle("TITLE");
	coreMetadata.setIdentifier("IDENTIFIER");
	coreMetadata.setAbstract("ABSTRACT");
	coreMetadata.addBoundingBox(0, 0, 0, 0);
	coreMetadata.addDistributionFormat("FORMAT");
	coreMetadata.addTemporalExtent("BEGIN", "END");

	coreMetadata.addDistributionOnlineResource("NAME", "http://linkage", "PROTOCOL", "information");

	String identifier = coreMetadata.getIdentifier();
	Assert.assertEquals(identifier, "IDENTIFIER");

	String title = coreMetadata.getTitle();
	Assert.assertEquals(title, "TITLE");

	String abstract_ = coreMetadata.getAbstract();
	Assert.assertEquals(abstract_, "ABSTRACT");

	GeographicBoundingBox boundingBox = coreMetadata.getBoundingBox();
	Assert.assertEquals(boundingBox.getNorth(), 0, 0);
	Assert.assertEquals(boundingBox.getSouth(), 0, 0);
	Assert.assertEquals(boundingBox.getWest(), 0, 0);
	Assert.assertEquals(boundingBox.getWest(), 0, 0);

	TemporalExtent temporalExtent = coreMetadata.getTemporalExtent();
	Assert.assertEquals(temporalExtent.getBeginPosition(), "BEGIN");
	Assert.assertEquals(temporalExtent.getEndPosition(), "END");

	Format format = coreMetadata.getFormat();
	Assert.assertEquals(format.getName(), "FORMAT");

	Online online = coreMetadata.getOnline();
	Assert.assertEquals(online.getName(), "NAME");
	Assert.assertEquals(online.getLinkage(), "http://linkage");
	Assert.assertEquals(online.getProtocol(), "PROTOCOL");
	String functionCode = online.getFunctionCode();
	Assert.assertEquals("information", functionCode);

	MIMetadata mdMetadata = coreMetadata.getMIMetadata();
	try {

	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    mdMetadata.toStream(outputStream);
	    // System.out.println(outputStream);

	    InputStream stream = new ByteArrayInputStream(outputStream.toByteArray());
	    MIMetadata metadata = new MIMetadata(stream);

	    String fileIdentifier = metadata.getFileIdentifier();
	    Assert.assertEquals(fileIdentifier, "IDENTIFIER");

	} catch (JAXBException e) {

	    e.printStackTrace();

	    fail("Exception thrown");
	}
    }
}
