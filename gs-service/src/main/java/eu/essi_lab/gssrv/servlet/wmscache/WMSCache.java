package eu.essi_lab.gssrv.servlet.wmscache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import com.amazonaws.util.IOUtils;

import eu.essi_lab.authorization.userfinder.UserFinder;
import eu.essi_lab.gssrv.rest.ESSIProfilerService;
import eu.essi_lab.gssrv.rest.SimpleHttpServletRequest;
import eu.essi_lab.gssrv.rest.SimpleUriInfo;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.auth.GSUser;

public class WMSCache {

    private static final String DEFAULT_FOLDER_NAME = "wms-cache";

    public static String CACHE_HOST = "cache-host.dab";

    private static WMSCache instance = null;

    WMSCacheStats stats;

    public WMSCacheStats getStats() {
	return stats;
    }

    public void setStats(WMSCacheStats stats) {
	this.stats = stats;
    }

    WMSCacheStorage storage;

    public WMSCacheStorage getStorage() {
	return storage;
    }

    public void setStorage(WMSCacheStorage storage) {
	this.storage = storage;
    }

    private WMSCache() {
	initInMemory(DEFAULT_FOLDER_NAME);
    }

    public static WMSCache getInstance() {
	if (instance == null) {
	    instance = new WMSCache();
	}
	return instance;
    }

    public Response getCachedResponse(String profile, WebRequest webRequest) {
	String[] viewLayerHashRequest = extractViewLayerHashRequest(profile, webRequest);
	if (viewLayerHashRequest == null) {
	    return null;
	}
	String view = viewLayerHashRequest[0];
	String layer = viewLayerHashRequest[1];
	String hash = viewLayerHashRequest[2];
	String request = viewLayerHashRequest[3];
	stats.incrementUsage(view, layer, hash);
	stats.storeRequest(view, layer, hash, request);
	stats.updateLeaderboard(view, layer, hash);

	File ret = storage.getCachedResponse(view, layer, hash);
	if (ret != null && ret.exists()) {
	    try (InputStream is = new FileInputStream(ret)) {
		Response.ResponseBuilder builder = Response.ok(is);
		builder.type(MediaType.valueOf("image/png")); // set MIME type
		builder.header("Content-Disposition", "inline; filename=\"" + hash + ".png\"");
		builder.entity(ret);
		return builder.build();
	    } catch (IOException e) {
		return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to read image").build();
	    }
	}
	return null;
    }

