package eu.essi_lab.workflow.processor.grid;

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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
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

	    HashMap<String, Variable> sourceBands = new HashMap<>();
	    if (source != null) {

		DataDescriptor sourceDescriptor = source.getDataDescriptor();
		if (sourceDescriptor != null) {
		    DataFormat format = sourceDescriptor.getDataFormat();
		    if (format != null) {
			if (format.equals(DataFormat.NETCDF()) || format.isSubTypeOf(DataFormat.NETCDF())) {

			    File inputFile = source.getFile();
			    NetcdfDataset inputReader = NetcdfDataset.openDataset(inputFile.getAbsolutePath());
			    List<Variable> bands = NetCDFUtils.getGeographicVariables(inputReader);
			    for (Variable band : bands) {
				sourceBands.put(band.getShortName(), band);
			    }
			    inputReader.close();
			}
		    }
		}
	    }

	    if (sourceBands.isEmpty()) {
		return input;
	    }

	    File inputFile = input.getFile();
	    NetcdfDataset reader = NetcdfDataset.openDataset(inputFile.getAbsolutePath());

	    File tmpFile = File.createTempFile("GDAL_To_NetCDF_Processor", ".nc");
	    tmpFile.deleteOnExit();
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
		if (sourceBands.containsKey(name)) {
		    Variable sourceBand = sourceBands.get(name);
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
		    GDAL_NETCDF_POST_CONVERSION_ERROR,//
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
		// for (Variable band : bands) {
		// Number min = null;
		// Number max = null;
		//
		// Array array = band.read();
		// switch (array.getDataType()) {
		// default:
		// case DOUBLE:
		// Double minDouble = null;
		// Double maxDouble = null;
		// for (int j = 0; j < array.getSize(); j++) {
		// double d = array.getDouble(j);
		// if (minDouble == null || d < minDouble) {
		// minDouble = d;
		// }
		// if (maxDouble == null || d > maxDouble) {
		// maxDouble = d;
		// }
		// }
		// min = minDouble;
		// max = maxDouble;
		// break;
		// case SHORT:
		// Short minInteger = null;
		// Short maxInteger = null;
		// for (int j = 0; j < array.getSize(); j++) {
		// short s = array.getShort(j);
		// if (minInteger == null || s < minInteger) {
		// minInteger = s;
		// }
		// if (maxInteger == null || s > maxInteger) {
		// maxInteger = s;
		// }
		// }
		// min = minInteger;
		// max = maxInteger;
		// break;
		// }
		// if (min != null && max != null) {
		// bandToMinMax.put(band.getShortName(), new SimpleEntry<Number, Number>(min, max));
		// }
		// }
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
		    GDAL_NETCDF_POST_CONVERSION_ERROR,//
		    e);
	}
    }

    public static DataObject doBandCorrections(DataObject output) throws GSException {
	try {

	    File tmpFile = File.createTempFile(GDALNetCDFPostConversionUtils.class.getSimpleName() + "doBandCorrections", ".nc");
	    tmpFile.deleteOnExit();

	    File outputFile = output.getFile();
	    NetcdfDataset reader = NetcdfDataset.openDataset(outputFile.getAbsolutePath());

	    List<Variable> bands = NetCDFUtils.getGeographicVariables(reader);

	    if (bands.size() > 1) {
		List<String> bandNames = new ArrayList<>();
		for (Variable band : bands) {
		    bandNames.add(band.getFullName());
		}
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
		// band dimension (because of GDAL splitting of bands in separate variables)
		String bandName = "gdalband";
		writer.addDimension(null, bandName, bands.size());

		// variables
		HashMap<String, Variable> newVariables = new HashMap<>();
		for (Variable variable : reader.getVariables()) {
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
		// band variable (because of GDAL splitting of bands in separate variables)
		Variable bandVariable = writer.addVariable(null, bandName, DataType.INT, bandName);

		Variable dataVariable = writer.addVariable(null, "gdaldata", bands.get(0).getDataType(),
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
		for (Variable variable : reader.getVariables()) {
		    String name = variable.getShortName();
		    if (!bandNames.contains(name)) {
			Variable newVariable = newVariables.get(name);
			writer.write(newVariable, variable.read());
		    }
		}
		// writing to band variable
		ArrayInt.D1 bandValues = new ArrayInt.D1(bands.size());
		for (int i = 0; i < bands.size(); i++) {
		    bandValues.set(i, i);
		}
		writer.write(bandVariable, bandValues);

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
		ret.setDataDescriptor(output.getDataDescriptor());
		ret.setFile(tmpFile);
		reader.close();
		outputFile.delete();
		return ret;

	    } else {
		FileInputStream fis = new FileInputStream(outputFile);
		FileOutputStream fos = new FileOutputStream(tmpFile);
		IOUtils.copy(fis, fos);
		output.setFile(tmpFile);
		reader.close();
		fis.close();
		fos.close();
		outputFile.delete();
		return output;
	    }

	} catch (Exception e) {
	    
	    e.printStackTrace();
	    
	    throw GSException.createException(//
		    GDALNetCDFPostConversionUtils.class, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    GDAL_NETCDF_POST_CONVERSION_ERROR,//
		    e);
	}
    }

}
