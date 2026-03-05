package eu.essi_lab.gssrv.starter;

import eu.essi_lab.messages.*;
import org.eclipse.jetty.ee11.annotations.*;
import org.eclipse.jetty.ee11.webapp.*;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.resource.*;


import java.nio.file.*;

/***
 *
 * @author Fabrizio
 *
 */
public class JettyLauncher {

    private static final String	CONTEXT_PATH = "/gs-service";
    private static final int MAX_REQUEST_HEADER_SIZE = 16384;

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

	Thread.currentThread().setName("JettyLauncher");

	Server server = new Server();

	HttpConfiguration httpConfig = new HttpConfiguration();
	httpConfig.setRequestHeaderSize(MAX_REQUEST_HEADER_SIZE);

	ServerConnector connector =
		new ServerConnector(server, new HttpConnectionFactory(httpConfig));
	connector.setPort(JVMOption.getIntValue(JVMOption.JETTY_LAUNCHER_PORT).get());
	server.addConnector(connector);

	WebAppContext webapp = new WebAppContext();

	webapp.setExtraClasspath(
		Path.of("target/classes")
			.toAbsolutePath()
			.toString()
	);

	webapp.setContextPath(CONTEXT_PATH);
	webapp.setWar("src/main/webapp");

	webapp.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",
		".*/spring-[^/]*\\.jar$|.*vaadin-[^/]*\\.jar$|.*flow-[^/]*\\.jar$");

	webapp.setAttribute("org.eclipse.jetty.server.webapp.WebInfIncludeJarPattern",
		".*/spring-[^/]*\\.jar$|.*vaadin-[^/]*\\.jar$|.*flow-[^/]*\\.jar$");

	server.setHandler(webapp);

	server.start();
	server.join();
    }
}
