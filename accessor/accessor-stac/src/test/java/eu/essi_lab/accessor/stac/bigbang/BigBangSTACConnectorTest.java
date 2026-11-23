package eu.essi_lab.accessor.stac.bigbang;

import java.util.List;
import java.util.ServiceLoader;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.accessor.stac.harvested.STACConnector;
import eu.essi_lab.accessor.stac.harvested.STACConnectorWrapperSetting;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.lib.utils.StreamUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.ommdk.IResourceMapper;

public class BigBangSTACConnectorTest {

    @Test
    public void resolveCatalogURLTest() {

	String expected = "http://boldrini.essi-lab.eu:8080/stac/catalog.json";

	Assert.assertEquals(expected, BigBangSTACConnector.resolveCatalogURL("http://boldrini.essi-lab.eu:8080/stac/catalog.json"));
	Assert.assertEquals(expected, BigBangSTACConnector.resolveCatalogURL(" http://boldrini.essi-lab.eu:8080/stac/catalog.json?x=1 "));
	Assert.assertEquals(expected, BigBangSTACConnector.resolveCatalogURL("http://boldrini.essi-lab.eu:8080/stac/"));
	Assert.assertEquals(expected, BigBangSTACConnector.resolveCatalogURL("http://boldrini.essi-lab.eu:8080/stac"));
    }

    @Test
    public void extractChildURLsTest() {

	JSONObject catalog = new JSONObject("{\"links\":[" //
		+ "{\"rel\":\"self\",\"href\":\"https://boldrini.essi-lab.eu/stac/catalog.json\"}," //
		+ "{\"rel\":\"license\",\"href\":\"https://www.dati.gov.it/content/italian-open-data-license-v20\"}," //
		+ "{\"rel\":\"child\",\"href\":\"https://boldrini.essi-lab.eu/stac/bigbang100-spi/collection.json\"}," //
		+ "{\"rel\":\"child\",\"href\":\"./bigbang100-spei/collection.json\"}]}");

	List<String> urls = BigBangSTACConnector.extractChildURLs(catalog, "https://boldrini.essi-lab.eu/stac/catalog.json");

	Assert.assertEquals(List.of("https://boldrini.essi-lab.eu/stac/bigbang100-spi/collection.json",
		"https://boldrini.essi-lab.eu/stac/bigbang100-spei/collection.json"), urls);
    }

    @Test
    public void rebaseTest() {

	String published = "https://boldrini.essi-lab.eu/stac/catalog.json";
	String configured = "http://boldrini.essi-lab.eu:8080/stac/catalog.json";

	Assert.assertEquals("http://boldrini.essi-lab.eu:8080/stac/bigbang100-spi/collection.json",
		BigBangSTACConnector.rebase("https://boldrini.essi-lab.eu/stac/bigbang100-spi/collection.json", published, configured));

	// not under the published catalog folder: unchanged
	Assert.assertEquals("https://example.org/other/collection.json",
		BigBangSTACConnector.rebase("https://example.org/other/collection.json", published, configured));
    }

    @Test
    public void supportsTest() {

	GSSource source = new GSSource();

	source.setEndpoint("http://boldrini.essi-lab.eu:8080/stac/catalog.json");
	Assert.assertTrue(new BigBangSTACConnector().supports(source));

	source.setEndpoint("https://example.org/bigbang/stac/catalog.json");
	Assert.assertTrue(new BigBangSTACConnector().supports(source));

	source.setEndpoint("https://polarwatch.noaa.gov/stac/catalog.json");
	Assert.assertFalse(new BigBangSTACConnector().supports(source));
    }

    @Test
    public void serviceLoaderTest() {

	Assert.assertTrue(StreamUtils.iteratorToStream(ServiceLoader.load(STACConnector.class).iterator())
		.anyMatch(c -> c.getClass().equals(BigBangSTACConnector.class)));

	Assert.assertTrue(StreamUtils.iteratorToStream(ServiceLoader.load(IResourceMapper.class).iterator())
		.anyMatch(m -> BigBangSTACMapper.BIGBANG_STAC_SCHEME_URI.equals(m.getSupportedOriginalMetadataSchema())));
    }

    @Test
    public void parseTokenTest() {

	Assert.assertArrayEquals(new int[] { 0, 0, 0 }, BigBangSTACConnector.parseToken(null));
	Assert.assertArrayEquals(new int[] { 3, 120, 950 }, BigBangSTACConnector.parseToken("3:120:950"));
	// collections only token
	Assert.assertArrayEquals(new int[] { 5, 0, 5 }, BigBangSTACConnector.parseToken("5"));
    }

    @Test
    public void harvestItemsOptionTest() {

	STACConnectorWrapperSetting setting = new STACConnectorWrapperSetting();
	setting.selectConnectorType(BigBangSTACConnector.TYPE);

	Assert.assertFalse(setting.isHarvestItems());
	Assert.assertFalse(setting.getSelectedConnector().isHarvestItems());

	setting.setHarvestItems(true);

	Assert.assertTrue(setting.isHarvestItems());
	STACConnector connector = setting.getSelectedConnector();
	Assert.assertTrue(connector instanceof BigBangSTACConnector);
	Assert.assertTrue(connector.isHarvestItems());

	// the option survives the JSON serialization used to store the configuration
	Setting stored = new Setting(new JSONObject(setting.getObject().toString()));
	STACConnectorWrapperSetting copy = SettingUtils.downCast(stored, STACConnectorWrapperSetting.class);
	Assert.assertTrue(copy.isHarvestItems());
	Assert.assertTrue(copy.getSelectedConnector().isHarvestItems());
    }
}
