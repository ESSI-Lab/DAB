package eu.essi_lab.accessor.waf.onamet;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

public class ONAMETNormalizer {

    /**
     * @param input
     * @param output
     * @param deleteInput
     * @throws Exception
     */
    public static void normalize(File input, File output, boolean deleteInput) throws Exception {

	NetcdfDataset dataset = NetcdfDataset.openDataset(input.getAbsolutePath());

	NetcdfFileWriter writer = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf4, output.getAbsolutePath(), null);
	GSLoggerFactory.getLogger(ONAMETNormalizer.class).info("Converting {} to {}", input.getAbsolutePath(), output.getAbsolutePath());
	List<Attribute> globalAttributes = dataset.getGlobalAttributes();
	for (Attribute globalAttribute : globalAttributes) {
	    if (globalAttribute.getName().equals("TITLE")) {

		writer.addGroupAttribute(null, globalAttribute);
	    }
	}

	HashSet<String> lonNames = new HashSet<>();
	lonNames.add("XLONG");
	lonNames.add("XLONG_U");
	lonNames.add("XLONG_V");

	HashSet<String> latNames = new HashSet<>();
	latNames.add("XLAT");
	latNames.add("XLAT_U");
	latNames.add("XLAT_V");

	String timeName = "XTIME";

	List<Dimension> dimensions = dataset.getDimensions();
	for (Dimension dimension : dimensions) {
	    String dimName = dimension.getShortName();
	    if (dimName.equals(timeName)) {
		dimName = "time";
	    }
	    writer.addDimension(null, dimName, dimension.getLength(), dimension.isShared(), dimension.isUnlimited(),
		    dimension.isVariableLength());
	}

	for (String latName : latNames) {
	    Integer size = findLatitudeSize(dataset, latName);
	    if (size != null) {
		Dimension newD = writer.addDimension(null, latName + "1D", size);
		Variable v = writer.addVariable(null, newD.getShortName(), dataset.findVariable(latName).getDataType(),
			newD.getShortName());
		v.addAttribute(new Attribute("units", "degree_north"));
	    }
	}

	for (String lonName : lonNames) {
	    Integer size = findLongitudeSize(dataset, lonName);
	    if (size != null) {
		Dimension newD = writer.addDimension(null, lonName + "1D", size);
		Variable v = writer.addVariable(null, newD.getShortName(), dataset.findVariable(lonName).getDataType(),
			newD.getShortName());
		v.addAttribute(new Attribute("units", "degree_east"));
	    }
	}

	List<Variable> variables = dataset.getVariables();
	HashMap<Variable, Variable> mapNewToOld = new HashMap<>();

	for (Variable variable : variables) {
	    Attribute coordinates = variable.findAttribute("coordinates");
	    boolean geo2D = false;
	    String latString = null;
	    String lonString = null;
	    if (coordinates != null) {
		String str = coordinates.getStringValue();
		if (str.contains(" ")) {
		    String[] split = str.split(" ");
		    for (String s : split) {
			if (lonNames.contains(s)) {
			    lonString = s;
			}
			if (latNames.contains(s)) {
			    latString = s;
			}
		    }

		    if (latString != null && lonString != null)//
		    {
			// Geo 2D variable
			geo2D = true;
		    }
		}
	    }
	    String dimensionString = variable.getDimensionsString();
	    if (geo2D) {
		dimensionString = dimensionString.replace("south_north_stag", latString + "1D");
		dimensionString = dimensionString.replace("south_north", latString + "1D");
		dimensionString = dimensionString.replace("west_east_stag", lonString + "1D");
		dimensionString = dimensionString.replace("west_east", lonString + "1D");
	    }
	    dimensionString = dimensionString.replace(timeName, "time");
	    String varName = variable.getShortName();
	    if (varName.equals(timeName)) {
		varName = "time";
	    }
	    Variable v = writer.addVariable(null, varName, variable.getDataType(), dimensionString);
	    List<Attribute> attributes = variable.getAttributes();
	    for (Attribute attribute : attributes) {
		if (!geo2D || !attribute.getShortName().equals("coordinates")) {
		    v.addAttribute(attribute);
		}
	    }
	    if (geo2D) {
		v.addAttribute(new Attribute("grid_mapping", "crs"));
		v.addAttribute(new Attribute("coordinates", dimensionString));
	    }
	    mapNewToOld.put(v, variable);
	}

