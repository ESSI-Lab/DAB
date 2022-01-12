package eu.essi_lab.workflow.blocks;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import eu.essi_lab.workflow.processor.ResamplingCapability;
import eu.essi_lab.workflow.processor.SubsettingCapability;
public abstract class TemporalSubsetter extends WorkblockBuilder {

    private DataType type;
    private DataFormat format;
    private CRS crs;

    public TemporalSubsetter(DataType type, DataFormat format, CRS crs) {

	this.type = type;
	this.format = format;
	this.crs = crs;
    }

    @Override
    public void init()  {

	setInputType(CapabilityElement.sameAsFromDataType(type));

	setOutputType(CapabilityElement.sameAsFromDataType(type));

	setFormat(//
		CapabilityElement.sameAsFromDataFormat(Arrays.asList(format)), //
		CapabilityElement.sameAsFromDataFormat(Arrays.asList(format)));//

	setCRS(//
		CapabilityElement.sameAsFromCRS(Arrays.asList(crs)), //
		CapabilityElement.sameAsFromCRS(Arrays.asList(crs)));//

	setSubsetting(

		new SubsettingCapability(//
			new BooleanCapabilityElement(PresenceType.SAME_AS), //
			new BooleanCapabilityElement(PresenceType.ANY), //
			new BooleanCapabilityElement(PresenceType.SAME_AS)),

		new SubsettingCapability(//
			new BooleanCapabilityElement(PresenceType.SAME_AS), //
			new BooleanCapabilityElement(true), //
			new BooleanCapabilityElement(PresenceType.SAME_AS)));

	setResampling(

		ResamplingCapability.SAME_AS_RESAMPLING(),

		ResamplingCapability.SAME_AS_RESAMPLING());

    }

}
