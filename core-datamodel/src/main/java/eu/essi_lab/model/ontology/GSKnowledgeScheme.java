/**
 * 
 */
package eu.essi_lab.model.ontology;

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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabrizio
 */
public class GSKnowledgeScheme {

    private String nameSpace;
    private GSPredicate labelPredicate;
    private GSPredicate abstractPredicate;
    private List<GSPredicate> expandPredicates;
    private List<GSPredicate> collapsePredicates;

    /**
     * 
     */
    public GSKnowledgeScheme() {

	this(null);
    }

    /**
     * @param nameSpace
     */
    public GSKnowledgeScheme(String nameSpace) {

	this.expandPredicates = new ArrayList<>();
	this.collapsePredicates = new ArrayList<>();
	this.nameSpace = nameSpace;
    }

    /**
     * @return
     */
    public List<GSPredicate> getExpandPredicates() {
	return expandPredicates;
    }

    /**
     * @param expandPredicates
     */
    public void setExpandPredicates(List<GSPredicate> expandPredicates) {
	this.expandPredicates = expandPredicates;
    }

    /**
     * @return
     */
    public List<GSPredicate> getCollapsePredicates() {
	return collapsePredicates;
    }

    /**
     * @param collapsePredicates
     */
    public void setCollapsePredicates(List<GSPredicate> collapsePredicates) {
	this.collapsePredicates = collapsePredicates;
    }

    /**
     * @return
     */
    public GSPredicate getLabelPredicate() {
	return labelPredicate;
    }

    /**
     * @param labelPredicate
     */
    public void setLabelPredicate(GSPredicate labelPredicate) {
	this.labelPredicate = labelPredicate;
    }

    /**
     * @return
     */
    public GSPredicate getAbstractPredicate() {
	return abstractPredicate;
    }

    /**
     * @param abstractPredicate
     */
    public void setAbstractPredicate(GSPredicate abstractPredicate) {
	this.abstractPredicate = abstractPredicate;
    }

    /**
     * @return
     */
    public String getNamespace() {
	return nameSpace;
    }

    /**
     * @param nameSpace
     */
    public void setNameSpace(String nameSpace) {
	this.nameSpace = nameSpace;
    }
}
