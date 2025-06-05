package eu.essi_lab.pdk;

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import com.amazonaws.util.IOUtils;

import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.lib.utils.ExpiringCache;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.web.WebRequest;

public class ProxyCache {

    private static ExpiringCache<Response> cache = new ExpiringCache<>();

    static {
	cache.setMaxSize(200);
    }

    private static ProxyCache instance = null;

    private ProxyCache() {
    }

    public static ProxyCache getInstance() {
	if (instance == null) {
	    instance = new ProxyCache();
	}
	return instance;
    }

    public boolean isToBeCached(WebRequest request) {
	if (true) {
	    // return false;
	}
	if (request == null) {
	    return false;
	}
	HttpServletRequest servlet = request.getServletRequest();
	if (servlet == null) {
	    return false;
	}
	 if (new File("/home/boldrini").exists()) {
	 return false;
	 }

	String url = servlet.getRequestURL().toString();
//	if (url.contains("/ArcGIS/")  // ESRI FeatureService used by WHOS-Arctic portal
	// url.contains("/i-change/wms-extent")|| // WMS profiler used by I-CHANGE
//		url.contains("/oapi/") // OGC API Record used by WIS in a a box UI
//	) {
//	    return true;
//	}
	return false;
    }

    private String getHash(WebRequest request) {
	HttpServletRequest servlet = request.getServletRequest();
	String url = servlet.getRequestURL().toString();
	String query = servlet.getQueryString();
	String method = servlet.getMethod();
	String body = "";
	ClonableInputStream stream = request.getBodyStream();
	if (stream != null) {
	    InputStream c = stream.clone();
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    try {
		IOUtils.copy(c, baos);
		c.close();
		baos.close();
		body = new String(baos.toByteArray());
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
	return url + query + "-" + method + body.hashCode();
    }

    public Response cache(WebRequest request, Response response) {
	if (!isToBeCached(request)) {
	    return response;
	}
	Response clone = cloneResponse(response);
	if (clone == null) {
	    // something went wrong with the clone
	    return response;
	}
	cache.put(getHash(request), clone);
	GSLoggerFactory.getLogger(getClass()).info("[PROXY-CACHE] put");
	return getCachedResponse(request);
    }

    public Response cloneResponse(Response response) {
	Object entity = response.getEntity();
	InputStream originalInputStream;
	if (entity instanceof StreamingOutput) {
	    // not to be cached
	    return null;
	} else if (entity instanceof String) {
	    String stringEntity = (String) entity;
	    originalInputStream = new ByteArrayInputStream(stringEntity.getBytes());
	} else if (entity instanceof InputStream) {
	    originalInputStream = (InputStream) entity;
	} else {
	    GSLoggerFactory.getLogger(getClass()).error("[PROXY-CACHE] error cloning response");
	    return null;
	}
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	try {
	    IOUtils.copy(originalInputStream, baos);
	    originalInputStream.close();
	    baos.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	ByteArrayInputStream bufferedInputStream = new ByteArrayInputStream(baos.toByteArray());
	Response.ResponseBuilder builder = Response.fromResponse(response);
	builder.entity(bufferedInputStream);
	return builder.build();
    }

    public Response getCachedResponse(WebRequest request) {
	if (!isToBeCached(request)) {
	    return null;
	}
	Response ret = cache.get(getHash(request));
	if (ret != null) {
	    synchronized (ret) {
		GSLoggerFactory.getLogger(getClass()).info("[PROXY-CACHE] hit");
		try {
		    ret.readEntity(InputStream.class).reset();
		} catch (IOException e) {
		    e.printStackTrace();
		}
		Response clone = cloneResponse(ret);
		try {
		    ret.readEntity(InputStream.class).reset();
		} catch (IOException e) {
		    e.printStackTrace();
		}
		return clone;
	    }
	}
	GSLoggerFactory.getLogger(getClass()).info("[PROXY-CACHE] miss");
	return null;
    }

}
