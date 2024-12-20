package eu.essi_lab.accessor.mgnify;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.math.BigDecimal;

import org.json.JSONObject;

public class Sample extends MGnifyObject {

    private JSONObject attributes;
    private JSONObject relationships;
    public Sample(JSONObject da) {
	super(da);
	this.attributes = da.getJSONObject("attributes");

    }

    public String getId() {
	return json.getString("id");
    }

    public BigDecimal getLongitude() {
	return attributes.optBigDecimal("longitude",null);
    }

    public BigDecimal getLatitude() {
	return attributes.optBigDecimal("latitude",null);
    }
    
    public String getSampleName() {
	return attributes.getString("sample-name");
    }
    
    public String getCollectionDate() {
	if (attributes.isNull("collection-date")) {
	    return null;
	}
	return attributes.getString("collection-date");
    }
    
    public String getEnvironmentBiome() {
	if (attributes.isNull("environment-biome")) {
	    return null;
	}
	return attributes.getString("environment-biome");
    }
    
    public String getEnvironmentFeature() {
	if (attributes.isNull("environment-feature")) {
	    return null;
	}
	return attributes.getString("environment-feature");
    }
    
    public String getEnvironmentMaterial() {
	if (attributes.isNull("environment-material")) {
	    return null;
	}
	return attributes.getString("environment-material");
    }

}
