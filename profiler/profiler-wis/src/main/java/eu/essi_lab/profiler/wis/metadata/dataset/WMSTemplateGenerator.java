package eu.essi_lab.profiler.wis.metadata.dataset;

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
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.accessor.wms._1_3_0.WMS_1_3_0Capabilities;
import eu.essi_lab.accessor.wms._1_3_0.WMS_1_3_0Connector;
import eu.essi_lab.accessor.wms._1_3_0.WMS_1_3_0Layer;
import eu.essi_lab.jaxb.wms._1_3_0.WMSCapabilities;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.ExpiringCache;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.resource.data.AxisOrder;
import eu.essi_lab.model.resource.data.CRS;
import net.opengis.gml.v_3_2_0.EnvelopeType;

public class WMSTemplateGenerator {

    public static void main(String[] args) throws Exception {
	WMSTemplateGenerator wtg = new WMSTemplateGenerator();
	JSONObject template = wtg.generate("http://alerta.ina.gob.ar/geoserver/public2/wms?SERVICE=WMS&", "hmfs_s2sep_asmt_month_4");
	System.out.println(template);
    }

    static JAXBContext context;

    static {

	try {

	    context = JAXBContext.newInstance(WMSCapabilities.class);

	} catch (JAXBException e) {

	    GSLoggerFactory.getLogger(WMS_1_3_0Connector.class).error("Fatal initialization error in WMS 1.3.0 template generator!", e);

	}
    }

    private static Unmarshaller u;
    private static ExpiringCache<WMS_1_3_0Capabilities> cache = new ExpiringCache<WMS_1_3_0Capabilities>();
    private static ExpiringCache<JSONObject> cache2 = new ExpiringCache<JSONObject>();

    static {
	try {
	    u = context.createUnmarshaller();
	    cache.setDuration(1200000);
	    cache2.setDuration(1200000);
	} catch (JAXBException e) {
	    e.printStackTrace();
	}
    }

    public JSONObject generate(String endpoint, String layer) throws Exception {

	String id = endpoint+";"+layer;
	if (cache2.get(id)!=null) {
	    return cache2.get(id);
	}
		
	
	JSONObject json = new JSONObject();
	json.put("rel", "item");

	String url = concatenate(endpoint, "&service=WMS&request=GetCapabilities");

	WMS_1_3_0Capabilities caps = cache.get(url);

	if (caps == null) {
	    Downloader d = new Downloader();
	    d.setConnectionTimeout(TimeUnit.SECONDS, 5);	    
	    Optional<String> capabilities = d.downloadOptionalString(url);
	    if (!capabilities.isPresent()) {
		return null;
	    }
	    WMSCapabilities wmsCapabilities = (WMSCapabilities) u.unmarshal(new ByteArrayInputStream(//
		    capabilities.get().getBytes(StandardCharsets.UTF_8)));

	    caps = new WMS_1_3_0Capabilities(wmsCapabilities);
	    cache.put(url, caps);
	}


	WMS_1_3_0Layer wmsLayer = new WMS_1_3_0Layer(caps, layer);

	String imageFormat = null;
	for (String format : wmsLayer.getFormat()) {
	    if (format.startsWith("image")) {
		imageFormat = format;
		if (format.contains("png")) {
		    break;
		}
	    }
	}

	json.put("type", imageFormat);
	json.put("title", wmsLayer.getTitle());
	json.put("href", concatenate(wmsLayer.getGetMapURL(), "service=WMS&request=GetMap&version=1.3.0&layers=" + wmsLayer.getName()
		+ "&styles=&format={format}&crs={crs}&bbox={bbox}&width={width}&height={height}"));
	json.put("templated", true);
	JSONObject vars = new JSONObject();

	JSONObject crs = new JSONObject();
	crs.put("description", "Coordinate reference system.");
	crs.put("type", "string");
	JSONArray crses = new JSONArray();
	for (String c : wmsLayer.getCRS()) {
	    crses.put(c);
	}
	crs.put("enum", crses);
	vars.put("crs", crs);

	JSONObject bbox = new JSONObject();
	bbox.put("description", "Bounding box corners (lower left, upper right) in CRS units.");
	bbox.put("type", "array");
	JSONObject items = new JSONObject();
	items.put("type", "number");
	items.put("format", "double");
	bbox.put("items", items);
	bbox.put("minItems", 4);
	bbox.put("maxItems", 4);
	vars.put("bbox", bbox);

	JSONObject width = new JSONObject();
	width.put("description", "Width in pixels of map picture.");
	width.put("type", "number");
	width.put("format", "integer");
	vars.put("width", width);

	JSONObject height = new JSONObject();
	height.put("description", "Height in pixels of map picture.");
	height.put("type", "number");
	height.put("format", "integer");
	vars.put("height", height);

	JSONObject format = new JSONObject();
	format.put("description", "Output format of map.");
	format.put("type", "string");
	JSONArray formats = new JSONArray();
	for (String f : wmsLayer.getFormat()) {
	    formats.put(f);
	}
	format.put("enum", formats);
	vars.put("format", format);

	json.put("variables", vars);

	Integer h = wmsLayer.getDefaultHeight();
	Integer w = wmsLayer.getDefaultWidth();
	String c = wmsLayer.getCRS().get(0);
	EnvelopeType envelope = wmsLayer.getEnvelope(c);
	CRS ccc = CRS.fromIdentifier(c);
	if (h == null || w == null) {
	    h = 300;
	    double e1 = envelope.getUpperCorner().getValue().get(0) - envelope.getLowerCorner().getValue().get(0);
	    double e2 = envelope.getUpperCorner().getValue().get(1) - envelope.getLowerCorner().getValue().get(1);
	    if (ccc.getAxisOrder().equals(AxisOrder.NORTH_EAST)) {
		w = (int) (h * (e2 / e1));
	    } else {
		w = (int) (h * (e1 / e2));
	    }
	}

	String b = envelope.getLowerCorner().getValue().get(0) + "," + envelope.getLowerCorner().getValue().get(1) + "," + //
		envelope.getUpperCorner().getValue().get(0) + "," + envelope.getUpperCorner().getValue().get(1);
	String sample = concatenate(wmsLayer.getGetMapURL(), "service=WMS&request=GetMap&version=1.3.0&layers=" + wmsLayer.getName()
		+ "&styles=&format=" + imageFormat + "&crs=" + c + "&bbox=" + b + "&width=" + w + "&height=" + h);
	json.put("sampleRequest", sample);
	cache2.put(id, json);
	return json;

    }

    private String concatenate(String endpoint, String parameters) {
	if (parameters.startsWith("&")) {
	    parameters = parameters.substring(1);
	}
	if (!endpoint.endsWith("?") && !endpoint.endsWith("&")) {
	    if (endpoint.contains("?")) {
		endpoint = endpoint + "&";
	    } else {
		endpoint = endpoint + "?";
	    }
	}
	return endpoint + parameters;
    }
}
