package eu.essi_lab.lib.geo;

import static org.junit.Assert.assertEquals;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class BBOXUtilsTest {

    private double delta = 0.00001;

    @Test
    public void testEast() {
	List<SimpleEntry<Double, Double>> list = new ArrayList<>();
	list.add(new SimpleEntry<>(-10., 10.));
	list.add(new SimpleEntry<>(10., 20.));
	SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> bbox = BBOXUtils.getBBOX(list);
	SimpleEntry<Double, Double> lowerCorner = bbox.getKey();
	SimpleEntry<Double, Double> upperCorner = bbox.getValue();
	double south = lowerCorner.getKey();
	double west = lowerCorner.getValue();
	double north = upperCorner.getKey();
	double east = upperCorner.getValue();
	assertEquals(-10.0, south, delta);
	assertEquals(10.0, west, delta);
	assertEquals(10.0, north, delta);
	assertEquals(20.0, east, delta);
    }
    
    @Test
    public void testWest() {
	List<SimpleEntry<Double, Double>> list = new ArrayList<>();
	list.add(new SimpleEntry<>(-10., -10.));
	list.add(new SimpleEntry<>(10., -20.));
	SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> bbox = BBOXUtils.getBBOX(list);
	SimpleEntry<Double, Double> lowerCorner = bbox.getKey();
	SimpleEntry<Double, Double> upperCorner = bbox.getValue();
	double south = lowerCorner.getKey();
	double west = lowerCorner.getValue();
	double north = upperCorner.getKey();
	double east = upperCorner.getValue();
	assertEquals(-10.0, south, delta);
	assertEquals(-20.0, west, delta);
	assertEquals(10.0, north, delta);
	assertEquals(-10.0, east, delta);
    }
    
    @Test
    public void testNormal() {
	List<SimpleEntry<Double, Double>> list = new ArrayList<>();
	list.add(new SimpleEntry<>(-10., -10.));
	list.add(new SimpleEntry<>(10., 20.));
	SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> bbox = BBOXUtils.getBBOX(list);
	SimpleEntry<Double, Double> lowerCorner = bbox.getKey();
	SimpleEntry<Double, Double> upperCorner = bbox.getValue();
	double south = lowerCorner.getKey();
	double west = lowerCorner.getValue();
	double north = upperCorner.getKey();
	double east = upperCorner.getValue();
	assertEquals(-10.0, south, delta);
	assertEquals(-10.0, west, delta);
	assertEquals(10.0, north, delta);
	assertEquals(20.0, east, delta);
    }
    
    @Test
    public void testCross() {
	List<SimpleEntry<Double, Double>> list = new ArrayList<>();
	list.add(new SimpleEntry<>(-10., -170.));
	list.add(new SimpleEntry<>(10., 170.));
	SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> bbox = BBOXUtils.getBBOX(list);
	SimpleEntry<Double, Double> lowerCorner = bbox.getKey();
	SimpleEntry<Double, Double> upperCorner = bbox.getValue();
	double south = lowerCorner.getKey();
	double west = lowerCorner.getValue();
	double north = upperCorner.getKey();
	double east = upperCorner.getValue();
	assertEquals(-10.0, south, delta);
	assertEquals(170.0, west, delta);
	assertEquals(10.0, north, delta);
	assertEquals(-170.0, east, delta);
    }

}
