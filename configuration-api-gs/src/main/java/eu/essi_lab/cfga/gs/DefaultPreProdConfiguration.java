package eu.essi_lab.cfga.gs;

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

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import eu.essi_lab.cfga.ConfigurationSource;

/**
 * @author Fabrizio
 */
public class DefaultPreProdConfiguration extends DefaultProdConfiguration {

    /**
     * @throws IOException
     * @throws Exception
     */
    public DefaultPreProdConfiguration() throws IOException, Exception {

	super();
    }
    
    /**
     * @param unit
     * @param interval
     * @throws IOException
     * @throws Exception
     */
    public DefaultPreProdConfiguration(ConfigurationSource source) throws IOException, Exception {

	super(source);
    }

    /**
     * @param unit
     * @param interval
     * @throws IOException
     * @throws Exception
     */
    public DefaultPreProdConfiguration(ConfigurationSource source, TimeUnit unit, int interval) throws IOException, Exception {

	super(source, unit, interval);
    }

    /**
     * @return
     */
    protected String getConfigName() {

	return "default-preprod-config.json";
    }
}
