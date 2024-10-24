package eu.essi_lab.profiler.os.handler.discover;

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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.cxf.helpers.IOUtils;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.NameSpace;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.termfrequency.TermFrequencyMap;
import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.pdk.rsf.DiscoveryResultSetFormatter;
import eu.essi_lab.pdk.rsf.FormattingEncoding;
import eu.essi_lab.profiler.os.OSParameters;
import eu.essi_lab.profiler.os.OSRequestParser;

/**
 * This formatter encapsulates the resources in a document according to the guide line
 * specified here:
 * <a href="http://www.opensearch.org/Specifications/OpenSearch/1.1#OpenSearch_response_elements">OpenSearch response
 * elements</a>. The {@link #OS_XML_FORMATTING_ENCODING}
 * has the following properties:
 * <ul>
 * <li>media type is {@link MediaType#APPLICATION_XML}</li>
 * <li>encoding name is {@value #OS_XML_FORMATTING_ENCODING_NAME}</li>
 * <li>encoding version is {@value #OS_XML_FORMATTING_ENCODING_VERSION}</li>
 * </ul>
 * 
 * @author Fabrizio
 */
public class OS_XML_ResultSetFormatter extends DiscoveryResultSetFormatter<String> {

    /**
     * The encoding name of {@link #OS_XML_FORMATTING_ENCODING}
     */
    public static final String OS_XML_FORMATTING_ENCODING_NAME = "os-xml-frm-encoding";
    /**
     * The encoding version of {@link #OS_XML_FORMATTING_ENCODING}
     */
    public static final String OS_XML_FORMATTING_ENCODING_VERSION = "1.0";

    /**
     * The {@link FormattingEncoding} of this formatter
     */
    public static final FormattingEncoding OS_XML_FORMATTING_ENCODING = new FormattingEncoding();
    static {
	OS_XML_FORMATTING_ENCODING.setEncoding(OS_XML_FORMATTING_ENCODING_NAME);
	OS_XML_FORMATTING_ENCODING.setEncodingVersion(OS_XML_FORMATTING_ENCODING_VERSION);
	OS_XML_FORMATTING_ENCODING.setMediaType(MediaType.APPLICATION_XML_TYPE);
    }

    private static final String OS_XML_RES_FORMATTER_TF_MAP_ERROR = "OS_XML_RES_FORMATTER_TF_MAP_ERROR";
    private static String template;
    static {

	InputStream stream = OS_XML_ResultSetFormatter.class.getClassLoader().getResourceAsStream("os-response-template.xml");
	try {
	    template = IOUtils.toString(stream);
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    @Override
    public Response format(DiscoveryMessage message, ResultSet<String> mappedResultSet) throws GSException {

	String templateClone = new String(template);

	String requestUrl = message.getWebRequest().getServletRequest().getRequestURL().toString();

	String encodedUrl = StringEscapeUtils.escapeXml11(requestUrl);
	templateClone = templateClone.replace("QUERY", encodedUrl);

	templateClone = templateClone.replace("UPDATED", ISO8601DateTimeUtils.getISO8601DateTime());

	templateClone = templateClone.replace("TOTAL_RESULTS", String.valueOf(mappedResultSet.getCountResponse().getCount()));

	templateClone = templateClone.replace("START_INDEX", String.valueOf(message.getPage().getStart()));
	templateClone = templateClone.replace("ITEMS_PER_PAGE", String.valueOf(message.getPage().getSize()));

	String results = "";
	List<String> allResults = mappedResultSet.getResultsList();
	for (String result : allResults) {
	    results += result + "\n";
	}
	templateClone = templateClone.replace("RESULTS", results);

	Optional<TermFrequencyMap> frequencyMap = mappedResultSet.getCountResponse()
		.mergeTermFrequencyMaps(message.getMaxFrequencyMapItems());
	if (frequencyMap.isPresent()) {
	    try {
		String statistics = frequencyMap.get().asString(true);
		templateClone = templateClone.replace("STATISTICS", statistics);

	    } catch (Exception e) {

		throw GSException.createException(getClass(), e.getMessage(), null, ErrorInfo.ERRORTYPE_SERVICE, ErrorInfo.SEVERITY_ERROR,
			OS_XML_RES_FORMATTER_TF_MAP_ERROR, e);
	    }
	} else {
	    templateClone = templateClone.replace("STATISTICS", "");
	}

	ResponseBuilder builder = Response.status(Status.OK);
	builder = builder.entity(templateClone);

	String outputFormat = null;
	String queryString = message.getWebRequest().getQueryString();

	if (queryString != null) {

	    KeyValueParser keyValueParser = new KeyValueParser(message.getWebRequest().getQueryString());
	    OSRequestParser parser = new OSRequestParser(keyValueParser);

	    outputFormat = parser.parse(OSParameters.OUTPUT_FORMAT);
	    if (outputFormat.equals(NameSpace.GS_DATA_MODEL_XML_MEDIA_TYPE) || outputFormat.equals("application/atom+xml")) {
		outputFormat = MediaType.APPLICATION_XML;
	    }
	} else {
	    outputFormat = MediaType.APPLICATION_XML;
	}

	// the other case is atom+xml which should be correctly parsed by the browsers
	// if not, better use always MediaType.APPLICATION_XML

	builder = builder.type(MediaType.valueOf(outputFormat));

	return builder.build();
    }

    /**
     * @param requestUrl
     * @return
     */
    public static String getEmptyResponse(String requestUrl) {

	String templateClone = new String(template);

	String encodedUrl = StringEscapeUtils.escapeXml11(requestUrl);
	templateClone = templateClone.replace("QUERY", encodedUrl);

	templateClone = templateClone.replace("UPDATED", ISO8601DateTimeUtils.getISO8601DateTime());

	templateClone = templateClone.replace("TOTAL_RESULTS", "0");

	templateClone = templateClone.replace("START_INDEX", "0");
	templateClone = templateClone.replace("ITEMS_PER_PAGE", "0");

	templateClone = templateClone.replace("RESULTS", "");
	templateClone = templateClone.replace("STATISTICS", "");

	return templateClone;
    }

    /**
     * Returns {@link #OS_XML_FORMATTING_ENCODING}
     */
    @Override
    public FormattingEncoding getEncoding() {

	return OS_XML_FORMATTING_ENCODING;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }
}
