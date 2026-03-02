package eu.essi_lab.accessor.hiscentral.lombardia;

import java.net.URL;

import redis.clients.jedis.JedisPool;

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
 * Factory for {@link HISCentralLombardiaClient} with different session coordination strategies.
 * Use {@link #createWithRedisCoordinator(URL, String, String, String, JedisPool)} when running
 * on multiple nodes so that only one node holds the ARPA Lombardia session at a time (queue +
 * heartbeat and dead-node recovery).
 */
public final class LombardiaClients {

    private LombardiaClients() {
    }

    /**
     * Creates a client that uses Redis for token storage and a queue-based session coordinator.
     * Nodes enter a queue, send heartbeats every 5s; if the active node has not sent a heartbeat
     * in 30s it is considered dead and the first in queue will logout its token and take over.
     *
     * @param endpoint          HIS Central endpoint URL
     * @param keystorePassword   keystore password for the proxy
     * @param username          Lombardia username
     * @param password          Lombardia password
     * @param jedisPool         shared Redis connection pool (e.g. from configuration)
     * @return client that coordinates via Redis
     * @throws Exception if client construction fails
     */
    public static HISCentralLombardiaClient createWithRedisCoordinator(URL endpoint, String keystorePassword,
	    String username, String password, JedisPool jedisPool) throws Exception {
	LombardiaTokenStore tokenStore = new RedisLombardiaTokenStore(jedisPool);
	LombardiaSessionCoordinator coordinator = new JedisLombardiaSessionCoordinator(jedisPool, tokenStore);
	return new HISCentralLombardiaClient(endpoint, keystorePassword, username, password, tokenStore, coordinator);
    }
}
