package eu.essi_lab.pdk.handler;

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

import java.util.Iterator;
import java.util.ServiceLoader;

import eu.essi_lab.messages.count.SemanticCountResponse;
import eu.essi_lab.messages.sem.SemanticMessage;
import eu.essi_lab.messages.sem.SemanticResponse;
import eu.essi_lab.model.ontology.GSKnowledgeResourceDescription;
import eu.essi_lab.request.executor.IRequestExecutor;
import eu.essi_lab.request.executor.ISemanticExecutor;

/**
 * @author Fabrizio
 */
public class SemanticHandler<T> extends
	ProfilerHandler<SemanticMessage, GSKnowledgeResourceDescription, T, SemanticCountResponse, SemanticResponse<GSKnowledgeResourceDescription>, SemanticResponse<T>> {

    public SemanticHandler() {

	super();
    }

    @Override
    protected IRequestExecutor<SemanticMessage, GSKnowledgeResourceDescription, SemanticCountResponse, SemanticResponse<GSKnowledgeResourceDescription>> createExecutor() {

	ServiceLoader<ISemanticExecutor> loader = ServiceLoader.load(ISemanticExecutor.class);

	Iterator<ISemanticExecutor> it = loader.iterator();

	if (it.hasNext())
	    return it.next();

	return null;
    }
}
