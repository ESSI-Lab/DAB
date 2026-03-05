package eu.essi_lab.accessor.hiscentral.lombardia;

import eu.essi_lab.session.JedisSessionCoordinator;
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
 * Lombardia session coordinator using Redis. Delegates to {@link JedisSessionCoordinator}.
 */
public class JedisLombardiaSessionCoordinator implements LombardiaSessionCoordinator {

    /** Default namespace for Redis keys (arpa-lombardia for backward compatibility). */
    public static final String NAMESPACE = "arpa-lombardia";

    private final JedisSessionCoordinator delegate;

    /**
     * Uses default namespace {@value #NAMESPACE}.
     */
    public JedisLombardiaSessionCoordinator(JedisPool pool, LombardiaTokenStore tokenStore) {
	this(NAMESPACE, pool, tokenStore);
    }

    /**
     * @param namespace  identifier for Redis keys (e.g. arpa-lombardia, arpa-marche)
     * @param pool      Redis connection pool
     * @param tokenStore token store with the same namespace
     */
    public JedisLombardiaSessionCoordinator(String namespace, JedisPool pool, LombardiaTokenStore tokenStore) {
	this.delegate = new JedisSessionCoordinator(namespace, pool, tokenStore);
    }

    @Override
    public <T> T runWithExclusiveSession(HISCentralLombardiaClient client, SessionWork<T> work) throws Exception {
	return delegate.runWithExclusiveSession(client::logoutWithToken, work);
    }
}
