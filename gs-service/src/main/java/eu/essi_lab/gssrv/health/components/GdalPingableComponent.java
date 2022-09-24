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
import eu.essi_lab.workflow.processor.grid.GDALConstants;
public class GdalPingableComponent implements IGSHealthCheckPingableComponent {

    @Override
    public void ping() throws Exception {

	GDALConstants.IMPLEMENTATION = GDALConstants.Implementation.RUNTIME;
	boolean healthy = GDALConstants.isGDALAvailable();

	if (!healthy) {

	    throw new Exception("GDAL library not available");
	}
    }

    @Override
    public String getDescription() {
	return "GDAL Lib availabilty";
    }

    @Override
    public Urgency getUrgency() {
	return Urgency.REQUIRED;
    }

    @Override
    public String getId() {
	return "GdalPingableComponent";
    }

    @Override
    public Boolean applicableTo(ExecutionMode mode) {
	return true;
    }

}
