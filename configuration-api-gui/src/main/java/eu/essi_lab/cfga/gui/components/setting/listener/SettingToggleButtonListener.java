package eu.essi_lab.cfga.gui.components.setting.listener;

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

import com.vaadin.flow.component.HasValue.ValueChangeEvent;

import eu.essi_lab.cfga.gui.components.listener.AbstractValueChangeListener;
import eu.essi_lab.cfga.gui.components.setting.SettingComponent;

@SuppressWarnings("serial")
public class SettingToggleButtonListener extends AbstractValueChangeListener {

    private SettingComponent settingComponent;

    /**
     * @param settingComponent
     */
    public SettingToggleButtonListener(SettingComponent settingComponent) {

	this.settingComponent = settingComponent;
    }

    /**
     * @param event
     */
    @Override
    protected void handleEvent(ValueChangeEvent<?> event) {

	settingComponent.onToggleStateChanged(event);
    }
}
