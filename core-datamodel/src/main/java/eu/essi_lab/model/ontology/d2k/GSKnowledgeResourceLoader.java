package eu.essi_lab.model.ontology.d2k;

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

import java.util.Iterator;
import java.util.Optional;
import java.util.ServiceLoader;

import eu.essi_lab.model.ontology.d2k.resources.GSKnowledgeResource;

/**
 * @author ilsanto
 */
public class GSKnowledgeResourceLoader {

    /**
     * @param id
     * @param type
     * @return
     */
    public static Optional<GSKnowledgeResource> instantiateKnowledgeResource(String id, String type) {

	Iterator<GSKnowledgeResource> it = ServiceLoader.load(GSKnowledgeResource.class).iterator();

	while (it.hasNext()) {

	    GSKnowledgeResource next = it.next();

	    if (next.getKnowledgeClass().equals(type)) {
		next.setId(id);
		return Optional.of(next);
	    }

	}

	return Optional.empty();
    }
}
