package eu.essi_lab.accessor.debrandenburgpegel;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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

import java.util.Optional;

/**
 * Parameters published by the Pegelportal Brandenburg, each one with the tab of the gauge page holding its observations of the
 * last year
 *
 * @author boldrini
 */
public enum BrandenburgPegelParameter {

    /**
     * Water level, in centimetres above the gauge zero (Pegelnullpunkt)
     */
    W("Wasserstand", "Water level", "cm", "centimetres", "ws_graph", "Tab1"), //
    /**
     * Discharge
     */
    Q("Abfluss", "Discharge", "m3/s", "cubic metres per second", "q_graph", "Tab2");

    private final String originalName;
    private final String label;
    private final String unit;
    private final String unitName;
    private final String theme;
    private final String tab;

    /**
     * @param originalName
     * @param label
     * @param unit
     * @param unitName
     * @param theme
     * @param tab
     */
    private BrandenburgPegelParameter(String originalName, String label, String unit, String unitName, String theme, String tab) {

	this.originalName = originalName;
	this.label = label;
	this.unit = unit;
	this.unitName = unitName;
	this.theme = theme;
	this.tab = tab;
    }

    /**
     * @return the German name used by the portal, e.g. "Wasserstand"
     */
    public String getOriginalName() {

	return originalName;
    }

    /**
     * @return the English name, e.g. "Water level"
     */
    public String getLabel() {

	return label;
    }

    /**
     * @return the unit abbreviation, e.g. "cm"
     */
    public String getUnit() {

	return unit;
    }

    /**
     * @return the unit name, e.g. "centimetres"
     */
    public String getUnitName() {

	return unitName;
    }

    /**
     * @return the theme of the gauge page showing this parameter, e.g. "ws_graph"
     */
    public String getTheme() {

	return theme;
    }

    /**
     * @return the identifier of the tab of the gauge page holding the observations, e.g. "Tab1"
     */
    public String getTab() {

	return tab;
    }

    /**
     * @param code the parameter code, e.g. "W"
     * @return
     */
    public static Optional<BrandenburgPegelParameter> decode(String code) {

	if (code == null) {
	    return Optional.empty();
	}

	for (BrandenburgPegelParameter parameter : values()) {
	    if (parameter.name().equalsIgnoreCase(code.trim())) {
		return Optional.of(parameter);
	    }
	}

	return Optional.empty();
    }
}
