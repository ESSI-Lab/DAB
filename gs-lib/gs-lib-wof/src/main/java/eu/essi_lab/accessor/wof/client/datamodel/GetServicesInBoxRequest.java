package eu.essi_lab.accessor.wof.client.datamodel;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.InputStream;

import javax.xml.xpath.XPathExpressionException;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLDocumentWriter;

public class GetServicesInBoxRequest {

    protected XMLDocumentReader reader;
    protected XMLDocumentWriter writer;

    public GetServicesInBoxRequest() {
	this(GetServicesInBoxRequest.class.getClassLoader().getResourceAsStream("cuahsi/central/GetServicesInBoxRequest.xml"));
    }

    public GetServicesInBoxRequest(InputStream stream) {
	try {
	    this.reader = new XMLDocumentReader(stream);
	    this.writer = new XMLDocumentWriter(reader);
	} catch (Exception e) {
	    // this should not happen, as this constructor should be used only by "controlled classes" in this package
	    // (protected visibility)
	    
	    GSLoggerFactory.getLogger(GetServicesInBoxRequest.class).error("Error initializing GetServicesInBoxRequest", e);
	}

    }

    public void setXmin(String xmin) {
	try {
	    writer.setText("//*:xmin", xmin);
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	}

    }

    public String getXmin() {
	try {
	    return reader.evaluateString("//*:xmin");
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	}
	return null;
    }

    public void setXmax(String xmax) {
	try {
	    writer.setText("//*:xmax", xmax);
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	}

    }

    public String getXmax() {
	try {
	    return reader.evaluateString("//*:xmax");
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	}
	return null;
    }

    public void setYmin(String ymin) {
	try {
	    writer.setText("//*:ymin", ymin);
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	}

    }

    public String getYmin() {
	try {
	    return reader.evaluateString("//*:ymin");
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	}
	return null;
    }

    public void setYmax(String ymax) {
	try {
	    writer.setText("//*:ymax", ymax);
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	}

    }

    public String getYmax() {
	try {
	    return reader.evaluateString("//*:ymax");
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	}
	return null;
    }

    public XMLDocumentReader getReader() {
	return reader;
    }
}
