package eu.essi_lab.accessor.stac.polarwatch;

import java.util.List;
import java.util.ServiceLoader;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.accessor.stac.harvested.STACConnector;
import eu.essi_lab.lib.utils.StreamUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.ommdk.IResourceMapper;

public class PolarWatchSTACConnectorTest {

    @Test
    public void resolveCatalogURLTest() {

	String expected = "https://polarwatch.noaa.gov/stac/catalog.json";

	Assert.assertEquals(expected, PolarWatchSTACConnector.resolveCatalogURL("https://polarwatch.noaa.gov/stac/catalog.json"));
	Assert.assertEquals(expected, PolarWatchSTACConnector.resolveCatalogURL("https://polarwatch.noaa.gov/stac.html"));
	Assert.assertEquals(expected, PolarWatchSTACConnector.resolveCatalogURL("https://polarwatch.noaa.gov/"));
	Assert.assertEquals(expected, PolarWatchSTACConnector.resolveCatalogURL("https://polarwatch.noaa.gov/stac/"));
	Assert.assertEquals(expected, PolarWatchSTACConnector.resolveCatalogURL("https://polarwatch.noaa.gov/stac"));
    }

    @Test
    public void extractCollectionURLsTest() {

	JSONObject catalog = new JSONObject("{\"links\":[" //
		+ "{\"rel\":\"self\",\"href\":\"./catalog.json\"}," //
		+ "{\"rel\":\"child\",\"href\":\"./collections/a/collection.json\"}," //
		+ "{\"rel\":\"child\",\"href\":\"./collections/b/collection.json\"}]}");

	List<String> urls = PolarWatchSTACConnector.extractCollectionURLs(catalog, "https://polarwatch.noaa.gov/stac/catalog.json");

	Assert.assertEquals(List.of("https://polarwatch.noaa.gov/stac/collections/a/collection.json",
		"https://polarwatch.noaa.gov/stac/collections/b/collection.json"), urls);
    }

    @Test
    public void findERDDAPDatasetTest() {

	JSONObject service = new JSONObject("{\"links\":[" //
		+ "{\"rel\":\"about\",\"href\":\"https://coastwatch.noaa.gov/erddap/griddap/abc.html\"}," //
		+ "{\"rel\":\"service\",\"href\":\"https://coastwatch.noaa.gov/erddap/griddap/abc\"}]}");

	JSONObject erddap = PolarWatchSTACConnector.findERDDAPDataset(service).get();
	Assert.assertEquals("https://coastwatch.noaa.gov/erddap", erddap.getString(PolarWatchSTACMapper.ERDDAP_BASE));
	Assert.assertEquals("griddap", erddap.getString(PolarWatchSTACMapper.ERDDAP_PROTOCOL));
	Assert.assertEquals("abc", erddap.getString(PolarWatchSTACMapper.ERDDAP_DATASET_ID));

	JSONObject aboutOnly = new JSONObject("{\"links\":[{\"rel\":\"about\",\"href\":\"https://polarwatch.noaa.gov/erddap/griddap/lara.html\"}]}");
	Assert.assertEquals("lara", PolarWatchSTACConnector.findERDDAPDataset(aboutOnly).get().getString(PolarWatchSTACMapper.ERDDAP_DATASET_ID));

	// ERDDAP search pages and files directories are not datasets
	JSONObject none = new JSONObject("{\"links\":[],\"assets\":{" //
		+ "\"erddap\":{\"href\":\"https://coastwatch.noaa.gov/erddap/search/index.html?searchFor=Ice\"}," //
		+ "\"files\":{\"href\":\"https://coastwatch.noaa.gov/erddap/files/abc\"}}}");
	Assert.assertFalse(PolarWatchSTACConnector.findERDDAPDataset(none).isPresent());
    }

    @Test
    public void supportsTest() {

	GSSource source = new GSSource();
	source.setEndpoint("https://polarwatch.noaa.gov/stac/catalog.json");

	Assert.assertTrue(new PolarWatchSTACConnector().supports(source));
    }

    @Test
    public void serviceLoaderTest() {

	Assert.assertTrue(StreamUtils.iteratorToStream(ServiceLoader.load(STACConnector.class).iterator())
		.anyMatch(c -> c.getClass().equals(PolarWatchSTACConnector.class)));

	Assert.assertTrue(StreamUtils.iteratorToStream(ServiceLoader.load(IResourceMapper.class).iterator())
		.anyMatch(m -> PolarWatchSTACMapper.POLARWATCH_STAC_SCHEME_URI.equals(m.getSupportedOriginalMetadataSchema())));
    }
}
