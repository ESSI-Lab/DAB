package eu.essi_lab.netcdf.examples.grids.gridmappings;

import java.io.IOException;

import ucar.ma2.ArrayDouble;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

/**
 * Example 5.1. Independent coordinate variables
 * 
 * @author boldrini
 */
public class Example51Writer {
    public static void main(String[] args) throws IOException {
	Example51Writer ew = new Example51Writer();
	ew.write("/tmp/sample.nc");
	System.out.println("Wrote");
    }

    public void write(String location) throws IOException {
	write(location, 5000, 76, 80, 5000, 10, 40, 4, 32131, 42131);
    }

    public void write(String location, int latSize, double startLat, double endLat, int lonSize, double startLon, double endLon,
	    int timeSize, long startTime, long endTime) throws IOException {

	NetcdfFileWriter writer = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf4, location, null);
	// global attributes
	writer.addGroupAttribute(null, new Attribute("Conventions", "CF-1.7"));

	// dimensions
	writer.addDimension(null, "lat", latSize);
	writer.addDimension(null, "lon", lonSize);
	writer.addDimension(null, "time", timeSize);

	// independent variables

	Variable time = writer.addVariable(null, "time", DataType.DOUBLE, "time");
	time.addAttribute(new Attribute("long_name", "time"));
	time.addAttribute(new Attribute("units", "days since 1990-1-1 0:0:0"));

	Variable lon = writer.addVariable(null, "lon", DataType.DOUBLE, "lon");
	lon.addAttribute(new Attribute("long_name", "longitude"));
	lon.addAttribute(new Attribute("units", "degrees_east"));

	Variable lat = writer.addVariable(null, "lat", DataType.DOUBLE, "lat");
	lat.addAttribute(new Attribute("long_name", "latitude"));
	lat.addAttribute(new Attribute("units", "degrees_north"));

	// dependent variables
	Variable pressure = writer.addVariable(null, "pres", DataType.DOUBLE, "time lat lon");
	pressure.addAttribute(new Attribute("units", "hPa"));
	pressure.addAttribute(new Attribute("missing_value", 9999.0f));

	// create the file
	try {
	    writer.create();
	} catch (IOException e) {
	    System.err.printf("ERROR creating file %s%n%s", location, e.getMessage());
	}

	// Filling with values

	long timeStep = (endTime - startTime) / (timeSize - 1);
	double latStep = (endLat - startLat) / (latSize - 1);
	double lonStep = (endLon - startLon) / (lonSize - 1);

	ucar.ma2.ArrayDouble.D1 tValues = new ArrayDouble.D1(timeSize);
	for (int i = 0; i < timeSize; i++) {
	    tValues.setDouble(i, startTime + i * timeStep);
	}

	ucar.ma2.ArrayDouble.D1 yValues = new ArrayDouble.D1(latSize);
	for (int i = 0; i < latSize; i++) {
	    yValues.setDouble(i, startLat + i * latStep);
	}

	ucar.ma2.ArrayDouble.D1 xvalues = new ArrayDouble.D1(lonSize);
	for (int i = 0; i < lonSize; i++) {
	    xvalues.setDouble(i, startLon + i * lonStep);
	}

	ArrayDouble pressureDoubles = new ArrayDouble.D3(timeSize, latSize, lonSize);
	Index hIndex = pressureDoubles.getIndex();
	for (int i = 0; i < timeSize; i++) {
	    for (int j2 = 0; j2 < latSize; j2++) {
		for (int k = 0; k < lonSize; k++) {
		    hIndex.set(i, j2, k);
		    pressureDoubles.setDouble(hIndex, (Math.random() * 5.0));
		}
	    }
	}

	try {
	    writer.write(time, tValues);
	    writer.write(lon, xvalues);
	    writer.write(lat, yValues);
	    writer.write(pressure, pressureDoubles);
	} catch (IOException e) {
	    e.printStackTrace();
	} catch (InvalidRangeException e) {
	    e.printStackTrace();
	}

	writer.close();

	//
    }

}
