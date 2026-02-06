package eu.essi_lab.cfga.source.test;

import eu.essi_lab.cfga.source.*;
import org.junit.*;

import static org.junit.Assert.*;

public class S3SourceTest {

    @Test
    public void testWithoutEndpoint() throws Exception {

	S3Source source = S3Source.of("s3://user:pwd@bucket/config.json");

	assertEquals("user", source.getWrapper().getAccessKey());
	assertEquals("pwd", source.getWrapper().getSecretKey());
	assertEquals("bucket", source.getBucketName());
	assertEquals("config.json", source.getConfigName());

	Assert.assertFalse(source.getWrapper().getEndpoint().isPresent());
    }

    @Test
    public void testWithHttpsEndpoint() throws Exception {

	S3Source source = S3Source.of("s3://user:pwd@https:endpoint/bucket/config.json");

	assertEquals("user", source.getWrapper().getAccessKey());
	assertEquals("pwd", source.getWrapper().getSecretKey());
	assertEquals("bucket", source.getBucketName());
	assertEquals("config.json", source.getConfigName());

	Assert.assertEquals("https://endpoint", source.getWrapper().getEndpoint().get());
    }

    @Test
    public void testWithHttpEndpoint() throws Exception {

	S3Source source = S3Source.of("s3://user:pwd@http:endpoint/bucket/config.json");

	assertEquals("user", source.getWrapper().getAccessKey());
	assertEquals("pwd", source.getWrapper().getSecretKey());
	assertEquals("bucket", source.getBucketName());
	assertEquals("config.json", source.getConfigName());

	Assert.assertEquals("http://endpoint", source.getWrapper().getEndpoint().get());
    }

    @Test
    public void testWithHttpsAndPortEndpoint() throws Exception {

	S3Source source = S3Source.of("s3://user:pwd@https:endpoint:9000/bucket/config.json");

	assertEquals("user", source.getWrapper().getAccessKey());
	assertEquals("pwd", source.getWrapper().getSecretKey());
	assertEquals("bucket", source.getBucketName());
	assertEquals("config.json", source.getConfigName());

	Assert.assertEquals("https://endpoint:9000", source.getWrapper().getEndpoint().get());
    }

    @Test
    public void testWithHttpAndPortEndpoint() throws Exception {

	S3Source source = S3Source.of("s3://user:pwd@http:endpoint:9000/bucket/config.json");

	assertEquals("user", source.getWrapper().getAccessKey());
	assertEquals("pwd", source.getWrapper().getSecretKey());
	assertEquals("bucket", source.getBucketName());
	assertEquals("config.json", source.getConfigName());

	Assert.assertEquals("http://endpoint:9000", source.getWrapper().getEndpoint().get());
    }
}
