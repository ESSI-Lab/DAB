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

import eu.essi_lab.cfga.setting.Setting;

/**
 * @author Fabrizio
 */
public class RemoveDirective extends AddDirective {

    private boolean allowFullRemoval;

    /**
     * 
     */
    public RemoveDirective() {

	setName("REMOVE");

	setConfirmationPolicy(ConfirmationPolicy.ALWAYS);
    }

    /**
     * @param name
     * @param allowFullRemoval
     * @param settingClass
     */
    public RemoveDirective(String name, boolean allowFullRemoval, Class<? extends Setting> settingClass) {

	super(name, settingClass);

	setAllowFullRemoval(allowFullRemoval);

	setConfirmationPolicy(ConfirmationPolicy.ALWAYS);
    }

    /**
     * @param name
     * @param allowFullRemoval
     * @param settingClass
     */
    public RemoveDirective(String name, boolean allowFullRemoval, String settingClass) {

	super(name, settingClass);

	setAllowFullRemoval(allowFullRemoval);

	setConfirmationPolicy(ConfirmationPolicy.ALWAYS);
    }

    /**
     * @return
     */
    public boolean isFullRemovalAllowed() {

	return allowFullRemoval;
    }

    /**
     * @param
     */
    public void setAllowFullRemoval(boolean allowFullRemoval) {

	this.allowFullRemoval = allowFullRemoval;
    }

}
