package eu.essi_lab.session;

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
 * Coordinates exclusive access to a single session for systems that allow only one active session
 * (e.g. ARPA Lombardia, ARPA Marche).
 * <ul>
 * <li>File strategy: no cross-node coordination; work is executed directly.</li>
 * <li>Jedis strategy: nodes enter a queue, send heartbeats; first in queue runs after current
 * holder finishes or is considered dead (no heartbeat for 30s).</li>
 * </ul>
 */
public interface SessionCoordinator {

    /**
     * Work that runs under exclusive session; may throw Exception.
     */
    @FunctionalInterface
    interface SessionWork<T> {
	T run() throws Exception;
    }

    /**
     * Callback to reclaim (e.g. logout) a dead node's session when the coordinator detects the
     * active node has stopped sending heartbeats.
     */
    @FunctionalInterface
    interface SessionReclaimer {
	void reclaim(String token) throws Exception;
    }

    /**
     * Runs the given work while holding exclusive session access. The work typically performs
     * login(), the API call, then logout(). The coordinator guarantees only one such run
     * executes at a time (cluster-wide when using Jedis).
     *
     * @param reclaimer callback to logout a dead node's token when reclaiming
     * @param work      the work to run under the session (login → ... → logout)
     * @return the value returned by the work
     * @throws Exception if coordination or work fails
     */
    <T> T runWithExclusiveSession(SessionReclaimer reclaimer, SessionWork<T> work) throws Exception;
}
