/**
 * 
 */
package eu.essi_lab.lib.utils;

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

/**
 * @author Fabrizio
 */
public enum EuropeanLanguage implements LabeledEnum {
    /**
     * 
     */
    BULGARIAN("bg"),
    /**
     * 
     */
    CZECH("cs"),
    /**
     * 
     */
    DANISH("da"),
    /**
    * 
    */
    ENGLISH("en"),
    /**
     * 
     */
    GERMAN("de"),
    /**
     * 
     */
    GREEK("el"),
    /**
     * 
     */
    SPANISH("es"),
    /**
     * 
     */
    ESTONIAN("et"),
    /**
     * 
     */
    FINNISH("fi"),
    /**
     * 
     */
    FRENCH("fr"),
    /**
     * 
     */
    CROATIAN("hr"),
    /**
     * 
     */
    HUNGARIAN("hu"),
    /**
     * 
     */
    ITALIAN("it"),
    /**
     * 
     */
    LITHUANIAN("lt"),
    /**
     * 
     */
    LATVIAN("lv"),
    /**
     * 
     */
    DUTCH("nl"),
    /**
     * 
     */
    POLISH("pl"),
    /**
     * 
     */
    PORTUGUESE("pt"),
    /**
     * 
     */
    ROMANIAN("ro"),
    /**
     * 
     */
    SLOVAK("sk"),
    /**
     * 
     */
    SLOVENIAN("sl"),
    /**
     * 
     */
    SWEDISH("sv");

    private final String code;

    /**
     * @param code
     */
    private EuropeanLanguage(String code) {

	this.code = code;
    }

    @Override
    public String getLabel() {

	return code;
    }
}