	Variable crs = writer.addVariable(null, "crs", DataType.INT, "");
	crs.addAttribute(new Attribute("grid_mapping_name", "latitude_longitude"));
	crs.addAttribute(new Attribute("longitude_of_prime_meridian", 0.0));
	crs.addAttribute(new Attribute("semi_major_axis", 6378137.0));
	crs.addAttribute(new Attribute("inverse_flattening", 298.257223563));

	// create the file
	try {
	    writer.create();
	} catch (IOException e) {
	    System.err.printf("ERROR creating file %s%n%s", output.getAbsolutePath(), e.getMessage());
	}

	// Filling with values

	for (Variable variable : mapNewToOld.keySet()) {
	    Variable original = mapNewToOld.get(variable);
	    Array array = original.read();
	    writer.write(variable, array);
	}

	for (String lonName : lonNames) {
	    Variable v = dataset.findVariable(lonName);
	    Variable newV = writer.findVariable(lonName + "1D");
	    Array array = v.read();
	    Integer lonSize = findLongitudeSize(dataset, lonName);
	    Array newArray = Array.factory(v.getDataType(), new int[] { lonSize });
	    for (int i = 0; i < lonSize; i++) {
		newArray.setObject(i, array.getObject(i));
	    }
	    writer.write(newV, newArray);

	}

	for (String latName : latNames) {
	    Variable v = dataset.findVariable(latName);
	    Variable newV = writer.findVariable(latName + "1D");
	    Array array = v.read();
	    Integer latSize = findLatitudeSize(dataset, latName);
	    Integer lonSize = findLongitudeSize(dataset, latName);
	    Array newArray = Array.factory(v.getDataType(), new int[] { latSize });
	    for (int i = 0; i < latSize; i++) {
		newArray.setObject(i, array.getObject(i * lonSize));
	    }
	    writer.write(newV, newArray);

	}

	//
	// ArrayDouble pressureDoubles = new ArrayDouble.D3(timeSize, latSize, lonSize);
	// Index hIndex = pressureDoubles.getIndex();
	// for (int i = 0; i < timeSize; i++) {
	// for (int j2 = 0; j2 < latSize; j2++) {
	// for (int k = 0; k < lonSize; k++) {
	// hIndex.set(i, j2, k);
	// pressureDoubles.setDouble(hIndex, (Math.random() * 5.0));
	// }
	// }
	// }

	writer.close();
	dataset.close();

	if (deleteInput) {
	    input.delete();
	}

	GSLoggerFactory.getLogger(ONAMETNormalizer.class).info("Wrote {}", output.getAbsolutePath());

    }

    /**
     * @param dataset
     * @param latName
     * @return
     */
    private static Integer findLatitudeSize(NetcdfDataset dataset, String latName) {
	Variable latVariable = dataset.findVariable(latName);
	List<Dimension> dims = latVariable.getDimensions();
	for (Dimension d : dims) {
	    if (d.getShortName().startsWith("south_north")) {
		return d.getLength();
	    }
	}
	return null;
    }

    /**
     * @param dataset
     * @param lonName
     * @return
     */
    private static Integer findLongitudeSize(NetcdfDataset dataset, String lonName) {
	Variable lonVariable = dataset.findVariable(lonName);
	List<Dimension> dims = lonVariable.getDimensions();
	for (Dimension d : dims) {
	    if (d.getShortName().startsWith("west_east")) {
		return d.getLength();
	    }
	}
	return null;
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

	File f1 = new File("/home/boldrini/nc-test/onamet.nc");
	File f2 = new File("/home/boldrini/nc-test/onamet-2.nc");

	ONAMETNormalizer.normalize(f1, f2, false);
    }
}
