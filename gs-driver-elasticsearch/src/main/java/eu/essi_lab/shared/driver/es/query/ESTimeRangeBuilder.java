package eu.essi_lab.shared.driver.es.query;

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

import org.json.JSONObject;

/**
 * @author ilsanto
 */
public class ESTimeRangeBuilder {

    private Long from;
    private Long to;
    private final String dateAtt;
    private static final String LTE_KEY = "lte";
    private static final String GTE_KEY = "gte";

    public ESTimeRangeBuilder(String dateAttribute) {
	this.dateAtt = dateAttribute;
    }

    public ESTimeRangeBuilder withFrom(Long f) {

	this.from = f;

	return this;
    }

    public ESTimeRangeBuilder withTo(Long t) {

	this.to = t;

	return this;
    }

    public JSONObject build() {

	JSONObject json = new JSONObject();

	json.put(dateAtt, new JSONObject());

	if (this.to != null) {

	    json.getJSONObject(dateAtt).put(LTE_KEY, this.to);

	}

	if (this.from != null) {

	    json.getJSONObject(dateAtt).put(GTE_KEY, this.from);

	}

	return json;
    }

}
