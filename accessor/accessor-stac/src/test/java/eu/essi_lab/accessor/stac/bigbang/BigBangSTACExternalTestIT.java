package eu.essi_lab.accessor.stac.bigbang;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.DatasetCollection;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * Harvests the live BIGBANG STAC catalog
 */
public class BigBangSTACExternalTestIT {

    @Test
    public void harvestTest() throws Exception {

	BigBangSTACConnector connector = new BigBangSTACConnector();
	connector.setSourceURL("http://boldrini.essi-lab.eu:8080/stac/catalog.json");

	GSSource source = new GSSource();
	source.setUniqueIdentifier(UUID.randomUUID().toString());
	source.setEndpoint("http://boldrini.essi-lab.eu:8080/stac/catalog.json");

	Assert.assertTrue(connector.supports(source));

	BigBangSTACMapper mapper = new BigBangSTACMapper();
	List<GSResource> resources = new ArrayList<>();

	ListRecordsRequest request = new ListRecordsRequest();
	String token = null;

	do {

	    request.setResumptionToken(token);
	    ListRecordsResponse<OriginalMetadata> response = connector.listRecords(request);

	    for (OriginalMetadata om : (Iterable<OriginalMetadata>) () -> response.getRecords()) {
		resources.add(mapper.map(om, source));
	    }

	    token = response.getResumptionToken();

	} while (token != null);

	Assert.assertEquals(16, resources.size());

	for (GSResource resource : resources) {

	    String title = resource.getHarmonizedMetadata().getCoreMetadata().getTitle();
	    Assert.assertTrue(title.startsWith("BIGBANG 10.0 - "));
	    Assert.assertTrue(resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getCoverageDescriptions().hasNext());

	    Assert.assertTrue(resource.getExtensionHandler().getAttributeUnits().isPresent());
	    Assert.assertTrue(resource.getExtensionHandler().getAttributeUnitsAbbreviation().isPresent());

	    System.out.println("UNITS " + resource.getOriginalId().get() + " | "
		    + resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getCoverageDescriptions().next().getAttributeIdentifier()
		    + " | " + resource.getExtensionHandler().getAttributeUnits().get() + " | "
		    + resource.getExtensionHandler().getAttributeUnitsAbbreviation().get());
	}
    }

    /**
     * Full harvest, limited to the first collection record and its first items (crossing page boundaries)
     */
    @Test
    public void harvestItemsTest() throws Exception {

	BigBangSTACConnector connector = new BigBangSTACConnector();
	connector.setSourceURL("http://boldrini.essi-lab.eu:8080/stac/catalog.json");
	connector.setHarvestItems(true);
	connector.getSetting().setMaxRecords(25);
	connector.getSetting().setPageSize(10);

	GSSource source = new GSSource();
	source.setUniqueIdentifier(UUID.randomUUID().toString());
	source.setEndpoint("http://boldrini.essi-lab.eu:8080/stac/catalog.json");

	BigBangSTACMapper mapper = new BigBangSTACMapper();
	List<GSResource> resources = new ArrayList<>();

	ListRecordsRequest request = new ListRecordsRequest();
	String token = null;
	int pages = 0;

	do {

	    request.setResumptionToken(token);
	    ListRecordsResponse<OriginalMetadata> response = connector.listRecords(request);
	    pages++;

	    for (OriginalMetadata om : (Iterable<OriginalMetadata>) () -> response.getRecords()) {
		resources.add(mapper.map(om, source));
	    }

	    token = response.getResumptionToken();

	} while (token != null);

	Assert.assertEquals(3, pages);
	Assert.assertEquals(25, resources.size());

	GSResource parent = resources.get(0);
	Assert.assertTrue(parent instanceof DatasetCollection);
	Assert.assertEquals("bigbang100-actual-evapotranspiration", parent.getOriginalId().get());

	for (GSResource child : resources.subList(1, resources.size())) {

	    Assert.assertEquals("bigbang100-actual-evapotranspiration",
		    child.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getParentIdentifier());
	    Assert.assertTrue(child.getOriginalId().get().startsWith("bigbang100-actual-evapotranspiration/"));
	}

	System.out.println(resources.get(1).getOriginalId().get() + " | " + resources.get(1).getHarmonizedMetadata().getCoreMetadata().getTitle());
	System.out.println(resources.get(24).getOriginalId().get() + " | " + resources.get(24).getHarmonizedMetadata().getCoreMetadata().getTitle());
    }
}
