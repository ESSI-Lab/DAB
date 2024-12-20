/**
 * 
 */
package eu.essi_lab.profiler.wms.cluster.map;

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
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

import javax.imageio.ImageIO;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import org.hsqldb.TransactionManager2PL;

import com.amazonaws.util.IOUtils;

import eu.essi_lab.access.datacache.BBOX;
import eu.essi_lab.access.datacache.BBOX4326;
import eu.essi_lab.access.datacache.StationRecord;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.SpatialBond;
import eu.essi_lab.messages.bond.SpatialExtent;
import eu.essi_lab.messages.count.CountSet;
import eu.essi_lab.messages.termfrequency.TermFrequencyItem;
import eu.essi_lab.messages.termfrequency.TermFrequencyMap;
import eu.essi_lab.messages.termfrequency.TermFrequencyMap.TermFrequencyTarget;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.CRSUtils;
import eu.essi_lab.pdk.handler.StreamingRequestHandler;
import eu.essi_lab.pdk.wrt.WebRequestTransformer;
import eu.essi_lab.profiler.wms.cluster.WMSRequest.Parameter;
import eu.essi_lab.request.executor.IDiscoveryStringExecutor;

/**
 * @author boldrini
 */
public class WMSGetMapHandler extends StreamingRequestHandler {

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

    private static int maxBoxes = 100;
    private static HashMap<String, List<BBOX>> emptyBoxes = new HashMap<>();

    private static HashSet<String> inPreparation = new HashSet<String>();

