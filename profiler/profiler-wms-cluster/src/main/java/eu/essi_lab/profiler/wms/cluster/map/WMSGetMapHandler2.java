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

			String version = checkParameter(map, Parameter.VERSION).isEmpty() ? "1.3.0"
					: checkParameter(map, Parameter.VERSION);
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

					String outputCRS = crs;
					int max = 10;
					try {

						BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

						Graphics2D ig2 = bi.createGraphics();
						ig2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

						int r = 8;

						// we calculate bbox in x-y coordinates (map coordinates) and lat lon
						// coordinates (db coordinates)

						String[] split = bboxString.split(",");
						BigDecimal minx = null;
						BigDecimal miny = null;
						BigDecimal maxx = null;
						BigDecimal maxy = null;
						BigDecimal west = null;
						BigDecimal east = null;
						BigDecimal south = null;
						BigDecimal north = null;

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
							minx = new BigDecimal(split[1]);
							miny = new BigDecimal(split[0]);
							maxx = new BigDecimal(split[3]);
							maxy = new BigDecimal(split[2]);
						}

						if (outputCRS.equals("CRS:84")) {
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
						}

						south = new BigDecimal(split[0]);
						west = new BigDecimal(split[1]);
						north = new BigDecimal(split[2]);
						east = new BigDecimal(split[3]);

						if (outputCRS.contains("3857")) {
							SimpleEntry<Double, Double> lower = new SimpleEntry<>(minx.doubleValue(),
									miny.doubleValue());
							SimpleEntry<Double, Double> upper = new SimpleEntry<>(maxx.doubleValue(),
									maxy.doubleValue());
							SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> bbox3857 = new SimpleEntry<>(
									lower, upper);
							SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> bbox4326 = CRSUtils
									.translateBBOX(bbox3857, CRS.EPSG_3857(), CRS.EPSG_4326());
							lower = bbox4326.getKey();
							upper = bbox4326.getValue();
							west = new BigDecimal(lower.getValue());
							south = new BigDecimal(lower.getKey());
							east = new BigDecimal(upper.getValue());
							north = new BigDecimal(upper.getKey());

						}

						if (outputCRS.equals("EPSG:4326")) {
							outputCRS = "CRS:84";
						}

						double widthGeo = maxx.doubleValue() - minx.doubleValue();
						double heightGeo = maxy.doubleValue() - miny.doubleValue();
						double perc = 10.0;

						double tol = 0.0000000001;

						DatabaseExecutor executor = DatabaseProviderFactory
								.getDatabaseExecutor(ConfigurationWrapper.getDatabaseURI());

						WMSClusterRequest request = new WMSClusterRequest();

						request.setConstraints(constraints);
						request.setMaxResults(max);
						request.setView(view.get());

						int divisions = 2;

