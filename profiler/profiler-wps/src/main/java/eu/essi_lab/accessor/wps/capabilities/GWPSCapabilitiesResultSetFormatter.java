package eu.essi_lab.accessor.wps.capabilities;

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

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.RequestMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.pdk.rsf.DiscoveryResultSetFormatter;
import eu.essi_lab.pdk.rsf.FormattingEncoding;

/**
 * @author boldrini
 */
public class GWPSCapabilitiesResultSetFormatter extends DiscoveryResultSetFormatter<String> {

    /**
     * The encoding name of {@link #GWPS_CAPABILITIES_ENCODING_NAME}
     */
    public static final String GWPS_CAPABILITIES_ENCODING_NAME = "GWPS_CAPABILITIES_ENCODING_NAME";
    /**
     * The encoding version of {@link #GWPS_CAPABILITIES_ENCODING_VERSION}
     */
    public static final String GWPS_CAPABILITIES_ENCODING_VERSION = "1.0.0";

    /**
     * The {@link FormattingEncoding} of this formatter
     */
    public static final FormattingEncoding GWPS_FORMATTING_ENCODING = new FormattingEncoding();
    static {
	GWPS_FORMATTING_ENCODING.setEncoding(GWPS_CAPABILITIES_ENCODING_NAME);
	GWPS_FORMATTING_ENCODING.setEncodingVersion(GWPS_CAPABILITIES_ENCODING_VERSION);
	GWPS_FORMATTING_ENCODING.setMediaType(MediaType.APPLICATION_XML_TYPE);
    }

    private static final String GWPS_RESULT_SET_FORMATTER_ERROR = "GWPS_RESULT_SET_FORMATTER_ERROR";

    @Override
    public Response format(DiscoveryMessage message, ResultSet<String> mappedResultSet) throws GSException {

	try {

	    InputStream template = GWPSCapabilitiesResultSetFormatter.class.getClassLoader()
		    .getResourceAsStream("gwps/capabilities-template.xml");

	    String response = IOUtils.toString(template, StandardCharsets.UTF_8);

	    String endpoint = getEndpoint(message);
	    response = response.replace("${ENDPOINT}", endpoint);

	    String dateTime = ISO8601DateTimeUtils.getISO8601DateTimeWithMilliseconds(new Date());
	    response = response.replace("${DATE_TIME}", dateTime);

	    List<String> capabilities = mappedResultSet.getResultsList();

	    if (!capabilities.isEmpty()) {
		String capability = capabilities.get(0);
		response = response.replace("${CAPABILITIES}", capability);
	    } else {
		response = response.replace("${CAPABILITIES}", "Capabilities document not found!");
	    }

	    return buildResponse(response);

	} catch (

	Exception ex) {
	    throw GSException.createException(//
		    getClass(), //
		    ex.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    GWPS_RESULT_SET_FORMATTER_ERROR);
	}
    }

    protected String getEndpoint(RequestMessage message) {

	return message.getRequestAbsolutePath();
    }

    @Override
    public FormattingEncoding getEncoding() {

	return GWPS_FORMATTING_ENCODING;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    private Response buildResponse(String response) throws Exception {

	ResponseBuilder builder = Response.status(Status.OK);

	builder = builder.entity(response);
	builder = builder.type(MediaType.APPLICATION_XML);

	return builder.build();
    }
}
