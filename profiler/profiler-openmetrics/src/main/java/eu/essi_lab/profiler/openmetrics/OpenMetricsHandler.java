package eu.essi_lab.profiler.openmetrics;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import com.google.common.base.Charsets;

import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.StreamingRequestHandler;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

public class OpenMetricsHandler extends StreamingRequestHandler {
    private static HashMap<String, PrometheusMeterRegistry> registries = new HashMap<String, PrometheusMeterRegistry>();

    private static HashSet<String> interestingViews = new HashSet<String>();

    static {
	ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	Runnable task = new Runnable() {

	    @Override
	    public void run() {
		for (String view : interestingViews) {
		    
		}
	    }
	};
	scheduler.scheduleAtFixedRate(task, 2, 2, TimeUnit.MINUTES);
    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {
	return MediaType.valueOf("application/openmetrics-text");
    }

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {
	ValidationMessage ret = new ValidationMessage();
	ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	return ret;
    }

    public OpenMetricsHandler() {

    }

    @Override
    public StreamingOutput getStreamingResponse(WebRequest webRequest) throws GSException {

	Optional<String> optionalView = webRequest.extractViewId();
	String viewId = optionalView.isPresent() ? optionalView.get() : null;

	if (viewId != null) {
	    interestingViews.add(viewId);
	}

	return new StreamingOutput() {

	    @Override
	    public void write(OutputStream output) throws IOException, WebApplicationException {
		OutputStreamWriter writer = new OutputStreamWriter(output, Charsets.UTF_8);
		if (viewId == null) {
		    writer.write("A view id is needed to get metrics");
		    writer.flush();
		    writer.close();
		    return;
		}
		String ret = "";
		synchronized (registries) {
		    PrometheusMeterRegistry registry = registries.get(viewId);
		    if (registry == null) {
			PrometheusConfig config = PrometheusConfig.DEFAULT;
			registry = new PrometheusMeterRegistry(config);
			registries.put(viewId, registry);
		    }
		    ret = registry.scrape();
		}
		writer.write(ret);
		writer.flush();
		writer.close();
		output.close();
	    }
	};
    }

}
