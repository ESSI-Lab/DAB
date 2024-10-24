package eu.essi_lab.cfga.gs.option.mapper;

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

import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.option.OptionValueMapper;

/**
 * @author Fabrizio
 */
public abstract class AccessorSettingMapper extends OptionValueMapper<AccessorSetting> {

    /**
     * 
     */
    public AccessorSettingMapper() {

	super(AccessorSetting.class);
    }

    @Override
    public String asString(AccessorSetting value) {

	return value.getName();
    }

    @Override
    public AccessorSetting fromString(String value) {

	AccessorSetting output = loadSettings().//
		stream().//
		filter(h -> h.getName().equals(value)).//
		findFirst().//
		get();

	return output;
    }

    /**
     * @return
     */
    protected abstract List<AccessorSetting> loadSettings();
}
