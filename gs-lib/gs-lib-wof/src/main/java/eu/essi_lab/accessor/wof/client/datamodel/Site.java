package eu.essi_lab.accessor.wof.client.datamodel;

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

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.w3c.dom.Node;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLNodeReader;
import eu.essi_lab.lib.xml.XMLNodeWriter;
import eu.essi_lab.model.exceptions.GSException;

public class Site {
    private XMLNodeReader reader;

    public XMLNodeReader getReader() {
	return reader;
    }

    private Logger logger = GSLoggerFactory.getLogger(SiteInfo.class);
    private XMLNodeWriter writer;

    public Site(Node node) {
	this.reader = new XMLNodeReader(node);
	this.writer = new XMLNodeWriter(reader);
    }

    public SiteInfo getSitesInfo() throws GSException {
	SiteInfo ret = null;
	Node[] nodes = new Node[] {};
	try {
	    nodes = reader.evaluateNodes("*:siteInfo");
	} catch (XPathExpressionException e) {
	    // this should never happen, as the XPath is correct
	    e.printStackTrace();
	}

	if (nodes.length > 0) {

	    ret = new SiteInfo(nodes[0]);
	}

	return ret;
    }

    public String getSeriesCatalogWSDL() {

	String ret = "";
	try {
	    ret = reader.evaluateString("*:seriesCatalog/@serviceWsdl");
	} catch (XPathExpressionException e) {
	    // this should never happen, as the XPath is correct
	    e.printStackTrace();
	}
	return ret;
    }

    public void setSeriesCatalogWSDL(String endpoint) {

	XMLNodeWriter writer = new XMLNodeWriter(this.reader);
	try {
	    writer.addAttributes("*:seriesCatalog", "serviceWsdl", endpoint);
	} catch (XPathExpressionException e) {
	    // this should never happen, as the XPath is correct
	    e.printStackTrace();
	}
    }

    public List<TimeSeries> getSeries() throws GSException {
	List<TimeSeries> ret = new ArrayList<TimeSeries>();
	Node[] nodes = new Node[] {};
	try {
	    nodes = reader.evaluateNodes("*:seriesCatalog/*:series | *:seriesCatalog/*:timeSeries");
	} catch (XPathExpressionException e) {
	    // this should never happen, as the XPath is correct
	    e.printStackTrace();
	}

	for (Node node : nodes) {
	    TimeSeries series = new TimeSeries(node);
	    ret.add(series);
	}

	return ret;
    }

    public void clearSeries() {
	try {
	    writer.remove("*:seriesCatalog/*:series");
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	}

    }

    public void addSeries(TimeSeries timeSeries) {
	try {
	    writer.addNode("*:seriesCatalog", timeSeries.getReader().getNode());
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	}

    }
}
