package eu.essi_lab.netcdf.timeseries;

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

import java.io.IOException;

import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFileWriter;

public class TimeSeriesWriter {

    protected NetcdfFileWriter writer;

    public TimeSeriesWriter(String location) throws IOException {
	this.writer = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf4, location, null);

	// global attributes
	writer.addGroupAttribute(null, new Attribute("featureType", "timeSeries"));
	writer.addGroupAttribute(null, new Attribute("Conventions", "CF-1.7"));

    }

    public void addGlobalAttribute(String name, String value) {
	writer.addGroupAttribute(null, new Attribute(name, value));

    }

}
