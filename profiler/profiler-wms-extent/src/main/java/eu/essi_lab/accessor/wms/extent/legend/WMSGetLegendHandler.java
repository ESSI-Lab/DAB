/**
 * 
 */
package eu.essi_lab.accessor.wms.extent.legend;

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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import eu.essi_lab.access.datacache.DataCacheConnector;
import eu.essi_lab.access.datacache.DataCacheConnectorFactory;
import eu.essi_lab.accessor.wms.extent.WMSLayer;
import eu.essi_lab.accessor.wms.extent.WMSRequest.Parameter;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.dc_connector.DataCacheConnectorSetting;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.StreamingRequestHandler;

/**
 * @author boldrini
 */
public class WMSGetLegendHandler extends StreamingRequestHandler {

    private static final String WMS_GET_MAP_HANDLER_ERROR = "WMS_GET_MAP_HANDLER_ERROR";

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	ValidationMessage ret = new ValidationMessage();
	try {
	    new WMSLegendRequest(request);
	    WMSLegendRequest map = new WMSLegendRequest(request);
	    String layers = checkParameter(map, Parameter.LAYERS);
	    // // String styles = checkParameter(map,Parameter.STYLES);
	    // String crs = checkParameter(map, Parameter.CRS);
	    // String bboxString = checkParameter(map, Parameter.BBOX);
	    // String widthString = checkParameter(map, Parameter.WIDTH);
	    // String heightString = checkParameter(map, Parameter.HEIGHT);

	    ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	} catch (Exception e) {
	    ret.setError("Missing mandatory parameter: " + e.getMessage());
	    ret.setLocator(e.getMessage());
	    ret.setResult(ValidationResult.VALIDATION_FAILED);
	}

	return ret;
    }

    private String checkParameter(WMSLegendRequest map, Parameter parameter) throws Exception {
	String ret = map.getParameterValue(parameter);
	if (ret == null) {
	    throw new Exception(parameter.getKeys()[0]);
	}
	return ret;

    }

    @Override
    public StreamingOutput getStreamingResponse(WebRequest webRequest) throws GSException {
	return new StreamingOutput() {

	    @Override
	    public void write(OutputStream output) throws IOException, WebApplicationException {

		DataCacheConnector dataCacheConnector = null;

		try {

		    dataCacheConnector = DataCacheConnectorFactory.getDataCacheConnector();

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

		    WMSLegendRequest map = new WMSLegendRequest(webRequest);

		    // String version = checkParameter(map, Parameter.VERSION);
		    String layers = checkParameter(map, Parameter.LAYERS);
		    // String styles = checkParameter(map,Parameter.STYLES);
		    // String crs = checkParameter(map, Parameter.CRS);
		    // String bboxString = checkParameter(map, Parameter.BBOX);
		    String widthString = map.getParameterValue(Parameter.WIDTH);
		    String heightString = map.getParameterValue(Parameter.HEIGHT);
		    String format = map.getParameterValue(Parameter.FORMAT);
		    // String transparent = map.getParameterValue(Parameter.TRANSPARENT);
		    // String bgcolor = map.getParameterValue(Parameter.BGCOLOR);
		    // String exceptions = map.getParameterValue(Parameter.EXCEPTIONS);

		    if (format == null || format.isEmpty()) {
			format = "PNG";
		    }
		    if (format.toLowerCase().contains("png")) {
			format = "PNG";
		    }
		    if (format.toLowerCase().contains("jpeg")) {
			format = "JPEG";
		    }
		    if (format.toLowerCase().contains("jpg")) {
			format = "JPG";
		    }
		    if (format.toLowerCase().contains("gif")) {
			format = "GIF";
		    }

		    List<WMSLayer> wmsLayers = WMSLayer.decode(webRequest.extractViewId());

		    List<InfoLegend> infos = new ArrayList<>();
		    if (!wmsLayers.isEmpty()) {
			for (WMSLayer wmsLayer : wmsLayers) {
			    if (wmsLayer.getLayerName().equals(layers)) {
				infos = wmsLayer.getInfoLegend(layers, null);
				break;
			    }
			}

		    }

		    if (widthString == null || widthString.isEmpty()) {
			widthString = "" + (10 + 10 * WMSLayer.getSize(infos));
		    }
		    if (heightString == null || heightString.isEmpty()) {
			heightString = "" + (20 * infos.size());
		    }

		    Integer width = Integer.parseInt(widthString);
		    Integer height = Integer.parseInt(heightString);

		    BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		    Graphics2D ig2 = bi.createGraphics();

		    ig2.setStroke(new BasicStroke(2));

		    int offset = 0;
		    for (InfoLegend infoLegend : infos) {

			// point
			ig2.setColor(infoLegend.getColor());
			int r = 8;
			int pixMinX = 10;
			int pixMinY = 10;
			ig2.fillOval(pixMinX - r / 2, offset + pixMinY - r / 2, r, r);
			ig2.setColor(Color.gray);
			ig2.drawOval(pixMinX - r / 2, offset + pixMinY - r / 2, r, r);
			ig2.drawString(infoLegend.getLabel(), pixMinX + r + 5, offset + pixMinY + 5);
			offset += 20;

		    }

		    ImageIO.write(bi, format, output);

		} catch (Exception e) {
		    e.printStackTrace();

		}
	    }

	};
    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {

	return new MediaType("image", "png");
    }

}
