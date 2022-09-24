package eu.essi_lab.profiler.timeseries;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.ServiceLoader;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.namespace.QName;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.base.Charsets;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.xml.stax.StAXDocumentParser;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.StreamingRequestHandler;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;
import eu.essi_lab.request.executor.IAccessExecutor;
import eu.essi_lab.request.executor.IDiscoveryStringExecutor;

public class ProvidersHandler extends StreamingRequestHandler {

    private static IDiscoveryStringExecutor executor;
    private static IAccessExecutor accessExecutor;

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {
	ValidationMessage ret = new ValidationMessage();
	ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	return ret;
    }

    static {
	ServiceLoader<IDiscoveryStringExecutor> loader = ServiceLoader.load(IDiscoveryStringExecutor.class);
	executor = loader.iterator().next();

	ServiceLoader<IAccessExecutor> accessLoader = ServiceLoader.load(IAccessExecutor.class);
	accessExecutor = accessLoader.iterator().next();
    }

    public ProvidersHandler() {

    }

    @Override
    public StreamingOutput getStreamingResponse(WebRequest webRequest) throws GSException {

	return new StreamingOutput() {

	    private String provider;

	    @Override
	    public void write(OutputStream output) throws IOException, WebApplicationException {

		DiscoveryRequestTransformer transformer = getTransformer();

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
		OutputStreamWriter writer = new OutputStreamWriter(output, Charsets.UTF_8);

		Page userPage = discoveryMessage.getPage();
		int userSize = userPage.getSize();
		int pageSize = Math.min(userSize, 1000);
		userPage.setSize(pageSize);

		ResultSet<String> resultSet = null;
		int tempSize = 0;

		JSONObject ret = new JSONObject();
		JSONArray providers = new JSONArray();
		ret.put("providers", providers);
		do {

		    try {
			resultSet = executor.retrieveStrings(discoveryMessage);
			List<String> results = resultSet.getResultsList();
			tempSize += pageSize;

			if (results.isEmpty()) {
			    printErrorMessage(output, "No " + getObject() + " matched");
			    return;
			}

			boolean first = true;
			for (String result : results) {

			    first = false;

			    StAXDocumentParser parser = new StAXDocumentParser(result);
			    parser.add(new QName("sourceId"), v -> provider = v);
			    parser.parse();

			    Optional<String> label = ConfigurationWrapper.getAllSources().stream()
				    .filter(s -> s.getUniqueIdentifier().equals(provider)).map(s -> s.getLabel()).findFirst();

			    if (provider != null && !provider.isEmpty()) {
				JSONObject providerObject = new JSONObject();
				providerObject.put("code", provider);
				if (label.isPresent()) {
				    providerObject.put("label", label.get());
				}

				providers.put(providerObject);

			    }

			}
		    } catch (Exception e) {
			e.printStackTrace();
		    }
		    int rest = userSize - tempSize;
		    if (rest > 0 && rest < pageSize) {
			userPage.setSize(rest);
		    }
		    userPage.setStart(userPage.getStart() + pageSize);

		} while (tempSize < userSize && tempSize < resultSet.getCountResponse().getCount()
			&& !resultSet.getResultsList().isEmpty());

		writer.write(ret.toString());
		writer.flush();
		writer.close();
		output.close();

	    }

	};

    }

    public void printErrorMessage(OutputStream output, String message) throws IOException {
	OutputStreamWriter writer = new OutputStreamWriter(output);
	JSONObject error = new JSONObject();
	error.put("message", message);
	writer.write(error.toString());
	writer.close();
    }

    public String getObject() {
	return "providers";
    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {
	return MediaType.APPLICATION_JSON_TYPE;
    }

    public DiscoveryRequestTransformer getTransformer() {
	return new ProvidersTransformer();
    }

}
