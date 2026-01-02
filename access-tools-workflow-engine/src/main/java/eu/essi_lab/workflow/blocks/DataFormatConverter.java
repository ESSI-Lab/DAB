package eu.essi_lab.workflow.blocks;

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

import java.util.Arrays;

import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.workflow.builder.WorkblockBuilder;
import eu.essi_lab.workflow.processor.CapabilityElement;
import eu.essi_lab.workflow.processor.ResamplingCapability;
import eu.essi_lab.workflow.processor.SubsettingCapability;

/**
 * @author Fabrizio
 */
public abstract class DataFormatConverter extends WorkblockBuilder {

    private DataType type;
    private CRS crs;
    private DataFormat inputFormat;
    private DataFormat outputFormat;

    public DataFormatConverter(//
	    DataType type, //
	    CRS crs, //
	    DataFormat inputFormat, //
	    DataFormat outputFormat) {

	this.type = type;
	this.inputFormat = inputFormat;
	this.crs = crs;
	this.outputFormat = outputFormat;
    }

    @Override
    public void init()  {

	setInputType(CapabilityElement.sameAsFromDataType(type));

	setOutputType(CapabilityElement.sameAsFromDataType(type));

	setFormat(//
		CapabilityElement.anyFromDataFormat(Arrays.asList(inputFormat)),
		CapabilityElement.anyFromDataFormat(Arrays.asList(outputFormat)));

	setCRS(//
		CapabilityElement.sameAsFromCRS(Arrays.asList(crs)), //
		CapabilityElement.sameAsFromCRS(Arrays.asList(crs)));

	setSubsetting(//
		SubsettingCapability.SAME_AS_SUBSETTING(), //
		SubsettingCapability.SAME_AS_SUBSETTING());

	setResampling(//
		ResamplingCapability.SAME_AS_RESAMPLING(), //
		ResamplingCapability.SAME_AS_RESAMPLING());

    }

}
