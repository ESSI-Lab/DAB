package eu.essi_lab.pdk.rsf.impl.json.jsapi._2_0;

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

import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.MessageResponse;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.count.CountSet;
import eu.essi_lab.messages.termfrequency.TermFrequencyMap;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.pdk.rsf.FormattingEncoding;
import eu.essi_lab.pdk.rsf.impl.json.jsapi._1_0.JS_API_ResultSetFormatter_1_0;
public class JS_API_ResultSetFormatter_2_0 extends JS_API_ResultSetFormatter_1_0 {

    /**
     * The encoding name of {@link #JS_API_FORMATTING_ENCODING}
     */
    public static final String JS_API_FORMATTING_ENCODING_NAME = "js-api-frm-enc";
    /**
     * The encoding version of {@link #JS_API_FORMATTING_ENCODING}
     */
    public static final String JS_API_FORMATTING_ENCODING_VERSION = "2.0";

    /**
     * The {@link FormattingEncoding} of this formatter
     */
    public static final FormattingEncoding JS_API_FORMATTING_ENCODING = new FormattingEncoding();
    private static final String TERM_FREQ_MAP_JSON_SERIALIZATION_ERROR = null;
    static {

	GSLoggerFactory.getLogger(JS_API_ResultSetFormatter_2_0.class).trace("Static initialization of JS_API_FORMATTING_ENCODING");
	MediaType type = MediaType.APPLICATION_JSON_TYPE;
	type.withCharset("utf-8");
	JS_API_FORMATTING_ENCODING.setMediaType(type);

	JS_API_FORMATTING_ENCODING.setEncoding(JS_API_FORMATTING_ENCODING_NAME);
	JS_API_FORMATTING_ENCODING.setEncodingVersion(JS_API_FORMATTING_ENCODING_VERSION);
    }

    @Override
    protected JSONObject formatResultSet(DiscoveryMessage message, MessageResponse<String, CountSet> mappedResultSet) {

	JSONObject out = new JSONObject();

	// statistics
	try {
	    Optional<TermFrequencyMap> map = mappedResultSet.getCountResponse().mergeTermFrequencyMaps(message.getMaxFrequencyMapItems());

	    if (map.isPresent()) {
		JSONObject termFrequency = mapTermFrequencyMap(map.get().getElement(),message.getSources());
		out.put("statistics", termFrequency);
	    }
	} catch (Exception e) {
	    e.printStackTrace();

	    GSException exception = GSException.createException(getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_WARNING, //
		    TERM_FREQ_MAP_JSON_SERIALIZATION_ERROR);
	    message.getException().addInfoList(exception.getErrorInfoList());

	    GSLoggerFactory.getLogger(getClass()).warn(e.getMessage(), e);
	}

	// result set
	JS_API_ResultSet_2_0 resultSet = new JS_API_ResultSet_2_0(message, mappedResultSet);
	out.put("resultSet", resultSet.asJSONObject());

	// reports
	JSONArray reports = new JSONArray();
	List<String> allResults = mappedResultSet.getResultsList();
	for (String result : allResults) {
	    reports.put(new JSONObject(result));
	}
	out.put("reports", reports);

	return out;
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
