/**
 * 
 */
package eu.essi_lab.cfga.gs.setting.driver;

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

import java.util.List;

import org.json.JSONObject;

import eu.essi_lab.cfga.EditableSetting;
import eu.essi_lab.cfga.gs.GSTabIndex;
import eu.essi_lab.cfga.gui.extension.ComponentInfo;
import eu.essi_lab.cfga.gui.extension.TabInfo;
import eu.essi_lab.cfga.gui.extension.TabInfoBuilder;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.model.shared.SharedContent.SharedContentCategory;

/**
 * @author Fabrizio
 */
public abstract class DriverSetting extends Setting implements EditableSetting {

    /**
     * 
     */
    public DriverSetting() {

	//
	// set the component extension
	//
	setExtension(new DriverComponentInfo());

	setCanBeDisabled(false);
    }

    /**
     * @author Fabrizio
     */
    public static class DriverComponentInfo extends ComponentInfo {

	/**
	 * 
	 */
	public DriverComponentInfo() {

	    setComponentName(DriverSetting.class.getName());

	    TabInfo tabInfo = TabInfoBuilder.get().//
		    withIndex(GSTabIndex.REPOSITORY.getIndex()).//
		    withShowDirective("Repository").//
		    build();

	    setTabInfo(tabInfo);
	}
    }

    /**
     * @param object
     */
    public DriverSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public DriverSetting(String object) {

	super(object);
    }

    /**
     * @return
     */
    public abstract SharedContentCategory getCategory();

    /**
     * @return
     */
    protected abstract List<SharedContentCategory> availableCategories();

}
