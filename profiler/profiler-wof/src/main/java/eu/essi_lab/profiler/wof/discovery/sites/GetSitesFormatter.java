package eu.essi_lab.profiler.wof.discovery.sites;

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
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.pdk.rsf.DiscoveryResultSetFormatter;
import eu.essi_lab.pdk.rsf.FormattingEncoding;

/**
 * @author boldrini
 */
public class GetSitesFormatter extends DiscoveryResultSetFormatter<String> {

    /**
     * The encoding name of {@link #HIS_CENTRAL_SITES_FORMATTING_ENCODING}
     */
    public static final String HIS_CENTRAL_SITES_ENCODING = "HIS_CENTRAL_SITES_ENCODING";
    /**
     * The encoding version of {@link #HIS_CENTRAL_SITES_FORMATTING_ENCODING}
     */
    public static final String HIS_CENTRAL_SITES_ENCODING_VERSION = "2.6.2";

    /**
     * The {@link FormattingEncoding} of this formatter
     */
    public static final FormattingEncoding HIS_CENTRAL_SITES_FORMATTING_ENCODING = new FormattingEncoding();
    static {
	HIS_CENTRAL_SITES_FORMATTING_ENCODING.setEncoding(HIS_CENTRAL_SITES_ENCODING);
	HIS_CENTRAL_SITES_FORMATTING_ENCODING.setEncodingVersion(HIS_CENTRAL_SITES_ENCODING_VERSION);
	HIS_CENTRAL_SITES_FORMATTING_ENCODING.setMediaType(MediaType.TEXT_XML_TYPE);
    }

    private static final String HIS_CENTRAL_SITES_FORMATTER_ERROR = "HIS_CENTRAL_SITES_FORMATTER_ERROR";

    @Override
    public Response format(DiscoveryMessage message, ResultSet<String> mappedResultSet) throws GSException {

	try {

	    List<String> sitesRecords = mappedResultSet.getResultsList();

	    StringBuilder builder = new StringBuilder();
	    String method = message.getWebRequest().getServletRequest().getMethod();
	    if (method.equals("GET")) {
		String httpBegin = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + //
			"<ArrayOfSite xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://hiscentral.cuahsi.org/20100205/\">\n"; //
		builder.append(httpBegin);
	    } else {
		String soapBegin = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + //
			"<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" + //
			"    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + //
			"    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" + //
			"    <soap:Body>\n" + //
			"        <GetSitesResponse xmlns=\"http://hiscentral.cuahsi.org/20100205/\">\n" + //
			"            <GetSitesResult>\n"; //
		builder.append(soapBegin);
	    }

	    for (String siteRecord : sitesRecords) {
		builder.append(siteRecord);
	    }

	    if (method.equals("GET")) {
		String httpEnd = "</ArrayOfSite>";//
		builder.append(httpEnd);
	    } else {
		String soapEnd = "            </GetSitesResult>\n" + //
			"        </GetSitesResponse>\n" + //
			"    </soap:Body>\n" + //
			"</soap:Envelope>";//
		builder.append(soapEnd);
	    }

	    return buildResponse(builder.toString());

	} catch (

	Exception ex) {
	    throw GSException.createException(//
		    getClass(), //
		    ex.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    HIS_CENTRAL_SITES_FORMATTER_ERROR);
	}
    }

    @Override
    public FormattingEncoding getEncoding() {

	return HIS_CENTRAL_SITES_FORMATTING_ENCODING;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    private Response buildResponse(String response) throws Exception {

	ResponseBuilder builder = Response.status(Status.OK);

	builder = builder.entity(response);
	builder = builder.type(MediaType.TEXT_XML);

	return builder.build();
    }
}
