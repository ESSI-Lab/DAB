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

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.pdk.handler.StreamingRequestHandler;
import eu.essi_lab.profiler.semantic.SourceStatistics;
import eu.essi_lab.profiler.semantic.Stats;
import io.micrometer.core.instrument.Gauge;
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
		GSLoggerFactory.getLogger(getClass()).info("Updating metrics");
		for (String view : interestingViews) {
		    GSLoggerFactory.getLogger(getClass()).info("Updating metrics for view {}", view);
		    SourceStatistics sourceStats = null;
		    try {
			sourceStats = new SourceStatistics(null, Optional.of(view), ResourceProperty.SOURCE_ID);
		    } catch (Exception e1) {
			e1.printStackTrace();
		    }
		    HashMap<String, Stats> overallStats = sourceStats.getStatistics();
		    HashMap<String, Integer> datasets = new HashMap<String, Integer>();
		    HashMap<String, Integer> platforms = new HashMap<String, Integer>();
		    HashMap<String, Integer> variables = new HashMap<String, Integer>();
		    synchronized (registries) {

			PrometheusMeterRegistry registry = registries.get(view);
			for (String source : overallStats.keySet()) {
			    GSSource s = ConfigurationWrapper.getSource(source);
			    Gauge.builder("source_info", () -> 1)//
				    .description("Metadata about each source.")//
				    .tags("source_id", source, "source_label", s.getLabel())//
				    .register(registry);
			    try {
				Stats stats = overallStats.get(source);
				datasets.put(source, Integer.parseInt(stats.getTimeSeriesCount()));
				io.micrometer.core.instrument.Gauge.builder("timeseries_total", datasets, g -> g.get(source))//
					.description("Total number of timeseries ")//
					.tag("source", source).//
					register(registry);

				platforms.put(source, Integer.parseInt(stats.getSiteCount()));
				io.micrometer.core.instrument.Gauge.builder("platforms_total", platforms, g -> g.get(source))//
					.description("Total number of platforms ")//
					.tag("source", source).//
					register(registry);

				variables.put(source, Integer.parseInt(stats.getAttributeCount()));
				io.micrometer.core.instrument.Gauge.builder("variables_total", variables, g -> g.get(source))//
					.description("Total number of variables ")//
					.tag("source", source).//
					register(registry);

				// String content = "<tr><td colspan='15'><br/>"//
				// + "Data provider: <b>" + source + "</b><br/>"//
				// + "#Platforms: " + stats.getSiteCount() + "<br/>"//
				// + "#Variables:" + stats.getAttributeCount() + "<br/>"//
				// + "#Timeseries:" + stats.getTimeSeriesCount() + "<br/>"//
				// + "Begin:" + stats.getBegin() + "<br/>"//
				// + "End:" + stats.getEnd() + "<br/>"//
				// + "BBOX(w,s,e,n): " + stats.getWest() + "," + stats.getSouth() + "," +
				// stats.getEast() +
				// ","
				// + stats.getNorth() + "<br/>" //
				// + "Altitude:" + stats.getMinimumAltitude() + "/" + stats.getMaximumAltitude() +
				// "<br/>"//
				// + "</td></tr>" + "" //
				// + "<tr>";
			    } catch (Exception e) {
				e.printStackTrace();
				GSLoggerFactory.getLogger(getClass()).error(e);
			    }

			}
		    }
		}
	    }
	};
	scheduler.scheduleAtFixedRate(task, 0, 1, TimeUnit.MINUTES);
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