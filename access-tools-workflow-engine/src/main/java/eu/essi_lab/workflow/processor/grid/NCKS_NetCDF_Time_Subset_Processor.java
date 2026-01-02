package eu.essi_lab.workflow.processor.grid;

import java.io.BufferedReader;

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

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.model.resource.data.dimension.DataDimension;
import eu.essi_lab.netcdf.timeseries.NetCDFUtils;
import eu.essi_lab.workflow.processor.DataProcessor;
import eu.essi_lab.workflow.processor.TargetHandler;
import ucar.nc2.constants.AxisType;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.time.CalendarDate;
import ucar.nc2.time.CalendarDateUnit;

/**
 * Using NCKS, this processor subsets along the time dimension a given NetCDF
 * 
 * @author boldrini
 */
public class NCKS_NetCDF_Time_Subset_Processor extends DataProcessor {

    private static Logger logger = GSLoggerFactory.getLogger(NCKS_NetCDF_Time_Subset_Processor.class);

    private static HashMap<Integer, DataObject> cache = new HashMap<Integer, DataObject>();

    @Override
    public DataObject process(GSResource resource, DataObject dataObject, TargetHandler handler) throws Exception {

	File outputFile = File.createTempFile(getClass().getSimpleName(), ".nc");
	outputFile.delete();

	if (resource != null && resource.getSource().getEndpoint().contains("i-change")) {
	    String targetDimension = "time-subset-"+handler.getTargetTemporalDimension().toString();
	    int hash = targetDimension.hashCode();
	    DataObject ret = cache.get(hash);	    
	    if (ret != null&& ret.getFile().exists()) {
		return ret;
	    }
	    String tempDir = System.getProperty("java.io.tmpdir");
	    File tempFile = new File(tempDir);
	    outputFile = new File(tempFile, "i-change" + hash + ".nc");
	    if (outputFile.exists()) {
	    	outputFile.delete();
	    }
	}

	GSLoggerFactory.getLogger(getClass()).info("Starting temporal subset");
	File inputFile = dataObject.getFile();

	DataObject ret = new DataObject();

	DataDescriptor inputDescriptor = dataObject.getDataDescriptor();
	DataDescriptor outputDescriptor = inputDescriptor.clone();

	NetcdfDataset dataset = NetcdfDataset.openDataset(inputFile.getAbsolutePath());

	CoordinateAxis1D timeAxis = NetCDFUtils.getAxis(dataset, AxisType.Time);

	String timeSubset = "";
	if (timeAxis != null) {
	    int start = 0;
	    int end = 0;

	    DataDimension targetTemporal = handler.getTargetTemporalDimension();

	    Number l = targetTemporal.getContinueDimension().getLower();
	    Number u = targetTemporal.getContinueDimension().getUpper();

	    if (l != null && u != null) {
		long lower = targetTemporal.getContinueDimension().getLower().longValue();
		long upper = targetTemporal.getContinueDimension().getUpper().longValue();

		double i = getIndex(timeAxis, lower);
		double j = getIndex(timeAxis, upper);

		// dateUnit.m
		//
		// // Find the closest index on the axis

		start = timeAxis.findCoordElement(i);
		end = timeAxis.findCoordElement(j);
		timeSubset = timeAxis.getShortName() + "," + start + "," + end;
	    }

	}

	dataset.close();

	executeWithRuntime("ncks", "-d", timeSubset, inputFile.getAbsolutePath(), outputFile.getAbsolutePath());
	outputFile.deleteOnExit();

	ret.setFile(outputFile);

	ret.setDataDescriptor(outputDescriptor);
	GSLoggerFactory.getLogger(getClass()).info("Ended temporal subset");
	if (resource != null && resource.getSource().getEndpoint().contains("i-change")) {
	    String targetDimension = "time-subset-"+handler.getTargetTemporalDimension().toString();
	    int hash = targetDimension.hashCode();
	    cache.put(hash, ret);
	}
	return ret;
    }

    private double getIndex(CoordinateAxis1D timeAxis, long value) {
	String unitsString = timeAxis.getUnitsString();
	CalendarDateUnit dateUnit = CalendarDateUnit.of("standard", unitsString);
	CalendarDate userCalDate = CalendarDate.of(new Date(value));
	return dateUnit.makeOffsetFromRefDate(userCalDate);
    }

    /**
     * Calls NCKS directly
     * 
     * @param inputPath
     * @param outputPath
     * @param options
     * @throws Exception
     */
    protected static void executeWithRuntime(String... cmd) throws Exception {

	Runtime rt = Runtime.getRuntime();

	String cmdString = "";
	for (String c : cmd) {
	    cmdString += c + " ";
	}
	logger.info("Executing NCKS Runtime: " + cmdString);

	Process ps = rt.exec(cmd);

	// Read output stream
	new Thread(() -> {
	    try (BufferedReader reader = new BufferedReader(new InputStreamReader(ps.getInputStream()))) {
		String line;
		while ((line = reader.readLine()) != null) {
		    logger.info("stdout: " + line);
		}
	    } catch (IOException e) {
		logger.error("Error reading stdout", e);
	    }
	}).start();

	// Read error stream
	new Thread(() -> {
	    try (BufferedReader reader = new BufferedReader(new InputStreamReader(ps.getErrorStream()))) {
		String line;
		while ((line = reader.readLine()) != null) {
		    logger.error("stderr: " + line);
		}
	    } catch (IOException e) {
		logger.error("Error reading stderr", e);
	    }
	}).start();

	logger.info("Waiting");
	int exitVal = ps.waitFor();
	logger.info("Process finished with exit code: " + exitVal);
	logger.info("Executed");

    }

    public static void main(String[] args) throws Exception {
	executeWithRuntime("ncks", "-d", "XTIME,11", "/tmp/input.nc", "/tmp/output.nc");
    }

}
