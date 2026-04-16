package eu.essi_lab.cfga.gui.directive;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.cfga.setting.*;
import eu.essi_lab.lib.utils.*;

/**
 * @author Fabrizio
 */
public class AddDirective extends Directive {

    private String settingClassName;
    private boolean tabView;

    /**
     *
     */
    public AddDirective() {

	setName("ADD");

	setConfirmationPolicy(ConfirmationPolicy.ON_WARNINGS);
    }

    /**
     * @param name
     * @param settingClass
     */
    public AddDirective(String name, Class<? extends Setting> settingClass) {

	this(name, null, settingClass.getName(), false);
    }

    /**
     * @param name
     * @param settingClass
     * @param tabView
     */
    public AddDirective(String name, Class<? extends Setting> settingClass, boolean tabView) {

	this(name, null, settingClass.getName(), tabView);
    }

    /**
     * @param name
     * @param settingClass
     */
    public AddDirective(String name, String settingClassName) {

	this(name, null, settingClassName, false);

    }

    /**
     * @param name
     * @param settingClassName
     * @param tabView
     */
    public AddDirective(String name, String settingClassName, boolean tabView) {

	this(name, null, settingClassName, tabView);

    }

    /**
     * @param name
     * @param description
     * @param settingClass
     */
    public AddDirective(String name, String description, Class<? extends Setting> settingClass) {

	this(name, description, settingClass.getName(), false);
    }

    /**
     * @param name
     * @param description
     * @param settingClass
     * @param tabView
     */
    public AddDirective(String name, String description, Class<? extends Setting> settingClass, boolean tabView) {

	this(name, description, settingClass.getName(), tabView);
    }

    /**
     * @param name
     * @param description
     * @param settingClassName
     */
    public AddDirective(String name, String description, String settingClassName) {

	this(name, description, settingClassName, false);
    }

    /**
     * @param name
     * @param description
     * @param settingClassName
     * @param tabView
     */
    public AddDirective(String name, String description, String settingClassName, boolean tabView) {

	super(name);
	setSettingClass(settingClassName);

	if (description != null) {
	    setDescription(description);
	}

	setConfirmationPolicy(ConfirmationPolicy.ON_WARNINGS);
	setTabView(tabView);
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

    /**
     * @return
     */
    public boolean isTabView() {

	return tabView;
    }

    /**
     * @param tabView
     */
    public void setTabView(boolean tabView) {

	this.tabView = tabView;
    }
}
