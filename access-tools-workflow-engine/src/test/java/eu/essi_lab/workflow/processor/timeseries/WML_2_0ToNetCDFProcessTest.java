package eu.essi_lab.workflow.processor.timeseries;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ucar.ma2.Array;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.ft.FeatureDatasetPoint;
import ucar.nc2.ft.PointFeature;
import ucar.nc2.ft.StationTimeSeriesFeatureCollection;
import ucar.nc2.ft.point.StationPointFeature;
import ucar.nc2.time.CalendarDateRange;
import ucar.nc2.units.DateRange;
import ucar.unidata.geoloc.Station;

public class WML_2_0ToNetCDFProcessTest {

    private WML20_To_NetCDF_Processor process;

    @Before
    public void init() {
	this.process = new WML20_To_NetCDF_Processor();
    }

    // @Test
    // public void testWML2NetCDFEmpty() throws Exception {
    // InputStream stream = WML_2_0ToNetCDFProcessTest.class.getClassLoader().getResourceAsStream("wml_1_1-empty.xml");
    // InputStream output = process.convert(stream);
    // File tmp = File.createTempFile("test", ".nc");
    // FileOutputStream fos = new FileOutputStream(tmp);
    // IOUtils.copy(output, fos);
    // fos.close();
    // FeatureDataset featureDataset = FeatureDatasetFactoryManager.open(FeatureType.STATION, tmp.getAbsolutePath(),
    // null, null);
    // int variables = assertGeneralProperties(featureDataset, false);
    //
    // Assert.assertEquals(15, variables); // data variable and ancillary variables are not present in this case
    //
    // FeatureDatasetPoint fdp = (FeatureDatasetPoint) featureDataset;
    // StationTimeSeriesFeatureCollection fc = (StationTimeSeriesFeatureCollection)
    // fdp.getPointFeatureCollectionList().get(0);
    //
    // Assert.assertEquals(1, fc.getStations().size());
    //
    // Station station = fc.getStations().get(0);
    //
    // Assert.assertEquals(1345, station.getAltitude(), 10 ^ -10);
    // Assert.assertEquals(-111.946402, station.getLongitude(), 10 ^ -10);
    // Assert.assertEquals(41.718473, station.getLatitude(), 10 ^ -10);
    // Assert.assertEquals("Little Bear River at Mendon Road near Mendon, Utah", station.getName());
    //
    // int total = 0;
    // Long start = null;
    // Long end = null;
    // ucar.nc2.ft.PointFeatureCollection pfc = fc.flatten(null, (DateRange) null); // LOOK
    //
    // while (pfc.hasNext()) {
    // PointFeature pf = pfc.next();
    // StationPointFeature spf = (StationPointFeature) pf;
    // long time = spf.getNominalTimeAsDate().getTime();
    // if (start == null || time < start) {
    // start = time;
    // }
    // if (end == null || time > end) {
    // end = time;
    // }
    // total++;
    // }
    // System.out.println(total + " start " + start + " end " + end);
    // Assert.assertNull(start);
    // Assert.assertNull(end);
    // Assert.assertEquals(0, total);
    //
    // tmp.delete();
    //
    // }

