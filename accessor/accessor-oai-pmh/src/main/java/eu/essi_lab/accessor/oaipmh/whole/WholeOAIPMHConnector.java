package eu.essi_lab.accessor.oaipmh.whole;

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

import eu.essi_lab.accessor.oaipmh.OAIPMHConnector;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class WholeOAIPMHConnector extends OAIPMHConnector {

    /**
     * 
     */
    public static final String CONNECTOR_TYPE = "WholeOAIPMHConnector";

    /**
     * 
     */
    public WholeOAIPMHConnector() {

	super();
    }

    /**
     * @param setName
     */
    public WholeOAIPMHConnector(String setName) {

	super(setName);
    }

    @Override
    public boolean supportsIncrementalHarvesting() throws GSException {

	return false;
    }

    @Override
    public String getType() {

	return CONNECTOR_TYPE;
    }

    @Override
    protected WholeOAIPMHConnectorSetting initSetting() {

	return new WholeOAIPMHConnectorSetting();
    }
}
