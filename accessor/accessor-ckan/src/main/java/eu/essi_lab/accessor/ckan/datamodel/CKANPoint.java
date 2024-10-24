package eu.essi_lab.accessor.ckan.datamodel;

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

public class CKANPoint {
    private String crs;
    private Double lat;
    private Double lon;

    public CKANPoint(String crs, Double lat, Double lon) {
	super();
	this.crs = crs;
	this.lat = lat;
	this.lon = lon;
    }

    public String getCrs() {
	return crs;
    }

    public void setCrs(String crs) {
	this.crs = crs;
    }

    public Double getLat() {
	return lat;
    }

    public void setLat(Double lat) {
	this.lat = lat;
    }

    public Double getLon() {
	return lon;
    }

    public void setLon(Double lon) {
	this.lon = lon;
    }

}
