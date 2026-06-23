package eu.essi_lab.gssrv.servlet.wmscache;

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

import com.amazonaws.util.*;
import eu.essi_lab.authorization.userfinder.*;
import eu.essi_lab.gssrv.rest.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.messages.web.*;
import eu.essi_lab.model.auth.*;
import jakarta.ws.rs.core.*;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.*;
import java.util.stream.Collectors;

import eu.essi_lab.gssrv.servlet.WMSCacheFilter;
import eu.essi_lab.messages.bond.spatial.ShapeLayerOwner;

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

    /** View id for predefined shape WMS tiles (e.g. {@code his-central-shapes}); optional. */
    private String shapeView;

    public WMSCacheStorage getStorage() {
	return storage;
    }

    public void setStorage(WMSCacheStorage storage) {
	this.storage = storage;
    }

    public Optional<String> getShapeView() {
	return Optional.ofNullable(shapeView).filter(v -> !v.isBlank());
    }

    public void setShapeView(String shapeView) {
	this.shapeView = shapeView;
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

	return storage.getCachedResponse(view, layer, hash);

    }

    /**
     * @param profile WMS profile folder name ({@code wms}, {@code wms-cluster}, ...)
     * @param layersQueryValue raw {@code layers=} query value
     * @return cache layer key ({@code profile:layers})
     */
    public static String toCacheLayerKey(String profile, String layersQueryValue) {

	return profile + ":" + layersQueryValue.toLowerCase();
    }

    /**
     * Cache layer folder key for a predefined shape online id, matching {@link #extractViewLayerHashRequest}.
     */
    public static String toCacheLayerKeyFromOnlineId(String profile, String onlineId) {

	String layersValue = URLEncoder.encode(onlineId, StandardCharsets.UTF_8).replace("+", "%20").toLowerCase();

	return profile + ":" + layersValue;
    }

    /**
     * Removes cached GetMap tiles and stats for predefined shape entry names (after OpenSearch delete).
     *
     * @param entryNames shape-files folder entry names that were removed
     */
    public static void invalidatePredefinedShapeEntries(Collection<String> entryNames) {

	invalidatePredefinedShapeEntries(entryNames, null);
    }

    /**
     * @param entryNames shape-files folder entry names that were removed
     * @param shapeView WMS view id from the portal (e.g. {@code his-central-shapes}); falls back to cache settings when blank
     */
    public static void invalidatePredefinedShapeEntries(Collection<String> entryNames, String shapeView) {

	if (!WMSCacheFilter.enabled || entryNames == null || entryNames.isEmpty()) {
	    return;
	}

	List<String> cacheLayerKeys = entryNames.stream()//
		.filter(name -> name != null && !name.isBlank())//
		.map(name -> toCacheLayerKeyFromOnlineId("wms", ShapeLayerOwner.OPENSEARCH_ONLINE_PREFIX + name))//
		.collect(Collectors.toList());

	getInstance().invalidatePredefinedShapeLayers(cacheLayerKeys, shapeView);
    }

    /**
     * @param cacheLayerKeys layer keys as stored under {@code view/layer/} in cache storage
     */
    public void invalidatePredefinedShapeLayers(Collection<String> cacheLayerKeys) {

	invalidatePredefinedShapeLayers(cacheLayerKeys, null);
    }

    /**
     * @param cacheLayerKeys layer keys as stored under {@code view/layer/} in cache storage
     * @param requestShapeView WMS view id from the portal; falls back to {@link #getShapeView()} when blank
     */
    public void invalidatePredefinedShapeLayers(Collection<String> cacheLayerKeys, String requestShapeView) {

	if (cacheLayerKeys == null || cacheLayerKeys.isEmpty() || storage == null) {
	    return;
	}

	Optional<String> shapeView = Optional.ofNullable(requestShapeView).filter(v -> !v.isBlank());
	if (shapeView.isEmpty()) {
	    shapeView = getShapeView();
	}

	for (String cacheLayerKey : cacheLayerKeys) {

	    if (cacheLayerKey == null || cacheLayerKey.isBlank()) {
		continue;
	    }

	    GSLoggerFactory.getLogger(getClass()).info("Invalidating predefined shape WMS cache layer {} (view={})",
		    cacheLayerKey, shapeView.orElse("all"));

	    if (shapeView.isPresent()) {
		storage.deleteCachedLayer(shapeView.get(), cacheLayerKey);
		if (stats != null) {
		    stats.deleteLayer(shapeView.get(), cacheLayerKey);
		}
	    } else {
		invalidateCachedWmsLayers(List.of(cacheLayerKey));
	    }
	}
    }

    /**
     * @param cacheLayerKeys layer keys as stored in cache ({@code profile:layerName})
     */
    public void invalidateCachedWmsLayers(Collection<String> cacheLayerKeys) {

	if (cacheLayerKeys == null || cacheLayerKeys.isEmpty() || storage == null) {
	    return;
	}

	for (String cacheLayerKey : cacheLayerKeys) {

	    if (cacheLayerKey == null || cacheLayerKey.isBlank()) {
		continue;
	    }

	    GSLoggerFactory.getLogger(getClass()).info("Invalidating WMS cache layer {}", cacheLayerKey);

	    storage.deleteCachedLayerAllViews(cacheLayerKey);

	    if (stats != null) {
		for (String view : stats.getViews()) {
		    stats.deleteLayer(view, cacheLayerKey);
		}
	    }
	}
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
	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

    }

    private String[] extractViewLayerHashRequest(String profile, WebRequest request) {
	Optional<String> qs = request.getOptionalQueryString();
	Optional<String> optionalView = request.extractViewId();
	String fullUrl = request.getServletRequest().getRequestURL()
		.append(request.getOptionalQueryString().isPresent() ? "?" + request.getOptionalQueryString().get() : "").toString();
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

			URI oldUri = new URI(request);

			URI newUri = new URI( //
				oldUri.getScheme(),//
				oldUri.getUserInfo(),//
				CACHE_HOST,//
				oldUri.getPort(),//
				oldUri.getPath(),//
				oldUri.getQuery(),//
				oldUri.getFragment()//
			);

			URL url = newUri.toURL();

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

    public Integer getMaxSize() {
	return storage.getMaxSize();
    }

    public void setMaxSize(Integer size) {
	storage.setMaxSize(size);
    }

    public void initRedisS3(String redisHostname, String s3hostName, String s3Username, String s3Password, String s3Bucket) {
	URL url;
	try {
	    url = new URI(redisHostname).toURL();
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error(e);
	    return;
	}
	if (stats == null) {
	    stats = new WMSCacheStatsOnRedis(url.getHost(), url.getPort());
	} else {
	    if (stats instanceof WMSCacheStatsOnRedis wcsr) {
		if (wcsr.getHost().equals(redisHostname) && wcsr.getPort() == url.getPort()) {
		    // nothing to do
		} else {
		    stats = new WMSCacheStatsOnRedis(url.getHost(), url.getPort());
		}
	    } else {
		stats = new WMSCacheStatsOnRedis(url.getHost(), url.getPort());
	    }
	}
	if (storage == null) {
	    storage = new WMSCacheStorageOnS3(s3hostName, s3Username, s3Password, s3Bucket);
	} else {
	    if (storage instanceof WMSCacheStorageOnS3 ws3) {
		if (ws3.getHostname().equals(s3hostName) && ws3.getUsername().equals(s3Username) && ws3.getPassword().equals(s3Password)
			&& ws3.getBucketname().equals(s3Bucket)) {
		    // nothing to do
		} else {
		    storage = new WMSCacheStorageOnS3(s3hostName, s3Username, s3Password, s3Bucket);
		}
	    } else {
		storage = new WMSCacheStorageOnS3(s3hostName, s3Username, s3Password, s3Bucket);
	    }
	}

    }

}
