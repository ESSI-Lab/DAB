package eu.essi_lab.workflow.blocks.grid;

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

import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.workflow.blocks.DataFormatConverter;
import eu.essi_lab.workflow.processor.DataProcessor;
import eu.essi_lab.workflow.processor.grid.NetCDF_To_DDS_FormatConverterProcessor;

/**
 * @author boldrini
 */
public class NetCDF_To_DAS_FormatConverter extends DataFormatConverter {

    public NetCDF_To_DAS_FormatConverter() {
	super(DataType.GRID, //
		CRS.GDAL_ALL(), //
		DataFormat.NETCDF(), // INPUT FORMAT
		DataFormat.DDS() // OUTPUT FORMAT
	);
    }

    @Override
    protected DataProcessor createProcessor() {

	return new NetCDF_To_DDS_FormatConverterProcessor();
    }
}
