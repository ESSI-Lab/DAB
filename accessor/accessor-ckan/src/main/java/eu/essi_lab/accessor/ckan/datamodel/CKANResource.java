package eu.essi_lab.accessor.ckan.datamodel;

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

public class CKANResource {
    private String url;
    private String name;
    private String description;
    private String type;
    private String resourceType;
    private String format;
    private String mimetype;
    private String mimetypeInner;
    private String size;
    private String lastModified;
    private String hash;
    private String cacheUrl;
    private String cacheLastUpdated;
    private String webstoreUrl;
    private String webstoreLastUpdated;

    // additional fields

    private String resourceGroupId;
    private String revisionTimeStamp;
    private String id;
    private String state;
    private String urlType;
    private String created;
    private String cachedURL;

    public String getResourceGroupId() {
	return resourceGroupId;
    }

    public void setResourceGroupId(String resourceGroupId) {
	this.resourceGroupId = resourceGroupId;
    }

    public String getRevisionTimeStamp() {
	return revisionTimeStamp;
    }

    public void setRevisionTimeStamp(String revisionTimeStamp) {
	this.revisionTimeStamp = revisionTimeStamp;
    }

    public String getId() {
	return id;
    }

    public void setId(String id) {
	this.id = id;
    }

    public String getState() {
	return state;
    }

    public void setState(String state) {
	this.state = state;
    }

    public String getUrlType() {
	return urlType;
    }

    public void setUrlType(String urlType) {
	this.urlType = urlType;
    }

    public String getCreated() {
	return created;
    }

    public void setCreated(String created) {
	this.created = created;
    }

    public String getResourceType() {
	return resourceType;
    }

    public void setResourceType(String resourceType) {
	this.resourceType = resourceType;
    }

    public String getUrl() {
	return url;
    }

    public void setUrl(String url) {
	this.url = url;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getDescription() {
	return description;
    }

    public void setDescription(String description) {
	this.description = description;
    }

    public String getType() {
	return type;
    }

    public void setType(String type) {
	this.type = type;
    }

    public String getFormat() {
	return format;
    }

    public void setFormat(String format) {
	this.format = format;
    }

    public String getMimetype() {
	return mimetype;
    }

    public void setMimetype(String mimetype) {
	this.mimetype = mimetype;
    }

    public String getMimetypeInner() {
	return mimetypeInner;
    }

    public void setMimetypeInner(String mimetypeInner) {
	this.mimetypeInner = mimetypeInner;
    }

    public String getSize() {

	if (size != null && !"".equalsIgnoreCase(size)) {

	    try {
		double val = (Long.valueOf(size) / Double.valueOf("1000000"));
		return Double.toHexString(val);

	    } catch (Throwable thr) {
	    }

	}
	return size;
    }

    public void setSize(String size) {
	this.size = size;
    }

    public String getLastModified() {
	return lastModified;
    }

    public void setLastModified(String lastModified) {
	this.lastModified = lastModified;
    }

    public String getHash() {
	return hash;
    }

    public void setHash(String hash) {
	this.hash = hash;
    }

    public String getCacheUrl() {
	return cacheUrl;
    }

    public void setCacheUrl(String cacheUrl) {
	this.cacheUrl = cacheUrl;
    }

    public String getCacheLastUpdated() {
	return cacheLastUpdated;
    }

    public void setCacheLastUpdated(String cacheLastUpdated) {
	this.cacheLastUpdated = cacheLastUpdated;
    }

    public String getWebstoreUrl() {
	return webstoreUrl;
    }

    public void setWebstoreUrl(String webstoreUrl) {
	this.webstoreUrl = webstoreUrl;
    }

    public String getWebstoreLastUpdated() {
	return webstoreLastUpdated;
    }

    public void setWebstoreLastUpdated(String webstoreLastUpdated) {
	this.webstoreLastUpdated = webstoreLastUpdated;
    }

    public void setCachedURL(String url) {

	this.cachedURL = url;

    }

    public String getCachedURL() {

	return this.cachedURL;

    }

}
