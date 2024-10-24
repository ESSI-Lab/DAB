package eu.essi_lab.accessor.sentinel;

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

public enum SatelliteLayers {
    // reading capabilities from:
    // https://services.sentinel-hub.com/ogc/wms/token?SERVICE=WMS&VERSION=1.1.1&REQUEST=GetCapabilities
    NATURAL_COLOR("NATURAL-COLOR"), FALSE_COLOR("FALSE-COLOR"), TRUE_COLOR("TRUE-COLOR-S2L2A"), TRUE_COLOR_S2L2A("TRUE-COLOR-S2L2A"),
    //
    FALSE_COLOR_URBAN("FALSE-COLOR-URBAN"), AGRICULTURE("AGRICULTURE"),
    //
    MOISTURE_INDEX("MOISTURE-INDEX"),
    //
    GEOLOGY("GEOLOGY"), BATHYMETRIC("BATHYMETRIC"), 
    //VEGETATION_INDEX("VEGETATION_INDEX"), NDWI("NDWI"),
    //
    //ATMOSPHERIC_PENETRATION("90_ATMOSPHERIC_PENETRATION"), VEGETATION_INDEX("5_VEGETATION_INDEX"),
    //
    SWIR("SWIR"), NDVI("NDVI");

    private String layer;

    private SatelliteLayers(String l) {
	this.layer = l;
    }

    public String getWMSLayer() {
	return layer;
    }

}
