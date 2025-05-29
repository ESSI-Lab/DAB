package eu.essi_lab.accessor.fdsn;

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

import eu.essi_lab.adk.distributed.DistributedAccessor;
import eu.essi_lab.cfga.gs.setting.connector.DistributedConnectorSetting;

/**
 * @author Fabrizio
 */
public class FDSNAccessor extends DistributedAccessor<FDSNConnector> {

    /**
     * 
     */
    public static final String NAME = "USGS Earthquake Events Accessor";

    /**
     * 
     */
    public static final String TYPE = "FDSN";

    /**
     * 
     */
    public FDSNAccessor() {

	super();
    }

    /**
     * 
     */
    protected String initSourceLabel() {

	return "USGS Earthquake Events";
    }

    /**
     * 
     */
    protected String initSourceEndpoint() {

	return "https://earthquake.usgs.gov/fdsnws/event/1/";
    }

    @Override
    protected String initSettingName() {

	return NAME;
    }

    @Override
    protected String initAccessorType() {

	return TYPE;
    }

    @Override
    protected DistributedConnectorSetting initDistributedConnectorSetting() {

	return new FDSNConnectorSetting();
    }
}
