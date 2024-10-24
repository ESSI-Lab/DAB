package eu.essi_lab.model;

import java.io.InputStream;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.MetadataElement;

public class DatasetTest2 {

    @Test
    public void test() throws ClassCastException, JAXBException {

	InputStream stream = getClass().getClassLoader().getResourceAsStream("dataset5.xml");
	Dataset dataset = Dataset.create(stream);

	String privateId = dataset.getPrivateId();
	Assert.assertEquals("9cc7612b-dfd5-48b0-821d-acbbe6fe29a1", privateId);

	List<String> keywords = dataset.getIndexesMetadata().read(MetadataElement.KEYWORD);
	Assert.assertEquals(10, keywords.size());

	List<String> crs = dataset.getIndexesMetadata().read(MetadataElement.CRS_ID);
	Assert.assertEquals(2, crs.size());
    }
}
