package eu.essi_lab.pdk.rsf.impl.xml.gs;

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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.cxf.helpers.IOUtils;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.termfrequency.TermFrequencyMap;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.pdk.rsf.DiscoveryResultSetFormatter;
import eu.essi_lab.pdk.rsf.FormattingEncoding;
public class GS_XML_ResultSetFormatter extends DiscoveryResultSetFormatter<String> {

    /**
     * The encoding name of {@link #GS_FORMATTING_ENCODING}
     */
    public static final String GS_FORMATTING_ENCODING_NAME = "gs-frm-encoding";
    /**
     * The encoding version of {@link #GS_FORMATTING_ENCODING}
     */
    public static final String GS_FORMATTING_ENCODING_VERSION = "1.0";

    /**
     * The {@link FormattingEncoding} of this formatter
     */
    public static final FormattingEncoding GS_FORMATTING_ENCODING = new FormattingEncoding();
    private static final String GS_RES_FORMATTER_TF_MAP_ERROR = "GS_RES_FORMATTER_TF_MAP_ERROR";
    static {
	GS_FORMATTING_ENCODING.setEncoding(GS_FORMATTING_ENCODING_NAME);
	GS_FORMATTING_ENCODING.setEncodingVersion(GS_FORMATTING_ENCODING_VERSION);
	GS_FORMATTING_ENCODING.setMediaType(MediaType.APPLICATION_XML_TYPE);
    }
    private static String template;
    static {

	InputStream stream = GS_XML_ResultSetFormatter.class.getClassLoader().getResourceAsStream("gs-response-template.xml");
	try {
	    template = IOUtils.toString(stream);
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    @Override
    public Response format(DiscoveryMessage message, ResultSet<String> mappedResultSet) throws GSException {

	String templateClone = new String(template);

	templateClone = templateClone.replace("TIMESTAMP", ISO8601DateTimeUtils.getISO8601DateTime());

	templateClone = templateClone.replace("TOTAL_RECORDS", String.valueOf(mappedResultSet.getCountResponse().getCount()));

	templateClone = templateClone.replace("START_INDEX", String.valueOf(message.getPage().getStart()));
	templateClone = templateClone.replace("RETURNED_RECORDS", String.valueOf(mappedResultSet.getResultsList().size()));

	int lastIndex = message.getPage().getStart() + message.getPage().getSize();
	int nextReport = lastIndex > mappedResultSet.getCountResponse().getCount() ? //
		0 : //
		message.getPage().getStart() + mappedResultSet.getResultsList().size();

	templateClone = templateClone.replace("NEXT_RECORD", String.valueOf(nextReport));

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
		TermFrequencyMapFormatter formatter = new TermFrequencyMapFormatter(frequencyMap.get());
		String statistics = formatter.formatAsXML();
		templateClone = templateClone.replace("STATISTICS", statistics);

	    } catch (Exception e) {

		throw GSException.createException(//
			getClass(), //
			e.getMessage(), //
			null, //
			ErrorInfo.ERRORTYPE_SERVICE, //
			ErrorInfo.SEVERITY_ERROR, //
			GS_RES_FORMATTER_TF_MAP_ERROR, e);
	    }
	} else {
	    templateClone = templateClone.replace("STATISTICS", "");
	}

	ResponseBuilder builder = Response.status(Status.OK);
	builder = builder.entity(templateClone);
	builder = builder.type(MediaType.APPLICATION_XML);

	return builder.build();
    }

    @Override
    public FormattingEncoding getEncoding() {

	return GS_FORMATTING_ENCODING;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }
}
