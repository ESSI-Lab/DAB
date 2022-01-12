package eu.essi_lab.configuration;

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

import eu.essi_lab.model.configuration.IGSConfigurable;
import eu.essi_lab.model.configuration.composite.GSConfiguration;
import eu.essi_lab.model.exceptions.GSException;
public class ComponentTreeFlusher {

    private final GSConfiguration conf;

    public ComponentTreeFlusher(GSConfiguration configuration) {
	conf = configuration;
    }

    public void flushPath(ConfigurableKey key) throws GSException {
	recursiveFlush(key);
    }

    /**
     * This is to emulate the old flush procedure on entire configuration, see {@link GSConfiguration#onFlush}
     * @param key
     * @throws GSException
     */
    private void recursiveFlush(ConfigurableKey key) throws GSException {

	if (key.isOnRoot()) {
	    //TODO

	} else {

	    GSConfigurationManager manager = new GSConfigurationManager(conf);

	    ConfigurableKey k = new ConfigurableKey(key.keyString());

	    k.setOnRoot();

	    IGSConfigurable leafComponent = manager.readComponent(k);

	    key.oneLevelUp(true);

	    recursiveFlush(key);

	    leafComponent.onFlush();
	}
    }

}
