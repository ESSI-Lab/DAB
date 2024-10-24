package eu.essi_lab.testit.connector.ckan;

import java.io.IOException;
import java.io.InputStream;

import org.apache.cxf.helpers.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;

import eu.essi_lab.accessor.ckan.CKANMapper;
import eu.essi_lab.api.database.DatabaseProvider;
import eu.essi_lab.api.database.DatabaseWriter;
import eu.essi_lab.api.database.HarvestingStrategy;
import eu.essi_lab.api.database.SourceStorage;
import eu.essi_lab.api.database.factory.DatabaseConsumerFactory;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.api.database.marklogic.MarkLogicDatabase;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.StorageUri;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.testit.connector.geoss.GEOSSConnectorWriter;
import junit.framework.TestCase;

public class CKANConnectorWriterUKDataGOV {

    private GSSource source;
    
    public Logger logger;
    
    private StorageUri TEST_DB_URI;
    
    private MarkLogicDatabase root;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void init() throws GSException {
	logger = GSLoggerFactory.getLogger(GEOSSConnectorWriter.class);

	// ------------------------------------------------------------------------
	// this is used to delete all the repositories, including the ones created
	// with different suite identifiers
	TEST_DB_URI = new StorageUri("xdbc://localhost:8000,8004");

	TEST_DB_URI.setStorageName("TEST-DB");
	TEST_DB_URI.setUser(System.getProperty("dbUser")); // the admin user
	TEST_DB_URI.setPassword(System.getProperty("dbPassword"));

	root = new MarkLogicDatabase();
	root.initialize(TEST_DB_URI, "ROOT");
	//root.removeFolders();
	// ------------------------------------------------------------------------

	source = new GSSource();
	source.setBrokeringStrategy(BrokeringStrategy.HARVESTED);
	source.setEndpoint("https://data.gov.uk");
	source.setUniqueIdentifier("ukdatagovid");
	source.setLabel("UK Data Gov");
    }


    @Test
    public void testSupport1() throws GSException, IOException {
	InputStream stream = CKANConnectorWriterUKDataGOV.class.getClassLoader().getResourceAsStream("ukdatagov_b77fcedb-4792-4de4-935f-4f344ed4c2c6.json");
	TestCase.assertNotNull(stream);
	String string = IOUtils.toString(stream);
	stream.close();
	
	String suiteIdentifier = "gisuite";

	DatabaseProvider provider = new DatabaseProviderFactory().create(TEST_DB_URI);
	provider.initialize(TEST_DB_URI, suiteIdentifier);

	DatabaseWriter writer = new DatabaseConsumerFactory().createDataBaseWriter(TEST_DB_URI);
	SourceStorage storage = new DatabaseConsumerFactory().createSourceStorage(TEST_DB_URI);

	// harvesting started
	storage.harvestingStarted(source, HarvestingStrategy.FULL, false);


	OriginalMetadata om = new OriginalMetadata();

//	string = string.replace("<![CDATA[", "essi_labCDATA_start").replace("]]>", "essi_labCDATA_end");
//	System.out.println(string);
	om.setMetadata(string);
	CKANMapper mapper = new CKANMapper();
	GSResource gssource = mapper.map(om, source);
	
	writer.store(gssource);
	
	logger.info("INSERTED RECORD");
    }   
}
