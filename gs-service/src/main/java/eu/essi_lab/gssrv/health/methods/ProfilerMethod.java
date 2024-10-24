package eu.essi_lab.gssrv.health.methods;

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

import java.util.Optional;
import java.util.Properties;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.configuration.ExecutionMode;
import eu.essi_lab.gssrv.health.GSPingMethod;
import eu.essi_lab.gssrv.servlet.RateLimiterFilter;

/**
 * @author Fabrizio
 */
public class ProfilerMethod implements GSPingMethod {

    @Override
    public void ping() throws Exception {

	boolean ping = true;

	Optional<Properties> keyValueOption = ConfigurationWrapper.getSystemSettings().getKeyValueOptions();
	if (keyValueOption.isPresent()) {

	    ping = Boolean.valueOf(keyValueOption.get().getOrDefault("profilerHealthCheckMethodEnabled", "true").toString());
	}

	if (RateLimiterFilter.everythingIsBlocked() && ping) {

	    throw new Exception("Profilers are blocked!");
	}
    }

    @Override
    public String getDescription() {

	return "Verifies that the profilers are able to accept new incoming requests";
    }

    @Override
    public Boolean applicableTo(ExecutionMode mode) {

	switch (mode) {
	case CONFIGURATION:
	case LOCAL_PRODUCTION:
	case BATCH:
	    return false;
	case MIXED:
	case FRONTEND:
	case ACCESS:
	case INTENSIVE:
	default:
	    return true;
	}
    }
}
