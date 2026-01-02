package eu.essi_lab.adk.harvest;

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

import eu.essi_lab.cdk.harvest.IHarvestedQueryConnector;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;

/**
 * @author Fabrizio
 * @param <C>
 */
@SuppressWarnings("rawtypes")
public abstract class MixedHarvestedAccessor<C extends IHarvestedQueryConnector> extends HarvestedAccessor<C> {

    /**
     * 
     */
    public MixedHarvestedAccessor() {

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
    protected HarvestedConnectorSetting initHarvestedConnectorSetting() {

	throw new UnsupportedOperationException(
		"Harvested connector setting of mixed accessors must be set inside the initMixedSetting method");
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
