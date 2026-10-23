package eu.essi_lab.gssrv.conf.task.collection;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.access.datacache.DataRecord;
import eu.essi_lab.access.datacache.LatitudeLongitude;
import eu.essi_lab.access.datacache.WIS2DataItem;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.DefaultConfiguration;
import eu.essi_lab.cfga.gs.setting.WIS2Setting;
import eu.essi_lab.lib.whos.WIS2DataPolicy;
import eu.essi_lab.lib.whos.WIS2HydrologyTopic;
import eu.essi_lab.profiler.wis.WISProfilerSetting;
import eu.essi_lab.profiler.wis.WISUtils;

/**
 * Tests the WIS2 data notification messages
 *
 * @author boldrini
 */
public class WIS2DataNotifierTest {

    private static final String CENTRE_ID = "it-cnr-itiam-whos-dab-test";

    @Before
    public void before() throws Exception {

	DefaultConfiguration configuration = new DefaultConfiguration();
	configuration.clean();

	ConfigurationWrapper.setConfiguration(configuration);

	WIS2Setting setting = ConfigurationWrapper.getWIS2Setting();
	setting.setCentreId(CENTRE_ID);
	setting.setHostname("https://wis2.whos-dab.eu");
	configuration.replace(setting);
    }

    @Test
    public void messageTest() throws Exception {

	DataRecord record = new DataRecord();
	record.setDataIdentifier("urn:argentina-ina:52/stage");
	record.setSourceIdentifier("argentina-ina");
	record.setDate(new Date(1792746000000L));
	record.setValue(new BigDecimal("2.370"));
	record.setUom("m");
	record.setObservedProperty("Water level");
	record.setObservedPropertyURI("http://hydro.geodab.eu/hydro-ontology/concept/11");
	record.setLatitudeLongitude(new LatitudeLongitude(new BigDecimal("-34.6"), new BigDecimal("-58.3667")));

	String topic = WIS2HydrologyTopic.STREAM.getTopic(CENTRE_ID, WIS2DataPolicy.CORE);
	String metadataId = WISUtils.getMetadataIdentifier("argentina-ina");

	JSONObject message = WIS2DataNotifier.createMessage(record, topic, "whos", metadataId, "WHOS DAB").getJSONObject();

	System.out.println(message.toString(3));

	String itemId = WIS2DataItem.getItemId(record);
	String content = WIS2DataItem.encode(record);

	Assert.assertEquals("http://wis.wmo.int/spec/wnm/1/conf/core", message.getJSONArray("conformsTo").getString(0));
	Assert.assertEquals("Feature", message.getString("type"));
	UUID.fromString(message.getString("id"));

	JSONObject geometry = message.getJSONObject("geometry");
	Assert.assertEquals("Point", geometry.getString("type"));
	JSONArray coordinates = geometry.getJSONArray("coordinates");
	Assert.assertEquals(-58.3667, coordinates.getDouble(0), 0);
	Assert.assertEquals(-34.6, coordinates.getDouble(1), 0);

	JSONObject properties = message.getJSONObject("properties");
	Assert.assertEquals(CENTRE_ID + "/data/core/hydrology/surface-based-observations/water-quantity/stream/" + itemId,
		properties.getString("data_id"));
	Assert.assertEquals("urn:wmo:md:" + CENTRE_ID + ":argentina-ina", properties.getString("metadata_id"));
	Assert.assertEquals("2026-10-23T09:00:00Z", properties.getString("datetime"));
	Assert.assertTrue(properties.has("pubtime"));

	// inline content
	JSONObject contentObject = properties.getJSONObject("content");
	Assert.assertEquals("utf-8", contentObject.getString("encoding"));
	Assert.assertEquals(content, contentObject.getString("value"));
	Assert.assertEquals(content.getBytes(StandardCharsets.UTF_8).length, contentObject.getInt("size"));

	// integrity of the content, also served at the canonical link
	String digest = Base64.getEncoder()
		.encodeToString(MessageDigest.getInstance("SHA-512").digest(content.getBytes(StandardCharsets.UTF_8)));
	Assert.assertEquals("sha512", properties.getJSONObject("integrity").getString("method"));
	Assert.assertEquals(digest, properties.getJSONObject("integrity").getString("value"));

	JSONArray links = message.getJSONArray("links");
	Assert.assertEquals(1, links.length());
	JSONObject link = links.getJSONObject(0);
	Assert.assertEquals("canonical", link.getString("rel"));
	Assert.assertEquals("application/json", link.getString("type"));
	Assert.assertEquals("https://wis2.whos-dab.eu/gs-service/services/essi/view/whos/" + new WISProfilerSetting().getServicePath()
		+ "/collections/observations/items/" + itemId, link.getString("href"));
	Assert.assertEquals(content.getBytes(StandardCharsets.UTF_8).length, link.getInt("length"));
    }
}
