package eu.essi_lab.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.model.index.jaxb.BoundingBox;
import eu.essi_lab.model.index.jaxb.CardinalValues;
import eu.essi_lab.model.index.jaxb.DisjointValues;

public class BoundingBoxIndexMetadataTest {

    @Test
    public void test() {

	BoundingBox metadata = new BoundingBox();

	Assert.assertNull(metadata.getArea());
	Assert.assertNull(metadata.getCenter());
	Assert.assertNull(metadata.getDisjointValues());
	Assert.assertNull(metadata.getNe());
	Assert.assertNull(metadata.getNw());
	Assert.assertNull(metadata.getNe());
	Assert.assertNull(metadata.getSe());
	Assert.assertNull(metadata.getSw());
	Assert.assertNotNull(metadata.getCardinalValues());
	Assert.assertTrue(metadata.getCardinalValues().isEmpty());

	test(metadata);
	
	 

	try {
	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    metadata.toStream(outputStream);

	    ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
	    BoundingBox create = BoundingBox.create(inputStream);
	    test(create);
	    
	    create.toStream(System.out);

	} catch (JAXBException e) {

	    e.printStackTrace();
	}
    }

    private void test(BoundingBox metadata) {

	metadata.setArea("area");
	metadata.setCenter("center");
	metadata.setNe("ne");
	metadata.setNw("nw");
	metadata.setSw("sw");
	metadata.setSe("se");

	Assert.assertEquals(metadata.getArea(), "area");
	Assert.assertEquals(metadata.getCenter(), "center");
	Assert.assertEquals(metadata.getNe(), "ne");
	Assert.assertEquals(metadata.getNw(), "nw");
	Assert.assertEquals(metadata.getSw(), "sw");
	Assert.assertEquals(metadata.getSe(), "se");

	CardinalValues valuesMetadata = new CardinalValues();

	Assert.assertNull(valuesMetadata.getEast());
	Assert.assertNull(valuesMetadata.getWest());
	Assert.assertNull(valuesMetadata.getNorth());
	Assert.assertNull(valuesMetadata.getSouth());

	valuesMetadata.setEast("east");
	valuesMetadata.setNorth("north");
	valuesMetadata.setSouth("south");
	valuesMetadata.setWest("west");

	Assert.assertEquals(valuesMetadata.getWest(), "west");
	Assert.assertEquals(valuesMetadata.getSouth(), "south");
	Assert.assertEquals(valuesMetadata.getEast(), "east");
	Assert.assertEquals(valuesMetadata.getNorth(), "north");

	metadata.addCardinalValues(valuesMetadata);

	valuesMetadata = metadata.getCardinalValues().get(0);
	Assert.assertEquals(valuesMetadata.getWest(), "west");
	Assert.assertEquals(valuesMetadata.getSouth(), "south");
	Assert.assertEquals(valuesMetadata.getEast(), "east");
	Assert.assertEquals(valuesMetadata.getNorth(), "north");

	DisjointValues disjointValuesMetadata = new DisjointValues();

	Assert.assertNull(disjointValuesMetadata.getDisjEast());
	Assert.assertNull(disjointValuesMetadata.getDisjWest());
	Assert.assertNull(disjointValuesMetadata.getDisjNorth());
	Assert.assertNull(disjointValuesMetadata.getDisjSouth());

	disjointValuesMetadata.setDisjEast("east");
	disjointValuesMetadata.setDisjNorth("north");
	disjointValuesMetadata.setDisjSouth("south");
	disjointValuesMetadata.setDisjWest("west");

	Assert.assertEquals(disjointValuesMetadata.getDisjWest(), "west");
	Assert.assertEquals(disjointValuesMetadata.getDisjSouth(), "south");
	Assert.assertEquals(disjointValuesMetadata.getDisjEast(), "east");
	Assert.assertEquals(disjointValuesMetadata.getDisjNorth(), "north");

	metadata.setDisjointValues(disjointValuesMetadata);
	disjointValuesMetadata = metadata.getDisjointValues();
	Assert.assertEquals(disjointValuesMetadata.getDisjWest(), "west");
	Assert.assertEquals(disjointValuesMetadata.getDisjSouth(), "south");
	Assert.assertEquals(disjointValuesMetadata.getDisjEast(), "east");
	Assert.assertEquals(disjointValuesMetadata.getDisjNorth(), "north");
    }
}
