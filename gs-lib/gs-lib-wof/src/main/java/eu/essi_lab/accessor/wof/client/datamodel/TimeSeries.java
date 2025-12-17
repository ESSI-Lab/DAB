/**
 * This file is part of SDI HydroServer Accessor. SDI HydroServer Accessor is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or any later version. SDI HydroServer Accessor is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU
 * General Public License along with SDI HydroServer Accessor. If not, see <http://www.gnu.org/licenses/>. Copyright (C)
 * 2009-2011 ESSI-Lab <info@essi-lab.eu>
 */

package eu.essi_lab.accessor.wof.client.datamodel;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.events.XMLEvent;
import javax.xml.xpath.XPathExpressionException;

import eu.essi_lab.lib.xml.*;
import org.w3c.dom.Node;

public class TimeSeries {

    private XMLNodeReader reader;

    private SimpleDateFormat iso8601OutputFormat;

    private XMLNodeWriter writer;

    public XMLNodeReader getReader() {
	return reader;
    }

    public TimeSeries(Node node) {
	this.reader = new XMLNodeReader(node);
	this.writer = new XMLNodeWriter(reader);
	this.iso8601OutputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	iso8601OutputFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    /*
     * VARIABLE
     */

    public String getVariableID() {
	try {
	    return reader.evaluateString("*:variable/*:variableCode/@variableID");
	} catch (XPathExpressionException e) {
	    return null;
	}
    }

    public String getVariableVocabulary() {
	try {
	    return reader.evaluateString("*:variable/*:variableCode/@vocabulary");
	} catch (XPathExpressionException e) {
	    return null;
	}
    }

    public String getVariableCode() {
	try {
	    return reader.evaluateString("*:variable/*:variableCode");
	} catch (XPathExpressionException e) {
	    return null;
	}
    }

    public String getVariableName() {
	try {
	    return reader.evaluateString("*:variable/*:variableName");
	} catch (XPathExpressionException e) {
	    return "";
	}
    }

    public String getValueType() {
	try {
	    return reader.evaluateString("*:variable/*:valueType");
	} catch (XPathExpressionException e) {
	    return "";
	}
    }

    public String getDataType() {
	try {
	    return reader.evaluateString("*:variable/*:dataType");
	} catch (XPathExpressionException e) {
	    return "";
	}
    }

    public String getGeneralCategory() {
	try {
	    return reader.evaluateString("*:variable/*:generalCategory");
	} catch (XPathExpressionException e) {
	    return "";
	}
    }

    public String getSampleMedium() {
	try {
	    return reader.evaluateString("*:variable/*:sampleMedium");
	} catch (XPathExpressionException e) {
	    return "";
	}
    }

    public String getUnitName() {

	try {
	    return reader.evaluateString("*:variable/*:unit/*:unitName");
	} catch (XPathExpressionException e) {
	    return "";
	}
    }

    public String getUnitType() {

	try {
	    return reader.evaluateString("*:variable/*:unit/*:unitType");
	} catch (XPathExpressionException e) {
	    return "";
	}
    }

    public String getUnitAbbreviation() {

	try {
	    return reader.evaluateString("*:variable/*:unit/*:unitAbbreviation");
	} catch (XPathExpressionException e) {
	    return "";
	}
    }

    public String getUnitCode() {

	try {
	    return reader.evaluateString("*:variable/*:unit/*:unitCode");
	} catch (XPathExpressionException e) {
	    return "";
	}
    }

    public String getNoDataValue() {
	try {
	    return reader.evaluateString("*:variable/*:noDataValue");
	} catch (XPathExpressionException e) {
	    return "";
	}
    }

    public boolean isTimeScaleRegular() {
	try {
	    String ret = reader.evaluateString("*:variable/*:timeScale/@isRegular");
	    if (ret == null || ret.equals("")) {
		return false;
	    }
	    return Boolean.parseBoolean(ret);
	} catch (XPathExpressionException e) {
	    return false;
	}
    }

    public String getTimeScaleUnitName() {
	try {
	    String ret = reader.evaluateString("*:variable/*:timeScale/*:unit/*:unitName");
	    return ret;
	} catch (XPathExpressionException e) {
	    return "";
	}
    }

    public String getTimeScaleUnitType() {
	try {
	    String ret = reader.evaluateString("*:variable/*:timeScale/*:unit/*:unitType");
	    return ret;
	} catch (XPathExpressionException e) {
	    return "";
	}
    }

    public String getTimeScaleUnitAbbreviation() {
	try {
	    String ret = reader.evaluateString("*:variable/*:timeScale/*:unit/*:unitAbbreviation");
	    return ret;
	} catch (XPathExpressionException e) {
	    return "";
	}
    }

    public String getTimeScaleUnitCode() {
	try {
	    String ret = reader.evaluateString("*:variable/*:timeScale/*:unit/*:unitCode");
	    return ret;
	} catch (XPathExpressionException e) {
	    return "";
	}
    }

    public Number getTimeScaleTimeSupport() {
	try {
	    String str = reader.evaluateString("*:variable/*:timeScale/*:timeSupport");
	    try {
		Long ret = Long.parseLong(str);
		return ret;
	    } catch (NumberFormatException e) {
		Double ret = Double.parseDouble(str);
		return ret;
	    }
	} catch (Exception e) {
	    return null;
	}
    }
    
    public Number getTimeScaleTimeSpacing() {
	try {
	    String str = reader.evaluateString("*:variable/*:timeScale/*:timeSpacing");
	    try {
		Long ret = Long.parseLong(str);
		return ret;
	    } catch (NumberFormatException e) {
		Double ret = Double.parseDouble(str);
		return ret;
	    }
	} catch (Exception e) {
	    return null;
	}
    }


    public String getSpeciation() {
	try {
	    String ret = reader.evaluateString("*:variable/*:speciation");
	    return ret;
	} catch (XPathExpressionException e) {
	    return "";
	}
    }

    public Long getValueCount() {
	try {
	    return Long.parseLong(reader.evaluateString("*:valueCount"));
	} catch (Exception e) {
	    return null;
	}
    }

    /*
     * VARIABLE TIME INTERVAL
     */
    public Date getBeginTimePositionDate() {
	String time = getBeginTimePositionUTC();
	Date date;
	try {
	    date = iso8601OutputFormat.parse(time);
	    return date;
	} catch (ParseException e) {
	    e.printStackTrace();
	}
	return null;
    }

    public String getBeginTimePositionUTC() {

	try {
	    String dateString = reader.evaluateString("*:variableTimeInterval/*:beginDateTimeUTC");
	    return dateString;
	} catch (Exception e) {
	    return "";
	}
    }

    public void setBeginTimePositionUTC(String time) {
	try {
	    writer.setText("*:variableTimeInterval/*:beginDateTimeUTC", time);
	} catch (XPathExpressionException e) {

	}
    }

    public String getBeginTimePosition() {

	try {
	    String dateString = reader.evaluateString("*:variableTimeInterval/*:beginDateTime");
	    if (dateString != null && !dateString.equals("")) {
		return dateString;
	    }
	} catch (Exception e) {
	}
	return "";

    }

    public void setBeginTimePosition(String time) {
	try {
	    writer.setText("*:variableTimeInterval/*:beginDateTime", time);
	} catch (XPathExpressionException e) {

	}
    }

    public Date getEndTimePositionDate() {
	String time = getEndTimePositionUTC();
	Date date;
	try {
	    date = iso8601OutputFormat.parse(time);
	    return date;
	} catch (ParseException e) {
	    e.printStackTrace();
	}
	return null;
    }

    public String getEndTimePositionUTC() {

	try {
	    String dateString = reader.evaluateString("*:variableTimeInterval/*:endDateTimeUTC");
	    return dateString;
	} catch (Exception e) {
	    return "";
	}
    }

    public void setEndTimePositionUTC(String time) {
	try {
	    writer.setText("*:variableTimeInterval/*:endDateTimeUTC", time);
	} catch (XPathExpressionException e) {

	}
    }

    public String getEndTimePosition() {

	try {
	    String dateString = reader.evaluateString("*:variableTimeInterval/*:endDateTime");
	    if (dateString != null && !dateString.equals("")) {
		return dateString;
	    }
	} catch (Exception e) {
	}
	return "";

    }

    public void setEndTimePosition(String time) {
	try {
	    writer.setText("*:variableTimeInterval/*:endDateTime", time);
	} catch (XPathExpressionException e) {

	}
    }

    /*
     * METHOD
     */

    public String getMethodId() {
	try {
	    return reader.evaluateString("*:method/@methodID | *:values/*:method/@methodID");
	} catch (Exception e) {
	    return "";
	}
    }

    public String getMethodCode() {
	try {

	    return reader.evaluateString("*:method/*:methodCode | *:values/*:method/*:methodCode");
	} catch (Exception e) {
	    return "";
	}
    }

    public String getMethodDescription() {
	try {
	    return reader.evaluateString("*:method/*:methodDescription | *:values/*:method/*:methodDescription");
	} catch (Exception e) {
	    return "";
	}
    }

    public String getMethodLink() {
	try {
	    return reader.evaluateString("*:method/*:methodLink | *:values/*:method/*:methodLink");
	} catch (Exception e) {
	    return "";
	}
    }

    /*
     * SITE INFO
     */

    public SiteInfo getSiteInfo() {
	try {
	    Node node = reader.evaluateNode("*:sourceInfo");
	    if (node == null) {
		return null;
	    }
	    return new SiteInfo(node);
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	}
	return null;

    }

    /*
     * SOURCE
     */

    public String getSourceId() {
	try {
	    return reader.evaluateString("*:source/@sourceID | *:values/*:source/@sourceID");
	} catch (Exception e) {
	    return "";
	}
    }

    public String getSourceCode() {
	try {
	    return reader.evaluateString("*:source/*:sourceCode | *:values/*:source/*:sourceCode");
	} catch (Exception e) {
	    return "";
	}
    }

    public String getSourceOrganization() {
	try {
	    return reader.evaluateString("*:source/*:organization | *:values/*:source/*:organization");
	} catch (Exception e) {
	    return "";
	}
    }

    public String getSourceDescription() {
	try {
	    return reader.evaluateString("*:source/*:sourceDescription | *:values/*:source/*:sourceDescription");
	} catch (Exception e) {
	    return "";
	}
    }

    public String getSourceCitation() {
	try {
	    return reader.evaluateString("*:source/*:citation | *:values/*:source/*:citation");
	} catch (Exception e) {
	    return "";
	}
    }

    public String getSourceMetadataTopicCategory() {
	try {
	    return reader.evaluateString("*:source/*:metadata/*:topicCategory | *:values/*:source/*:metadata/*:topicCategory");
	} catch (Exception e) {
	    return "";
	}
    }

    public String getSourceMetadataTitle() {
	try {
	    return reader.evaluateString("*:source/*:metadata/*:title | *:values/*:source/*:metadata/*:title");
	} catch (Exception e) {
	    return "";
	}
    }

    public String getSourceMetadataAbstract() {
	try {
	    return reader.evaluateString("*:source/*:metadata/*:abstract | *:values/*:source/*:metadata/*:abstract");
	} catch (Exception e) {
	    return "";
	}
    }

    public String getSourceMetadataProfileVersion() {
	try {
	    return reader.evaluateString("*:source/*:metadata/*:profileVersion | *:values/*:source/*:metadata/*:profileVersion");
	} catch (Exception e) {
	    return "";
	}
    }

    public String getSourceContactName() {
	try {
	    return reader
		    .evaluateString("*:source/*:contactInformation/*:contactName | *:values/*:source/*:contactInformation/*:contactName");
	} catch (Exception e) {
	    return "";
	}
    }

    public String getSourceContactType() {
	try {
	    return reader.evaluateString(
		    "*:source/*:contactInformation/*:typeOfContact | *:values/*:source/*:contactInformation/*:typeOfContact");
	} catch (Exception e) {
	    return "";
	}
    }

    public String getSourceContactEmail() {
	try {
	    return reader.evaluateString("*:source/*:contactInformation/*:email | *:values/*:source/*:contactInformation/*:email");
	} catch (Exception e) {
	    return "";
	}
    }

    public String getSourceContactPhone() {
	try {
	    return reader.evaluateString("*:source/*:contactInformation/*:contactPhone | *:values/*:source/*:contactInformation/*:phone");
	} catch (Exception e) {
	    return "";
	}
    }

    public String getSourceContactAddress() {
	try {
	    return reader
		    .evaluateString("*:source/*:contactInformation/*:contactAddress | *:values/*:source/*:contactInformation/*:address");
	} catch (Exception e) {
	    return "";
	}
    }

    public String getSourceLink() {
	try {
	    return reader.evaluateString("*:source/*:sourceLink | *:values/*:source/*:sourceLink");
	} catch (Exception e) {
	    return "";
	}
    }

    /*
     * QUALITY CONTROL LEVEL
     */

    public String getQualityControlLevelID() {
	try {
	    return reader
		    .evaluateString("*:qualityControlLevel/@qualityControlLevelID | *:values/*:qualityControlLevel/@qualityControlLevelID");
	} catch (Exception e) {
	    return "";
	}
    }

    public String getQualityControlLevelCode() {
	try {
	    return reader.evaluateString(
		    "*:qualityControlLevel/*:qualityControlLevelCode | *:values/*:qualityControlLevel/*:qualityControlLevelCode");
	} catch (Exception e) {
	    return "";
	}
    }

    public String getQualityControlLevelDefinition() {
	try {
	    return reader.evaluateString("*:qualityControlLevel/*:definition | *:values/*:qualityControlLevel/*:definition");
	} catch (Exception e) {
	    return "";
	}
    }

    public String getQualityControlLevelExplanation() {
	try {
	    return reader.evaluateString("*:qualityControlLevel/*:explanation | *:values/*:qualityControlLevel/*:explanation");
	} catch (Exception e) {
	    return "";
	}
    }

    /*
     * CENSOR
     */

    public String getCensorCode() {
	try {
	    return reader.evaluateString("*:censorCode/*:censorCode | *:values/*:censorCode/*:censorCode");
	} catch (Exception e) {
	    return "";
	}
    }

    public String getCensorCodeDescription() {
	try {
	    return reader.evaluateString("*:censorCode/*:censorCodeDescription | *:values/*:censorCode/*:censorCodeDescription");
	} catch (Exception e) {
	    return "";
	}
    }

    /*
     * GET VALUES
     */

    public List<Value> getValues() {
	List<Value> ret = new ArrayList<Value>();
	Node[] nodes = new Node[] {};
	try {
	    nodes = reader.evaluateNodes("*:values/*:value");
	} catch (XPathExpressionException e) {
	    // this should never happen, as the XPath is correct
	    e.printStackTrace();
	}

	for (Node node : nodes) {
	    Value v = new Value(node);
	    ret.add(v);
	}

	return ret;
    }

    /**
     * This method creates a sourceInfo inside the time series.
     * 
     * @param site
     * @return
     */
    public TimeSeries enrichWithSiteInfo(SiteInfo site) {
	try {
	    XMLEventFactory xmlEventFactory = XMLEventFactory.newInstance();
	    XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
	    xmlOutputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    XMLEventWriter writer = xmlOutputFactory.createXMLEventWriter(baos);
	    writer.add(xmlEventFactory.createStartDocument());
	    writer.setDefaultNamespace("http://www.cuahsi.org/waterML/1.1/");
	    writer.add(xmlEventFactory.createStartElement(new QName("http://www.cuahsi.org/waterML/1.1/", "timeSeries"), null, null));
	    writer.add(xmlEventFactory.createNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance"));
	    // writer.add(xmlEventFactory.createNamespace("wml", "http://www.cuahsi.org/waterML/1.1/"));

	    writer.add(xmlEventFactory.createStartElement(new QName("http://www.cuahsi.org/waterML/1.1/", "sourceInfo"), null, null));
	    writer.add(
		    xmlEventFactory.createAttribute(new QName("http://www.w3.org/2001/XMLSchema-instance", "type", "xsi"), "SiteInfoType"));

	    XMLInputFactory inputFactory = XMLFactories.newXMLInputFactory();

	    XMLEventReader siteReader = inputFactory.createXMLEventReader(site.getReader().asStream());
	    while (siteReader.hasNext()) {

		XMLEvent event = siteReader.nextEvent();
		if (event.isStartDocument() || event.isEndDocument()) {
		    continue;
		}
		// this is to skip the root element (we have already inserted it with a different name:
		// from siteInfo to sourceInfo
		if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("siteInfo")) {
		    continue;
		}
		if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equals("siteInfo")) {
		    break;
		}

		writer.add(event);

	    }
	    writer.add(xmlEventFactory.createEndElement(new QName("http://www.cuahsi.org/waterML/1.1/", "sourceInfo"), null));

	    XMLEventReader seriesReader = inputFactory.createXMLEventReader(getReader().asStream());
	    while (seriesReader.hasNext()) {

		XMLEvent event = seriesReader.nextEvent();
		if (event.isStartDocument() || event.isEndDocument()) {
		    continue;
		}
		if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("series")) {
		    continue;
		}
		if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equals("series")) {
		    break;
		}

		writer.add(event);

	    }

	    writer.add(xmlEventFactory.createEndElement(new QName("http://www.cuahsi.org/waterML/1.1/", "timeSeries"), null));
	    writer.close();
	    XMLDocumentReader reader = new XMLDocumentReader(new ByteArrayInputStream(baos.toByteArray()));
	    return new TimeSeries(reader.getDocument().getDocumentElement());
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return null;
    }

}
