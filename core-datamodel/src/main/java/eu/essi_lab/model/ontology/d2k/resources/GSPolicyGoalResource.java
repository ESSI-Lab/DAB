package eu.essi_lab.model.ontology.d2k.resources;

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

import eu.essi_lab.model.ontology.OntologyURIs;

/**
 * @author ilsanto
 */
public class GSPolicyGoalResource extends GSKnowledgeResource {

    /**
     * 
     */
    private static final long serialVersionUID = -7397125528512961430L;
    private final static String POLICY_GOAL_TYPE = OntologyURIs.ESSI_D2K_NAMESPACE + "Policy_Goal";

    @Override
    public String getKnowledgeClass() {
	return POLICY_GOAL_TYPE;
    }
}
