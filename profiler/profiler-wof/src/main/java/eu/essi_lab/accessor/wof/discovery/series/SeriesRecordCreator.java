package eu.essi_lab.accessor.wof.discovery.series;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.nio.charset.StandardCharsets;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

/**
 * Small utility to create a fragment of XML - the SeriesRecord document
 * 
 * @author boldrini
 */
public class SeriesRecordCreator {

    public String createSeriesRecord(String servCode, String servURL, String location, String varCode, String varName, String beginDate,
	    String endDate, //
	    String valueCount, String siteName, String latitude, String longitude, String dataType, String valueType, String sampleMedium,
	    String timeUnits, //
	    String conceptKeywords, String genCategory, String timeSupport) throws Exception {

	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	XMLEventWriter writer = XMLOutputFactory.newInstance().createXMLEventWriter(baos);
	XMLEventFactory events = XMLEventFactory.newInstance();
	writer.add(events.createStartElement("", "", "SeriesRecord"));

	addElement(writer, events, "ServCode", servCode);
	addElement(writer, events, "ServURL", servURL);
	addElement(writer, events, "location", location);
	addElement(writer, events, "VarCode", varCode);
	addElement(writer, events, "VarName", varName);
	addElement(writer, events, "beginDate", beginDate);
	addElement(writer, events, "endDate", endDate);
	addElement(writer, events, "ValueCount", valueCount);
	addElement(writer, events, "Sitename", siteName);
	addElement(writer, events, "latitude", latitude);
	addElement(writer, events, "longitude", longitude);
	addElement(writer, events, "datatype", dataType);
	addElement(writer, events, "valuetype", valueType);
	addElement(writer, events, "samplemedium", sampleMedium);
	addElement(writer, events, "timeunits", timeUnits);
	addElement(writer, events, "conceptKeyword", conceptKeywords);
	addElement(writer, events, "genCategory", genCategory);
	addElement(writer, events, "TimeSupport", timeSupport);

	writer.add(events.createEndElement("", "", "SeriesRecord"));
	writer.close();
	ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
	String ret = IOUtils.toString(bais, StandardCharsets.UTF_8);
	bais.close();
	baos.close();
	return ret;

    }

    private void addElement(XMLEventWriter writer, XMLEventFactory events, String tag, String value) throws XMLStreamException {
	writer.add(events.createStartElement("", "", tag));
	if (value != null) {
	    Characters characters = events.createCharacters(value);
	    writer.add(characters);
	}
	writer.add(events.createEndElement("", "", tag));

    }

}
