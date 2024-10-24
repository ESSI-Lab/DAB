package eu.essi_lab.accessor.wof.client;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.junit.Test;

import eu.essi_lab.accessor.wof.client.datamodel.SitesResponseDocument;

public class CUAHSIDecoderInputStreamTest {

    @Test
    public void testFilter() throws Exception {
	InputStream getSitesObject = CUAHSIDecoderInputStreamTest.class.getClassLoader()
		.getResourceAsStream("filter-test/getSitesObject.xml");
	SitesResponseDocument srd = new SitesResponseDocument(getSitesObject);
	System.out.println(srd.getSitesInfo().size() + " sites");
	assertEquals(16, srd.getSitesInfo().size());
	
	InputStream getSites = CUAHSIDecoderInputStreamTest.class.getClassLoader().getResourceAsStream("filter-test/getSites.xml");
	CUAHSIDecoderInputStream decoder = new CUAHSIDecoderInputStream(getSites);
	srd = new SitesResponseDocument(decoder);
	System.out.println(srd.getSitesInfo().size() + " sites");
	assertEquals(16, srd.getSitesInfo().size());
    }

}
