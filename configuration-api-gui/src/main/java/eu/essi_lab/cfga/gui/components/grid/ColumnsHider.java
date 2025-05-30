package eu.essi_lab.cfga.gui.components.grid;

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

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import eu.essi_lab.cfga.gui.components.listener.CheckBoxClickListener;

/**
 * @author Fabrizio
 */
@SuppressWarnings("serial")
public class ColumnsHider extends HorizontalLayout {

    /**
     * 
     */
    private static final HashMap<String, Boolean> VALUES_MAP = new HashMap<>();

    /**
     * @param grid
     * @param gridInfo
     */
    public ColumnsHider(GridComponent grid, GridInfo gridInfo) {

	setMargin(false);
	setSpacing(false);
	setHeight("30px");

	gridInfo.getColumnsDescriptors().forEach(descriptor -> {

	    if (grid.getColumns().//
		    stream().//
		    filter(c -> !c.getKey().isEmpty()).//
		    filter(c -> c.getKey().equals(descriptor.getColumnName()) && c.isVisible()).//
		    findFirst().//
		    isPresent()) {

		Checkbox checkBox = new Checkbox(descriptor.getColumnName());
		checkBox.getStyle().set("font-size", "12px");

		add(checkBox);

		checkBox.addClickListener(new CheckBoxClickListener() {

		    @Override
		    public void handleEvent(ClickEvent<Checkbox> event) {

			String label = event.getSource().getLabel();

			findColumn(grid, label).setVisible(event.getSource().getValue());

			VALUES_MAP.put(label, event.getSource().getValue());
		    }
		});

		Boolean value = VALUES_MAP.get(descriptor.getColumnName());
		checkBox.setValue(value != null ? value : true);

		findColumn(grid, descriptor.getColumnName()).setVisible(checkBox.getValue());
	    }
	});
    }

    /**
     * @param grid
     * @param colName
     * @return
     */
    private Column<HashMap<String, String>> findColumn(GridComponent grid, String colName) {

	return grid.getColumns().//
		stream().//
		filter(c -> c.getKey().equals(colName)).//
		findFirst().//
		get();
    }

    /**
     * 
     */
    public static void clearValuesCache() {

	VALUES_MAP.clear();
    }
}
