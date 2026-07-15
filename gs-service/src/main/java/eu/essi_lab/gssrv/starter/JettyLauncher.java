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

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.*;
import org.eclipse.jetty.ee11.annotations.*;
import org.eclipse.jetty.ee11.webapp.*;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.*;
import org.eclipse.jetty.util.resource.*;
import org.eclipse.jetty.util.ssl.*;

import java.nio.file.*;
import java.time.*;
import java.time.temporal.*;
import java.util.*;
import java.util.concurrent.*;

/***
 *
 * @author Fabrizio
 *
 */
public class JettyLauncher {

    private static final String CONTEXT_PATH = "/gs-service";
    private static final int MAX_REQUEST_HEADER_SIZE = 16384;

    /**
     * Starts an embedded Jetty server with HTTP (and optional HTTPS) for local development.
     *
     * @param args command-line arguments (unused)
     * @throws Exception if the server fails to start or the HTTPS keystore is invalid
     */
    public static void main(String[] args) throws Exception {

	Thread.currentThread().setName("JettyLauncher");
	Server server = new Server();

	HttpConfiguration httpConfig = new HttpConfiguration();
	httpConfig.setRequestHeaderSize(MAX_REQUEST_HEADER_SIZE);

	JVMOption.getIntValue(JVMOption.JETTY_LAUNCHER_HTTPS_PORT).ifPresent(httpsPort -> {
	    httpConfig.setSecureScheme("https");
	    httpConfig.setSecurePort(httpsPort);
	    server.addConnector(createHttpsConnector(server, httpsPort));
	});

	ServerConnector connector = new ServerConnector(server, new HttpConnectionFactory(httpConfig));
	connector.setPort(JVMOption.getIntValue(JVMOption.JETTY_LAUNCHER_PORT).get());
	server.addConnector(connector);

	WebAppContext webapp = new WebAppContext();

	webapp.setContextPath(CONTEXT_PATH);
	Path devWebapp = Path.of("src/main/webapp");

	webapp.setWar(devWebapp.toString());
	webapp.setExtraClasspath(Path.of("target/classes").toAbsolutePath().toString());

	webapp.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",
		".*/spring-[^/]*\\.jar$|.*vaadin-[^/]*\\.jar$|.*flow-[^/]*\\.jar$");

	webapp.setAttribute("org.eclipse.jetty.server.webapp.WebInfIncludeJarPattern",
		".*/spring-[^/]*\\.jar$|.*vaadin-[^/]*\\.jar$|.*flow-[^/]*\\.jar$");

	CrossOriginHandler cors = new CrossOriginHandler();
	cors.setAllowedMethods(Set.of("GET", "POST", "HEAD", "PUT", "DELETE", "OPTIONS"));
	cors.setExposedHeaders(Set.of("Access-Control-Allow-Origin","Access-Control-Allow-Credentials", "Content-Type", "Authorization", "X-Requested-With"));
	cors.setAllowedOriginPatterns(Set.of("*"));
	cors.setAllowedHeaders(Set.of("*"));
	cors.setAllowCredentials(true);
	cors.setPreflightMaxAge(Duration.of(30, ChronoUnit.MINUTES));
	cors.setHandler(webapp);

	server.setHandler(cors);

	server.start();
	server.join();
    }

    private static ServerConnector createHttpsConnector(Server server, int httpsPort) {

	Path keystorePath = JVMOption.getStringValue(JVMOption.JETTY_LAUNCHER_HTTPS_KEYSTORE_PATH).//
		map(Path::of).//
		orElseThrow(() -> requiredHttpsOptionMissing(JVMOption.JETTY_LAUNCHER_HTTPS_KEYSTORE_PATH));

	if (!Files.isRegularFile(keystorePath)) {

	    throw new IllegalStateException("HTTPS keystore not found: " + keystorePath.toAbsolutePath());
	}

	String keystorePassword = JVMOption.getStringValue(JVMOption.JETTY_LAUNCHER_HTTPS_KEYSTORE_PASSWORD).//
		orElseThrow(() -> requiredHttpsOptionMissing(JVMOption.JETTY_LAUNCHER_HTTPS_KEYSTORE_PASSWORD));

	String keystoreType = JVMOption.getStringValue(JVMOption.JETTY_LAUNCHER_HTTPS_KEYSTORE_TYPE).//
		or(() -> JVMOption.JETTY_LAUNCHER_HTTPS_KEYSTORE_TYPE.getDefaultStringValue()).//
		orElse("PKCS12");

	String keyManagerPassword = JVMOption.getStringValue(JVMOption.JETTY_LAUNCHER_HTTPS_KEY_MANAGER_PASSWORD).//
		orElse(keystorePassword);

	SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
	sslContextFactory.setKeyStorePath(keystorePath.toAbsolutePath().toString());
	sslContextFactory.setKeyStorePassword(keystorePassword);
	sslContextFactory.setKeyStoreType(keystoreType);
	sslContextFactory.setKeyManagerPassword(keyManagerPassword);

	JVMOption.getStringValue(JVMOption.JETTY_LAUNCHER_HTTPS_KEY_ALIAS).ifPresent(sslContextFactory::setCertAlias);

	HttpConfiguration httpsConfig = new HttpConfiguration();
	httpsConfig.setRequestHeaderSize(MAX_REQUEST_HEADER_SIZE);
	httpsConfig.addCustomizer(new SecureRequestCustomizer());

	HttpConnectionFactory httpsConnectionFactory = new HttpConnectionFactory(httpsConfig);
	SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(sslContextFactory, httpsConnectionFactory.getProtocol());

	ServerConnector httpsConnector = new ServerConnector(server, sslConnectionFactory, httpsConnectionFactory);
	httpsConnector.setPort(httpsPort);

	GSLoggerFactory.getLogger(JettyLauncher.class).info("HTTPS connector enabled on port {} with keystore {}", httpsPort,
		keystorePath.toAbsolutePath());

	return httpsConnector;
    }

    private static IllegalStateException requiredHttpsOptionMissing(JVMOption option) {

	return new IllegalStateException(
		"JVM option '" + option.getOption() + "' is required when '" + JVMOption.JETTY_LAUNCHER_HTTPS_PORT.getOption()
			+ "' is set");
    }
}
