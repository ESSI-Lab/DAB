/**
 * 
 */
package eu.essi_lab.cfga.gs.setting;

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
import java.util.stream.Collectors;

import eu.essi_lab.cfga.ConfigurableLoader;
import eu.essi_lab.cfga.Selector;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;

/**
 * @author Fabrizio
 */
public class ProfilerSettingSelector<P extends ProfilerSetting> extends Selector<P> {

    /**
     * 
     */
    public ProfilerSettingSelector() {

	setName("Select profiler type");
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<P> initSettings() {

	List<? extends Setting> collect = ConfigurableLoader.//
		loadToList().//
		stream().//
		filter(c -> c.getSetting() != null).//
		filter(c -> ProfilerSetting.class.isAssignableFrom(c.getSetting().getSettingClass())).//
		map(c -> SettingUtils.create(c.getSetting().getSettingClass())).//
		collect(Collectors.toList());

	return (List<P>) collect;
    }
}
