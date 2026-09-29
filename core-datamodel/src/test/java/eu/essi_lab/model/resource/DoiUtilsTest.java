package eu.essi_lab.model.resource;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.model.GSSource;

public class DoiUtilsTest {

    @Test
    public void normalizeDoiTest() {

	Assert.assertEquals("10.1594/PANGAEA.66871", DoiUtils.normalizeDoi("10.1594/PANGAEA.66871").get());
	Assert.assertEquals("10.1594/PANGAEA.66871", DoiUtils.normalizeDoi("https://doi.org/10.1594/PANGAEA.66871").get());
	Assert.assertEquals("10.1594/PANGAEA.66871", DoiUtils.normalizeDoi("doi:10.1594/PANGAEA.66871").get());
	Assert.assertFalse(DoiUtils.normalizeDoi("not-a-doi").isPresent());
    }

    @Test
    public void extractDoiFromResourceTest() {

	Dataset dataset = new Dataset();
	dataset.setSource(new GSSource());
	dataset.getHarmonizedMetadata().getCoreMetadata().getDataIdentification()
		.setResourceIdentifier("https://doi.org/10.1594/PANGAEA.66871");

	Assert.assertEquals("10.1594/PANGAEA.66871", DoiUtils.extractDoi(dataset).get());
	Assert.assertEquals("10.1594/PANGAEA.66871", dataset.getDoi().get());
    }
}
