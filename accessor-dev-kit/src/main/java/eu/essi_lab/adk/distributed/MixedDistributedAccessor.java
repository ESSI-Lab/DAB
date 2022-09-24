package eu.essi_lab.adk.distributed;

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

import eu.essi_lab.cdk.query.IDistributedQueryConnector;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.gs.setting.connector.DistributedConnectorSetting;

/**
 * @author Fabrizio
 */
@SuppressWarnings("rawtypes")
public abstract class MixedDistributedAccessor<C extends IDistributedQueryConnector> extends DistributedAccessor<C> {

    /**
     * 
     */
    public MixedDistributedAccessor() {

    }

    /**
     * 
     */
    protected void configure() {

	AccessorSetting accessorSetting = initMixedSetting();

	configure(accessorSetting);
    }

    /**
     * @return
     */
    protected abstract AccessorSetting initMixedSetting();

    @Override
    protected String initSourceLabel() {

	throw new UnsupportedOperationException("Source label of mixed accessors must be set inside the initMixedSetting method");
    }

    @Override
    protected String initSourceEndpoint() {

	throw new UnsupportedOperationException("Source endpoint of mixed accessors must be set inside the initMixedSetting method");
    }

    @Override
    protected DistributedConnectorSetting initDistributedConnectorSetting() {

	throw new UnsupportedOperationException(
		"Distributed connector setting of mixed accessors must be set inside the initMixedSetting method");
    }

    @Override
    protected String initSettingName() {

	return null;
    }

    @Override
    public String getType() {

	return initAccessorType();
    }
}
