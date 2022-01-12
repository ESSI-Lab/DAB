package eu.essi_lab.accessor.wof.client.datamodel;

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

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

import eu.essi_lab.lib.xml.XMLNodeReader;
import eu.essi_lab.lib.xml.XMLNodeWriter;

public class Value {
    private XMLNodeReader reader;
    private XMLNodeWriter writer;

    public Value(Node node) {
	this.reader = new XMLNodeReader(node);
	this.writer = new XMLNodeWriter(reader);
    }

    public XMLNodeReader getReader() {
	return reader;
    }

    public String getCensorCode() {
	try {
	    return reader.evaluateString("@censorCode");
	} catch (XPathExpressionException e) {
	    return null;
	}
    }

    // public Date getDate() {
    //
    // }

    public String getDateTime() {
	try {
	    return reader.evaluateString("@dateTime");
	} catch (XPathExpressionException e) {
	    return null;
	}
    }
    
    public void setDateTime(String time) {
	try {
	    writer.setText("@dateTime", time);
	} catch (XPathExpressionException e) {
	   
	}
    }

    public String getTimeOffset() {
	try {
	    return reader.evaluateString("@timeOffset");
	} catch (XPathExpressionException e) {
	    return null;
	}
    }

    public String getDateTimeUTC() {
	try {
	    return reader.evaluateString("@dateTimeUTC");
	} catch (XPathExpressionException e) {
	    return null;
	}
    }

    public void setDateTimeUTC(String time) {
	try {
	    writer.setText("@dateTimeUTC", time);
	} catch (XPathExpressionException e) {
	   
	}
    }

    public String getMethodCode() {
	try {
	    return reader.evaluateString("@methodCode");
	} catch (XPathExpressionException e) {
	    return null;
	}
    }

    public String getSourceCode() {
	try {
	    return reader.evaluateString("@sourceCode");
	} catch (XPathExpressionException e) {
	    return null;
	}
    }

    public String getQualityControlLevelCode() {
	try {
	    return reader.evaluateString("@qualityControlLevelCode");
	} catch (XPathExpressionException e) {
	    return null;
	}
    }

    public String getValue() {
	try {
	    return reader.evaluateString(".");
	} catch (XPathExpressionException e) {
	    return null;
	}
    }
}
