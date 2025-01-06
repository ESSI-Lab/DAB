package eu.essi_lab.profiler.wof.access;

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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;

import eu.essi_lab.messages.AccessMessage;
import eu.essi_lab.messages.MessageResponse;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.count.CountSet;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.pdk.rsf.AccessResultSetFormatter;
import eu.essi_lab.pdk.rsf.FormattingEncoding;
import eu.essi_lab.profiler.wof.WOFRequest;
import eu.essi_lab.profiler.wof.WOFRequest.Parameter;

/**
 * @author boldrini
 */
public class GetValuesResultSetFormatter extends AccessResultSetFormatter<DataObject> {

    @Override
    public Response format(AccessMessage message, ResultSet<DataObject> mappedResultSet) throws GSException {

	List<ErrorInfo> errors = mappedResultSet.getException().getErrorInfoList();
	if (!errors.isEmpty()) {

	    return handleException(message, mappedResultSet, errors);
	}

	DataObject dataObject = mappedResultSet.getResultsList().get(0);

	Response.status(Status.OK);
	File file = dataObject.getFile();
	ResponseBuilder builder = Response.status(Status.OK);
//	File tmp = new File("/home/boldrini/test/wof.xml");
//	File file = new File("/home/boldrini/test/dest.xml");
//	try {
//	    IOUtils.copy(new FileInputStream(tmp), new FileOutputStream(file));
//	} catch (IOException e1) {
//	    // TODO Auto-generated catch block
//	    e1.printStackTrace();
//	}
	
	StreamingOutput stream = new StreamingOutput() {
	    @Override
	    public void write(OutputStream out) throws IOException, WebApplicationException {
		try (FileInputStream inp = new FileInputStream(file)) {
		    IOUtils.copy(inp, out);
		    out.flush();
		    out.close();
		} catch (Exception e) {
		    throw new IOException("Stream error: " + e.getMessage());
		} finally {
		    file.delete();
		}
	    }

	};

	builder = builder.entity(stream);

	WOFRequest getValuesRequest;

	try {
	    getValuesRequest = new GetValuesObjectRequest(message.getWebRequest());
	} catch (Exception e) {
	    getValuesRequest = new GetValuesRequest(message.getWebRequest());
	}

	String extension;

	String format = getValuesRequest.getParameterValue(Parameter.FORMAT);
	if (format != null && format.toLowerCase().contains("netcdf")) {
	    // netcdf
	    builder.type(new MediaType("application", "x-netcdf"));
	    extension = ".nc";
	} else {
	    // waterml 1 & 2
	    builder.type(MediaType.TEXT_XML);
	    extension = ".xml";
	}

	String filename = "timeSeries" + extension;

	builder.header("Content-Disposition", "attachment; filename=\"" + filename + "\"");

	return builder.build();
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
