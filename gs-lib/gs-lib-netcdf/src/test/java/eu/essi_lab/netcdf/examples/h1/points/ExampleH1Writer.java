package eu.essi_lab.netcdf.examples.h1.points;

import java.io.IOException;

import eu.essi_lab.netcdf.NetCDFCFExampleWriter;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

/**
 * Example H.2. Timeseries with common element times in a time coordinate variable using the orthogonal multidimensional
 * array representation.
 * 
 * @author boldrini
 */
public class ExampleH1Writer extends NetCDFCFExampleWriter {
    public static void main(String[] args) throws IOException {
	ExampleH1Writer ew = new ExampleH1Writer();
	ew.write("/tmp/exampleh1.nc");
	System.out.println("Wrote");
    }

    public void write(String location) throws IOException {

	NetcdfFileWriter writer = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, location, null);
	// global attributes
	writer.addGroupAttribute(null, new Attribute("featureType", "point"));
	writer.addGroupAttribute(null, new Attribute("Conventions", "CF-1.7"));

	int observationSize = 1234;

	// dimensions
	writer.addDimension(null, "obs", observationSize);

	// independent variables

	// dependent variables

	Variable lon = writer.addVariable(null, "lon", DataType.FLOAT, "obs");
	lon.addAttribute(new Attribute("long_name", "longitude of the observation"));
	lon.addAttribute(new Attribute("standard_name", "longitude"));
	lon.addAttribute(new Attribute("units", "degrees_east"));

	Variable lat = writer.addVariable(null, "lat", DataType.FLOAT, "obs");
	lat.addAttribute(new Attribute("long_name", "latitude of the observation"));
	lat.addAttribute(new Attribute("standard_name", "latitude"));
	lat.addAttribute(new Attribute("units", "degrees_north"));

	Variable alt = writer.addVariable(null, "alt", DataType.FLOAT, "obs");
	alt.addAttribute(new Attribute("long_name", "vertical distance above the surface"));
	alt.addAttribute(new Attribute("standard_name", "height"));
	alt.addAttribute(new Attribute("units", "m"));
	alt.addAttribute(new Attribute("positive", "up"));
	alt.addAttribute(new Attribute("axis", "Z"));

	Variable time = writer.addVariable(null, "time", DataType.DOUBLE, "obs");
	time.addAttribute(new Attribute("long_name", "time of measurement"));
	time.addAttribute(new Attribute("standard_name", "time"));
	time.addAttribute(new Attribute("units", "days since 1970-01-01 00:00:00"));

	Variable humidity = writer.addVariable(null, "humidity", DataType.FLOAT, "obs");
	humidity.addAttribute(new Attribute("standard_name", "specific_humidity"));
	humidity.addAttribute(new Attribute("coordinates", "time lat lon alt"));
	humidity.addAttribute(new Attribute("_FillValue", -999.9f));

	Variable temperature = writer.addVariable(null, "temp", DataType.FLOAT, "obs");
	temperature.addAttribute(new Attribute("standard_name", "air_temperature"));
	temperature.addAttribute(new Attribute("units", "Celsius"));
	temperature.addAttribute(new Attribute("coordinates", "time lat lon alt"));
	temperature.addAttribute(new Attribute("_FillValue", -999.9f));

	// create the file
	try {
	    writer.create();
	} catch (IOException e) {
	    System.err.printf("ERROR creating file %s%n%s", location, e.getMessage());
	}

	// Filling with values

	ArrayFloat lonFloats = new ArrayFloat.D1(observationSize);
	Index lonIndex = lonFloats.getIndex();
	ArrayFloat latFloats = new ArrayFloat.D1(observationSize);
	Index latIndex = latFloats.getIndex();
	ArrayFloat altFloats = new ArrayFloat.D1(observationSize);
	Index altIndex = altFloats.getIndex();
	ArrayDouble timeDoubles = new ArrayDouble.D1(observationSize);
	Index tiIndex = timeDoubles.getIndex();
	ArrayFloat humidityFloats = new ArrayFloat.D1(observationSize);
	Index hIndex = humidityFloats.getIndex();
	ArrayFloat temperatureFloats = new ArrayFloat.D1(observationSize);
	Index teIndex = temperatureFloats.getIndex();

	double myTime = 0.0;
	for (int j = 0; j < observationSize; j++) {
	    float lonFloat = (float) (Math.random() * 360.0 - 180.0);
	    lonFloats.setFloat(lonIndex.set(j), lonFloat);
	    float latFloat = (float) (Math.random() * 180.0 - 90.0);
	    latFloats.setFloat(latIndex.set(j), latFloat);
	    float altFloat = (float) (Math.random() * 5.0);
	    altFloats.setFloat(altIndex.set(j), altFloat);
	    myTime = myTime + Math.random();
	    timeDoubles.setDouble(tiIndex.set(j), myTime);
	    humidityFloats.setFloat(hIndex.set(j), (float) (1.0 + Math.random()));
	    temperatureFloats.setFloat(teIndex.set(j), (float) (21.0 + Math.random()));
	}

	try {
	    writer.write(time, timeDoubles);
	    writer.write(lon, lonFloats);
	    writer.write(lat, latFloats);
	    writer.write(alt, altFloats);
	    writer.write(humidity, humidityFloats);
	    writer.write(temperature, temperatureFloats);
	} catch (IOException e) {
	    e.printStackTrace();
	} catch (InvalidRangeException e) {
	    e.printStackTrace();
	}

	writer.close();

	//
    }

    private float checkLatitude(float f) {
	if (f > 90) {
	    f = 90;
	}
	if (f < -90) {
	    f = -90;
	}
	return f;
    }

    private float checkLongitude(float f) {
	if (f > 180) {
	    f = 180;
	}
	if (f < -180) {
	    f = -180;
	}
	return f;
    }

}
