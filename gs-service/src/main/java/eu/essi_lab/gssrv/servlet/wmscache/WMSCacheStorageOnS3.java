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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.lib.net.s3.S3TransferWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import software.amazon.awssdk.services.s3.model.CommonPrefix;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;
import jakarta.ws.rs.core.Response;

public class WMSCacheStorageOnS3 implements WMSCacheStorage {

    private int maxSize = 1000;
    private S3TransferWrapper manager;
    private String bucketname;
    private String hostname;
    public String getBucketname() {
        return bucketname;
    }

    public String getHostname() {
        return hostname;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    private String username;
    private String password;
    private static final String DEFAULT_FOLDER = "dab-wms-cache";

    public WMSCacheStorageOnS3(String hostname, String username, String password, String bucketname) {
	this.hostname = hostname;
	this.username = username;
	this.password = password;
	this.manager = new S3TransferWrapper();
	manager.setACLPublicRead(true);
	manager.setAccessKey(username);
	manager.setSecretKey(password);
	if (hostname != null && !hostname.isEmpty()) {
	    manager.setEndpoint(hostname);
	}
	manager.initialize();
	this.bucketname = bucketname;

    }

    @Override
    public Response getCachedResponse(String view, String layer, String hash) {
	try {
	    HeadObjectResponse metadata = manager.getObjectMetadata(bucketname, DEFAULT_FOLDER + "/" + view + "/" + layer + "/" + hash);
	    if (metadata==null) {
		return null;
	    }
	    URL url = manager.getObjectURL(bucketname, DEFAULT_FOLDER + "/" + view + "/" + layer + "/" + hash);
	    
	    if(url==null) {
		return null;
	    }
	    return Response.status(Response.Status.MOVED_PERMANENTLY) // 301
		    .location(url.toURI()).build();
	} catch (Exception e) {
	    return null;
	}
    }

    @Override
    public void putCachedResponse(String view, String layer, String hash, File file) {
	manager.uploadFile(file.getAbsolutePath(), bucketname, DEFAULT_FOLDER + "/" + view + "/" + layer + "/" + hash, "application/png");

    }

    @Override
    public Date getCachedResponseDate(String view, String layer, String hash) {
	try {
	    return manager.getObjectDate(bucketname, DEFAULT_FOLDER + "/" + view + "/" + layer + "/" + hash);
	} catch (Exception e) {
	    return null;
	}
    }

    @Override
    public void deleteCachedResponse(String view, String layer, String hash) {
	try {
	    manager.deleteObject(bucketname, DEFAULT_FOLDER + "/" + view + "/" + layer + "/" + hash);
	} catch (Exception e) {
	    throw new RuntimeException("Failed to delete cached response", e);
	}
    }

    @Override
    public void deleteCachedLayer(String view, String layer) {

	String prefix = DEFAULT_FOLDER + "/" + view + "/" + layer + "/";
	GSLoggerFactory.getLogger(getClass()).info("Deleting WMS cache folder s3://{}/{}", bucketname, prefix);
	deleteObjectsWithPrefix(prefix);
    }

    @Override
    public void deleteCachedLayerAllViews(String layer) {

	for (String view : getViews()) {
	    deleteCachedLayer(view, layer);
	}
    }

    private void deleteObjectsWithPrefix(String prefix) {

	String token = null;
	int page = 0;
	int deletedTotal = 0;

	do {

	    page++;
	    ListObjectsV2Response response = manager.listObjects(bucketname, prefix, token);
	    List<String> keys = response.contents().stream().map(S3Object::key).toList();

	    GSLoggerFactory.getLogger(getClass()).info(
		    "WMS cache delete (prefix={}, page={}, matched={})",
		    prefix, page, keys.size());

	    if (!keys.isEmpty()) {
		GSLoggerFactory.getLogger(getClass()).info("Deleting WMS cache objects with prefix {}: {}", prefix, keys);
		manager.deleteObjects(bucketname, keys);
		deletedTotal += keys.size();
	    }

	    token = response.nextContinuationToken();

	} while (token != null);

	GSLoggerFactory.getLogger(getClass()).info("WMS cache delete completed for prefix {} ({} object(s) deleted)", prefix,
		deletedTotal);
    }

    @Override
    public Integer getSize() {
	Integer ret = 0;
	List<String> views = getViews();
	for (String view : views) {
	    List<String> layers = getLayers(view);
	    for (String layer : layers) {
		Integer size = getSize(view, layer);
		ret += size;
	    }
	}
	return ret;
    }

    @Override
    public Integer getSize(String view, String layer) {
	if (bucketname == null || bucketname.isEmpty()) {
	    return 0;
	}
	int count = manager.countObjects(bucketname, DEFAULT_FOLDER + "/" + view + "/" + layer);
	return count;
    }

    @Override
    public Integer getMaxSize() {
	return maxSize;
    }

    @Override
    public void setMaxSize(Integer size) {
	this.maxSize = size;

    }

    @Override
    public List<String> getViews() {

	return listChildPrefixes(DEFAULT_FOLDER);
    }

    @Override
    public List<String> getLayers(String view) {

	if (view == null || view.isBlank()) {
	    return List.of();
	}

	return listChildPrefixes(DEFAULT_FOLDER + "/" + view);
    }

    /**
     * @param prefix virtual folder prefix without a trailing slash (e.g. {@code dab-wms-cache/his-central-shapes})
     */
    private List<String> listChildPrefixes(String prefix) {

	List<String> children = new ArrayList<>();

	if (bucketname == null || bucketname.isEmpty()) {
	    return children;
	}

	String folderPrefix = prefix.endsWith("/") ? prefix : prefix + "/";
	String token = null;

	do {

	    ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder().//
		    bucket(bucketname).//
		    prefix(folderPrefix).//
		    delimiter("/");

	    if (token != null) {
		requestBuilder.continuationToken(token);
	    }

	    ListObjectsV2Response response = manager.listObjects(requestBuilder.build());

	    if (response == null) {
		break;
	    }

	    for (CommonPrefix commonPrefix : response.commonPrefixes()) {

		String childPrefix = commonPrefix.prefix();
		if (!childPrefix.startsWith(folderPrefix)) {
		    continue;
		}

		String name = childPrefix.substring(folderPrefix.length());

		if (name.endsWith("/")) {
		    name = name.substring(0, name.length() - 1);
		}

		if (!name.isBlank()) {
		    children.add(name);
		}
	    }

	    token = response.nextContinuationToken();

	} while (token != null);

	return children;
    }

}
