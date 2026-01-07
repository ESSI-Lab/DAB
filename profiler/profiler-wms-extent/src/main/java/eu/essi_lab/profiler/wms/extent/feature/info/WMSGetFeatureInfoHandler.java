/**
 * 
 */
package eu.essi_lab.profiler.wms.extent.feature.info;

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
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;

import eu.essi_lab.access.datacache.BBOX;
import eu.essi_lab.access.datacache.DataCacheConnector;
import eu.essi_lab.access.datacache.DataCacheConnectorFactory;
import eu.essi_lab.access.datacache.StationRecord;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.dc_connector.DataCacheConnectorSetting;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.StreamingRequestHandler;
import eu.essi_lab.profiler.wms.extent.WMSLayer;
import eu.essi_lab.profiler.wms.extent.WMSRequest.Parameter;
import eu.essi_lab.profiler.wms.extent.map.Layer;
import eu.essi_lab.profiler.wms.extent.map.WMSGetMapHandler;

/**
 * @author boldrini
 */
public class WMSGetFeatureInfoHandler extends StreamingRequestHandler {

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	ValidationMessage ret = new ValidationMessage();
	try {
	    new WMSGetFeatureInfoRequest(request);
	    ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	} catch (Exception e) {
	    ret.setResult(ValidationResult.VALIDATION_FAILED);
	}

