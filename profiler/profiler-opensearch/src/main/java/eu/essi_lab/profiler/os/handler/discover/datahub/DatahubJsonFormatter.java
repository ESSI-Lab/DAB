package eu.essi_lab.profiler.os.handler.discover.datahub;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import javax.ws.rs.core.Response.Status;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.pdk.rsf.DiscoveryResultSetFormatter;
import eu.essi_lab.pdk.rsf.FormattingEncoding;

/**
 * Formatter for the DataHub JSON output format (v3.8 metadata model).
 */
public class DatahubJsonFormatter extends DiscoveryResultSetFormatter<String> {

    /**
     * Encoding name of {@link #DATAHUB_JSON_FORMATTING_ENCODING}
     */
    public static final String DATAHUB_JSON_FORMATTING_ENCODING_NAME = "datahub-json-frm-enc";

    /**
     * Encoding version of {@link #DATAHUB_JSON_FORMATTING_ENCODING}
     */
    public static final String DATAHUB_JSON_FORMATTING_ENCODING_VERSION = "1.0";

    /**
     * The {@link FormattingEncoding} for this DataHub JSON format.
     */
    public static final FormattingEncoding DATAHUB_JSON_FORMATTING_ENCODING = new FormattingEncoding();

    static {
	DATAHUB_JSON_FORMATTING_ENCODING.setMediaType(MediaType.valueOf(DatahubJsonMapper.DATAHUB_JSON_MEDIA_TYPE));
	DATAHUB_JSON_FORMATTING_ENCODING.setEncoding(DATAHUB_JSON_FORMATTING_ENCODING_NAME);
	DATAHUB_JSON_FORMATTING_ENCODING.setEncodingVersion(DATAHUB_JSON_FORMATTING_ENCODING_VERSION);
    }

    @Override
    public Response format(DiscoveryMessage message, ResultSet<String> mappedResultSet) throws GSException {

	int totalResults = mappedResultSet.getCountResponse() != null ? mappedResultSet.getCountResponse().getCount() : 0;
	JSONArray results = new JSONArray();
	List<String> resultsList = mappedResultSet.getResultsList();
	if (resultsList != null) {
	    for (String jsonStr : resultsList) {
		if (jsonStr != null && !jsonStr.isEmpty()) {
		    try {
			results.put(new JSONObject(jsonStr));
		    } catch (Exception e) {
			results.put(new JSONObject().put("raw", jsonStr));
		    }
		}
	    }
	}
	JSONObject out = new JSONObject();
	out.put("totalResults", totalResults);
	out.put("results", results);

	return Response.status(Status.OK)
		.type(DatahubJsonMapper.DATAHUB_JSON_MEDIA_TYPE)
		.entity(out.toString())
		.build();
    }

    @Override
    public FormattingEncoding getEncoding() {

	return DATAHUB_JSON_FORMATTING_ENCODING;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }
}
