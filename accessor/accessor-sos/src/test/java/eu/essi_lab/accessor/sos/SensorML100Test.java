/**
 * 
 */
package eu.essi_lab.accessor.sos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.opengis.geometry.BoundingBox;

import eu.essi_lab.accessor.sos._1_0_0.SOSObservedProperty;
import eu.essi_lab.accessor.sos._1_0_0.SOSSensorML;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;

/**
 * @author Fabrizio
 */
public class SensorML100Test {

    @Test
    public void test1() throws Exception {

	InputStream stream = getClass().getClassLoader().getResourceAsStream("SensorML100Example1.xml");

	SOSSensorML sosSensorML = new SOSSensorML(stream);

	List<SOSObservedProperty> observervedProperties = sosSensorML.getObservervedProperties();
	assertEquals(29, observervedProperties.size());

	assertTrue(observervedProperties.//
		stream().//
		filter(p -> p.getName().equals("As")).//
		filter(p -> p.getDefinition().equals("urn:ogc:def:parameter:x-igrac:1.0:Quality Measurement:As")).//
		filter(p -> p.getUom().equals("mg/L")).//
		findFirst().isPresent());

	assertTrue(observervedProperties.//
		stream().//
		filter(p -> p.getName().equals("Salinity")).//
		filter(p -> p.getDefinition().equals("urn:ogc:def:parameter:x-igrac:1.0:Quality Measurement:Salinity")).//
		filter(p -> p.getUom().equals("g/L")).//
		findFirst().isPresent());

	Optional<String> name = sosSensorML.getName();
	assertEquals("Test WHOS", name.get());

	Optional<String> country = sosSensorML.getCountry();
	assertEquals("Switzerland", country.get());

	Optional<Double> elevation = sosSensorML.getElevation();
	assertEquals(Double.valueOf(100.0), elevation.get());

	Optional<String> elevationUOM = sosSensorML.getElevationUOM();
	assertEquals("m", elevationUOM.get());

	Optional<String> licenseName = sosSensorML.getLicenseName();
	assertEquals("Attribution 4.0 International (CC BY 4.0)", licenseName.get());

	Optional<String> licenseDescription = sosSensorML.getLicenseDescription();
	assertTrue(licenseDescription.get().startsWith("You are free to:"));

	Optional<String> licenseSummary = sosSensorML.getLicenseSummary();
	assertEquals("This data was made available by WHOS test organisation under the CC BY 4.0 license.", licenseSummary.get());

	Optional<BoundingBox> location = sosSensorML.getLocation();

	Double minX = location.get().getMinX();
	assertEquals(Double.valueOf(6.1386108), minX);

	Double minY = location.get().getMinY();
	assertEquals(Double.valueOf(46.201791), minY);

	Double maxX = location.get().getMaxX();
	assertEquals(Double.valueOf(6.1386108), maxX);

	Double maxY = location.get().getMaxY();
	assertEquals(Double.valueOf(46.201791), maxY);

	Optional<String> restriction = sosSensorML.getRestriction();
	assertTrue(restriction.get().startsWith("exclusive right to"));

	Optional<String> sensorType = sosSensorML.getSensorType();
	assertEquals("measurement", sensorType.get());

	Optional<String> systemType = sosSensorML.getSystemType();
	assertEquals("insitu-fixed-point", systemType.get());

	Optional<TemporalExtent> temporalExtent = sosSensorML.getTemporalExtent();
	assertEquals("2022-02-14T14:15:00.000000Z", temporalExtent.get().getBeginPosition());
	assertEquals("2024-03-18T14:12:00.000000Z", temporalExtent.get().getEndPosition());

	Optional<String> uniqueIdentifier = sosSensorML.getUniqueIdentifier();
	assertEquals("WHOS test organisation-whostest123", uniqueIdentifier.get());
    }

    @Test
    public void test2() throws Exception {

	InputStream stream = getClass().getClassLoader().getResourceAsStream("SensorML100Example2.xml");

	SOSSensorML sosSensorML = new SOSSensorML(stream);

	List<SOSObservedProperty> observervedProperties = sosSensorML.getObservervedProperties();
	assertEquals(29, observervedProperties.size());

	assertTrue(observervedProperties.//
		stream().//
		filter(p -> p.getName().equals("As")).//
		filter(p -> p.getDefinition().equals("urn:ogc:def:parameter:x-igrac:1.0:Quality Measurement:As")).//
		filter(p -> p.getUom().equals("mg/L")).//
		findFirst().isPresent());

	assertTrue(observervedProperties.//
		stream().//
		filter(p -> p.getName().equals("Salinity")).//
		filter(p -> p.getDefinition().equals("urn:ogc:def:parameter:x-igrac:1.0:Quality Measurement:Salinity")).//
		filter(p -> p.getUom().equals("g/L")).//
		findFirst().isPresent());

	Optional<String> name = sosSensorML.getName();
	assertEquals("1000609756", name.get());

	Optional<String> country = sosSensorML.getCountry();
	assertEquals("United States", country.get());

	Optional<Double> elevation = sosSensorML.getElevation();
	assertTrue(elevation.isEmpty());

	Optional<String> elevationUOM = sosSensorML.getElevationUOM();
	assertTrue(elevationUOM.isEmpty());

	Optional<String> licenseName = sosSensorML.getLicenseName();
	assertTrue(licenseName.isEmpty());

	Optional<String> licenseDescription = sosSensorML.getLicenseDescription();
	assertTrue(licenseDescription.isEmpty());

	Optional<String> licenseSummary = sosSensorML.getLicenseSummary();
	assertTrue(licenseSummary.isEmpty());

	Optional<BoundingBox> location = sosSensorML.getLocation();

	Double minX = location.get().getMinX();
	assertEquals(Double.valueOf(-74.91317), minX);

	Double minY = location.get().getMinY();
	assertEquals(Double.valueOf(39.0009), minY);

	Double maxX = location.get().getMaxX();
	assertEquals(Double.valueOf(-74.91317), maxX);

	Double maxY = location.get().getMaxY();
	assertEquals(Double.valueOf(39.0009), maxY);

	Optional<String> restriction = sosSensorML.getRestriction();
	assertTrue(restriction.isEmpty());

	Optional<String> sensorType = sosSensorML.getSensorType();
	assertEquals("measurement", sensorType.get());

	Optional<String> systemType = sosSensorML.getSystemType();
	assertEquals("insitu-fixed-point", systemType.get());

	Optional<TemporalExtent> temporalExtent = sosSensorML.getTemporalExtent();
	assertEquals("2008-01-01T00:00:00.000000Z", temporalExtent.get().getBeginPosition());
	assertEquals("2018-01-01T00:00:00.000000Z", temporalExtent.get().getEndPosition());

	Optional<String> uniqueIdentifier = sosSensorML.getUniqueIdentifier();
	assertEquals("Provisional-1000609756", uniqueIdentifier.get());
    }
}
