package eu.essi_lab.harvester.worker;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import eu.essi_lab.adk.AccessorFactory;
import eu.essi_lab.adk.harvest.IHarvestedAccessor;
import eu.essi_lab.augmenter.Augmenter;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.gs.setting.augmenter.AugmenterSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting;

/**
 * @author Fabrizio
 */
public class HarvestingSettingHelper {

    private HarvestingSetting workerSetting;

    /**
     * @param setting
     */
    public HarvestingSettingHelper(HarvestingSetting setting) {

	this.workerSetting = setting;
    }

    @SuppressWarnings({ "rawtypes" })
    public IHarvestedAccessor getSelectedAccessor() throws Exception {

	AccessorSetting setting = workerSetting.getSelectedAccessorSetting();

	IHarvestedAccessor harvestedAccessor = AccessorFactory.getConfiguredHarvestedAccessor(setting);

	return harvestedAccessor;
    }

    /**
     * @return
     */
    @SuppressWarnings("rawtypes")
    public List<Augmenter> getSelectedAugmenters() {

	return workerSetting.getSelectedAugmenterSettings().//
		stream().//
		sorted(Comparator.comparing(AugmenterSetting::getPriority)).//
		map(s -> (Augmenter) s.createConfigurableOrNull()). //
		filter(Objects::nonNull).//
		collect(Collectors.toList());

    }
}
