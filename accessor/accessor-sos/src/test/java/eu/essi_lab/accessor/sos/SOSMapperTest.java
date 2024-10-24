package eu.essi_lab.accessor.sos;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;

public class SOSMapperTest {

    @Test
    public void testName() throws Exception {
	SOSMapper mapper = new SOSMapper();
	OriginalMetadata originalMD = new OriginalMetadata();
	String metadata = "#Tue May 21 11:23:19 CEST 2019\n" + //
		"LONGITUDE=6.14102537485383\n" + //
		"FOI_NAME=iQuest\n" + //
		"FOI_ID=http://kiwis.kisters.de/stations/KISTERS_AC5\n" + //
		"PROPERTY_NAME=BatVolt\n" + //
		"PROCEDURE_IDENTIFIER=http://kiwis.kisters.de/tstypes/SV.Cmd.O\n" + //
		"LATITUDE=50.7112900962093\n" + //
		"PROPERTY_ID=http://kiwis.kisters.de/parameters/BatVolt\n" + //
		"END=2016-09-17T00:50:00Z\n" + //
		"BEGIN=2008-08-11T02:01:14Z\n" + //
		"";
	originalMD.setMetadata(metadata);
	GSSource source = new GSSource();
	source.setEndpoint("http://localhost/my-sos");
	GSResource resource = mapper.map(originalMD, source);
	CoreMetadata core = resource.getHarmonizedMetadata().getCoreMetadata();
	assertEquals(6.14102537485383, core.getBoundingBox().getWest(), 0.000001);
	assertEquals(50.7112900962093, core.getBoundingBox().getSouth(), 0.000001);
	assertEquals("http://kiwis.kisters.de/stations/KISTERS_AC5", core.getMIMetadata().getMIPlatform().getMDIdentifierCode());
	// assertEquals("http://kiwis.kisters.de/tstypes/SV.Cmd.O", core.getMIMetadata().get);
	assertEquals("http://kiwis.kisters.de/parameters/BatVolt", core.getMIMetadata().getCoverageDescription().getAttributeIdentifier());
	SOSIdentifierMangler mangler = new SOSIdentifierMangler();
	mangler.setFeature("http://kiwis.kisters.de/stations/KISTERS_AC5");
	mangler.setProcedure("http://kiwis.kisters.de/tstypes/SV.Cmd.O");
	mangler.setObservedProperty("http://kiwis.kisters.de/parameters/BatVolt");

	String name = mangler.getMangling(); //
	assertEquals(name, core.getMIMetadata().getDistribution().getDistributionOnline().getName());
    }
}
