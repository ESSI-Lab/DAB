package eu.essi_lab.profiler.wof.access;

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
import java.io.FileOutputStream;
import java.util.Optional;

import org.apache.commons.io.IOUtils;

import eu.essi_lab.access.wml.WML2DataDownloader;
import eu.essi_lab.jaxb.wml._2_0.CollectionType;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLDocumentWriter;
import eu.essi_lab.messages.AccessMessage;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.profiler.wof.WOFRequest;
import eu.essi_lab.profiler.wof.WOFRequest.Parameter;
import eu.essi_lab.wml._2.JAXBWML2;

/**
 * @author boldrini
 */
public class GetValuesObjectResultSetMapper extends GetValuesResultSetMapper {

    @Override
    public DataObject map(AccessMessage message, DataObject resource) throws GSException {

	WOFRequest getValuesRequest = getValuesRequest(message.getWebRequest());
	String format = getValuesRequest.getParameterValue(Parameter.FORMAT);
	// WATERML 2.0 switch (e.g. used by WHOS-Arctic portal)
	if (format != null && format.contains("2")) {
	    Optional<GSResource> optionalResource = resource.getResource();
	    if (optionalResource.isPresent()) {
		try {
		    CollectionType collection = JAXBWML2.getInstance().unmarshalCollection(resource.getFile());
		    WML2DataDownloader.augmentCollection(collection, optionalResource.get());
		    File tmp = File.createTempFile(getClass().getSimpleName(), ".xml");
		    tmp.deleteOnExit();
		    JAXBWML2.getInstance().marshal(collection, tmp);
		    resource.getFile().delete();
		    resource.setFile(tmp);
		} catch (Exception e) {
		    e.printStackTrace();
		}

	    }
	    return resource;

	}
	
	// NetCDF switch (e.g. used by WHOS-Arctic portal)
	if (format != null && (format.toLowerCase().contains("netcdf")|| format.toLowerCase().contains("csv"))) {
//	    Optional<GSResource> optionalResource = resource.getResource();
//	    if (optionalResource.isPresent()) {
//		try {
//		    CollectionType collection = JAXBWML2.getInstance().unmarshalCollection(resource.getFile());
//		    WML2DataDownloader.augmentCollection(collection, optionalResource.get());
//		    File tmp = File.createTempFile(getClass().getSimpleName(), ".xml");
//		    tmp.deleteOnExit();
//		    JAXBWML2.getInstance().marshal(collection, tmp);
//		    resource.getFile().delete();
//		    resource.setFile(tmp);
//		} catch (Exception e) {
//		    e.printStackTrace();
//		}
//
//	    }
	    return resource;

	}

	try {

	    GSLoggerFactory.getLogger(getClass()).info("Get base response");
	    XMLDocumentReader reader = getBaseResponse(message.getWebRequest(), resource);
	    GSLoggerFactory.getLogger(getClass()).info("Got base response");
	    XMLDocumentWriter writer = new XMLDocumentWriter(reader);
	    
	    writer.removePrefixes();
	    
	    XMLDocumentReader reader2 = getResponseTemplate();
	    
	    XMLDocumentWriter writer2 = new XMLDocumentWriter(reader2);
	    
	    writer2.addNode("//*:TimeSeriesResponse[1]", reader.evaluateNode("//*:timeSeriesResponse[1]"));
	    
	    File tmpFile = File.createTempFile("GetValuesObjectResultSetMapper", ".xml");
	    
	    tmpFile.deleteOnExit();

	    FileOutputStream fos = new FileOutputStream(tmpFile);
	    
	    IOUtils.copy(reader2.asStream(), fos);
	    GSLoggerFactory.getLogger(getClass()).info("Finalizing");
	    fos.close();
	    if (reader2.asStream() != null)
		reader2.asStream().close();

	    // TODO: question: is it correct to return the input DataObject or should we return a new DataObject ??
	    // delete old file
	    resource.getFile().delete();
	    // set new file
	    resource.setFile(tmpFile);

	    // /////////////////////////////////////////
	    // FileInputStream fis = new FileInputStream(new File("/tmp/data.xml"));
	    // File target = new File("/tmp/datacopy.xml");
	    // FileOutputStream fos2 = new FileOutputStream(target);
	    // IOUtils.copy(fis, fos2);
	    // fis.close();
	    // fos2.close();
	    // resource.setFile(target);
	    // /////////////////////////////////////////

	} catch (Exception e) {
	    e.printStackTrace();
	}
	GSLoggerFactory.getLogger(getClass()).info("Finalized");
	return resource;
    }

    public XMLDocumentReader getResponseTemplate() {
	XMLDocumentReader ret = null;
	try {
	    ret = new XMLDocumentReader("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + //
		    "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" " + //
		    "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" + //
		    "   <soap:Body>\n" + //
		    "     <TimeSeriesResponse xmlns=\"http://www.cuahsi.org/waterML/1.1/\" >\n" + //
		    "     </TimeSeriesResponse>\n" + //
		    "   </soap:Body>\n" + //
		    "</soap:Envelope>\n");
	} catch (Exception e) {
	    // This should never happen, as this method is tested by JUnit!
	    e.printStackTrace();
	}
	return ret;
    }

    public String getMethodName() {
	return "GetValuesObject";
    }

    public WOFRequest getValuesRequest(WebRequest webRequest) {
	return new GetValuesObjectRequest(webRequest);
    }

}
