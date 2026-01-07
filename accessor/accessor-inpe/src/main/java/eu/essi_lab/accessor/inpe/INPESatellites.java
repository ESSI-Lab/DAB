package eu.essi_lab.accessor.inpe;

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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.xml.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;

import eu.essi_lab.lib.xml.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import eu.essi_lab.lib.utils.GSLoggerFactory;

public class INPESatellites {

    private List<INPESatellite> satellites = new ArrayList<>();

    public List<INPESatellite> getSatellites() {
	return satellites;
    }

    private static INPESatellites instance;

    public static INPESatellites getInstance() {
	if (instance == null) {
	    instance = new INPESatellites();
	}
	return instance;
    }

    private INPESatellites() {

    }

    public void init(InputStream stream) throws Exception {

	satellites.clear();

	// ByteArrayOutputStream baos = new ByteArrayOutputStream();
	// IOUtils.copy(stream, baos);
	// stream.close();

	// ByteArrayInputStream stream2 = new ByteArrayInputStream(baos.toByteArray());
	InputStreamReader reader = new InputStreamReader(stream, Charset.forName("ISO-8859-1"));
	InputSource input = new InputSource(reader);

	XMLReader tagsoupReader = null;

	try {
	    tagsoupReader = XMLReaderFactory.createXMLReader("org.ccil.cowan.tagsoup.Parser");
	    tagsoupReader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	SAXSource source = new SAXSource(tagsoupReader, input);
	DOMResult result = new DOMResult();

	TransformerFactory factory = XMLFactories.newTransformerFactory();

	Transformer transformer = factory.newTransformer();

	transformer.transform(source, result);

	XMLDocumentReader panel = new XMLDocumentReader((Document) result.getNode());

	stream.close();
	reader.close();

	// stream2 = new ByteArrayInputStream(baos.toByteArray());
	// reader = new InputStreamReader(stream2, Charset.forName("ISO-8859-1"));
	// BufferedReader buffer = new BufferedReader(reader);
	//
	// String line;
	//
	// String interest
	// while ((line = buffer.readLine()) != null) {
	//
	// }
	//
	// stream2.close();
	// reader.close();
	// buffer.close();

	String js = panel.evaluateString("//*:script[1]");

	String[] lines = js.split("\n");
	List<String> sensorLines = new ArrayList<>();
	boolean comment = false;
	for (String line : lines) {
	    String trimmed = line.trim();
	    if (!trimmed.startsWith("//")) {
		if (trimmed.startsWith("/*")) {
		    comment = true;
		} else if (trimmed.startsWith("*/")) {
		    comment = false;
		} else {
		    if (!comment) {
			if (trimmed.startsWith("sensors[") && trimmed.contains("= new Array(")) {
			    sensorLines.add(trimmed);
			}
		    }
		}
	    }
	}

	Node[] satelliteNodes = panel.evaluateNodes("//*:select[@id='SATELITE']/*:option");
	// we start from 1, as 0 is the unselected field
	for (int i = 1; i < satelliteNodes.length; i++) {
	    Node satelliteNode = satelliteNodes[i];
	    String id = panel.evaluateString(satelliteNode, "@value");
	    String title = panel.evaluateString(satelliteNode, ".");
	    INPESatellite satellite = new INPESatellite();
	    satellite.setId(id);
	    satellite.setTitle(title);

	    // these are known satellites that can't be harvested (they need a different service)
	    HashSet<String> blackList = new HashSet<>();
	    blackList.add("CB4");
	    blackList.add("DEIMOS");
	    blackList.add("L8");
	    blackList.add("NOAA20");
	    blackList.add("NOAA-20");
	    blackList.add("P6");
	    blackList.add("RE");
	    blackList.add("RES2");
	    blackList.add("NPP");
	    blackList.add("SPOT");
	    blackList.add("PHR");

	    if (blackList.contains(id)) {
		GSLoggerFactory.getLogger(getClass()).warn("Found a black list satellite: " + id);
	    } else {
		satellites.add(satellite);
	    }

	    String sensorLine = sensorLines.get(i);
	    // initially something like:
	    // sensors[2] = new Array(["",""],["CCD","CCD"],["IRM","IRM"],["WFI","WFI"]); //CBERS2
	    sensorLine = sensorLine.substring(sensorLine.indexOf("new Array"));
	    sensorLine = sensorLine.replace("new Array(", "");
	    sensorLine = sensorLine.substring(0, sensorLine.indexOf(");"));
	    sensorLine = sensorLine.replace("[", "");
	    sensorLine = sensorLine.replace("]", "");
	    sensorLine = sensorLine.replace(" ", "");
	    sensorLine = sensorLine.replace("\"", "");
	    // will become something like "","","CCD","CCD","IRM","IRM","WFI","WFI"
	    String[] split = sensorLine.split(",");
	    for (int j = 0; j < split.length; j = j + 2) {
		String sensorId = split[j];
		String sensorTitle = split[j + 1];
		if (!sensorId.equals("")) {
		    INPESensor sensor = new INPESensor();
		    sensor.setId(sensorId);
		    sensor.setTitle(sensorTitle);
		    satellite.getSensors().add(sensor);
		}
	    }
	}

    }

}
