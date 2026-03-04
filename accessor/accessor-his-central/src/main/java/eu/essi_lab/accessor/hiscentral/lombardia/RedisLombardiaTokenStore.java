package eu.essi_lab.accessor.hiscentral.lombardia;

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
 */
public class RedisLombardiaTokenStore implements LombardiaTokenStore {

    private static final String KEY_TOKEN = "arpa-lombardia:token";

    private final JedisPool pool;

    public RedisLombardiaTokenStore(JedisPool pool) {
	this.pool = pool;
    }

    @Override
    public String readToken() throws IOException {
	try (Jedis jedis = pool.getResource()) {
	    return jedis.get(KEY_TOKEN);
	} catch (Exception e) {
	    throw new IOException("Redis read token failed", e);
	}
    }

    @Override
    public void writeToken(String token) throws IOException {
	try (Jedis jedis = pool.getResource()) {
	    jedis.set(KEY_TOKEN, token);
	} catch (Exception e) {
	    throw new IOException("Redis write token failed", e);
	}
    }

    @Override
    public void deleteToken(String token) throws IOException {
	try (Jedis jedis = pool.getResource()) {
	    jedis.del(KEY_TOKEN);
	} catch (Exception e) {
	    throw new IOException("Redis delete token failed", e);
	}
    }
}