    public void cacheResponse(String profile, WebRequest request, byte[] body) {
	GSLoggerFactory.getLogger(getClass()).info("WMS CACHE TILE");
	String[] viewLayerHashRequest = extractViewLayerHashRequest(profile, request);
	if (viewLayerHashRequest == null) {
	    return;
	}
	String view = viewLayerHashRequest[0];
	String layer = viewLayerHashRequest[1];
	String hash = viewLayerHashRequest[2];

	try {
	    File tmpFile = File.createTempFile(getClass().getSimpleName(), ".png");
	    ByteArrayInputStream bis = new ByteArrayInputStream(body);
	    OutputStream os = new FileOutputStream(tmpFile);
	    IOUtils.copy(bis, os);
	    bis.close();
	    os.close();
	    storage.putCachedResponse(view, layer, hash, tmpFile);
	    tmpFile.delete();
	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

    }

    private String[] extractViewLayerHashRequest(String profile, WebRequest request) {
	Optional<String> qs = request.getOptionalQueryString();
	Optional<String> optionalView = request.extractViewId();
	String fullUrl = request.getServletRequest().getRequestURL()
		.append(request.getQueryString() != null ? "?" + request.getQueryString() : "").toString();
	String view = null;
	if (optionalView.isPresent()) {
	    view = optionalView.get();
	}
	if (qs.isPresent()) {
	    String query = qs.get();
	    String layer = query.toLowerCase();
	    if (layer.contains("layers=")) {
		layer = layer.substring(layer.indexOf("layers="));
		if (layer.contains("&")) {
		    layer = layer.substring(0, layer.indexOf("&"));
		}
		layer = layer.replace("layers=", "");
		layer = profile + ":" + layer;

		try {
		    String hash = StringUtils.hashSHA256messageDigest(query);
		    String[] ret = new String[] { view, layer, hash, fullUrl };
		    return ret;
		} catch (Exception e) {
		    e.printStackTrace();
		    GSLoggerFactory.getLogger(getClass()).error(e);
		}

	    }
	}
	return null;
    }

    public void refreshCache(int cacheSize) {
	List<String> views = stats.getViews();
	for (String view : views) {
	    List<String> layers = stats.getLayers(view);
	    for (String layer : layers) {
		GSLoggerFactory.getLogger(getClass()).info("Refreshing cache of view {}, layer {}", view, layer);
		List<Entry<String, Double>> topRequests = stats.getTopRequests(view, layer, cacheSize);
		GSLoggerFactory.getLogger(getClass()).info("Found {} target tiles", topRequests.size());
		ESSIProfilerService eps = new ESSIProfilerService();
		for (Entry<String, Double> topRequest : topRequests) {
		    try {
			String hash = topRequest.getKey();
			Double value = topRequest.getValue();
			String request = stats.loadRequest(view, layer, hash);
			GSLoggerFactory.getLogger(getClass()).info("Refreshing " + hash + " " + value);

			URL oldUrl = new URL(request);
			URL url = new URL(oldUrl.getProtocol(), CACHE_HOST, oldUrl.getPort(), oldUrl.getFile());

			String path = url.getPath();
			if (path.endsWith("/")) {
			    path = path.substring(0, path.length() - 1);
			}
			if (path.contains("/")) {
			    path = path.substring(path.lastIndexOf("/") + 1);
			}
			SimpleUriInfo uriInfo = new SimpleUriInfo(url.toURI());
			Optional<String> token = WebRequest.extractTokenId(url);
			String tk = null;
			if (token.isPresent()) {
			    tk = token.get();
			}
			SimpleHttpServletRequest hsr = new SimpleHttpServletRequest(url.toURI());
			GSUser user = UserFinder.findCurrentUser(hsr);
			hsr.setAttribute(WebRequest.HTTP_SERVLET_REQUEST_USER_ATTRIBUTE, user);
			Response response = eps.getRequestWithTokenAndView(hsr, uriInfo, tk, view, path);
			WebRequest webRequest = new WebRequest(hsr, false);

			Object entity = response.getEntity();

			if (entity instanceof byte[] bytes) {
			    cacheResponse(path, webRequest, bytes);
			} else if (entity instanceof StreamingOutput so) {
			    ByteArrayOutputStream baos = new ByteArrayOutputStream();
			    so.write(baos);
			    byte[] bytes = baos.toByteArray();
			    cacheResponse(path, webRequest, bytes);
			} else {
			    GSLoggerFactory.getLogger(getClass()).error("Unexpected content {}" + entity.getClass().getName());
			}

		    } catch (Exception e) {
			e.printStackTrace();
			GSLoggerFactory.getLogger(getClass()).error(e);
		    }
		}
	    }
	}

    }

    public void initInMemory(String foldername) {
	String name = DEFAULT_FOLDER_NAME;
	if (foldername != null && !foldername.isEmpty()) {
	    name = foldername;
	}
	if (stats == null || !(stats instanceof WMSCacheStatsOnMemory)) {
	    stats = new WMSCacheStatsOnMemory();
	}
	if (storage == null) {
	    storage = new WMSCacheStorageOnDisk(name);
	}
	if (storage instanceof WMSCacheStorageOnDisk wsod) {
	    String existingFolder = wsod.getCacheSubFolder();
	    if (!existingFolder.equals(name)) {
		storage = new WMSCacheStorageOnDisk(name);
	    }
	} else {
	    storage = new WMSCacheStorageOnDisk(name);
	}
    }

}
