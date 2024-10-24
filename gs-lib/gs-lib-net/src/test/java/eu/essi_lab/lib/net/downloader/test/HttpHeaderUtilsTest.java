package eu.essi_lab.lib.net.downloader.test;

import static org.junit.Assert.assertEquals;

import java.net.http.HttpHeaders;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;

import eu.essi_lab.lib.net.downloader.HttpHeaderUtils;

/**
 * @author Fabrizio
 */
public class HttpHeaderUtilsTest {

    @Test
    public void emptyTest() {

	HttpHeaders headers = HttpHeaderUtils.buildEmpty();

	int size = headers.map().size();

	assertEquals(0, size);
    }

    @Test
    public void emptyTest2() {

	HttpHeaders headers = HttpHeaderUtils.buildEmpty();

	headers.map().put("name1", Arrays.asList("value1"));
	headers.map().put("name2", Arrays.asList("value2"));

	int size = headers.map().size();
	assertEquals(0, size);
    }

    @Test
    public void mapTest() {

	Map<String, String> headersMap = new HashMap<String, String>();
	headersMap.put("h1", "v1");
	headersMap.put("h2", "v2");
	headersMap.put("h3", "v3");

	HttpHeaders headers = HttpHeaderUtils.build(headersMap);

	int size = headers.map().size();

	assertEquals(3, size);

	assertEquals("v1", headers.firstValue("h1").get());
	assertEquals("v2", headers.firstValue("h2").get());
	assertEquals("v3", headers.firstValue("h3").get());
    }

    @Test
    public void toStringTest() {

	Map<String, String> headersMap = new HashMap<String, String>();
	headersMap.put("h1", "v1");
	headersMap.put("h2", "v2");
	headersMap.put("h3", "v3");

	HttpHeaders headers = HttpHeaderUtils.build(headersMap);

	String string = HttpHeaderUtils.toString(headers);
	assertEquals("h1=[v1], h2=[v2], h3=[v3]", string);
    }

    @Test
    public void simpleTest() {

	HttpHeaders headers = HttpHeaderUtils.build("h1", "v1");

	int size = headers.map().size();

	assertEquals(1, size);

	assertEquals("v1", headers.firstValue("h1").get());
    }

    @Test
    public void multiValueTest() {

	Map<String, List<String>> headersMap = new HashMap<>();
	headersMap.put("h1", Arrays.asList("a1", "b1", "c1"));
	headersMap.put("h2", Arrays.asList("a2", "b2", "c2"));
	headersMap.put("h3", Arrays.asList("a3", "b3", "c3"));

	HttpHeaders headers = HttpHeaderUtils.buildMultiValue(headersMap);

	int mapSize = headers.map().size();

	assertEquals(3, mapSize);

	List<String> allValues1 = headers.allValues("h1").stream().sorted().collect(Collectors.toList());
	assertEquals(3, allValues1.size());

	assertEquals("a1", allValues1.get(0));
	assertEquals("b1", allValues1.get(1));
	assertEquals("c1", allValues1.get(2));

	List<String> allValues2 = headers.allValues("h2").stream().sorted().collect(Collectors.toList());
	assertEquals(3, allValues2.size());

	assertEquals("a2", allValues2.get(0));
	assertEquals("b2", allValues2.get(1));
	assertEquals("c2", allValues2.get(2));

	List<String> allValues3 = headers.allValues("h3").stream().sorted().collect(Collectors.toList());
	assertEquals(3, allValues3.size());

	assertEquals("a3", allValues3.get(0));
	assertEquals("b3", allValues3.get(1));
	assertEquals("c3", allValues3.get(2));
    }

}
