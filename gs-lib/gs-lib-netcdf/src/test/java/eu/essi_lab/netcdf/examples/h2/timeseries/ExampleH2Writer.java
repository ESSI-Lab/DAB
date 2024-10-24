package eu.essi_lab.netcdf.examples.h2.timeseries;

import java.io.IOException;

import eu.essi_lab.netcdf.NetCDFCFExampleWriter;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayDouble.D1;
import ucar.ma2.ArrayFloat;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

/**
 * H.2.1. Orthogonal multidimensional array representation of time series
 * Example H.2. Timeseries with common element times in a time coordinate variable using the orthogonal multidimensional
 * array representation.
 * 
 * @author boldrini
 */
public class ExampleH2Writer extends NetCDFCFExampleWriter {
    public static void main(String[] args) throws IOException {
	ExampleH2Writer ew = new ExampleH2Writer();
	ew.write("/tmp/exampleh2.nc");
	System.out.println("Wrote");
    }

    public void write(String location) throws IOException {

	NetcdfFileWriter writer = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf4, location, null);
	// global attributes
	writer.addGroupAttribute(null, new Attribute("featureType", "timeSeries"));
	writer.addGroupAttribute(null, new Attribute("Conventions", "CF-1.7"));

	int stationSize = 10;
	int timeSize = 20;
	// dimensions
	Dimension nameStrLen = writer.addDimension(null, "name_strlen", 80);
	writer.addDimension(null, "station", stationSize);
	Dimension timeDimension = writer.addDimension(null, "time", timeSize);
	timeDimension.setUnlimited(true);

	// independent variables

	Variable time = writer.addVariable(null, "time", DataType.DOUBLE, "time");
	time.addAttribute(new Attribute("long_name", "time of measurement"));
	time.addAttribute(new Attribute("standard_name", "time"));
	time.addAttribute(new Attribute("units", "days since 1970-01-01 00:00:00"));

	Variable lon = writer.addVariable(null, "lon", DataType.FLOAT, "station");
	lon.addAttribute(new Attribute("long_name", "station longitude"));
	lon.addAttribute(new Attribute("standard_name", "longitude"));
	lon.addAttribute(new Attribute("units", "degrees_east"));

	Variable lat = writer.addVariable(null, "lat", DataType.FLOAT, "station");
	lat.addAttribute(new Attribute("long_name", "station latitude"));
	lat.addAttribute(new Attribute("standard_name", "latitude"));
	lat.addAttribute(new Attribute("units", "degrees_north"));

	Variable alt = writer.addVariable(null, "alt", DataType.FLOAT, "station");
	alt.addAttribute(new Attribute("long_name", "vertical distance above the surface"));
	alt.addAttribute(new Attribute("standard_name", "height"));
	alt.addAttribute(new Attribute("units", "m"));
	alt.addAttribute(new Attribute("positive", "up"));
	alt.addAttribute(new Attribute("axis", "Z"));

	Variable stationNames = writer.addVariable(null, "station_name", DataType.CHAR, "station name_strlen");
	stationNames.addAttribute(new Attribute("long_name", "station name"));
	stationNames.addAttribute(new Attribute("cf_role", "timeseries_id"));

	// dependent variables
	Variable humidity = writer.addVariable(null, "humidity", DataType.FLOAT, "station time");
	humidity.addAttribute(new Attribute("standard_name", "specific_humidity"));
	humidity.addAttribute(new Attribute("coordinates", "lat lon alt station_name"));
	humidity.addAttribute(new Attribute("_FillValue", -999.9f));

	// create the file
	try {
	    writer.create();
	} catch (IOException e) {
	    System.err.printf("ERROR creating file %s%n%s", location, e.getMessage());
	}

	// Filling with values

	D1 timeDoubles = new ArrayDouble.D1(timeSize);
	for (int i = 0; i < timeSize; i++) {
	    timeDoubles.setDouble(i, i);
	}

	int[] stationNamesShape = stationNames.getShape();
	ucar.ma2.ArrayFloat.D1 altFloats = new ArrayFloat.D1(stationSize);
	ucar.ma2.ArrayFloat.D1 lonFloats = new ArrayFloat.D1(stationSize);
	ucar.ma2.ArrayFloat.D1 latFloats = new ArrayFloat.D1(stationSize);
	ArrayChar stationNamesArray = new ArrayChar.D2(stationNamesShape[0], stationNamesShape[1]);
	Index stationNamesArrayIndex = stationNamesArray.getIndex();

	ArrayFloat humidityFloats = new ArrayFloat.D2(stationSize, timeSize);
	Index hIndex = humidityFloats.getIndex();
	for (int i = 0; i < stationSize; i++) {
	    lonFloats.setFloat(i, (float) (Math.random() * 360.0 - 180.0));
	    latFloats.setFloat(i, (float) (Math.random() * 180.0 - 90.0));
	    altFloats.setFloat(i, (float) (Math.random() * 5.0));
	    stationNamesArray.setString(stationNamesArrayIndex.set(i), "station " + (i + 1));

	    for (int j = 0; j < timeSize; j++) {
		humidityFloats.setFloat(hIndex.set(i, j), (float) (i + Math.random()));
	    }
	}

	try {
	    writer.write(time, timeDoubles);
	    writer.write(lon, lonFloats);
	    writer.write(lat, latFloats);
	    writer.write(alt, altFloats);
	    writer.write(stationNames, stationNamesArray);
	    writer.write(humidity, humidityFloats);
	} catch (IOException e) {
	    e.printStackTrace();
	} catch (InvalidRangeException e) {
	    e.printStackTrace();
	}

	writer.close();

	//
    }

}
