/**
 * 
 */
package eu.essi_lab.profiler.wms.cluster.feature.info;

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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;

import eu.essi_lab.access.datacache.BBOX;
import eu.essi_lab.access.datacache.StationRecord;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.SpatialExtent;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.CRSUtils;
import eu.essi_lab.pdk.handler.StreamingRequestHandler;
import eu.essi_lab.pdk.wrt.WebRequestTransformer;
import eu.essi_lab.profiler.wms.cluster.WMSRequest.Parameter;
import eu.essi_lab.request.executor.IDiscoveryStringExecutor;

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
		    int maxRecords = 10;
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

			    if (crs.equals("EPSG:3857")) {

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

			WMSFeatureInfoGenerator generator = new StationFeatureInfoGenerator();

			List<StationRecord> stations = new ArrayList<StationRecord>();

			DiscoveryMessage discoveryMessage = new DiscoveryMessage();
			discoveryMessage.setDistinctValuesElement(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER);
			discoveryMessage.setRequestId(getClass().getSimpleName() + "-" + (new Date().getTime()));
			discoveryMessage.getResourceSelector().setIndexesPolicy(IndexesPolicy.NONE);
			discoveryMessage.getResourceSelector().setSubset(ResourceSubset.NONE);
			discoveryMessage.getResourceSelector().addIndex(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER);
			discoveryMessage.getResourceSelector().addIndex(MetadataElement.PLATFORM_TITLE);
			discoveryMessage.getResourceSelector().setIncludeOriginal(false);
			discoveryMessage.setPage(new Page(1, maxRecords));
			double south = fminy;
			double north = fmaxy;
			double west = fminx;
			double east = fmaxx;
			SpatialExtent extent = new SpatialExtent(south, west, north, east);
			discoveryMessage.setUserBond(BondFactory.createSpatialExtentBond(BondOperator.INTERSECTS, extent));

			discoveryMessage.setSources(ConfigurationWrapper.getHarvestedSources());
			discoveryMessage.setDataBaseURI(ConfigurationWrapper.getDatabaseURI());
			String viewId = webRequest.extractViewId().get();
			Optional<View> view = WebRequestTransformer.findView(ConfigurationWrapper.getDatabaseURI(), viewId);
			WebRequestTransformer.setView(view.get().getId(), ConfigurationWrapper.getDatabaseURI(), discoveryMessage);

			Page userPage = discoveryMessage.getPage();
			userPage.setStart(1);
			userPage.setSize(maxRecords);
			discoveryMessage.setPage(userPage);

			ServiceLoader<IDiscoveryStringExecutor> loader = ServiceLoader.load(IDiscoveryStringExecutor.class);
			IDiscoveryStringExecutor discoveryExecutor = loader.iterator().next();

			ResultSet<String> resultSet = discoveryExecutor.retrieveStrings(discoveryMessage);
			List<String> results = resultSet.getResultsList();
			for (String result : results) {
			    String id = result.substring(result.indexOf("<gs:uniquePlatformId>"), result.indexOf("</gs:uniquePlatformId>"));
			    id = id.replace("<gs:uniquePlatformId>", "");
			    String platformTitle = result.substring(result.indexOf("<gs:platformTitle>"),
				    result.indexOf("</gs:platformTitle>"));
			    platformTitle = platformTitle.replace("<gs:platformTitle>", "");

			    StationRecord station = new StationRecord();
			    station.setPlatformIdentifier(id);
			    station.setPlatformName(platformTitle);
			    station.setDatasetName(platformTitle);
			    stations.add(station);
			}

			InputStream stream = generator.getInfoPage(viewId, stations,resultSet.getCountResponse().getCount(), format, request);

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
