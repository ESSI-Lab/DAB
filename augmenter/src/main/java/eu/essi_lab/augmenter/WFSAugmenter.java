package eu.essi_lab.augmenter;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;

import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.Distribution;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.lib.net.protocols.NetProtocols;
import eu.essi_lab.lib.net.utils.Downloader;
import eu.essi_lab.lib.net.utils.whos.WMOOntology;
import eu.essi_lab.lib.net.utils.whos.WMOUnit;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
public class WFSAugmenter extends ResourceAugmenter {

    private Map<String, List<String>> wfsMap = new HashMap<String, List<String>>();

    public WFSAugmenter() {

	setLabel("WFS augmenter");

    }

    @Override
    public Optional<GSResource> augment(GSResource resource) throws GSException {

	GSLoggerFactory.getLogger(getClass()).info("WFS augmentation of current resource STARTED");

	Distribution dist = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDistribution();
	if (dist != null) {
	    List<Online> onlines = Lists.newArrayList(dist.getDistributionOnlines());
	    for (Online o : onlines) {
		if (o.getProtocol().equalsIgnoreCase("ogc:wfs") || o.getProtocol().equals(NetProtocols.WFS_1_1_0.getCommonURN())
			|| o.getProtocol().equals(NetProtocols.WFS.getCommonURN())
			|| o.getProtocol().equals(NetProtocols.WFS_1_1_0.getCommonURN())
			|| o.getProtocol().equals(NetProtocols.WFS_2_0_0.getCommonURN())) {
		    String baseWFS = o.getLinkage();
		    String name = o.getName();
		    // https://sdi.iia.cnr.it/gmosgeoserver/ows?request=GetFeature&service=WFS&version=1.1.0&typeName=GMOS:MAL&outputFormat=csv
		    // https://sdi.iia.cnr.it/gmosgeoserver/ows?request=GetFeature&service=WFS&version=1.1.0&typeName=GMOS:MAL&outputFormat=application%2Fjson

		    // get capabilities & return format
		    if (!wfsMap.containsKey(baseWFS)) {
			String getCapabilitiesRequest = baseWFS.endsWith("?")
				? baseWFS + "service=WFS&request=GetCapabilities&version=1.1.0"
				: baseWFS + "?service=WFS&request=GetCapabilities&version=1.1.0";

			List<String> outputFormats = readOutputFormatsFromCapabilities(getCapabilitiesRequest);
			wfsMap.put(baseWFS, outputFormats);
		    }

		    for (String s : wfsMap.get(baseWFS)) {
			Online newOnline = new Online();
			newOnline.setName(name + " (" + s + ")"); 
			String newLinkage = baseWFS.endsWith("?")
				? baseWFS + "request=GetFeature&service=WFS&version=1.1.0&typeName=" + name + "&outputFormat=" + s
				: baseWFS +"?request=GetFeature&service=WFS&version=1.1.0&typeName=" + name + "&outputFormat=" + s;
			newOnline.setLinkage(newLinkage);
			// String protocol;
			newOnline.setProtocol(s);
			newOnline.setDescription("Output Format: " + s +" . WFS layer name: " + name);
			dist.addDistributionOnline(newOnline);
		    }

		}

	    }

	}

	GSLoggerFactory.getLogger(getClass()).warn("WFS augmentation of current resource ENDED");

	return Optional.of(resource);
    }

    private List<String> readOutputFormatsFromCapabilities(String getCapabilitiesRequest) {
	List<String> ret = new ArrayList<String>();
	Downloader d = new Downloader();
	Optional<InputStream> res = d.downloadStream(getCapabilitiesRequest);
	if (res.isPresent()) {
	    try {
		XMLDocumentReader xdoc = new XMLDocumentReader(res.get());
		Node n = xdoc.evaluateNode("//*:Operation[@name='GetFeature']/*:Parameter[@name='outputFormat']");
		if (n != null) {
		    Node[] nodesValue = xdoc.evaluateNodes(n, "*:Value");
		    for (Node values : nodesValue) {
			String val = values.getTextContent();
			ret.add(val);
		    }
		}
	    } catch (SAXException | IOException | XPathExpressionException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
	return ret;
    }

    @Override
    public void onOptionSet(GSConfOption<?> option) throws GSException {

    }

    @Override
    public void onFlush() throws GSException {
    }
}
