package eu.essi_lab.cfga.option;

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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author Fabrizio
 */
public abstract class ValuesLoader<T> {

    /**
     * 
     */
    private static final ExecutorService SERVICE = Executors.newSingleThreadExecutor();

    /**
     * @author Fabrizio
     * @param <T>
     */
    @FunctionalInterface
    public interface ValuesLoadingLister<T> {

	/**
	 * @param loadedValues
	 * @param exception
	 */
	void valuesLoaded(List<T> loadedValues, Optional<Exception> exception);
    }

    /**
     * @param listener
     * @param input
     */
    public final void load(ValuesLoadingLister<T> listener, Optional<String> input) {

	SERVICE.execute(() -> {

	    try {

		GSLoggerFactory.getLogger(ValuesLoader.class).info("Loading values STARTED");

		List<T> loadedValues = loadValues(input);

		listener.valuesLoaded(loadedValues, Optional.empty());

		GSLoggerFactory.getLogger(ValuesLoader.class).info("Loading values ENDED");

	    } catch (Exception e) {

		listener.valuesLoaded(Arrays.asList(), Optional.of(e));

		GSLoggerFactory.getLogger(ValuesLoader.class).error(e.getMessage(), e);
	    }
	});
    }

    /**
     * This method must return {@link Optional#empty()} if {@link #requestInput()} returns <code>false</code>,
     * otherwise it should return a short, descriptive text of the requested input.<br>
     * For example in order to load
     * the available sets of a OAI-PMH service, the text could be "Please provide the service endpoint"
     */
    public String getRequestInputText() {

	return null;
    }

    /**
     * Returns <code>true</code> if an input is required in order to load the option values. For example, in order to
     * load the available sets of a OAI-PMH service, the endpoint of the service is required and it must be provided by
     * the user
     */
    public boolean requestInput() {

	return false;
    }

    /**
     * The only method to implement. Asynchronously performs the action needed to load the option values. When the work
     * is done, the {@link ValuesLoadingLister} is notified with the resulting list of loaded values, or with an
     * an empty list and the thrown exception if errors occurred
     * 
     * @see ValuesLoadingLister#valuesLoaded(List, Optional)
     * @see #requestInput()
     * @see #getRequestInputText()
     * @param input the optional input provided by the user
     * @return the loaded values
     * @throws Exception
     */
    protected abstract List<T> loadValues(Optional<String> input) throws Exception;
}
