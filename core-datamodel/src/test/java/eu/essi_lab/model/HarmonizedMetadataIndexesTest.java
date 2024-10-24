package eu.essi_lab.model;

import static org.junit.Assert.fail;

import java.util.List;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.junit.Test;

import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.xml.NameSpace;
import eu.essi_lab.model.index.IndexedElement;
import eu.essi_lab.model.index.IndexedMetadataElement;
import eu.essi_lab.model.index.jaxb.BoundingBox;
import eu.essi_lab.model.index.jaxb.CardinalValues;
import eu.essi_lab.model.index.jaxb.DisjointValues;
import eu.essi_lab.model.index.jaxb.IndexesMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.HarmonizedMetadata;
import eu.essi_lab.model.resource.MetadataElement;

public class HarmonizedMetadataIndexesTest {

    @Test
    public void test() {

	Dataset ds = new Dataset();
	IndexesMetadata indexesMetadata = ds.getIndexesMetadata();

	BoundingBox boxIndexMetadata = new BoundingBox();
	boxIndexMetadata.setArea("area");
	boxIndexMetadata.setCenter("center");
	boxIndexMetadata.setNe("ne");
	boxIndexMetadata.setNw("nw");
	boxIndexMetadata.setSw("sw");
	boxIndexMetadata.setSe("se");

	CardinalValues valuesMetadata = new CardinalValues();
	valuesMetadata.setEast("east");
	valuesMetadata.setWest("west");
	valuesMetadata.setNorth("north");
	valuesMetadata.setSouth("south");
	boxIndexMetadata.addCardinalValues(valuesMetadata);

	DisjointValues disjointValuesMetadata = new DisjointValues();
	disjointValuesMetadata.setDisjEast("disjEast");
	disjointValuesMetadata.setDisjWest("disjWest");
	disjointValuesMetadata.setDisjNorth("disjNorth");
	disjointValuesMetadata.setDisjSouth("disjSouth");
	boxIndexMetadata.setDisjointValues(disjointValuesMetadata);

	indexesMetadata.write(new IndexedMetadataElement(boxIndexMetadata) {
	    @Override
	    public void defineValues(GSResource resource) {
	    }
	});

	List<MetadataElement> elements = MetadataElement.getOrderedElements();

	for (MetadataElement element : elements) {
	    indexesMetadata.write(new IndexedElement(element.getName(), UUID.randomUUID().toString()));
	}
	for (MetadataElement element : elements) {
	    indexesMetadata.write(new IndexedElement(element.getName(), UUID.randomUUID().toString()));
	}
	for (MetadataElement element : elements) {
	    indexesMetadata.write(new IndexedElement(element.getName(), UUID.randomUUID().toString()));
	}
	
	indexesMetadata.write(new IndexedElement(MetadataElement.ABSTRACT.getName(), "xxxx"));

	try {
	    JAXBContext context = JAXBContext.newInstance(HarmonizedMetadata.class);
	    Marshaller marshaller = context.createMarshaller();
	    marshaller.setProperty("jaxb.formatted.output", true);
	    marshaller.setProperty(NameSpace.NAMESPACE_PREFIX_MAPPER_IMPL, new CommonNameSpaceContext());

	    // marshaller.marshal(hm, System.out);

	} catch (JAXBException e) {

	    fail("Exception thrown");
	}
    }
}
