package eu.essi_lab.accessor.hiscentral.lombardia;

import eu.essi_lab.session.RedisTokenStore;
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
 * Lombardia token store using Redis. Delegates to {@link RedisTokenStore}.
 */
public class RedisLombardiaTokenStore extends RedisTokenStore implements LombardiaTokenStore {

    /**
     * Uses default namespace {@value JedisLombardiaSessionCoordinator#NAMESPACE}.
     */
    public RedisLombardiaTokenStore(JedisPool pool) {
	this(JedisLombardiaSessionCoordinator.NAMESPACE, pool);
    }

    /**
     * @param namespace identifier for Redis keys (e.g. arpa-lombardia, arpa-marche)
     * @param pool      Redis connection pool
     */
    public RedisLombardiaTokenStore(String namespace, JedisPool pool) {
	super(namespace, pool);
    }
}
