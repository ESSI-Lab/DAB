package eu.essi_lab.workflow.processor.grid;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.io.IOUtils;

import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.netcdf.timeseries.NetCDFUtils;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.NetcdfFileWriter.Version;
import ucar.nc2.Variable;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.NetcdfDataset;

public class GDALNetCDFPostConversionUtils {

    private static final String GDAL_NETCDF_POST_CONVERSION_ERROR = "GDAL_NETCDF_POST_CONVERSION_ERROR";

    /**
     * Copy variable attributes (because GDAL eliminates them)
     * 
     * @param input
     * @return
     * @throws GSException
     */
    public static DataObject copyAttributes(DataObject source, DataObject input) throws GSException {
	try {

	    DataDescriptor sourceDescriptor = source.getDataDescriptor();
	    if (sourceDescriptor != null) {
		DataFormat format = sourceDescriptor.getDataFormat();
		if (format != null) {
		    if (format.equals(DataFormat.NETCDF()) || format.isSubTypeOf(DataFormat.NETCDF())) {

		    } else {
			return input;
		    }
		}
	    }

	    HashMap<String, Variable> mainVariablesMap = new HashMap<>();
	    File sourceFile = source.getFile();
	    NetcdfDataset sourceReader = NetcdfDataset.openDataset(sourceFile.getAbsolutePath());
	    File inputFile = input.getFile();
	    NetcdfDataset inputReader = NetcdfDataset.openDataset(inputFile.getAbsolutePath());

	    List<Variable> mainVariables = NetCDFUtils.getGeographicVariables(inputReader);
	    for (Variable mainVariable : mainVariables) {
		mainVariablesMap.put(mainVariable.getShortName(), mainVariable);
	    }

	    File tmpFile = File.createTempFile("GDAL_To_NetCDF_Processor", ".nc");
	    tmpFile.deleteOnExit();
	    NetcdfFileWriter writer = NetcdfFileWriter.createNew(Version.netcdf3, tmpFile.getAbsolutePath());

	    // global attributes
	    for (Attribute globalAttribute : sourceReader.getGlobalAttributes()) {
		String name = globalAttribute.getShortName();
		if (name.equals("history")) {
		    globalAttribute = new Attribute("history",
			    globalAttribute.getStringValue() + "\n ESSI-Lab " + GDALNetCDFPostConversionUtils.class.getSimpleName());
		}
		writer.addGroupAttribute(null, globalAttribute);
	    }

	    // dimensions
	    for (Dimension dimension : inputReader.getDimensions()) {
		writer.addDimension(null, dimension.getShortName(), dimension.getLength());
	    }

	    // variables
	    HashMap<String, Variable> newVariables = new HashMap<>();
	    for (Variable inputVariable : inputReader.getVariables()) {
		String name = inputVariable.getShortName();
		Variable newVariable = writer.addVariable(null, name, inputVariable.getDataType(), inputVariable.getDimensionsString());
		Variable sourceVariable = sourceReader.findVariable(name);
		Variable copyFrom = sourceVariable != null ? sourceVariable : inputVariable;

		if (mainVariablesMap.containsKey(name)) {
		    // copy in case of main variables
		    Variable sourceBand = mainVariablesMap.get(name);
		    List<Attribute> sourceAttributes = sourceBand.getAttributes();
		    for (Attribute sourceAttribute : sourceAttributes) {
			String attributeName = sourceAttribute.getShortName();
			switch (attributeName) {
			case "valid_min":
			case "valid_max":
			case "_FillValue":
			case "missing_value":
			    newVariable.addAttribute(sourceAttribute);
			    break;
			// newVariable.addAttribute(sourceAttribute);
			// Attribute missingAttribute = new Attribute("missing_value", sourceAttribute.getValues());
			// newVariable.addAttribute(missingAttribute );
			// break;
			default:
			    break;
			}
		    }
		} else {
		    // copy in case of general variables
		    for (Attribute attribute : copyFrom.getAttributes()) {
			Attribute newAttribute = new Attribute(attribute.getShortName(), attribute);
			newVariable.addAttribute(newAttribute);
		    }
		}
		newVariables.put(name, newVariable);

	    }

	    writer.create();

	    // finished definition, start writing of data
	    for (Variable variable : inputReader.getVariables()) {
		String name = variable.getShortName();
		Variable newVariable = newVariables.get(name);
		writer.write(newVariable, variable.read());
	    }

	    writer.close();
	    inputReader.close();
	    sourceReader.close();

	    DataObject output = new DataObject();
	    output.setDataDescriptor(input.getDataDescriptor());
	    output.setFile(tmpFile);
	    sourceFile.delete();

	    return output;

	} catch (

	Exception e) {

	    e.printStackTrace();

	    throw GSException.createException(//
		    GDALNetCDFPostConversionUtils.class, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    GDAL_NETCDF_POST_CONVERSION_ERROR, //
		    e);
	}
    }

