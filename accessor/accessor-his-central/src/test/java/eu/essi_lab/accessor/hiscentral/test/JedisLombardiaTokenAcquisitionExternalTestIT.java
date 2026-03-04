package eu.essi_lab.accessor.hiscentral.test;

import eu.essi_lab.accessor.hiscentral.lombardia.HISCentralLombardiaClient;
import eu.essi_lab.accessor.hiscentral.lombardia.JedisLombardiaSessionCoordinator;
import eu.essi_lab.accessor.hiscentral.lombardia.LombardiaSessionCoordinator.SessionWork;
import eu.essi_lab.accessor.hiscentral.lombardia.RedisLombardiaTokenStore;
import org.junit.Assume;
import org.junit.Test;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

/**
 * Integration test for Jedis-based token acquisition: runs multiple threads (simulating nodes)
 * that compete for exclusive session access. Verifies that only one "node" runs at a time.
 * <p>
 * System properties (all optional; test is skipped if Redis is not configured):
 * <ul>
 *   <li>{@code lombardia.jedis.host} – Redis host (default: localhost)</li>
 *   <li>{@code lombardia.jedis.port} – Redis port (default: 6379)</li>
 *   <li>{@code lombardia.jedis.nodes} – Number of simulated nodes / threads (default: 4)</li>
 *   <li>{@code lombardia.jedis.runs} – Number of acquisitions per node (default: 5)</li>
 * </ul>
 * Run with Redis: {@code -Dlombardia.jedis.host=localhost -Dlombardia.jedis.port=6379}
 * Skip when Redis is down: omit these properties or set {@code lombardia.jedis.skip=true}.
 */
public class JedisLombardiaTokenAcquisitionExternalTestIT {

    private static final String PROP_HOST = "lombardia.jedis.host";
    private static final String PROP_PORT = "lombardia.jedis.port";
    private static final String PROP_NODES = "lombardia.jedis.nodes";
    private static final String PROP_RUNS = "lombardia.jedis.runs";
    private static final String PROP_SKIP = "lombardia.jedis.skip";

    @Test
    public void multipleNodesAcquireTokenExclusively() throws Exception {
        String skip = System.getProperty(PROP_SKIP, "false");
        Assume.assumeTrue("Test skipped by " + PROP_SKIP, !Boolean.parseBoolean(skip));

        String host = System.getProperty(PROP_HOST, "localhost");
        int port = Integer.parseInt(System.getProperty(PROP_PORT, "6379"));
        int numNodes = Integer.parseInt(System.getProperty(PROP_NODES, "4"));
        int runsPerNode = Integer.parseInt(System.getProperty(PROP_RUNS, "5"));

        JedisPool pool;
        try {
            pool = new JedisPool(new JedisPoolConfig(), host, port);
            try (var jedis = pool.getResource()) {
                jedis.ping();
            }
        } catch (Exception e) {
            Assume.assumeTrue("Redis not available at " + host + ":" + port + " - " + e.getMessage(), false);
            return;
        }

        RedisLombardiaTokenStore tokenStore = new RedisLombardiaTokenStore(pool);
        JedisLombardiaSessionCoordinator coordinator = new JedisLombardiaSessionCoordinator(pool, tokenStore);

        HISCentralLombardiaClient mockClient = mock(HISCentralLombardiaClient.class);
        doNothing().when(mockClient).logoutWithToken(anyString());

        // Only one thread may hold the "session" at a time.
        AtomicInteger inSession = new AtomicInteger(0);
        AtomicLong totalAcquisitions = new AtomicLong(0);
        List<AssertionError> failures = new ArrayList<>();

        CyclicBarrier startBarrier = new CyclicBarrier(numNodes);
        Thread[] threads = new Thread[numNodes];

        for (int n = 0; n < numNodes; n++) {
            int nodeId = n;
            threads[n] = new Thread(() -> {
                try {
                    startBarrier.await();
                    for (int r = 0; r < runsPerNode; r++) {
                        coordinator.runWithExclusiveSession(mockClient, (SessionWork<Void>) () -> {
                            int now = inSession.incrementAndGet();
                            if (now != 1) {
                                failures.add(new AssertionError(
                                        "Node " + nodeId + " run : expected 1 thread in session, got " + now));
                            }
                            try {
                                Thread.sleep(20);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                throw new RuntimeException(e);
                            }
                            inSession.decrementAndGet();
                            totalAcquisitions.incrementAndGet();
                            return null;
                        });
                    }
                } catch (Exception e) {
                    failures.add(new AssertionError("Node " + nodeId + ": " + e.getMessage(), e));
                }
            }, "node-" + n);
            threads[n].start();
        }

        for (Thread t : threads) {
            t.join(60_000);
            assertTrue("Thread " + t.getName() + " did not finish in time", !t.isAlive());
        }

        if (!failures.isEmpty()) {
            AssertionError first = failures.get(0);
            for (int i = 1; i < failures.size(); i++) {
                first.addSuppressed(failures.get(i));
            }
            throw first;
        }

        long expected = (long) numNodes * runsPerNode;
        assertEquals("Total acquisitions", expected, totalAcquisitions.get());
    }
}
