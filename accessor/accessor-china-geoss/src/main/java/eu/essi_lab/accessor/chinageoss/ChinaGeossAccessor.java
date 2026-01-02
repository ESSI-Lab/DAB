package eu.essi_lab.accessor.chinageoss;

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

import eu.essi_lab.adk.harvest.HarvestedAccessor;

/**
 * @author Fabrizio
 */
public class ChinaGeossAccessor extends HarvestedAccessor<ChinaGeossConnector> {

    /**
     * 
     */
    public static final String TYPE = "ChinaGeoss";

    /**
     * 
     */
    protected String initSourceEndpoint() {

	return "http://144.76.78.204:8080";
    }

    @Override
    protected String initSettingName() {

	return "China Geoss Accessor";
    }

    @Override
    protected String initAccessorType() {

	return TYPE;
    }

    @Override
    protected ChinaGeossConnectorSetting initHarvestedConnectorSetting() {

	return new ChinaGeossConnectorSetting();
    }
}
