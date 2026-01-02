package eu.essi_lab.netcdf.trajectory;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;

import eu.essi_lab.netcdf.timeseries.NetCDFUtils;
import eu.essi_lab.netcdf.timeseries.NetCDFVariable;
import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayLong;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;

/**
 * H.4.2. Single trajectory
 * Example H.13. A single trajectory.
 * 
 * @author boldrini
 */
public class H13SingleTrajectoryWriter extends TrajectoryWriter {

    public H13SingleTrajectoryWriter(String location) throws IOException {
	super(location);

    }

    public void write(SimpleTrajectory trajectory, NetCDFVariable<Long> timeVariable, NetCDFVariable<Double> latVariable,
	    NetCDFVariable<Double> lonVariable, NetCDFVariable<Double> altVariable, NetCDFVariable<?>... variables) throws IOException {

	int timeSize = timeVariable.getValues().size();
	int varSize;
	for (NetCDFVariable<?> variable : variables) {
	    varSize = variable.getValues().size();
	    if (timeSize != varSize && varSize != 0) {
		throw new RuntimeException("Size mismatch between time and main variables");
	    }
	}

	varSize = latVariable.getValues().size();
	if (timeSize != varSize && varSize != 0) {
	    throw new RuntimeException("Size mismatch between time and latitude variable");
	}

	varSize = lonVariable.getValues().size();
	if (timeSize != varSize && varSize != 0) {
	    throw new RuntimeException("Size mismatch between time and longitude variable");
	}

	if (altVariable != null) {
	    varSize = altVariable.getValues().size();
	    if (timeSize != varSize && varSize != 0) {
		throw new RuntimeException("Size mismatch between time and altitude variable");
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
	Variable time = writer.addVariable(null, timeVariable.getName(), timeVariable.getDataType(), mainVariableDimensions);
	coordinates += time.getShortName();
	time.addAttribute(new Attribute("standard_name", "time"));
	time.addAttribute(new Attribute("long_name", "time"));
	time.addAttribute(new Attribute("units", timeVariable.getUnits()));
	for (SimpleEntry<String, Object> attr : timeVariable.getAttributes()) {
	    time.addAttribute(getAttribute(attr.getKey(), attr.getValue()));
	}
	if (timeVariable.getMissingValue() != null) {
	    time.addAttribute(new Attribute("missing_Value", timeVariable.getMissingValue()));
	}

	Variable lon = writer.addVariable(null, lonVariable.getName(), lonVariable.getDataType(), mainVariableDimensions);
	coordinates += " " + lon.getShortName();
	lon.addAttribute(new Attribute("standard_name", "longitude"));
	lon.addAttribute(new Attribute("long_name", "longitude"));
	lon.addAttribute(new Attribute("units", "degrees_east"));

	Variable lat = writer.addVariable(null, latVariable.getName(), latVariable.getDataType(), mainVariableDimensions);
	coordinates += " " + lat.getShortName();
	lat.addAttribute(new Attribute("standard_name", "latitude"));
	lat.addAttribute(new Attribute("long_name", "latitude"));
	lat.addAttribute(new Attribute("units", "degrees_north"));

	Variable alt = null;

	if (altVariable != null) {
	    alt = writer.addVariable(null, altVariable.getName(),altVariable.getDataType(), mainVariableDimensions);
	    coordinates += " " + alt.getShortName();
	    alt.addAttribute(new Attribute("standard_name", "altitude"));
	    alt.addAttribute(new Attribute("long_name", "height above mean sea level"));
	    if (trajectory.getVerticalDatum() != null) {
		alt.addAttribute(new Attribute("vertical_datum", trajectory.getVerticalDatum()));
	    }
	    alt.addAttribute(new Attribute("units", "m"));
	    alt.addAttribute(new Attribute("positive", "up"));
	    alt.addAttribute(new Attribute("axis", "Z"));
	}

	Variable stationNames = writer.addVariable(null, "trajectory", DataType.CHAR, strLenDimension.getShortName());
	stationNames.addAttribute(new Attribute("cf_role", trajectory.getIdentifier()));
	stationNames.addAttribute(new Attribute("long_name", trajectory.getName()));

	if (trajectory.getDescription() != null) {
	    stationNames.addAttribute(new Attribute("description", trajectory.getDescription()));
	}

	List<SimpleEntry<String, String>> properties = trajectory.getStationProperties();
	// List<Variable> propertyVariables = new ArrayList<>();
	// List<ArrayChar> propertyArrays = new ArrayList<>();
	for (int i = 0; i < properties.size(); i++) {
	    SimpleEntry<String, String> property = properties.get(i);
	    String netcdfName = NetCDFUtils.getNetCDFName(property.getKey());

	    stationNames.addAttribute(new Attribute(netcdfName, property.getValue()));
	}

	// dependent variables
	Variable[] ncVars = new Variable[variables.length];
	for (int i = 0; i < variables.length; i++) {
	    NetCDFVariable<?> variable = variables[i];
	    varSize = variable.getValues().size();
	    if (timeSize != varSize && varSize != 0) {
		throw new RuntimeException("Size mismatch between time and " + variable.getName() + " variable");
	    }

	    Variable ncVar = writer.addVariable(null, variable.getName(), variable.getDataType(), mainVariableDimensions);

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

	ucar.ma2.ArrayLong.D1 timeLongs = new ArrayLong.D1(timeSize,false);
	ucar.ma2.ArrayDouble.D1 altFloats = new ArrayDouble.D1(timeSize);
	ucar.ma2.ArrayDouble.D1 lonFloats = new ArrayDouble.D1(timeSize);
	ucar.ma2.ArrayDouble.D1 latFloats = new ArrayDouble.D1(timeSize);
	List<Long> timeValues = timeVariable.getValues();
	List<Double> lonValues = lonVariable.getValues();
	List<Double> latValues = latVariable.getValues();
	List<Double> altValues = null;
	if (altVariable != null) {
	    altValues = altVariable.getValues();
	}
	for (int i = 0; i < timeSize; i++) {
	    timeLongs.setLong(i, timeValues.get(i));
	    lonFloats.setDouble(i, lonValues.get(i));
	    latFloats.setDouble(i, latValues.get(i));
	    if (altVariable != null) {
		altFloats.setDouble(i, altValues.get(i));
	    }
	}

	int[] stationNamesShape = stationNames.getShape();

	ArrayChar stationNamesArray = new ArrayChar.D1(stationNamesShape[0]);

	if (trajectory.getName() != null) {
	    byte[] bytes = trajectory.getName().getBytes(StandardCharsets.UTF_8);
	    for (int i = 0; i < bytes.length; i++) {
		stationNamesArray.setByte(i, bytes[i]);
	    }
	}

	Array[] arrays = new Array[variables.length];

	for (int i = 0; i < arrays.length; i++) {
	    NetCDFVariable<?> variable = variables[i];
	    arrays[i] = variable.getArray();
	}

	try {
	    writer.write(time, timeLongs);
	    writer.write(lon, lonFloats);
	    writer.write(lat, latFloats);
	    if (altValues != null) {
		writer.write(alt, altFloats);
	    }
	    writer.write(stationNames, stationNamesArray);

//	    for (int i = 0; i < properties.size(); i++) {
//		writer.write(propertyVariables.get(i), propertyArrays.get(i));
//	    }

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
