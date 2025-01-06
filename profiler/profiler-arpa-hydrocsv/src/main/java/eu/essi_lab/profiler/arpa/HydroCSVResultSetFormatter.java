package eu.essi_lab.profiler.arpa;

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

import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.pdk.rsf.DiscoveryResultSetFormatter;
import eu.essi_lab.pdk.rsf.FormattingEncoding;
import eu.essi_lab.profiler.arpa.HydroCSVParameters.HydroCSVParameter;
import eu.essi_lab.profiler.arpa.HydroCSVTimeSeriesEncoder.CSV_Field;

public class HydroCSVResultSetFormatter extends DiscoveryResultSetFormatter<String> {

    /**
     * The {@link FormattingEncoding} of this formatter
     */
    public static final FormattingEncoding HYDRO_CSV_FORMATTING_ENCODING = new FormattingEncoding();
    static {
	HYDRO_CSV_FORMATTING_ENCODING.setEncoding("HYDRO_CSV_ENCODING");
	HYDRO_CSV_FORMATTING_ENCODING.setEncodingVersion("1.0");
	HYDRO_CSV_FORMATTING_ENCODING.setMediaType(new MediaType("text", "csv"));
    }

    @Override
    public Response format(DiscoveryMessage message, ResultSet<String> messageResponse) throws GSException {
	// -------------------------------
	//
	// creates the response
	//
	ResponseBuilder builder = Response.status(Status.OK);

	HydroCSVParameters parameters = new HydroCSVParameters(message.getWebRequest());

	String results = parameters.getParameter(HydroCSVParameter.RESULTS);

	String response = "";

	if (results != null && results.equals(HydroCSVParameters.HITS)) {

	    int matched = messageResponse.getCountResponse().getCount();

	    response = "" + matched;

	    builder = builder.type(new MediaType("text", "plain"));

	} else {

	    List<String> csvlines = messageResponse.getResultsList();

	    HydroCSVTimeSeriesEncoder encoder = new HydroCSVTimeSeriesEncoder();
	    CSV_Field[] fields = encoder.getFields();
	    for (CSV_Field field : fields) {
		encoder.add(field, field.toString());
	    }
	    response = encoder.toString() + "\n"; // for the headers;

	    for (String csvline : csvlines) {
		response += csvline + "\n";
	    }

	    builder = builder.type(new MediaType("text", "csv"));

	}

	builder = builder.entity(response);

	return builder.build();
    }

    @Override
    public FormattingEncoding getEncoding() {
	return HYDRO_CSV_FORMATTING_ENCODING;
    }

    @Override
    public Provider getProvider() {
	return new ESSILabProvider();
    }

}
