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
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import com.amazonaws.util.IOUtils;

import eu.essi_lab.access.datacache.BBOX;
import eu.essi_lab.access.datacache.BBOX4326;
import eu.essi_lab.access.datacache.StationRecord;
import eu.essi_lab.api.database.DatabaseExecutor;
import eu.essi_lab.api.database.DatabaseExecutor.WMSClusterRequest;
import eu.essi_lab.api.database.DatabaseExecutor.WMSClusterResponse;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.SpatialExtent;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.termfrequency.TermFrequencyItem;
import eu.essi_lab.messages.termfrequency.TermFrequencyMap;
import eu.essi_lab.messages.termfrequency.TermFrequencyMap.TermFrequencyTarget;
import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.CRSUtils;
import eu.essi_lab.pdk.wrt.WebRequestTransformer;
import eu.essi_lab.profiler.wms.cluster.WMSRequest.Parameter;

/**
 * @author boldrini
 */
public class WMSGetMapHandler2 extends WMSGetMapHandler {

    @Override
    public StreamingOutput getStreamingResponse(WebRequest webRequest) throws GSException {
	try {

	    WMSMapRequest map = new WMSMapRequest(webRequest);

	    String viewId = webRequest.extractViewId().get();

	    Optional<View> view = WebRequestTransformer.findView(ConfigurationWrapper.getDatabaseURI(), viewId);

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

	    LogicalBond constraints = getConstraints(webRequest);

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

			double tol = 0.0000000001;

			SpatialExtent extent = new SpatialExtent(bboxMiny + tol, bboxMinx + tol, bboxMaxy - tol, bboxMaxx - tol);

			DatabaseExecutor executor = DatabaseProviderFactory.getDatabaseExecutor(ConfigurationWrapper.getDatabaseURI());

			WMSClusterRequest request = new WMSClusterRequest();

			request.setConstraints(constraints);
			request.setMaxResults(max);
			request.setView(view.get());

			int divisions = 2;

			double totalWest = extent.getWest();
			double totalSouth = extent.getSouth();
			double totalEast = extent.getEast();
			double totalNorth = extent.getNorth();

			double totalLatitude = totalNorth - totalSouth;
			double totalLongitude = totalEast - totalWest;

			double tmpSouth = totalSouth;
			double tmpWest = totalWest;
			double tmpLatitude = totalLatitude / ((double) divisions);
			double tmpLongitude = totalLongitude / ((double) divisions);
			for (int i = 0; i < divisions; i++) {
			    tmpSouth = totalSouth + i * tmpLatitude;
			    for (int j = 0; j < divisions; j++) {
				tmpWest = totalWest + j * tmpLongitude;
				SpatialExtent tmp = new SpatialExtent(tmpSouth, tmpWest, tmpSouth + tmpLatitude, tmpWest + tmpLongitude);
				request.addExtent(tmp);

			    }
			}

			int subImageWidth = width / divisions;
			int subImageHeight = height / divisions;

			List<WMSClusterResponse> responseList = executor.execute(request);

			for (WMSClusterResponse response : responseList) {

			    if (response.getMap().isPresent()) {

				Optional<SpatialExtent> optionalAvgBbox = response.getAvgBbox();

				SpatialExtent bbox = response.getBbox();

				TermFrequencyMap tfm = response.getMap().get();

				int totalCount = response.getTotalCount().get();
				int stationsCount = response.getStationsCount().get();

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
				int diameter = Math.min(subImageWidth, subImageHeight) - 60; // Keep some padding
				int od = diameter;
				double pd = ((double) totalCount) / 1000;
				diameter = (int) (diameter * pd);
				if (diameter > od) {
				    diameter = od;
				}
				if (diameter < 10) {
				    diameter = 10;
				}

				int subOffsetX = (int) (subImageWidth * (Math.round((bbox.getWest() - totalWest) / totalLongitude)));
				int subOffsetY = (int) (subImageHeight * (Math.round((bbox.getSouth() - totalSouth) / totalLatitude)));

				int x = subOffsetX + (subImageWidth - diameter) / 2;
				int y = subOffsetY + (subImageHeight - diameter) / 2;

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

				String centerLabel = "" + stationsCount;
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
				ig2.setColor(new Color(255, 255, 255, 128));
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

				if (optionalAvgBbox.isPresent()) {
				    SpatialExtent avgBBox = optionalAvgBbox.get();
				    int centroidX = subOffsetX + (int) (avgBBox.getWest() - bbox.getWest()) / subImageWidth;
				    int centroidY = subOffsetY + (int) (avgBBox.getSouth() - bbox.getSouth()) / subImageHeight;
				    ig2.setColor(Color.RED);
				    ig2.drawOval(centroidX, centroidY, 3, 3);
				}

			    } else if (!response.getDatasets().isEmpty()) {

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

				    BigDecimal sb = new BigDecimal(Double.valueOf(
					    res.getIndexesMetadata().readBoundingBox().get().getCardinalValues().get(0).getSouth()));
				    BigDecimal nb = new BigDecimal(Double.valueOf(
					    res.getIndexesMetadata().readBoundingBox().get().getCardinalValues().get(0).getNorth()));
				    BigDecimal wb = new BigDecimal(Double.valueOf(
					    res.getIndexesMetadata().readBoundingBox().get().getCardinalValues().get(0).getWest()));
				    BigDecimal eb = new BigDecimal(Double.valueOf(
					    res.getIndexesMetadata().readBoundingBox().get().getCardinalValues().get(0).getEast()));
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
				    Color ac = new Color(color.getRed(), color.getGreen(), color.getBlue(), 127);
				    ig2.setColor(ac);
				    ig2.fillOval(pixMinX - r / 2, pixMinY - r / 2, r, r);
				    Color g = Color.black;
				    Color ag = new Color(g.getRed(), g.getGreen(), g.getBlue(), 127);
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

    /**
     * @param webRequest
     * @return
     */
    private LogicalBond getConstraints(WebRequest webRequest) {

	String queryString = webRequest.getQueryString();
	KeyValueParser parser = new KeyValueParser(queryString);

	LogicalBond andBond = BondFactory.createAndBond();

	Optional<String> what = parser.getOptionalValue("what");

	if (what.isPresent() && !what.get().equals(KeyValueParser.UNDEFINED)) {

	    LogicalBond orBond = BondFactory.createOrBond();

	    orBond.getOperands().add(BondFactory.createSimpleValueBond(//
		    BondOperator.LIKE, //
		    MetadataElement.TITLE, //
		    what.get()));

	    orBond.getOperands().add(BondFactory.createSimpleValueBond(//
		    BondOperator.LIKE, //
		    MetadataElement.KEYWORD, //
		    what.get()));

	    andBond.getOperands().add(orBond);
	}

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

	Optional<String> attributeTitle = parser.getOptionalValue("attributeTitle");
	if (attributeTitle.isPresent() && !attributeTitle.get().equals(KeyValueParser.UNDEFINED)) {

	    andBond.getOperands().add(BondFactory.createSimpleValueBond(//
		    BondOperator.EQUAL, //
		    MetadataElement.ATTRIBUTE_TITLE, //
		    attributeTitle.get()));
	}

	Optional<String> platformTitle = parser.getOptionalValue("platformTitle");
	if (platformTitle.isPresent() && !platformTitle.get().equals(KeyValueParser.UNDEFINED)) {

	    andBond.getOperands().add(BondFactory.createSimpleValueBond(//
		    BondOperator.EQUAL, //
		    MetadataElement.PLATFORM_TITLE, //
		    platformTitle.get()));
	}

	Optional<String> isValidated = parser.getOptionalValue("isValidated");
	if (isValidated.isPresent() && !isValidated.get().equals(KeyValueParser.UNDEFINED)) {

	    andBond.getOperands().add(BondFactory.createResourcePropertyBond(//
		    BondOperator.EQUAL, //
		    ResourceProperty.IS_VALIDATED, //
		    isValidated.get()));
	}

	return andBond;
    }

}
