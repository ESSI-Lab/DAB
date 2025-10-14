/**
 * 
 */
package eu.essi_lab.lib.skoss;

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

import eu.essi_lab.lib.utils.LabeledEnum;

/**
 * @author Fabrizio
 */
public enum SKOSSemanticRelation implements LabeledEnum {

    /**
     * 
     */
    SEMANTIC_RELATION("skos:semanticRelation"),
    /**
     * 
     */
    RELATED("skos:related"),
    /**
     * 
     */
    RELATED_MATCH("skos:relatedMatch"),
    /**
    * 
    */
    BROADER_TRANSITIVE("skos:broaderTransitive"),
    /**
    * 
    */
    BROADER("skos:broader"),
    /**
    * 
    */
    BROAD_MATCH("skos:broadMatch"),
    /**
     * 
     */
    NARROWER_TRANSITIVE("skos:narrowerTransitive"),
    /**
     * 
     */
    NARROWER("skos:narrower"),
    /**
     * 
     */
    NARROW_MATCH("skos:narrowMatch"),
    /**
     * 
     */
    MAPPING_RELATION("skos:mappingRelation"),
    /**
     * 
     */
    CLOSE_MATCH("skos:closeMatch"),
    /**
     * 
     */
    EXACT_MATCH("skos:exactMatch");

    private String label;

    /**
     * @param label
     */
    private SKOSSemanticRelation(String label) {

	this.label = label;
    }

    @Override
    public String getLabel() {
	return label;
    }

}