//						the requests are created

						double tmpBboxMinY = miny.doubleValue();
						double bboxWidth = widthGeo / ((double) divisions);
						double tmpBboxMinX = minx.doubleValue();
						double bboxHeight = heightGeo / ((double) divisions);
						for (int i = 0; i < divisions; i++) {
							tmpBboxMinY = miny.doubleValue() + i * bboxHeight;
							for (int j = 0; j < divisions; j++) {
								tmpBboxMinX = minx.doubleValue() + j * bboxWidth;
								double tmpBboxMaxX = tmpBboxMinX + bboxWidth;
								double tmpBboxMaxY = tmpBboxMinY + bboxHeight;
								System.out.println("Original bbox: minx "+tmpBboxMinX+" miny "+tmpBboxMinY+" maxx "+tmpBboxMaxX+" maxy "+tmpBboxMaxY);
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
									tmpBboxMinX = -180+tol;
								}
								if (tmpBboxMaxX > 180) {
									tmpBboxMaxX = 180-tol;
								}
								if (tmpBboxMinY < -90) {
									tmpBboxMinY = -90+tol;
								}
								if (tmpBboxMaxY > 90) {
									tmpBboxMaxY = 90-tol;
								}
								SpatialExtent tmp = new SpatialExtent(tmpBboxMinY, tmpBboxMinX, tmpBboxMaxY,
										tmpBboxMaxX);
								request.addExtent(tmp);
								System.out.println("Created request with bbox "+tmp);

							}
						}

						int subImageWidth = width / divisions;
						int subImageHeight = height / divisions;

						List<WMSClusterResponse> responseList = executor.execute(request);

						for (WMSClusterResponse response : responseList) {

							if (response.getMap().isPresent()) {

								Optional<SpatialExtent> optionalAvgBbox = response.getAvgBbox();

								SpatialExtent bbox = response.getBbox();

								double bboxMinY = bbox.getWest();
								double bboxMaxY = bbox.getEast();
								double bboxMaxX = bbox.getNorth();
								double bboxMinX = bbox.getSouth();

								if (outputCRS.contains("3857")) {
									SimpleEntry<Double, Double> lower = new SimpleEntry<>(bboxMinX, bboxMinY);
									SimpleEntry<Double, Double> upper = new SimpleEntry<>(bboxMaxX, bboxMaxY);
									SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> bbox4326 = new SimpleEntry<>(
											lower, upper);
									SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> bbox3857 = CRSUtils
											.translateBBOX(bbox4326, CRS.EPSG_4326(), CRS.EPSG_3857());
									lower = bbox3857.getKey();
									upper = bbox3857.getValue();
									bboxMinX = lower.getValue();
									bboxMinY = lower.getKey();
									bboxMaxX = upper.getValue();
									bboxMaxY = upper.getKey();
								}

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

								int subOffsetX = getXPixel(width, bboxMinX, minx.doubleValue(), widthGeo);
								int subOffsetY = getYPixel(height, bboxMaxY, maxy.doubleValue(), heightGeo);

								int x = subOffsetX + (subImageWidth - diameter) / 2;
								int y = subOffsetY + (subImageHeight - diameter) / 2;

								for (int i = 0; i < percentages.size(); i++) {
									double percentage = percentages.get(i).getValue();
									double arcAngle = 360 * (percentage / 100); // Convert percentage to degrees
									Color color = getRandomColorFromSourceId(percentages.get(i).getKey());

									// Set slice color
									ig2.setColor(color);

									// Draw slice
									ig2.fillArc(x, y, diameter, diameter, (int) Math.round(startAngle),
											(int) Math.round(arcAngle));

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
								ig2.fillRect(centerLabelX - labelWidth / 2 - padding / 2,
										centerLabelY - labelHeight / 2 - padding / 2, labelWidth + padding,
										labelHeight - padding);

								// Draw the black border around the white background
								ig2.setColor(Color.BLACK);
								ig2.drawRect(centerLabelX - labelWidth / 2 - padding / 2,
										centerLabelY - labelHeight / 2 - padding / 2, labelWidth + padding,
										labelHeight - padding);

								// Draw the label text
								ig2.setColor(Color.BLACK);
								ig2.drawString(centerLabel, centerLabelX - labelWidth / 2, centerLabelY + padding * 4);

								ig2.setColor(Color.BLACK);
								ig2.setStroke(new BasicStroke(1)); // Thin line for the border
								ig2.drawOval(x, y, diameter, diameter);

								ig2.setColor(new Color(0, 0, 0, 40));
								ig2.setStroke(new BasicStroke(1)); // Slightly thicker line for the border
								ig2.drawRect(subOffsetX, subOffsetY, subImageWidth - 2, subImageHeight - 2);

								if (optionalAvgBbox.isPresent()) {
									SpatialExtent avgBBox = optionalAvgBbox.get();

									double avgbboxMinX = avgBBox.getWest();
									double avgbboxMaxX = avgBBox.getEast();
									double avgbboxMaxY = avgBBox.getNorth();
									double avgbboxMinY = avgBBox.getSouth();

									if (outputCRS.contains("3857")) {
										SimpleEntry<Double, Double> lower = new SimpleEntry<>(avgbboxMinX, avgbboxMinY);
										SimpleEntry<Double, Double> upper = new SimpleEntry<>(avgbboxMaxX, avgbboxMaxY);
										SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> bbox4326 = new SimpleEntry<>(
												lower, upper);
										SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> bbox3857 = CRSUtils
												.translateBBOX(bbox4326, CRS.EPSG_4326(), CRS.EPSG_3857());
										lower = bbox3857.getKey();
										upper = bbox3857.getValue();
										avgbboxMinX = lower.getValue();
										avgbboxMinY = lower.getKey();
										avgbboxMaxX = upper.getValue();
										avgbboxMaxY = upper.getKey();
									}
									
									
									int centroidX = getXPixel(width, avgbboxMinX, minx.doubleValue(), widthGeo);
									int centroidY = getYPixel(height, avgbboxMinY, maxy.doubleValue(),
											heightGeo);

//				    int centroidX = subOffsetX + (int) (((avgBBox.getWest() - bbox.getWest()) / tmpLongitude)*subImageWidth);
//				    int centroidY = subOffsetY + (int) (((bbox.getNorth() - avgBBox.getNorth()) / tmpLatitude)*subImageHeight);
									ig2.setColor(Color.RED);
									ig2.drawOval(centroidX, centroidY, 3, 3);
								}

							} else if (!response.getDatasets().isEmpty()) {

								SpatialExtent extent = new SpatialExtent(miny.doubleValue() + tol,
										minx.doubleValue() + tol, maxy.doubleValue() - tol, maxx.doubleValue() - tol);
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

									String id = res.getIndexesMetadata()
											.read(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER).get(0);// (res,
									// "gs:uniquePlatformId");
									String sourceId = res.getIndexesMetadata().read(ResourceProperty.SOURCE_ID).get();

									BigDecimal sb = new BigDecimal(Double.valueOf(res.getIndexesMetadata()
											.readBoundingBox().get().getCardinalValues().get(0).getSouth()));
									BigDecimal nb = new BigDecimal(Double.valueOf(res.getIndexesMetadata()
											.readBoundingBox().get().getCardinalValues().get(0).getNorth()));
									BigDecimal wb = new BigDecimal(Double.valueOf(res.getIndexesMetadata()
											.readBoundingBox().get().getCardinalValues().get(0).getWest()));
									BigDecimal eb = new BigDecimal(Double.valueOf(res.getIndexesMetadata()
											.readBoundingBox().get().getCardinalValues().get(0).getEast()));
									station.setSourceIdentifier(sourceId);

									BBOX4326 b = new BBOX4326(sb, nb, wb, eb);
									station.setBbox4326(b);
									stations.add(station);
								}

								// draws each station
								for (StationRecord station : stations) {

									BBOX stationBbbox = station.getBbox4326();

									Double sminx = stationBbbox.getMinx().doubleValue();
									Double sminy = stationBbbox.getMiny().doubleValue();
									Double smaxx = stationBbbox.getMaxx().doubleValue();
									Double smaxy = stationBbbox.getMaxy().doubleValue();

									if (outputCRS.contains("3857")) {
										SimpleEntry<Double, Double> lower = new SimpleEntry<>(sminx, sminy);
										SimpleEntry<Double, Double> upper = new SimpleEntry<>(smaxx, smaxy);
										SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> bbox4326 = new SimpleEntry<>(
												lower, upper);
										SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> bbox3857 = CRSUtils
												.translateBBOX(bbox4326, CRS.EPSG_4326(), CRS.EPSG_3857());
										lower = bbox3857.getKey();
										upper = bbox3857.getValue();
										sminx = lower.getValue();
										sminy = lower.getKey();
										smaxx = upper.getValue();
										smaxy = upper.getKey();
									}

									int pixMinY = getYPixel(height, sminy, maxy.doubleValue(), heightGeo);
									int pixMinX = getXPixel(width, sminx, minx.doubleValue(), widthGeo);

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

				private int getXPixel(Integer imageWidthInPixel, Double stationX, double imageMinX,
						double imageWidthX) {
					return (int) ((stationX - imageMinX) * (imageWidthInPixel / imageWidthX));
				}

				private int getYPixel(Integer imageHeightInPixel, Double stationY, double imageMaxY,
						double imageHeightY) {
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
