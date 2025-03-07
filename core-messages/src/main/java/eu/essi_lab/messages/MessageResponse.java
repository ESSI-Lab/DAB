package eu.essi_lab.messages;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.messages.count.AbstractCountResponse;
import eu.essi_lab.model.GSPropertyHandler;
import eu.essi_lab.model.RuntimeInfoElement;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.rip.RuntimeInfoProvider;

/**
 * Provides information about the response of a {@link GSMessage}
 * 
 * @author Fabrizio
 */
public abstract class MessageResponse<T, C extends AbstractCountResponse> implements RuntimeInfoProvider {

    private List<T> results;
    private C countResponse;
    private GSException exception;
    private String profilerName;
    private GSPropertyHandler propertyHandler;

    /**
     * 
     */
    public MessageResponse() {
	setException(GSException.createException());
	setResultsList(new ArrayList<T>());
	propertyHandler = new GSPropertyHandler();
    }

    /**
     * Creates a new {@link MessageResponse} which is a clone of the supplied <code>resultSet</code>
     * but without {@link #getResultsList()}
     * 
     * @param response a non <code>null</code> {@link MessageResponse} to clone
     */
    public MessageResponse(MessageResponse<?, C> response) {
	setException(response.getException());
	setCountResponse(response.getCountResponse());
	propertyHandler = new GSPropertyHandler();
    }

    /**
     * This default implementation returns a map with common info
     */
    @Override
    public HashMap<String, List<String>> provideInfo() {

	HashMap<String, List<String>> map = new HashMap<>();

	getProfilerName().ifPresent(name -> map.put(RuntimeInfoElement.PROFILER_NAME.getName(), Arrays.asList(name)));

	return map;
    }

    /**
     * @return
     */
    public Optional<String> getProfilerName() {

	return Optional.ofNullable(profilerName);
    }

    /**
     * @param name
     */
    public void setProfilerName(String name) {

	this.profilerName = name;
    }

    /**
     * @return
     */
    public C getCountResponse() {

	return countResponse;
    }

    /**
     * @param countResponse
     */
    public void setCountResponse(C countResponse) {

	this.countResponse = countResponse;
    }

    /**
     * @return
     */
    public List<T> getResultsList() {

	return results;
    }

    /**
     * @param results
     */
    public void setResultsList(List<T> results) {

	this.results = results;
    }

    /**
     * @return
     */
    public GSException getException() {

	return exception;
    }

    /**
     * @param handler
     */
    public void setPropertyHandler(GSPropertyHandler handler) {

	this.propertyHandler = handler;
    }

    /**
     * @return the propertyHandler
     */
    public GSPropertyHandler getPropertyHandler() {

	return propertyHandler;
    }

    /**
     * @param exception
     */
    protected void setException(GSException exception) {

	this.exception = exception;
    }

}
