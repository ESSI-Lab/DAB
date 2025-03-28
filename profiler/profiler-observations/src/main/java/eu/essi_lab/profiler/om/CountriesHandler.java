package eu.essi_lab.profiler.om;

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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.base.Charsets;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.stats.ComputationResult;
import eu.essi_lab.messages.stats.ResponseItem;
import eu.essi_lab.messages.stats.StatisticsMessage;
import eu.essi_lab.messages.stats.StatisticsResponse;
import eu.essi_lab.messages.termfrequency.TermFrequencyItem;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Country;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.pdk.handler.StreamingRequestHandler;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;
import eu.essi_lab.request.executor.IStatisticsExecutor;

public class CountriesHandler extends StreamingRequestHandler {

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {
	ValidationMessage ret = new ValidationMessage();
	ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	return ret;
    }

    public CountriesHandler() {

    }

    @Override
    public StreamingOutput getStreamingResponse(WebRequest webRequest) throws GSException {

	return new StreamingOutput() {

	    @Override
	    public void write(OutputStream output) throws IOException, WebApplicationException {
		OutputStreamWriter writer = new OutputStreamWriter(output, Charsets.UTF_8);

		OMTransformer transformer = new OMTransformer();
		DiscoveryMessage discoveryMessage = null;
		try {
		    discoveryMessage = transformer.transform(webRequest);
		} catch (GSException e) {
		    e.printStackTrace();
		}
		String pathInfo = webRequest.getServletRequest().getPathInfo();
		String objectPart = pathInfo.substring(pathInfo.lastIndexOf("/") + 1);

		StatisticsMessage statisticsMessage = new StatisticsMessage();

		statisticsMessage.setDataBaseURI(ConfigurationWrapper.getStorageInfo());
		if (discoveryMessage.getView().isPresent()) {
		    statisticsMessage.setView(discoveryMessage.getView().get());
		}

		if (discoveryMessage.getUserBond().isPresent()) {
		    statisticsMessage.setUserBond(discoveryMessage.getUserBond().get());
		}

		List<Queryable> queryables = new ArrayList<Queryable>();
		Queryable q = null;

		switch (objectPart.toLowerCase()) {
		case "countries":
		    q = MetadataElement.COUNTRY_ISO3;
		    break;
		case "observedproperties":
		    q = MetadataElement.ATTRIBUTE_TITLE;
		    break;
		case "observedpropertiesuri":
		    q = MetadataElement.OBSERVED_PROPERTY_URI;
		    break;
		case "timeinterpolations":
		    q = MetadataElement.TIME_INTERPOLATION;
		    break;
		case "intendedobservationspacings":
		    q = MetadataElement.TIME_RESOLUTION_DURATION_8601;
		    break;
		case "aggregationdurations":
		    q = MetadataElement.TIME_AGGREGATION_DURATION_8601;
		    break;
		case "providers":
		    q = ResourceProperty.SOURCE_ID;
		    break;
		default:

		    writer.write("Not recognized object: " + objectPart);
		    writer.flush();
		    writer.close();
		    output.close();
		    break;
		}

		queryables.add(q);
		int max = discoveryMessage.getPage().getSize();

		statisticsMessage.computeFrequency(queryables, max);

		ServiceLoader<IStatisticsExecutor> loader = ServiceLoader.load(IStatisticsExecutor.class);
		IStatisticsExecutor executor = loader.iterator().next();

		StatisticsResponse response = null;
		try {
		    response = executor.compute(statisticsMessage);
		} catch (GSException e) {
		    e.printStackTrace();
		    writer.write("Error during processing: " + e);
		    writer.flush();
		    writer.close();
		    output.close();
		    return;
		}

		List<ResponseItem> items = response.getItems();

		ResponseItem responseItem = items.get(0);

		Optional<ComputationResult> freq = responseItem.getFrequency(q);

		JSONObject ret = new JSONObject();

		if (freq.isPresent()) {
		    JSONArray array = new JSONArray();
		    ret.put(objectPart, array);
		    ComputationResult cr = freq.get();
		    List<TermFrequencyItem> fitems = cr.getFrequencyItems();
		    for (TermFrequencyItem fitem : fitems) {
			String term = fitem.getDecodedTerm();
			JSONObject obj = new JSONObject();
			obj.put("value", term);
			obj.put("observationCount", fitem.getFreq());
			array.put(obj);

			if (q.equals(MetadataElement.COUNTRY_ISO3)) {
			    Country country = Country.decode(term);
			    if (country != null) {
				obj.put("shortName", country.getShortName());
				obj.put("officialName", country.getOfficialName());
			    }
			}
			if (q.equals(ResourceProperty.SOURCE_ID)) {
			    GSSource s = ConfigurationWrapper.getSource(term);
			    if (s != null) {
				obj.put("label", s.getLabel());
			    }
			}
		    }
		}

		//

		writer.write(ret.toString());
		writer.flush();
		writer.close();
		output.close();
	    }
	};
    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {
	return MediaType.APPLICATION_JSON_TYPE;
    }

    public DiscoveryRequestTransformer getTransformer() {
	return new CountriesTransformer();
    }

}
