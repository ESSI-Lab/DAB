package eu.essi_lab.shared.driver.es.connector.aws;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author ilsanto
 */
public class AWSESUrlParserTest {

    @Test
    public void test() {

	String url = "http://example.com";

	Assert.assertFalse(new AWSESUrlParser(url).isAWSESEndpoint());

    }

    @Test
    public void test2() {

	String url = "https://search-esdablog-no37oojoxwuimt6nd7m6aao4v4.us-east-1.es.amazonaws.com";

	Assert.assertTrue(new AWSESUrlParser(url).isAWSESEndpoint());
    }

    @Test
    public void test2_1() {

	String url = "https://search-es-essi-v79-6t3rrqu54enphpeqbzxmwkbtsu.us-east-1.es.amazonaws.com";

	Assert.assertTrue(new AWSESUrlParser(url).isAWSESEndpoint());
    }

    @Test
    public void test3() {

	String url = "http://example.:::com";

	Assert.assertFalse(new AWSESUrlParser(url).isAWSESEndpoint());
    }

    @Test
    public void test4() {

	String url = "https://search-esdablog-no37oojoxwuimt6nd7m6aao4v4.us-east-1.es.amazonaws.com";

	Assert.assertEquals("esdablog", new AWSESUrlParser(url).getDomainName());
    }

    @Test
    public void test4_1() {

	String url = "https://search-es-essi-v79-6t3rrqu54enphpeqbzxmwkbtsu.us-east-1.es.amazonaws.com";

	Assert.assertEquals("es-essi-v79", new AWSESUrlParser(url).getDomainName());
    }

    @Test
    public void test5() {

	String url = "https://search-esdablog-no37oojoxwuimt6nd7m6aao4v4.us-east-1.es.amazonaws.com";

	Assert.assertEquals("us-east-1", new AWSESUrlParser(url).getRegion());

    }

    @Test
    public void test6() {

	String url = "https://search-es-essi-v79-6t3rrqu54enphpeqbzxmwkbtsu.us-east-1.es.amazonaws.com";

	Assert.assertEquals("us-east-1", new AWSESUrlParser(url).getRegion());
    }
}