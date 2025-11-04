/**
 * 
 */
package eu.essi_lab.cfga.gs.setting.ontology;

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

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import com.vaadin.flow.component.grid.contextmenu.GridContextMenu.GridContextMenuItemClickEvent;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.gs.setting.ontology.OntologySetting.Availability;
import eu.essi_lab.cfga.gui.components.TabContainer;
import eu.essi_lab.cfga.gui.components.grid.GridMenuItemHandler;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;

/**
 * @author Fabrizio
 */
public class OntologyEnableItemHandler extends GridMenuItemHandler {

    /**
     * 
     */
    public OntologyEnableItemHandler() {
    }

    /**
     * @param withTopDivider
     * @param withBottomDivider
     */
    public OntologyEnableItemHandler(boolean withTopDivider, boolean withBottomDivider) {

	super(withTopDivider, withBottomDivider);
    }

    @Override
    public void onClick(//
	    GridContextMenuItemClickEvent<HashMap<String, String>> event, //
	    TabContainer tabContainer, //
	    Configuration configuration, //
	    Optional<Setting> setting, //
	    HashMap<String, Boolean> selection) {

	List<OntologySetting> settings = configuration.//
		list(OntologySetting.class, false).//
		stream().//
		filter(s -> selection.containsKey(s.getIdentifier()) && selection.get(s.getIdentifier())).//

		map(s -> SelectionUtils.resetAndSelect(s, false)).//

		map(s -> (OntologySetting) SettingUtils.downCast(s, s.getSettingClass())).//

		peek(s -> {

		    s.setOntologyAvailability(enabled() ? Availability.ENABLED : Availability.DISABLED);
		    SelectionUtils.deepClean(s);

		}).//

		toList();

	settings.forEach(configuration::replace);

	tabContainer.render(true);
    }

    /**
     * @return
     */
    protected boolean enabled() {

	return true;
    }

    @Override
    public String getItemText() {

	return "Enable selected ontologies";
    }

    @Override
    public boolean isEnabled(//
	    HashMap<String, String> eventItem, //
	    TabContainer tabContainer, //
	    Configuration configuration, //
	    Setting setting, //
	    HashMap<String, Boolean> selection) {

	return selection.values().stream().anyMatch(v -> v == true);
    }
}
