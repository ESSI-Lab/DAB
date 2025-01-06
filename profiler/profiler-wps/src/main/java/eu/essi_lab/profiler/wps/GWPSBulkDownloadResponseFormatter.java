package eu.essi_lab.profiler.wps;

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
import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import eu.essi_lab.messages.BulkDownloadMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.count.CountSet;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.pdk.rsf.FormattingEncoding;
import eu.essi_lab.pdk.rsf.MessageResponseFormatter;

public class GWPSBulkDownloadResponseFormatter
	implements MessageResponseFormatter<BulkDownloadMessage, DataObject, CountSet, ResultSet<DataObject>> {

    @Override
    public Provider getProvider() {
	
	return Provider.essiLabProvider();
    }

    @Override
    public Response format(BulkDownloadMessage message, ResultSet<DataObject> response) throws GSException {

	ResponseBuilder builder = Response.status(Status.OK);

	DataObject finalResponse = response.getResultsList().get(0);

	final File file = finalResponse.getFile();

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
	builder.type(MediaType.valueOf("application/zip"));

	return builder.build();
    }

    @Override
    public FormattingEncoding getEncoding() {

	return null;
    }
}
