/**
 * 
 */
package eu.essi_lab.authorization.xacml;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.ow2.authzforce.core.pdp.api.CloseablePdpEngine;

import eu.essi_lab.authorization.BasicRole;
import eu.essi_lab.authorization.MessageAuthorizer;
import eu.essi_lab.authorization.PolicySetWrapper.Action;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.AccessMessage;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.RequestMessage;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.model.exceptions.GSException;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;

/**
 * @author Fabrizio
 */
public class XACMLAuthorizer implements Closeable, MessageAuthorizer<RequestMessage> {

    private PdpEngineWrapper wrapper;
    private StringBuilder logBuilder;

    /**
     * 
     */
    public XACMLAuthorizer() {
    }

    @Override
    public boolean isAuthorized(RequestMessage message) throws GSException {

	logBuilder = new StringBuilder();

	// GSLoggerFactory.getLogger(getClass()).debug("Authorization check STARTED");

	//
	// developer machine
	//
	if (isLocalHost(message)) {

	    GSLoggerFactory.getLogger(getClass()).debug("Dev. machine authorized");

	    return true;
	}

	Optional<GSUser> requestUser = message.getCurrentUser();

	String role = null;
	String identifier = null;

	if (Objects.nonNull(requestUser) && requestUser.isPresent()) {

	    role = requestUser.get().getRole();
	    identifier = requestUser.get().getIdentifier();

	} else {

	    GSLoggerFactory.getLogger(getClass()).warn("Missing user");

	    role = BasicRole.ANONYMOUS.getRole();
	}

	logBuilder.append("\n- Identifier: " + identifier);

	logBuilder.append("\n- Role: " + role);

	if (message instanceof DiscoveryMessage) {
	    mapMessage(role, (DiscoveryMessage) message);
	} else {
	    mapMessage(role, (AccessMessage) message);
	}

	// GSLoggerFactory.getLogger(getClass()).debug("Evaluating request STARTED");

	boolean result = evaluate();

	logBuilder.append("\n- Evaluation result: " + (!result ? "denied" : "authorized"));

	GSLoggerFactory.getLogger(getClass()).info(logBuilder.toString());

	// GSLoggerFactory.getLogger(getClass()).debug("Evaluating request ENDED");

	// GSLoggerFactory.getLogger(getClass()).debug("Authorization check ENDED");

	return result;
    }

    @Override
    public void close() throws IOException {

	// GSLoggerFactory.getLogger(getClass()).debug("Closing STARTED");

	wrapper.close();

	// GSLoggerFactory.getLogger(getClass()).debug("Closing ENDED");
    }

    /**
     * @param pdp
     * @throws Exception
     */
    public void setPdpEngine(CloseablePdpEngine pdp) throws Exception {

	this.wrapper = new PdpEngineWrapper(pdp);
    }

    /**
     * @param role
     * @param message
     */
    private void mapMessage_(String role, RequestMessage message) {

	wrapper.reset();

	if (message instanceof DiscoveryMessage) {

	    wrapper.setAction(Action.DISCOVERY.getId());
	    logBuilder.append("\n- Action: " + Action.DISCOVERY.getId());

	} else {
	    logBuilder.append("\n- Action: " + Action.ACCESS.getId());
	    wrapper.setAction(Action.ACCESS.getId());
	}

	wrapper.setUserRole(role);

	Page page = message.getPage();
	if (page != null) {

	    int offset = message.getPage().getStart();
	    wrapper.setOffset(offset);

	    logBuilder.append("\n- Offset: " + offset);

	    int maxRecords = message.getPage().getSize();
	    wrapper.setMaxRecords(maxRecords);

	    logBuilder.append("\n- Max records: " + maxRecords);
	}

	Optional<View> view = message.getView();

	if (view.isPresent()) {

	    wrapper.setViewIdentifier(view.get().getId());

	    logBuilder.append("\n- View id: " + view.get().getId());

	    String creator = view.get().getCreator();
	    if (creator != null) {
		wrapper.setViewCreator(creator);

		logBuilder.append("\n- View creator: " + creator);
	    }
	}

	Optional<String> originHeader = message.getWebRequest().readOriginHeader();
	if (originHeader.isPresent()) {

	    logBuilder.append("\n- Origin header: " + originHeader.get());
	    wrapper.setOriginHeader(originHeader.get());
	}

	ArrayList<String> ips = new ArrayList<String>();

	String remoteIp = message.getWebRequest().readRemoteHostHeader().orElse(null);
	List<String> xforHeaders = message.getWebRequest().readXForwardedForHeaders();

	if (Objects.nonNull(remoteIp) && !remoteIp.isEmpty()) {
	    ips.add(remoteIp);
	    logBuilder.append("\n- Remote IP: " + remoteIp);
	}

	if (!xforHeaders.isEmpty()) {
	    ips.addAll(xforHeaders);

	    xforHeaders.forEach(ip -> logBuilder.append("\n- x-forwarded-for-header: " + ip));
	}

	if (!ips.isEmpty()) {

	    wrapper.setIPs(ips.toArray(new String[] {}));
	}

	String path = message.getWebRequest().getProfilerPath();
	if (path != null) {

	    logBuilder.append("\n- Path: " + path);

	    wrapper.setPath(path);
	}

	String clientId = message.getWebRequest().readClientIdentifierHeader().orElse(null);
	if (clientId != null) {

	    logBuilder.append("\n- Client Id: " + clientId);

	    wrapper.setClientId(clientId);
	}
    }

    /**
     * @param role
     * @param message
     * @return
     */
    private void mapMessage(String role, DiscoveryMessage message) {

	mapMessage_(role, message);

	/////

	String[] ids = message.//
		getSources().//
		stream().//
		map(s -> s.getUniqueIdentifier()).//
		collect(Collectors.toList()).//
		toArray(new String[] {});

	if (ids.length > 0) {

	    List<String> toPrint = new ArrayList<>(Arrays.asList(ids));

	    if (toPrint.size() > 3) {

		toPrint = toPrint.subList(0, 3);
		toPrint.add("...");
	    }

	    logBuilder.append("\n- Sources (" + ids.length + "): " + toPrint);

	    wrapper.setSources(ids);
	}
    }

    /**
     * @param role
     * @param message
     * @return
     */
    private void mapMessage(String role, AccessMessage message) {

	mapMessage_(role, message);

	//////

	Optional<Integer> count = message.getDownloadCount();
	if (count.isPresent()) {

	    GSLoggerFactory.getLogger(getClass()).info("Download count: " + count.get());

	    wrapper.setDownloadCount(count.get());
	}
    }

    /**
     * @param message
     * @return
     */
    private boolean isLocalHost(RequestMessage message) {

	return message.getRequestAbsolutePath() != null && message.getRequestAbsolutePath().startsWith("http://localhost");
    }

    /**
     * @return
     */
    private boolean evaluate() {

	DecisionType decision = wrapper.evaluate();

	return decision == DecisionType.PERMIT;
    }
}
