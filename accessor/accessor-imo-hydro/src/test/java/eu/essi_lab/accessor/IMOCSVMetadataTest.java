package eu.essi_lab.accessor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import eu.essi_lab.accessor.imo.IMOCSVMetadata;
import eu.essi_lab.accessor.imo.IMOStationMetadata;

public class IMOCSVMetadataTest {

    @Test
    public void test() throws Exception {
	InputStream stream = IMOCSVMetadataTest.class.getClassLoader().getResourceAsStream("test-data/arctic_hycos_grdc_metadata.csv");
	File tmp = File.createTempFile(IMOCSVMetadataTest.class.getSimpleName(), ".csv");
	tmp.deleteOnExit();
	FileOutputStream fos = new FileOutputStream(tmp);
	IOUtils.copy(stream, fos);
	stream.close();
	fos.close();
	IMOCSVMetadata doc = new IMOCSVMetadata(tmp);
	
	assertTrue(!doc.getStationMetadata().isEmpty());
	
	IMOStationMetadata metadata = doc.getStationMetadata().get("V102");
	
	assertEquals("V102", metadata.getStationId());
	assertEquals("65.62330", metadata.getLatitude());
	assertEquals("-16.1893", metadata.getLongitude());
	assertEquals("Iceland Meteorological Office", metadata.getInstitute());
	assertEquals("JOEKULSA A FJOELLUM", metadata.getRiver());
	assertEquals("JOEKULSA A FJOELLUM at GRIMSSTADIR", metadata.getStationName());	
	
	tmp.delete();
    }

}
