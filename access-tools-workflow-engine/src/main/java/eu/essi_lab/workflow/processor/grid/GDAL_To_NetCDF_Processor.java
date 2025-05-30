package eu.essi_lab.workflow.processor.grid;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataObject;

/**
 * Using GDAL, this processor converts from a supported format to NetCDF
 * 
 * @author boldrini
 */
public class GDAL_To_NetCDF_Processor extends GDALFormatConverterProcessor {

    @Override
    public String getExtension() {
	return ".nc";
    }

    @Override
    public String getOutputFormat() {
	return "netCDF";
    }

    @Override
    public DataObject postProcessCorrections(DataObject inputData, DataObject outputData) throws GSException {
	outputData = GDALNetCDFPostConversionUtils.doBandCorrections(inputData, outputData);
	// add min and max values
	DataDescriptor inputDescriptor = inputData == null ? null : inputData.getDataDescriptor();
	outputData = GDALNetCDFPostConversionUtils.addMinMax(inputDescriptor, outputData);
	return outputData;

    }

}
