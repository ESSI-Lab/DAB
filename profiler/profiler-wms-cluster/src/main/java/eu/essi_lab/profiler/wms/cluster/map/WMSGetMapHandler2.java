/**
 * 
 */
package eu.essi_lab.profiler.wms.cluster.map;

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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import com.amazonaws.util.IOUtils;

import eu.essi_lab.access.availability.AvailabilityMonitor;
import eu.essi_lab.access.availability.DownloadInformation;
import eu.essi_lab.access.datacache.BBOX;
import eu.essi_lab.access.datacache.BBOX4326;
import eu.essi_lab.access.datacache.StationRecord;
import eu.essi_lab.api.database.DatabaseExecutor;
import eu.essi_lab.api.database.DatabaseExecutor.WMSClusterRequest;
import eu.essi_lab.api.database.DatabaseExecutor.WMSClusterResponse;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.net.utils.whos.HISCentralOntology;
import eu.essi_lab.lib.net.utils.whos.HydroOntology;
import eu.essi_lab.lib.net.utils.whos.SKOSConcept;
import eu.essi_lab.lib.net.utils.whos.WHOSOntology;
import eu.essi_lab.lib.utils.ExpiringCache;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.SpatialExtent;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.termfrequency.TermFrequencyItem;
import eu.essi_lab.messages.termfrequency.TermFrequencyMap;
import eu.essi_lab.messages.termfrequency.TermFrequencyMap.TermFrequencyTarget;
import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.CRSUtils;
import eu.essi_lab.pdk.handler.StreamingRequestHandler;
import eu.essi_lab.pdk.wrt.WebRequestTransformer;
import eu.essi_lab.profiler.wms.cluster.WMSRequest.Parameter;

/**
 * @author boldrini
 */
public class WMSGetMapHandler2 extends StreamingRequestHandler {

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	ValidationMessage ret = new ValidationMessage();
	try {
	    new WMSMapRequest(request);
	    WMSMapRequest map = new WMSMapRequest(request);
	    String layers = checkParameter(map, Parameter.LAYERS);
	    // String styles = checkParameter(map,Parameter.STYLES);
	    String crs = checkParameter(map, Parameter.CRS);
	    String bboxString = checkParameter(map, Parameter.BBOX);
	    String widthString = checkParameter(map, Parameter.WIDTH);
	    String heightString = checkParameter(map, Parameter.HEIGHT);

	    ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	} catch (Exception e) {
	    ret.setError("Missing mandatory parameter: " + e.getMessage());
	    ret.setLocator(e.getMessage());
	    ret.setResult(ValidationResult.VALIDATION_FAILED);
	}

