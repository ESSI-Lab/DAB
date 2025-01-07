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
import java.util.List;
import java.util.Vector;

import org.gdal.gdal.Dataset;
import org.gdal.gdal.TranslateOptions;
import org.gdal.gdal.gdal;
import org.slf4j.Logger;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.netcdf.timeseries.NetCDFUtils;
import eu.essi_lab.workflow.processor.DataProcessor;
import eu.essi_lab.workflow.processor.TargetHandler;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

public abstract class GDALFormatConverterProcessor extends DataProcessor {

    private static Logger logger = GSLoggerFactory.getLogger(GDALFormatConverterProcessor.class);

    @Override
    public DataObject process(DataObject inputData, TargetHandler handler) throws Exception {
	File inputFile = inputData.getFile();
	File outputFile = File.createTempFile(GDALFormatConverterProcessor.class.getSimpleName() + getOutputFormat(), getExtension());
	outputFile.deleteOnExit();
	DataObject outputData = new DataObject();

	Vector<String> vector = new Vector<String>();
	vector.add("-of");
	vector.add(getOutputFormat());

	String inputName = inputFile.getAbsolutePath();

	DataFormat outputFormat = DataFormat.fromIdentifier(getOutputFormat());

	DataDescriptor inputDescriptor = inputData.getDataDescriptor();
	if (inputDescriptor != null) {
	    DataFormat inputFormat = inputDescriptor.getDataFormat();

	    if (inputFormat.equals(DataFormat.IMAGE_PNG())) {
		CRS crs = inputDescriptor.getCRS();
		vector.add("-a_srs");
		vector.add(crs.getIdentifier());

		Double left = null;
		Double upper = null;
		Double right = null;
		Double lower = null;
		switch (crs.getAxisOrder()) {
		case NORTH_EAST:
		    lower = inputDescriptor.getFirstSpatialDimension().getContinueDimension().getLower().doubleValue();
		    upper = inputDescriptor.getFirstSpatialDimension().getContinueDimension().getUpper().doubleValue();
		    left = inputDescriptor.getSecondSpatialDimension().getContinueDimension().getLower().doubleValue();
		    right = inputDescriptor.getSecondSpatialDimension().getContinueDimension().getUpper().doubleValue();
		    break;
		case EAST_NORTH:
		default:
		    lower = inputDescriptor.getSecondSpatialDimension().getContinueDimension().getLower().doubleValue();
		    upper = inputDescriptor.getSecondSpatialDimension().getContinueDimension().getUpper().doubleValue();
		    left = inputDescriptor.getFirstSpatialDimension().getContinueDimension().getLower().doubleValue();
		    right = inputDescriptor.getFirstSpatialDimension().getContinueDimension().getUpper().doubleValue();
		    break;
		}
		vector.add("-a_ullr");
		vector.add("" + left);
		vector.add("" + upper);
		vector.add("" + right);
		vector.add("" + lower);
	    }

	    Number min = null;
	    Number max = null;
	    if (inputFormat.isSubTypeOf(DataFormat.NETCDF()) || inputFormat.equals(DataFormat.NETCDF())) {

		NetcdfDataset dataset = NetcdfDataset.openDataset(inputFile.getAbsolutePath());
		List<Variable> mainVariables = NetCDFUtils.getGeographicVariables(dataset);

		if (mainVariables != null && !mainVariables.isEmpty()) {

		    switch (mainVariables.size()) {
		    case 1:
			// check if additional dimensions case

			if (outputFormat.equals(DataFormat.IMAGE_PNG()) || //
				outputFormat.equals(DataFormat.IMAGE_JPG())) {

			    Variable mainVariable = mainVariables.get(0);

			    List<Dimension> dimensions = mainVariable.getDimensions();

			    if (dimensions.size() > 2) {

				Dimension nonSpatialDim = dimensions.get(0);

				int l = nonSpatialDim.getLength();
				if (l > 0) {
				    vector.add("-b");
				    vector.add("1");
				}
				if (l > 1) {
				    vector.add("-b");
				    vector.add("2");
				}
				if (l > 2) {
				    vector.add("-b");
				    vector.add("3");
				}
			    }

			}

			break;
		    default:
			// uses the first band, ignore the others
			inputName = "NETCDF:\"" + inputFile.getAbsolutePath() + "\":" + mainVariables.get(0).getShortName();
			break;
		    }
		    for (int i = 0; i < mainVariables.size(); i++) {
			Variable mainVariable = mainVariables.get(i);
			Attribute minAttribute = mainVariable.findAttribute("valid_min");
			Attribute maxAttribute = mainVariable.findAttribute("valid_max");
			if (minAttribute != null && maxAttribute != null) {
			    min = minAttribute.getNumericValue();
			    max = maxAttribute.getNumericValue();
			}
		    }
		    Variable mainVariable = mainVariables.get(0);

		    DataType dataType = mainVariable.getDataType();
		    switch (dataType) {
		    case SHORT:
			vector.add("-ot");
			vector.add("Int16");
			break;
		    default:
			break;
		    }
		}
		dataset.close();
	    }
	    if (inputDescriptor.getRangeMaximum() != null) {
		max = inputDescriptor.getRangeMaximum();
	    }
	    if (inputDescriptor.getRangeMinimum() != null) {
		min = inputDescriptor.getRangeMinimum();
	    }

	    if (scaleOutput()) {

		if (min != null && max != null) {

		    String minString = "" + min;
		    String maxString = "" + max;

		    if (!minString.toLowerCase().contains("nan") && !maxString.toLowerCase().contains("nan")) {
			if (!minString.toLowerCase().contains("inf") && !maxString.toLowerCase().contains("inf")) {
			    vector.add("-scale");
			    vector.add(minString);
			    vector.add(maxString);
			}
		    }
		}
	    }
	}

	switch (GDALConstants.IMPLEMENTATION) {
	case JNI:
	    executeWithJNI(inputFile.getAbsolutePath(), outputFile.getAbsolutePath(), vector);
	    break;
	case RUNTIME:
	default:
	    executeWithRuntime(inputName, outputFile.getAbsolutePath(), vector);
	    break;
	}

	outputData.setFile(outputFile);
	outputData = postProcessCorrections(inputData, outputData);
	DataDescriptor outputDescriptor = inputDescriptor.clone();
	outputDescriptor.setDataFormat(DataFormat.fromIdentifier(getOutputFormat()));
	outputData.setDataDescriptor(outputDescriptor);
	return outputData;
    }

