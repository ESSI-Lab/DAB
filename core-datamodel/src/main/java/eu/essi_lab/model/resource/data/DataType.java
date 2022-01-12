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

public enum DataType {
    GRID, // regular grid along the provided dimensions, including spatial dimensions
    POINT, // data at scattered locations and times with no implied relationship among of coordinate positions
    TIME_SERIES, // data may be taken over periods of time at a set of discrete point, spatial locations called stations
    TRAJECTORY, // data may be taken along discrete paths through space, each path constituting a connected set of
		// points called a trajectory
    PROFILE, // a series of connected observations along a vertical line, like an atmospheric or ocean sounding
    TIME_SERIES_PROFILE, // when profiles are taken repeatedly at a station, one gets a time series of profiles
    TRAJECTORY_PROFILE, // when profiles are taken repeatedly at a station, one gets a time series of profiles
    GML_FEATURE // other general and complex features that can be encoded by GML (e.g. including circles, polygons etc.)
}
