package eu.essi_lab.harvester.worker;

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

import org.json.JSONObject;

import eu.essi_lab.augmenter.AugmentersSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting;
import eu.essi_lab.cfga.setting.Setting;

/**
 * @author Fabrizio
 */
public class HarvestingSettingImpl extends HarvestingSetting {

    public HarvestingSettingImpl() {

    }

    /**
     * @param object
     */
    public HarvestingSettingImpl(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public HarvestingSettingImpl(String object) {

	super(object);
    }

    @Override
    protected String initConfigurableType() {

	return HarvesterWorker.HARVESTER_WORKER_TYPE;
    }

    @Override
    protected Setting initAccessorsSetting() {

	return new HarvestedAccessorsSetting();
    }

    @Override
    protected Setting initAugmentersSetting() {

	return new AugmentersSetting();
    }

    @Override
    protected String getAccessorsSettingIdentifier() {

	return HarvestedAccessorsSetting.IDENTIFIER;
    }

    @Override
    protected String getAugmentersSettingIdentifier() {

	return AugmentersSetting.IDENTIFIER;
    }
}
