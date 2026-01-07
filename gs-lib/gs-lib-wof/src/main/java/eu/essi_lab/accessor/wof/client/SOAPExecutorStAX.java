package eu.essi_lab.accessor.wof.client;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpHeaderUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodWithBody;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.stax.StAXDocumentIterator;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * Utility class to be used inside the Hydro Server client to make SOAP requests to HydroServers
 * 
 * @author boldrini
 */
class SOAPExecutorStAX {

    private static final String CUAHSI_HIS_CLIENT_ERROR = "CUAHSI_HIS_CLIENT_ERROR";

    private String action;
    private byte[] input;

    private Logger logger = GSLoggerFactory.getLogger(SOAPExecutorStAX.class);

    private String endpoint;

    private static Boolean FAKE = false;

    public SOAPExecutorStAX(String endpoint) {
	this.endpoint = endpoint;
    }

    private Boolean decodeEntities = false;

    public void setDecodeEntities(Boolean decodeEntities) {
	this.decodeEntities = decodeEntities;
    }

    public File execute() throws GSException {

	try {

	    HashMap<String, String> headers = new HashMap<String, String>();
	    headers.put("SOAPAction", action);
	    headers.put("Content-Type", "text/xml;charset=UTF-8");

	    HttpRequest postRequest = HttpRequestUtils.build(MethodWithBody.POST, //
		    endpoint.trim(), //
		    input, //
		    HttpHeaderUtils.build(headers));

	    logger.info("Sending SOAP Request (" + action + ") to: " + endpoint);

	    File tmpFile = null;

	    if (FAKE) {
		// to perform a fake download of the USGS get sites document (almost 400 MB)
		tmpFile = File.createTempFile("SOAPExecutorStAX", ".xml");
		logger.info("Downloading sites document to : " + tmpFile.getAbsolutePath());
		tmpFile.deleteOnExit();
		FileOutputStream fos = new FileOutputStream(tmpFile);
		IOUtils.copy(new FileInputStream(new File("/home/boldrini/bigSite.xml")), fos);
		logger.info("Downloaded sites document. Size: " + tmpFile.length() + " bytes");

	    } else {
		HttpResponse<InputStream> response = new Downloader().downloadResponse(postRequest);

		InputStream output = response.body();

		tmpFile = File.createTempFile("SOAPExecutorStAX", ".xml");
		logger.info("Downloading sites document to : " + tmpFile.getAbsolutePath());
		tmpFile.deleteOnExit();
		FileOutputStream fos = new FileOutputStream(tmpFile);
		if (decodeEntities) {
		    output = new CUAHSIDecoderInputStream(output);
		}
		IOUtils.copy(output, fos);
		logger.info("Downloaded sites document. Size: " + tmpFile.length() + " bytes");
		// here we check that the document is a good response and not a server fault

		FileInputStream fis = new FileInputStream(tmpFile);
		StAXDocumentIterator reader = new StAXDocumentIterator(fis, "siteInfo");
		if (!reader.hasNext()) {
		    fis.close();
		    throw GSException.createException(//
			    getClass(), //
			    "Server fault", //
			    null, //
			    ErrorInfo.ERRORTYPE_SERVICE, //
			    ErrorInfo.SEVERITY_ERROR, //
			    CUAHSI_HIS_CLIENT_ERROR //
		    );

		} else {
		    reader.close();
		    fis.close();
		}

	    }

	    return tmpFile;

	} catch (Exception e) {

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_CLIENT, //
		    ErrorInfo.SEVERITY_ERROR, //
		    CUAHSI_HIS_CLIENT_ERROR, //
		    e);
	}
    }

    public void setSOAPAction(String action) {
	this.action = action;

    }

    public void setBody(byte[] input) {
	this.input = input;
    }

    public void setBody(InputStream input) {
	try {
	    this.input = IOUtils.toByteArray(input);
	    input.close();
	} catch (IOException e) {
	    logger.error("Problem setting SOAP POST body");
	    e.printStackTrace();
	}

    }
}
