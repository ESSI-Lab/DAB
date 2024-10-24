package eu.essi_lab.sensorthings.client._1_1.service.request.test;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.lib.sensorthings._1_1.client.request.EntityRef;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.Thing;
import eu.essi_lab.lib.utils.IOStreamUtils;

/**
 * @author Fabrizio
 */
public class ClerarInlineEntitiesTest {

    @Test
    public void test() throws JSONException, IOException {

	Thing thing = new Thing(new JSONObject(IOStreamUtils.asUTF8String(
		getClass().getClassLoader().getResourceAsStream("thingWithDatastreamInline.json"))));

	Assert.assertTrue(thing.isInline(EntityRef.DATASTREAMS).get());

	Assert.assertEquals(1, thing.getDatastreams().size());
	
	Assert.assertTrue(thing.clearInlineEntities(EntityRef.DATASTREAMS));
	
	Assert.assertEquals(0, thing.getDatastreams().size());
    }
}
