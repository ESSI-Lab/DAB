package eu.essi_lab.netcdf.examples.grids.gridmappings;

import java.io.IOException;

import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

/**
 * Example 5.7. Lambert conformal projection
 * 
 * @author boldrini
 */
public class Example57Writer {
    public static void main(String[] args) throws IOException {
	Example57Writer ew = new Example57Writer();
	ew.write("/tmp/example57.nc");
	System.out.println("Wrote");
    }

    public void write(String location) throws IOException {

	NetcdfFileWriter writer = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf4, location, null);
	// global attributes
	writer.addGroupAttribute(null, new Attribute("Conventions", "CF-1.7"));

	int ySize = 228;
	float startY = 0;

	int xSize = 306;
	float startX = 0;

	int timeSize = 41;
	int startTime = 0;
	// dimensions
	writer.addDimension(null, "y", ySize);
	writer.addDimension(null, "x", xSize);
	writer.addDimension(null, "time", timeSize);

	Variable lc = writer.addVariable(null, "Lambert_Conformal", DataType.INT, "");
	lc.addAttribute(new Attribute("grid_mapping_name", "lambert_conformal_conic"));
	lc.addAttribute(new Attribute("standard_parallel", 25.0));
	// IT WAS LONGITUDE 265!! TODO: signal to Unidata
	lc.addAttribute(new Attribute("longitude_of_central_meridian", 118.0));
	lc.addAttribute(new Attribute("latitude_of_projection_origin", 25.0));

	// independent variables

	Variable time = writer.addVariable(null, "time", DataType.INT, "time");
	time.addAttribute(new Attribute("long_name", "forecast time"));
	time.addAttribute(new Attribute("units", "hours since 2004-06-23T22:00:00Z"));
	time.addAttribute(new Attribute("calendar", "standard"));

	Variable x = writer.addVariable(null, "x", DataType.FLOAT, "x");
	x.addAttribute(new Attribute("long_name", "x coordinate of projection"));
	x.addAttribute(new Attribute("standard_name", "projection_x_coordinate"));
	x.addAttribute(new Attribute("units", "km"));

	Variable y = writer.addVariable(null, "y", DataType.FLOAT, "y");
	y.addAttribute(new Attribute("long_name", "y coordinate of projection"));
	y.addAttribute(new Attribute("standard_name", "projection_y_coordinate"));
	y.addAttribute(new Attribute("units", "km"));

	// dependent variables
	Variable temperature = writer.addVariable(null, "Temperature", DataType.FLOAT, "time y x");
	temperature.addAttribute(new Attribute("long_name", "Temperature @ surface"));
	temperature.addAttribute(new Attribute("units", "K"));
	temperature.addAttribute(new Attribute("missing_value", 9999.0f));
	// THIS HAS BEEN COMMENTED TODO: implement
	// temperature.addAttribute(new Attribute("coordinates", "lat lon"));
	temperature.addAttribute(new Attribute("grid_mapping", "Lambert_Conformal"));

	// create the file
	try {
	    writer.create();
	} catch (IOException e) {
	    System.err.printf("ERROR creating file %s%n%s", location, e.getMessage());
	}

	// Filling with values

	ucar.ma2.ArrayInt.D1 timeDoubles = new ArrayInt.D1(timeSize,false);
	for (int i = 0; i < timeSize; i++) {
	    timeDoubles.setDouble(i, startTime + i);
	}

	ucar.ma2.ArrayFloat.D1 yFloats = new ArrayFloat.D1(ySize);
	for (int i = 0; i < ySize; i++) {
	    yFloats.setFloat(i, startY + i);
	}

	ucar.ma2.ArrayFloat.D1 xFloats = new ArrayFloat.D1(xSize);
	for (int i = 0; i < xSize; i++) {
	    xFloats.setFloat(i, startX + i);
	}

	ArrayFloat xwindFloats = new ArrayFloat.D3(timeSize, ySize, xSize);
	Index hIndex = xwindFloats.getIndex();
	for (int i = 0; i < timeSize; i++) {
	    for (int j2 = 0; j2 < ySize; j2++) {
		for (int k = 0; k < xSize; k++) {
		    hIndex.set(i, j2, k);
		    xwindFloats.setFloat(hIndex, (float) (Math.random() * 5.0));
		}
	    }
	}

	try {
	    writer.write(time, timeDoubles);
	    writer.write(x, xFloats);
	    writer.write(y, yFloats);
	    writer.write(temperature, xwindFloats);
	} catch (IOException e) {
	    e.printStackTrace();
	} catch (InvalidRangeException e) {
	    e.printStackTrace();
	}

	writer.close();

	//
    }

}
