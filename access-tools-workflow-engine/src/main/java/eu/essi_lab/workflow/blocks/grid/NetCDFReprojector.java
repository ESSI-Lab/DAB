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

import java.util.Arrays;

import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.workflow.builder.WorkblockBuilder;
import eu.essi_lab.workflow.processor.BooleanCapabilityElement;
import eu.essi_lab.workflow.processor.CapabilityElement;
import eu.essi_lab.workflow.processor.CapabilityElement.PresenceType;
import eu.essi_lab.workflow.processor.DataProcessor;
import eu.essi_lab.workflow.processor.ResamplingCapability;
import eu.essi_lab.workflow.processor.SubsettingCapability;
import eu.essi_lab.workflow.processor.grid.GDAL_NetCDF_CRS_Converter_Processor;

public class NetCDFReprojector extends WorkblockBuilder {

    private DataType type = DataType.GRID;
    private DataFormat format = DataFormat.NETCDF();

    public NetCDFReprojector() {

    }

    @Override
    public void init() {

	setInputType(CapabilityElement.sameAsFromDataType(type));

	setOutputType(CapabilityElement.sameAsFromDataType(type));

	setFormat(//
		CapabilityElement.sameAsFromDataFormat(Arrays.asList(format)), //
		CapabilityElement.sameAsFromDataFormat(Arrays.asList(format)));//

	setCRS( //
		CapabilityElement.anyFromCRS(CRS.GDAL_ALL()), //
		CapabilityElement.anyFromCRS(CRS.GDAL_ALL()));

	setSubsetting(

		new SubsettingCapability(//
			new BooleanCapabilityElement(PresenceType.ANY), //
			new BooleanCapabilityElement(PresenceType.SAME_AS), //
			new BooleanCapabilityElement(PresenceType.SAME_AS)),

		new SubsettingCapability(//
			new BooleanCapabilityElement(true), //
			new BooleanCapabilityElement(PresenceType.SAME_AS), //
			new BooleanCapabilityElement(PresenceType.SAME_AS)));

	setResampling(//

		new ResamplingCapability(//
			new BooleanCapabilityElement(PresenceType.ANY), //
			new BooleanCapabilityElement(PresenceType.SAME_AS), //
			new BooleanCapabilityElement(PresenceType.SAME_AS)),

		new ResamplingCapability(//
			new BooleanCapabilityElement(true), //
			new BooleanCapabilityElement(PresenceType.SAME_AS), //
			new BooleanCapabilityElement(PresenceType.SAME_AS)));

    }

    @Override
    protected DataProcessor createProcessor() {
	return new GDAL_NetCDF_CRS_Converter_Processor();
    }

}
