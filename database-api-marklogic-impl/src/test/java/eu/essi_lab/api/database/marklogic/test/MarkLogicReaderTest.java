//package eu.essi_lab.api.database.marklogic.test;
//
//import org.junit.Test;
//
//import eu.essi_lab.api.database.Database;
//import eu.essi_lab.api.database.marklogic.MarkLogicDatabase;
//import eu.essi_lab.api.database.marklogic.MarkLogicReader;
//import eu.essi_lab.model.StorageUri;
//import eu.essi_lab.model.configuration.Deserializer;
//import eu.essi_lab.model.exceptions.GSException;
//
///**
// * @author ilsanto
// */
//public class MarkLogicReaderTest {
//
//    @Test
//    public void serialize() throws GSException {
//
//	new MarkLogicReader().serialize();
//
//	MarkLogicReader reader = new MarkLogicReader();
//
//	Database db = new MarkLogicDatabase();
//	reader.setDatabase(db);
//	reader.serialize();
//
//	StorageUri uri = new StorageUri();
//	uri.setUri("xdbc://uri");
//	reader.supports(uri);
//
//	MarkLogicReader deserialized = new Deserializer().deserialize(reader.serialize(), MarkLogicReader.class);
//    }
//}
