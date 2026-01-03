package eu.essi_lab.accessor.cmr.cwic.harvested;

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

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;

import org.slf4j.Logger;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodNoBody;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author ilsanto
 */
public class CWICCMRCollectionAtomEntry {

    private String base;

    private Logger logger = GSLoggerFactory.getLogger(CWICCMRCollectionAtomEntry.class);
    private static final String CWICATOMECOLLECTIONENTRY_READ_IOEXC_ERR = "CWICATOMECOLLECTIONENTRY_READ_IOEXC_ERR";
    private static final String CWICATOMECOLLECTIONENTRY_READ_BAD_CODE_ERR = "CWICATOMECOLLECTIONENTRY_READ_BAD_CODE_ERR";

    public CWICCMRCollectionAtomEntry(String baseurl) {

	base = baseurl;

	if (!base.endsWith("?"))
	    base += "?";

    }

    public InputStream getCollectionAtom(String id) throws GSException {

	Downloader downloader = creatDownloader();

	String osdd = buildOSDD(id);
	try {

	    HttpResponse<InputStream> response = downloader.downloadResponse(//
		    HttpRequestUtils.build(MethodNoBody.GET, osdd));

	    int code = response.statusCode();

	    logger.trace("Retrieved http code {} from {}", code, osdd);

	    if (code - 200 != 0)

		throw GSException.createException(getClass(),
			"Failed validation of url " + osdd + " for cmr collection " + id + ": returned " + "http code is " + code, null,
			ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_WARNING, CWICATOMECOLLECTIONENTRY_READ_BAD_CODE_ERR);

	    return response.body();

	} catch (Exception e) {

	    logger.warn("IOException reading osdd ({}) for collection {}", osdd, id, e);

	    throw GSException.createException(getClass(), "Can't open url " + osdd + " for cmr collection " + id, null,
		    ErrorInfo.ERRORTYPE_SERVICE, ErrorInfo.SEVERITY_WARNING, CWICATOMECOLLECTIONENTRY_READ_IOEXC_ERR, e);

	}

    }

    public Downloader creatDownloader() {
	return new Downloader();
    }

    private String buildOSDD(String id) {
	return base + "uid=" + id;
    }

}