    @Test
    public void testWML2NetCDF() throws Exception {
	InputStream stream = WML_2_0ToNetCDFProcessTest.class.getClassLoader().getResourceAsStream("wml_2_0-his4values.xml");
	InputStream output = process.convert(stream);
	File tmp = File.createTempFile("test", ".nc");
	FileOutputStream fos = new FileOutputStream(tmp);
	IOUtils.copy(output, fos);
	fos.close();
	FeatureDataset featureDataset = FeatureDatasetFactoryManager.open(FeatureType.STATION, tmp.getAbsolutePath(), null, null);
	int variables = assertGeneralProperties(featureDataset, true);

	Assert.assertEquals(5, variables);

	FeatureDatasetPoint fdp = (FeatureDatasetPoint) featureDataset;
	StationTimeSeriesFeatureCollection fc = (StationTimeSeriesFeatureCollection) fdp.getPointFeatureCollectionList().get(0);

	Assert.assertEquals(1, fc.getStationFeatures().size());

	int total = 0;
	Long start = null;
	Long end = null;
	ucar.nc2.ft.PointFeatureCollection pfc = fc.flatten(null, (CalendarDateRange) null); // LOOK

	while (pfc.hasNext()) {
	    PointFeature pf = pfc.next();
	    StationPointFeature spf = (StationPointFeature) pf;
	    long time = spf.getNominalTimeAsCalendarDate().getMillis();
	    if (start == null || time < start) {
		start = time;
	    }
	    if (end == null || time > end) {
		end = time;
	    }
	    total++;
	}
	System.out.println(total + " start " + start + " end " + end);
	Assert.assertEquals(1123180200000l, (long) start);
	Assert.assertEquals(1123266600000l, (long) end);
	Assert.assertEquals(49, total);

	tmp.delete();

    }

