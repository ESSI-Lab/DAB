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
 * H.2.3. Single time series, including deviations from a nominal fixed spatial location
 * Example H.5. A single timeseries with time-varying deviations from a nominal point spatial location
 * 
 * @author boldrini
 */
public class ExampleH5Writer extends NetCDFCFExampleWriter {
    public static void main(String[] args) throws IOException {
	ExampleH5Writer ew = new ExampleH5Writer();
	ew.write("/tmp/exampleh5.nc");
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

	Variable preciseLon = writer.addVariable(null, "precise_lon", DataType.FLOAT, "time");
	preciseLon.addAttribute(new Attribute("long_name", "station longitude"));
	preciseLon.addAttribute(new Attribute("standard_name", "longitude"));
	preciseLon.addAttribute(new Attribute("units", "degrees_east"));

	Variable preciseLat = writer.addVariable(null, "precise_lat", DataType.FLOAT, "time");
	preciseLat.addAttribute(new Attribute("long_name", "station latitude"));
	preciseLat.addAttribute(new Attribute("standard_name", "latitude"));
	preciseLat.addAttribute(new Attribute("units", "degrees_north"));

	Variable humidity = writer.addVariable(null, "humidity", DataType.FLOAT, "time");
	humidity.addAttribute(new Attribute("standard_name", "specific_humidity"));
	humidity.addAttribute(new Attribute("coordinates", "time lat lon alt precise_lon precise_lat station_name"));
	humidity.addAttribute(new Attribute("_FillValue", -999.9f));

	Variable temperature = writer.addVariable(null, "temp", DataType.FLOAT, "time");
	temperature.addAttribute(new Attribute("standard_name", "air_temperature"));
	temperature.addAttribute(new Attribute("units", "Celsius"));
	temperature.addAttribute(new Attribute("coordinates", "time lat lon alt precise_lon precise_lat station_name"));
	temperature.addAttribute(new Attribute("_FillValue", -999.9f));

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

	ArrayFloat preciseLonFloats = new ArrayFloat.D1(timeSize);
	Index pLonIndex = preciseLonFloats.getIndex();
	ArrayFloat preciseLatFloats = new ArrayFloat.D1(timeSize);
	Index pLatIndex = preciseLatFloats.getIndex();
	ArrayFloat humidityFloats = new ArrayFloat.D1(timeSize);
	Index hIndex = humidityFloats.getIndex();
	ArrayFloat temperatureFloats = new ArrayFloat.D1(timeSize);
	Index tIndex = temperatureFloats.getIndex();
	float baseLon = (float) (Math.random() * 360.0 - 180.0);
	lonFloats.set(baseLon);
	float baseLat = (float) (Math.random() * 180.0 - 90.0);
	latFloats.set(baseLat);
	altFloats.set((float) (Math.random() * 5.0));
	stationNamesArray.setString("station 1");

	for (int j = 0; j < timeSize; j++) {
	    baseLon = checkLongitude((float) (baseLon + Math.random()));
	    preciseLonFloats.setFloat(pLonIndex.set(j), baseLon);
	    baseLat = checkLatitude((float) (baseLat + Math.random()));
	    preciseLatFloats.setFloat(pLatIndex.set(j), baseLat);
	    humidityFloats.setFloat(hIndex.set(j), (float) (1.0 + Math.random()));
	    temperatureFloats.setFloat(tIndex.set(j), (float) (21.0 + Math.random()));
	}

	try {
	    writer.write(time, timeDoubles);
	    writer.write(lon, lonFloats);
	    writer.write(lat, latFloats);
	    writer.write(alt, altFloats);
	    writer.write(stationNames, stationNamesArray);
	    writer.write(preciseLat, preciseLatFloats);
	    writer.write(preciseLon, preciseLonFloats);
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
