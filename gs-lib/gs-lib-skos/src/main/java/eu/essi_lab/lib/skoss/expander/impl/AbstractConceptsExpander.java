/**
 * 
 */
package eu.essi_lab.lib.skoss.expander.impl;

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

import eu.essi_lab.lib.skoss.expander.ConceptsExpander;
import eu.essi_lab.lib.skoss.expander.ExpandConceptsQueryBuilder;

/**
 * @author Fabrizio
 */
public abstract class AbstractConceptsExpander implements ConceptsExpander {

    private ExpandConceptsQueryBuilder builder;
    private boolean traceQuery;

    /**
     * 
     */
    public AbstractConceptsExpander() {

	setQueryBuilder(new DefaultExpandConceptsQueryBuilder());
    }

    /**
     * @return the
     */
    public ExpandConceptsQueryBuilder getQueryBuilder() {

	return builder;
    }

    /**
     * @param builder
     */
    public void setQueryBuilder(ExpandConceptsQueryBuilder builder) {

	this.builder = builder;
    }

    /**
     * @return
     */
    public boolean traceQuery() {

	return traceQuery;
    }

    /**
     * @param traceQuery
     */
    public void setTraceQuery(boolean traceQuery) {

	this.traceQuery = traceQuery;
    }
}
