package eu.essi_lab.model;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Dataset;

public class GSSourceTest {

    @Test
    public void test() throws GSException, JAXBException, UnsupportedEncodingException {

	GSSource source = new GSSource();

	Assert.assertTrue(source.getDeployment().isEmpty());

	source.addDeployment("whos");
	source.addDeployment("geoss");

	Assert.assertEquals(2, source.getDeployment().size());

	Dataset dataset = new Dataset();
	dataset.setSource(source);

	InputStream stream = dataset.asStream();
	Dataset dataset2 = Dataset.create(stream);

	GSSource source2 = dataset2.getSource();
	Assert.assertEquals(2, source2.getDeployment().size());
    }
}
