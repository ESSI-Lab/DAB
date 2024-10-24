/**
 * 
 */
package eu.essi_lab.augmenter.worker;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.List;
import java.util.Optional;

import org.json.JSONObject;

import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.GetViewIdentifiersRequest;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.augmenter.AugmentersSetting;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.augmenter.worker.AugmenterWorkerSetting;
import eu.essi_lab.cfga.option.ValuesLoader;
import eu.essi_lab.cfga.setting.Setting;

/**
 * @author Fabrizio
 */
public class AugmenterWorkerSettingImpl extends AugmenterWorkerSetting {
    
    public static void main(String[] args) {
	
	System.out.println(new AugmenterWorkerSettingImpl());
    }

    public AugmenterWorkerSettingImpl() {
    }

    /**
     * @param object
     */
    public AugmenterWorkerSettingImpl(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public AugmenterWorkerSettingImpl(String object) {

	super(object);
    }

    @Override
    protected String initConfigurableType() {

	return AugmenterWorker.CONFIGURABLE_TYPE;
    }

    @Override
    protected Setting initAugmentersSetting() {

	return new AugmentersSetting();
    }

    @Override
    protected String getAugmentersSettingIdentifier() {

	return AugmentersSetting.IDENTIFIER;
    }

    /**
     * @author Fabrizio
     */
    public static class ViewIdentifiersLoader extends ValuesLoader<String> {

	@Override
	protected List<String> loadValues(Optional<String> input) throws Exception {

	    DatabaseReader reader = DatabaseProviderFactory.getDatabaseReader(ConfigurationWrapper.getDatabaseURI());

	    return reader.getViewIdentifiers(GetViewIdentifiersRequest.create());
	}
    }

    @Override
    protected ValuesLoader<String> getViewIdentifiersLoader() {

	return new ViewIdentifiersLoader();
    }
}
