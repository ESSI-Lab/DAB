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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.exceptions.GSException;

/**
 * This class is a wrapper for a WaterML 1.1 sitesResponse document
 * 
 * @author boldrini
 */
public class SitesResponseDocument implements ISitesResponseDocument {

    private XMLDocumentReader reader;

    public XMLDocumentReader getReader() {
	return reader;
    }

    public SitesResponseDocument(Document doc) {
	this.reader = new XMLDocumentReader(doc);

    }

    public SitesResponseDocument(InputStream stream) throws SAXException, IOException {
	this.reader = new XMLDocumentReader(stream);
    }

    public SitesResponseDocument(String metadata) throws SAXException, IOException {
	this.reader = new XMLDocumentReader(metadata);
    }

    public List<SiteInfo> getSitesInfo() throws GSException {
	List<SiteInfo> ret = new ArrayList<SiteInfo>();
	Node[] nodes = new Node[] {};
	try {
		// added xpath option for Argent-INA Accessor
	    nodes = reader.evaluateNodes("*:sitesResponse/*:site/*:siteInfo | //*:siteResponse/*:site/*:siteInfo");
	} catch (XPathExpressionException e) {
	    // this should never happen, as the XPath is correct
	    e.printStackTrace();
	}

	for (Node node : nodes) {
	    SiteInfo si = new SiteInfo(node);
	    ret.add(si);
	}

	return ret;
    }

    public List<Site> getSites() throws GSException {
	List<Site> ret = new ArrayList<Site>();
	Node[] nodes = new Node[] {};
	try {
		// added xpath option for Argent-INA Accessor
	    nodes = reader.evaluateNodes("*:sitesResponse/*:site | //*:siteResponse/*:site");
	} catch (XPathExpressionException e) {
	    // this should never happen, as the XPath is correct
	    e.printStackTrace();
	}

	for (Node node : nodes) {
	    Site si = new Site(node);
	    ret.add(si);
	}

	return ret;
    }

}
