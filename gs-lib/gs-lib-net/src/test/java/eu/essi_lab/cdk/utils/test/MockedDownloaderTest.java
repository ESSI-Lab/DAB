package eu.essi_lab.cdk.utils.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import eu.essi_lab.lib.net.utils.MockedDownloader;
import eu.essi_lab.model.exceptions.GSException;

/**
 * A test for the mocked downloader.
 * 
 * @author boldrini
 */
public class MockedDownloaderTest {

    @Test
    public void test0() throws GSException {
	MockedDownloader downloader = new MockedDownloader();
	assertEquals("", downloader.downloadOptionalString(UUID.randomUUID().toString()).get()  );
    }

    @Test
    public void test1() throws GSException {
	MockedDownloader downloader = new MockedDownloader("content");
	assertEquals("content", downloader.downloadOptionalString(UUID.randomUUID().toString()).get());
    }

    @Test
    public void test2() throws GSException {
	MockedDownloader downloader = new MockedDownloader("url", "content");
	assertEquals("", downloader.downloadOptionalString(UUID.randomUUID().toString()).get());
	assertEquals("content", downloader.downloadOptionalString("url").get());
    }

    @Test
    public void test3() throws GSException {
	MockedDownloader downloader = new MockedDownloader("url", "content", "abc");
	assertEquals("", downloader.downloadOptionalString(UUID.randomUUID().toString()).get());
	assertEquals("content", downloader.downloadOptionalString("url").get());
    }

    @Test
    public void test4() throws GSException {
	MockedDownloader downloader = new MockedDownloader("url1", "content1", "url2", "content2");
	assertEquals("", downloader.downloadOptionalString(UUID.randomUUID().toString()).get());
	assertEquals("content1", downloader.downloadOptionalString("url1").get());
	assertEquals("content2", downloader.downloadOptionalString("url2").get());
    }

    @Test
    public void test5() throws GSException {
	MockedDownloader downloader = new MockedDownloader("url1", "content1", "url2", "content2", "abc");
	assertEquals("", downloader.downloadOptionalString(UUID.randomUUID().toString()).get());
	assertEquals("content1", downloader.downloadOptionalString("url1").get());
	assertEquals("content2", downloader.downloadOptionalString("url2").get());
    }

    @Test
    public void testStream() throws GSException, IOException {
	MockedDownloader downloader = new MockedDownloader("content");
	StringWriter writer = new StringWriter();
	IOUtils.copy(downloader.downloadOptionalStream("url1").get(), writer,StandardCharsets.UTF_8);
	String theString = writer.toString();
	assertEquals("content", theString);
    }

}
