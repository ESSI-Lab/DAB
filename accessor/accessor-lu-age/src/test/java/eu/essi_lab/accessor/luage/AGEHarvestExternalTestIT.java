package eu.essi_lab.accessor.luage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.model.resource.data.DataDescriptor;

/**
 * Harvests all the stations, maps all the records and downloads the last day of a series
 *
 * @author boldrini
 */
public class AGEHarvestExternalTestIT {

    @Test
    public void test() throws Exception {

	AGEConnector connector = new AGEConnector();
	connector.setSourceURL(AGEClient.DEFAULT_ENDPOINT);

	GSSource source = new GSSource();
	source.setEndpoint(AGEClient.DEFAULT_ENDPOINT);

	AGEMapper mapper = new AGEMapper();

	List<GSResource> resources = new ArrayList<>();
	GSResource hesperange = null;

	String token = null;
	do {
	    ListRecordsRequest request = new ListRecordsRequest();
	    request.setResumptionToken(token);

	    ListRecordsResponse<OriginalMetadata> response = connector.listRecords(request);
	    for (OriginalMetadata record : response.getRecordsAsList()) {

		GSResource resource = mapper.map(record, source);
		System.out.println("MAPPED " + resource.getHarmonizedMetadata().getCoreMetadata().getTitle() + " ["
			+ resource.getExtensionHandler().getAttributeUnitsAbbreviation().orElse("") + ", "
			+ resource.getExtensionHandler().getCountry().orElse("") + "]");
		resources.add(resource);

		if (resource.getHarmonizedMetadata().getCoreMetadata().getOnline().getName().equals("Hesperange|W")) {
		    hesperange = resource;
		}
	    }
	    token = response.getResumptionToken();

	} while (token != null);

	assertTrue(resources.size() > 30);
	// the stations operated by other agencies are not harvested
	for (String partner : new String[] { "SN_Grevenmacher|W", "SN_Remich|W", "SN_Stadtbredimus|W", "SN_Wasserbillig|W", "Perl|W",
		"Bollendorf|W", "Gemünd_Our|W" }) {
	    assertTrue(partner, resources.stream()
		    .noneMatch(r -> r.getHarmonizedMetadata().getCoreMetadata().getOnline().getName().equals(partner)));
	}
	assertEquals(resources.size(), resources.stream().map(GSResource::getOriginalId).distinct().count());
	assertNotNull(hesperange);

	Online online = hesperange.getHarmonizedMetadata().getCoreMetadata().getOnline();
	// during harvesting the identifier is assigned by the identifier decorator
	online.setIdentifier();

	AGEDownloader downloader = new AGEDownloader();
	downloader.setOnlineResource(hesperange, online.getIdentifier());
	assertTrue(downloader.canDownload());
	assertTrue(downloader.canConnect());

	DataDescriptor descriptor = downloader.getRemoteDescriptors().get(0);
	assertNotNull(descriptor.getTemporalDimension());

	long end = descriptor.getTemporalDimension().getContinueDimension().getUpper().longValue();
	descriptor.setTemporalDimension(new Date(end - 24l * 60 * 60 * 1000), new Date(end));

	File file = downloader.download(descriptor);
	assertNotNull(file);

	String wml = Files.readString(file.toPath());
	assertTrue(wml.contains(":value"));
	System.out.println(wml.substring(wml.indexOf(":value") - 4, Math.min(wml.length(), wml.indexOf(":value") + 400)));
    }
}
