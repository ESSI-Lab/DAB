package eu.essi_lab.accessor.ana;

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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import eu.essi_lab.lib.xml.XMLDocumentReader;

public class StationListDocument extends XMLDocumentReader {

	public StationListDocument(InputStream stream) throws SAXException, IOException, ParserConfigurationException {
		super(stream);
	}

	public LinkedHashSet<String> listIdentifiers() throws Exception {

		List<String> ret = new ArrayList<>();
		Node[] nodes;
		try {
			nodes = evaluateNodes("//*:CodEstacao");
			for (int i = 0; i < nodes.length; i++) {
				ret.add(nodes[i].getTextContent());
			}

		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		return new LinkedHashSet<>(ret);
	}

	public StationDocument getStation(String identifier) {
		List<Node> nodes = new ArrayList<>();
		try {
			nodes = evaluateOriginalNodesList("//*:Table[*:CodEstacao='" + identifier + "']");

			if (nodes.isEmpty()) {
				return null;
			}

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			Source xmlSource = new DOMSource(nodes.get(0));
			Result outputTarget = new StreamResult(outputStream);

			TransformerFactory.newInstance().newTransformer().transform(xmlSource, outputTarget);

			InputStream is = new ByteArrayInputStream(outputStream.toByteArray());

			return new StationDocument(is);

		} catch (XPathExpressionException e) {
			e.printStackTrace();
		} catch (TransformerException | TransformerFactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public List<StationDocument> getStations(String basinId, String subBasinId) {
		List<StationDocument> ret = new ArrayList<>();
		List<Node> nodes = new ArrayList<>();
		try {
			nodes = evaluateOriginalNodesList("//*:Table[*:Bacia='" + basinId + "'][*:SubBacia='" + subBasinId + "']");
		
		if (nodes.isEmpty()) {
			return null;
		}
		for (Node node : nodes) {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			Source xmlSource = new DOMSource(node);
			Result outputTarget = new StreamResult(outputStream);

			TransformerFactory.newInstance().newTransformer().transform(xmlSource, outputTarget);

			InputStream is = new ByteArrayInputStream(outputStream.toByteArray());

			ret.add(new StationDocument(is));
		}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return ret;
	}

}
