package eu.essi_lab.gssrv.conf.task.collection;

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

import eu.essi_lab.iso.datamodel.classes.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.profiler.wis.*;
import org.json.*;

import java.util.Date;

class WISNotificationMessage {

    private JSONObject json = null;

    public WISNotificationMessage(String id, GeographicBoundingBox bbox, String dataId, Link link) {
	json = new JSONObject();
	JSONArray conformsArray = new JSONArray();
	conformsArray.put("http://wis.wmo.int/spec/wnm/1/conf/core");
	json.put("conformsTo", conformsArray);
	json.put("type", "Feature");
	setID(id);
	setPublicationTime(new Date());
	json.put("generated_by", "WHOS DAB");
	setDataId(dataId);
	addLink(link);
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
	if (!json.has("properties")) {
	    json.put("properties", new JSONObject());
	}
	JSONObject properties = json.getJSONObject("properties");
	properties.put(property, value);
    }

    public void setID(String id) {
	json.put("id", id);
    }

    public void setBBOX(GeographicBoundingBox bbox) {
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
