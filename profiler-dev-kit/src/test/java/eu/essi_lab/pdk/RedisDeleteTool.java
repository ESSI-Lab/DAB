package eu.essi_lab.pdk;

public class RedisDeleteTool {

    public static void main(String[] args) {
	RedisTool tool = new RedisTool("essi-lab.eu", 6379);
//	RedisTool tool = new RedisTool("localhost", 6379, 0);
//	tool.deleteDatabase();
//	tool.deleteKey("foo", "user-session:123");
	tool.deleteKeysWithPattern("{prod-access}*");
	tool.close();
    }

}
