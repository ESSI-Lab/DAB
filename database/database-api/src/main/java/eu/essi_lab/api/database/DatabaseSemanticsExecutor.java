/**
 * 
 */
package eu.essi_lab.api.database;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.Optional;

import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.count.SemanticCountResponse;
import eu.essi_lab.messages.sem.SemanticMessage;
import eu.essi_lab.messages.sem.SemanticResponse;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.ontology.GSKnowledgeResourceDescription;
import eu.essi_lab.model.ontology.GSKnowledgeScheme;

/**
 * @author Fabrizio
 */
public interface DatabaseSemanticsExecutor extends DatabaseProvider {

    /**
     * Counts the {@link GSKnowledgeResourceDescription} which match the supplied
     * <code>message</code>
     *
     * @param message
     * @return
     * @throws GSException
     */
    public SemanticCountResponse count(SemanticMessage message) throws GSException;

    /**
     * Get the {@link SemanticResponse} resulting from the supplied
     * <code>message</code>.<br>
     * If <code>message</code> has no {@link Page} set, no limitation on the semantic objects is applied
     *
     * @param message
     * @return
     * @throws GSException
     */
    public SemanticResponse<GSKnowledgeResourceDescription> execute(SemanticMessage message) throws GSException;

    /**
     * @param scheme
     * @param subjectId
     * @return
     * @throws GSException
     */
    public Optional<GSKnowledgeResourceDescription> getKnowlegdeResource(GSKnowledgeScheme scheme, String subjectId) throws GSException;
}
