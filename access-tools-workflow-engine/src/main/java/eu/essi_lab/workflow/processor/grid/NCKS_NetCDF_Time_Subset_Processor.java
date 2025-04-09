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

import org.slf4j.Logger;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.model.resource.data.dimension.DataDimension;
import eu.essi_lab.netcdf.timeseries.NetCDFUtils;
import eu.essi_lab.workflow.processor.DataProcessor;
import eu.essi_lab.workflow.processor.TargetHandler;
import ucar.nc2.constants.AxisType;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.NetcdfDataset;

/**
 * Using NCKS, this processor subsets along the time dimension a given NetCDF
 * 
 * @author boldrini
 */
public class NCKS_NetCDF_Time_Subset_Processor extends DataProcessor {

	private static Logger logger = GSLoggerFactory.getLogger(NCKS_NetCDF_Time_Subset_Processor.class);

	@Override
	public DataObject process(DataObject dataObject, TargetHandler handler) throws Exception {
		File inputFile = dataObject.getFile();
		File outputFile = File.createTempFile(getClass().getSimpleName(), ".nc");
		outputFile.deleteOnExit();
		DataObject ret = new DataObject();

		DataDescriptor inputDescriptor = dataObject.getDataDescriptor();
		DataDescriptor outputDescriptor = inputDescriptor.clone();

		NetcdfDataset dataset = NetcdfDataset.openDataset(inputFile.getAbsolutePath());
		
		CoordinateAxis1D timeAxis = NetCDFUtils.getAxis(dataset, AxisType.Time);
		
		dataset.close();
		
		String options = "";
		
		if (timeAxis!=null) {
			int start = 0;
			int end = 0;

			DataDimension targetTemporal = handler.getTargetTemporalDimension();

			long lower = targetTemporal.getContinueDimension().getLower().longValue();
			long upper = targetTemporal.getContinueDimension().getLower().longValue();
			
			start = timeAxis.findCoordElement(lower);
			end = timeAxis.findCoordElement(upper);
			options = "-d time," + start + "," + end + " ";
			
		}
				
		executeWithRuntime(inputFile.getAbsolutePath(), outputFile.getAbsolutePath(), options);

		ret.setFile(outputFile);

		ret.setDataDescriptor(outputDescriptor);

		return ret;
	}

	/**
	 * Calls NCKS directly
	 * 
	 * @param inputPath
	 * @param outputPath
	 * @param options
	 * @throws Exception
	 */
	protected static void executeWithRuntime(String inputPath, String outputPath, String options) throws Exception {

		Runtime rt = Runtime.getRuntime();

		if (options == null) {
			options = "";
		}

		String command = "ncks " + options + inputPath + " " + outputPath;

		logger.info("Executing NCKS Runtime: " + command);

		Process ps = rt.exec(command);

		int exitVal = ps.waitFor();

		if (exitVal > 0) {

			GSLoggerFactory.getLogger(NCKS_NetCDF_Time_Subset_Processor.class)
					.error(IOStreamUtils.asUTF8String(ps.getErrorStream()));

		}

		logger.info("Executed");

	}

}
