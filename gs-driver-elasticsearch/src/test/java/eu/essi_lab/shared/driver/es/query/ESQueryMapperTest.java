package eu.essi_lab.shared.driver.es.query;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.messages.Page;
import eu.essi_lab.shared.messages.SharedContentQuery;

/**
 * @author ilsanto
 */
public class ESQueryMapperTest {

    @Test
    public void test() {

	ESQueryMapper mapper = new ESQueryMapper();

	// SharedContentType type = Mockito.mock(SharedContentType.class);
	//
	// String typeString = "typeString";
	//
	//// Mockito.doReturn(typeString).when(type).getType();

	SharedContentQuery query = new SharedContentQuery();

	JSONObject json = mapper.mapToQuery(query);

	Assert.assertEquals(0, json.getInt("from"));

	Assert.assertEquals(10, json.getInt("size"));

	Assert.assertNotNull(json.getJSONObject("query").getJSONObject("match_all"));

    }

    @Test
    public void testWithPage() {

	ESQueryMapper mapper = new ESQueryMapper();

	// SharedContentType type = Mockito.mock(SharedContentType.class);
	//
	// String typeString = "typeString";
	//
	// Mockito.doReturn(typeString).when(type).getType();

	SharedContentQuery query = new SharedContentQuery();

	int start = 3;
	int count = 12;

	Page page = new Page(start, count);
	query.setPage(page);

	JSONObject json = mapper.mapToQuery(query);

	Assert.assertEquals(start - 1, json.getInt("from"));

	Assert.assertEquals(count, json.getInt("size"));

	Assert.assertNotNull(json.getJSONObject("query").getJSONObject("match_all"));

	Assert.assertFalse(json.has("filter"));

    }

    @Test
    public void testWithOneTimeAxisFromOnly() {

	ESQueryMapper mapper = new ESQueryMapper();

	// SharedContentType type = Mockito.mock(SharedContentType.class);
	//
	// String typeString = "typeString";
	//
	// Mockito.doReturn(typeString).when(type).getType();

	SharedContentQuery query = new SharedContentQuery();

	Long from = 1000000000L;
	query.setFrom(from);

	JSONObject json = mapper.mapToQuery(query);

	System.out.println(json);

	System.out.println(query.getTimeConstraints().get(0).getTimeAxis());

	Assert.assertEquals(0, json.getInt("from"));

	Assert.assertEquals(10, json.getInt("size"));

	Assert.assertFalse(json.getJSONObject("query").has("match_all"));

	Assert.assertTrue(json.getJSONObject("query").has("bool"));

	Assert.assertTrue(json.getJSONObject("query").getJSONObject("bool").has("must"));

	Assert.assertEquals(1, json.getJSONObject("query").getJSONObject("bool").getJSONArray("must").length());

	Assert.assertEquals((Long) from, (Long) json.getJSONObject("query").getJSONObject("bool").getJSONArray("must").getJSONObject(0)
		.getJSONObject("range").getJSONObject(query.getTimeConstraints().get(0).getTimeAxis()).getLong("gte"));

    }

    @Test
    public void testWithOneTimeAxisToOnly() {

	ESQueryMapper mapper = new ESQueryMapper();

	// SharedContentType type = Mockito.mock(SharedContentType.class);
	//
	// String typeString = "typeString";
	//
	// Mockito.doReturn(typeString).when(type).getType();

	SharedContentQuery query = new SharedContentQuery();

	Long to = 1000000000L;
	query.setTo(to);

	JSONObject json = mapper.mapToQuery(query);

	System.out.println(json);

	System.out.println(query.getTimeConstraints().get(0).getTimeAxis());

	Assert.assertEquals(0, json.getInt("from"));

	Assert.assertEquals(10, json.getInt("size"));

	Assert.assertFalse(json.getJSONObject("query").has("match_all"));

	Assert.assertTrue(json.getJSONObject("query").has("bool"));

	Assert.assertTrue(json.getJSONObject("query").getJSONObject("bool").has("must"));

	Assert.assertEquals(1, json.getJSONObject("query").getJSONObject("bool").getJSONArray("must").length());

	Assert.assertEquals((Long) to, (Long) json.getJSONObject("query").getJSONObject("bool").getJSONArray("must").getJSONObject(0)
		.getJSONObject("range").getJSONObject(query.getTimeConstraints().get(0).getTimeAxis()).getLong("lte"));

    }

    @Test
    public void testWithOneTimeAxisFromAndTo() {

	ESQueryMapper mapper = new ESQueryMapper();

	// SharedContentType type = Mockito.mock(SharedContentType.class);
	//
	// String typeString = "typeString";
	//
	// Mockito.doReturn(typeString).when(type).getType();

	SharedContentQuery query = new SharedContentQuery();

	Long to = 1000000000L;
	query.setTo(to);

	long from = 900000000L;
	query.setFrom(from);

	JSONObject json = mapper.mapToQuery(query);

	System.out.println(json);

	System.out.println(query.getTimeConstraints().get(0).getTimeAxis());

	Assert.assertEquals(0, json.getInt("from"));

	Assert.assertEquals(10, json.getInt("size"));

	Assert.assertFalse(json.getJSONObject("query").has("match_all"));

	Assert.assertTrue(json.getJSONObject("query").has("bool"));

	Assert.assertTrue(json.getJSONObject("query").getJSONObject("bool").has("must"));

	Assert.assertEquals(1, json.getJSONObject("query").getJSONObject("bool").getJSONArray("must").length());

	Assert.assertEquals((Long) to, (Long) json.getJSONObject("query").getJSONObject("bool").getJSONArray("must").getJSONObject(0)
		.getJSONObject("range").getJSONObject(query.getTimeConstraints().get(0).getTimeAxis()).getLong("lte"));

	Assert.assertEquals((Long) from, (Long) json.getJSONObject("query").getJSONObject("bool").getJSONArray("must").getJSONObject(0)
		.getJSONObject("range").getJSONObject(query.getTimeConstraints().get(0).getTimeAxis()).getLong("gte"));

    }

}