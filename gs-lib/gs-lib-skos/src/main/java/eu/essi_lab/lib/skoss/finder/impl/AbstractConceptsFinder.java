/**
 * 
 */
package eu.essi_lab.lib.skoss.finder.impl;

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

import eu.essi_lab.lib.skoss.finder.ConceptsFinder;
import eu.essi_lab.lib.skoss.finder.ConceptsQueryExecutor;
import eu.essi_lab.lib.skoss.finder.ConceptsQueryBuilder;

/**
 * @author Fabrizio
 */
public abstract class AbstractConceptsFinder implements ConceptsFinder {

    private ConceptsQueryBuilder builder;
    private ConceptsQueryExecutor executor;

    /**
     * 
     */
    public AbstractConceptsFinder() {

	setQueryBuilder(new DefaultConceptsQueryBuilder());
    }

    /**
     * @return the builder
     */
    public ConceptsQueryBuilder getQueryBuilder() {

	return builder;
    }

    /**
     * @param builder
     */
    public void setQueryBuilder(ConceptsQueryBuilder builder) {

	this.builder = builder;
    }

    /**
     * @return the executor
     */
    public ConceptsQueryExecutor getExecutor() {

	return executor;
    }

    /**
     * @param executor
     */
    public void setExecutor(ConceptsQueryExecutor executor) {

	this.executor = executor;
    }
}
