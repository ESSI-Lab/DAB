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
 * Example Svalbard UTM writer for salzano
 * 
 * @author boldrini
 */
public class ExampleSvalbardUTMWriter  {
    public static void main(String[] args) throws IOException {
	ExampleSvalbardUTMWriter ew = new ExampleSvalbardUTMWriter();
	ew.write("/home/boldrini/test/svalbard.nc");
	System.out.println("Wrote");
    }

    public void write(String location) throws IOException {

	NetcdfFileWriter writer = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf4, location, null);
	// global attributes
	writer.addGroupAttribute(null, new Attribute("Conventions", "CF-1.7"));

	int ySize = 469;
	double startY = 8761225.1;
	double endY = 8765905.0;

	int xSize = 672;
	double startX = 430685.0;
	double endX = 437395.0;
	
//	int ySize = 469;
//	double startY = 8761225.0;
//	double endY = 8765905.0;
//
//	int xSize = 672;
//	double startX = 430685.0;
//	double endX = 437395.0;

	int zSize = 2;
	int startZ = 0;
	// dimensions
	writer.addDimension(null, "z", zSize);
	writer.addDimension(null, "y", ySize);
	writer.addDimension(null, "x", xSize);
	

	Variable lc = writer.addVariable(null, "transverse_mercator", DataType.CHAR, "");
	lc.addAttribute(new Attribute("grid_mapping_name", "transverse_mercator"));
	lc.addAttribute(new Attribute("longitude_of_central_meridian", 15.0));
	lc.addAttribute(new Attribute("false_easting", 500000.0));
	lc.addAttribute(new Attribute("false_northing", 0.0));
	lc.addAttribute(new Attribute("latitude_of_projection_origin", 0.0));
	lc.addAttribute(new Attribute("scale_factor_at_central_meridian", 0.99960));
	lc.addAttribute(new Attribute("long_name", "CRS definition"));
	lc.addAttribute(new Attribute("longitude_of_prime_meridian", 0.0));
	lc.addAttribute(new Attribute("semi_major_axis", 6378137.0));
	lc.addAttribute(new Attribute("inverse_flattening", 298.257223563));
	// lc.addAttribute(new Attribute("spatial_ref",
	// "PROJCS[\"WGS 84 / UTM zone 33N\",GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS
	// 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.0174532925199433,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]],PROJECTION[\"Transverse_Mercator\"],PARAMETER[\"latitude_of_origin\",0],PARAMETER[\"central_meridian\",15],PARAMETER[\"scale_factor\",0.9996],PARAMETER[\"false_easting\",500000],PARAMETER[\"false_northing\",0],UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]],AXIS[\"Easting\",EAST],AXIS[\"Northing\",NORTH],AUTHORITY[\"EPSG\",\"32633\"]]"));

	// lc.addAttribute(new Attribute("GeoTransform", "346487.8194347093 46686.02344167536 0 9988344.911171738 0
	// -46686.02344167536"));

	// independent variables

	Variable z = writer.addVariable(null, "z", DataType.DOUBLE, "z");
	z.addAttribute(new Attribute("standard_name", "height_above_reference_ellipsoid"));
	z.addAttribute(new Attribute("long_name", "height_above_osgb_newlyn_datum_masl"));
	z.addAttribute(new Attribute("units", "m"));

	Variable x = writer.addVariable(null, "x", DataType.DOUBLE, "x");
	x.addAttribute(new Attribute("standard_name", "projection_x_coordinate"));
	x.addAttribute(new Attribute("long_name", "x coordinate of projection"));
	x.addAttribute(new Attribute("units", "m"));

	Variable y = writer.addVariable(null, "y", DataType.DOUBLE, "y");
	y.addAttribute(new Attribute("standard_name", "projection_y_coordinate"));
	y.addAttribute(new Attribute("long_name", "y coordinate of projection"));
	y.addAttribute(new Attribute("units", "m"));

	// dependent variables
	Variable pressure = writer.addVariable(null, "pres", DataType.DOUBLE, "z y x");
	pressure.addAttribute(new Attribute("standard_name", "air_pressure"));
	pressure.addAttribute(new Attribute("units", "Pa"));
	pressure.addAttribute(new Attribute("missing_value", 9999.0f));
	// THIS HAS BEEN COMMENTED TODO: implement
	pressure.addAttribute(new Attribute("coordinates", "z y x"));
	pressure.addAttribute(new Attribute("grid_mapping", "transverse_mercator"));

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
	double stepY = (endY - startY) / ySize;
	for (int i = 0; i < ySize; i++) {
	    yFloats.setDouble(i, startY + stepY * i);
	}

	ucar.ma2.ArrayDouble.D1 xFloats = new ArrayDouble.D1(xSize);
	double stepX = (endX - startX) / xSize;
	for (int i = 0; i < xSize; i++) {
	    xFloats.setDouble(i, startX + stepX * i);
	}

	ArrayDouble pressureDoubles = new ArrayDouble.D3(zSize, ySize, xSize);
	// ArrayDouble pressureDoubles = new ArrayDouble.D2(ySize, xSize);
	Index hIndex = pressureDoubles.getIndex();
	for (int i = 0; i < zSize; i++) {
	    for (int j2 = 0; j2 < ySize; j2++) {
		for (int k = 0; k < xSize; k++) {
		    hIndex.set(i, j2, k);
		    // hIndex.set(j2, k);
		    pressureDoubles.setDouble(hIndex, (Math.random() * 5.0));
		}
	    }
	}

	try {
	    writer.write(z, zDoubles);
	    writer.write(y, yFloats);
	    writer.write(x, xFloats);
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
