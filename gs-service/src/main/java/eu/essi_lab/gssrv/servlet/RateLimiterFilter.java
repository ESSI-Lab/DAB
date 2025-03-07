package eu.essi_lab.gssrv.servlet;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response.Status;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.ratelimiter.ExecutionModeSetting;
import eu.essi_lab.cfga.gs.setting.ratelimiter.RateLimiterSetting;
import eu.essi_lab.cfga.gs.setting.ratelimiter.RateLimiterSetting.ComputationType;
import eu.essi_lab.configuration.ExecutionMode;
import eu.essi_lab.lib.servlet.RequestManager;
import eu.essi_lab.lib.utils.Chronometer;
import eu.essi_lab.lib.utils.ExpiringCache;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.RuntimeInfoElement;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.AbstractRequestBouncer;
import eu.essi_lab.pdk.DistributedRequestBouncer;
import eu.essi_lab.pdk.LocalRequestBouncer;
import eu.essi_lab.rip.RuntimeInfoProvider;
import eu.essi_lab.shared.driver.es.stats.ElasticsearchInfoPublisher;

/**
 * @author Fabrizio
 */
public class RateLimiterFilter implements Filter {

    /**
     * 
     */
    private static final long EXECUTION_WAIT_TIME = 10;

    /**
     * 
     */
    private static final int TOO_MANY_REQUESTS_STATUS = 429;

    /**
     * 
     */
    private Chronometer chronometer;
    private static ExpiringCache<String> successRequests;
    static {
	successRequests = new ExpiringCache<String>();
	successRequests.setDuration(TimeUnit.MINUTES.toMillis(20));
    }

    /**
     * 
     */
    private static AbstractRequestBouncer bouncer;

    /**
     * 
     */
    private static boolean everythingIsBlocked;

    /**
     * @return
     */
    public static boolean everythingIsBlocked() {

	return everythingIsBlocked;
    }

    /**
     * Initializing the Rate limiter here is safe, the configuration is ready
     * 
     * @param filterConfig
     * @throws ServletException
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

	if (skipInitialization()) {

	    return;
	}

	initRateLimiter();

	//
	//
	//

	ConfigurationWrapper.getConfiguration().get().addChangeEventListener(l -> {

	    initRateLimiter();
	});
    }

    @Override
    public void doFilter(//
	    ServletRequest servletRequest, //
	    ServletResponse response, //
	    FilterChain filterChain) throws IOException, ServletException {

	// updates the current Thread name
	String requestId = UUID.randomUUID().toString();
	RequestManager.getInstance().updateThreadName(getClass(), requestId);

	// parameter MUST be false to avoid consuming the stream
	WebRequest webRequest = new WebRequest((HttpServletRequest) servletRequest, false, requestId);

	String requestPath = webRequest.getRequestPath();
	String address = webRequest.getRemoteAddress();

	//
	//
	//

	RateLimiterSetting setting = ConfigurationWrapper.getRateLimiterSettingSettings();

	if (bouncer == null || //
		address.startsWith("149.139.19") || // CNR-IIA
		address.equals("[0:0:0:0:0:0:0:1]") || //
		requestPath.contains("configuration") || //
		requestPath.contains("check") || //
		requestPath.contains("giapi") || //
		requestPath.contains("request-monitor") || //
		requestPath.endsWith("/wms") || //
		requestPath.endsWith("/wms-extent") || //
		requestPath.endsWith("/wms-cluster") || //
		requestPath.contains("/FeatureServer/") || //
		requestPath.contains("/worldcereal/") || //
		setting.getComputationType() == ComputationType.DISABLED) {
	    GSLoggerFactory.getLogger(getClass()).info("Skipping RLF");
	    filterChain.doFilter(servletRequest, response);
	    return;
	}

	if (chronometer == null) {
	    chronometer = new Chronometer();
	    chronometer.start();
	}

	//
	//
	//

	Optional<ElasticsearchInfoPublisher> publisher = ElasticsearchInfoPublisher.create(webRequest);

	HttpServletResponse httpResponse = (HttpServletResponse) response;

	boolean ret;
	try {

	    ret = bouncer.askForExecutionAndWait(address, requestId, EXECUTION_WAIT_TIME, TimeUnit.MINUTES);

	} catch (Exception ex) {

	    publish(publisher, webRequest, Status.INTERNAL_SERVER_ERROR.getStatusCode());

	    bouncer.notifyExecutionEnded(address, requestId);

	    GSLoggerFactory.getLogger(getClass()).error(ex);

	    httpResponse.sendError(Status.INTERNAL_SERVER_ERROR.getStatusCode(), ex.getMessage());

	    return;
	}

	if (!ret) {

	    //
	    // if too many requests from a specific IP or after 10 minutes waiting no free slot is made available
	    // (e.g. other requests from this IP are blocking this execution)
	    //

	    bouncer.notifyExecutionEnded(address, requestId);

	    GSLoggerFactory.getLogger(getClass())
		    .error("Waiting for other pending requests to complete before accepting new requests from address: {}", address);

	    if (chronometer.getElapsedTimeMillis() > TimeUnit.MINUTES.toMillis(10) && successRequests.size() == 0) {
		everythingIsBlocked = true;
	    }

	    httpResponse.sendError(TOO_MANY_REQUESTS_STATUS,
		    "Waiting for other pending requests to complete before accepting new requests from address: " + address);

	    publish(publisher, webRequest, TOO_MANY_REQUESTS_STATUS);

	    return;
	}

	filterChain.doFilter(servletRequest, response);

	successRequests.put(requestId, requestId);

	bouncer.notifyExecutionEnded(address, requestId);
    }

    @Override
    public void destroy() {

	if (bouncer != null && bouncer instanceof DistributedRequestBouncer) {

	    ((DistributedRequestBouncer) bouncer).getPool().close();
	}
    }

    /**
     * @param publisher
     * @param webRequest
     * @param status
     */
    private void publish(Optional<ElasticsearchInfoPublisher> publisher, WebRequest webRequest, int status) {

	publisher.ifPresent(p -> {
	    try {
		p.publish(webRequest);
		p.publish(new RuntimeInfoProvider() {

		    @Override
		    public HashMap<String, List<String>> provideInfo() {

			HashMap<String, List<String>> map = new HashMap<>();
			map.put(//
				RuntimeInfoElement.CHRONOMETER_TIME_STAMP.getName(), //
				Arrays.asList(ISO8601DateTimeUtils.getISO8601DateTimeWithMilliseconds()));

			map.put(//
				RuntimeInfoElement.RESPONSE_STATUS.getName(), //
				Arrays.asList(String.valueOf(status)));

			return map;
		    }

		    @Override
		    public String getName() {

			return "RateLimiterFilter";
		    }

		    @Override
		    public String getBaseType() {

			return getName();
		    }
		});

		p.write();

	    } catch (GSException e) {

		GSLoggerFactory.getLogger(getClass()).error(e);
	    }
	});
    }

