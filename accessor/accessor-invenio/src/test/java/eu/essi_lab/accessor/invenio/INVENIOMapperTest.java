package eu.essi_lab.accessor.invenio;

import java.io.InputStream;
import java.util.Iterator;

import org.apache.cxf.helpers.IOUtils;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.HarmonizedMetadata;
import eu.essi_lab.model.resource.OriginalMetadata;
import junit.framework.TestCase;

public class INVENIOMapperTest {

    private INVENIOMapper mapper;

    private GSSource source;

    @Before
    public void init() {
	this.mapper = new INVENIOMapper();
    }

    @Test
    public void testMinimalMapperFromExample() throws Exception {
	InputStream stream = INVENIOMapperTest.class.getClassLoader().getResourceAsStream("invenioSample.json");

	ClonableInputStream cloneStream = new ClonableInputStream(stream);

	TestCase.assertNotNull(stream);

	OriginalMetadata originalMD = new OriginalMetadata();

	String mmetadata = IOUtils.toString(cloneStream.clone());

	originalMD.setMetadata(mmetadata);

	GSSource gsSource = new GSSource();

	GSResource resource = mapper.map(originalMD, gsSource);

	HarmonizedMetadata result = resource.getHarmonizedMetadata();

	// BufferedReader bfReader = null;
	//
	// bfReader = new BufferedReader(new InputStreamReader(stream));
	String temp = null;

	TestCase.assertNotNull(result);

	CoreMetadata core = result.getCoreMetadata();

	MIMetadata metadata = core.getMIMetadata();

	DataIdentification dataIdentification = metadata.getDataIdentification();
	TestCase.assertNotNull(dataIdentification);

	// title
	TestCase.assertEquals("Human populations in the world's mountains: spatio-temporal patterns and potential controls (output data)",
		dataIdentification.getCitationTitle());

	TestCase.assertEquals(
		"<p>Output data for Human populations in the world's mountains: spatio-temporal patterns and potential controls (Thornton et al.)</p>",
		dataIdentification.getAbstract());

	// id
	TestCase.assertEquals("6p7f6-m5993", resource.getOriginalId().get());

	// responsible party
	ResponsibleParty originator = dataIdentification.getPointOfContact("publisher");

	TestCase.assertEquals("GEO Knowledge Hub", originator.getOrganisationName());

	// time
	TemporalExtent time = dataIdentification.getTemporalExtent();
	TestCase.assertEquals("2021-10-27T22:59:04.645818+00:00", time.getBeginPosition());
	TestCase.assertEquals("2021-10-28T12:06:54.706441+00:00", time.getEndPosition());
	// TestCase.assertEquals("2019-04-04T22:05:00Z", time.getBeginPosition());
	// TestCase.assertEquals("2019-03-08", time.getEndPosition());

	// online

	// TestCase.assertEquals(gsSource.getEndpoint(),
	// metadata.getDistribution().getDistributionOnline().getLinkage());
	// Iterator<Online> onlines = metadata.getDistribution().getDistributionOnlines();
	// while(onlines.hasNext()) {
	// Online o = onlines.next();
	// String protocol = o.getProtocol();
	// if(protocol != null && protocol != "")
	// TestCase.assertTrue(protocol.equalsIgnoreCase(CommonNameSpaceContext.ANA_URI));
	// }

    }

    @Test
    public void testMapperGOS4mExample() throws Exception {
	InputStream stream = INVENIOMapperTest.class.getClassLoader().getResourceAsStream("invenioGOS4M.json");

	ClonableInputStream cloneStream = new ClonableInputStream(stream);

	TestCase.assertNotNull(stream);

	OriginalMetadata originalMD = new OriginalMetadata();

	String mmetadata = IOUtils.toString(cloneStream.clone());

	originalMD.setMetadata(mmetadata);

	GSSource gsSource = new GSSource();

	GSResource resource = mapper.map(originalMD, gsSource);

	HarmonizedMetadata result = resource.getHarmonizedMetadata();

	// BufferedReader bfReader = null;
	//
	// bfReader = new BufferedReader(new InputStreamReader(stream));
	String temp = null;

	TestCase.assertNotNull(result);

	CoreMetadata core = result.getCoreMetadata();

	MIMetadata metadata = core.getMIMetadata();

	DataIdentification dataIdentification = metadata.getDataIdentification();
	TestCase.assertNotNull(dataIdentification);

	// title
	TestCase.assertEquals("Presentation of GOS4M Knowledge Hub", dataIdentification.getCitationTitle());

	TestCase.assertEquals(
		"<p>Presentation at the Sixteenth Plenary Session \u2013 GEO-XVI Canberra, Australia 6-7 November 2019:</p><p>http://www.gos4m.org/communication/</p>",
		dataIdentification.getAbstract());

	// id
	TestCase.assertEquals("7fm1q-5xq17", resource.getOriginalId().get());

	// responsible party
	ResponsibleParty originator = dataIdentification.getPointOfContact("publisher");

	TestCase.assertEquals("GOS4M Knowledge Hub", originator.getOrganisationName());

	// time
	TemporalExtent time = dataIdentification.getTemporalExtent();
	TestCase.assertEquals("2021-08-21T00:07:12.404217+00:00", time.getBeginPosition());
	TestCase.assertEquals("2021-08-21T00:07:12.510432+00:00", time.getEndPosition());
	// TestCase.assertEquals("2019-04-04T22:05:00Z", time.getBeginPosition());
	// TestCase.assertEquals("2019-03-08", time.getEndPosition());

	// online

	// TestCase.assertEquals(gsSource.getEndpoint(),
	// metadata.getDistribution().getDistributionOnline().getLinkage());
	Iterator<Online> onlines = metadata.getDistribution().getDistributionOnlines();
	while (onlines.hasNext()) {
	    Online o = onlines.next();
	    String protocol = o.getProtocol();
	    if (protocol != null && protocol != "")
		TestCase.assertTrue(protocol.contains("WWW:LINK-1.0-http–link") || protocol.contains("WWW:DOWNLOAD-1.0-http–download") );
	}

    }
}
