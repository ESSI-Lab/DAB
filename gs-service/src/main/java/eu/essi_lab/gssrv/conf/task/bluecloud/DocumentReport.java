package eu.essi_lab.gssrv.conf.task.bluecloud;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

import eu.essi_lab.lib.xml.XMLDocumentReader;

/**
 * Contains a map from metadata elements to their values as occurring in a
 * document
 */
public class DocumentReport {

	private HashMap<MetadataElement, List<String>> map = new HashMap<>();
	private XMLDocumentReader document;

	public DocumentReport(XMLDocumentReader document) {
		this.document = document;
	}

	public HashMap<MetadataElement, List<String>> getMap() {
		return map;
	}

	public void addMetadata(MetadataElement element, List<String> values) {
		map.put(element, values);
	}

	public void addMetadata(MetadataElement element, String value) {
		List<String> list = new ArrayList<>();
		list.add(value);
		map.put(element, list);
	}

	public void addValues(MetadataElement element) throws XPathExpressionException {

		String[] paths = element.getPaths();
		List<String> values = new ArrayList<String>();
		boolean atLeastOneGood = false;

		if (paths.length > 1) {
			for (String path : paths) {
				String value = document.evaluateString(path);
				if (value != null && !value.isEmpty()) {
					atLeastOneGood = true;
					values.add(value);
				}				
			}
		} else {
			for (String path : paths) {
				Node[] nodes = document.evaluateNodes(path);
				for (Node node : nodes) {
					String v = document.evaluateString(node, ".");
					if (v != null && !v.isEmpty()) {
						atLeastOneGood = true;
						values.add(v);
					}
				}
			}
		}
		if (atLeastOneGood) {
			addMetadata(element, values);
		}

	}

}