	return ret;
    }

    @Override
    public StreamingOutput getStreamingResponse(WebRequest webRequest) throws GSException {
	return new StreamingOutput() {

	    @Override
	    public void write(OutputStream output) throws IOException, WebApplicationException {

		

		try {

			

		    WMSGetFeatureInfoRequest request = new WMSGetFeatureInfoRequest(webRequest);

		    String version = request.getParameterValue(Parameter.VERSION);
		    if (version == null || version.isEmpty()) {
			version = "1.3.0";
		    }

		    String time = request.getParameterValue(Parameter.TIME);
		    Date beginDate = null;
		    Date endDate = null;
		    if (time != null && !time.isEmpty()) {
			String[] split = time.split("/");
			beginDate = ISO8601DateTimeUtils.parseISO8601(split[0]);
			endDate = ISO8601DateTimeUtils.parseISO8601(split[1]);
		    }

		    Integer width = Integer.parseInt(request.getParameterValue(Parameter.WIDTH));
		    Integer height = Integer.parseInt(request.getParameterValue(Parameter.HEIGHT));
		    String layers = request.getParameterValue(Parameter.LAYERS);
		    String crs = request.getParameterValue(Parameter.CRS);
		    crs = crs.toUpperCase();
		    String bboxString = request.getParameterValue(Parameter.BBOX);
		    String format = request.getParameterValue(Parameter.INFO_FORMAT);
		    if (format == null || format.isEmpty()) {
			format = "text/html";
		    }

		    //
		    String featureCountString = request.getParameterValue(Parameter.FEATURE_COUNT);
		    Integer featureCount = null;
		    if (featureCountString != null && !featureCountString.isEmpty()) {
			featureCount = Integer.parseInt(featureCountString);
		    }

		    String iParameter = request.getParameterValue(Parameter.I);
		    String jParameter = request.getParameterValue(Parameter.J);
		    Integer i = null;
		    if (iParameter != null) {
			i = Integer.parseInt(iParameter);
		    }
		    Integer j = null;
		    if (jParameter != null) {
			j = Integer.parseInt(jParameter);
		    }

		    // BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		    //
		    // Graphics2D ig2 = bi.createGraphics();
		    //
		    // int r = 8;

		    if (bboxString != null && crs != null) {
			String[] split = bboxString.split(",");
			BigDecimal minx = null;
			BigDecimal miny = null;
			BigDecimal maxx = null;
			BigDecimal maxy = null;

			//
			if (version.equals("1.3.0") && crs.equals("EPSG:4326")) {
			    // northing, easting
			    miny = new BigDecimal(split[0]);
			    minx = new BigDecimal(split[1]);
			    maxy = new BigDecimal(split[2]);
			    maxx = new BigDecimal(split[3]);
			} else {
			    // case "CRS:84":
			    // case "EPSG:3857":
			    // easting, northing
			    minx = new BigDecimal(split[0]);
			    miny = new BigDecimal(split[1]);
			    maxx = new BigDecimal(split[2]);
			    maxy = new BigDecimal(split[3]);
			}

			if (crs.equals("EPSG:4326")) {
			    crs = "CRS:84";
			}

			double bminx = minx.doubleValue();
			double bminy = miny.doubleValue();
			double bmaxx = maxx.doubleValue();
			double bmaxy = maxy.doubleValue();

			if (crs.equals("CRS:84")) {
			    bminx = normalizeLongitude(bminx);
			    bmaxx = normalizeLongitude(bmaxx);
			}

			double w = bmaxx - bminx;
			double h = bmaxy - bminy;

			double pixelWidth = w / width;
			double pixelHeight = h / height;
			int r = 8;
			double fminx = bminx + (i - r) * pixelWidth;
			double fmaxx = bminx + (i + r) * pixelWidth;
			double fmaxy = bmaxy - (j - r) * pixelHeight;
			double fminy = bmaxy - (j + r) * pixelHeight;

			BBOX bbox = new BBOX(crs, fminx, fminy, fmaxx, fmaxy);
			List<SimpleEntry<String, String>> properties = new ArrayList();
			String[] layerSplit = new String[] {};
			if (layers.contains(",")) {
			    layerSplit = layers.split(",");
			} else {
			    layerSplit = new String[] { layers };
			}
			List<WMSLayer> wmsLayers = WMSLayer.decode(webRequest.extractViewId());

			WMSFeatureInfoGenerator generator = null;

			for (String layer : layerSplit) {
			    for (WMSLayer wmsLayer : wmsLayers) {
				if (wmsLayer.getLayerName().equals(layer)) {
				    generator = wmsLayer.getGenerator();
				    properties.add(new SimpleEntry<>(wmsLayer.getProperty(), wmsLayer.getValue()));
				}
			    }

			}

			List<StationRecord> stations;

			Layer cachedLayer = null;
			if ((layers.equals("i-change-monitoring-points") || layers.equals("trigger-monitoring-points"))
				&& (cachedLayer = WMSGetMapHandler.getCachedLayer(layers, webRequest.extractViewId())) != null) {
			    stations = cachedLayer.getStations(bbox, beginDate, endDate);
			} else {
				DataCacheConnector dataCacheConnector =  DataCacheConnectorFactory.getDataCacheConnector();

			    if (dataCacheConnector == null) {
				DataCacheConnectorSetting setting = ConfigurationWrapper.getDataCacheConnectorSetting();
				dataCacheConnector = DataCacheConnectorFactory.newDataCacheConnector(setting);
				String cachedDays = setting.getOptionValue(DataCacheConnector.CACHED_DAYS).get();
				String flushInterval = setting.getOptionValue(DataCacheConnector.FLUSH_INTERVAL_MS).get();
				String maxBulkSize = setting.getOptionValue(DataCacheConnector.MAX_BULK_SIZE).get();
				dataCacheConnector.configure(DataCacheConnector.MAX_BULK_SIZE, maxBulkSize);
				dataCacheConnector.configure(DataCacheConnector.FLUSH_INTERVAL_MS, flushInterval);
				dataCacheConnector.configure(DataCacheConnector.CACHED_DAYS, cachedDays);
				DataCacheConnectorFactory.setDataCacheConnector(dataCacheConnector);
			    }
			    stations = dataCacheConnector.getStationsWithProperties(bbox, 0, featureCount, false,
				    properties.toArray(new SimpleEntry[] {}));
			}

			InputStream stream = generator.getInfoPage(stations, format, request);

			IOUtils.copy(stream, output);

		    }

		    // ImageIO.write(bi, "PNG", new File("/home/boldrini/a.png"));

		    // ImageIO.write(bi, "PNG", output);

		} catch (Exception e) {
		    e.printStackTrace();

		}
	    }

	    private double normalizeLongitude(double lon) {
		if (lon > 180) {
		    lon = lon + 180;
		    lon = lon % 360;
		    lon = lon - 180;
		    return lon;
		}
		if (lon < -180) {
		    lon = normalizeLongitude(-lon);
		    return -lon;
		}
		return lon;
	    }
	};
    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {
	WMSGetFeatureInfoRequest map = new WMSGetFeatureInfoRequest(webRequest);
	String format = map.getParameterValue(Parameter.INFO_FORMAT);
	if (format == null || format.isEmpty()) {
	    format = "text/html";
	}
	if (format.toLowerCase().contains("json")) {
	    return new MediaType("application", "json");
	} else {
	    return new MediaType("text", "html");
	}

    }

    private boolean areEquals(double s, double n) {
	return Math.abs(s - n) < 0.0000001d;
    }
}
