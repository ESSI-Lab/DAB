package eu.essi_lab.services.lock;

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

import java.util.*;

/**
 * @author Fabrizio
 */
public class LocalServiceLock implements ServiceLock {

    private final String value;
    private final String hostName;
    private final String serviceId;

    public static final Set<Map.Entry<String, String>> ACTIVE_SERVICES = new LinkedHashSet<>();

    /**
     *
     * @param serviceId
     * @param hostName
     */
    public LocalServiceLock(String serviceId, String hostName) {

	this.value = hostName + ":" + serviceId;
        this.hostName = hostName;
        this.serviceId = serviceId;
    }

    /**
     * @return
     */
    @Override
    public boolean tryAcquire() {

        ACTIVE_SERVICES.add(Map.entry(hostName, serviceId));

	return true;
    }

    /**
     * @return
     */
    @Override
    public boolean renew() {

	return true;
    }

    /**
     *
     */
    @Override
    public void release() {

        ACTIVE_SERVICES.remove(Map.entry(hostName, serviceId));
    }

    /**
     * @return
     */
    @Override
    public String getValue() {

	return value;
    }
}
