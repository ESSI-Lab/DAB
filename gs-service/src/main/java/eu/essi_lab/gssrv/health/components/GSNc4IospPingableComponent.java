package eu.essi_lab.gssrv.health.components;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import com.indeed.status.core.Urgency;

import eu.essi_lab.configuration.ExecutionMode;
import ucar.nc2.jni.netcdf.Nc4Iosp;
public class GSNc4IospPingableComponent implements IGSHealthCheckPingableComponent {

    @Override
    public void ping() throws Exception {

	boolean healthy = Nc4Iosp.isClibraryPresent();

	if (!healthy) {

	    throw new Exception("netCDF C library not available");
	}
    }

    @Override
    public String getDescription() {
	return "NC 4 IOSP Lib availabilty";
    }

    @Override
    public Urgency getUrgency() {
	return Urgency.REQUIRED;
    }

    @Override
    public String getId() {
	return "GSNc4IospPingableComponent";
    }

    @Override
    public Boolean applicableTo(ExecutionMode mode) {
	return true;
    }
}
