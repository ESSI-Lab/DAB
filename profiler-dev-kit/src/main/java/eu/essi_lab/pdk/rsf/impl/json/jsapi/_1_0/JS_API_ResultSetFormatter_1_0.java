package eu.essi_lab.pdk.rsf.impl.json.jsapi._1_0;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.MessageResponse;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.count.CountSet;
import eu.essi_lab.messages.termfrequency.TermFrequencyMap;
import eu.essi_lab.messages.termfrequency.TermFrequencyMapType;
import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.pdk.rsf.DiscoveryResultSetFormatter;
import eu.essi_lab.pdk.rsf.FormattingEncoding;

/**
 * This formatter encapsulates the resources in a JSON object according to an encoding defined for the
 * <a href="http://api.eurogeoss-broker.eu/docs/index.html">JavaScript API</a>. The {@link #JS_API_FORMATTING_ENCODING}
 * has the following properties:
 * <ul>
 * <li>media type is "application/json; charset=utf-8"</li>
 * <li>encoding name is {@value #JS_API_FORMATTING_ENCODING_NAME}</li>
 * <li>encoding version is {@value #JS_API_FORMATTING_ENCODING_VERSION}</li>
 * </ul>
 * 
 * @author Fabrizio
 */
public class JS_API_ResultSetFormatter_1_0 extends DiscoveryResultSetFormatter<String> {

    /**
     * The encoding name of {@link #JS_API_FORMATTING_ENCODING}
     */
    public static final String JS_API_FORMATTING_ENCODING_NAME = "js-api-frm-enc";
    /**
     * The encoding version of {@link #JS_API_FORMATTING_ENCODING}
     */
    public static final String JS_API_FORMATTING_ENCODING_VERSION = "1.0";

    /**
     * The {@link FormattingEncoding} of this formatter
     */
    public static final FormattingEncoding JS_API_FORMATTING_ENCODING = new FormattingEncoding();

    static {

	GSLoggerFactory.getLogger(JS_API_ResultSetFormatter_1_0.class).trace("Static initialization of JS_API_FORMATTING_ENCODING");
	MediaType type = MediaType.APPLICATION_JSON_TYPE;
	type.withCharset("utf-8");
	JS_API_FORMATTING_ENCODING.setMediaType(type);
	JS_API_FORMATTING_ENCODING.setEncoding(JS_API_FORMATTING_ENCODING_NAME);
	JS_API_FORMATTING_ENCODING.setEncodingVersion(JS_API_FORMATTING_ENCODING_VERSION);
    }

    @Override
    public Response format(DiscoveryMessage message, ResultSet<String> mappedResultSet) throws GSException {

	JSONObject jsonOutput = null;
	if (message.isOutputSources()) {
	    jsonOutput = formatSources(mappedResultSet.getResultsList());
	} else {
	    jsonOutput = formatResultSet(message, mappedResultSet);
	}

	String outToString = jsonOutput.toString(3);

	Optional<String> formData = message.getWebRequest().getFormData();

	if (formData.isPresent()) {

	    //
	    // handles the jquery callback (if available)
	    //
	    String queryString = formData.get();
	    KeyValueParser parser = new KeyValueParser(queryString);

	    String callback = parser.getValue("callback");
	    if (callback != null) {
		outToString = callback + "(" + outToString + ")";
	    }
	}

	ResponseBuilder builder = Response.status(Status.OK);
	builder = builder.entity(outToString);
	builder = builder.type(MediaType.valueOf("application/json; charset=utf-8"));

	return builder.build();
    }

    protected JSONObject formatSources(List<String> sources) {

	JSONObject out = new JSONObject();

	// result set
	JSONObject resultSet = new JSONObject();
	resultSet.put("size", sources.size());
	resultSet.put("start", 1);
	resultSet.put("pageSize", sources.size());
	resultSet.put("pageCount", 1);
	resultSet.put("pageIndex", 1);
	out.put("resultSet", resultSet);

	// reports
	JSONArray reports = new JSONArray();

	for (String source : sources) {
	    JSONObject object = new JSONObject(source);
	    reports.put(object);
	}
	out.put("reports", reports);

	return out;
    }

    protected JSONObject formatResultSet(DiscoveryMessage message, MessageResponse<String, CountSet> mappedResultSet) {

	JSONObject out = new JSONObject();

	// result set
	JSONObject resultSet = new JSONObject();
	resultSet.put("size", mappedResultSet.getCountResponse().getCount());
	resultSet.put("start", message.getPage().getStart());
	resultSet.put("pageSize", message.getPage().getSize());
	resultSet.put("pageCount", mappedResultSet.getCountResponse().getPageCount());
	resultSet.put("pageIndex", mappedResultSet.getCountResponse().getPageIndex());
	out.put("resultSet", resultSet);

	// reports
	JSONArray reports = new JSONArray();
	List<String> allResults = mappedResultSet.getResultsList();
	for (String result : allResults) {
	    reports.put(new JSONObject(result));
	}
	out.put("reports", reports);

	try {
	    Optional<TermFrequencyMap> map = mappedResultSet.getCountResponse().mergeTermFrequencyMaps(message.getMaxFrequencyMapItems());
	    if (map.isPresent()) {

		JSONObject termFrequency = mapTermFrequencyMap(map.get().getElement(), message.getSources());
		out.put("termFrequency", termFrequency);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).warn(e.getMessage(), e);
	}

	return out;
    }

    protected JSONObject mapTermFrequencyMap(TermFrequencyMapType type, List<GSSource> sources) throws Exception {

	ObjectMapper mapper = new ObjectMapper();
	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

	mapper.writeValue(outputStream, type);

	JSONObject tfMap = new JSONObject(outputStream.toString("UTF-8"));
	JSONArray sourceId = tfMap.getJSONArray("sourceId");

	for (GSSource gsSource : sources) {
	    for (int i = 0; i < sourceId.length(); i++) {
		JSONObject item = sourceId.getJSONObject(i);
		String id = item.getString("term");
		if (id.equals(gsSource.getUniqueIdentifier())) {
		    item.put("term", gsSource.getLabel());
		    item.put("decodedTerm", gsSource.getLabel());
		    item.put("sourceId", id);
		    break;
		}
	    }
	}

	tfMap.remove(TermFrequencyMap.TermFrequencyTarget.SOURCE.getName());
	tfMap.put("source", sourceId);

	// this seems to be a bug of the mapper, it produces an "sscScore" object (see TermFrequencyMapType)
	// but also a "ssccore" object equivalent but with a lower-case key
	tfMap.remove("sscscore");

	// removes all empty entries
	Set<String> keySet = tfMap.keySet();
	Set<String> toRemove = new HashSet<>();
	for (String key : keySet) {
	    JSONArray obj = tfMap.getJSONArray(key);
	    if (obj.length() == 0) {
		toRemove.add(key);
	    }
	}
	for (String key : toRemove) {
	    tfMap.remove(key);
	}

	return tfMap;
    }

    /**
     * Returns {@link #JS_API_FORMATTING_ENCODING}
     */
    @Override
    public FormattingEncoding getEncoding() {

	return JS_API_FORMATTING_ENCODING;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }
}
