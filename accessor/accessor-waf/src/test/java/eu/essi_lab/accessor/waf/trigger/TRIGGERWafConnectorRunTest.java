package eu.essi_lab.accessor.waf.trigger;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class TRIGGERWafConnectorRunTest {

    private static final String[] VARIABLES = { "2r", "2t", "mrt", "utci", "wbgt", "wct", "cams_eu_ozone_web",
	    "cams_eu_particulate_matter_2.5um_web" };
    private static final String[] CITIES = { "Augsburg", "Bologna", "Geneve", "Irakleion", "Oulu" };

    @Test
    public void completeLatestRunIsUsed() throws Exception {

	List<URL> urls = files("2026110100", CITIES.length);
	urls.addAll(files("2026110200", CITIES.length));
	urls.add(new URL("https://aux.ecmwf.int/ecpds/data/file/utci_2026110201.tiff"));

	List<URL> selected = TRIGGERWafConnector.filterURLsByLatestRun(urls);

	Assert.assertEquals(VARIABLES.length * CITIES.length, selected.size());
	Assert.assertTrue(selected.stream().allMatch(url -> url.toString().endsWith("_2026110200.json")));
    }

    @Test
    public void notHarvestedFilesAreIgnored() throws Exception {

	// previous run also has uvindex and the extended anomaly (published the following day)
	List<URL> urls = files("2026110100", CITIES.length);
	for (String city : CITIES) {
	    urls.add(new URL("https://aux.ecmwf.int/ecpds/data/file/uvindex_" + city + "_2026110100.json"));
	    urls.add(new URL("https://aux.ecmwf.int/ecpds/data/file/extended_anomaly_clim_2t_" + city + "_2026110100.json"));
	}
	urls.addAll(files("2026110200", CITIES.length));

	List<URL> selected = TRIGGERWafConnector.filterURLsByLatestRun(urls);

	Assert.assertEquals(VARIABLES.length * CITIES.length, selected.size());
	Assert.assertTrue(selected.stream().allMatch(url -> url.toString().endsWith("_2026110200.json")));
    }

    @Test
    public void unknownFilesAreIgnored() throws Exception {

	List<URL> urls = files("2026110100", CITIES.length);
	for (String city : CITIES) {
	    urls.add(new URL("https://aux.ecmwf.int/ecpds/data/file/newindex_" + city + "_2026110100.json"));
	    urls.add(new URL("https://aux.ecmwf.int/ecpds/data/file/cams_eu_dust_web_" + city + "_2026110100.json"));
	}
	urls.addAll(files("2026110200", CITIES.length));

	List<URL> selected = TRIGGERWafConnector.filterURLsByLatestRun(urls);

	Assert.assertEquals(VARIABLES.length * CITIES.length, selected.size());
	Assert.assertTrue(selected.stream().allMatch(url -> url.toString().endsWith("_2026110200.json")));
    }

    @Test
    public void variableFromFileName() {

	Assert.assertEquals(TRIGGERWAFVariable.UTCI, TRIGGERWafConnector.getVariable("utci_Bologna_2026110200.json"));
	Assert.assertEquals(TRIGGERWAFVariable.PM2_5,
		TRIGGERWafConnector.getVariable("cams_eu_particulate_matter_2.5um_web_Bologna_2026110200.json"));
	Assert.assertNull(TRIGGERWafConnector.getVariable("uvindex_Bologna_2026110200.json"));
	Assert.assertNull(TRIGGERWafConnector.getVariable("extended_anomaly_clim_2t_Bologna_2026110100.json"));
	Assert.assertNull(TRIGGERWafConnector.getVariable("cams_eu_dust_web_Bologna_2026110200.json"));
    }

    @Test
    public void incompleteLatestRunFallsBackToPrevious() throws Exception {

	List<URL> urls = files("2026110100", CITIES.length);
	urls.addAll(files("2026110200", 2));

	List<URL> selected = TRIGGERWafConnector.filterURLsByLatestRun(urls);

	Assert.assertEquals(VARIABLES.length * CITIES.length, selected.size());
	Assert.assertTrue(selected.stream().allMatch(url -> url.toString().endsWith("_2026110100.json")));
    }

    @Test
    public void singleRunIsUsed() throws Exception {

	List<URL> selected = TRIGGERWafConnector.filterURLsByLatestRun(files("2026110200", 3));

	Assert.assertEquals(VARIABLES.length * 3, selected.size());
    }

    @Test
    public void listingLinksAreIgnored() throws Exception {

	List<URL> urls = new ArrayList<>();
	urls.add(new URL("https://aux.ecmwf.int/#"));
	urls.add(new URL("https://aux.ecmwf.int/"));
	urls.add(new URL("https://aux.ecmwf.int"));
	urls.add(new URL("https://aux.ecmwf.int/ecpds/data/file/shiwe_output_20261102.nc"));
	urls.addAll(files("2026110200", CITIES.length));

	Assert.assertEquals(VARIABLES.length * CITIES.length, TRIGGERWafConnector.filterURLsByLatestRun(urls).size());
    }

    @Test
    public void noJsonFiles() throws Exception {

	List<URL> urls = new ArrayList<>();
	urls.add(new URL("https://aux.ecmwf.int/ecpds/data/file/utci_2026110201.tiff"));

	Assert.assertTrue(TRIGGERWafConnector.filterURLsByLatestRun(urls).isEmpty());
    }

    private static List<URL> files(String run, int cities) throws Exception {

	List<URL> urls = new ArrayList<>();
	for (String variable : VARIABLES) {
	    for (int i = 0; i < cities; i++) {
		urls.add(new URL("https://aux.ecmwf.int/ecpds/data/file/" + variable + "_" + CITIES[i] + "_" + run + ".json"));
	    }
	}
	return urls;
    }
}
