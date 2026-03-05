package eu.essi_lab.session;

import java.io.IOException;

import redis.clients.jedis.Jedis;
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
 * Token store using Redis. Shared across nodes so only one session token exists cluster-wide.
 * Uses a namespace (e.g. lombardia, marche) to isolate tokens for different systems.
 */
public class RedisTokenStore implements TokenStore {

    private static final String KEY_TOKEN_SUFFIX = ":token";

    private final String namespace;
    private final JedisPool pool;

    /**
     * @param namespace identifier for the target system (e.g. lombardia, marche)
     * @param pool      Redis connection pool
     */
    public RedisTokenStore(String namespace, JedisPool pool) {
	this.namespace = namespace != null && !namespace.isEmpty() ? namespace : "default";
	this.pool = pool;
    }

    private String tokenKey() {
	return namespace + KEY_TOKEN_SUFFIX;
    }

    @Override
    public String readToken() throws IOException {
	try (Jedis jedis = pool.getResource()) {
	    return jedis.get(tokenKey());
	} catch (Exception e) {
	    throw new IOException("Redis read token failed", e);
	}
    }

    @Override
    public void writeToken(String token) throws IOException {
	try (Jedis jedis = pool.getResource()) {
	    jedis.set(tokenKey(), token);
	} catch (Exception e) {
	    throw new IOException("Redis write token failed", e);
	}
    }

    @Override
    public void deleteToken(String token) throws IOException {
	try (Jedis jedis = pool.getResource()) {
	    jedis.del(tokenKey());
	} catch (Exception e) {
	    throw new IOException("Redis delete token failed", e);
	}
    }
}
