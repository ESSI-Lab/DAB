<%@page import="eu.essi_lab.cfga.gs.ConfigurationWrapper"%>
<%@page import="eu.essi_lab.cfga.gs.setting.sessioncoordinator.SessionCoordinatorSetting"%>
<%@page import="redis.clients.jedis.Jedis"%>
<%@page import="redis.clients.jedis.JedisPool"%>
<%@page import="redis.clients.jedis.JedisPoolConfig"%>
<%@page import="java.util.List"%>
<html>
<head>
<title>Session coordinator</title>
<style>
body { font-family: sans-serif; margin: 1em; }
h1 { color: #333; }
.section { margin: 1em 0; padding: 1em; border: 1px solid #ccc; border-radius: 4px; }
.active { background: #e8f5e9; }
.queue { background: #e3f2fd; }
.error { color: #c62828; }
.inactive { color: #666; }
ul { list-style: none; padding-left: 0; }
li { padding: 0.25em 0; }
</style>
</head>
<body>
	<h1>Session coordinator</h1>
	<%
		try {
			SessionCoordinatorSetting setting = ConfigurationWrapper.getSessionCoordinatorSetting();
			if (!setting.isDistributedSessionCoordinator()) {
				out.println("<p class='inactive'>Distributed session coordinator is <b>disabled</b>. Using file-based single-node strategy.</p>");
				out.println("<p>Enable it in System &rarr; Session coordinator to see queue and active node.</p>");
			} else {
				String redisEndpoint = setting.getRedisEndpoint();
				if (redisEndpoint == null || redisEndpoint.isEmpty()) {
					redisEndpoint = "localhost:6379";
				}
				String[] parts = redisEndpoint.split(":");
				String host = parts.length > 0 ? parts[0].trim() : "localhost";
				int port = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 6379;

				String namespace = setting.getNamespace();
				String keyActive = namespace + ":active";
				String keyQueue = namespace + ":queue";
				String keyAlivePrefix = namespace + ":alive:";

				JedisPool pool = new JedisPool(new JedisPoolConfig(), host, port);
				try {
					try (Jedis jedis = pool.getResource()) {
						jedis.ping();
						String activeNodeId = jedis.get(keyActive);
						List<String> queue = jedis.lrange(keyQueue, 0, -1);

						out.println("<p>Redis: " + host + ":" + port + " &bull; Namespace: " + namespace + "</p>");

						out.println("<div class='section active'>");
						out.println("<h2>Active node</h2>");
						if (activeNodeId == null || activeNodeId.isEmpty()) {
							out.println("<p>None</p>");
						} else {
							Long ttl = jedis.ttl(keyAlivePrefix + activeNodeId);
							boolean alive = ttl != null && ttl > 0;
							out.println("<p><b>" + activeNodeId + "</b> " + (alive ? "(alive, TTL " + ttl + "s)" : "<span class='error'>DEAD</span>") + "</p>");
						}
						out.println("</div>");

						out.println("<div class='section queue'>");
						out.println("<h2>Queue</h2>");
						if (queue == null || queue.isEmpty()) {
							out.println("<p>Empty</p>");
						} else {
							out.println("<ul>");
							for (int i = 0; i < queue.size(); i++) {
								String nodeId = queue.get(i);
								Long ttl = jedis.ttl(keyAlivePrefix + nodeId);
								boolean alive = ttl != null && ttl > 0;
								String pos = (i == 0) ? " (first)" : "";
								out.println("<li>" + (i + 1) + ". " + nodeId + pos + " " + (alive ? "(alive, TTL " + ttl + "s)" : "<span class='error'>DEAD</span>") + "</li>");
							}
							out.println("</ul>");
						}
						out.println("</div>");
					}
				} catch (Exception redisEx) {
					out.println("<p class='inactive'>Redis session coordinator is configured but <b>unavailable</b> (" + host + ":" + port + ").</p>");
					out.println("<p>Clients fall back to file-based single-node mode. Check Redis is running and reachable.</p>");
					out.println("<p class='error'>" + redisEx.getMessage() + "</p>");
				} finally {
					pool.close();
				}
			}
		} catch (Exception e) {
			out.println("<p class='error'>Error: " + e.getMessage() + "</p>");
			e.printStackTrace(new java.io.PrintWriter(out));
		}
	%>
</body>
</html>