    /**
     * Adds min and max values to the variable attributes. Possibly the values are copied from the input data
     * descriptor, if present. Otherwise they will be calculated.
     * 
     * @param dataDescriptor
     * @param input
     * @return
     * @throws GSException
     */
    public static DataObject addMinMax(DataDescriptor dataDescriptor, DataObject input) throws GSException {
	try {

	    Number globalMin = dataDescriptor == null ? null : dataDescriptor.getRangeMinimum();
	    Number globalMax = dataDescriptor == null ? null : dataDescriptor.getRangeMaximum();

	    File inputFile = input.getFile();
	    NetcdfDataset reader = NetcdfDataset.openDataset(inputFile.getAbsolutePath());

	    // first, bands are scanned for min/max values, to be added later
	    List<Variable> bands = NetCDFUtils.getGeographicVariables(reader);
	    List<String> bandNames = new ArrayList<>();
	    for (Variable band : bands) {
		bandNames.add(band.getFullName());
	    }
	    HashMap<String, SimpleEntry<Number, Number>> bandToMinMax = new HashMap<>();
	    if (globalMin != null && Double.isFinite(globalMin.doubleValue()) && globalMax != null
		    && Double.isFinite(globalMax.doubleValue())) {
		for (String bandName : bandNames) {
		    bandToMinMax.put(bandName, new SimpleEntry<Number, Number>(globalMin, globalMax));
		}
	    } else {

	    }

	    File tmpFile = File.createTempFile("GDAL_To_NetCDF_Processor", ".nc");
	    NetcdfFileWriter writer = NetcdfFileWriter.createNew(Version.netcdf3, tmpFile.getAbsolutePath());

	    // global attributes
	    for (Attribute globalAttribute : reader.getGlobalAttributes()) {
		String name = globalAttribute.getShortName();
		if (name.equals("history")) {
		    globalAttribute = new Attribute("history",
			    globalAttribute.getStringValue() + "\n ESSI-Lab " + GDALNetCDFPostConversionUtils.class.getSimpleName());
		}
		writer.addGroupAttribute(null, globalAttribute);
	    }

	    // dimensions
	    for (Dimension dimension : reader.getDimensions()) {
		writer.addDimension(null, dimension.getShortName(), dimension.getLength());
	    }

	    // variables
	    HashMap<String, Variable> newVariables = new HashMap<>();
	    for (Variable variable : reader.getVariables()) {
		String name = variable.getShortName();
		Variable newVariable = writer.addVariable(null, name, variable.getDataType(), variable.getDimensionsString());
		for (Attribute attribute : variable.getAttributes()) {
		    Attribute newAttribute = new Attribute(attribute.getShortName(), attribute);
		    newVariable.addAttribute(newAttribute);
		}
		if (bandNames.contains(name)) {
		    SimpleEntry<Number, Number> minMax = bandToMinMax.get(name);
		    if (minMax != null) {
			Attribute minAttribute = new Attribute("valid_min", minMax.getKey());
			newVariable.addAttribute(minAttribute);
			Attribute maxAttribute = new Attribute("valid_max", minMax.getValue());
			newVariable.addAttribute(maxAttribute);
		    }
		}
		newVariables.put(name, newVariable);

	    }

	    writer.create();

	    // finished definition, start writing of data
	    for (Variable variable : reader.getVariables()) {
		String name = variable.getShortName();
		Variable newVariable = newVariables.get(name);
		writer.write(newVariable, variable.read());
	    }

	    writer.close();
	    reader.close();

	    tmpFile.deleteOnExit();
	    DataObject output = new DataObject();
	    output.setDataDescriptor(input.getDataDescriptor());
	    output.setFile(tmpFile);
	    inputFile.delete();

	    return output;

	} catch (Exception e) {

	    e.printStackTrace();

	    throw GSException.createException(//
		    GDALNetCDFPostConversionUtils.class, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    GDAL_NETCDF_POST_CONVERSION_ERROR, //
		    e);
	}
    }

