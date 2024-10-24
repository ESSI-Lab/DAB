package eu.essi_lab.shared.driver.es.query;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author ilsanto
 */
public class ESTimeRangeBuilderTest {

    @Test
    public void test() {

	String att = "start";

	Long from = 1539183706000L;// 10 Oct 2018 15:01:46

	Long to = 1540479706000L; // 25 Oct 2018 15:01:46

	JSONObject json = new ESTimeRangeBuilder(att).withFrom(from).withTo(to).build();

	Assert.assertTrue(json.has(att));

	Assert.assertTrue(json.getJSONObject(att).has("gte"));
	Assert.assertTrue(json.getJSONObject(att).has("lte"));

	Assert.assertEquals(to, (Long) json.getJSONObject(att).getLong("lte"));
	Assert.assertEquals(from, (Long) json.getJSONObject(att).getLong("gte"));

    }
}