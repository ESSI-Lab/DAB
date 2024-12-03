/**
 * 
 */
package eu.essi_lab.cfga;

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

import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;

/**
 * Specialized kind of {@link Setting} used to select other settings
 * 
 * @author Fabrizio
 */
public abstract class Selector<S extends Setting> extends Setting {

    /**
     * 
     */
    public Selector() {

	setName("Selector");

	setSelectionMode(SelectionMode.SINGLE);

	initSettings().forEach(s -> addSetting(s));

	getSettings().//
		stream().//
		sorted((s1, s2) -> s1.getName().compareTo(s2.getName())).//
		findFirst().//
		get().//
		setSelected(true);
    }

    /**
     * Initializes the settings to select
     * 
     * @return
     */
    protected abstract List<S> initSettings();

    /**
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<S> getSelectedSettings() {

	return (List<S>) getSettings().//
		stream().filter(s -> s.isSelected()).//
		map(s -> SettingUtils.downCast(s, s.getSettingClass())).//
		collect(Collectors.toList());
    }
}
