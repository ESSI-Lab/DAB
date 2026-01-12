package eu.essi_lab.profiler.om;

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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import eu.essi_lab.messages.*;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.base.Charsets;

import eu.essi_lab.api.database.DatabaseExecutor;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
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
import eu.essi_lab.profiler.om.OMRequest.APIParameters;

public class PropertiesHandler extends StreamingRequestHandler {

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {
	ValidationMessage ret = new ValidationMessage();
	ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	return ret;
    }

    public PropertiesHandler() {

    }

    public enum DatasetProperty {
	COUNTRY(new String[] { "country" }, MetadataElement.COUNTRY_ISO3), //
	FEATURE(new String[] { "feature", "featureId" }, MetadataElement.UNIQUE_PLATFORM_IDENTIFIER), //
	OBSERVATION(new String[] { "observation" }, MetadataElement.ONLINE_ID), //
	OBSERVED_PROPERTY(new String[] { "observedProperty" }, MetadataElement.ATTRIBUTE_TITLE), //
	OBSERVED_PROPERTY_URI(new String[] { "observedPropertyURI" }, MetadataElement.OBSERVED_PROPERTY_URI), //
	INTENDED_OBSERVATION_SPACING(new String[] { "intendedObservationSpacing" }, MetadataElement.TIME_RESOLUTION_DURATION_8601), //
	AGGREGATION_DURATION(new String[] { "aggregationDuration" }, MetadataElement.TIME_AGGREGATION_DURATION_8601), //
	TIME_INTERPOLATION(new String[] { "timeInterpolation" }, MetadataElement.TIME_INTERPOLATION), //
	PROVIDER(new String[] { "provider" }, ResourceProperty.SOURCE_ID), //
	FORMAT(new String[] { "format" }, null), //
	ONTOLOGY(new String[] { "ontology" }, null),//
	;

	String[] names;

	public String[] getNames() {
	    return names;
	}

	public Queryable getQueryable() {
	    return queryable;
	}

	Queryable queryable;

	private DatasetProperty(String[] names, Queryable queryable) {
	    this.names = names;
	    this.queryable = queryable;
	}

	public static DatasetProperty decode(String property) {
	    for (DatasetProperty prop : values()) {
		for (String name : prop.getNames()) {
		    if (name.equals(property)) {
			return prop;
		    }
		}
	    }
	    return null;
	}

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

		OMRequest request = new OMRequest(webRequest);

		String property = request.getParameterValue(eu.essi_lab.profiler.om.OMRequest.APIParameters.PROPERTY);
		DatasetProperty dp = DatasetProperty.decode(property);
		if (property == null || dp == null) {
		    DatasetProperty[] properties = DatasetProperty.values();
		    JSONObject error = new JSONObject();
		    error.put("status", "error");
		    error.put("message", "The property parameter must be specified");
		    JSONArray array = new JSONArray();
		    for (DatasetProperty prop : properties) {
			array.put(prop.getNames()[0]);
		    }
		    error.put("suggestions", array);
		    writer.write(error.toString());
		    writer.flush();
		    writer.close();
		    output.close();
		    return;

		}

		Queryable q = dp.getQueryable();

		String maxString = request.getParameterValue(APIParameters.LIMIT);
		int max;
		if (maxString == null) {
		    max = 20;
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
		String resumption = request.getParameterValue(eu.essi_lab.profiler.om.OMRequest.APIParameters.RESUMPTION_TOKEN);

		ResultSet<TermFrequencyItem> results = null;
		switch (dp) {
		case ONTOLOGY: {
		    results = getResult("whos", "his-central");
		    break;

		}
		case FORMAT: {
		    results = getResult(OMFormat.stringValues());
		    break;

		}
		default:

		    try {
			discoveryMessage.setPage(new Page(1,max));
			results = executor.getIndexValues(discoveryMessage, q, max, resumption);
		    } catch (GSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		    break;
		}

		JSONObject ret = getJSONEncoding(results, dp);

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

    private JSONObject getJSONEncoding(ResultSet<TermFrequencyItem> results, DatasetProperty dp) {
	List<TermFrequencyItem> fitems = results.getResultsList();

	JSONObject ret = new JSONObject();
	Optional<SearchAfter> sa = results.getSearchAfter();
	if (sa.isPresent()) {
	    Optional<List<Object>> values = sa.get().getValues();
	    if (values.isPresent()) {
		ret.put("resumptionToken", values.get().get(0));
		ret.put("completed", false);
	    } else {
		ret.put("completed", true);
	    }
	} else {
	    ret.put("completed", true);
	}

	JSONArray array = new JSONArray();

	ret.put(dp.getNames()[0], array);
	for (TermFrequencyItem fitem : fitems) {
	    String term = fitem.getDecodedTerm();
	    JSONObject obj = new JSONObject();

	    obj.put("value", term);
	    obj.put("observationCount", fitem.getFreq());
	    array.put(obj);

	    if (dp.equals(DatasetProperty.COUNTRY)) {
		Country country = Country.decode(term);
		if (country != null) {
		    obj.put("shortName", country.getShortName());
		    obj.put("officialName", country.getOfficialName());
		}
	    }
	    if (dp.equals(DatasetProperty.PROVIDER)) {
		GSSource s = ConfigurationWrapper.getSource(term);
		if (s != null) {
		    obj.put("label", s.getLabel());
		}
	    }
	}
	return ret;
    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {
	return MediaType.APPLICATION_JSON_TYPE;
    }



}
