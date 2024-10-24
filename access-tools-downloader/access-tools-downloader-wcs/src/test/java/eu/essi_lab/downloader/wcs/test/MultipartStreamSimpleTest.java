package eu.essi_lab.downloader.wcs.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.InputStream;

import org.junit.After;
import org.junit.Test;

import eu.essi_lab.downloader.wcs.MultipartStreamSimple;

public class MultipartStreamSimpleTest {

    private MultipartStreamSimple mss;

    private MultipartStreamSimple decode(InputStream stream) throws Exception {
	this.mss = new MultipartStreamSimple(stream, "wcs");
	return mss;
    }

    @Test
    public void test1() throws Exception {
	InputStream stream = MultipartStreamSimpleTest.class.getClassLoader().getResourceAsStream("multipart-test-lf.eml");
	MultipartStreamSimple mss = decode(stream);
	assertEquals(2, mss.getParts().size());		
	assertNotNull(mss.getParts().get("text/xml"));
	assertNotNull(mss.getParts().get("image/tiff"));
    }

    @Test
    public void test2() throws Exception {
	InputStream stream = MultipartStreamSimpleTest.class.getClassLoader().getResourceAsStream("multipart-test-crlf.eml");
	MultipartStreamSimple mss = decode(stream);
	assertEquals(2, mss.getParts().size());
	assertNotNull(mss.getParts().get("text/xml"));
	assertNotNull(mss.getParts().get("image/tiff"));
    }

    @After
    public void after() {
	if (mss != null) {
	    for (File file : mss.getParts().values()) {
		file.delete();
	    }
	}
    }
}
