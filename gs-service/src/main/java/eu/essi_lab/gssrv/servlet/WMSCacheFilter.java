package eu.essi_lab.gssrv.servlet;

import java.io.File;
import java.io.FileInputStream;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import com.amazonaws.util.IOUtils;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.WMSCacheSetting;
import eu.essi_lab.cfga.gs.setting.WMSCacheSetting.WMSCacheMode;
import eu.essi_lab.gssrv.servlet.wmscache.CachedBodyHttpServletResponse;
import eu.essi_lab.gssrv.servlet.wmscache.WMSCache;
import eu.essi_lab.lib.servlet.RequestManager;
import eu.essi_lab.lib.utils.Chronometer.TimeFormat;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.pdk.ChronometerInfoProvider;

/**
 * @author boldrini
 */
public class WMSCacheFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

	ConfigurationWrapper.getConfiguration().get().addChangeEventListener(l -> {

	    initWMSCache();
	});
    }

    public static boolean enabled = false;

    @Override
    public void doFilter(//
	    ServletRequest servletRequest, //
	    ServletResponse servletResponse, //
	    FilterChain filterChain) throws IOException, ServletException {

	if (!enabled) {
	    // continue to next filter
	    filterChain.doFilter(servletRequest, servletResponse);
	    return;
	}

	// updates the current Thread name
	String requestId = UUID.randomUUID().toString();
	RequestManager.getInstance().updateThreadName(getClass(), requestId);

	// parameter MUST be false to avoid consuming the stream
	WebRequest webRequest = new WebRequest((HttpServletRequest) servletRequest, false, requestId);

	String requestPath = webRequest.getRequestPath();

	//
	//
	//

	if ( // requestPath.endsWith("/wms") || //
	     // requestPath.endsWith("/wms-extent") || //
	requestPath.endsWith("/wms-cluster") //

	) {

	    StringBuffer requestURL = webRequest.getServletRequest().getRequestURL();
	    URL url = new URL(requestURL.toString());
	    String hostname = url.getHost();
	    boolean useCache = true;
	    if (hostname.equals(WMSCache.CACHE_HOST)) {
		useCache = false;
	    }

	    requestPath = requestPath.substring(requestPath.lastIndexOf("/") + 1);

	    Response cached = WMSCache.getInstance().getCachedResponse(requestPath, webRequest);

	    // CACHE HIT

	    if (useCache && cached != null) {
		ChronometerInfoProvider chronometer = new ChronometerInfoProvider(TimeFormat.MIN_SEC_MLS);
		chronometer.start();
		GSLoggerFactory.getLogger(getClass()).info("{} ENDED - handling time: {}",
			getRequestLogPrefix(webRequest) + " WMS CACHE HIT", chronometer.formatElapsedTime());
		HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
		httpResponse.setStatus(cached.getStatus());
		httpResponse.setContentType(cached.getMediaType().getType());
		MultivaluedMap<String, String> headers = cached.getStringHeaders();
		Set<Entry<String, List<String>>> entries = headers.entrySet();
		for (Entry<String, List<String>> entry : entries) {
		    String key = entry.getKey();
		    List<String> values = entry.getValue();
		    if (!values.isEmpty()) {
			httpResponse.setHeader(key, values.get(0));
		    }
		}
		Object entity = cached.getEntity();
		if (entity != null) {
		    if (entity instanceof StreamingOutput so) {
			so.write(httpResponse.getOutputStream());
		    } else if (entity instanceof InputStream is) {
			try (InputStream in = is; OutputStream out = httpResponse.getOutputStream()) {
			    in.transferTo(out);
			}
		    } else if (entity instanceof byte[] bytes) {
			httpResponse.getOutputStream().write(bytes);
		    } else if (entity instanceof File file) {
			FileInputStream fis = new FileInputStream(file);
			IOUtils.copy(fis, httpResponse.getOutputStream());
			fis.close();

		    } else {
			httpResponse.getWriter().write(entity.toString());
		    }
		}
		return;
	    } else {
		// must be cached

		HttpServletResponse response = (HttpServletResponse) servletResponse;
		CachedBodyHttpServletResponse wrapper = new CachedBodyHttpServletResponse(response);

		filterChain.doFilter(servletRequest, wrapper);

		byte[] body = wrapper.getBody();

		WMSCache.getInstance().cacheResponse(requestPath, webRequest, body);

		response.setContentLength(body.length);
		response.getOutputStream().write(body);

		return;

	    }

	}

	// NOT A WMS

	filterChain.doFilter(servletRequest, servletResponse);
	return;

    }

    @Override
    public void destroy() {

    }

    private String getRequestLogPrefix(WebRequest request) {

	return "[WMS cache] REQ# " + request.getRequestId();
    }

    /**
     * 
     */
    private void initWMSCache() {
	GSLoggerFactory.getLogger(getClass()).info("WMS Cache initialization STARTED");

	Optional<WMSCacheSetting> setting = ConfigurationWrapper.getWMSCacheSettings();

	if (setting != null && setting.isPresent()) {
	    WMSCacheMode mode = setting.get().getMode();

	    if (mode != null) {
		switch (mode) {
		case DISABLED: {
		    enabled = false;
		}
		case LOCAL_FILESYSTEM: {
		    Optional<String> name = setting.get().getFoldername();
		    String n = null;
		    if (name.isPresent()) {
			n = name.get();
		    }
		    WMSCache.getInstance().initInMemory(n);
		    enabled = true;
		}
		case REDIS: {
		    enabled = true;
		}
		default:
		    enabled = false;
		}
	    } else {
		enabled = false;
	    }

	} else {
	    enabled = false;
	}

	GSLoggerFactory.getLogger(getClass()).info("WMS Cache initialization ENDED");
    }

}