    /**
     * @return
     */
    private boolean skipInitialization() {

	//
	// skips initialization for non prod/preprod execution modes
	//

	switch (ExecutionMode.get()) {
	case ACCESS:
	case FRONTEND:
	case INTENSIVE:

	    return false;

	case AUGMENTER:
	case BATCH:
	case BULK:
	case CONFIGURATION:
	case LOCAL_PRODUCTION:
	case MIXED:

	    GSLoggerFactory.getLogger(getClass()).info("RequestBouncer initialization SKIPPED");

	    return true;
	}

	//
	// skips initialization if disabled
	//

	RateLimiterSetting setting = ConfigurationWrapper.getRateLimiterSettingSettings();

	if (setting.getComputationType() == ComputationType.DISABLED) {

	    GSLoggerFactory.getLogger(getClass()).info("RequestBouncer disabled, initialization SKIPPED");

	    return true;
	}

	return false;
    }

    /**
     * 
     */
    private void initRateLimiter() {

	RateLimiterSetting setting = ConfigurationWrapper.getRateLimiterSettingSettings();

	GSLoggerFactory.getLogger(getClass()).info("RequestBouncer initialization STARTED");

	String db = setting.getDefaultDB();
	Integer overallMaxRequestsPerIp = setting.getDefaultMaxRequestsPerIP();
	Integer maxConcurrentRequests = setting.getDefaultMaxConcurrentRequests();
	Integer maxConcurrentRequestsPerIp = setting.getDefaultMaxConcurrenRequestsPerIP();
	String hostname = setting.getHostName();
	Integer port = setting.getPort();

	Optional<ExecutionModeSetting> specificSetting = setting.getExecutionModeSetting(ExecutionMode.get());

	if (specificSetting.isPresent()) {

	    String tmpDb = specificSetting.get().getDB();

	    if (tmpDb != null && !tmpDb.isEmpty()) {
		db = tmpDb;
	    }

	    Integer tmpOverallMaxRequestsPerIp = specificSetting.get().getMaxRequestsPerIP();
	    if (tmpOverallMaxRequestsPerIp != null) {
		overallMaxRequestsPerIp = tmpOverallMaxRequestsPerIp;
	    }

	    Integer tmpMaxRequests = specificSetting.get().getMaxConcurrentRequests();
	    if (tmpMaxRequests != null) {
		maxConcurrentRequests = tmpMaxRequests;
	    }

	    Integer tmpMaxRequestsPerIp = specificSetting.get().getMaxConcurrentRequestsPerIP();
	    if (tmpMaxRequestsPerIp != null) {
		maxConcurrentRequestsPerIp = tmpMaxRequestsPerIp;
	    }
	}

	AbstractRequestBouncer tmp = null;

	if (setting.getComputationType().equals(ComputationType.DISTRIBUTED)) {
	    try {
		if (bouncer != null && bouncer instanceof DistributedRequestBouncer) {
		    DistributedRequestBouncer drb = (DistributedRequestBouncer) bouncer;
		    if (drb.getHostname().equals(hostname) && //
			    port == drb.getPort() && //
			    db.equals(drb.getHash())) {
			// it is already initialized.. just setting
			bouncer.setMaximumOverallRequestsPerIp(overallMaxRequestsPerIp);
			bouncer.setMaxConcurrentRequests(maxConcurrentRequests);
			bouncer.setMaxConcurrentRequestsPerIP(maxConcurrentRequestsPerIp);
			GSLoggerFactory.getLogger(getClass()).info("Distributed bouncer just setting");
			return;
		    }
		}
		tmp = new DistributedRequestBouncer(hostname, port, db, maxConcurrentRequests, maxConcurrentRequestsPerIp,
			overallMaxRequestsPerIp);
	    } catch (Exception e) {
		GSLoggerFactory.getLogger(getClass()).error(e);
	    }
	}

	if (tmp != null) {
	    bouncer = tmp;
	} else {
	    bouncer = new LocalRequestBouncer(maxConcurrentRequests, maxConcurrentRequestsPerIp, overallMaxRequestsPerIp);
	}

	GSLoggerFactory.getLogger(getClass()).info("RequestBouncer initialization ENDED");
    }

    public static void main(String[] args) {
	RateLimiterFilter rlf = new RateLimiterFilter();
	System.out.println(rlf.everythingIsBlocked());
    }
}
