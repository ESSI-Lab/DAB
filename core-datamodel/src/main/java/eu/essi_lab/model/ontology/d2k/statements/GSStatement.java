package eu.essi_lab.model.ontology.d2k.statements;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;

import eu.essi_lab.model.ontology.GSPredicate;
import eu.essi_lab.model.ontology.d2k.resources.GSKnowledgeResource;
public class GSStatement implements Statement {

    /**
     * 
     */
    private static final long serialVersionUID = 4810299943464169245L;

    @Override
    public GSKnowledgeResource getSubject() {
	return null;
    }

    @Override
    public GSPredicate getPredicate() {
	return null;
    }

    @Override
    public Value getObject() {
	return null;
    }

    @Override
    public Resource getContext() {
	return null;
    }
}
