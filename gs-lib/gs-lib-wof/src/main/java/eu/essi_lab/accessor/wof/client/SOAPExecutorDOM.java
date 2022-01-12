package eu.essi_lab.accessor.wof.client;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.slf4j.Logger;
import org.w3c.dom.Node;

import eu.essi_lab.lib.net.utils.HttpRequestExecutor;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLNodeReader;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
class SOAPExecutorDOM {

    private static final String CUAHSI_HIS_CLIENT_ERROR = "CUAHSI_HIS_CLIENT_ERROR";

    private String action;
    private byte[] input;
    private String resultPath;

    private transient Logger logger = GSLoggerFactory.getLogger(SOAPExecutorDOM.class);

    private String endpoint;

    public SOAPExecutorDOM(String endpoint) {
	this.endpoint = endpoint;
    }

    public XMLDocumentReader execute() throws GSException {
	HttpPost post = new HttpPost(endpoint.trim());
	post.addHeader("SOAPAction", action);
	post.addHeader("Content-Type", "text/xml;charset=UTF-8");

	ByteArrayEntity inputEntity = new ByteArrayEntity(input);
	inputEntity.setChunked(false);
	post.setEntity(inputEntity);

	logger.info("Sending SOAP Request (" + action + ") to: " + endpoint);

	try {

	    HttpResponse response = new HttpRequestExecutor().execute(post);

	    InputStream output = response.getEntity().getContent();

	    File tmpFile = File.createTempFile("SOAPExecutorDOM", ".xml");
	    logger.info("Downloading document to : " + tmpFile.getAbsolutePath());
	    tmpFile.deleteOnExit();
	    FileOutputStream fos = new FileOutputStream(tmpFile);
	    IOUtils.copy(output, fos);
	    if(output != null)
	    	output.close();
	    logger.info("Downloaded document. Size: " + tmpFile.length() + " bytes");

	    XMLDocumentReader xdoc = new XMLDocumentReader(tmpFile);

	    logger.info("XML response parsed, removing temporary file: " + tmpFile.getAbsolutePath());
	    if(fos != null)
	    	fos.close();
	    tmpFile.delete();

	    Node[] nodes = xdoc.evaluateNodes(resultPath);

	    if (nodes == null || nodes.length == 0) {
		GSException gse = new GSException();

		ErrorInfo ei = new ErrorInfo();
		ei.setContextId(this.getClass().getName());
		ei.setErrorDescription("Remote server fault: ");
		ei.setErrorId(CUAHSIHISServerClient.REMOTE_SERVER_ERROR);
		ei.setErrorType(ErrorInfo.ERRORTYPE_CLIENT);
		ei.setErrorCorrection("Provide a correct service endpoint");
		ei.setSeverity(ErrorInfo.SEVERITY_ERROR);

		String faultMessage = xdoc.evaluateString("//*:faultstring");
		if (faultMessage != null && !faultMessage.equals("")) {
		    ei.setErrorDescription("Remote server fault: " + faultMessage);
		}

		gse.addInfo(ei);

		throw gse;

	    } else {

		XMLNodeReader children = new XMLNodeReader(nodes[0]);
		// this is to check if we are in the case of the getSites or getSitesObject
		if (children.evaluateNodes("*[1]").length > 0) {		    
		    return new XMLDocumentReader(children.asStream());
		} else {

		    String gsr = xdoc.evaluateString(resultPath);

		    // this unescape is not needed, as it is done by the evaluateString method above!
		    // gsr = gsr.replace("gt;", ">").replace("lt;", ">");

		    XMLDocumentReader reader = new XMLDocumentReader(gsr);
		    // System.out.println(reader.asString());
		    return reader;
		}
	    }
	} catch (GSException e) {

	    throw e;

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

    public void setResultPath(String path) {
	this.resultPath = path;
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
