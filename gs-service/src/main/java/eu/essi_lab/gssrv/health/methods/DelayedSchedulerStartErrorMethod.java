package eu.essi_lab.gssrv.health.methods;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import eu.essi_lab.configuration.ExecutionMode;
import eu.essi_lab.gssrv.health.GSPingMethod;
import eu.essi_lab.gssrv.starter.DABStarter;

/**
 * @author Fabrizio
 */
public class DelayedSchedulerStartErrorMethod implements GSPingMethod {

    @Override
    public void ping() throws Exception {

	if (DABStarter.schedulerStartError) {

	    throw new Exception("Delayed scheduler start error!");
	}
    }

    @Override
    public String getDescription() {

	return "Verifies that the delayed scheduler start was successful";
    }

    @Override
    public Boolean applicableTo(ExecutionMode mode) {

	return mode == ExecutionMode.BATCH;
    }
}
