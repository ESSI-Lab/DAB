package eu.essi_lab.pdk;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisTest2 {

    public static void main(String[] args) {
	RedisTool tool = new RedisTool("localhost", 6379);
	JedisPool pool = tool.getPool();
	for (int i = 0; i < 100; i++) {
	    Jedis jedis = pool.getResource();
	    jedis.rpush("test", "a");
	    jedis.close();
	}
	Jedis jedis = pool.getResource();
	jedis.del("test");
	jedis.close();
	// Jedis jedis = tool.getJedis();
	// jedis.rpush("list", "a","b","c");
	// jedis.close();
	tool.close();
    }

}
