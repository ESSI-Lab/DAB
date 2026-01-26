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

import org.json.*;

class Link {
    private String rel = null;
    private String type = null;
    private String href = null;
    private Integer length = null;

    public String getRel() {
	return rel;
    }

    public void setRel(String rel) {
	this.rel = rel;
    }

    public String getType() {
	return type;
    }

    public void setType(String type) {
	this.type = type;
    }

    public String getHref() {
	return href;
    }

    public void setHref(String href) {
	this.href = href;
    }

    public Integer getLength() {
	return length;
    }

    public void setLength(Integer length) {
	this.length = length;
    }

    public Link(String rel, String type, String href) {
	super();
	this.rel = rel;
	this.type = type;
	this.href = href;
    }

    public JSONObject asJSONObject() {
	JSONObject ret = new JSONObject();
	if (rel != null) {
	    ret.put("rel", rel);
	}
	if (type != null) {
	    ret.put("type", type);
	}
	if (href != null) {
	    ret.put("href", href);
	}
	if (length != null) {
	    ret.put("length", length);
	}
	return ret;
    }

}