    private int assertGeneralProperties(FeatureDataset featureDataset, boolean assertValues) throws IOException {
	if (featureDataset instanceof FeatureDatasetPoint) {
	    FeatureDatasetPoint fdp = (FeatureDatasetPoint) featureDataset;
	    StationTimeSeriesFeatureCollection fc = (StationTimeSeriesFeatureCollection) fdp.getPointFeatureCollectionList().get(0);

	    Assert.assertEquals(1, fc.getStationFeatures().size());

	    Station station = fc.getStationFeatures().get(0);

	    // Assert.assertEquals(1345, station.getAltitude(), 10 ^ -10);
	    Assert.assertEquals(-111.946402, station.getLongitude(), 10 ^ -10);
	    Assert.assertEquals(41.718473, station.getLatitude(), 10 ^ -10);
	    Assert.assertEquals("Little Bear River at Mendon Road near Mendon, Utah", station.getName());

	    List<Variable> variables = featureDataset.getNetcdfFile().getVariables();

	    for (Variable variable : variables) {
		System.out.println("variable: " + variable.getShortName());
	    }

	    for (Variable variable : variables) {
		String name = variable.getShortName();
		System.out.println(name);
		int[] shape = variable.getShape();
		if (variable.getDataType().equals(ucar.ma2.DataType.CHAR)) {
		    if (shape.length == 1) {
			String values = variable.readScalarString();
			System.out.println("VAL (S) ->" + values);
		    } else {
			System.out.println("Shape: " + shape.length);
		    }
		} else if (variable.getDataType().equals(ucar.ma2.DataType.DOUBLE)) {
		    Array array = variable.read();
		    if (array.getSize() > 0) {
			double values = array.getDouble(0);
			System.out.println("VAL (D) ->" + values);
		    }
		} else if (variable.getDataType().equals(ucar.ma2.DataType.LONG)) {
		    Array array = variable.read();
		    if (array.getSize() > 0) {
			double values = array.getLong(0);
			System.out.println("VAL (L) ->" + values);
		    }
		} else {
		    System.out.println("Unexpected type: " + variable.getDataType());
		}
		String coordinates = "time lat lon alt station_name county state site_comments posaccuracy_m site_type";
		switch (name) {
		case "county":
		    assertAttribute(variable, "long_name", "County");
		    assertValue(variable, "Cache");
		    break;
		case "state":
		    assertAttribute(variable, "long_name", "State");
		    assertValue(variable, "Utah");
		    break;
		case "site_comments":
		    assertAttribute(variable, "long_name", "Site Comments");
		    assertValue(variable, "Located below county road bridge at Mendon Road crossing");
		    break;
		case "posaccuracy_m":
		    assertAttribute(variable, "long_name", "PosAccuracy_m");
		    assertValue(variable, "10");
		    break;
		case "site_type":
		    assertAttribute(variable, "long_name", "Site Type");
		    assertValue(variable, "Stream");
		    break;
		case "battery_voltage":
		    assertAttribute(variable, "long_name", "Battery voltage");
		    if (assertValues)
			assertValue(variable, 13.24467);
//		    assertAttribute(variable, "coordinates", coordinates);
		    // assertAttribute(variable, "_FillValue", "");
//		    assertAttribute(variable, "wml_value_type", "Field Observation");
//		    assertAttribute(variable, "wml_data_type", "Minimum");
//		    assertAttribute(variable, "wml_general_category", "Instrumentation");
//		    assertAttribute(variable, "wml_sample_medium", "Not Relevant");
		    assertAttribute(variable, "wml_unit_name", "volts");
//		    assertAttribute(variable, "wml_unit_type", "Potential Difference");
		    assertAttribute(variable, "wml_unit_abbreviation", "168");
//		    assertAttribute(variable, "wml_unit_code", "");
//		    assertAttribute(variable, "wml_speciation", "Not Applicable");
		    // assertAttribute(variable, "ancillary_variables",
		    // "battery_voltage_censor_code battery_voltage_method_code battery_voltage_quality_control_level_code battery_voltage_source_code");
		    // assertAttribute(variable, "", "");
		    break;
		case "battery_voltage_censor_code":
		    assertAttribute(variable, "long_name", "Censor code");
		    assertAttribute(variable, "coordinates", coordinates);
		    if (assertValues) {
			assertAttribute(variable, "flag_values", "nc");
			assertAttribute(variable, "flag_meanings", "not_censored");
			assertAttribute(variable, "flag_descriptions", "not censored");
		    }
		    break;
		case "battery_voltage_method_code":
		    assertAttribute(variable, "long_name", "Method code");
		    // assertValue(variable, "");
		    assertAttribute(variable, "coordinates", coordinates);
		    if (assertValues) {
			assertAttribute(variable, "flag_values", "4");
			assertAttribute(variable, "flag_meanings", "battery_voltage_measured_by_campbell_scientific_cr206_datalogger.");
			assertAttribute(variable, "flag_descriptions", "Battery voltage measured by Campbell Scientific CR206 datalogger.");
			assertAttribute(variable, "flag_links", "http://www.campbellsci.com");
		    }
		    break;
		case "battery_voltage_quality_control_level_code":
		    assertAttribute(variable, "long_name", "Quality control level code");
		    // assertValue(variable, "");
		    assertAttribute(variable, "coordinates", coordinates);
		    if (assertValues) {
			assertAttribute(variable, "flag_values", "0");
			assertAttribute(variable, "flag_meanings", "raw_data");
			assertAttribute(variable, "flag_descriptions", "Raw data");
			assertAttribute(variable, "flag_long_descriptions", "Raw data");
		    }
		    break;
		case "battery_voltage_source_code":
		    assertAttribute(variable, "long_name", "Source code");
		    // assertValue(variable, "");
		    assertAttribute(variable, "coordinates", coordinates);
		    if (assertValues) {
			assertAttribute(variable, "flag_values", "1");
			assertAttribute(variable, "flag_meanings", "utah_state_university_utah_water_research_laboratory");
			assertAttribute(variable, "flag_descriptions", "Utah State University Utah Water Research Laboratory");
			assertAttribute(variable, "flag_long_descriptions",
				"Continuous water quality monitoring by Utah State University as part of the USDA CEAP Grant");
			assertAttribute(variable, "flag_links", "[http://littlebearriver.usu.edu];");
		    }
		    // assertAttribute(variable, "", "");
		    break;
		case "lon":
		    assertAttribute(variable, "long_name", "station longitude");
		    assertValue(variable, -111.946402);
		    assertAttribute(variable, "standard_name", "longitude");
		    assertAttribute(variable, "units", "degrees_east");
		    assertAttribute(variable, "_CoordinateAxisType", "Lon");
		    // assertAttribute(variable, "", "");
		    // assertAttribute(variable, "", "");
		    // assertAttribute(variable, "", "");
		    // assertAttribute(variable, "", "");
		    break;
		case "lat":
		    assertAttribute(variable, "long_name", "station latitude");
		    assertValue(variable, 41.718473);
		    assertAttribute(variable, "standard_name", "latitude");
		    assertAttribute(variable, "units", "degrees_north");
		    assertAttribute(variable, "_CoordinateAxisType", "Lat");
		    // assertAttribute(variable, "", "");
		    // assertAttribute(variable, "", "");
		    // assertAttribute(variable, "", "");
		    // assertAttribute(variable, "", "");
		    break;
		case "alt":
		    assertAttribute(variable, "long_name", "vertical distance above the surface");
		    assertValue(variable, 1345.0);
		    assertAttribute(variable, "standard_name", "height");
		    assertAttribute(variable, "vertical_datum", "NGVD29");
		    assertAttribute(variable, "units", "m");
		    assertAttribute(variable, "positive", "up");
		    assertAttribute(variable, "axis", "Z");
		    assertAttribute(variable, "_CoordinateAxisType", "Height");
		    assertAttribute(variable, "_CoordinateZisPositive", "up");
		    break;
		case "station_name":
		    assertAttribute(variable, "long_name", "station name");
		    assertValue(variable, "Little Bear River at Mendon Road near Mendon, Utah");
		    assertAttribute(variable, "cf_role", "timeseries_id");
		    // assertAttribute(variable, "", "");
		    // assertAttribute(variable, "", "");
		    // assertAttribute(variable, "", "");
		    // assertAttribute(variable, "", "");
		    // assertAttribute(variable, "", "");
		    // assertAttribute(variable, "", "");
		    break;
		case "time":
		    assertAttribute(variable, "long_name", "time of measurement");
		    if (assertValues)
			assertValue(variable, 1.1231802E12);
		    assertAttribute(variable, "standard_name", "time");
		    assertAttribute(variable, "units", "milliseconds since 1970-01-01 00:00:00");
//		    assertAttribute(variable, "wml_time_scale_is_regular", "true");
//		    assertAttribute(variable, "wml_time_scale_unit_name", "minute");
//		    assertAttribute(variable, "wml_time_scale_unit_type", "Time");
//		    assertAttribute(variable, "wml_time_scale_unit_abbreviation", "min");
//		    assertAttribute(variable, "wml_time_scale_unit_code", "102");
//		    assertAttribute(variable, "wml_cuahsi_time_scale_time_support", "30.0");
		    assertAttribute(variable, "calendar", "gregorian");
		    assertAttribute(variable, "_CoordinateAxisType", "Time");
		    // assertAttribute(variable, "", "");
		    // assertAttribute(variable, "", "");
		    break;
		// case "":
		// assertAttribute(variable, "long_name", "");
		// assertValue(variable, "");
		// assertAttribute(variable, "", "");
		// assertAttribute(variable, "", "");
		// assertAttribute(variable, "", "");
		// assertAttribute(variable, "", "");
		// assertAttribute(variable, "", "");
		// assertAttribute(variable, "", "");
		// assertAttribute(variable, "", "");
		// break;

		default:
		    break;
		}

		// System.out.println(variable.getShortName());
		List<Attribute> attributes = variable.getAttributes();
		for (Attribute attribute : attributes) {
		    System.out.println("ATTR: " + attribute.getShortName() + " " + attribute.getStringValue());
		}
		System.out.println();
	    }

	    return variables.size();

	} else {
	    Assert.fail();
	}

	return 0;

    }

    private void assertValue(Variable variable, String expected) throws IOException {
	String actual = variable.readScalarString();
	Assert.assertEquals(expected, actual);

    }

    private void assertValue(Variable variable, Double expected) throws IOException {
	Double actual = variable.read().getDouble(0);
	Assert.assertEquals(expected, actual, Math.pow(10, -10));

    }

    private void assertAttribute(Variable variable, String attributeName, String attributeValue) {
	Attribute attr = variable.findAttribute(attributeName);
	if (attr == null) {
	    System.err.println("Attribute not found: " + attributeName);
	}
	String value = attr.getStringValue();
	Assert.assertEquals(attributeValue, value);

    }
}
