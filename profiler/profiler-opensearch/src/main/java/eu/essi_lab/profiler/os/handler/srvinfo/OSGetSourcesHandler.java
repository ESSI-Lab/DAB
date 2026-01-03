package eu.essi_lab.profiler.os.handler.srvinfo;

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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.xml.NameSpace;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.count.CountSet;
import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.pdk.handler.DefaultRequestHandler;
import eu.essi_lab.pdk.rsf.DiscoveryResultSetFormatter;
import eu.essi_lab.pdk.rsf.impl.atom.AtomGPResultSetFormatter;
import eu.essi_lab.pdk.rsf.impl.json.jsapi._1_0.JS_API_ResultSetFormatter_1_0;
import eu.essi_lab.pdk.rsm.impl.atom.AtomGPResultSetMapper;
import eu.essi_lab.pdk.rsm.impl.json.jsapi.JS_API_ResultSetMapper;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;
import eu.essi_lab.profiler.os.OSParameters;
import eu.essi_lab.profiler.os.OSProfiler;
import eu.essi_lab.profiler.os.OSRequestParser;

/**
 * @author Fabrizio
 */
public class OSGetSourcesHandler extends DefaultRequestHandler {

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	ValidationMessage message = new ValidationMessage();
	message.setResult(ValidationResult.VALIDATION_SUCCESSFUL);

	return message;
    }

    @Override
    public Response handle(WebRequest request) throws GSException {

	Response response = null;

	Optional<String> viewId = request.extractViewId();

	Optional<View> view = Optional.empty();

	if (viewId.isPresent()) {

	    view = DiscoveryRequestTransformer.findView(ConfigurationWrapper.getStorageInfo(), viewId.get());
	}

	String outputFormat = OSProfiler.readOutputFormat(request);

	DiscoveryMessage message = new DiscoveryMessage();
	message.setOutputSources(true);
	message.setWebRequest(request);

	KeyValueParser keyValueParser = new KeyValueParser(request.getFormData().get());
	OSRequestParser parser = new OSRequestParser(keyValueParser);

	message.setPage(new Page(1, Integer.parseInt(parser.parse(OSParameters.COUNT))));

	DiscoveryResultSetFormatter<String> formatter = null;
	ResultSet<String> resultSet = new ResultSet<String>();
	List<String> sources = null;

	Stream<GSSource> stream = view.isPresent() ? //
		ConfigurationWrapper.getViewSources(view.get()).stream() : //
		ConfigurationWrapper.getAllSources().stream();

	switch (outputFormat) {
	case MediaType.APPLICATION_JSON:

	    sources = stream.//
		    map(s -> {

			Dataset dataset = new Dataset();
			dataset.setSource(s);

			JS_API_ResultSetMapper mapper = new JS_API_ResultSetMapper();
			return mapper.map(message, dataset);

		    }).collect(Collectors.toList());

	    formatter = new JS_API_ResultSetFormatter_1_0();

	    break;

	case NameSpace.GS_DATA_MODEL_XML_MEDIA_TYPE:
	case MediaType.APPLICATION_ATOM_XML:
	default:

	    sources = stream.//
		    map(s -> {

			Dataset dataset = new Dataset();
			dataset.setSource(s);

			AtomGPResultSetMapper mapper = new AtomGPResultSetMapper();
			try {
			    return mapper.map(message, dataset);
			} catch (GSException e) {
			}

			return null;
		    }).filter(Objects::nonNull).//
		    collect(Collectors.toList());

	    formatter = new AtomGPResultSetFormatter();

	    int size = sources.size();

	    resultSet.setCountResponse(new CountSet() {

		public int getCount() {

		    return size;
		}
	    });

	    break;
	}

	sources.forEach(d -> resultSet.getResultsList().add(d));

	response = formatter.format(message, resultSet);

	return response;
    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {

	return MediaType.APPLICATION_JSON_TYPE;
    }

    @Override
    public String getStringResponse(WebRequest webRequest) throws GSException {

	return null;
    }
}
