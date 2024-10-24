package eu.essi_lab.model.resource;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.junit.Test;

public class DatasetTest {
    @Test
    public void testName() throws Exception {
	InputStream stream = DatasetTest.class.getClassLoader().getResourceAsStream("pangaea.xml");
	Dataset dataset = Dataset.create(stream);
	String language = dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getLanguages().next();
	assertEquals("eng", language);
    }
}
