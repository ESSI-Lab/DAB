package eu.essi_lab.test.authorization.xacmlauthorizer;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.junit.Before;

import eu.essi_lab.authorization.xacml.XACMLAuthorizer;
import eu.essi_lab.lib.utils.Chronometer;
import eu.essi_lab.lib.utils.Chronometer.TimeFormat;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.AccessMessage;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.RequestMessage;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.bond.View.ViewVisibility;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public abstract class AbstractXACMLAuthorizerTest {

    protected XACMLAuthorizer authorizer;
    protected Chronometer chronometer;

    @Before
    public void init() throws Exception {

	chronometer = new Chronometer(TimeFormat.SEC_MLS);
	chronometer.start();

	authorizer = new XACMLAuthorizer();
    }

    /**
     * @param message
     * @return
     * @throws GSException
     */
    protected boolean isAuthorized(RequestMessage message) throws GSException {

	boolean authorized = authorizer.isAuthorized(message);

	GSLoggerFactory.getLogger(getClass()).debug("El time: " + chronometer.formatElapsedTime());

	return authorized;
    }

    /**
     * @param message
     * @return
     * @throws GSException
     */
    protected boolean isAuthorized(AccessMessage message) throws GSException {

	boolean authorized = authorizer.isAuthorized(message);

	GSLoggerFactory.getLogger(getClass()).debug("El time: " + chronometer.formatElapsedTime());

	return authorized;
    }

    /**
     * @param message
     * @param role
     */
    protected void setUser(RequestMessage message, String role) {

	setUser(message, role, null);
    }

    /**
     * @param message
     * @param role
     * @param identifier
     */
    protected void setUser(RequestMessage message, String role, String identifier) {

	GSUser user = new GSUser();

	user.setRole(role);

	if (identifier != null) {
	    user.setIdentifier(identifier);
	}

	message.setCurrentUser(user);
    }

    /**
     * @param message
     * @param path
     */
    protected void setWebRequest(RequestMessage message, String path) {

	setWebRequest(message, path, null, null, null, null);
    }

    /**
     * @param message
     * @param path
     * @param origin
     */
    protected void setWebRequest(RequestMessage message, String path, String origin) {

	setWebRequest(message, path, null, null, null, origin);
    }

    /**
     * @param message
     * @param path
     * @param remoteAddress
     * @param clientId
     */
    protected void setWebRequest(RequestMessage message, //
	    String path, //
	    String remoteAddress, //
	    String clientId) {

	setWebRequest(message, path, remoteAddress, clientId, null, null);
    }

    /**
     * @param message
     * @param path
     * @param remoteAddress
     * @param clientId
     * @param xforHeader
     */
    protected void setWebRequest(RequestMessage message, //
	    String path, //
	    String remoteAddress, //
	    String clientId, //
	    String xforHeader, //
	    String originHeader) {

	WebRequest webRequest = message.getWebRequest();

	if (webRequest == null) {

	    if (Objects.isNull(remoteAddress) && clientId == null && //
		    xforHeader == null && //
		    originHeader == null //

	    ) {

		webRequest = new WebRequest();

	    } else {

		HashMap<String, String> headers = new HashMap<>();
		headers.put(WebRequest.CLIENT_IDENTIFIER_HEADER, clientId);
		headers.put(WebRequest.X_FORWARDED_FOR_HEADER, xforHeader);
		headers.put(WebRequest.ORIGIN_HEADER, originHeader);

		webRequest = WebRequest.createGET("http://localhost", remoteAddress, headers);
	    }
	}

	webRequest.setProfilerPath(path);
	message.setWebRequest(webRequest);
    }

    /**
     * @param message
     * @param id
     */
    protected void setView(RequestMessage message, String id) {

	setView(message, id, null, null, null);
    }

    /**
     * @param message
     * @param id
     * @param creator
     */
    protected void setView(RequestMessage message, String id, String creator) {

	setView(message, id, creator, null, null);
    }

    /**
     * @param message
     * @param id
     * @param creator
     * @param viewVisbility
     */
    protected void setView(RequestMessage message, String id, String creator, ViewVisibility viewVisibility, String viewOwner) {

	if (id == null) {
	    id = "viewId";
	}

	View view = new View(id);
	
	if (creator != null) {
	    
	    view.setCreator(creator);
	}

	if (viewVisibility != null) {

	    view.setVisibility(viewVisibility);
	}

	if (viewOwner != null) {

	    view.setOwner(viewOwner);
	}

	message.setView(view);
    }

    /**
     * @param message
     * @param sources
     */
    protected void setSources(DiscoveryMessage message, List<String> sources) {

	message.setSources(//
		sources.stream().map(s -> {
		    GSSource gsSource = new GSSource();
		    gsSource.setUniqueIdentifier(s);
		    return gsSource;
		}).collect(Collectors.toList()));
    }

    /**
     * @param message
     * @param offset
     */
    protected void setOffset(RequestMessage message, int offset) {

	Page page = new Page();
	page.setSize(10);
	page.setStart(offset);

	message.setPage(page);
    }

    /**
     * @param message
     * @param count
     */
    protected void setDownloadCount(AccessMessage message, int count) {

	message.setDownloadCount(count);
    }
}
