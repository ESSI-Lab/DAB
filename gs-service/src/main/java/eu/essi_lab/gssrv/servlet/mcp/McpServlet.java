package eu.essi_lab.gssrv.servlet.mcp;

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

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import io.modelcontextprotocol.json.jackson2.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpStatelessSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletStatelessServerTransport;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import static jakarta.servlet.http.HttpServletResponse.SC_OK;

/**
 * Draft MCP server exposing stateless streamable HTTP via the MCP Java SDK.
 * Registers {@link HydroOntologyMcpSpecifications HIS-Central hydro ontology} resources
 * (JSON) backed by {@link eu.essi_lab.lib.skos.SKOSClient} against HIS-Central SPARQL.
 * <p>
 * Stateless transport: each POST is handled independently (no {@code Mcp-Session-Id}),
 * so the endpoint can run behind a load balancer without session affinity.
 * <p>
 * Plain GET requests (e.g. from a browser) are not MCP traffic and receive a short
 * plain-text notice instead of a JSON-RPC error.
 *
 * @author ESSI-Lab
 */
public class McpServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /** Must match suffix that clients use; {@link jakarta.servlet.http.HttpServletRequest#getRequestURI} must end with this. */
    public static final String MCP_ENDPOINT = "/mcp";

    private static final String SERVER_NAME = "DAB MCP";
    private static final String SERVER_VERSION = "0.1.0-draft";

    private transient HttpServletStatelessServerTransport transport;
    private transient McpStatelessSyncServer mcpServer;

    @Override
    public void init() throws ServletException {

	try {

	    ObjectMapper objectMapper = new ObjectMapper();
	    JacksonMcpJsonMapper jsonMapper = new JacksonMcpJsonMapper(objectMapper);

	    transport = HttpServletStatelessServerTransport.builder() //
		    .jsonMapper(jsonMapper) //
		    .messageEndpoint(MCP_ENDPOINT) //
		    .build();

	    /*
	     * Do not pass a custom ServerCapabilities object here: capabilities are
	     * auto-derived from registered features when serverCapabilities is null.
	     */
	    mcpServer = McpServer.sync(transport) //
		    .serverInfo(SERVER_NAME, SERVER_VERSION) //
		    .jsonMapper(jsonMapper) //
		    .resources(//
			    HydroOntologyMcpSpecifications.hydroOntologyMetadataResource(objectMapper)) //
		    .resourceTemplates(//
			    HydroOntologyMcpSpecifications.hydroOntologyTermsResourceTemplate(objectMapper), //
			    ViewObservedPropertiesMcpSpecifications.viewObservedPropertiesResourceTemplate(objectMapper)) //
		    .tools(OmApiMcpTools.toolSpecifications(jsonMapper, objectMapper)) //
		    .build();

	    GSLoggerFactory.getLogger(getClass()).info("{} {} started (stateless MCP servlet, endpoint suffix: {})",
		    SERVER_NAME, SERVER_VERSION, MCP_ENDPOINT);

	} catch (RuntimeException e) {

	    GSLoggerFactory.getLogger(getClass()).error("Failed to initialize draft MCP servlet", e);
	    throw new ServletException(e);
	}
    }

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

	if (isBrowserGetProbe(request)) {
	    writeEndpointInfo(response);
	    return;
	}

	transport.service(request, response);
    }

    /**
     * Returns {@code true} for plain GET requests that are not valid MCP traffic.
     *
     * @param request the incoming HTTP servlet request
     * @return {@code true} when the request method is GET
     */
    private static boolean isBrowserGetProbe(HttpServletRequest request) {

	return "GET".equalsIgnoreCase(request.getMethod());
    }

    private static void writeEndpointInfo(HttpServletResponse response) throws IOException {

	response.setStatus(SC_OK);
	response.setCharacterEncoding(StandardCharsets.UTF_8.name());
	response.setContentType("text/plain;charset=UTF-8");
	response.setHeader("Cache-Control", "no-store");

	String body = "DAB MCP — stateless streamable HTTP endpoint (Model Context Protocol).\r\n\r\n"
		+ "Opening this URL in a browser is not a supported use case. MCP clients must send "
		+ "JSON-RPC POST requests to this path (no session id required).\r\n\r\n"
		+ "Specification: https://modelcontextprotocol.io/\r\n";

	try (PrintWriter out = response.getWriter()) {
	    out.print(body);
	}
    }

    @Override
    public void destroy() {

	if (mcpServer != null) {

	    try {

		mcpServer.close();

	    } catch (RuntimeException e) {

		GSLoggerFactory.getLogger(getClass()).warn("Error closing MCP stateless server", e);
	    }

	    mcpServer = null;
	}

	if (transport != null) {

	    transport.destroy();
	    transport = null;
	}

	super.destroy();

	GSLoggerFactory.getLogger(getClass()).info("Draft MCP servlet destroyed");
    }
}
