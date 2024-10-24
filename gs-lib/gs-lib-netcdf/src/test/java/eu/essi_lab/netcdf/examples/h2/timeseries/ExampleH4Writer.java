package eu.essi_lab.netcdf.examples.h2.timeseries;

import java.io.IOException;

import eu.essi_lab.netcdf.NetCDFCFExampleWriter;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayDouble.D1;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayFloat.D0;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

/**
 * H.2.3. Single time series
 * Example H.4. A single timeseries.
 * 
 * @author boldrini
 */
public class ExampleH4Writer extends NetCDFCFExampleWriter {
    public static void main(String[] args) throws IOException {
	ExampleH4Writer ew = new ExampleH4Writer();
	ew.write("/tmp/exampleh4.nc");
	System.out.println("Wrote");
    }

    public void write(String location) throws IOException {

	NetcdfFileWriter writer = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, location, null);
	// global attributes
	writer.addGroupAttribute(null, new Attribute("featureType", "timeSeries"));
	writer.addGroupAttribute(null, new Attribute("Conventions", "CF-1.7"));

	int timeSize = 100233;
	// dimensions
	writer.addDimension(null, "name_strlen", 23);
	writer.addDimension(null, "time", timeSize);

	// independent variables

	Variable lon = writer.addVariable(null, "lon", DataType.FLOAT, "");
	lon.addAttribute(new Attribute("long_name", "station longitude"));
	lon.addAttribute(new Attribute("standard_name", "longitude"));
	lon.addAttribute(new Attribute("units", "degrees_east"));

	Variable lat = writer.addVariable(null, "lat", DataType.FLOAT, "");
	lat.addAttribute(new Attribute("long_name", "station latitude"));
	lat.addAttribute(new Attribute("standard_name", "latitude"));
	lat.addAttribute(new Attribute("units", "degrees_north"));

	Variable alt = writer.addVariable(null, "alt", DataType.FLOAT, "");
	alt.addAttribute(new Attribute("long_name", "vertical distance above the surface"));
	alt.addAttribute(new Attribute("standard_name", "height"));
	alt.addAttribute(new Attribute("units", "m"));
	alt.addAttribute(new Attribute("positive", "up"));
	alt.addAttribute(new Attribute("axis", "Z"));

	Variable stationNames = writer.addVariable(null, "station_name", DataType.CHAR, "name_strlen");
	stationNames.addAttribute(new Attribute("long_name", "station name"));
	stationNames.addAttribute(new Attribute("cf_role", "timeseries_id"));

	Variable time = writer.addVariable(null, "time", DataType.DOUBLE, "time");
	time.addAttribute(new Attribute("long_name", "time of measurement"));
	time.addAttribute(new Attribute("standard_name", "time"));
	time.addAttribute(new Attribute("units", "days since 1970-01-01 00:00:00"));
	time.addAttribute(new Attribute("missing_Value", -999.9));

	// dependent variables
	Variable humidity = writer.addVariable(null, "humidity", DataType.FLOAT, "time");
	humidity.addAttribute(new Attribute("standard_name", "specific_humidity"));
	humidity.addAttribute(new Attribute("coordinates", "time lat lon alt station_name"));
	humidity.addAttribute(new Attribute("_FillValue", -999.9));

	Variable temperature = writer.addVariable(null, "temp", DataType.FLOAT, "time");
	temperature.addAttribute(new Attribute("standard_name", "air_temperature"));
	temperature.addAttribute(new Attribute("units", "Celsius"));
	temperature.addAttribute(new Attribute("coordinates", "time lat lon alt station_name"));
	temperature.addAttribute(new Attribute("_FillValue", -999.9));

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
	D0 altFloats = new ArrayFloat.D0();
	D0 lonFloats = new ArrayFloat.D0();
	D0 latFloats = new ArrayFloat.D0();
	ArrayChar stationNamesArray = new ArrayChar.D1(stationNamesShape[0]);

	ArrayFloat humidityFloats = new ArrayFloat.D1(timeSize);
	Index hIndex = humidityFloats.getIndex();
	ArrayFloat temperatureFloats = new ArrayFloat.D1(timeSize);
	Index tIndex = temperatureFloats.getIndex();
	lonFloats.set((float) (Math.random() * 360.0 - 180.0));
	latFloats.set((float) (Math.random() * 180.0 - 90.0));
	altFloats.set((float) (Math.random() * 5.0));
	stationNamesArray.setString("station 1");

	for (int j = 0; j < timeSize; j++) {
	    humidityFloats.setFloat(hIndex.set(j), (float) (1.0 + Math.random()));
	    temperatureFloats.setFloat(tIndex.set(j), (float) (21.0 + Math.random()));
	}

	try {
	    writer.write(time, timeDoubles);
	    writer.write(lon, lonFloats);
	    writer.write(lat, latFloats);
	    writer.write(alt, altFloats);
	    writer.write(stationNames, stationNamesArray);
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

}
