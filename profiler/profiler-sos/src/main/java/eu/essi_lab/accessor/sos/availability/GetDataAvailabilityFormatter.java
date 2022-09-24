package eu.essi_lab.accessor.sos.availability;

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

import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import eu.essi_lab.messages.DiscoveryMessage;
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
public class GetDataAvailabilityFormatter extends DiscoveryResultSetFormatter<String> {

    public static final String SOS_DATA_AVAILABILITY_ENCODING = "SOS_DATA_AVAILABILITY_ENCODING";

    public static final String SOS_DATA_AVAILABILITY_ENCODING_VERSION = "2.0";

    /**
     * The {@link FormattingEncoding} of this formatter
     */
    public static final FormattingEncoding SOS_DATA_AVAILABILITY_FORMATTING_ENCODING = new FormattingEncoding();
    static {
	SOS_DATA_AVAILABILITY_FORMATTING_ENCODING.setEncoding(SOS_DATA_AVAILABILITY_ENCODING);
	SOS_DATA_AVAILABILITY_FORMATTING_ENCODING.setEncodingVersion(SOS_DATA_AVAILABILITY_ENCODING_VERSION);
	SOS_DATA_AVAILABILITY_FORMATTING_ENCODING.setMediaType(MediaType.APPLICATION_XML_TYPE);
    }

    private static final String SOS_DATA_AVAILABILITY_FORMATTER_ERROR = "SOS_DATA_AVAILABILITY_FORMATTER_ERROR";

    @Override
    public Response format(DiscoveryMessage message, ResultSet<String> mappedResultSet) throws GSException {

	try {

	    List<String> fois = mappedResultSet.getResultsList();

	    String ret = "";
	    for (String foi : fois) {

		ret = foi;
	    }

	    return buildResponse(ret);

	} catch (

	Exception ex) {
	    throw GSException.createException(//
		    getClass(), //
		    ex.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    SOS_DATA_AVAILABILITY_FORMATTER_ERROR);
	}
    }

    @Override
    public FormattingEncoding getEncoding() {

	return SOS_DATA_AVAILABILITY_FORMATTING_ENCODING;
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
