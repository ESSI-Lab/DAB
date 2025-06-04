package eu.essi_lab.model.ontology;

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
 * @author ilsanto
 */
public class RelationToParent {

    private GSPredicate relation;
    private String relationName;
    private String relationDescription;

    private GSKnowledgeOntology ontology;

    public GSPredicate getRelation() {
	return relation;
    }

    public void setRelation(GSPredicate relation) {
	this.relation = relation;
    }

    /**
     * @return
     */
    public String getRelationName() {
	return relationName;
    }

    /**
     * @param relationName
     */
    public void setRelationName(String relationName) {
	this.relationName = relationName;
    }

    /**
     * @return
     */
    public String getRelationDescription() {
	return relationDescription;
    }

    /**
     * @param relationDescription
     */
    public void setRelationDescription(String relationDescription) {
	this.relationDescription = relationDescription;
    }

    public GSKnowledgeOntology getOntology() {
	return ontology;
    }

    public void setOntology(GSKnowledgeOntology ontology) {
	this.ontology = ontology;
    }
}
