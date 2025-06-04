package eu.essi_lab.cdk.query;

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

import eu.essi_lab.cfga.gs.setting.connector.DistributedConnectorSetting;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;

/**
 * @author Fabrizio
 * @param <T>
 */
public abstract class DistributedQueryConnector<T extends DistributedConnectorSetting> implements IDistributedQueryConnector<T> {

    private T setting;
    private String url;

    /**
     * 
     */
    public DistributedQueryConnector() {

	configure(initSetting());
    }

    @Override
    public void setSourceURL(String url) {

	this.url = url;
    }

    @Override
    public String getSourceURL() {

	return this.url;
    }

    /**
     * @return
     */
    protected String getQueryStringURL() {

	return getSourceURL().endsWith("?") ? getSourceURL() : getSourceURL() + "?";
    }

    /**
     * @return
     */
    protected abstract T initSetting();

    @Override
    public void configure(T setting) {

	this.setting = setting;
    }

    @Override
    public T getSetting() {

	return this.setting;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }
}
