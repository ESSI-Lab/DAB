package eu.essi_lab.shared.driver.es.connector;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import eu.essi_lab.model.StorageInfo;

/**
 * @author ilsanto
 */
public class ESConnectorTest {

    @Test
    public void test() {

	ESConnector connector = new ESConnector();

	String url = "http://localhost";

	StorageInfo storage = new StorageInfo(url);

	connector.setEsStorageUri(storage);

	assertEquals(url + "/", connector.getEsStorageUri().getUri());

    }


    @Test
    public void test2() {

	ESConnector connector = new ESConnector();

	String url = "http://localhost/";

	StorageInfo storage = new StorageInfo(url);

	connector.setEsStorageUri(storage);

	assertEquals(url , connector.getEsStorageUri().getUri());

    }

}