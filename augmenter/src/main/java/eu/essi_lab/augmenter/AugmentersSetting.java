package eu.essi_lab.augmenter;

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

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.lib.utils.StreamUtils;

/**
 * @author Fabrizio
 */
public class AugmentersSetting extends Setting {

    /**
     * 
     */
    public final static String IDENTIFIER = "augmentersSetting";

    @SuppressWarnings("rawtypes")
    public AugmentersSetting() {

	setCanBeDisabled(false);
	setEditable(false);
	enableCompactMode(false);

	setName("Augmenters");

	setIdentifier(IDENTIFIER);
	setSelectionMode(SelectionMode.MULTI);

	List<Augmenter> list = StreamUtils.iteratorToStream(ServiceLoader.load(Augmenter.class).iterator()).//
		collect(Collectors.toList());

	list.forEach(a -> {

	    Setting setting = a.getSetting().clone();
	    setting.setIdentifier(setting.getConfigurableType());
	    addSetting(setting);
	});
    }
}
