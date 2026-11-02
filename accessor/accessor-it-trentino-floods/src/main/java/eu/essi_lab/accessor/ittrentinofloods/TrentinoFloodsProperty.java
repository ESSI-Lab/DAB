package eu.essi_lab.accessor.ittrentinofloods;

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

import java.util.Arrays;
import java.util.Optional;

/**
 * The quantities monitored by the hydrometric sensors of the Ufficio Dighe, as named by the "quantity" member of the series index
 *
 * @author boldrini
 */
public enum TrentinoFloodsProperty {

    /**
     * Water level (gage height) measured by the sensor
     */
    LEVEL("gage height", "Water level", "Livello idrometrico", "m", "metres"),

    /**
     * Discharge calculated from the water level by means of a rating curve
     */
    DISCHARGE("discharge", "Discharge", "Portata (calcolata)", "m3/s", "cubic metres per second");

    private final String quantity;
    private final String label;
    private final String originalName;
    private final String unitAbbreviation;
    private final String unitName;

    /**
     * @param quantity
     * @param label
     * @param originalName
     * @param unitAbbreviation
     * @param unitName
     */
    private TrentinoFloodsProperty(String quantity, String label, String originalName, String unitAbbreviation, String unitName) {

	this.quantity = quantity;
	this.label = label;
	this.originalName = originalName;
	this.unitAbbreviation = unitAbbreviation;
	this.unitName = unitName;
    }

    /**
     * @return the quantity as named by the series index, e.g. "gage height"
     */
    public String getQuantity() {

	return quantity;
    }

    /**
     * @return the English name, e.g. "Water level"
     */
    public String getLabel() {

	return label;
    }

    /**
     * @return the Italian name used by the provider, e.g. "Livello idrometrico"
     */
    public String getOriginalName() {

	return originalName;
    }

    /**
     * @return
     */
    public String getUnitAbbreviation() {

	return unitAbbreviation;
    }

    /**
     * @return
     */
    public String getUnitName() {

	return unitName;
    }

    /**
     * @param quantity the "quantity" member of the series index, e.g. "gage height"
     * @return
     */
    public static Optional<TrentinoFloodsProperty> decode(String quantity) {

	if (quantity == null) {
	    return Optional.empty();
	}

	return Arrays.stream(values()).filter(p -> p.getQuantity().equalsIgnoreCase(quantity.trim())).findFirst();
    }
}
