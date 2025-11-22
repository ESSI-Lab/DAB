/**
 * 
 */
package eu.essi_lab.messages;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.model.RuntimeInfoElement;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.DimensionType;
import eu.essi_lab.model.resource.data.dimension.DataDimension;

/**
 * @author Fabrizio
 */
public class DataDescriptorRuntimeInfo {

    enum TargetProvider {

	ACCESS_MESSAGE, //
	RESULT_SET//
    }

    /**
     * @param descriptor
     * @param map
     */
    static void publishDataDescriptorInfo(TargetProvider provider, DataDescriptor descriptor, HashMap<String, List<String>> map) {

	if (descriptor == null) {
	    GSLoggerFactory.getLogger(DataDescriptorRuntimeInfo.class).warn("No output descriptor");
	    return;
	}

	CRS crs = descriptor.getCRS();
	if (crs != null) {
	    String identifier = crs.getIdentifier();
	    if (StringUtils.isNotEmptyAndNotNull(identifier)) {

		RuntimeInfoElement el = provider == TargetProvider.ACCESS_MESSAGE ? //
			RuntimeInfoElement.ACCESS_MESSAGE_CRS : //
			RuntimeInfoElement.RESULT_SET_CRS;

		map.put(el.getName(), Arrays.asList(identifier));
	    }
	}

	DataFormat dataFormat = descriptor.getDataFormat();
	if (dataFormat != null) {
	    String identifier = dataFormat.getIdentifier();
	    if (StringUtils.isNotEmptyAndNotNull(identifier)) {

		RuntimeInfoElement el = provider == TargetProvider.ACCESS_MESSAGE ? //
			RuntimeInfoElement.ACCESS_MESSAGE_DATA_FORMAT : //
			RuntimeInfoElement.RESULT_SET_DATA_FORMAT;

		map.put(el.getName(), Arrays.asList(identifier));
	    }
	}

	DataType dataType = descriptor.getDataType();
	if (dataType != null) {

	    RuntimeInfoElement el = provider == TargetProvider.ACCESS_MESSAGE ? //
		    RuntimeInfoElement.ACCESS_MESSAGE_DATA_TYPE : //
		    RuntimeInfoElement.RESULT_SET_DATA_TYPE;

	    map.put(el.getName(), Arrays.asList(dataType.name()));
	}

	descriptor.getSpatialDimensions().forEach(dim -> {

	    String name = dim.getName();
	    DimensionType type = dim.getType();
	    String identifier = null;
	    if (type != null) {
		identifier = type.getIdentifier();
	    }

	    if (StringUtils.isNotEmptyAndNotNull(name)) {

		RuntimeInfoElement el = provider == TargetProvider.ACCESS_MESSAGE ? //
		RuntimeInfoElement.ACCESS_MESSAGE_SPATIAL_DIMENSION_NAME : //
		RuntimeInfoElement.RESULT_SET_SPATIAL_DIMENSION_NAME;

		List<String> list = map.computeIfAbsent(el.getName(), k -> new ArrayList<>());

		list.add(name);
	    }

	    if (StringUtils.isNotEmptyAndNotNull(identifier)) {

		RuntimeInfoElement el = provider == TargetProvider.ACCESS_MESSAGE ? //
		RuntimeInfoElement.ACCESS_MESSAGE_SPATIAL_DIMENSION_ID : //
		RuntimeInfoElement.RESULT_SET_SPATIAL_DIMENSION_ID;

		List<String> list = map.computeIfAbsent(el.getName(), k -> new ArrayList<>());

		list.add(identifier);
	    }
	});

	DataDimension temporalDimension = descriptor.getTemporalDimension();
	if (temporalDimension != null) {

	    String name = temporalDimension.getName();
	    String identifier = null;
	    DimensionType type = temporalDimension.getType();
	    if (type != null) {
		identifier = type.getIdentifier();
	    }

	    if (StringUtils.isNotEmptyAndNotNull(name)) {

		RuntimeInfoElement nameEl = provider == TargetProvider.ACCESS_MESSAGE ? //
			RuntimeInfoElement.ACCESS_MESSAGE_TEMPORAL_DIMENSION_NAME : //
			RuntimeInfoElement.RESULT_SET_TEMPORAL_DIMENSION_NAME;

		map.put(nameEl.getName(), Arrays.asList(name));
	    }

	    if (StringUtils.isNotEmptyAndNotNull(identifier)) {

		RuntimeInfoElement idEl = provider == TargetProvider.ACCESS_MESSAGE ? //
			RuntimeInfoElement.ACCESS_MESSAGE_TEMPORAL_DIMENSION_ID : //
			RuntimeInfoElement.RESULT_SET_TEMPORAL_DIMENSION_ID;

		map.put(idEl.getName(), Arrays.asList(identifier));
	    }
	}
    }
}
