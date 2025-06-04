package eu.essi_lab.pdk.rsf;

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

import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import eu.essi_lab.messages.AccessMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataObject;

/**
 * @author boldrini
 */
public class AccessResultSetFormatterAttachment extends AccessResultSetFormatterInline {

    @Override
    public Response format(AccessMessage message, ResultSet<DataObject> mappedResultSet) throws GSException {

	List<ErrorInfo> errors = mappedResultSet.getException().getErrorInfoList();
	if (!errors.isEmpty()) {

	    return handleException(message, mappedResultSet, errors);
	}

	DataObject dataObject = mappedResultSet.getResultsList().get(0);

	ResponseBuilder builder = getBuilder(dataObject);

	String onlineId = message.getOnlineId();

	String extension = ".bin";

	DataFormat dataFormat = dataObject.getDataDescriptor().getDataFormat();

	if (dataFormat.equals(DataFormat.O_M()) || //
		dataFormat.equals(DataFormat.WATERML_1_1()) || //
		dataFormat.equals(DataFormat.WATERML_2_0()) || //
		dataFormat.equals(DataFormat.GML_3_1()) || //
		dataFormat.equals(DataFormat.GML_3_2())) {

	    extension = ".xml";

	} else if (dataFormat.equals(DataFormat.IMAGE_GEOTIFF())) {

	    extension = ".tif";

	} else if (dataFormat.equals(DataFormat.IMAGE_JPG())) {

	    extension = ".jpg";

	} else if (dataFormat.equals(DataFormat.IMAGE_PNG())) {

	    extension = ".png";

	} else if (dataFormat.equals(DataFormat.NETCDF()) || dataFormat.isSubTypeOf(DataFormat.NETCDF())) {

	    extension = ".nc";
	} else if (dataFormat.equals(DataFormat.DAS())) {

	    extension = ".das";
	} else if (dataFormat.equals(DataFormat.DDS())) {

	    extension = ".dds";
	}

	String filename = "dataset" + extension;

	if (onlineId != null && !onlineId.isEmpty()) {
	    filename = onlineId + extension;
	}

	builder.header("Content-Disposition", "attachment; filename=\"" + filename + "\"");

	return builder.build();
    }

}
