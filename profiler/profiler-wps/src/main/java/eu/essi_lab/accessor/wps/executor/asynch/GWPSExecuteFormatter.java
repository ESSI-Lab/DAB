package eu.essi_lab.accessor.wps.executor.asynch;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.Optional;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import com.google.common.collect.Lists;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.messages.RequestMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.count.CountSet;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.pdk.rsf.FormattingEncoding;
import eu.essi_lab.pdk.rsf.MessageResponseFormatter;

public class GWPSExecuteFormatter implements MessageResponseFormatter<RequestMessage, String, CountSet, ResultSet<String>> {

    @Override
    public Provider getProvider() {
	return new ESSILabProvider();
    }

    @Override
    public Response format(RequestMessage message, ResultSet<String> messageResponse) throws GSException {

	String endpoint = getEndpoint(message);

	ArrayList<String> identifiers = Lists.newArrayList(messageResponse.getResultsList().iterator());

	String out = "";

	if (!identifiers.isEmpty()) {

	    out = "<ns2:ExecuteResponse xmlns:ows=\"http://www.opengis.net/ows/1.1\" " + "xmlns:ns2=\"http://www.opengis.net/wps/1.0.0\" "
		    + "xmlns:ns3=\"http://www.w3.org/1999/xlink\" " + "statusLocation=\"" + endpoint + "/status/" + identifiers.get(0)
		    + "\" service=\"WPS\" version=\"1.0.0\"/>";

	}
	// It seems that in some cases when we send the response and clients get results
	// from statusLocation, clients get Error retrieving status.
	boolean flag = true;
	int i = 0;
	while (flag && i < 5) {
	    Downloader d = new Downloader();
	    Optional<String> res = d.downloadOptionalString(endpoint + "/status/" + identifiers.get(0));
	    i++;
	    if (res.isPresent()) {
		String s = res.get();
		if (!s.contains("ERROR"))
		    flag = false;
	    }
	}

	ResponseBuilder builder = Response.status(Status.OK);
	builder = builder.entity(out);
	builder = builder.type(MediaType.APPLICATION_XML_TYPE);

	return builder.build();
    }

    protected String getEndpoint(RequestMessage message) {

	return message.getRequestAbsolutePath();
    }

    @Override
    public FormattingEncoding getEncoding() {
	return null;
    }

}
