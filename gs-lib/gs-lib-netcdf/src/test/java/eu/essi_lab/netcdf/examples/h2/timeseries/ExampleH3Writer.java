package eu.essi_lab.netcdf.examples.h2.timeseries;

import java.io.IOException;

import eu.essi_lab.netcdf.NetCDFCFExampleWriter;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

/**
 * H.2.2. Incomplete multidimensional array representation of time series
 * Example H.3. Timeseries of station data in the incomplete multidimensional array representation.
 * 
 * @author boldrini
 */
public class ExampleH3Writer extends NetCDFCFExampleWriter {
    public static void main(String[] args) throws IOException {
	ExampleH3Writer ew = new ExampleH3Writer();
	ew.write("/tmp/exampleh3.nc");
	System.out.println("Wrote");
    }

    public void write(String location) throws IOException {

	NetcdfFileWriter writer = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, location, null);
	// global attributes
	writer.addGroupAttribute(null, new Attribute("featureType", "timeSeries"));
	writer.addGroupAttribute(null, new Attribute("Conventions", "CF-1.7"));

	int stationSize = 10;
	int observationSize = 13;
	// dimensions
	Dimension nameStrLen = writer.addDimension(null, "name_strlen", 80);
	Dimension stationDimension = writer.addDimension(null, "station", stationSize);
	stationDimension.setUnlimited(true);

	writer.addDimension(null, "obs", observationSize);

	// independent variables

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
	alt.addAttribute(new Attribute("standard_name", "Z"));

	Variable stationNames = writer.addVariable(null, "station_name", DataType.CHAR, "station name_strlen");
	stationNames.addAttribute(new Attribute("long_name", "station name"));
	stationNames.addAttribute(new Attribute("cf_role", "timeseries_id"));

	Variable stationInfo = writer.addVariable(null, "station_info", DataType.INT, "station");
	stationInfo.addAttribute(new Attribute("long_name", "any kind of station info"));

	Variable elevation = writer.addVariable(null, "station_elevation", DataType.FLOAT, "station");
	elevation.addAttribute(new Attribute("long_name", "height above the geoid"));
	elevation.addAttribute(new Attribute("standard_name", "surface_altitude"));
	elevation.addAttribute(new Attribute("units", "m"));

	// dependent variables
	Variable time = writer.addVariable(null, "time", DataType.DOUBLE, "station obs");
	time.addAttribute(new Attribute("long_name", "time of measurement"));
	time.addAttribute(new Attribute("standard_name", "time"));
	time.addAttribute(new Attribute("units", "days since 1970-01-01 00:00:00"));
	time.addAttribute(new Attribute("missing_value", "-999.9"));

	Variable humidity = writer.addVariable(null, "humidity", DataType.FLOAT, "station obs");
	humidity.addAttribute(new Attribute("standard_name", "specific_humidity"));
	humidity.addAttribute(new Attribute("coordinates", "time lat lon alt station_name"));
	humidity.addAttribute(new Attribute("_FillValue", -999.9f));

	Variable temperature = writer.addVariable(null, "temp", DataType.FLOAT, "station obs");
	temperature.addAttribute(new Attribute("standard_name", "air_temperature"));
	temperature.addAttribute(new Attribute("units", "Celsius"));
	temperature.addAttribute(new Attribute("coordinates", "time lat lon alt station_name"));
	temperature.addAttribute(new Attribute("_FillValue", -999.9f));

	// writer.addDimension(null, "names", 3);
	// writer.addVariable(null, "names", DataType.CHAR, "names svar_len");

	// Filling with values

	// create the file
	try {
	    writer.create();
	} catch (IOException e) {
	    System.err.printf("ERROR creating file %s%n%s", location, e.getMessage());
	}

	int[] stationNamesShape = stationNames.getShape();
	ucar.ma2.ArrayFloat.D1 altFloats = new ArrayFloat.D1(stationSize);
	ucar.ma2.ArrayFloat.D1 lonFloats = new ArrayFloat.D1(stationSize);
	ucar.ma2.ArrayFloat.D1 latFloats = new ArrayFloat.D1(stationSize);
	ucar.ma2.ArrayFloat.D1 elevationFloats = new ArrayFloat.D1(stationSize);
	ArrayChar stationNamesArray = new ArrayChar.D2(stationNamesShape[0], stationNamesShape[1]);
	Index stationNamesArrayIndex = stationNamesArray.getIndex();
	ArrayDouble timeDoubles = new ArrayDouble.D2(stationSize, observationSize);
	ArrayFloat humidityFloats = new ArrayFloat.D2(stationSize, observationSize);
	ArrayFloat temperatureFloats = new ArrayFloat.D2(stationSize, observationSize);
	Index hIndex = humidityFloats.getIndex();
	Index teIndex = temperatureFloats.getIndex();
	Index tiIndex = timeDoubles.getIndex();
	for (int i = 0; i < stationSize; i++) {
	    lonFloats.setFloat(i, (float) (Math.random() * 360.0 - 180.0));
	    latFloats.setFloat(i, (float) (Math.random() * 180.0 - 90.0));
	    altFloats.setFloat(i, (float) (Math.random() * 5.0));
	    elevationFloats.setFloat(i, (float) (Math.random() * 100.0));
	    stationNamesArray.setString(stationNamesArrayIndex.set(i), "station " + (i + 1));

	    int toRemove = (int) (Math.random() * observationSize / 2.0);
	    int maxObservations = observationSize - toRemove;
	    for (int j = 0; j < observationSize; j++) {
		if (j < maxObservations) {
		    humidityFloats.setFloat(hIndex.set(i, j), (float) (i + Math.random()));
		    temperatureFloats.setFloat(teIndex.set(i, j), (float) (i + Math.random()));
		    timeDoubles.setDouble(tiIndex.set(i, j), j + Math.random() / 5.0);
		} else {
		    humidityFloats.setFloat(hIndex.set(i, j), -999.9f);
		    temperatureFloats.setFloat(teIndex.set(i, j), -999.9f);
		    timeDoubles.setDouble(tiIndex.set(i, j), -999.9f);
		}
	    }
	}

	try {
	    writer.write(lon, lonFloats);
	    writer.write(lat, latFloats);
	    writer.write(alt, altFloats);
	    writer.write(stationNames, stationNamesArray);
	    // writer.write(stationInfos, stationInfosArray);
	    writer.write(elevation, elevationFloats);
	    writer.write(time, timeDoubles);
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
