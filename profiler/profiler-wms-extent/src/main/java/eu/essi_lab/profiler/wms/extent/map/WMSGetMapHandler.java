/**
 * 
 */
package eu.essi_lab.profiler.wms.extent.map;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import com.amazonaws.util.IOUtils;

import eu.essi_lab.access.datacache.BBOX;
import eu.essi_lab.access.datacache.DataCacheConnector;
import eu.essi_lab.access.datacache.DataCacheConnectorFactory;
import eu.essi_lab.access.datacache.StationRecord;
import eu.essi_lab.access.datacache.StationsStatistics;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.dc_connector.DataCacheConnectorSetting;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.StreamingRequestHandler;
import eu.essi_lab.profiler.wms.extent.WMSLayer;
import eu.essi_lab.profiler.wms.extent.WMSRequest.Parameter;

/**
 * @author boldrini
 */
public class WMSGetMapHandler extends StreamingRequestHandler {

	private static final HashMap<String, Layer> cachedLayers = new HashMap<>();

	private static final String WMS_GET_MAP_HANDLER_ERROR = "WMS_GET_MAP_HANDLER_ERROR";

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

	private String checkParameter(WMSMapRequest map, Parameter parameter) throws Exception {
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

			if (layers.equals("i-change-monitoring-points") || layers.equals("trigger-monitoring-points")) {
				Layer cachedLayer = getCachedLayer(layers, webRequest.extractViewId());
				if (cachedLayer != null) {
					return cachedLayer.getImageResponse(version, crs, bboxString, width, height, format, time);
				}
			}

			return new StreamingOutput() {

				@Override
				public void write(OutputStream output) throws IOException, WebApplicationException {
					if (layers.equals("i-change-monitoring-points") || layers.equals("trigger-monitoring-points")) {
						BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
						String[] split = bboxString.split(",");
						BigDecimal minx = null;
						BigDecimal miny = null;
						BigDecimal maxx = null;
						BigDecimal maxy = null;

						String crs2 = crs;
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

						double bboxMinx = bminx - w / perc;
						double bboxMiny = bminy - h / perc;
						double bboxMaxx = bmaxx + w / perc;
						double bboxMaxy = bmaxy + h / perc;

						int pixsMinY = getYPixel(height, bboxMiny, bminy, h);
						int pixsMinX = getXPixel(width, bboxMinx, bminx, w);
						int pixsMaxY = getYPixel(height, bboxMaxy, bminy, h);
						int pixsMaxX = getXPixel(width, bboxMaxx, bminx, w);

						Graphics2D ig2 = bi.createGraphics();
						ig2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
						ig2.setColor(Color.LIGHT_GRAY);
						ig2.fillRect(pixsMinX, pixsMaxY, pixsMaxX - pixsMinX, pixsMinY - pixsMaxY);
						ig2.setColor(Color.BLACK);
						ig2.drawString("loading...", width / 2, height / 2);
						ImageIO.write(bi, format, output);
					} else {
						String crs2 = crs;

						try {

							BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

							Graphics2D ig2 = bi.createGraphics();
							ig2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
							int r = 8;

							if (bboxString != null && crs2 != null) {
								String[] split = bboxString.split(",");
								BigDecimal minx = null;
								BigDecimal miny = null;
								BigDecimal maxx = null;
								BigDecimal maxy = null;

								//
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

								double bboxMinx = bminx - w / perc;
								double bboxMiny = bminy - h / perc;
								double bboxMaxx = bmaxx + w / perc;
								double bboxMaxy = bmaxy + h / perc;
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
								BBOX bbox = new BBOX(crs2, bboxMinx, bboxMiny, bboxMaxx, bboxMaxy);

								String[] layerSplit = new String[] {};
								if (layers.contains(",")) {
									layerSplit = layers.split(",");
								} else {
									layerSplit = new String[] { layers };
								}

								List<SimpleEntry<String, String>> properties = new ArrayList<>();

								List<WMSLayer> wmsLayers = WMSLayer.decode(webRequest.extractViewId());
								for (String layer : layerSplit) {
									for (WMSLayer wmsLayer : wmsLayers) {
										if (wmsLayer.getLayerName().equals(layer)) {
											properties.add(
													new SimpleEntry<>(wmsLayer.getProperty(), wmsLayer.getValue()));
										}
									}
								}

								SimpleEntry<String, String>[] props = properties.toArray(new SimpleEntry[] {});
								DataCacheConnector dataCacheConnector = DataCacheConnectorFactory
										.getDataCacheConnector();

								if (dataCacheConnector == null) {
									DataCacheConnectorSetting setting = ConfigurationWrapper
											.getDataCacheConnectorSetting();
									dataCacheConnector = DataCacheConnectorFactory.newDataCacheConnector(setting);
									String cachedDays = setting.getOptionValue(DataCacheConnector.CACHED_DAYS).get();
									String flushInterval = setting.getOptionValue(DataCacheConnector.FLUSH_INTERVAL_MS)
											.get();
									String maxBulkSize = setting.getOptionValue(DataCacheConnector.MAX_BULK_SIZE).get();
									dataCacheConnector.configure(DataCacheConnector.MAX_BULK_SIZE, maxBulkSize);
									dataCacheConnector.configure(DataCacheConnector.FLUSH_INTERVAL_MS, flushInterval);
									dataCacheConnector.configure(DataCacheConnector.CACHED_DAYS, cachedDays);
									DataCacheConnectorFactory.setDataCacheConnector(dataCacheConnector);
								}
								StationsStatistics stats = dataCacheConnector.getStationStatisticsWithProperties(bbox,
										false, props);

								long count = stats.getCount();
								if (count > 8000) {
									Double statminx = null;
									Double statminy = null;
									Double statmaxx = null;
									Double statmaxy = null;
									BBOX statBbbox = null;
									switch (crs2) {
									case "CRS:84":
									case "EPSG:4326":
										statBbbox = stats.getBbox4326();
										break;
									case "EPSG:3857":
									default:
										statBbbox = stats.getBbox3857();
										break;
									}
									statminx = statBbbox.getMinx().doubleValue();
									statminy = statBbbox.getMiny().doubleValue();
									statmaxx = statBbbox.getMaxx().doubleValue();
									statmaxy = statBbbox.getMaxy().doubleValue();

									int pixsMinY = getYPixel(height, statminy, bminy, h);
									int pixsMinX = getXPixel(width, statminx, bminx, w);
									int pixsMaxY = getYPixel(height, statmaxy, bminy, h);
									int pixsMaxX = getXPixel(width, statmaxx, bminx, w);

									ig2.setColor(Color.LIGHT_GRAY);

									ig2.fillRect(pixsMinX, pixsMaxY, pixsMaxX - pixsMinX, pixsMinY - pixsMaxY);
									ig2.setColor(Color.black);

									ig2.drawString("zoom", pixsMinX + (pixsMaxX - pixsMinX) / 2 - 20,
											pixsMinY + (pixsMaxY - pixsMinY) / 2);
									ig2.drawString("please", pixsMinX + (pixsMaxX - pixsMinX) / 2 - 20,
											pixsMinY + (pixsMaxY - pixsMinY) / 2 + 20);
								} else {
									List<StationRecord> stations = new ArrayList<>();

									if (!isEmptyBbox(layers, bbox)) {
										stations = dataCacheConnector.getStationsWithProperties(bbox, 0, 10000, false,
												props);
									} else {
										System.out.println("EMPTY HIT");
									}

									if (stations.isEmpty()) {
										addEmptyBox(layers, bbox);

									}

									for (StationRecord station : stations) {
										Double sminx = null;
										Double sminy = null;
										Double smaxx = null;
										Double smaxy = null;
										BBOX stationBbbox = null;
										switch (crs2) {
										case "CRS:84":
										case "EPSG:4326":
											stationBbbox = station.getBbox4326();
											break;
										case "EPSG:3857":
										default:
											stationBbbox = station.getBbox3857();
											break;
										}
										sminx = stationBbbox.getMinx().doubleValue();
										sminy = stationBbbox.getMiny().doubleValue();
										smaxx = stationBbbox.getMaxx().doubleValue();
										smaxy = stationBbbox.getMaxy().doubleValue();

										int pixMinY = getYPixel(height, sminy, bminy, h);
										int pixMinX = getXPixel(width, sminx, bminx, w);

										ig2.setStroke(new BasicStroke(2));

										WMSLayer wl = null;
										for (WMSLayer wmsLayer : wmsLayers) {
											if (wmsLayer.getLayerName().equals(layerSplit[0])) {
												String property = wmsLayer.getLayerValue(station);
												if (property != null && property.equals(wmsLayer.getValue())) {
													wl = wmsLayer;
													break;
												}
											}
										}

										Color c = wl.getInfoLegend(layers, station).get(0).getColor();
										if (areEquals(sminx, smaxx) && areEquals(sminy, smaxy)) {
											// point
											Color ac = new Color(c.getRed(), c.getGreen(), c.getBlue(), 127);
											ig2.setColor(ac);
											ig2.fillOval(pixMinX - r / 2, pixMinY - r / 2, r, r);
											Color g = Color.gray;
											Color ag = new Color(g.getRed(), g.getGreen(), g.getBlue(), 127);
											ig2.setColor(ag);
											ig2.drawOval(pixMinX - r / 2, pixMinY - r / 2, r, r);

										} else {
											// bbox

											int pixMaxY = height - (int) ((smaxy - bminy) * height / h);
											int pixMaxX = (int) ((smaxx - bminx) * width / w);

											// needs the top left (minx maxy)
											Color ac = new Color(c.getRed(), c.getGreen(), c.getBlue(), 127);
											ig2.setColor(ac);
											ig2.fillRect(pixMinX, pixMaxY, pixMaxX - pixMinX, pixMinY - pixMaxY);
											ig2.setColor(c);
											ig2.drawRect(pixMinX, pixMaxY, pixMaxX - pixMinX, pixMinY - pixMaxY);
										}
									}
								}
							}

							ImageIO.write(bi, format, output);

						} catch (Exception e) {
							e.printStackTrace();

						}
					}

				}

				private int getXPixel(Integer imageWidthPixel, Double stationX, double imageMinX, double imageWidthY) {
					return (int) ((stationX - imageMinX) * imageWidthPixel / imageWidthY);
				}

				private int getYPixel(Integer imageHeightPixel, Double stationY, double imageMinY,
						double imageHeightY) {
					return imageHeightPixel - (int) ((stationY - imageMinY) * imageHeightPixel / imageHeightY);
				}

			};
		} catch (Exception e) {
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

	public static Layer getCachedLayer(String layers, Optional<String> view) {
		synchronized (cachedLayers) {
			Layer cachedLayer = cachedLayers.get(layers);
			if (cachedLayer == null) {
			    	if (view.isPresent()) {
			    	    String v = view.get();
			    	    if (v.equals("i-change")) {
			    		return null;
			    		// skipping cache creation as it fills the memory currently
			    	    }
			    	}
				if (!inPreparation.contains(layers)) {
					inPreparation.add(layers);
					GSLoggerFactory.getLogger(WMSGetMapHandler.class).info("Added layer in preparation: " + layers);
					cachedLayer = new Layer(layers, view);
					cachedLayer.prepare();
					cachedLayers.put(layers, cachedLayer);
				}
			} else {
				if (cachedLayer.isCompleted()) {
					inPreparation.remove(layers);
					return cachedLayer;
				}
			}
		}

		return null;
	}

	private String decodeFormat(String format) {
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
		emptyBoxes.put(layer, boxes);

	}

	public static void getCachedLayer(WMSLayer layer) {
		getCachedLayer(layer.getLayerName(), Optional.of(layer.getView()));

	}

}
