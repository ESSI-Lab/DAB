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
 * Example 5.10. British National Grid
 * 
 * @author boldrini
 */
public class Example510Writer {
    public static void main(String[] args) throws IOException {
	Example510Writer ew = new Example510Writer();
	ew.write("/tmp/example510.nc");
	System.out.println("Wrote");
    }

    public void write(String location) throws IOException {

	NetcdfFileWriter writer = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf4, location, null);
	// global attributes
	writer.addGroupAttribute(null, new Attribute("Conventions", "CF-1.7"));

	int ySize = 100;
	float startY = 0;

	int xSize = 100;
	float startX = 0;

	int zSize = 2;
	int startZ = 0;
	// dimensions
	writer.addDimension(null, "y", ySize);
	writer.addDimension(null, "x", xSize);
	writer.addDimension(null, "z", zSize);

	Variable lc = writer.addVariable(null, "crsOSGB", DataType.INT, "");
	lc.addAttribute(new Attribute("grid_mapping_name", "transverse_mercator"));
	lc.addAttribute(new Attribute("semi_major_axis", 6377563.396));
	lc.addAttribute(new Attribute("inverse_flattening", 299.3249646));
	lc.addAttribute(new Attribute("longitude_of_prime_meridian", 0.0));
	lc.addAttribute(new Attribute("latitude_of_projection_origin", 49.0));
	lc.addAttribute(new Attribute("longitude_of_central_meridian", -2.0));
	lc.addAttribute(new Attribute("scale_factor_at_central_meridian", 0.9996012717));
	lc.addAttribute(new Attribute("false_easting", 400000.0));
	lc.addAttribute(new Attribute("false_northing", -100000.0));
	lc.addAttribute(new Attribute("unit", "metre"));

	// independent variables

	Variable z = writer.addVariable(null, "z", DataType.DOUBLE, "z");
	z.addAttribute(new Attribute("standard_name", "height_above_reference_ellipsoid"));
	z.addAttribute(new Attribute("long_name", "height_above_osgb_newlyn_datum_masl"));
	z.addAttribute(new Attribute("units", "m"));

	Variable x = writer.addVariable(null, "x", DataType.DOUBLE, "x");
	x.addAttribute(new Attribute("standard_name", "projection_x_coordinate"));
	x.addAttribute(new Attribute("long_name", "Easting"));
	x.addAttribute(new Attribute("units", "m"));

	Variable y = writer.addVariable(null, "y", DataType.DOUBLE, "y");
	y.addAttribute(new Attribute("standard_name", "projection_y_coordinate"));
	y.addAttribute(new Attribute("long_name", "Northing"));
	y.addAttribute(new Attribute("units", "m"));

	// dependent variables
	Variable pressure = writer.addVariable(null, "pres", DataType.DOUBLE, "z y x");
	pressure.addAttribute(new Attribute("standard_name", "air_pressure"));
	pressure.addAttribute(new Attribute("units", "Pa"));
	pressure.addAttribute(new Attribute("missing_value", 9999.0f));
	// THIS HAS BEEN COMMENTED TODO: implement
	// temperature.addAttribute(new Attribute("coordinates", "lat lon"));
	pressure.addAttribute(new Attribute("grid_mapping", "crsOSGB"));

	// create the file
	try {
	    writer.create();
	} catch (IOException e) {
	    System.err.printf("ERROR creating file %s%n%s", location, e.getMessage());
	}

	// Filling with values

	ucar.ma2.ArrayDouble.D1 zDoubles = new ArrayDouble.D1(zSize);
	for (int i = 0; i < zSize; i++) {
	    zDoubles.setDouble(i, startZ + i);
	}

	ucar.ma2.ArrayDouble.D1 yFloats = new ArrayDouble.D1(ySize);
	for (int i = 0; i < ySize; i++) {
	    yFloats.setDouble(i, startY + i);
	}

	ucar.ma2.ArrayDouble.D1 xFloats = new ArrayDouble.D1(xSize);
	for (int i = 0; i < xSize; i++) {
	    xFloats.setDouble(i, startX + i);
	}

	ArrayDouble pressureDoubles = new ArrayDouble.D3(zSize, ySize, xSize);
	Index hIndex = pressureDoubles.getIndex();
	for (int i = 0; i < zSize; i++) {
	    for (int j2 = 0; j2 < ySize; j2++) {
		for (int k = 0; k < xSize; k++) {
		    hIndex.set(i, j2, k);
		    pressureDoubles.setDouble(hIndex, (Math.random() * 5.0));
		}
	    }
	}

	try {
	    writer.write(z, zDoubles);
	    writer.write(x, xFloats);
	    writer.write(y, yFloats);
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
