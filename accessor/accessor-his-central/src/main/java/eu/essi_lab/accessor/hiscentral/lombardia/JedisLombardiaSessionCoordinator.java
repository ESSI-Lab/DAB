package eu.essi_lab.accessor.hiscentral.lombardia;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import eu.essi_lab.lib.utils.GSLoggerFactory;
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
 * Session coordinator using Redis: nodes enter a queue, send heartbeats every 5s; the first in
 * queue runs after the current holder finishes or is considered dead (no heartbeat for 30s).
 * Dead nodes in the queue are removed so the next can proceed.
 */
public class JedisLombardiaSessionCoordinator implements LombardiaSessionCoordinator {

    private static final String KEY_QUEUE = "arpa-lombardia:queue";
    private static final String KEY_ACTIVE = "arpa-lombardia:active";
    private static final String KEY_ALIVE_PREFIX = "arpa-lombardia:alive:";
    private static final int HEARTBEAT_INTERVAL_MS = 5_000;
    private static final int ALIVE_TTL_SECONDS = 30;
    private static final int POLL_SLEEP_MS = 1_000;

    private final JedisPool pool;
    private final LombardiaTokenStore tokenStore;

    public JedisLombardiaSessionCoordinator(JedisPool pool, LombardiaTokenStore tokenStore) {
	this.pool = pool;
	this.tokenStore = tokenStore;
    }

    @Override
    public <T> T runWithExclusiveSession(HISCentralLombardiaClient client, SessionWork<T> work) throws Exception {
	String nodeId = UUID.randomUUID().toString();
	AtomicBoolean heartbeatRunning = new AtomicBoolean(true);
	Thread heartbeatThread = new Thread(() -> heartbeatLoop(nodeId, heartbeatRunning), "lombardia-heartbeat-" + nodeId);
	heartbeatThread.setDaemon(true);
	heartbeatThread.start();

	try {
	    try (Jedis jedis = pool.getResource()) {
		jedis.rpush(KEY_QUEUE, nodeId);
		jedis.setex(KEY_ALIVE_PREFIX + nodeId, ALIVE_TTL_SECONDS, "1");
	    }
	    GSLoggerFactory.getLogger(getClass()).debug("Node {} entered queue", nodeId);

	    while (true) {
		removeDeadNodesFromQueue();
		String first = getFirstInQueue();
		if (!nodeId.equals(first)) {
		    Thread.sleep(POLL_SLEEP_MS);
		    continue;
		}
		String activeNodeId = getActiveNodeId();
		if (activeNodeId != null && !activeNodeId.isEmpty()) {
		    if (isAlive(activeNodeId)) {
			Thread.sleep(POLL_SLEEP_MS);
			continue;
		    }
		    GSLoggerFactory.getLogger(getClass()).info("Active node {} is dead (no heartbeat 30s), reclaiming session", activeNodeId);
		    reclaimDeadSession(client, activeNodeId);
		}
		setActiveNodeId(nodeId);
		try {
		    return work.run();
		} finally {
		    clearActiveNodeId(nodeId);
		    removeSelfFromQueue(nodeId);
		}
	    }
	} finally {
	    heartbeatRunning.set(false);
	}
    }

    private void heartbeatLoop(String nodeId, AtomicBoolean running) {
	while (running.get()) {
	    try (Jedis jedis = pool.getResource()) {
		jedis.setex(KEY_ALIVE_PREFIX + nodeId, ALIVE_TTL_SECONDS, "1");
	    } catch (Exception e) {
		GSLoggerFactory.getLogger(getClass()).warn("Heartbeat failed for node {}: {}", nodeId, e.getMessage());
	    }
	    try {
		Thread.sleep(HEARTBEAT_INTERVAL_MS);
	    } catch (InterruptedException e) {
		Thread.currentThread().interrupt();
		break;
	    }
	}
    }

    private void removeDeadNodesFromQueue() {
	try (Jedis jedis = pool.getResource()) {
	    while (true) {
		String first = jedis.lindex(KEY_QUEUE, 0);
		if (first == null || first.isEmpty()) {
		    break;
		}
		if (!isAlive(jedis, first)) {
		    Long removed = jedis.lrem(KEY_QUEUE, 1, first);
		    if (removed != null && removed > 0) {
			GSLoggerFactory.getLogger(getClass()).debug("Removed dead node {} from queue", first);
		    }
		} else {
		    break;
		}
	    }
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).warn("Failed to remove dead nodes from queue: {}", e.getMessage());
	}
    }

    private boolean isAlive(String nodeId) {
	try (Jedis jedis = pool.getResource()) {
	    return isAlive(jedis, nodeId);
	} catch (Exception e) {
	    return false;
	}
    }

    private boolean isAlive(Jedis jedis, String nodeId) {
	String key = KEY_ALIVE_PREFIX + nodeId;
	Long ttl = jedis.ttl(key);
	return ttl != null && ttl > 0;
    }

    private String getFirstInQueue() {
	try (Jedis jedis = pool.getResource()) {
	    return jedis.lindex(KEY_QUEUE, 0);
	} catch (Exception e) {
	    return null;
	}
    }

    private String getActiveNodeId() {
	try (Jedis jedis = pool.getResource()) {
	    return jedis.get(KEY_ACTIVE);
	} catch (Exception e) {
	    return null;
	}
    }

    private void setActiveNodeId(String nodeId) {
	try (Jedis jedis = pool.getResource()) {
	    jedis.set(KEY_ACTIVE, nodeId);
	}
    }

    private void clearActiveNodeId(String nodeId) {
	try (Jedis jedis = pool.getResource()) {
	    String current = jedis.get(KEY_ACTIVE);
	    if (nodeId.equals(current)) {
		jedis.del(KEY_ACTIVE);
	    }
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).warn("Failed to clear active node: {}", e.getMessage());
	}
    }

    private void removeSelfFromQueue(String nodeId) {
	try (Jedis jedis = pool.getResource()) {
	    jedis.lrem(KEY_QUEUE, 1, nodeId);
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).warn("Failed to remove self from queue: {}", e.getMessage());
	}
    }

    private void reclaimDeadSession(HISCentralLombardiaClient client, String deadNodeId) {
	try {
	    String previousToken = tokenStore.readToken();
	    if (previousToken != null && !previousToken.isEmpty()) {
		client.logoutWithToken(previousToken);
	    }
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).warn("Logout of dead session failed (token may already be invalid): {}", e.getMessage());
	} finally {
	    try {
		tokenStore.deleteToken(null);
	    } catch (Exception e) {
		// ignore
	    }
	}
	try (Jedis jedis = pool.getResource()) {
	    jedis.del(KEY_ACTIVE);
	    jedis.lrem(KEY_QUEUE, 1, deadNodeId);
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).warn("Failed to clear dead active state: {}", e.getMessage());
	}
    }
}
