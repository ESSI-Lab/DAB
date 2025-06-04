package eu.essi_lab.cfga.gui.extension.directive;

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

import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author Fabrizio
 */
public class AddDirective extends Directive {

    private String settingClassName;

    /**
     * 
     */
    public AddDirective() {

	setName("Add");

	setConfirmationPolicy(ConfirmationPolicy.ON_WARNINGS);
    }

    /**
     * @param name
     * @param settingClass
     */
    public AddDirective(String name, Class<? extends Setting> settingClass) {

	super(name);
	this.settingClassName = settingClass.getName();

	setConfirmationPolicy(ConfirmationPolicy.ON_WARNINGS);
    }

    /**
     * @param name
     * @param settingClass
     */
    public AddDirective(String name, String settingClassName) {

	super(name);
	this.settingClassName = settingClassName;

	setConfirmationPolicy(ConfirmationPolicy.ON_WARNINGS);
    }

    /**
     * @return the settingClass
     */
    public Class<? extends Setting> getSettingClass() {

	return createSettingClass();
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Setting> createSettingClass() {

	try {
	    return (Class<? extends Setting>) Class.forName(getSettingClassName());
	} catch (ClassNotFoundException e) {
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return null;
    }

    /**
     * @return the settingClass
     */
    public String getSettingClassName() {

	return settingClassName;
    }

    /**
     * @param settingClass the settingClass to set
     */
    public void setSettingClass(String settingClassName) {

	this.settingClassName = settingClassName;
    }

    /**
     * @param settingClass the settingClass to set
     */
    public void setSettingClass(Class<? extends Setting> settingClass) {

	this.settingClassName = settingClass.getName();
    }
}
