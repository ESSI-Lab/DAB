package eu.essi_lab.cfga.source;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class S3SourceTest {

    @Test
    public void test() throws Exception {
	S3Source s3Source = S3Source.of("s3://user:pwd@bucket/filename.ext");
	assertEquals("user", s3Source.getWrapper().getAccessKey());
	assertEquals("pwd", s3Source.getWrapper().getSecretKey());
	assertEquals("bucket", s3Source.getBucketName());
	assertEquals("filename.ext", s3Source.getConfigName());

    }

}
