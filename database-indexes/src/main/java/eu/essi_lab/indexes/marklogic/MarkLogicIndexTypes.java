package eu.essi_lab.indexes.marklogic;

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
public enum MarkLogicIndexTypes {

    /**
     * 
     */
    RANGE_ELEMENT_INDEX("range-element-indexes"),
    /**
    * 
    */
    RANGE_ELEMENT_ATTRIBUTE_INDEX("range-element-attribute-indexes"),

    /**
     * 
     */
    ELEMENT_WORD_LEXICON("element-word-lexicons"),

    /**
     * 
     */
    ELEMENT_ATTRIBUTE_WORD_LEXICON("element-attribute-word-lexicons"),

    /**
     * 
     */
    WORD_QUERY("word-query"),

    /**
     * 
     */
    GEOSPATIAL_ELEMENT_INDEX("geospatial-element-indexes");

    private String type;

    private MarkLogicIndexTypes(String type) {
	this.type = type;

    }

    public String getType() {
	return type;
    }

    public String toString() {

	return type;
    }

}