    @Override
    public StreamingOutput getStreamingResponse(WebRequest webRequest) throws GSException {
	try {

	    WMSMapRequest map = new WMSMapRequest(webRequest);
	    String view = webRequest.extractViewId().get();
	    String version = checkParameter(map, Parameter.VERSION).isEmpty() ? "1.3.0" : checkParameter(map, Parameter.VERSION);
	    String layers = checkParameter(map, Parameter.LAYERS);
	    // String styles = checkParameter(map,Parameter.STYLES);
	    String crs = checkParameter(map, Parameter.CRS).toUpperCase();
	    String bboxString = checkParameter(map, Parameter.BBOX);
	    String widthString = checkParameter(map, Parameter.WIDTH);
	    String heightString = checkParameter(map, Parameter.HEIGHT);
	    String format = decodeFormat(map.getParameterValue(Parameter.FORMAT));
	    String time = map.getParameterValue(Parameter.TIME);
	    // String transparent = map.getParameterValue(Parameter.TRANSPARENT);
	    // String bgcolor = map.getParameterValue(Parameter.BGCOLOR);
	    // String exceptions = map.getParameterValue(Parameter.EXCEPTIONS);

	    Integer width = Integer.parseInt(widthString);
	    Integer height = Integer.parseInt(heightString);

	    return new StreamingOutput() {

		@Override
		public void write(OutputStream output) throws IOException, WebApplicationException {

		    String crs2 = crs;
		    int max = 10;
		    try {

			BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

			Graphics2D ig2 = bi.createGraphics();
			ig2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			int r = 8;

			String[] split = bboxString.split(",");
			BigDecimal minx = null;
			BigDecimal miny = null;
			BigDecimal maxx = null;
			BigDecimal maxy = null;

			if (version.equals("1.3.0") && crs2.equals("EPSG:4326")) {
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

			if (crs2.contains("3857")) {
			    SimpleEntry<Double, Double> lower = new SimpleEntry<>(minx.doubleValue(), miny.doubleValue());
			    SimpleEntry<Double, Double> upper = new SimpleEntry<>(maxx.doubleValue(), maxy.doubleValue());
			    SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> bbox3857 = new SimpleEntry<>(lower,
				    upper);
			    SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> bbox4326 = CRSUtils
				    .translateBBOX(bbox3857, CRS.EPSG_3857(), CRS.EPSG_4326());
			    lower = bbox4326.getKey();
			    upper = bbox4326.getValue();
			    minx = new BigDecimal(lower.getValue());
			    miny = new BigDecimal(lower.getKey());
			    maxx = new BigDecimal(upper.getValue());
			    maxy = new BigDecimal(upper.getKey());

			}

			if (crs2.equals("EPSG:4326")) {
			    crs2 = "CRS:84";
			}

			double bminx = minx.doubleValue();
			double bminy = miny.doubleValue();
			double bmaxx = maxx.doubleValue();
			double bmaxy = maxy.doubleValue();
			double w = bmaxx - bminx;
			double h = bmaxy - bminy;
			double perc = 10.0;

			double bboxMinx = bminx;
			double bboxMiny = bminy;
			double bboxMaxx = bmaxx;
			double bboxMaxy = bmaxy;
			if (crs2.equals("CRS:84")) {
			    if (bboxMinx < -180) {
				bboxMinx = -180;
			    }
			    if (bboxMaxx > 180) {
				bboxMaxx = 180;
			    }
			    if (bboxMiny < -90) {
				bboxMiny = -90;
			    }
			    if (bboxMaxy > 90) {
				bboxMaxy = 90;
			    }
			}

			ServiceLoader<IDiscoveryStringExecutor> loader = ServiceLoader.load(IDiscoveryStringExecutor.class);
			IDiscoveryStringExecutor executor = loader.iterator().next();

			DiscoveryMessage discoveryMessage = new DiscoveryMessage();
			discoveryMessage.setRequestId(getClass().getSimpleName() + "-" + (new Date().getTime()));
			discoveryMessage.getResourceSelector().setIndexesPolicy(IndexesPolicy.NONE);
			discoveryMessage.getResourceSelector().setSubset(ResourceSubset.NONE);
			discoveryMessage.getResourceSelector().setIncludeOriginal(false);

			discoveryMessage.setPage(new Page(1, max));

			discoveryMessage.setSources(ConfigurationWrapper.getHarvestedSources());
			discoveryMessage.setDataBaseURI(ConfigurationWrapper.getDatabaseURI());

			WebRequestTransformer.setView(view, ConfigurationWrapper.getDatabaseURI(), discoveryMessage);

			double tol = 0.0000000001;
			SpatialExtent se = new SpatialExtent(bboxMiny + tol, bboxMinx + tol, bboxMaxy - tol, bboxMaxx - tol);
			SpatialBond spatial = BondFactory.createSpatialExtentBond(BondOperator.BBOX, se);

			// we are interested only on downloadable datasets
			ResourcePropertyBond accessBond = BondFactory.createIsExecutableBond(true);

			// we are interested only on downloadable datasets
			ResourcePropertyBond downBond = BondFactory.createIsDownloadableBond(true);

			// we are interested only on TIME SERIES datasets
			ResourcePropertyBond timeSeriesBond = BondFactory.createIsTimeSeriesBond(true);

			LogicalBond bond = BondFactory.createAndBond(spatial, timeSeriesBond);

			discoveryMessage.setPermittedBond(bond);
			discoveryMessage.setUserBond(bond);
			discoveryMessage.setNormalizedBond(bond);

			discoveryMessage.setDistinctValuesElement(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER);

			CountSet count = executor.count(discoveryMessage);
			int stationCount = count.getCount();
			int transparency = 200;
			if (stationCount > 0) {

			    List<StationRecord> stations = new ArrayList<>();

			    discoveryMessage.setDistinctValuesElement(null);
			    discoveryMessage.getResourceSelector().addIndex(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER);
			    discoveryMessage.getResourceSelector().addIndex(MetadataElement.BOUNDING_BOX);
			    discoveryMessage.getResourceSelector().addIndex(ResourceProperty.SOURCE_ID);
			    // discoveryMessage.setDistinctValuesElement(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER);
			    List<Queryable> tfList = new ArrayList<Queryable>();
			    tfList.add(ResourceProperty.SOURCE_ID);
			    discoveryMessage.setTermFrequencyTargets(tfList);

			    count = executor.count(discoveryMessage);
			    int totalCount = count.getCount();
			    if (stationCount > max) {
				TermFrequencyMap tfm = count.mergeTermFrequencyMaps(10).get();

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
				int diameter = Math.min(width, height) - 60; // Keep some padding
				int od = diameter;
				double pd = ((double) totalCount) / 1000;
				diameter = (int) (diameter * pd);
				if (diameter > od) {
				    diameter = od;
				}
				if (diameter < 10) {
				    diameter = 10;
				}

				int x = (width - diameter) / 2;
				int y = (height - diameter) / 2;
				for (int i = 0; i < percentages.size(); i++) {
				    double percentage = percentages.get(i).getValue();
				    double arcAngle = 360 * (percentage / 100); // Convert percentage to degrees
				    Color color = getRandomColorFromSourceId(percentages.get(i).getKey());

				    // Set slice color
				    ig2.setColor(color);

				    // Draw slice
				    ig2.fillArc(x, y, diameter, diameter, (int) Math.round(startAngle), (int) Math.round(arcAngle));

				    // Update start angle for the next slice
				    startAngle += arcAngle;
				}

				String centerLabel = "" + stationCount;
				// Draw center label with white background
				ig2.setFont(new Font("SansSerif", Font.BOLD, 10));
				FontMetrics metrics = ig2.getFontMetrics();
				int labelWidth = metrics.stringWidth(centerLabel);
				int labelHeight = metrics.getHeight();

				int centerX = x + diameter / 2;
				int centerY = y + diameter / 2;

				int centerLabelX = x + diameter / 2 + 20;
				int centerLabelY = y + diameter / 2 + 20;

				// Draw the white background rectangle
				int padding = 1; // Padding around the text
				ig2.setColor(new Color(255, 255, 255, transparency));
				ig2.fillRect(centerLabelX - labelWidth / 2 - padding / 2, centerLabelY - labelHeight / 2 - padding / 2,
					labelWidth + padding, labelHeight - padding);

				// Draw the black border around the white background
				ig2.setColor(Color.BLACK);
				ig2.drawRect(centerLabelX - labelWidth / 2 - padding / 2, centerLabelY - labelHeight / 2 - padding / 2,
					labelWidth + padding, labelHeight - padding);

				// Draw the label text
				ig2.setColor(Color.BLACK);
				ig2.drawString(centerLabel, centerLabelX - labelWidth / 2, centerLabelY + padding * 4);

				ig2.setColor(Color.BLACK);
				ig2.setStroke(new BasicStroke(1)); // Thin line for the border
				ig2.drawOval(x, y, diameter, diameter);

				ig2.setColor(new Color(0, 0, 0, 40));
				ig2.setStroke(new BasicStroke(1)); // Slightly thicker line for the border
				ig2.drawRect(0, 0, width - 2, height - 2);

			    } else {

				double be = se.getEast();
				double bw = se.getWest();
				double bn = se.getNorth();
				double bs = se.getSouth();
				double bx = be - bw;
				double by = bn - bs;
				se.setEast(be - bx / 10.);
				se.setWest(bw + bx / 10.);
				se.setNorth(bn - by / 10.);
				se.setSouth(bs + by / 10.);

				ResultSet<String> resultSet = executor.retrieveStrings(discoveryMessage);
				Optional<TermFrequencyMap> frequencyMap = resultSet.getCountResponse()
					.mergeTermFrequencyMaps(discoveryMessage.getMaxFrequencyMapItems());
				for (String res : resultSet.getResultsList()) {
				    StationRecord station = new StationRecord();
				    String id = extractValue(res, "gs:uniquePlatformId");
				    String sourceId = extractValue(res, "gs:sourceId");
				    BigDecimal sb = new BigDecimal(extractValue(res, "gs:south"));
				    BigDecimal nb = new BigDecimal(extractValue(res, "gs:north"));
				    BigDecimal wb = new BigDecimal(extractValue(res, "gs:west"));
				    BigDecimal eb = new BigDecimal(extractValue(res, "gs:east"));
				    station.setSourceIdentifier(sourceId);

				    BBOX4326 b = new BBOX4326(sb, nb, wb, eb);
				    station.setBbox4326(b);
				    stations.add(station);

				}

				// draws each station
				for (StationRecord station : stations) {
				    Double sminx = null;
				    Double sminy = null;
				    Double smaxx = null;
				    Double smaxy = null;
				    BBOX stationBbbox = station.getBbox4326();

				    sminx = stationBbbox.getMinx().doubleValue();
				    sminy = stationBbbox.getMiny().doubleValue();
				    smaxx = stationBbbox.getMaxx().doubleValue();
				    smaxy = stationBbbox.getMaxy().doubleValue();

				    int pixMinY = getYPixel(height, sminy, bminy, h);
				    int pixMinX = getXPixel(width, sminx, bminx, w);

				    ig2.setStroke(new BasicStroke(2));

				    // Color c = wl.getInfoLegend(layers, station).get(0).getColor();

				    Color color = Color.red;
				    String sourceId = station.getSourceIdentifier();
				    if (sourceId != null) {
					color = getRandomColorFromSourceId(sourceId);
				    }
				    // point
				    Color ac = new Color(color.getRed(), color.getGreen(), color.getBlue(), transparency);
				    ig2.setColor(ac);
				    ig2.fillOval(pixMinX - r / 2, pixMinY - r / 2, r, r);
				    Color g = Color.black;
				    Color ag = new Color(g.getRed(), g.getGreen(), g.getBlue(), transparency);
				    ig2.setColor(ag);
				    ig2.drawOval(pixMinX - r / 2, pixMinY - r / 2, r, r);

				}
			    }
			}

			ImageIO.write(bi, format, output);

		    } catch (Exception e) {
			e.printStackTrace();

		    }

		}

		private String extractValue(String res, String key) {
		    return res.substring(res.indexOf("<" + key + ">"), res.indexOf("</" + key + ">")).replace("<" + key + ">", "");
		}

		private int getXPixel(Integer imageWidthPixel, Double stationX, double imageMinX, double imageWidthY) {
		    return (int) ((stationX - imageMinX) * imageWidthPixel / imageWidthY);
		}

		private int getYPixel(Integer imageHeightPixel, Double stationY, double imageMinY, double imageHeightY) {
		    return imageHeightPixel - (int) ((stationY - imageMinY) * imageHeightPixel / imageHeightY);
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

    public static Color getRandomColorFromSourceId(String sourceId) {
	int transparency = 200;
	String hexacode = null;
	switch (sourceId) {
	case "ita-sir-toscana":
	    hexacode = "#e30613";
	    break;
	case "hisCentralItaMarche":
	    hexacode = "#ffffff";
	    break;
	case "hisCentralItaFriuli":
	    hexacode = "#0063e7";
	    break;
	case "ita-sir-veneto":
	    hexacode = "#c84a46";
	    break;
	case "hisCentralItaLazio":
	    hexacode = "#0075d0";
	    break;
	case "hisCentralItaAosta":
	    hexacode = "#000000";
	    break;
	case "hisCentralItaPiemonte":
	    hexacode = "#d5001d";
	    break;
	case "hisCentralItaLiguria":
	    hexacode = "#009cce";
	    break;
	case "hisCentralItaBolzano":
	    hexacode = "#000000";
	    break;
	case "ita-sir-lombardia":
	    hexacode = "#00a040";
	    break;
	case "ita-sir-emilia-romagna":
	    hexacode = "#009a49";
	    break;
	case "ita-sir-sardegna":
	    hexacode = "#d80000";
	    break;
	case "ita-sir-basilicata":
	    hexacode = "#2c4878";
	    break;
	case "ita-sir-umbria":
	    hexacode = "#00943a";
	    break;

	default:
	    break;
	}
	if (hexacode != null) {
	    Color color = Color.decode(hexacode);
	    Color transparentColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), transparency);
	    return transparentColor;
	}
	int hash = sourceId.hashCode();
	int r = (hash & 0xFF0000) >> 16;
	int g = (hash & 0x00FF00) >> 8;
	int b = (hash & 0x0000FF);
	return new Color(r, g, b, transparency);
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

    @Override
    public MediaType getMediaType(WebRequest webRequest) {

	return new MediaType("image", "png");
    }

    private boolean areEquals(double s, double n) {
	return Math.abs(s - n) < 0.0000001d;
    }

    static synchronized boolean isEmptyBbox(String layer, BBOX bbox) {

	List<BBOX> boxes = emptyBoxes.get(layer);
	if (boxes == null) {
	    return false;
	}
	for (BBOX empty : boxes) {
	    if (empty.contains(bbox)) {
		return true;
	    }
	}
	return false;
    }

    static synchronized void addEmptyBox(String layer, BBOX bbox) {
	List<BBOX> boxes = emptyBoxes.get(layer);
	if (boxes == null) {
	    boxes = new ArrayList<>();
	}
	for (BBOX empty : boxes) {
	    if (empty.contains(bbox)) {
		// nothing to do, a bigger bbox is already present
		return;
	    }
	}
	boxes.add(bbox);
	if (boxes.size() > maxBoxes) {
	    boxes.sort(new Comparator<BBOX>() {

		@Override
		public int compare(BBOX o1, BBOX o2) {
		    Double area1 = o1.getArea();
		    Double area2 = o2.getArea();
		    return area2.compareTo(area1);
		}
	    });
	    boxes.remove(0);
	}
	System.out.println("Added empty: " + boxes.size());
	emptyBoxes.put(layer, boxes);

    }

}
