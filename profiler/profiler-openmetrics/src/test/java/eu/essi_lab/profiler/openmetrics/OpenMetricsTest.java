package eu.essi_lab.profiler.openmetrics;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Test;

import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

public class OpenMetricsTest {

    @Test
    public void test() {
	PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
	HashMap<String, Double> gauges = new HashMap<String, Double>();
	gauges.put("source1", 34.);
	io.micrometer.core.instrument.Gauge.builder("resources_total", gauges, s -> s.get("source1"))//
		.description("Total number of resources")//
		.tag("source", "source1").//
		register(registry);
	String ret = registry.scrape();
	assertNotNull(ret);
	assertTrue(!ret.isEmpty());
	System.out.println(ret);
    }

}
