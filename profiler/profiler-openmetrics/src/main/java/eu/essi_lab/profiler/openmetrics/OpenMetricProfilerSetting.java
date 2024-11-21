/**
 * 
 */
package eu.essi_lab.profiler.openmetrics;

import eu.essi_lab.cfga.gs.setting.ProfilerSetting;

/**
 * @author Fabrizio
 */
public class OpenMetricProfilerSetting extends ProfilerSetting{

    /**
     * 
     */
    public OpenMetricProfilerSetting() {
	
	setServiceName("OpenMetrics");
	setServiceType("OPEN_METRICS");
	setServicePath("metrics");
	setServiceVersion("1.0");
    }
}
