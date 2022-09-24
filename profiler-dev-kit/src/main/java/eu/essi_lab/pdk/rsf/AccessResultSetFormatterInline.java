package eu.essi_lab.pdk.rsf;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import eu.essi_lab.messages.AccessMessage;
import eu.essi_lab.messages.MessageResponse;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.count.CountSet;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataObject;

/**
 * @author Fabrizio
 */
public class AccessResultSetFormatterInline extends AccessResultSetFormatter<DataObject> {

    private static boolean fileEntity;

    /**
     * For test purpose.<br>
     * <br>
     * This flag can be enabled in order to get as {@link Response} entity a {@link File} instead of
     * a {@link StreamingOutput}.
     * 
     * @param fileEntity
     */
    public static void setFileEntity(boolean fileEntity) {

	AccessResultSetFormatterInline.fileEntity = fileEntity;
    }

    @Override
    public Response format(AccessMessage message, ResultSet<DataObject> mappedResultSet) throws GSException {

	List<ErrorInfo> errors = mappedResultSet.getException().getErrorInfoList();
	if (!errors.isEmpty()) {

	    return handleException(message, mappedResultSet, errors);
	}

	DataObject dataObject = mappedResultSet.getResultsList().get(0);

	refineDataObject(dataObject);

	ResponseBuilder builder = getBuilder(dataObject);

	return builder.build();
    }

    /**
     * Sub classes may need to override this method
     * 
     * @param dataObject
     */
    public void refineDataObject(DataObject dataObject) {

    }

    protected ResponseBuilder getBuilder(DataObject dataObject) {

	Response.status(Status.OK);
	File file = dataObject.getFile();

	ResponseBuilder builder = Response.status(Status.OK);

	if (fileEntity) {

	    builder = builder.entity(file);

	} else {

	    StreamingOutput stream = new StreamingOutput() {
		@Override
		public void write(OutputStream out) throws IOException, WebApplicationException {
		    try (FileInputStream inp = new FileInputStream(file)) {
			byte[] buff = new byte[1024];
			int len = 0;
			while ((len = inp.read(buff)) >= 0) {
			    out.write(buff, 0, len);
			}
			out.flush();
			out.close();
			inp.close();
		    } catch (Exception e) {
			throw new IOException("Stream error: " + e.getMessage());
		    } finally {
			file.delete();
		    }
		}

	    };

	    builder = builder.entity(stream);
	}

	DataDescriptor dataDescriptor = dataObject.getDataDescriptor();

	DataFormat dataFormat = dataDescriptor == null ? null : dataDescriptor.getDataFormat();

	if (dataFormat == null) {

	    builder.type(MediaType.APPLICATION_OCTET_STREAM_TYPE);

	} else if (dataFormat.equals(DataFormat.O_M()) || //
		dataFormat.equals(DataFormat.WATERML_1_1()) || //
		dataFormat.equals(DataFormat.WATERML_2_0()) || //
		dataFormat.equals(DataFormat.GML_3_1()) || //
		dataFormat.equals(DataFormat.GML_3_2())) {

	    builder.type(MediaType.TEXT_XML);

	} else if (dataFormat.equals(DataFormat.IMAGE_GEOTIFF())) {

	    builder.type(new MediaType("application", "x-geotiff"));

	} else if (dataFormat.equals(DataFormat.IMAGE_JPG())) {

	    builder.type(new MediaType("image", "jpeg"));

	} else if (dataFormat.equals(DataFormat.IMAGE_PNG())) {

	    builder.type(new MediaType("image", "png"));

	} else if (dataFormat.equals(DataFormat.NETCDF()) || dataFormat.isSubTypeOf(DataFormat.NETCDF())) {

	    builder.type(new MediaType("application", "x-netcdf"));

	} else if (dataFormat.equals(DataFormat.DAS())) {

	    builder.type(MediaType.TEXT_PLAIN);

	} else if (dataFormat.equals(DataFormat.DDS())) {

	    builder.type(MediaType.TEXT_PLAIN);

	}

	return builder;
    }

    /**
     * @param message
     * @param mappedResultSet
     * @param errors
     * @return
     */
    protected Response handleException(//
	    AccessMessage message, //
	    MessageResponse<DataObject, CountSet> mappedResultSet, //
	    List<ErrorInfo> errors) {

	return null;
    }

    @Override
    public FormattingEncoding getEncoding() {

	return null;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }
}
