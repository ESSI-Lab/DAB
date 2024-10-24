package eu.essi_lab.testit.connector.wcs;

import java.util.Iterator;
import java.util.UUID;

import eu.essi_lab.access.augmenter.AccessAugmenter;
import eu.essi_lab.accessor.wcs.connector.WCSConnector_100;
import eu.essi_lab.adk.harvest.HarvestedAccessor;
import eu.essi_lab.api.database.DatabaseProvider;
import eu.essi_lab.api.database.DatabaseWriter;
import eu.essi_lab.api.database.HarvestingStrategy;
import eu.essi_lab.api.database.SourceStorage;
import eu.essi_lab.api.database.factory.DatabaseConsumerFactory;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.api.database.marklogic.MarkLogicDatabase;
import eu.essi_lab.indexes.IndexedElementsWriter;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.StorageUri;
import eu.essi_lab.model.resource.GSResource;

public class WCSConnectorWriter {

    public static void main(String[] args) throws Exception {
	// ------------------------------------------------------------------------
	// this is used to delete all the repositories, including the ones created
	// with different suite identifiers
	StorageUri TEST_DB_URI = new StorageUri(System.getProperty("dbUrl"));
	
	TEST_DB_URI.setStorageName("TEST-DB");
	// TEST_DB_URI.setStorageName("TEST-DB");
	TEST_DB_URI.setUser(System.getProperty("dbUser")); // the admin user
	TEST_DB_URI.setPassword(System.getProperty("dbPassword"));

	String suiteIdentifier = "gisuite";
	boolean delete = false;
	if (delete) {
	    MarkLogicDatabase root = new MarkLogicDatabase();
	    root.initialize(TEST_DB_URI, suiteIdentifier);
	    root.removeFolders();
	}
	// ------------------------------------------------------------------------

	GSSource source = new GSSource();
	source.setBrokeringStrategy(BrokeringStrategy.HARVESTED);

	int n = 2;

	switch (n) {
	case 0:
	    source.setEndpoint("http://afromaison.grid.unep.ch:8080/geoserver/ows?");
	    source.setUniqueIdentifier("afrowcs100");
	    source.setLabel("Afromaison - UNEP GRID WCS");
	    break;
	case 1:
	    source.setEndpoint("http://sedac.ciesin.columbia.edu/geoserver/wcs?");
	    source.setUniqueIdentifier("sedacwcs111");
	    source.setLabel("");
	    break;
	case 2:
	    source.setEndpoint("http://ows.eox.at/cite/mapserver?");
	    source.setUniqueIdentifier("eox"); // not in wcs config
	    source.setLabel("");
	    break;
	case 3:
	    source.setEndpoint("http://gis.csiss.gmu.edu/cgi-bin/mapserv?MAP=/media/gisiv01/mapfiles/drought/16days/2012/drought.2012.065.map&");
	    source.setUniqueIdentifier("drought"); // not in wcs config
	    source.setLabel("");
	    break;
	case 4:
	    source.setEndpoint("http://gis.csiss.gmu.edu/cgi-bin/cdl_services?");
	    source.setUniqueIdentifier("croplandwcs100");
	    source.setLabel("");
	    break;
	case 5:
	    source.setEndpoint("http://nsidc.org/cgi-bin/atlas_north?");
	    source.setUniqueIdentifier("nsidcwcs100");
	    source.setLabel("");
	    break;
	default:
	    break;
	}

	DatabaseProvider provider = new DatabaseProviderFactory().create(TEST_DB_URI);
	provider.initialize(TEST_DB_URI, suiteIdentifier);

	DatabaseWriter writer = new DatabaseConsumerFactory().createDataBaseWriter(TEST_DB_URI);
	SourceStorage storage = new DatabaseConsumerFactory().createSourceStorage(TEST_DB_URI);

	// harvesting started
	storage.harvestingStarted(source, HarvestingStrategy.FULL, false);

	HarvestedAccessor accessor = new HarvestedAccessor();
	accessor.setGSSource(source);

	WCSConnector_100 connector = new WCSConnector_100();
	// connector.setFirstSiteOnly(true);
	connector.setMaxRecords(5);
	accessor.setConnector(connector);

	ListRecordsRequest request = new ListRecordsRequest();

	String resumptionToken = null;
	do {

	    request.setResumptionToken(resumptionToken);

	    ListRecordsResponse<GSResource> response = accessor.listRecords(request);
	    Iterator<GSResource> records = response.getRecords();
	    while (records.hasNext()) {

		GSResource next = records.next();
		next.setPrivateId(UUID.randomUUID().toString());
		boolean augment = true;
		if (augment) {
		    AccessAugmenter augmenter = new AccessAugmenter();
		    augmenter.augment(next);
		}

		IndexedElementsWriter.write(next);

		writer.store(next);
	    }

	    resumptionToken = response.getResumptionToken();
	} while (resumptionToken != null);

	// harvesting ended
	storage.harvestingEnded(source, HarvestingStrategy.FULL);
    }
}
