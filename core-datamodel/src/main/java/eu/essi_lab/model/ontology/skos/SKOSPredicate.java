/**
 * 
 */
package eu.essi_lab.model.ontology.skos;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.model.ontology.GSPredicate;
import eu.essi_lab.model.ontology.OntologyURIs;

/**
 * @author Fabrizio
 */
public class SKOSPredicate extends GSPredicate {

    /**
     * 
     */
    private static final long serialVersionUID = 2487036801868876093L;

    private String uri;

    /**
     * @param uri
     */
    public SKOSPredicate(String uri) {

	this.uri = uri;
    }

    @Override
    public String getNamespace() {

	return OntologyURIs.SKOS_NAMESPACE;
    }

    @Override
    public String getLocalName() {

	return uri;
    }
}
