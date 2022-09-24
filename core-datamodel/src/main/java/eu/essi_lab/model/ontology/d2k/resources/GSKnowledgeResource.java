package eu.essi_lab.model.ontology.d2k.resources;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import org.openrdf.model.IRI;
import org.openrdf.model.Resource;
import org.openrdf.model.impl.SimpleValueFactory;

import eu.essi_lab.model.ontology.GSKnowledgeOntology;

/**
 * @author ilsanto
 */
public abstract class GSKnowledgeResource implements Resource, IRI {

    /**
     * 
     */
    private static final long serialVersionUID = -3342069891925698270L;
    private String id;
    private GSKnowledgeOntology source;

    public GSKnowledgeResource(String identifier) {
	id = identifier;
    }

    public GSKnowledgeResource() {

    }

    public abstract String getKnowledgeClass();

    public void setSource(GSKnowledgeOntology source) {
	this.source = source;
    }

    @Override
    public String stringValue() {
	return id;
    }

    public void setId(String identifier) {
	id = identifier;
    }

    public GSKnowledgeOntology getSource() {
	return source;
    }


    @Override
    public String getNamespace() {
	return SimpleValueFactory.getInstance().createIRI(id).getNamespace();
    }

    @Override
    public String getLocalName() {
	return SimpleValueFactory.getInstance().createIRI(id).getLocalName();
    }

    @Override
    public String toString() {
        return stringValue();
    }
}
