package eu.essi_lab.accessor.ana;

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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import eu.essi_lab.lib.xml.XMLDocumentReader;

public class BasinDocument extends XMLDocumentReader {

	public BasinDocument(InputStream stream) throws SAXException, IOException {
		super(stream);
	}

	public String getBasinName(String basinCode) {
		try {
			return evaluateString("//*:nmBacia[../*:codBacia='" + basinCode + "']");
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return "";
	}

	public String getSubBasinName(String subBasinCode) {
		try {
			return evaluateString("//*:nmSubBacia[../*:codSubBacia='" + subBasinCode + "']");
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return "";
	}

	public String getBasinIdentifier(String subBasinCode) {
		try {
			return evaluateString("//*:codBacia[../*:codSubBacia='" + subBasinCode + "']");
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return "";
	}

	public LinkedHashSet<String> getBasinIdentifiers() {
		List<String> ret = new ArrayList<>();
		Node[] nodes;
		try {
			nodes = evaluateNodes("//*:codBacia");
			for(int i = 0; i < nodes.length; i++) {
				ret.add(nodes[i].getTextContent());
			}
			
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		return new LinkedHashSet<>(ret);
	}

	public LinkedHashSet<String> getSubBasinIdentifiers(String basinId) {
		List<String> ret = new ArrayList<>();
		Node[] nodes;
		try {
			nodes = evaluateNodes("//*:codSubBacia[../*:codBacia='" + basinId + "']");
			for(int i = 0; i < nodes.length; i++) {
				ret.add(nodes[i].getTextContent());
			}
			
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		
		return new LinkedHashSet<>(ret);
	}

}
