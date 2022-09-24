/**
 * This file is part of SDI HydroServer Accessor. SDI HydroServer Accessor is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or any later version. SDI HydroServer Accessor is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU
 * General Public License along with SDI HydroServer Accessor. If not, see <http://www.gnu.org/licenses/>. Copyright (C)
 * 2009-2011 Flora research <info@floraresearch.eu>
 */

package eu.essi_lab.accessor.wof.client.datamodel;

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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLDocumentWriter;
import eu.essi_lab.model.exceptions.GSException;

/**
 * This class is a wrapper for a WaterML 1.1 TimeSeriesResponse document
 * 
 * @author boldrini
 */
public class TimeSeriesResponseDocument {

    private XMLDocumentReader reader;
    protected XMLDocumentWriter writer;

    public XMLDocumentReader getReader() {
	return reader;
    }

    public XMLDocumentWriter getWriter() {
	return writer;
    }

    public void setReader(XMLDocumentReader reader) {
	this.reader = reader;
    }

    public void setWriter(XMLDocumentWriter writer) {
	this.writer = writer;
    }

    public TimeSeriesResponseDocument(Document doc) {
	this.reader = new XMLDocumentReader(doc);
	this.writer = new XMLDocumentWriter(reader);
    }

    public TimeSeriesResponseDocument(InputStream stream) throws SAXException, IOException {
	this.reader = new XMLDocumentReader(stream);
	this.writer = new XMLDocumentWriter(reader);
    }

    public TimeSeriesResponseDocument(String string) throws SAXException, IOException {
	this.reader = new XMLDocumentReader(string);
	this.writer = new XMLDocumentWriter(reader);
    }

    public List<TimeSeries> getTimeSeries() throws GSException {
	List<TimeSeries> ret = new ArrayList<TimeSeries>();
	Node[] nodes = new Node[] {};
	try {
	    nodes = reader.evaluateNodes("//*:timeSeries");
	} catch (XPathExpressionException e) {
	    // this should never happen, as the XPath is correct
	    e.printStackTrace();
	}

	for (Node node : nodes) {
	    TimeSeries si = new TimeSeries(node);
	    ret.add(si);
	}

	return ret;
    }

    public void setValueCount(String variableVocabulary, String variableCode, Long count) {
	Element element = reader.getDocument().createElementNS(CommonNameSpaceContext.WML1_NS_URI, "valueCount");
	element.setTextContent("" + count);
	try {
	    String xpath = "//*:timeSeries[*:variable[*:variableCode='" + variableCode + "'][*:variableCode/@vocabulary='"
		    + variableVocabulary + "']][1]";
	    writer.addNode(xpath, element);
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	}
    }

    public void setVariableTimeInterval(String variableVocabulary, String variableCode, String beginTime, String endTime,
	    String beginTimeUTC, String endTimeUTC) {
	Document doc = reader.getDocument();
	Element element = doc.createElementNS(CommonNameSpaceContext.WML1_NS_URI, "variableTimeInterval");

	Element beginTimeElement = doc.createElementNS(CommonNameSpaceContext.WML1_NS_URI, "beginDateTime");
	beginTimeElement.setTextContent(beginTime);
	element.appendChild(beginTimeElement);

	Element endTimeElement = doc.createElementNS(CommonNameSpaceContext.WML1_NS_URI, "endDateTime");
	endTimeElement.setTextContent(endTime);
	element.appendChild(endTimeElement);

	Element beginTimeElementUTC = doc.createElementNS(CommonNameSpaceContext.WML1_NS_URI, "beginDateTimeUTC");
	beginTimeElementUTC.setTextContent(beginTimeUTC);
	element.appendChild(beginTimeElementUTC);

	Element endTimeElementUTC = doc.createElementNS(CommonNameSpaceContext.WML1_NS_URI, "endDateTimeUTC");
	endTimeElementUTC.setTextContent(endTimeUTC);
	element.appendChild(endTimeElementUTC);

	try {
	    String xpath = "//*:timeSeries[*:variable[*:variableCode='" + variableCode + "'][*:variableCode/@vocabulary='"
		    + variableVocabulary + "']][1]";
	    writer.addNode(xpath, element);
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	}
    }

    /**
     * Removes from the time series all the values that don't belong to the given method, quality control level and
     * source (all the fields are optional and can be null)
     * 
     * @param methodId
     * @param qualityControlLevelCode
     * @param sourceId
     */
    public void reduceValues(String methodId, String qualityControlLevelCode, String sourceId) {
	List<String> constraints = new ArrayList<>();
	if (methodId != null && !methodId.isEmpty()) {
	    constraints.add("@methodCode!='" + methodId + "'");
	}
	if (qualityControlLevelCode != null && !qualityControlLevelCode.isEmpty()) {
	    constraints.add("@qualityControlLevelCode!='" + qualityControlLevelCode + "'");
	}
	if (sourceId != null && !sourceId.isEmpty()) {
	    constraints.add("@sourceCode!='" + sourceId + "'");
	}
	if (!constraints.isEmpty()) {

	    String c = "";
	    String or = " or ";
	    for (int i = 0; i < constraints.size(); i++) {
		c += constraints.get(i) + or;
	    }
	    c = c.substring(0, c.lastIndexOf(or));

	    try {
		writer.remove("//*:value[" + c + "]");
	    } catch (XPathExpressionException e) {
		e.printStackTrace();
	    }
	}

    }

    public void fixTimes() throws GSException {
	List<TimeSeries> series = getTimeSeries();
	for (TimeSeries serie : series) {
	    serie.setBeginTimePosition(normalizeTime(serie.getBeginTimePosition()));
	    serie.setBeginTimePositionUTC(normalizeTime(serie.getBeginTimePositionUTC()));
	    serie.setEndTimePosition(normalizeTime(serie.getEndTimePosition()));
	    serie.setEndTimePositionUTC(normalizeTime(serie.getEndTimePositionUTC()));
	    List<Value> values = serie.getValues();
	    for (Value value : values) {
		value.setDateTime(normalizeTime(value.getDateTime()));
		value.setDateTimeUTC(normalizeTime(value.getDateTimeUTC()));
	    }
	}

    }

    private String normalizeTime(String time) {
	if (time == null) {
	    return null;
	}
	if (time.length() >= 10 && time.charAt(10) == ' ') {
	    time = time.substring(0, 10) + 'T' + time.substring(11);
	}
	return time;
    }

}