    public static DataObject doBandCorrections(DataObject source, DataObject input) throws GSException {
	try {
	    NetcdfDataset sourceDataset = NetcdfDataset.openDataset(source.getFile().getAbsolutePath());
	    NetcdfDataset inputDataset = NetcdfDataset.openDataset(input.getFile().getAbsolutePath());
	    List<Variable> sourceVariables = NetCDFUtils.getGeographicVariables(sourceDataset);
	    Variable sourceVariable = sourceVariables.get(0);
	    List<CoordinateAxis> nonSpatialAxes = new ArrayList<CoordinateAxis>();
	    List<CoordinateAxis> axes = sourceDataset.getCoordinateAxes();
	    for (CoordinateAxis axis : axes) {
		switch (axis.getAxisType()) {
		case GeoX:
		case GeoY:
		case Lat:
		case Lon:
		    break;
		default:
		    nonSpatialAxes.add(axis);
		}
	    }

	    CoordinateAxis otherDimension = null;
	    // additional dimensions and variables to be copied, e.g.temporal bonds
	    // XTIME_bnds(TXTIM, bnds)
	    HashSet<String> additionalDimensionNames = new HashSet<String>();
	    List<Dimension> additionalDimensions = new ArrayList<Dimension>();
	    List<Variable> additionalVariables = new ArrayList<Variable>();
	    List<Variable> bands = NetCDFUtils.getGeographicVariables(inputDataset);
	    HashSet<String> bandNames = new HashSet<>();
	    for (Variable band : bands) {
		bandNames.add(band.getFullName());
	    }
	    List<Variable> sourceBands = NetCDFUtils.getGeographicVariables(sourceDataset);
	    HashSet<String> sourceBandNames = new HashSet<>();
	    for (Variable sourceBand : sourceBands) {
		sourceBandNames.add(sourceBand.getFullName());
	    }
	    if (!axes.isEmpty()) {
		otherDimension = axes.get(0);
		main: for (Variable v : sourceDataset.getVariables()) {
		    List<Dimension> dimensions = v.getDimensions();
		    for (Dimension dimension : dimensions) {
			if (dimension.getShortName().equals(otherDimension.getShortName())) {
			    if (sourceBandNames.contains(v.getFullName())) {
				continue main;
			    }
			    for (Dimension d : dimensions) {
				if (!additionalDimensionNames.contains(d.getShortName())) {
				    additionalDimensions.add(d);
				    additionalDimensionNames.add(d.getShortName());
				}
			    }
			    additionalVariables.add(v);
			}
		    }
		}
	    }

	    File tmpFile = File.createTempFile(GDALNetCDFPostConversionUtils.class.getSimpleName() + "doBandCorrections", ".nc");
	    tmpFile.deleteOnExit();

	    if (bands.size() > 1) {

		NetcdfFileWriter writer = NetcdfFileWriter.createNew(Version.netcdf3, tmpFile.getAbsolutePath());

		// global attributes
		for (Attribute globalAttribute : inputDataset.getGlobalAttributes()) {
		    String name = globalAttribute.getShortName();
		    if (name.equals("history")) {
			globalAttribute = new Attribute("history",
				globalAttribute.getStringValue() + "\n ESSI-Lab " + GDALNetCDFPostConversionUtils.class.getSimpleName());
		    }
		    writer.addGroupAttribute(null, globalAttribute);
		}

		// dimensions
		HashSet<String> newDimensions = new HashSet<String>();
		for (Dimension dimension : inputDataset.getDimensions()) {
		    writer.addDimension(null, dimension.getShortName(), dimension.getLength());
		    newDimensions.add(dimension.getShortName());
		}

		// band dimension (because of GDAL splitting of bands in separate variables)
		String bandName = otherDimension != null ? otherDimension.getShortName() : "gdalband";
		writer.addDimension(null, bandName, bands.size());
		newDimensions.add(bandName);
		// additional dimensions
		for (Dimension dimension : additionalDimensions) {
		    if (!newDimensions.contains(dimension.getShortName())) {
			writer.addDimension(null, dimension.getShortName(), dimension.getLength());
		    }
		}

		// variables
		HashMap<String, Variable> newVariables = new HashMap<>();
		for (Variable variable : inputDataset.getVariables()) {
		    String name = variable.getShortName();
		    if (!bandNames.contains(name)) {
			Variable newVariable = writer.addVariable(null, name, variable.getDataType(), variable.getDimensionsString());
			for (Attribute attribute : variable.getAttributes()) {
			    Attribute newAttribute = new Attribute(attribute.getShortName(), attribute);
			    newVariable.addAttribute(newAttribute);
			}
			newVariables.put(name, newVariable);
		    }
		}
		DataType dataType = otherDimension == null ? DataType.INT : otherDimension.getDataType();
		// band variable (because of GDAL splitting of bands in separate variables)
		Variable bandVariable = writer.addVariable(null, bandName, dataType, bandName);
		newVariables.put(bandName, bandVariable);

		for (Variable variable : additionalVariables) {
		    if (newVariables.get(variable.getShortName()) == null) {
			Variable newVariable = writer.addVariable(null, variable.getShortName(), variable.getDataType(),
				variable.getDimensionsString());
			for (Attribute attribute : variable.getAttributes()) {
			    Attribute newAttribute = new Attribute(attribute.getShortName(), attribute);
			    newVariable.addAttribute(newAttribute);
			}
			newVariables.put(variable.getShortName(), newVariable);
		    }

		}

		Variable dataVariable = writer.addVariable(null, sourceVariable.getShortName(), bands.get(0).getDataType(),
			bandName + " " + bands.get(0).getDimensionsString());
		for (Attribute attribute : bands.get(0).getAttributes()) {
		    Attribute newAttribute = new Attribute(attribute.getShortName(), attribute);
		    dataVariable.addAttribute(newAttribute);
		}

		// for (Variable inputBand : inputBands) {
		//
		// }

		writer.create();

		// finished definition, start writing of data
		for (Variable variable : inputDataset.getVariables()) {
		    String name = variable.getShortName();
		    if (!bandNames.contains(name)) {
			Variable newVariable = newVariables.get(name);
			writer.write(newVariable, variable.read());
		    }
		}
		//
		for (Variable variable : additionalVariables) {
		    String name = variable.getShortName();
		    Variable newVariable = newVariables.get(name);
		    writer.write(newVariable, variable.read());

		}
		// writing to band variable
		if (otherDimension == null) {
		    ArrayInt.D1 bandValues = new ArrayInt.D1(bands.size(),false);
		    for (int i = 0; i < bands.size(); i++) {
			bandValues.set(i, i);
		    }
		    writer.write(bandVariable, bandValues);
		} else {
		    writer.write(bandVariable, otherDimension.read());
		}

		// writing to data variable
		int[] shape = dataVariable.getShape();
		Array dataValues = Array.factory(bands.get(0).getDataType(), shape);
		for (int i = 0; i < bands.size(); i++) {
		    Variable band = bands.get(i);
		    Array array = band.read();
		    long size = array.getSize();
		    Array.arraycopy(array, 0, dataValues, (int) (i * size), (int) size);
		}
		writer.write(dataVariable, dataValues);
		writer.close();

		DataObject ret = new DataObject();
		ret.setDataDescriptor(input.getDataDescriptor());
		ret.setFile(tmpFile);
		inputDataset.close();
		sourceDataset.close();
		return ret;

	    } else {
		FileInputStream fis = new FileInputStream(input.getFile());
		FileOutputStream fos = new FileOutputStream(tmpFile);
		IOUtils.copy(fis, fos);
		input.setFile(tmpFile);
		inputDataset.close();
		sourceDataset.close();
		fis.close();
		fos.close();
		return input;
	    }

	} catch (Exception e) {

	    e.printStackTrace();

	    throw GSException.createException(//
		    GDALNetCDFPostConversionUtils.class, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    GDAL_NETCDF_POST_CONVERSION_ERROR, //
		    e);
	}
    }

}
