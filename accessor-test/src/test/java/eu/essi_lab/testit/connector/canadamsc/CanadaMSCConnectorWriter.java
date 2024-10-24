package eu.essi_lab.testit.connector.canadamsc;

import java.util.Iterator;
import java.util.UUID;

import eu.essi_lab.access.augmenter.AccessAugmenter;
import eu.essi_lab.accessor.canada.CANADAMSCConnector;
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

public class CanadaMSCConnectorWriter {

	public static void main(String[] args) throws Exception {

		// ------------------------------------------------------------------------
		// this is used to delete all the repositories, including the ones created
		// with different suite identifiers
		StorageUri TEST_DB_URI = new StorageUri("xdbc://localhost:8000,8004");
		TEST_DB_URI.setStorageName("TEST-DB");
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
		// source.setEndpoint("http://icewater.usu.edu/littlebearriverwof/cuahsi_1_1.asmx?WSDL");
		// source.setUniqueIdentifier("lbr");
		// source.setLabel("Little Bear River");

		// source.setEndpoint("http://hydrolite.ddns.net/hsl-fi/index.php/default/services/cuahsi_1_1.asmx?WSDL");
		// source.setUniqueIdentifier("wof");

		// source.setLabel("WOF");

		source.setEndpoint("http://dd.weather.gc.ca/hydrometric/");
		source.setUniqueIdentifier("wof");
		source.setLabel("WOF");

		// source.setLabel("finland");

//	source.setEndpoint("http://hydrolite.ddns.net/hsl-no/index.php/default/services/cuahsi_1_1.asmx?WSDL");
//	source.setUniqueIdentifier("wof");
//	source.setLabel("norway");
//	CUAHSIHISServerConnector connector = new CUAHSIHISServerConnector();

		DatabaseProvider provider = new DatabaseProviderFactory().create(TEST_DB_URI);
		provider.initialize(TEST_DB_URI, suiteIdentifier);

		DatabaseWriter writer = new DatabaseConsumerFactory().createDataBaseWriter(TEST_DB_URI);
		SourceStorage storage = new DatabaseConsumerFactory().createSourceStorage(TEST_DB_URI);

		// harvesting started
		storage.harvestingStarted(source, HarvestingStrategy.FULL, false);

		HarvestedAccessor accessor = new HarvestedAccessor();
		accessor.setGSSource(source);

		CANADAMSCConnector connector = new CANADAMSCConnector();

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
