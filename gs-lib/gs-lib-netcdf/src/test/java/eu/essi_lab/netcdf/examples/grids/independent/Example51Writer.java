package eu.essi_lab.netcdf.examples.grids.independent;

import java.io.IOException;

import eu.essi_lab.netcdf.NetCDFCFExampleWriter;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayDouble.D1;
import ucar.ma2.ArrayFloat;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

/**
 * 5.1. Independent Latitude, Longitude, Vertical, and Time Axes
 * Example 5.1. Independent coordinate variables
 * When each of a variable’s spatiotemporal dimensions is a latitude, longitude, vertical, or time dimension, then each
 * axis is identified by a coordinate variable.
 * 
 * @author boldrini
 */
public class Example51Writer extends NetCDFCFExampleWriter {
    public static void main(String[] args) throws IOException {
	Example51Writer ew = new Example51Writer();
	ew.write("/tmp/example51.nc");
	System.out.println("Wrote");
    }

    public void write(String location) throws IOException {

	NetcdfFileWriter writer = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf4, location, null);
	// global attributes
	writer.addGroupAttribute(null, new Attribute("Conventions", "CF-1.7"));

	int latSize = 18;
	float startLat = 10;
	int lonSize = 36;
	float startLon = 20;
	int presSize = 15;
	float startPres = 5;
	int timeSize = 4;
	int startTime = 0;
	// dimensions
	writer.addDimension(null, "lat", latSize);
	writer.addDimension(null, "lon", lonSize);
	writer.addDimension(null, "pres", presSize);
	writer.addDimension(null, "time", timeSize);

	// independent variables

	Variable time = writer.addVariable(null, "time", DataType.DOUBLE, "time");
	time.addAttribute(new Attribute("long_name", "time"));
	time.addAttribute(new Attribute("units", "days since 1990-01-01 0:0:0"));
	time.addAttribute(new Attribute("calendar", "standard"));

	Variable lon = writer.addVariable(null, "lon", DataType.FLOAT, "lon");
	lon.addAttribute(new Attribute("long_name", "longitude"));
	lon.addAttribute(new Attribute("units", "degrees_east"));

	Variable lat = writer.addVariable(null, "lat", DataType.FLOAT, "lat");
	lat.addAttribute(new Attribute("long_name", "latitude"));
	lat.addAttribute(new Attribute("units", "degrees_north"));

	Variable pres = writer.addVariable(null, "pres", DataType.FLOAT, "pres");
	pres.addAttribute(new Attribute("long_name", "pressure"));
	pres.addAttribute(new Attribute("units", "hPa"));

	// dependent variables
	Variable xwind = writer.addVariable(null, "xwind", DataType.FLOAT, "time pres lat lon");
	xwind.addAttribute(new Attribute("long_name", "zonal wind"));
	xwind.addAttribute(new Attribute("units", "m/s"));

	// create the file
	try {
	    writer.create();
	} catch (IOException e) {
	    System.err.printf("ERROR creating file %s%n%s", location, e.getMessage());
	}

	// Filling with values

	D1 timeDoubles = new ArrayDouble.D1(timeSize);
	for (int i = 0; i < timeSize; i++) {
	    timeDoubles.setDouble(i, startTime + i);
	}

	ucar.ma2.ArrayFloat.D1 latFloats = new ArrayFloat.D1(latSize);
	for (int i = 0; i < latSize; i++) {
	    latFloats.setFloat(i, startLat + i);
	}

	ucar.ma2.ArrayFloat.D1 lonFloats = new ArrayFloat.D1(lonSize);
	for (int i = 0; i < lonSize; i++) {
	    lonFloats.setFloat(i, startLon + i);
	}

	ucar.ma2.ArrayFloat.D1 presFloats = new ArrayFloat.D1(presSize);
	for (int i = 0; i < presSize; i++) {
	    presFloats.setFloat(i, startPres + i);
	}

	ArrayFloat xwindFloats = new ArrayFloat.D4(timeSize, presSize, latSize, lonSize);
	Index hIndex = xwindFloats.getIndex();
	for (int i = 0; i < timeSize; i++) {
	    for (int j = 0; j < presSize; j++) {
		for (int j2 = 0; j2 < latSize; j2++) {
		    for (int k = 0; k < lonSize; k++) {
			hIndex.set(i, j, j2, k);
			xwindFloats.setFloat(hIndex, (float) (Math.random() * 5.0));
		    }
		}
	    }
	}

	try {
	    writer.write(time, timeDoubles);
	    writer.write(pres, presFloats);
	    writer.write(lon, lonFloats);
	    writer.write(lat, latFloats);
	    writer.write(xwind, xwindFloats);
	} catch (IOException e) {
	    e.printStackTrace();
	} catch (InvalidRangeException e) {
	    e.printStackTrace();
	}

	writer.close();

	//
    }

}
