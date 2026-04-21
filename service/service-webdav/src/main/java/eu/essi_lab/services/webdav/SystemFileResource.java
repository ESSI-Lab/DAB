package eu.essi_lab.services.webdav;

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
