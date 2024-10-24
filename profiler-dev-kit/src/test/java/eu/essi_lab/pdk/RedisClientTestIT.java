package eu.essi_lab.pdk;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisClientTestIT {

   
    public void test() {

        JedisPool pool = new JedisPool("localhost", 6379);
        
        try (Jedis jedis = pool.getResource()) {
            
            
            // Store & Retrieve a simple string
            jedis.set("foo", "bar");
            System.out.println(jedis.get("foo")); // prints bar
            
            // Store & Retrieve a HashMap
            Map<String, String> hash = new HashMap<>();;
            hash.put("name", "John");
            hash.put("surname", "Smith");
            hash.put("company", "Redis");
            hash.put("age", "29");
            jedis.hset("user-session:123", hash);
            System.out.println(jedis.hgetAll("user-session:123"));
            // Prints: {name=John, surname=Smith, company=Redis, age=29}
            String pong = jedis.ping();
            assertTrue(pong.equals("PONG"));
            
            jedis.lpush("test", "v1");
            
            List<String> result = jedis.blpop(3, "test");
            for (String res : result) {
		System.out.println(res);
	    }
    	
        }
        
        pool.close();
        
    }

 

}