    public boolean scaleOutput() {
	return false;
    }

    /**
     * Sub classes may implement this method to correct possible problems in the output from GDAL. The source data is
     * also passed.
     * 
     * @param ret
     * @throws GSException
     */
    public DataObject postProcessCorrections(DataObject input, DataObject output) throws GSException {
	return output;
    }

    public abstract String getOutputFormat();

    public abstract String getExtension();

    /**
     * Calls GDAL using JNI
     * Unfortunately this method doesn't write NetCDF lat lon dimensions when the result has no data... because of this
     * use executeWithRuntime!
     * 
     * @param inputPath
     * @param outputPath
     * @param vector
     */
    protected static void executeWithJNI(String inputPath, String outputPath, Vector<String> vector) {
	GDALConstants.initJNI();
	Dataset dataset = gdal.Open(inputPath);

	logger.info("Executing GDAL JNI Translate: " + vector.toString());

	gdal.Translate(outputPath, dataset, new TranslateOptions(vector));

    }

    /**
     * Calls GDAL directly
     * 
     * @param inputPath
     * @param outputPath
     * @param vector
     * @throws Exception
     */
    protected static void executeWithRuntime(String inputPath, String outputPath, Vector<String> vector) throws Exception {
	Runtime rt = Runtime.getRuntime();
	String options = "";
	for (String option : vector) {
	    options += option + " ";
	}
	String command = "gdal_translate " + inputPath + " " + options + " " + outputPath;
	Process ps = rt.exec(command);
	int exitVal = ps.waitFor();

	if (exitVal > 0) {

	    GSLoggerFactory.getLogger(GDAL_NetCDF_CRS_Converter_Processor.class).error(IOStreamUtils.asUTF8String(ps.getErrorStream()));

	}

	logger.info("Executing GDAL Runtime: " + command);

    }

}
