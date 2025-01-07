package eu.essi_lab.profiler.terms;

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
import java.util.Date;
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
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.SpatialBond;
import eu.essi_lab.messages.bond.SpatialEntity;
import eu.essi_lab.messages.bond.SpatialExtent;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.pdk.handler.StreamingRequestHandler;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;
import eu.essi_lab.profiler.terms.TermsRequest.APIParameters;
import eu.essi_lab.request.executor.discover.QueryInitializer;

public class TermsHandler extends StreamingRequestHandler {

    @Override
    public MediaType getMediaType(WebRequest webRequest) {
	return MediaType.APPLICATION_JSON_TYPE;
    }

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {
	ValidationMessage ret = new ValidationMessage();
	ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	return ret;
    }

    public TermsHandler() {

    }

    @Override
    public StreamingOutput getStreamingResponse(WebRequest webRequest) throws GSException {

	Optional<String> optionalView = webRequest.extractViewId();
	String viewId = optionalView.isPresent() ? optionalView.get() : null;

	return new StreamingOutput() {

	    @Override
	    public void write(OutputStream output) throws IOException, WebApplicationException {

		TermsRequest request = new TermsRequest(webRequest);
		Optional<String> view = webRequest.extractViewId();
		Optional<SpatialBond> spatialBond = request.getSpatialBond();
		Double w = null;
		Double s = null;
		Double e = null;
		Double n = null;
		if (spatialBond.isPresent()) {
		    SpatialEntity spatialValue = spatialBond.get().getPropertyValue();
		    if (spatialValue instanceof SpatialExtent) {
			SpatialExtent se = (SpatialExtent) spatialValue;
			w = se.getWest();
			s = se.getSouth();
			e = se.getEast();
			n = se.getNorth();
		    }
		}
		Optional<SimpleValueBond> beginBond = request.getBeginBond();
		Optional<SimpleValueBond> endBond = request.getEndBond();
		Date begin = null;
		Date end = null;
		if (beginBond.isPresent() && endBond.isPresent()) {

		    if (beginBond.isPresent()) {
			begin = ISO8601DateTimeUtils.parseISO8601ToDate(beginBond.get().getPropertyValue()).get();
		    }
		    if (endBond.isPresent()) {
			end = ISO8601DateTimeUtils.parseISO8601ToDate(endBond.get().getPropertyValue()).get();
		    }
		}

		String format = request.getParameterValue(APIParameters.FORMAT);
		if (format == null) {
		    format = "JSON";
		}

		DiscoveryRequestTransformer transformer = new TermsTransformer();

		DiscoveryMessage discoveryMessage;
		try {
		    discoveryMessage = transformer.transform(webRequest);
		} catch (GSException gse) {
		    List<ErrorInfo> list = gse.getErrorInfoList();
		    if (list.isEmpty()) {
			printErrorMessage(output, "Unknown error");
		    } else {
			ErrorInfo error = list.get(0);
			printErrorMessage(output, error.getErrorDescription());

		    }
		    return;
		}
		QueryInitializer initializer = new QueryInitializer();
		try {
		    initializer.initializeQuery(discoveryMessage);
		} catch (GSException e1) {
		    e1.printStackTrace();
		    GSLoggerFactory.getLogger(getClass()).error(e1);
		}

		String type = request.getParameterValue(APIParameters.TYPE);
		MetadataElement metadata = null;
		if (type == null) {
		    type = "";
		}
		switch (type) {
		case "parameter":
		case "observed_property":
		    metadata = MetadataElement.ATTRIBUTE_TITLE;
		    break;
		case "parameter_uri":
		case "observed_property_uri":
		    if (isBlueCloud(view)) {
			metadata = MetadataElement.ATTRIBUTE_IDENTIFIER;
		    } else {
			metadata = MetadataElement.OBSERVED_PROPERTY_URI;
		    }
		    break;
		case "instrument":
		    metadata = MetadataElement.INSTRUMENT_TITLE;
		    break;
		case "instrument_uri":
		    metadata = MetadataElement.INSTRUMENT_URI;
		    break;
		case "platform":
		    metadata = MetadataElement.PLATFORM_TITLE;
		    break;
		case "platform_uri":
		    metadata = MetadataElement.PLATFORM_URI;
		    break;
		case "keyword":
		    if (isBlueCloud(view)) {
			metadata = MetadataElement.KEYWORD_BLUE_CLOUD;
		    } else {
			metadata = MetadataElement.KEYWORD;
		    }
		    break;
		case "keyword_uri":
		    if (isBlueCloud(view)) {
			metadata = MetadataElement.KEYWORD_URI_BLUE_CLOUD;
		    } else {
			metadata = MetadataElement.KEYWORD_URI;
		    }
		    break;
		case "keyword_type":
		    metadata = MetadataElement.KEYWORD_TYPE;
		    break;
		case "organization":
		    metadata = MetadataElement.ORGANISATION_NAME;
		    break;
		case "organization_uri":
		    metadata = MetadataElement.ORIGINATOR_ORGANISATION_IDENTIFIER;
		    break;
		case "cruise":
		    metadata = MetadataElement.CRUISE_NAME;
		    break;
		case "cruise_uri":
		    metadata = MetadataElement.CRUISE_URI;
		    break;
		case "project":
		    metadata = MetadataElement.PROJECT_NAME;
		    break;
		case "project_uri":
		    metadata = MetadataElement.PROJECT_URI;
		    break;

		default:
		    metadata = null;

		    break;
		}

		try {

		    Page page = discoveryMessage.getPage();

		    DatabaseExecutor executor = DatabaseProviderFactory.getExecutor(ConfigurationWrapper.getDatabaseURI());

		    List<String> results = executor.getIndexValues(discoveryMessage, metadata, page.getStart(), page.getSize());

		    OutputStreamWriter writer = new OutputStreamWriter(output, Charsets.UTF_8);

		    JSONObject ret = new JSONObject();
		    ret.put("type", type);
		    // ret.put("expected", resultSet.getCountResponse().getCount());
		    ret.put("requestOffset", page.getStart());
		    ret.put("requestSize", page.getSize());
		    ret.put("responseSize", results.size());
		    JSONArray terms = new JSONArray();
		    terms.putAll(results);
		    ret.put("terms", terms);

		    writer.write(ret.toString());

		    writer.flush();
		    writer.close();
		    output.close();

		} catch (Exception ee) {

		    ee.printStackTrace();
		}
	    }

	    private boolean isBlueCloud(Optional<String> view) {
		return (view.isPresent() &&
		//
			(view.get().equals("test") || //
				view.get().contains("seadatanet") || //
				view.get().contains("blue-cloud"))
		//
		);
	    }

	};
    }

}
