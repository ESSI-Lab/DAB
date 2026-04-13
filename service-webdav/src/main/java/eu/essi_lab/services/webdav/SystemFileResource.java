package eu.essi_lab.services.webdav;

import eu.essi_lab.lib.utils.*;
import io.milton.http.*;
import io.milton.resource.*;
import org.apache.commons.io.*;

import java.io.*;
import java.util.*;

/**
 * @author Fabrizio
 */
public class SystemFileResource implements GetableResource, PropFindableResource {

    private final String name;

    /**
     * @param name
     */
    public SystemFileResource(String name) {

	this.name = name;
    }

    /**
     * @param path
     * @return
     */
    static boolean isSystemFile(String path) {

	return path.contains("desktop.ini") || //
		path.contains("favicon.ico") || //
		path.contains("Thumbs.db") || //
		path.contains("folder.gif") || //
		path.contains("folder.jpg");//
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) {

	GSLoggerFactory.getLogger(getClass()).debug("Sending content of [{}] STARTED", this);

	try {

	    InputStream binary = IOStreamUtils.asStream("");

	    IOUtils.copy(binary, out);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	GSLoggerFactory.getLogger(getClass()).debug("Sending content of [{}] ENDED", this);
    }

    @Override
    public String getUniqueId() {

	return name;
    }

    @Override
    public String getName() {

	return name;
    }

    @Override
    public String getContentType(String accepts) {

	return "application/octet-stream";
    }

    @Override
    public Object authenticate(String user, String password) {

	return "ok";
    }

    @Override
    public boolean authorise(Request request, Request.Method method, Auth auth) {

	return true;
    }

    @Override
    public Long getMaxAgeSeconds(Auth auth) {

	return null;
    }

    @Override
    public Long getContentLength() {

	return null;
    }

    @Override
    public String getRealm() {

	return "";
    }

    @Override
    public Date getModifiedDate() {

	return null;
    }

    @Override
    public String checkRedirect(Request request) {

	return "";
    }

    /**
     * @return
     */
    @Override
    public Date getCreateDate() {

	return new Date();
    }
}
