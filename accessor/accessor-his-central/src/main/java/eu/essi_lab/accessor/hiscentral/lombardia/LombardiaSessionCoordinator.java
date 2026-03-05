package eu.essi_lab.accessor.hiscentral.lombardia;

import eu.essi_lab.session.SessionCoordinator;

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

/**
 * Lombardia-specific session coordinator. Extends the generic {@link SessionCoordinator} with a
 * convenience method that takes the Lombardia client directly.
 */
public interface LombardiaSessionCoordinator {

    /**
     * Work that runs under exclusive session; may throw Exception.
     */
    @FunctionalInterface
    interface SessionWork<T> extends SessionCoordinator.SessionWork<T> {
	@Override
	T run() throws Exception;
    }

    /**
     * Runs the given work while holding exclusive session access. The work typically
     * performs login(), the SOAP call, then logout(). The coordinator guarantees only one
     * such run executes at a time (cluster-wide when using Jedis).
     *
     * @param client the Lombardia client (used by Jedis coordinator to logout a dead node's token)
     * @param work  the work to run under the session (login → ... → logout)
     * @return the value returned by the work
     * @throws Exception if coordination or work fails
     */
    <T> T runWithExclusiveSession(HISCentralLombardiaClient client, SessionWork<T> work) throws Exception;
}
