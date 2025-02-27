/**
 * 
 */
package eu.essi_lab.profiler.wms.cluster.legend;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.StreamingRequestHandler;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;
import eu.essi_lab.profiler.wms.cluster.WMSClusterProfilerSetting;
import eu.essi_lab.profiler.wms.cluster.WMSRequest.Parameter;
import eu.essi_lab.profiler.wms.cluster.map.WMSGetMapHandler2;

/**
 * @author boldrini
 */
public class WMSGetLegendHandler extends StreamingRequestHandler {

    private int fontSize;
    private int lineSize;
    private int fontStyle;

    /**
     * @param setting
     */
    public WMSGetLegendHandler(WMSClusterProfilerSetting setting) {

	Properties properties = setting.getKeyValueOptions().orElse(new Properties());
	fontSize = Integer.valueOf(properties.getOrDefault("legendFontSize", "13").toString());
	lineSize = Integer.valueOf(properties.getOrDefault("legendLineSize", "20").toString());
	fontStyle = properties.getOrDefault("legendFontStyle", "plain").toString().equals("plain") ? Font.PLAIN : Font.BOLD;
    }

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	ValidationMessage ret = new ValidationMessage();
	try {
	    new WMSLegendRequest(request);
	    WMSLegendRequest map = new WMSLegendRequest(request);
	    String layers = checkParameter(map, Parameter.LAYERS);

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
    
    public static Color getRandomColorFromSourceId(String sourceId,boolean availability) {
   	int transparency = 200;
   	String hexacode = null;
   	// switch (sourceId) {
   	// case "ita-sir-toscana":
   	// hexacode = "#e30613";
   	// break;
   	// case "hisCentralItaMarche":
   	// hexacode = "#ffffff";
   	// break;
   	// case "hisCentralItaFriuli":
   	// hexacode = "#0063e7";
   	// break;
   	// case "ita-sir-veneto":
   	// hexacode = "#c84a46";
   	// break;
   	// case "hisCentralItaLazio":
   	// hexacode = "#0075d0";
   	// break;
   	// case "hisCentralItaAosta":
   	// hexacode = "#000000";
   	// break;
   	// case "hisCentralItaPiemonte":
   	// hexacode = "#d5001d";
   	// break;
   	// case "hisCentralItaLiguria":
   	// hexacode = "#009cce";
   	// break;
   	// case "hisCentralItaBolzano":
   	// hexacode = "#000000";
   	// break;
   	// case "ita-sir-lombardia":
   	// hexacode = "#00a040";
   	// break;
   	// case "ita-sir-emilia-romagna":
   	// hexacode = "#009a49";
   	// break;
   	// case "ita-sir-sardegna":
   	// hexacode = "#d80000";
   	// break;
   	// case "ita-sir-basilicata":
   	// hexacode = "#2c4878";
   	// break;
   	// case "ita-sir-umbria":
   	// hexacode = "#00943a";
   	// break;
   	//
   	// default:
   	// break;
   	// }
   	// if (hexacode != null) {
   	// Color color = Color.decode(hexacode);
   	// Color transparentColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), transparency);
   	// return transparentColor;
   	// }
   	int hash = sourceId.hashCode();
   	// int r = (hash & 0xFF0000) >> 16;
   	// int g = (hash & 0x00FF00) >> 8;
   	// int b = (hash & 0x0000FF);
   	// return new Color(r, g, b, transparency);
   	Random random = new Random(hash);
   	final float hue = random.nextFloat();
   	final float saturation = 0.5f + 0.6f * random.nextFloat();// 1.0 for brilliant, 0.0 for dull

   	final float luminance = 0.5f + 0.6f * random.nextFloat(); // 1.0 for brighter, 0.0 for black
   	return Color.getHSBColor(hue, saturation, luminance);
       }

    @Override
    public StreamingOutput getStreamingResponse(WebRequest webRequest) throws GSException {
	return new StreamingOutput() {

	    @Override
	    public void write(OutputStream output) throws IOException, WebApplicationException {

		// DataCacheConnector dataCacheConnector = null;

		try {

		    // dataCacheConnector = DataCacheConnectorFactory.getDataCacheConnector();
		    //
		    // if (dataCacheConnector == null) {
		    // DataCacheConnectorSetting setting = ConfigurationWrapper.getDataCacheConnectorSetting();
		    // dataCacheConnector = DataCacheConnectorFactory.newDataCacheConnector(setting);
		    // String cachedDays = setting.getOptionValue(DataCacheConnector.CACHED_DAYS).get();
		    // String flushInterval = setting.getOptionValue(DataCacheConnector.FLUSH_INTERVAL_MS).get();
		    // String maxBulkSize = setting.getOptionValue(DataCacheConnector.MAX_BULK_SIZE).get();
		    // dataCacheConnector.configure(DataCacheConnector.MAX_BULK_SIZE, maxBulkSize);
		    // dataCacheConnector.configure(DataCacheConnector.FLUSH_INTERVAL_MS, flushInterval);
		    // dataCacheConnector.configure(DataCacheConnector.CACHED_DAYS, cachedDays);
		    // DataCacheConnectorFactory.setDataCacheConnector(dataCacheConnector);
		    // }

		    WMSLegendRequest map = new WMSLegendRequest(webRequest);

		    // String version = checkParameter(map, Parameter.VERSION);
		    String layers = checkParameter(map, Parameter.LAYERS);
		    boolean availability = false;
		    if (layers.endsWith("availability")) {
			availability = true;
		    }
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

		    int r = 15;
		    int pixMinX = 10;
		    int pixMinY = 10;

		    boolean drawIcon = true;

		    String viewId = webRequest.extractViewId().get();

		    List<InfoLegend> infos = new ArrayList<>();

		    Optional<View> view = DiscoveryRequestTransformer.findView(ConfigurationWrapper.getStorageInfo(), viewId);

		    List<GSSource> sources = ConfigurationWrapper.getViewSources(view.get());

		    Font font = new Font("SansSerif", fontStyle, fontSize);
		    BufferedImage testBI = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
		    Graphics2D igTest = testBI.createGraphics();
		    igTest.setFont(font);
		    FontMetrics metrics = igTest.getFontMetrics();
		    for (GSSource source : sources) {
			String label = source.getLabel();
			String sourceId = source.getUniqueIdentifier();
			if (sourceId.startsWith("ita-sir") || sourceId.toLowerCase().contains("hiscentral")) {
			    label = label.replace("Italy, Sistema Informativo Regionale", "");
			    label = label.replace("Italy, Sistema Informativo della Provincia Autonoma di", "");
			    if (label.startsWith(",")) {
				label = label.substring(1);
			    }
			    label = label.trim();
			}
			InfoLegend info = new InfoLegend(WMSGetMapHandler2.getRandomColorFromSourceId(sourceId, availability), label);
			infos.add(info);
		    }

		    infos.sort(new Comparator<InfoLegend>() {

			@Override
			public int compare(InfoLegend o1, InfoLegend o2) {
			    return o1.getLabel().compareTo(o2.getLabel());
			}
		    });

		    int maxLengthInPixels = 0;
		    for (InfoLegend info : infos) {
			int l = metrics.stringWidth(info.getLabel());
			if (l > maxLengthInPixels) {
			    maxLengthInPixels = l;
			}
		    }

		    int initialGap = pixMinX + r + 5;

		    if (!drawIcon) {
			initialGap = 0;
		    }
		    // if (widthString == null || widthString.isEmpty()) {
		    // for (InfoLegend info : infos) {
		    // int size = info.getLabel().length();
		    // if (size > maxLength) {
		    // maxLength = size;
		    // }
		    // }
		    widthString = "" + (maxLengthInPixels + initialGap);
		    // }
		    if (heightString == null || heightString.isEmpty()) {
			heightString = "" + lineSize * infos.size();
		    }

		    Integer width = Integer.parseInt(widthString);
		    Integer height = Integer.parseInt(heightString) - lineSize;

		    BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		    Graphics2D ig2 = bi.createGraphics();
		    ig2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		    ig2.setFont(new Font("SansSerif", fontStyle, fontSize));

		    ig2.setStroke(new BasicStroke(2));

		    int offset = 10;

		    for (InfoLegend infoLegend : infos) {

			// point
			if (drawIcon) {
			    ig2.setColor(infoLegend.getColor());
			    ig2.fillOval(pixMinX - r / 2, offset + pixMinY - r, r, r);
			    ig2.setColor(Color.black);
			    ig2.drawOval(pixMinX - r / 2, offset + pixMinY - r, r, r);
			}
			ig2.setColor(Color.black);
			ig2.drawString(infoLegend.getLabel(), initialGap, offset + pixMinY);
			offset += (fontSize * 1.4);
		    }

		    ImageIO.write(bi, format, output);

		} catch (Exception e) {

		    GSLoggerFactory.getLogger(getClass()).error(e);
		}
	    }
	};
    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {

	return new MediaType("image", "png");
    }

}
