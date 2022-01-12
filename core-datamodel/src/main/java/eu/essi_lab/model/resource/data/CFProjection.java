package eu.essi_lab.model.resource.data;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

public enum CFProjection {
    ALBERS_CONICAL_EQUAL_AREA("albers_conical_equal_area"), //
    AZIMUTHAL_EQUIDISTANT("azimuthal_equidistant"), //
    GEOSTATIONARY("geostationary"), //
    LAMBERT_AZIMUTHAL_EQUAL_AREA("lambert_azimuthal_equal_area"), //
    LAMBERT_CONFORMAL_CONIC("lambert_conformal_conic"), //
    LAMBERT_CYLINDRICAL_EQUAL_AREA("lambert_cylindrical_equal_area"), //
    LATITUDE_LONGITUDE("latitude_longitude"), //
    MERCATOR("mercator"), //
    ORTHOGRAPHIC("orthographic"), //
    POLAR_STEREOGRAPHIC("polar_stereographic"), //
    ROTATED_LATITUDE_LONGITUDE("rotated_latitude_longitude"), //
    STEREOGRAPHIC("stereographic"), //
    SINUSOIDAL("sinusoidal"), // //
    TRANSVERSE_MERCATOR("transverse_mercator"), //
    VERTICAL_PERSPECTIVE("vertical_perspective");

    private String gridMappingName;

    public String getGridMappingName() {
	return gridMappingName;
    }

    CFProjection(String gridMappingName) {
	this.gridMappingName = gridMappingName;
    }

}
