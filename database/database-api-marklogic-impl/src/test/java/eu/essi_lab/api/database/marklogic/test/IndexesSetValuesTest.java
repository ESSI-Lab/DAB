package eu.essi_lab.api.database.marklogic.test;

import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

import org.junit.Test;

import eu.essi_lab.indexes.IndexedMetadataElements;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.model.index.IndexedMetadataElement;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;

public class IndexesSetValuesTest {

    @Test
    public void test() {

	InputStream stream = IndexesSetValuesTest.class.getClassLoader().getResourceAsStream("testMiMetadata.xml");

	try {

	    Dataset dataset = new Dataset();

	    CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	    MIMetadata miMetadata = new MIMetadata(stream);
	    coreMetadata.setMIMetadata(miMetadata);

	    dataset.getHarmonizedMetadata().setCoreMetadata(coreMetadata);

	    List<IndexedMetadataElement> indexes = IndexedMetadataElements.getIndexes();
	    for (IndexedMetadataElement index : indexes) {
		index.getValues().clear();
		index.defineValues(dataset);
		if (!index.getValues().isEmpty()) {
		    dataset.getIndexesMetadata().write(index);
		}
	    }

	    dataset.toStream(new ByteArrayOutputStream());
	    // dataset.toStream(System.out);

	} catch (Exception e) {

	    fail("Exception thrown");
	}

    }
}
