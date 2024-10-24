package eu.essi_lab.cdk.utils.test;

import static org.junit.Assert.assertTrue;

import java.net.URISyntaxException;

import org.junit.Test;

import eu.essi_lab.lib.net.utils.HttpConnectionUtils;

/**
 * @author Fabrizio
 */
public class HttpConnectionUtilsExternalTestIT {

    @Test
    public void resolveRedirectTest() {
	String url = "https://www.seadatanet.org/urnurl/SDN:P06::ULAA";
	url = HttpConnectionUtils.resolveRedirect(url);
	assertTrue(url.startsWith("https://vocab.nerc.ac.uk/collection/P06/current/ULAA"));
    }

    @Test
    public void checkConnectivityTest() throws URISyntaxException {

	check(true, "http://www.google.com");
	check(false, "http://www.google43u4903c.com");
	check(true, "http://afromaison.grid.unep.ch:8080/geoserver/ows?");
    }

    private boolean check(boolean expected, String url) throws URISyntaxException {
	long start = System.currentTimeMillis();
	boolean ret = HttpConnectionUtils.checkConnectivity(url);
	long gap = System.currentTimeMillis() - start;
	assertTrue("The check took too much time (more than 10 seconds): " + (gap / 1000) + " seconds", gap < 10000);
	return ret;
    }
}
