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
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.HttpHeaders;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import static jakarta.servlet.http.HttpServletResponse.SC_OK;

/**
 * Draft MCP server exposing streamable HTTP via the MCP Java SDK.
 * Registers {@link HydroOntologyMcpSpecifications HIS-Central hydro ontology} resources
 * (JSON) backed by {@link eu.essi_lab.lib.skos.SKOSClient} against HIS-Central SPARQL.
 * <p>
 * For this transport, plain GET requests (e.g. from a browser) are not MCP traffic:
 * SSE GET requires {@link HttpHeaders#ACCEPT Accept: text/event-stream} and header
 * {@link HttpHeaders#MCP_SESSION_ID Mcp-Session-Id} returned from an earlier POST. Those
 * requests are answered with a short plain-text notice instead of a JSON-RPC error.
 *
 * @author ESSI-Lab
 */
public class McpServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /** Must match suffix that clients use; {@link jakarta.servlet.http.HttpServletRequest#getRequestURI} must end with this. */
    public static final String MCP_ENDPOINT = "/mcp";

    private static final String SERVER_NAME = "DAB MCP";
    private static final String SERVER_VERSION = "0.1.0-draft";

    private transient HttpServletStreamableServerTransportProvider transport;
    private transient McpSyncServer mcpServer;

    @Override
    public void init() throws ServletException {

	try {

	    ObjectMapper objectMapper = new ObjectMapper();
	    JacksonMcpJsonMapper jsonMapper = new JacksonMcpJsonMapper(objectMapper);

	    transport = HttpServletStreamableServerTransportProvider.builder() //
		    .jsonMapper(jsonMapper) //
		    .mcpEndpoint(MCP_ENDPOINT) //
		    .build();

	    /*
	     * Do not pass a custom ServerCapabilities object here: McpServerFeatures.Sync
	     * auto-derives capabilities from registered features when serverCapabilities is
	     * null (see McpServerFeatures.Sync constructor). That sets a non-null
	     * ResourceCapabilities when the resources map is non-empty, which is required for
	     * McpAsyncServer to register JSON-RPC handlers for resources/list and
	     * resources/read.
	     */
	    mcpServer = McpServer.sync(transport) //
		    .serverInfo(SERVER_NAME, SERVER_VERSION) //
		    .jsonMapper(jsonMapper) //
		    .resources(//
			    HydroOntologyMcpSpecifications.hydroOntologyMetadataResource(objectMapper)) //
		    .resourceTemplates(//
			    HydroOntologyMcpSpecifications.hydroOntologyTermsResourceTemplate(objectMapper)) //
		    .build();

	    GSLoggerFactory.getLogger(getClass()).info("{} {} started (draft MCP servlet, MCP endpoint suffix: {})",
		    SERVER_NAME, SERVER_VERSION, MCP_ENDPOINT);

	} catch (RuntimeException e) {

	    GSLoggerFactory.getLogger(getClass()).error("Failed to initialize draft MCP servlet", e);
	    throw new ServletException(e);
	}
    }

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

	if (isNonSseGetProbe(request)) {
	    writeStreamableHttpInfo(response);
	    return;
	}

	transport.service(request, response);
    }

    /**
     * Returns true for GETs that the streamable transport would reject: GET is only for
     * opening an SSE stream on an existing session (see SDK {@code doGet}).
     */
    private static boolean isNonSseGetProbe(HttpServletRequest request) {

	if (!"GET".equalsIgnoreCase(request.getMethod())) {
	    return false;
	}

	String accept = request.getHeader(HttpHeaders.ACCEPT);
	String sessionId = request.getHeader(HttpHeaders.MCP_SESSION_ID);

	boolean wantsEventStream = accept != null && accept.contains(HttpServletStreamableServerTransportProvider.TEXT_EVENT_STREAM);
	boolean hasSession = sessionId != null && !sessionId.isBlank();

	return !(wantsEventStream && hasSession);
    }

    private static void writeStreamableHttpInfo(HttpServletResponse response) throws IOException {

	response.setStatus(SC_OK);
	response.setCharacterEncoding(StandardCharsets.UTF_8.name());
	response.setContentType("text/plain;charset=UTF-8");
	response.setHeader("Cache-Control", "no-store");

	String body = "DAB MCP — streamable HTTP endpoint (Model Context Protocol).\r\n\r\n"
		+ "Opening this URL in a browser is not a supported use case. MCP clients must:\r\n"
		+ "  1) POST JSON-RPC to this path to negotiate a session (the response sets header Mcp-Session-Id).\r\n"
		+ "  2) For the SSE stream, send GET with header Accept: text/event-stream and Mcp-Session-Id set to that value.\r\n\r\n"
		+ "Specification: https://modelcontextprotocol.io/\r\n";

	try (PrintWriter out = response.getWriter()) {
	    out.print(body);
	}
    }

    @Override
    public void destroy() {

	if (mcpServer != null) {

	    try {

		mcpServer.closeGracefully();

	    } catch (RuntimeException e) {

		GSLoggerFactory.getLogger(getClass()).warn("Error closing MCP sync server", e);
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
