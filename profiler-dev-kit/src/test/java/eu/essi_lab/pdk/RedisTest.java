package eu.essi_lab.pdk;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisTest {

    public static void main(String[] args) {

	JedisPoolConfig poolConfig = new JedisPoolConfig();
	poolConfig.setMaxTotal(3); // Maximum number of connections
	poolConfig.setMaxIdle(3);
	poolConfig.setBlockWhenExhausted(true);
	// Create a JedisPool instance
	JedisPool pool = new JedisPool(poolConfig, "localhost", 6379);

	for (int i = 0; i < 10; i++) {

	    Thread t = new Thread() {
		@Override
		public void run() {
		    try (Jedis jedis = pool.getResource()) {
			// Perform Redis operations
			System.out.println("Waiting on test");
			jedis.blpop(5, "test");
		    } catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		    }
		}
	    };
	    t.start();

	}
	
	pool.close();

	// jedis.watch("list");
	//
	// Transaction t = jedis.multi();
	// t.rpush("list", "d");
	// List<Object> ret = t.exec();
	// if (ret == null) {
	// System.out.println("NULL");
	// } else {
	// System.out.println(ret.size());
	// for (Object object : ret) {
	// System.out.println(object);
	// }
	// }
	//
	// jedis.close();
	// tool.close();
    }

}
