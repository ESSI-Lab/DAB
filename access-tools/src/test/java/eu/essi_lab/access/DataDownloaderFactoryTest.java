package eu.essi_lab.access;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import eu.essi_lab.model.resource.Dataset;

public class DataDownloaderFactoryTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void init() {

    }

    @Test
    public void testFail() throws Exception {
	DataDownloader downloader = DataDownloaderFactory.getDataDownloader(new Dataset(), "38");
	Assert.assertNull(downloader);
    }

}
