package eu.essi_lab.testit.connector.csw;

import java.util.Iterator;
import java.util.UUID;

import eu.essi_lab.accessor.csw.CSWConnector;
import eu.essi_lab.adk.harvest.HarvestedAccessor;
import eu.essi_lab.api.database.DatabaseProvider;
import eu.essi_lab.api.database.DatabaseWriter;
import eu.essi_lab.api.database.HarvestingStrategy;
import eu.essi_lab.api.database.SourceStorage;
import eu.essi_lab.api.database.factory.DatabaseConsumerFactory;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.indexes.IndexedElementsWriter;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.StorageUri;
import eu.essi_lab.model.resource.GSResource;

public class CSWConnectorWriter {

    public static void main(String[] args) throws Exception {

	// ------------------------------------------------------------------------
	// this is used to delete all the repositories, including the ones created
	// with different suite identifiers
	StorageUri TEST_DB_URI = new StorageUri("xdbc://localhost:8000,8004");

	TEST_DB_URI.setStorageName("TEST-DB");
	TEST_DB_URI.setUser(System.getProperty("dbUser")); // the admin user
	TEST_DB_URI.setPassword(System.getProperty("dbPassword"));

	// MarkLogicDatabase root = new MarkLogicDatabase();
	// root.initialize(TEST_DB_URI, "ROOT");
	// root.removeRepositories();
	// ------------------------------------------------------------------------

	GSSource source = new GSSource();
	source.setBrokeringStrategy(BrokeringStrategy.HARVESTED);

	// source.setEndpoint("http://transmed.almazovcentre.ru/jour/oai");
	// source.setEndpoint("http://www.memoriadigitalvasca.es/dspace-oai/request");
	// source.setEndpoint("http://eprints.arums.ac.ir/cgi/oai2");
	// source.setEndpoint("http://papeschi.essi-lab.eu:8085/gs-service/services/essi/cswiso?");
	source.setEndpoint("http://apps.ecmwf.int/csw");
	source.setLabel("Copernicus Atmosphere Monitoring Service (CAMS)");

	source.setUniqueIdentifier("csw");

	String suiteIdentifier = "gisuite";

	DatabaseProvider provider = new DatabaseProviderFactory().create(TEST_DB_URI);
	provider.initialize(TEST_DB_URI, suiteIdentifier);

	DatabaseWriter writer = new DatabaseConsumerFactory().createDataBaseWriter(TEST_DB_URI);
	SourceStorage storage = new DatabaseConsumerFactory().createSourceStorage(TEST_DB_URI);

	// harvesting started
	storage.harvestingStarted(source, HarvestingStrategy.FULL, false);

	HarvestedAccessor accessor = new HarvestedAccessor();
	accessor.setGSSource(source);

	CSWConnector oaiConnector = new CSWConnector();
	// oaiConnector.setMaxRecords(5000);
	accessor.setConnector(oaiConnector);
	// accessor.setMapper(new GMDResourceMapper());

	ListRecordsRequest request = new ListRecordsRequest();
	// request.setFromDateStamp("2017-06-07");

	String resumptionToken = null;
	int count = 0;
	String doc = "<xml>";
	do {

	    request.setResumptionToken(resumptionToken);

	    ListRecordsResponse<GSResource> response = accessor.listRecords(request);
	    Iterator<GSResource> records = response.getRecords();

	    while (records.hasNext()) {

		GSResource next = records.next();
		next.setPrivateId(UUID.randomUUID().toString());

		// System.err.println(next.asString(true));

		count++;
		IndexedElementsWriter.write(next);

		// doc += next.asString(true);

		// writer.store(next);

		System.out.println("---");
		System.out.println(next.asString(true));
		System.out.println("---");

	    }

	    System.out.println("Current count: " + count);

	    resumptionToken = response.getResumptionToken();
	} while (resumptionToken != null);

	doc += "</xml>";
	// System.out.println("**************************************");
	// System.out.println();
	// System.out.println();
	//
	// System.out.println(doc);

	// harvesting ended
	storage.harvestingEnded(source, HarvestingStrategy.FULL);
    }
}
