package eu.essi_lab.shared.driver.es.connector;

import java.util.ServiceLoader;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author ilsanto
 */
public class IESConnectorTest {

    @Test
    public void test() {

	Assert.assertTrue(ServiceLoader.load(IESConnector.class).iterator().hasNext());

    }

}