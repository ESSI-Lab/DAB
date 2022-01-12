package eu.essi_lab.netcdf.timeseries;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;

import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayLong;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;
public class H4SingleTimeSeriesWriter extends TimeSeriesWriter {

    public H4SingleTimeSeriesWriter(String location) throws IOException {
	super(location);

    }

    public void write(SimpleStation station, NetCDFVariable<Long> timeVariable, NetCDFVariable<?>... variables) throws IOException {

	int timeSize = timeVariable.getValues().size();

	for (NetCDFVariable<?> variable : variables) {
	    int varSize = variable.getValues().size();
	    if (timeSize != varSize && varSize != 0) {
		throw new RuntimeException("Size mismatch between time and main variables");
	    }
	}

	// dimensions
	int strLen = 128;

	String coordinates = ""; // ;"time lat lon alt station_name";

	Dimension strLenDimension = writer.addDimension(null, "name_strlen", strLen);

	Dimension timeDimension = writer.addDimension(null, "time", 1); // first is set to 1, then is changed on the row
									// below
	timeDimension.setUnlimited(true);
	timeDimension.setLength(timeSize);

	List<Dimension> mainVariableDimensions = new ArrayList<>();
	mainVariableDimensions.add(timeDimension);

	// independent variables
	// DataType timeDataType = DataType.DOUBLE;
	Variable time = writer.addVariable(null, "time", timeVariable.getDataType(), mainVariableDimensions);
	coordinates += time.getShortName();
	time.addAttribute(new Attribute("long_name", "time of measurement"));
	time.addAttribute(new Attribute("standard_name", "time"));
	time.addAttribute(new Attribute("units", timeVariable.getUnits()));
	for (SimpleEntry<String, Object> attr : timeVariable.getAttributes()) {
	    time.addAttribute(getAttribute(attr.getKey(), attr.getValue()));
	}
	if (timeVariable.getMissingValue() != null) {
	    time.addAttribute(new Attribute("missing_Value", timeVariable.getMissingValue()));
	}

	Variable lat = writer.addVariable(null, "lat", DataType.DOUBLE, "");
	coordinates += " " + lat.getShortName();
	lat.addAttribute(new Attribute("long_name", "station latitude"));
	lat.addAttribute(new Attribute("standard_name", "latitude"));
	lat.addAttribute(new Attribute("units", "degrees_north"));

	Variable lon = writer.addVariable(null, "lon", DataType.DOUBLE, "");
	coordinates += " " + lon.getShortName();
	lon.addAttribute(new Attribute("long_name", "station longitude"));
	lon.addAttribute(new Attribute("standard_name", "longitude"));
	lon.addAttribute(new Attribute("units", "degrees_east"));

	Variable alt = null;

	if (station.getAltitude() != null) {
	    alt = writer.addVariable(null, "alt", DataType.DOUBLE, "");
	    coordinates += " " + alt.getShortName();
	    alt.addAttribute(new Attribute("long_name", "vertical distance above the surface"));
	    alt.addAttribute(new Attribute("standard_name", "height"));
	    if (station.getVerticalDatum() != null) {
		alt.addAttribute(new Attribute("vertical_datum", station.getVerticalDatum()));
	    }
	    alt.addAttribute(new Attribute("units", "m"));
	    alt.addAttribute(new Attribute("positive", "up"));
	    alt.addAttribute(new Attribute("axis", "Z"));
	}

	Variable stationNames = writer.addVariable(null, "station_name", DataType.CHAR, strLenDimension.getShortName());
	coordinates += " " + stationNames.getShortName();
	stationNames.addAttribute(new Attribute("long_name", "station name"));
	stationNames.addAttribute(new Attribute("cf_role", "timeseries_id"));

	List<SimpleEntry<String, String>> properties = station.getStationProperties();
	List<Variable> propertyVariables = new ArrayList<>();
	List<ArrayChar> propertyArrays = new ArrayList<>();
	for (int i = 0; i < properties.size(); i++) {
	    SimpleEntry<String, String> property = properties.get(i);
	    String netcdfName = NetCDFUtils.getNetCDFName(property.getKey());
	    Variable propertyVariable = writer.addVariable(null, netcdfName, DataType.CHAR, strLenDimension.getShortName());
	    coordinates += " " + propertyVariable.getShortName();
	    propertyVariable.addAttribute(new Attribute("long_name", property.getKey()));
	    propertyVariables.add(propertyVariable);
	    ArrayChar propertyArray = new ArrayChar.D1(propertyVariable.getShape()[0]);
	    propertyArray.setString(property.getValue());
	    propertyArrays.add(propertyArray);
	}

	// dependent variables
	Variable[] ncVars = new Variable[variables.length];
	for (int i = 0; i < variables.length; i++) {
	    NetCDFVariable<?> variable = variables[i];
	    Variable ncVar;
	    if (variable.getDataType() != null && variable.getDataType().equals(DataType.STRING)) {
		ncVar = writer.addStringVariable(null, variable.getName(), mainVariableDimensions, strLen);
	    } else {
		ncVar = writer.addVariable(null, variable.getName(), variable.getDataType(), mainVariableDimensions);
	    }

	    if (variable.getLongName() != null) {
		ncVar.addAttribute(new Attribute("long_name", variable.getLongName()));
	    }

	    if (variable.getStandardName() != null) {
		ncVar.addAttribute(new Attribute("standard_name", variable.getStandardName()));
	    }
	    if (variable.getUnits() != null) {
		ncVar.addAttribute(new Attribute("units", variable.getUnits()));
	    }
	    ncVar.addAttribute(new Attribute("coordinates", coordinates));
	    if (variable.getMissingValue() != null) {
		Object missingValue = variable.getMissingValue();
		if (missingValue instanceof Number) {
		    ncVar.addAttribute(new Attribute("_FillValue", (Number) missingValue));
		} else if (missingValue instanceof String) {
		    ncVar.addAttribute(new Attribute("_FillValue", (String) missingValue));
		} else {
		    System.err.println("Unrecognized attribute type: " + missingValue);
		}

	    }
	    for (SimpleEntry<String, Object> attr : variable.getAttributes()) {
		if (attr.getValue() != null) {
		    ncVar.addAttribute(getAttribute(attr.getKey(), attr.getValue()));
		}
	    }
	    ncVars[i] = ncVar;
	}

	// create the file
	try {
	    writer.create();
	} catch (IOException e) {
	    System.err.printf("ERROR creating file %s%n%s", writer.getNetcdfFile().getLocation(), e.getMessage());
	}

	// Filling with values

	ucar.ma2.ArrayLong.D1 timeLongs = new ArrayLong.D1(timeSize);
	List<Long> timeValues = timeVariable.getValues();
	for (int i = 0; i < timeSize; i++) {
	    timeLongs.setLong(i, timeValues.get(i));
	}

	int[] stationNamesShape = stationNames.getShape();
	ucar.ma2.ArrayDouble.D0 altFloats = new ArrayDouble.D0();
	ucar.ma2.ArrayDouble.D0 lonDoubles = new ArrayDouble.D0();
	ucar.ma2.ArrayDouble.D0 latDoubles = new ArrayDouble.D0();
	ArrayChar stationNamesArray = new ArrayChar.D1(stationNamesShape[0]);

	if (station.getAltitude() != null) {
	    altFloats.set(station.getAltitude());
	}
	if (station.getLatitude() != null) {
	    latDoubles.set(station.getLatitude());
	}
	if (station.getLongitude() != null) {
	    lonDoubles.set(station.getLongitude());
	}

	if (station.getName() != null) {
	    stationNamesArray.setString(station.getName());
	}

	Array[] arrays = new Array[variables.length];

	for (int i = 0; i < arrays.length; i++) {
	    NetCDFVariable<?> variable = variables[i];
	    arrays[i] = variable.getArray();
	}

	try {
	    writer.write(time, timeLongs);
	    if (station.getLatitude() != null) {
		writer.write(lat, latDoubles);
	    }
	    if (station.getLongitude() != null) {
		writer.write(lon, lonDoubles);
	    }
	    if (station.getAltitude() != null) {
		writer.write(alt, altFloats);
	    }
	    writer.write(stationNames, stationNamesArray);

	    for (int i = 0; i < properties.size(); i++) {
		writer.write(propertyVariables.get(i), propertyArrays.get(i));
	    }

	    for (int i = 0; i < arrays.length; i++) {
		System.out.println("writing var " + ncVars[i].getShortName());
		switch (ncVars[i].getDataType()) {
		case CHAR:
		    writer.writeStringData(ncVars[i], arrays[i]);
		    break;
		default:
		    writer.write(ncVars[i], arrays[i]);
		}

	    }

	} catch (IOException e) {
	    e.printStackTrace();
	} catch (InvalidRangeException e) {
	    e.printStackTrace();
	}

	writer.close();

	//
    }

    private Attribute getAttribute(String key, Object value) {
	Attribute ret;
	if (value instanceof Number) {
	    Number number = (Number) value;
	    ret = new Attribute(key, number);
	} else {
	    ret = new Attribute(key, value.toString());
	}
	return ret;
    }

}
