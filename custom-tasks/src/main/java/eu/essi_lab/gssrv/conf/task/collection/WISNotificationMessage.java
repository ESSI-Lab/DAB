package eu.essi_lab.gssrv.conf.task.collection;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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

import eu.essi_lab.iso.datamodel.classes.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.profiler.wis.*;
import org.json.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;

class WISNotificationMessage {

    private JSONObject json = null;

    public WISNotificationMessage(String id, GeographicBoundingBox bbox, String dataId, Link link, String generatedBy) {
	json = new JSONObject();
	JSONArray conformsArray = new JSONArray();
	conformsArray.put("http://wis.wmo.int/spec/wnm/1/conf/core");
	json.put("conformsTo", conformsArray);
	json.put("type", "Feature");
	setID(id);
	setPublicationTime(new Date());
	json.put("generated_by", generatedBy);
	setDataId(dataId);
	setDatetime(null);
	setBBOX(bbox);
	addLink(link);
    }

    /**
     * Sets the <code>datetime</code> property (required, but may be null, e.g. for discovery metadata)
     * 
     * @param date
     */
    public void setDatetime(Date date) {
	setProperty("datetime", date == null ? JSONObject.NULL : ISO8601DateTimeUtils.getISO8601DateTime(date));
    }

    /**
     * Sets the <code>integrity</code> property as the base64 encoded SHA-512 checksum of the given content, that
     * must be identical to the one served at the canonical link
     * 
     * @param content
     */
    public void setIntegrity(String content) {
	try {
	    byte[] digest = MessageDigest.getInstance("SHA-512").digest(content.getBytes(StandardCharsets.UTF_8));
	    JSONObject integrity = new JSONObject();
	    integrity.put("method", "sha512");
	    integrity.put("value", Base64.getEncoder().encodeToString(digest));
	    setProperty("integrity", integrity);
	} catch (NoSuchAlgorithmException e) {
	    // SHA-512 is always available in Java SE
	    throw new IllegalStateException(e);
	}
    }

    public void addLink(Link link) {
	if (!json.has("links")) {
	    json.put("links", new JSONArray());
	}
	JSONArray links = json.getJSONArray("links");
	links.put(link.asJSONObject());
    }

    public void setDataId(String id) {
	setPropertyString("data_id", id);
    }

    public void setPublicationTime(Date date) {
	String value = ISO8601DateTimeUtils.getISO8601DateTime(date);
	setPropertyString("pubtime", value);
    }

    public void setPropertyString(String property, String value) {
	setProperty(property, value);
    }

    private void setProperty(String property, Object value) {
	if (!json.has("properties")) {
	    json.put("properties", new JSONObject());
	}
	JSONObject properties = json.getJSONObject("properties");
	properties.put(property, value);
    }

    public void setID(String id) {
	json.put("id", id);
    }

    /**
     * Sets the (required) geometry from the given bounding box, or null if the bounding box is missing or incomplete
     * 
     * @param bbox
     */
    public void setBBOX(GeographicBoundingBox bbox) {
	if (bbox == null || bbox.getWest() == null || bbox.getEast() == null || bbox.getSouth() == null || bbox.getNorth() == null) {
	    json.put("geometry", JSONObject.NULL);
	    return;
	}
	Double w = bbox.getWest();
	Double e = bbox.getEast();
	Double s = bbox.getSouth();
	Double n = bbox.getNorth();
	WISUtils.addGeometry(json, w, e, s, n);
    }

    public JSONObject getJSONObject() {
	return json;
    }

}
