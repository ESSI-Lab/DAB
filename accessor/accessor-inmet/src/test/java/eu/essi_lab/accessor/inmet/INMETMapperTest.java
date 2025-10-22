package eu.essi_lab.accessor.inmet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.Dimension;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.GridSpatialRepresentation;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.iso.datamodel.classes.VerticalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.HarmonizedMetadata;
import eu.essi_lab.model.resource.OriginalMetadata;
import junit.framework.TestCase;

/**
 * @author roncella
 */

public class INMETMapperTest {

    private INMETMapper mapper;
    private INMETConnector connector;

    @Before
    public void init() {
	this.mapper = new INMETMapper();
	this.connector = new INMETConnector();
    }

    @Test
    public void testMinimalMapperFromExample() throws Exception {
	InputStream is = INMETMapperTest.class.getClassLoader().getResourceAsStream(("PREC_A001_20180927.HIS.CSV"));
	TestCase.assertNotNull(is);
	File stream = File.createTempFile("MapperTest", "xls");
	OutputStream outputStream = null;
	outputStream = new FileOutputStream(stream);
	IOUtils.copy(is, outputStream);

	TestCase.assertNotNull(stream);

	// BufferedReader bfReader = null;
	//
	// bfReader = new BufferedReader(new InputStreamReader(stream));
	//
	//
	// // String temp = null;
	// bfReader.readLine(); // skip header line
	// String temp = bfReader.readLine();

	String res = connector.createCSVMetadataRecord(stream, "PREC_A001_20180927.HIS.CSV");
	if (stream.exists())
	    stream.delete();

	OriginalMetadata originalMD = new OriginalMetadata();

	originalMD.setMetadata(res);

	GSResource resource = mapper.map(originalMD, new GSSource());

	HarmonizedMetadata result = resource.getHarmonizedMetadata();

	TestCase.assertNotNull(result);

	CoreMetadata core = result.getCoreMetadata();

	MIMetadata metadata = core.getMIMetadata();

	DataIdentification dataIdentification = metadata.getDataIdentification();
	TestCase.assertNotNull(dataIdentification);

	// title
	TestCase.assertEquals("Acquisitions at BRASILIA - Precipitation", dataIdentification.getCitationTitle());

	// bbox
	GeographicBoundingBox bbox = dataIdentification.getGeographicBoundingBox();
	TestCase.assertEquals(-47.92583332, bbox.getEast());
	TestCase.assertEquals(-47.92583332, bbox.getWest());
	TestCase.assertEquals(-15.78944444, bbox.getNorth());
	TestCase.assertEquals(-15.78944444, bbox.getSouth());

	// vertical extent
	VerticalExtent verticalExtent = dataIdentification.getVerticalExtent();
	TestCase.assertEquals(1159.54, verticalExtent.getMinimumValue(), 10 ^ -7);
	TestCase.assertEquals(1159.54, verticalExtent.getMaximumValue(), 10 ^ -7);

	// id
	TestCase.assertEquals(Optional.empty(), resource.getOriginalId());

	// GRID
	GridSpatialRepresentation grid = metadata.getGridSpatialRepresentation();
	Assert.assertEquals((long) 1, (long) grid.getNumberOfDimensions());
	Assert.assertEquals("point", grid.getCellGeometryCode());
	Dimension time = grid.getAxisDimension();
	Assert.assertEquals("time", time.getDimensionNameTypeCode());
	Assert.assertEquals(new BigInteger("15183"), time.getDimensionSize());
	Assert.assertEquals(null, time.getResolutionUOM());
	Assert.assertEquals(null, time.getResolutionValue());

	// responsible party
	ResponsibleParty originator = dataIdentification.getPointOfContact("author");

	TestCase.assertEquals("National Metereology Institute of Brazil", originator.getOrganisationName());

	// time
	TemporalExtent timeExtent = dataIdentification.getTemporalExtent();
	TestCase.assertEquals(null, timeExtent.getIndeterminateBeginPosition());
	// TestCase.assertEquals("NOW", time.getIndeterminateEndPosition().toString());
	TestCase.assertEquals("2017-01-01T00:00:00Z", timeExtent.getBeginPosition());
	TestCase.assertEquals("2018-09-27T14:00:00Z", timeExtent.getEndPosition());
	// TestCase.assertEquals("2019-03-08", time.getEndPosition());

	// online

	Iterator<Online> onlines = metadata.getDistribution().getDistributionOnlines();
	while (onlines.hasNext()) {
	    Online o = onlines.next();
	    String protocol = o.getProtocol();
	    if (protocol != null && protocol != "") {
		TestCase.assertTrue(protocol.equalsIgnoreCase(CommonNameSpaceContext.INMET_CSV_URI));
		TestCase.assertEquals("urn:brazil-inmet:Precipitation@BRASILIA@PREC_A001_20180927.HIS.CSV", o.getName());
	    }
	}
	// platform
	MIPlatform platform = metadata.getMIPlatform();
	TestCase.assertEquals("BRASILIA", platform.getDescription());

	// coverage
	CoverageDescription coverage = metadata.getCoverageDescription();
	TestCase.assertEquals("Precipitation Units: millimeter Resolution: hourly", coverage.getAttributeDescription());
	TestCase.assertEquals("urn:brazil-inmet:Precipitation", coverage.getAttributeIdentifier());

    }

    @Test
    public void testMapperwithErrorHeight() throws Exception {
	InputStream is = INMETMapperTest.class.getClassLoader().getResourceAsStream(("PREC_S101_20180927.HIS.CSV"));
	TestCase.assertNotNull(is);
	File stream = File.createTempFile("MapperTest", ".xls");
	OutputStream outputStream = null;
	outputStream = new FileOutputStream(stream);
	IOUtils.copy(is, outputStream);

	TestCase.assertNotNull(stream);

	// BufferedReader bfReader = null;
	//
	// bfReader = new BufferedReader(new InputStreamReader(stream));
	//
	//
	// // String temp = null;
	// bfReader.readLine(); // skip header line
	// String temp = bfReader.readLine();

	String res = connector.createCSVMetadataRecord(stream, "PREC_S101_20180927.HIS.CSV");
	if (stream.exists())
	    stream.delete();

	OriginalMetadata originalMD = new OriginalMetadata();

	originalMD.setMetadata(res);

	HarmonizedMetadata result = mapper.execMapping(originalMD, new GSSource()).getHarmonizedMetadata();

	TestCase.assertNotNull(result);

	CoreMetadata core = result.getCoreMetadata();

	MIMetadata metadata = core.getMIMetadata();

	DataIdentification dataIdentification = metadata.getDataIdentification();
	TestCase.assertNotNull(dataIdentification);

	// vertical extent
	VerticalExtent verticalExtent = dataIdentification.getVerticalExtent();
	Assert.assertNull(verticalExtent);

    }

}
