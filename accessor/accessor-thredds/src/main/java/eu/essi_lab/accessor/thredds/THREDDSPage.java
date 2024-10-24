package eu.essi_lab.accessor.thredds;

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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

import eu.essi_lab.lib.xml.XMLDocumentReader;

/**
 * @author boldrini
 */
public class THREDDSPage {
    private XMLDocumentReader reader;
    private THREDDSReference reference = null;
    private URL url;
    private THREDDSPage father;

    public THREDDSPage getFather() {
	return father;
    }

    public XMLDocumentReader getReader() {
        return reader;
    }

    public void setReader(XMLDocumentReader reader) {
        this.reader = reader;
    }

    public THREDDSPage(THREDDSPage father, URL url, XMLDocumentReader reader) {
	this.father = father;
	setURL(url);
	this.reader = reader;
    }

    public List<String> getCatalogRefs() throws Exception {
	List<String> ret = new ArrayList<String>();
	Node[] nodes = reader.evaluateNodes("//*:catalogRef/@*:href");
	for (Node node : nodes) {
	    String link = reader.evaluateString(node, ".");
	    ret.add(link);
	}
	return ret;
    }

    public List<Node> getDatasetNodes() throws Exception {
	List<Node> ret = new ArrayList<>();
	Node[] nodes = reader.evaluateNodes("//*:dataset[@urlPath]");
	for (Node node : nodes) {
	    ret.add(node);
	}
	return ret;
    }

    public List<Node> getServiceNodes() throws Exception {
	List<Node> ret = new ArrayList<>();
	Node[] nodes = reader.evaluateNodes("//*:service");
	for (Node node : nodes) {
	    ret.add(node);
	}
	return ret;
    }

    public List<THREDDSService> getServices() throws Exception {
	List<THREDDSService> ret = new ArrayList<>();
	List<Node> nodes = getServiceNodes();
	for (Node node : nodes) {
	    String name = reader.evaluateString(node, "@name");
	    String serviceType = reader.evaluateString(node, "@serviceType");
	    String base = reader.evaluateString(node, "@base");
	    THREDDSService service = new THREDDSService();
	    service.setName(name);
	    service.setServiceType(serviceType);
	    service.setBase(base);
	    ret.add(service);
	}
	return ret;
    }

    public List<THREDDSReference> getReferences() throws Exception {
	List<THREDDSReference> ret = new ArrayList<THREDDSReference>();
	List<String> catalogRef = getCatalogRefs();
	for (int i = 0; i < catalogRef.size(); i++) {
	    THREDDSReference childReference = reference.clone();
	    childReference.addTarget(i);
	    ret.add(childReference);
	}
	return ret;
    }

    public void setReference(THREDDSReference reference) {
	this.reference = reference;

    }

    public void setURL(URL url) {
	this.url = url;
    }

    public URL getURL() {
	return url;
    }

    public List<THREDDSDataset> getDatasets() throws Exception {
	List<THREDDSService> services = getServices();
	List<THREDDSDataset> ret = new ArrayList<>();
	List<Node> nodes = getDatasetNodes();
	for (Node node : nodes) {
	    THREDDSDataset dataset = new THREDDSDataset();
	    String name = reader.evaluateString(node, "@name");
	    String id = reader.evaluateString(node, "@ID");
	    String url = reader.evaluateString(node, "@urlPath");
	    dataset.setName(name);
	    dataset.setId(id);
	    for (THREDDSService service : services) {
		String base = service.getBase();
		if (!base.isEmpty()) {
		    URL serviceURL = new URL(this.url, base);
		    URL datasetURL = new URL(serviceURL, url);
		    dataset.getServices().put(service.getServiceType(), datasetURL);
		}
	    }
	    ret.add(dataset);
	}
	return ret;
    }

    public THREDDSReference getNextReference(THREDDSReference currentReference) throws Exception {
	List<THREDDSReference> references = getReferences();
	if (currentReference.getFirstTarget() == null) {

	    // first node

	    if (references.isEmpty()) {
		return null;
	    } else {
		return references.get(0);
	    }
	} else if (currentReference.equals(this.reference)) {

	    // we are on the correct node

	    if (references.isEmpty()) {
		if (father == null) {
		    return null;
		} else {
		    return father.getNextReference(currentReference);
		}
	    } else {
		return references.get(0);
	    }
	} else {

	    // we are on a father

	    for (int i = 0; i < references.size(); i++) {
		THREDDSReference child = references.get(i);

		if (currentReference.equals(child)) {
		    if (i == references.size() - 1) {
			// last
			if (father == null) {
			    // very last
			    return null;
			} else {
			    THREDDSReference clone = currentReference.clone();
			    clone.removeLastTarget();
			    return father.getNextReference(clone);
			}
		    } else {
			return references.get(i + 1);
		    }
		}

	    }

	    return null;

	}

    }

}
