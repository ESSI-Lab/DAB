package eu.essi_lab.model;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.model.index.IndexedElement;
import eu.essi_lab.model.index.IndexedMetadataElement;
import eu.essi_lab.model.index.jaxb.BoundingBox;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;

/**
 * @author Fabrizio
 */
public class IndexesSizeMethodTest {

    @Test
    public void test() {

	Dataset dataset = new Dataset();

	dataset.getIndexesMetadata().write(create("a"));
	dataset.getIndexesMetadata().write(create("b"));
	dataset.getIndexesMetadata().write(create("c"));
	dataset.getIndexesMetadata().write(create("d"));
	dataset.getIndexesMetadata().write(createWithBBOX());

	dataset.getIndexesMetadata().write(create("a"));
	dataset.getIndexesMetadata().write(create("b"));
	dataset.getIndexesMetadata().write(create("c"));
	dataset.getIndexesMetadata().write(create("d"));
	dataset.getIndexesMetadata().write(createWithBBOX());

	dataset.getIndexesMetadata().write(create("a"));
	dataset.getIndexesMetadata().write(create("b"));
	dataset.getIndexesMetadata().write(create("c"));
	dataset.getIndexesMetadata().write(create("d"));
	dataset.getIndexesMetadata().write(createWithBBOX());

	// 12 indexes plus 1 bbox (only 1 bbox can be set) 
	// and resourceProperty (added in the constructor)
	
	Assert.assertEquals(14, dataset.getIndexesMetadata().size());

    }

    private IndexedElement create(String name) {

	IndexedElement element = new IndexedElement(name);
	element.getValues().add("a");

	return element;
    }

    private IndexedElement createWithBBOX() {

	IndexedMetadataElement element = new IndexedMetadataElement(new BoundingBox()) {

	    @Override
	    public void defineValues(GSResource resource) {

		resource.getHarmonizedMetadata().getCoreMetadata().addBoundingBox(0, 0, 0, 0);
	    }
	};

	return element;
    }
}
