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
import org.gdal.gdal.WarpOptions;
import org.gdal.gdal.gdal;
import org.slf4j.Logger;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.model.resource.data.Authority;
import eu.essi_lab.model.resource.data.AxisOrder;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.DataDimension;
import eu.essi_lab.workflow.processor.DataProcessor;
import eu.essi_lab.workflow.processor.TargetHandler;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

/**
 * Using GDAL, this processor converts CRS of the given NetCDF
 * 
 * @author boldrini
 */
public class GDAL_NetCDF_CRS_Converter_Processor extends DataProcessor {

    private static Logger logger = GSLoggerFactory.getLogger(GDAL_NetCDF_CRS_Converter_Processor.class);

    @Override
    public DataObject process(DataObject dataObject, TargetHandler handler) throws Exception {
	File inputFile = dataObject.getFile();
	File outputFile = File.createTempFile(getClass().getSimpleName(), ".nc");
	outputFile.deleteOnExit();
	DataObject ret = new DataObject();

	DataDescriptor inputDescriptor = dataObject.getDataDescriptor();
	DataDescriptor outputDescriptor = inputDescriptor.clone();

	NetcdfDataset dataset = NetcdfDataset.openDataset(inputFile.getAbsolutePath());
	List<Variable> variables = dataset.getVariables();
	String fillValue = null;
	for (Variable variable : variables) {
	    Attribute fillAttribute = variable.findAttribute("_FillValue");
	    if (fillAttribute != null) {
		if (fillAttribute.getLength() > 0) {
		    fillValue = "" + fillAttribute.getValue(0);
		}
		break;
	    }
	}
	dataset.close();

	Vector<String> vector = new Vector<String>(20, 20);
	vector.add("-of");
	vector.add("netCDF");

	CRS sourceCRS = handler.getCurrentCRS();

	if (sourceCRS == null) {
	    sourceCRS = dataObject.getDataDescriptor().getCRS();
	}
	if (sourceCRS != null) {
	    String crs = sourceCRS.getIdentifier();
	    if (sourceCRS.getAuthority() != null && sourceCRS.getAuthority().equals(Authority.EPSG)) {
		crs = "EPSG:" + sourceCRS.getCode();
	    }
	    if (crs == null || crs.equals("")) {
		crs = sourceCRS.getWkt();
	    }
	    vector.add("-s_srs");
	    vector.add(crs);
	}

	if (fillValue == null) {
	    fillValue = "-9999.0";
	}
	vector.add("-dstnodata");
	vector.add(fillValue);

	CRS targetCRS = handler.getTargetCRS();
	if (targetCRS == null) {
	    targetCRS = handler.getCurrentCRS();
	}
	if (targetCRS == null) {
	    targetCRS = dataObject.getDataDescriptor().getCRS();
	}
	String crs = targetCRS.getIdentifier();
	if (targetCRS.getAuthority() != null && targetCRS.getAuthority().equals(Authority.EPSG)) {
	    crs = "EPSG:" + targetCRS.getCode();
	}
	if (crs == null || crs.equals("")) {
	    crs = targetCRS.getWkt();
	}
	vector.add("-t_srs");
	vector.add(crs);
	outputDescriptor.setCRS(targetCRS);
	List<DataDimension> spatialDimensions = handler.getTargetSpatialDimensions();
	outputDescriptor.setSpatialDimensions(spatialDimensions);
	if (spatialDimensions != null && !spatialDimensions.isEmpty()) {
	    ContinueDimension cd1 = spatialDimensions.get(0).getContinueDimension();
	    ContinueDimension cd2 = spatialDimensions.get(1).getContinueDimension();
	    if (cd1 != null && cd2 != null) {
		boolean neAxisOrder = targetCRS != null && targetCRS.getAxisOrder().equals(AxisOrder.NORTH_EAST);
		Number res1 = cd1.getResolution();
		Number res2 = cd2.getResolution();
		Number lower1 = cd1.getLower();
		Number upper1 = cd1.getUpper();
		Number lower2 = cd2.getLower();
		Number upper2 = cd2.getUpper();
		Long size1 = cd1.getSize();
		Long size2 = cd2.getSize();
		if (res1 == null) {
		    if (lower1 != null && upper1 != null && size1 != null) {
			double extent = upper1.doubleValue() - lower1.doubleValue();
			res1 = extent / (size1 - 1);
		    }
		}
		if (res2 == null) {
		    if (lower2 != null && upper2 != null && size2 != null) {
			double extent = upper2.doubleValue() - lower2.doubleValue();
			res2 = extent / (size2 - 1);
		    }
		}

		if (res1 != null && res2 != null) {
		    vector.add("-tr");
		    if (neAxisOrder) {
			vector.add("" + res2.doubleValue());
			vector.add("" + res1.doubleValue());
		    } else {
			vector.add("" + res1.doubleValue());
			vector.add("" + res2.doubleValue());
		    }

		} else {
		    if (size1 != null && size2 != null) {
			vector.add("-ts");
			if (neAxisOrder) {
			    vector.add("" + size2.longValue());
			    vector.add("" + size1.longValue());
			} else {
			    vector.add("" + size1.longValue());
			    vector.add("" + size2.longValue());
			}
		    }
		}

		if (lower1 != null && lower2 != null && upper1 != null && upper2 != null) {
		    if (res1 != null && res2 != null) {
			// the -te parameter considers the total envelope, while our datamodel is based on center grid
			// points.
			// half the resolution is added to the exterior grid points to calculate the total envelope
			double res1_2 = res1.doubleValue() / 2.0;
			lower1 = lower1.doubleValue() - res1_2;
			upper1 = upper1.doubleValue() + res1_2;
			double res2_2 = res2.doubleValue() / 2.0;
			lower2 = lower2.doubleValue() - res2_2;
			upper2 = upper2.doubleValue() + res2_2;
		    }
		    vector.add("-te");
		    if (neAxisOrder) {
			vector.add("" + lower2.doubleValue());
			vector.add("" + lower1.doubleValue());
			vector.add("" + upper2.doubleValue());
			vector.add("" + upper1.doubleValue());
		    } else {
			vector.add("" + lower1.doubleValue());
			vector.add("" + lower2.doubleValue());
			vector.add("" + upper1.doubleValue());
			vector.add("" + upper2.doubleValue());
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
	    executeWithRuntime(inputFile.getAbsolutePath(), outputFile.getAbsolutePath(), vector);
	    break;
	}

	ret.setFile(outputFile);

	ret.setDataDescriptor(outputDescriptor);
	// needed to group the multi variables created by GDAL
	ret = GDALNetCDFPostConversionUtils.doBandCorrections(ret);
	// copy attributes, because GDAL removes them after transformation from NetCDF to NetCDF
	ret = GDALNetCDFPostConversionUtils.copyAttributes(dataObject, ret);

	return ret;
    }

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
	GSLoggerFactory.getLogger(GDAL_NetCDF_CRS_Converter_Processor.class).info(vector.toString());

	logger.info("Executing GDAL JNI Warp: " + vector.toString());

	gdal.Warp(outputPath, new Dataset[] { dataset }, new WarpOptions(vector));
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
	String command = "gdalwarp " + inputPath + " " + options + " " + outputPath;
	Process ps = rt.exec(command);

	int exitVal = ps.waitFor();

	if (exitVal > 0) {

	    GSLoggerFactory.getLogger(GDAL_NetCDF_CRS_Converter_Processor.class).error(IOStreamUtils.asUTF8String(ps.getErrorStream()));

	}

	logger.info("Executing GDAL Runtime: " + command);

    }

}
