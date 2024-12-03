package eu.essi_lab.profiler.wof.info.datamodel;

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

import java.io.UnsupportedEncodingException;

import javax.xml.transform.TransformerException;

import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLDocumentWriter;

/**
 * @author boldrini
 */
public class ArrayOfServiceInfo {
    private XMLDocumentReader reader;
    private XMLDocumentWriter writer;

    public ArrayOfServiceInfo() throws Exception {
	this.reader = new XMLDocumentReader(
		ArrayOfSeriesRecord.class.getClassLoader().getResourceAsStream("cuahsi/ArrayOfServiceInfo.xml"));
	this.writer = new XMLDocumentWriter(reader);
    }

    public void addServiceInfo(String serverURL, String title, String serviceDescriptionURL, String email, String phone,
	    String organization, String orgwebsite, String citation, String aabstract, String valuecount, String variablecount,
	    String sitecount, String serviceID, String networkName, String minx, String miny, String maxx, String maxy

    ) throws Exception {
	String xmlString = "<ServiceInfo xmlns=\"http://hiscentral.cuahsi.org/20100205/\">"//
		+ "<servURL>" + serverURL + "</servURL>" //
		+ "<Title>" + title + "</Title>" //
		+ "<ServiceDescriptionURL>" + serviceDescriptionURL + "</ServiceDescriptionURL>" //
		+ "<Email>" + email + "</Email>" //
		+ "<phone>" + phone + "</phone>" //
		+ "<organization>" + organization + "</organization>" //
		+ "<orgwebsite>" + orgwebsite + "</orgwebsite>"//
		+ "<citation>" + citation + "</citation>" //
		+ "<aabstract>" + aabstract + "</aabstract>" //
		+ "<valuecount>" + valuecount + "</valuecount>" //
		+ "<variablecount>" + variablecount + "</variablecount>" //
		+ "<sitecount>" + sitecount + "</sitecount>" //
		+ "<ServiceID>" + serviceID + "</ServiceID>" //
		+ "<NetworkName>" + networkName + "</NetworkName>" //
		+ "<minx>" + minx + "</minx>" //
		+ "<miny>" + miny + "</miny>" //
		+ "<maxx>" + maxx + "</maxx>" //
		+ "<maxy>" + maxy + "</maxy>" //
		+ "<serviceStatus />" //
		+ "</ServiceInfo>";
	XMLDocumentReader reader = new XMLDocumentReader(xmlString);
	this.writer.addNode("/*[1]", reader.getDocument().getDocumentElement());
    }

    public String asString() throws UnsupportedEncodingException, TransformerException {
	return this.reader.asString();
    }
}
