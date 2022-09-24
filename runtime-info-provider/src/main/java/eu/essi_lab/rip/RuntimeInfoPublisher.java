/**
 * 
 */
package eu.essi_lab.rip;

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

import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public abstract class RuntimeInfoPublisher {

    private String runtimeId;
    private String context;

    /**
     * @param runtimeId
     * @param context
     */
    public RuntimeInfoPublisher(String runtimeId, String context) {

	setRuntimeId(runtimeId);
	setContext(context);
    }

    /**
     * @return
     */
    public String getRuntimeId() {
	return runtimeId;
    }

    /**
     * @param runtimeId
     */
    public void setRuntimeId(String runtimeId) {
	this.runtimeId = runtimeId;
    }

    /**
     * @return
     */
    public String getContext() {
	return context;
    }

    /**
     * @param context
     */
    public void setContext(String context) {
	this.context = context;
    }

    /**
     * @param provider
     * @return
     */
    public abstract void publish(RuntimeInfoProvider provider) throws GSException;
}
