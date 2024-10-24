/**
 * 
 */
package eu.essi_lab.api.database.marklogic.search;

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

import eu.essi_lab.api.database.marklogic.MarkLogicDatabase;
import eu.essi_lab.api.database.marklogic.search.def.DefaultMarkLogicSearchBuilder;
import eu.essi_lab.api.database.marklogic.search.module.ModuleMarkLogicSearchBuilder;
import eu.essi_lab.messages.DiscoveryMessage;

/**
 * @author Fabrizio
 */
public class MarkLogicSearchBuilderFactory {

    /**
     * @author Fabrizio
     */
    public enum SearchBuilderImpl {

	/**
	 * 
	 */
	DEFAULT,
	/**
	 * 
	 */
	MODULE;
    }

    /**
     * @param impl
     * @param message
     * @param markLogicDB
     * @return
     */
    public static MarkLogicSearchBuilder createBuilder(DiscoveryMessage message, MarkLogicDatabase markLogicDB) {

	return createBuilder(SearchBuilderImpl.MODULE, message, markLogicDB);
    }

    /**
     * @param impl
     * @param message
     * @param markLogicDB
     * @return
     */
    public static MarkLogicSearchBuilder createBuilder(SearchBuilderImpl impl, DiscoveryMessage message, MarkLogicDatabase markLogicDB) {

	switch (impl) {
	case DEFAULT:
	    return new DefaultMarkLogicSearchBuilder(message, markLogicDB);
	case MODULE:
	default:
	    return new ModuleMarkLogicSearchBuilder(message, markLogicDB);
	}
    }
}
