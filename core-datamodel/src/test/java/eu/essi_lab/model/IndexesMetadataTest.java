package eu.essi_lab.model;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Optional;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.model.index.IndexedElement;
import eu.essi_lab.model.index.IndexedMetadataElement;
import eu.essi_lab.model.index.jaxb.BoundingBox;
import eu.essi_lab.model.index.jaxb.IndexesMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;

public class IndexesMetadataTest {

    @Test
    public void test() {

	IndexesMetadata indexesMetadata = new IndexesMetadata();

	Assert.assertFalse(indexesMetadata.readBoundingBox().isPresent());

	indexesMetadata.write(new IndexedElement(MetadataElement.ABSTRACT.getName(), "ABSTRACT"));

	Assert.assertEquals("ABSTRACT", indexesMetadata.read(MetadataElement.ABSTRACT).get(0));

	indexesMetadata.write(new IndexedElement(MetadataElement.ABSTRACT.getName(), "ABSTRACT2"));

	Assert.assertEquals("ABSTRACT2", indexesMetadata.read(MetadataElement.ABSTRACT).get(1));

	indexesMetadata.write(new IndexedElement(MetadataElement.TITLE.getName(), "TITLE"));

	Assert.assertEquals("TITLE", indexesMetadata.read(MetadataElement.TITLE).get(0));

	BoundingBox boxIndexMetadata = new BoundingBox();
	boxIndexMetadata.setArea("area");
	boxIndexMetadata.setCenter("center");
	boxIndexMetadata.setNe("ne");
	boxIndexMetadata.setNw("nw");
	boxIndexMetadata.setSw("sw");
	boxIndexMetadata.setSe("se");

	indexesMetadata.write(new IndexedMetadataElement(boxIndexMetadata) {
	    @Override
	    public void defineValues(GSResource resource) {
	    }
	});

	Optional<BoundingBox> bbox = indexesMetadata.readBoundingBox();
	Assert.assertEquals("center", bbox.get().getCenter());

	try {

	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    indexesMetadata.toStream(outputStream);
	    // System.out.println(outputStream);

	    ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
	    IndexesMetadata indexes = IndexesMetadata.create(inputStream);

	    Assert.assertEquals(indexes.read(MetadataElement.ABSTRACT).get(0), "ABSTRACT");
	    Assert.assertEquals(indexes.read(MetadataElement.ABSTRACT).get(1), "ABSTRACT2");

	    Assert.assertEquals(indexes.read(MetadataElement.TITLE).get(0), "TITLE");
	    
	    Assert.assertTrue(indexes.hasBoundingBox());

	    bbox = indexes.readBoundingBox();
	    Assert.assertEquals("center", bbox.get().getCenter());

	} catch (JAXBException e) {

	    e.printStackTrace();
	    fail("Exception thrown");
	}

    }
}
