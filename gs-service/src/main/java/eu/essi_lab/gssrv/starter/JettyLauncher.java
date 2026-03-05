package eu.essi_lab.gssrv.starter;

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
    private static final int JETTY_PORT = 9090;
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
	connector.setPort(JETTY_PORT);
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
