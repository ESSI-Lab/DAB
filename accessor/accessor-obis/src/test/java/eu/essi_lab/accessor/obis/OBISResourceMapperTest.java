package eu.essi_lab.accessor.obis;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.accessor.obis.harvested.OBISResourceMapper;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author ilsanto
 */
public class OBISResourceMapperTest {

    @Test
    public void test() throws IOException, GSException {

	InputStream stream = OBISResourceMapperTest.class.getClassLoader().getResourceAsStream("obisOriginal.json");

	String md = IOStreamUtils.asUTF8String(stream);

	OriginalMetadata om = new OriginalMetadata();
	om.setMetadata(md);

	GSSource source = new GSSource();
	source.setUniqueIdentifier(UUID.randomUUID().toString());

	OBISResourceMapper mapper = new OBISResourceMapper();
	GSResource mapped = mapper.map(om, source);

	String origid = mapped.getOriginalId().get();

	Assert.assertEquals("9f1eb607-5c81-4bd3-a869-09a190c5aee3", origid);

	Assert.assertEquals("2018-11-13T15:01:20.000Z",
		mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getCitationPublicationDate());

	Assert.assertEquals("Marine and Coastal Management - Linefish Dataset (AfrOBIS)",
		mapped.getHarmonizedMetadata().getCoreMetadata().getTitle());

	Assert.assertEquals("Marine and Coastal Management; Department of Environmental Affairs and Tourism", mapped.getHarmonizedMetadata()
		.getCoreMetadata().getMIMetadata().getDataIdentification().getPointOfContact().getOrganisationName());

    }
}