	return ret;
    }

    public static String checkParameter(WMSMapRequest map, Parameter parameter) throws Exception {
	String ret = map.getParameterValue(parameter);
	if (ret == null) {
	    throw new Exception(parameter.getKeys()[0]);
	}
	return ret;

    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {

	return new MediaType("image", "png");
    }

    public static String decodeFormat(String format) {
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
	return format;
    }

    private static ExpiringCache<Color> availibilityCache = new ExpiringCache<Color>();

    static {
	availibilityCache.setDuration(TimeUnit.MINUTES.toMillis(120));
    }

    public static Color getRandomColorFromSourceId(String sourceId, boolean availability) {
	if (availability) {
	    Color cachedAvailability = availibilityCache.get(sourceId);
	    if (cachedAvailability != null) {
		return cachedAvailability;
	    }
	    DownloadInformation goodInfo = AvailabilityMonitor.getInstance().getLastDownloadDate(sourceId);
	    DownloadInformation badInfo = AvailabilityMonitor.getInstance().getLastFailedDownloadDate(sourceId);
	    Date goodDate = goodInfo == null ? null : goodInfo.getDate();
	    Date failedDate = badInfo == null ? null : badInfo.getDate();
	    if (failedDate != null && (goodDate == null || failedDate.after(goodDate))) {
		// there was an error, and it was the last result
		availibilityCache.put(sourceId, Color.red);
		return Color.red;
	    }
	    // download never done
	    if (goodDate == null) {
		availibilityCache.put(sourceId, Color.blue);
		return Color.blue;
	    } else {
		Calendar calendar = Calendar.getInstance();
		Date now = calendar.getTime(); // Current date

		// Set calendar to one week ago
		calendar.add(Calendar.DAY_OF_YEAR, -7);
		Date oneMonthAgo = calendar.getTime();

		// within last week a download occurred
		if (goodDate.after(oneMonthAgo) && goodDate.before(now)) {
		    availibilityCache.put(sourceId, Color.green);
		    return Color.green;
		} else {
		    // a download occurred more than a week ago
		    availibilityCache.put(sourceId, Color.green.brighter());
		    return Color.green.brighter();
		}
	    }
	}
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
	boolean availability = false;
	try {

	    WMSMapRequest map = new WMSMapRequest(webRequest);

	    String viewId = webRequest.extractViewId().get();

	    Optional<View> view = WebRequestTransformer.findView(ConfigurationWrapper.getStorageInfo(), viewId);

	    String version = checkParameter(map, Parameter.VERSION).isEmpty() ? "1.3.0" : checkParameter(map, Parameter.VERSION);
	    String layers = checkParameter(map, Parameter.LAYERS);
	    if (layers.endsWith("availability")) {
		availability = true;
	    }
	    boolean finalAvailability = availability;
	    // String styles = checkParameter(map,Parameter.STYLES);
	    String crs = checkParameter(map, Parameter.CRS).toUpperCase();
	    String bboxString = checkParameter(map, Parameter.BBOX);
	    String widthString = checkParameter(map, Parameter.WIDTH);
	    String heightString = checkParameter(map, Parameter.HEIGHT);
	    String format = decodeFormat(map.getParameterValue(Parameter.FORMAT));
	    String time = map.getParameterValue(Parameter.TIME);
	    boolean debug = false;
	    // String transparent = map.getParameterValue(Parameter.TRANSPARENT);
	    // String bgcolor = map.getParameterValue(Parameter.BGCOLOR);
	    // String exceptions = map.getParameterValue(Parameter.EXCEPTIONS);

	    Integer width = Integer.parseInt(widthString);
	    Integer height = Integer.parseInt(heightString);

	    Bond constraints = getConstraints(webRequest);

	    return new StreamingOutput() {

		@Override
		public void write(OutputStream output) throws IOException, WebApplicationException {

		    String outputCRS = crs;

		    int minimumClusterSize = 10; // minimum number of stations per cluster
		    int maximumClusterSize = 1000; // minimum number of stations for the biggest pie

		    int stationDiameterInPixels = 8;
		    int minimumClusterDiameterInPixels = 10;
		    int fontSizeInPixels = 10;
		    double maxDiameterRatio = 0.4; // pie diameter with respect to sub image

		    boolean eachPieinEachSubTile = false;

		    try {

			BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

			Graphics2D ig2 = bi.createGraphics();
			ig2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			// we calculate bbox in x-y coordinates (map coordinates) and lat lon
			// coordinates (db coordinates)

			// minx means the horizontal coordinate
			// miny means the vertical coordinate
			// in case of EPSG:4326 the first coordinate is the vertical, so they are
			// inverted

			String[] split = bboxString.split(",");
			BigDecimal minx = null;
			BigDecimal miny = null;
			BigDecimal maxx = null;
			BigDecimal maxy = null;

			if (version.equals("1.3.0") && outputCRS.equals("EPSG:4326")) {
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
			switch (outputCRS) {
			case "EPSG:4326":
			case "CRS:84":
			    if (minx.doubleValue() < -180) {
				minx = new BigDecimal("-180");
			    }
			    if (maxx.doubleValue() > 180) {
				maxx = new BigDecimal("180");
			    }
			    if (miny.doubleValue() < -90) {
				miny = new BigDecimal("-90");
			    }
			    if (maxy.doubleValue() > 90) {
				maxy = new BigDecimal("90");
			    }
			    break;

			default:
			    break;
			}

			if (outputCRS.contains("3857")) {
			    SimpleEntry<Double, Double> lower = new SimpleEntry<>(minx.doubleValue(), miny.doubleValue());
			    SimpleEntry<Double, Double> upper = new SimpleEntry<>(maxx.doubleValue(), maxy.doubleValue());
			    SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> bbox3857 = new SimpleEntry<>(lower,
				    upper);
			    SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> bbox4326 = CRSUtils
				    .translateBBOX(bbox3857, CRS.EPSG_3857(), CRS.EPSG_4326());
			    lower = bbox4326.getKey();
			    upper = bbox4326.getValue();

			}

			if (outputCRS.equals("EPSG:4326")) {
			    outputCRS = "CRS:84";
			}

			double widthGeo = maxx.doubleValue() - minx.doubleValue();
			double heightGeo = maxy.doubleValue() - miny.doubleValue();

			double tol = 0.0000000001;

			StorageInfo uri = ConfigurationWrapper.getStorageInfo();
			DatabaseExecutor executor = DatabaseProviderFactory.getExecutor(uri);

			WMSClusterRequest request = new WMSClusterRequest();

			request.setConstraints(constraints);
			request.setMaxResults(minimumClusterSize);
			request.setMaxTermFrequencyItems(10);
			request.setView(view.get());

			int divisions = 3;

			// the requests are created

			double tmpBboxMinY = miny.doubleValue();
			double bboxWidth = widthGeo / ((double) divisions);
			double tmpBboxMinX = minx.doubleValue();
			double bboxHeight = heightGeo / ((double) divisions);
			for (int i = 0; i < divisions; i++) {
			    for (int j = 0; j < divisions; j++) {
				tmpBboxMinY = miny.doubleValue() + i * bboxHeight;
				tmpBboxMinX = minx.doubleValue() + j * bboxWidth;
				double tmpBboxMaxX = tmpBboxMinX + bboxWidth;
				double tmpBboxMaxY = tmpBboxMinY + bboxHeight;
				// System.out.println("Original bbox: minx " + tmpBboxMinX + " miny " + tmpBboxMinY + "
				// maxx " + tmpBboxMaxX
				// + " maxy " + tmpBboxMaxY);
				if (outputCRS.contains("3857")) {
				    SimpleEntry<Double, Double> lower = new SimpleEntry<>(tmpBboxMinX, tmpBboxMinY);
				    SimpleEntry<Double, Double> upper = new SimpleEntry<>(tmpBboxMaxX, tmpBboxMaxY);
				    SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> bbox3857 = new SimpleEntry<>(
					    lower, upper);
				    SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> bbox4326 = CRSUtils
					    .translateBBOX(bbox3857, CRS.EPSG_3857(), CRS.EPSG_4326());
				    lower = bbox4326.getKey();
				    upper = bbox4326.getValue();
				    tmpBboxMinX = lower.getValue();
				    tmpBboxMinY = lower.getKey();
				    tmpBboxMaxX = upper.getValue();
				    tmpBboxMaxY = upper.getKey();
				}
				if (tmpBboxMinX < -180) {
				    tmpBboxMinX = -180 + tol;
				}
				if (tmpBboxMaxX > 180) {
				    tmpBboxMaxX = 180 - tol;
				}
				if (tmpBboxMinY < -90) {
				    tmpBboxMinY = -90 + tol;
				}
				if (tmpBboxMaxY > 90) {
				    tmpBboxMaxY = 90 - tol;
				}
				String name = "region." + i + "." + j;
				SpatialExtent tmp = new SpatialExtent(tmpBboxMinY, tmpBboxMinX, tmpBboxMaxY, tmpBboxMaxX);
				tmp.setName(name);
				request.addExtent(tmp);
				// System.out.println("Created request with bbox " + tmp);

			    }
			}

			int subImageWidth = width / divisions;
			int subImageHeight = height / divisions;

			List<WMSClusterResponse> responseList = executor.execute(request);

			int bbboxIndex = 0;
			for (WMSClusterResponse response : responseList) {

			    SpatialExtent bbox = response.getBbox();

			    double bboxMinX = bbox.getWest();
			    double bboxMaxX = bbox.getEast();
			    double bboxMaxY = bbox.getNorth();
			    double bboxMinY = bbox.getSouth();

			    if (outputCRS.contains("3857")) {
				SimpleEntry<Double, Double> lower = new SimpleEntry<>(bboxMinY, bboxMinX);
				SimpleEntry<Double, Double> upper = new SimpleEntry<>(bboxMaxY, bboxMaxX);
				SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> bbox4326 = new SimpleEntry<>(lower,
					upper);
				SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> bbox3857 = CRSUtils
					.translateBBOX(bbox4326, CRS.EPSG_4326(), CRS.EPSG_3857());
				lower = bbox3857.getKey();
				upper = bbox3857.getValue();
				bboxMinX = lower.getKey();
				bboxMinY = lower.getValue();
				bboxMaxX = upper.getKey();
				bboxMaxY = upper.getValue();
			    }
			    // the top left pixel of each sub plot
			    int subMinX = getXPixel(width, bboxMinX, minx.doubleValue(), widthGeo);
			    int subMinY = getYPixel(height, bboxMaxY, maxy.doubleValue(), heightGeo);
			    // and the bottom right pixel of each sub plot
			    int subMaxX = getXPixel(width, bboxMaxX, minx.doubleValue(), widthGeo);
			    int subMaxY = getYPixel(height, bboxMinY, maxy.doubleValue(), heightGeo);

			    // blue square per debug
			    if (debug) {
				ig2.setColor(getRandomColorFromSourceId(UUID.randomUUID().toString(), false));
				ig2.setStroke(new BasicStroke(1)); // Slightly thicker line for the border
				ig2.drawRect(subMinX, subMinY, subImageWidth - 2, subImageHeight - 2);
				String debugString = bbboxIndex++ + ": " + bbox;
				ig2.drawString(debugString, subMinX, subMinY + 10);
			    }

			    if (response.getMap().isPresent()) {

				Optional<SpatialExtent> optionalAvgBbox = response.getAvgBbox();

				TermFrequencyMap tfm = response.getMap().get();

//				int totalCount = response.getTotalCount().get();
				int totalCount = response.getStationsCount().get();

				List<TermFrequencyItem> items = tfm.getItems(TermFrequencyTarget.SOURCE);
				List<SimpleEntry<String, Double>> percentages = new ArrayList<SimpleEntry<String, Double>>();
				int tmpTotal = 0;
				for (TermFrequencyItem item : items) {
				    double percentage = (((double) item.getFreq()) / totalCount) * 100.0;
				    tmpTotal += item.getFreq();
				    percentages.add(new SimpleEntry<String, Double>(item.getTerm(), percentage));
				}
				if (tmpTotal < totalCount) {
				    int rest = totalCount - tmpTotal;
				    double percentage = (((double) rest) / totalCount) * 100.0;
				    percentages.add(new SimpleEntry<String, Double>("rest", percentage));
				}
				percentages.sort(new Comparator<SimpleEntry<String, Double>>() {
				    @Override
				    public int compare(SimpleEntry<String, Double> o1, SimpleEntry<String, Double> o2) {
					return o1.getValue().compareTo(o2.getValue());
				    }
				});
				double startAngle = 0.0;
				int maxDiameter = (int) (Math.min(subImageWidth, subImageHeight) * maxDiameterRatio);

				int pieDiameter = (int) (maxDiameter * ((double) totalCount) / ((double) maximumClusterSize));
				if (pieDiameter > maxDiameter) {
				    pieDiameter = maxDiameter;
				}
				if (pieDiameter < minimumClusterDiameterInPixels) {
				    pieDiameter = minimumClusterDiameterInPixels;
				}

				int centroidX = subMinX + subImageWidth / 2;
				int centroidY = subMinY + subImageHeight / 2;

				if (optionalAvgBbox.isPresent()) {
				    SpatialExtent avgBBox = optionalAvgBbox.get();

				    double avgbboxMinX = avgBBox.getWest();
				    double avgbboxMaxX = avgBBox.getEast();
				    double avgbboxMaxY = avgBBox.getNorth();
				    double avgbboxMinY = avgBBox.getSouth();

				    if (outputCRS.contains("3857")) {
					SimpleEntry<Double, Double> lower = new SimpleEntry<>(avgbboxMinY, avgbboxMinX);
					SimpleEntry<Double, Double> upper = new SimpleEntry<>(avgbboxMaxY, avgbboxMaxX);
					SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> bbox4326 = new SimpleEntry<>(
						lower, upper);
					SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> bbox3857 = CRSUtils
						.translateBBOX(bbox4326, CRS.EPSG_4326(), CRS.EPSG_3857());
					lower = bbox3857.getKey();
					upper = bbox3857.getValue();
					avgbboxMinX = lower.getKey();
					avgbboxMinY = lower.getValue();
					avgbboxMaxX = upper.getKey();
					avgbboxMaxY = upper.getValue();
				    }

				    centroidX = getXPixel(width, avgbboxMinX, minx.doubleValue(), widthGeo);
				    centroidY = getYPixel(height, avgbboxMinY, maxy.doubleValue(), heightGeo);

				}
				String centerLabel = "" + totalCount;
				// Draw center label with white background
				ig2.setFont(new Font("SansSerif", Font.BOLD, fontSizeInPixels));
				FontMetrics metrics = ig2.getFontMetrics();
				int labelWidth = metrics.stringWidth(centerLabel);
				int labelHeight = metrics.getHeight();

				int pieMinX = centroidX - pieDiameter / 2;
				int pieMinY = centroidY - pieDiameter / 2;
				int pieMaxX = centroidX + pieDiameter / 2;
				int pieMaxY = centroidY + pieDiameter / 2;

				int labelMaxY = pieMaxY + labelHeight + 1;

				// in these cases the pie is moved, otherwise would partially render outside the
				// tile
				int offsetX = 0;
				int offsetY = 0;
				if (eachPieinEachSubTile) {
				    if (pieMinX < subMinX) {
					offsetX = subMinX - pieMinX;
				    }
				    if (pieMaxX > subMaxX) {
					offsetX = subMaxX - pieMaxX;
				    }
				    if (pieMinY < subMinY) {
					offsetY = subMinY - pieMinY;
				    }
				    if (labelMaxY > subMaxY) {
					offsetY = subMaxY - pieMaxY;
				    }

				} else {
				    if (pieMinX < 0) {
					offsetX = -pieMinX;
				    }
				    if (pieMaxX > width) {
					offsetX = width - pieMaxX;
				    }
				    if (pieMinY < 0) {
					offsetY = -pieMinY;
				    }
				    if (labelMaxY > height) {
					offsetY = height - labelMaxY;
				    }
				}

				pieMinX += offsetX;
				pieMaxX += offsetX;
				pieMinY += offsetY;
				pieMaxY += offsetY;

				for (int i = 0; i < percentages.size(); i++) {
				    double percentage = percentages.get(i).getValue();
				    double arcAngle = 360 * (percentage / 100); // Convert percentage to degrees
				    Color color = getRandomColorFromSourceId(percentages.get(i).getKey(), finalAvailability);
				    ig2.setColor(addTransparency(color));
				    ig2.fillArc(pieMinX, pieMinY, pieDiameter, pieDiameter, (int) Math.round(startAngle),
					    (int) Math.round(arcAngle));
				    startAngle += arcAngle;
				}

				int centerLabelX = pieMinX + pieDiameter / 2;
				int centerLabelY = pieMaxY + labelHeight / 2 + 1;

				// Draw the white background rectangle
				int padding = 1; // Padding around the text
				ig2.setColor(addTransparency(Color.WHITE));
				ig2.fillRoundRect(centerLabelX - labelWidth / 2 - padding, centerLabelY - labelHeight / 2,
					labelWidth + 2 * padding, labelHeight, 5, 5);

				// Draw the black border around the white background
				ig2.setColor(addTransparency(Color.BLACK));
				ig2.drawRoundRect(centerLabelX - labelWidth / 2 - padding, centerLabelY - labelHeight / 2,
					labelWidth + 2 * padding, labelHeight, 5, 5);

				// Draw the label text
				ig2.setColor(addTransparency(Color.BLACK));
				ig2.drawString(centerLabel, centerLabelX - labelWidth / 2, (int) (centerLabelY + labelHeight / 2.8));

				ig2.setColor(addTransparency(Color.BLACK));
				ig2.setStroke(new BasicStroke(1)); // Thin line for the border
				ig2.drawOval(pieMinX, pieMinY, pieDiameter, pieDiameter);

				// ig2.setColor(new Color(0, 0, 0, 40));
				// ig2.setStroke(new BasicStroke(1)); // Slightly thicker line for the border
				// ig2.drawRect(subOffsetX, subOffsetY, subImageWidth - 2, subImageHeight - 2);

				// draws the centroid
				if (debug) {
				    ig2.setStroke(new BasicStroke(2));
				    ig2.setColor(Color.RED);
				    ig2.drawOval(centroidX, centroidY, 9, 9);
				    ig2.setColor(Color.black);
				    ig2.drawOval(centroidX, centroidY, 7, 7);
				    ig2.setColor(Color.white);
				    ig2.drawOval(centroidX, centroidY, 5, 5);
				    ig2.setColor(Color.green);
				    ig2.drawOval(centroidX, centroidY, 3, 3);
				    ig2.setColor(Color.BLACK);
				}
				if (debug) {
				    // draws the crosses
				    if (offsetX != 0 || offsetY != 0) {
					ig2.setStroke(new BasicStroke(2));
					ig2.setColor(addTransparency(Color.gray));
					int crossSize = stationDiameterInPixels / 2;
					ig2.drawLine(centroidX - crossSize, centroidY - crossSize, centroidX + crossSize,
						centroidY + crossSize);
					ig2.drawLine(centroidX - crossSize, centroidY + crossSize, centroidX + crossSize,
						centroidY - crossSize);
					ig2.setColor(addTransparency(Color.gray));
					ig2.drawLine(centroidX, centroidY, centroidX + offsetX, centroidY + offsetY);
				    }
				}

			    } else if (!response.getDatasets().isEmpty()) {

				SpatialExtent extent = new SpatialExtent(miny.doubleValue() + tol, minx.doubleValue() + tol,
					maxy.doubleValue() - tol, maxx.doubleValue() - tol);
				double be = extent.getEast();
				double bw = extent.getWest();
				double bn = extent.getNorth();
				double bs = extent.getSouth();
				double bx = be - bw;
				double by = bn - bs;
				extent.setEast(be - bx / 10.);
				extent.setWest(bw + bx / 10.);
				extent.setNorth(bn - by / 10.);
				extent.setSouth(bs + by / 10.);

				List<Dataset> resultSet = response.getDatasets();
				List<StationRecord> stations = new ArrayList<>();

				for (Dataset res : resultSet) {

				    StationRecord station = new StationRecord();

				    String id = res.getIndexesMetadata().read(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER).get(0);// (res,
				    // "gs:uniquePlatformId");
				    String sourceId = res.getIndexesMetadata().read(ResourceProperty.SOURCE_ID).get();
				    // String sourceLabel = ConfigurationWrapper.getSource(sourceId).getLabel();

				    BigDecimal sb = new BigDecimal(Double.valueOf(
					    res.getIndexesMetadata().readBoundingBox().get().getCardinalValues().get(0).getSouth()));
				    BigDecimal nb = new BigDecimal(Double.valueOf(
					    res.getIndexesMetadata().readBoundingBox().get().getCardinalValues().get(0).getNorth()));
				    BigDecimal wb = new BigDecimal(Double.valueOf(
					    res.getIndexesMetadata().readBoundingBox().get().getCardinalValues().get(0).getWest()));
				    BigDecimal eb = new BigDecimal(Double.valueOf(
					    res.getIndexesMetadata().readBoundingBox().get().getCardinalValues().get(0).getEast()));
				    station.setSourceIdentifier(sourceId);
				    // station.setSourceLabel(sourceLabel);

				    BBOX4326 b = new BBOX4326(sb, nb, wb, eb);
				    station.setBbox4326(b);
				    stations.add(station);
				}

				// draws each station
				for (StationRecord station : stations) {

				    BBOX stationBbbox = station.getBbox4326();

				    Double stationX = stationBbbox.getMinx().doubleValue();
				    Double stationY = stationBbbox.getMiny().doubleValue();

				    if (outputCRS.contains("3857")) {
					SimpleEntry<Double, Double> lower = new SimpleEntry<>(stationY, stationX);
					SimpleEntry<Double, Double> upper = new SimpleEntry<>(stationY, stationX);
					SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> bbox4326 = new SimpleEntry<>(
						lower, upper);
					SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> bbox3857 = CRSUtils
						.translateBBOX(bbox4326, CRS.EPSG_4326(), CRS.EPSG_3857());
					lower = bbox3857.getKey();
					upper = bbox3857.getValue();
					stationX = lower.getKey();
					stationY = lower.getValue();
				    }

				    int stationCenterY = getYPixel(height, stationY, maxy.doubleValue(), heightGeo);
				    int stationCenterX = getXPixel(width, stationX, minx.doubleValue(), widthGeo);

				    ig2.setStroke(new BasicStroke(2));

				    Color color = Color.red;
				    String sourceId = station.getSourceIdentifier();
				    if (sourceId != null) {
					color = getRandomColorFromSourceId(sourceId, finalAvailability);
				    }
				    Color ac = new Color(color.getRed(), color.getGreen(), color.getBlue());
				    ig2.setColor(ac);
				    int stationMinX = stationCenterX - (stationDiameterInPixels) / 2;
				    int stationMinY = stationCenterY - (stationDiameterInPixels) / 2;
				    int stationMaxX = stationCenterX + (stationDiameterInPixels) / 2;
				    int stationMaxY = stationCenterY + (stationDiameterInPixels) / 2;
				    int offsetX = 0;
				    int offsetY = 0;
				    if (stationMinX < 0) {
					offsetX = -stationMinX + 1;
				    }
				    if (stationMaxX > width) {
					offsetX = width - stationMaxX - 1;
				    }
				    if (stationMinY < 0) {
					offsetY = -stationMinY + 1;
				    }
				    if (stationMaxY > height) {
					offsetY = height - stationMaxY - 1;
				    }
				    stationMinX += offsetX;
				    stationMaxX += offsetX;
				    stationMinY += offsetY;
				    stationMaxY += offsetY;

				    ig2.fillOval(stationMinX, stationMinY, stationDiameterInPixels, stationDiameterInPixels);
				    Color g = Color.black;
				    if (offsetX != 0 || offsetY != 0) {
					g = Color.gray;
				    }
				    ig2.setColor(g);
				    if (debug) {
					if (offsetX != 0 || offsetY != 0) {
					    ig2.drawLine(stationCenterX, stationCenterY, stationCenterX + offsetX,
						    stationCenterY + offsetY);
					}
				    }
				    ig2.drawOval(stationMinX, stationMinY, stationDiameterInPixels, stationDiameterInPixels);

				}
			    }

			}

			ImageIO.write(bi, format, output);

		    } catch (Exception e) {
			e.printStackTrace();

		    }

		}

		private int getXPixel(Integer imageWidthInPixel, Double stationX, double imageMinX, double imageWidthX) {
		    return (int) ((stationX - imageMinX) * (imageWidthInPixel / imageWidthX));
		}

		private int getYPixel(Integer imageHeightInPixel, Double stationY, double imageMaxY, double imageHeightY) {
		    return (int) ((imageMaxY - stationY) * (imageHeightInPixel / imageHeightY));
		}

	    };
	} catch (

	Exception e) {
	    e.printStackTrace();
	    return new StreamingOutput() {

		@Override
		public void write(OutputStream output) throws IOException, WebApplicationException {
		    String error = e.getMessage();
		    IOUtils.copy(new ByteArrayInputStream(error.getBytes()), output);
		}

	    };
	}
    }

    private Color addTransparency(Color color) {
	if (color.getTransparency() == 1) {
	    color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 200);
	}
	return color;
    }

    /**
     * @param webRequest
     * @return
     */
    private Bond getConstraints(WebRequest webRequest) {

	String queryString = webRequest.getQueryString();
	KeyValueParser parser = new KeyValueParser(queryString);

	LogicalBond andBond = BondFactory.createAndBond();

	Optional<String> what = parser.getOptionalValue("what");

	if (what.isPresent() && !what.get().equals(KeyValueParser.UNDEFINED)) {

	    LogicalBond orBond = BondFactory.createOrBond();

	    orBond.getOperands().add(BondFactory.createSimpleValueBond(//
		    BondOperator.TEXT_SEARCH, //
		    MetadataElement.TITLE, //
		    what.get()));

	    orBond.getOperands().add(BondFactory.createSimpleValueBond(//
		    BondOperator.TEXT_SEARCH, //
		    MetadataElement.KEYWORD, //
		    what.get()));

	    andBond.getOperands().add(orBond);
	}

	Optional<String> sources = parser.getOptionalValue("sources");

	if (sources.isPresent() && !sources.get().equals(KeyValueParser.UNDEFINED)) {

	    String csSources = sources.get();

	    if (csSources != null && !csSources.isEmpty()) {

		if (csSources.contains(",")) {

		    String[] split = csSources.split(",");
		    LogicalBond orBond = BondFactory.createOrBond();
		    for (String s : split) {
			ResourcePropertyBond sourceBond = BondFactory.createSourceIdentifierBond(s);
			orBond.getOperands().add(sourceBond);
		    }

		    andBond.getOperands().add(orBond);
		} else {
		    ResourcePropertyBond sourceBond = BondFactory.createSourceIdentifierBond(csSources);
		    andBond.getOperands().add(sourceBond);
		}

	    }

	}

	addSimpleValueBonds(parser, "organizationName", andBond, MetadataElement.ORGANISATION_NAME);
	addSimpleValueBonds(parser, "platformTitle", andBond, MetadataElement.PLATFORM_TITLE);
	addSimpleValueBonds(parser, "keyword", andBond, MetadataElement.KEYWORD);

	addAttributeBond(parser, andBond);

	Optional<String> from = parser.getOptionalValue("from");

	if (from.isPresent() && !from.get().equals(KeyValueParser.UNDEFINED)) {

	    andBond.getOperands().add(BondFactory.createSimpleValueBond(//
		    BondOperator.GREATER_OR_EQUAL, //
		    MetadataElement.TEMP_EXTENT_BEGIN, //
		    from.get()));

	}

	Optional<String> to = parser.getOptionalValue("to");

	if (to.isPresent() && !to.get().equals(KeyValueParser.UNDEFINED)) {

	    andBond.getOperands().add(//
		    BondFactory.createSimpleValueBond(//
			    BondOperator.LESS_OR_EQUAL, //
			    MetadataElement.TEMP_EXTENT_BEGIN, //
			    to.get()));

	}

	Optional<String> where = parser.getOptionalValue("where");

	if (where.isPresent() && !where.get().equals(KeyValueParser.UNDEFINED)) {

	    SpatialExtent extent = new SpatialExtent(//
		    Double.valueOf(where.get().split(",")[0]), //
		    Double.valueOf(where.get().split(",")[1]), //
		    Double.valueOf(where.get().split(",")[2]), //
		    Double.valueOf(where.get().split(",")[3]));//

	    Optional<String> spatialOp = parser.getOptionalValue("spatialOp");

	    andBond.getOperands().add(//
		    BondFactory.createSpatialExtentBond(BondOperator.decode(spatialOp.get()), extent));
	}

	Optional<String> instrumentTitle = parser.getOptionalValue("instrumentTitle");
	if (instrumentTitle.isPresent() && !instrumentTitle.get().equals(KeyValueParser.UNDEFINED)) {

	    andBond.getOperands().add(BondFactory.createSimpleValueBond(//
		    BondOperator.EQUAL, //
		    MetadataElement.INSTRUMENT_TITLE, //
		    instrumentTitle.get()));
	}

	Optional<String> platformTitle = parser.getOptionalValue("platformTitle");
	if (platformTitle.isPresent() && !platformTitle.get().equals(KeyValueParser.UNDEFINED)) {

	    andBond.getOperands().add(BondFactory.createSimpleValueBond(//
		    BondOperator.EQUAL, //
		    MetadataElement.PLATFORM_TITLE, //
		    platformTitle.get()));
	}

	Optional<String> observedPropertyURI = parser.getOptionalValue("observedPropertyURI");
	if (observedPropertyURI.isPresent() && !observedPropertyURI.get().equals(KeyValueParser.UNDEFINED)) {

	    andBond.getOperands().add(BondFactory.createSimpleValueBond(//
		    BondOperator.EQUAL, //
		    MetadataElement.OBSERVED_PROPERTY_URI, //
		    observedPropertyURI.get()));
	}

	Optional<String> isValidated = parser.getOptionalValue("isValidated");
	if (isValidated.isPresent() && !isValidated.get().equals(KeyValueParser.UNDEFINED)) {

	    andBond.getOperands().add(BondFactory.createResourcePropertyBond(//
		    BondOperator.EQUAL, //
		    ResourceProperty.IS_VALIDATED, //
		    isValidated.get()));
	}

	 if (andBond.getOperands().isEmpty()) {
	 return null;
	 }
	 if (andBond.getOperands().size() == 1) {
	 return andBond.getOperands().get(0);
	 }
	return andBond;
    }

    private void addAttributeBond(KeyValueParser parser, LogicalBond andBond) {
	Optional<String> propertyString = parser.getOptionalValue("attributeTitle");
	Optional<String> semantics = parser.getOptionalValue("semantics");
	Optional<String> ontology = parser.getOptionalValue("ontology");
	HydroOntology ho = null;
	if (semantics.isPresent() && !semantics.get().equals(KeyValueParser.UNDEFINED)) {
	    if (ontology.isPresent() && !ontology.get().equals(KeyValueParser.UNDEFINED)) {
		switch (ontology.get()) {
		case "whos":
		    ho = new WHOSOntology();
		    break;
		case "his-central":
		    ho = new HISCentralOntology();
		    break;
		default:
		    break;
		}
	    }
	}

	List<String> attributeTitles = new ArrayList<String>();
	LogicalBond orBond = BondFactory.createOrBond();
	if (propertyString.isPresent() && !propertyString.get().equals(KeyValueParser.UNDEFINED)) {

	    String value = propertyString.get();

	    if (value != null && !value.isEmpty()) {
		if (value.contains(",")) {
		    String[] split = value.split(",");

		    for (String s : split) {
			attributeTitles.add(s);
		    }
		} else {
		    attributeTitles.add(value);
		}
	    }
	}

	for (String attributeTitle : attributeTitles) {

	    SimpleValueBond bond = BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.ATTRIBUTE_TITLE,
		    attributeTitle);
	    orBond.getOperands().add(bond);
	    if (ho != null) {
		List<SKOSConcept> concepts = ho.findConcepts(attributeTitle, true, false);
		HashSet<String> uris = new HashSet<String>();
		for (SKOSConcept concept : concepts) {
		    uris.add(concept.getURI());
		}
		for (String uri : uris) {
		    SimpleValueBond b = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.OBSERVED_PROPERTY_URI, uri);
		    orBond.getOperands().add(b);
		}
	    }
	}
	switch (orBond.getOperands().size()) {
	case 0:
	    break;
	case 1:
	    andBond.getOperands().add(orBond.getFirstOperand());
	    break;
	default:
	    andBond.getOperands().add(orBond);

	}

    }

    private void addSimpleValueBonds(KeyValueParser parser, String propertyName, LogicalBond andBond, MetadataElement element) {
	Optional<String> propertyString = parser.getOptionalValue(propertyName);

	if (propertyString.isPresent() && !propertyString.get().equals(KeyValueParser.UNDEFINED)) {

	    String value = propertyString.get();

	    if (value != null && !value.isEmpty()) {

		if (value.contains(",")) {
		    String[] split = value.split(",");
		    LogicalBond orBond = BondFactory.createOrBond();
		    for (String s : split) {
			SimpleValueBond sourceBond = BondFactory.createSimpleValueBond(BondOperator.EQUAL, element, s);
			orBond.getOperands().add(sourceBond);
		    }

		    andBond.getOperands().add(orBond);
		} else {
		    SimpleValueBond sourceBond = BondFactory.createSimpleValueBond(BondOperator.EQUAL, element, value);
		    andBond.getOperands().add(sourceBond);
		}

	    }

	}

    }

}
