package eu.essi_lab.accessor.wof.discovery.series;

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

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.pdk.rsf.DiscoveryResultSetFormatter;
import eu.essi_lab.pdk.rsf.FormattingEncoding;
public class GetSeriesCatalogForBoxResultSetFormatter extends DiscoveryResultSetFormatter<String> {

    /**
     * The encoding name of {@link #HIS_CENTRAL_FORMATTING_ENCODING}
     */
    public static final String HIS_SERIES_RECORD_ENCODING_NAME = "his-series-record-encoding";
    /**
     * The encoding version of {@link #HIS_CENTRAL_FORMATTING_ENCODING}
     */
    public static final String HIS_SERIES_RECORD_ENCODING_VERSION = "2.6.2";

    /**
     * The {@link FormattingEncoding} of this formatter
     */
    public static final FormattingEncoding HIS_CENTRAL_FORMATTING_ENCODING = new FormattingEncoding();

    private static final String BEGIN_RESPONSE = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
	    + "<ArrayOfSeriesRecord xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns=\"http://hiscentral.cuahsi.org/20100205/\">\n";
    private static final String END_RESPONSE = "</ArrayOfSeriesRecord>";

    static {
	HIS_CENTRAL_FORMATTING_ENCODING.setEncoding(HIS_SERIES_RECORD_ENCODING_NAME);
	HIS_CENTRAL_FORMATTING_ENCODING.setEncodingVersion(HIS_SERIES_RECORD_ENCODING_VERSION);
	HIS_CENTRAL_FORMATTING_ENCODING.setMediaType(MediaType.APPLICATION_XML_TYPE);
	try {
	    emptyResponse = buildResponse(BEGIN_RESPONSE + END_RESPONSE);
	    GSLoggerFactory.getLogger(GetSeriesCatalogForBoxResultSetFormatter.class).info("Empty response optimization created");
	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    private static final String HIS_CENTRAL_RESULT_SET_FORMATTER_ERROR = "HIS_CENTRAL_RESULT_SET_FORMATTER_ERROR";

    private static Response emptyResponse = null;

    public GetSeriesCatalogForBoxResultSetFormatter() {

    }

    @Override
    public Response format(DiscoveryMessage message, ResultSet<String> mappedResultSet) throws GSException {

	try {

	    List<String> seriesRecords = mappedResultSet == null ? null : mappedResultSet.getResultsList();

	    if (seriesRecords == null) {
		seriesRecords = new ArrayList<>();
	    }

	    if (seriesRecords.isEmpty()) {
		if (emptyResponse != null) {
		    return emptyResponse;
		}
	    }

	    StringBuilder builder = new StringBuilder();
	    builder.append(BEGIN_RESPONSE);

	    for (String seriesRecord : seriesRecords) {
		builder.append(seriesRecord);
	    }

	    builder.append(END_RESPONSE);

	    return buildResponse(builder.toString());

	} catch (

	Exception ex) {
	    ex.printStackTrace();
	    throw GSException.createException(//
		    getClass(), //
		    ex.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    HIS_CENTRAL_RESULT_SET_FORMATTER_ERROR);
	}
    }

    @Override
    public FormattingEncoding getEncoding() {

	return HIS_CENTRAL_FORMATTING_ENCODING;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    private static Response buildResponse(String response) throws Exception {

	ResponseBuilder builder = Response.status(Status.OK);

	builder = builder.entity(response);
	builder = builder.type(MediaType.APPLICATION_XML);

	return builder.build();
    }
}
