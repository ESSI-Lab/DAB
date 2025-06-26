package eu.essi_lab.profiler.ckan;

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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.base.Charsets;

import eu.essi_lab.api.database.DatabaseExecutor;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.SearchAfter;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.termfrequency.TermFrequencyItem;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Country;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.pdk.handler.StreamingRequestHandler;
import eu.essi_lab.profiler.ckan.CKANRequest.APIParameters;

public class PackageListHandler extends StreamingRequestHandler {

    protected static final int MAX = 1000;

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {
	ValidationMessage ret = new ValidationMessage();
	ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	return ret;
    }

    public PackageListHandler() {

    }

    @Override
    public StreamingOutput getStreamingResponse(WebRequest webRequest) throws GSException {

	return new StreamingOutput() {

	    @Override
	    public void write(OutputStream output) throws IOException, WebApplicationException {
		OutputStreamWriter writer = new OutputStreamWriter(output, Charsets.UTF_8);

		PackageListTransformer transformer = new PackageListTransformer();
		DiscoveryMessage discoveryMessage = null;
		try {
		    discoveryMessage = transformer.transform(webRequest);
		} catch (GSException e) {
		    e.printStackTrace();
		}

		CKANRequest request = new CKANRequest(webRequest);

		String maxString = request.getParameterValue(APIParameters.LIMIT);
		int max;
		if (maxString == null) {
		    max = MAX;
		} else {
		    max = Integer.parseInt(maxString);
		}

		DatabaseExecutor executor = null;
		try {
		    executor = DatabaseProviderFactory.getExecutor(ConfigurationWrapper.getStorageInfo());
		} catch (GSException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
		// String resumption =
		// request.getParameterValue(eu.essi_lab.profiler.om.OMRequest.APIParameters.RESUMPTION_TOKEN);

		ResultSet<TermFrequencyItem> results = null;
		try {
		    results = executor.getIndexValues(discoveryMessage, ResourceProperty.PUBLIC_ID, max, null);
		} catch (GSException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		    GSLoggerFactory.getLogger(getClass()).error(e);
		}

		List<TermFrequencyItem> fitems = results.getResultsList();

		JSONObject ret = new JSONObject();
		ret.put("success", true);
		ret.put("help", "https://demo.ckan.org/api/3/action/help_show?name=package_list");
		JSONArray array = new JSONArray();

		for (TermFrequencyItem fitem : fitems) {
		    String term = fitem.getDecodedTerm();
		    array.put(term);

		}
		ret.put("result", array);

		writer.write(ret.toString());
		writer.flush();
		writer.close();
		output.close();
	    }

	    private ResultSet<TermFrequencyItem> getResult(String... terms) {
		ResultSet<TermFrequencyItem> ret = new ResultSet<TermFrequencyItem>();

		for (String term : terms) {
		    TermFrequencyItem item = new TermFrequencyItem();
		    item.setFreq(1);
		    item.setTerm(term);
		    item.setDecodedTerm(term);
		    ret.getResultsList().add(item);
		}

		return ret;
	    }

	};
    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {
	return MediaType.APPLICATION_JSON_TYPE;
    }

}
