package eu.essi_lab.accessor.wms._1_3_0;

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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.xml.xpath.XPathExpressionException;

import eu.essi_lab.accessor.wms.IWMSLayer;
import eu.essi_lab.jaxb.wms._1_3_0.BoundingBox;
import eu.essi_lab.jaxb.wms._1_3_0.Dimension;
import eu.essi_lab.jaxb.wms._1_3_0.EXGeographicBoundingBox;
import eu.essi_lab.jaxb.wms._1_3_0.Keyword;
import eu.essi_lab.jaxb.wms._1_3_0.KeywordList;
import eu.essi_lab.jaxb.wms._1_3_0.Layer;
import eu.essi_lab.jaxb.wms._1_3_0.Style;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.resource.data.CRS;
import net.opengis.gml.v_3_2_0.DirectPositionType;
import net.opengis.gml.v_3_2_0.EnvelopeType;

public class WMS_1_3_0Layer extends IWMSLayer {

    private WMS_1_3_0Capabilities capabilities;
    private Layer layer;
    private List<Layer> hierarchy;
    private static final String EPSG4326 = "EPSG:4326";

    public WMS_1_3_0Layer(WMS_1_3_0Capabilities capabilities, Layer layer) {
	this.capabilities = capabilities;
	Layer rootLayer = capabilities.getCapabilities().getCapability().getLayer();
	this.layer = layer;
	this.hierarchy = capabilities.getHierarchy(rootLayer, layer);
    }

    public WMS_1_3_0Layer(WMS_1_3_0Capabilities capabilities, String name) {
	this.capabilities = capabilities;
	Layer rootLayer = capabilities.getCapabilities().getCapability().getLayer();
	this.layer = capabilities.getLayer(rootLayer, name);
	this.hierarchy = capabilities.getHierarchy(rootLayer, name);
    }

    public String getTitle() {
	String ret = null;
	for (int i = hierarchy.size() - 1; i >= 0; i--) {
	    Layer tmp = hierarchy.get(i);
	    ret = tmp.getTitle();
	    if (ret != null) {
		break;
	    }
	}
	return ret;
    }

    public String getAbstract() {
	String ret = null;
	for (int i = hierarchy.size() - 1; i >= 0; i--) {
	    Layer tmp = hierarchy.get(i);
	    ret = tmp.getAbstract();
	    if (ret != null) {
		break;
	    }
	}
	return ret;
    }

    public List<String> getKeywords() {
	List<String> ret = new ArrayList<>();
	KeywordList lkl = null;
	for (int i = hierarchy.size() - 1; i >= 0; i--) {
	    Layer tmp = hierarchy.get(i);
	    lkl = tmp.getKeywordList();
	    if (lkl != null) {
		break;
	    }
	}
	if (lkl != null) {
	    List<Keyword> keywordList = lkl.getKeywords();
	    for (Keyword keyword : keywordList) {
		ret.add(keyword.getValue());
	    }
	}
	return ret;

    }

    @Override
    public List<EnvelopeType> getEnvelopes() {
	List<EnvelopeType> ret = new ArrayList<>();
	for (int i = hierarchy.size() - 1; i >= 0; i--) {
	    Layer localLayer = hierarchy.get(i);
	    ret = getSingleLayerEnvelopes(localLayer);
	    if (ret != null && !ret.isEmpty()) {
		return ret;
	    }
	}
	return ret;

    }

    private List<EnvelopeType> getSingleLayerEnvelopes(Layer layer) {
	List<EnvelopeType> ret = new ArrayList<>();
	List<BoundingBox> boxes = layer.getBoundingBoxes();
	EXGeographicBoundingBox box4326 = layer.getEXGeographicBoundingBox();
	if (box4326 != null) {
	    BoundingBox bbox = new BoundingBox();
	    bbox.setCRS(EPSG4326);
	    bbox.setMinx(box4326.getSouthBoundLatitude());
	    bbox.setMaxx(box4326.getNorthBoundLatitude());
	    bbox.setMiny(box4326.getWestBoundLongitude());
	    bbox.setMaxy(box4326.getEastBoundLongitude());
	    if (boxes == null) {
		boxes = new ArrayList<>();
	    }
	    boxes.add(bbox);
	}
	if (boxes == null || boxes.isEmpty()) {

	    for (int i = hierarchy.size() - 1; i >= 0; i--) {
		Layer tmp = hierarchy.get(i);
		boxes = tmp.getBoundingBoxes();
		box4326 = tmp.getEXGeographicBoundingBox();
		if (box4326 != null) {
		    BoundingBox bbox = new BoundingBox();
		    bbox.setCRS(EPSG4326);
		    bbox.setMinx(box4326.getSouthBoundLatitude());
		    bbox.setMaxx(box4326.getNorthBoundLatitude());
		    bbox.setMiny(box4326.getWestBoundLongitude());
		    bbox.setMaxy(box4326.getEastBoundLongitude());
		    if (boxes == null) {
			boxes = new ArrayList<>();
		    }
		    boxes.add(bbox);
		}
		if (boxes != null && !boxes.isEmpty()) {
		    break;
		}
	    }
	}
	boolean epsg4326Present = false;
	boolean crs84Present = false;

	if (boxes != null && !boxes.isEmpty()) {
	    for (BoundingBox box : boxes) {
		EnvelopeType envelope = new EnvelopeType();
		envelope.setSrsName(box.getCRS());
		DirectPositionType dpt1 = new DirectPositionType();
		dpt1.getValue().add(box.getMinx());
		dpt1.getValue().add(box.getMiny());
		envelope.setLowerCorner(dpt1);
		DirectPositionType dpt2 = new DirectPositionType();
		dpt2.getValue().add(box.getMaxx());
		dpt2.getValue().add(box.getMaxy());
		envelope.setUpperCorner(dpt2);
		if (envelope.getSrsName().equals(EPSG4326)) {
		    if (!epsg4326Present) {
			epsg4326Present = true;
			ret.add(envelope);
		    }
		} else if (envelope.getSrsName().equals("OGC:CRS84")) {
		    if (!crs84Present) {
			crs84Present = true;

			ret.add(envelope);
		    }
		} else {
		    ret.add(envelope);
		}
	    }
	}

	return ret;
    }

    @Override
    public List<String> getStyleNames() throws XPathExpressionException {
	List<String> ret = new ArrayList<>();
	List<Style> styles = layer.getStyles();
	for (Style style : styles) {
	    ret.add(style.getName());
	}
	return ret;
    }

    @Override
    public List<String> getStyleTitles() throws XPathExpressionException {
	List<String> ret = new ArrayList<>();
	List<Style> styles = layer.getStyles();
	for (Style style : styles) {
	    ret.add(style.getTitle());
	}
	return ret;
    }

    @Override
    public List<String> getCRS() throws XPathExpressionException {
	for (int i = hierarchy.size() - 1; i >= 0; i--) {
	    Layer tmp = hierarchy.get(i);
	    List<String> ret = tmp.getCRS();
	    if (ret != null && !ret.isEmpty()) {
		return ret;
	    }
	}
	return new ArrayList<>();
    }

    @Override
    public void setTitle(String title) {
	getLayer().setTitle(title);

    }

    @Override
    public void setLatLonBoundingBox(double south, double west, double north, double east) {
	EXGeographicBoundingBox bbox = new EXGeographicBoundingBox();
	bbox.setNorthBoundLatitude(north);
	bbox.setSouthBoundLatitude(south);
	bbox.setWestBoundLongitude(west);
	bbox.setEastBoundLongitude(east);
	getLayer().setEXGeographicBoundingBox(bbox);

    }

    @Override
    public void addBoundingBox(String srs, double minx, double miny, double maxx, double maxy) {
	BoundingBox bbox = new BoundingBox();
	bbox.setCRS(srs);
	bbox.setMinx(minx);
	bbox.setMaxx(maxx);
	bbox.setMiny(miny);
	bbox.setMaxy(maxy);
	getLayer().getBoundingBoxes().add(bbox);

    }

    @Override
    public List<String> getFormat() throws XPathExpressionException {
	return capabilities.getFormats();
    }

    @Override
    public void addFormat(String format) {
	if (!capabilities.getFormats().contains(format)) {
	    capabilities.getFormats().add(format);
	}
    }

    @Override
    public void addSRS(String crs) {
	if (!getLayer().getCRS().contains(crs)) {
	    getLayer().getCRS().add(crs);
	}

    }

    @Override
    public void addTimeDimension(String begin, String end, String timeResolution) {
	Dimension timeDimension = null;
	List<Dimension> dimensions = getLayer().getDimensions();

	for (Dimension dimension : dimensions) {
	    if (dimension.getName().equals("time")) {
		timeDimension = dimension;
	    }
	}
	if (timeDimension == null) {
	    timeDimension = new Dimension();
	    timeDimension.setName("time");
	    timeDimension.setUnits("ISO8601");
	    getLayer().getDimensions().add(timeDimension);
	}
	String value = begin + "/" + end;
	if (timeResolution != null) {
	    value = value + "/" + timeResolution;
	}
	timeDimension.setValue(value);
    }

    @Override
    public void addElevationDimension(String elevationMin, String elevationMax, String resolution, String units) {
	Dimension elevationDimension = null;
	List<Dimension> dimensions = getLayer().getDimensions();

	for (Dimension dimension : dimensions) {
	    if (dimension.getName().equals("elevation")) {
		elevationDimension = dimension;
	    }
	}
	if (elevationDimension == null) {
	    elevationDimension = new Dimension();
	    elevationDimension.setName("elevation");
	    elevationDimension.setUnits(units);
	    getLayer().getDimensions().add(elevationDimension);
	}
	String value = elevationMin + "/" + elevationMax;
	if (resolution != null) {
	    value = value + "/" + resolution;
	}
	elevationDimension.setValue(value);
    }

    @Override
    public boolean isSubsettable() throws XPathExpressionException {
	return !layer.isNoSubsets();
    }

    @Override
    public Integer getFixedWidth() throws XPathExpressionException {
	BigInteger ret = layer.getFixedWidth();
	if (ret == null) {
	    return null;
	}
	return ret.intValue();
    }

    @Override
    public Integer getFixedHeight() throws XPathExpressionException {
	BigInteger ret = layer.getFixedHeight();
	if (ret == null) {
	    return null;
	}
	return ret.intValue();
    }

    @Override
    public String getName() {
	return layer.getName();
    }

    @Override
    public Optional<Date> getDefaultPosition() {
	Dimension timeDimension = findDimension("time");
	if (timeDimension != null) {
	    String value = timeDimension.getDefault().trim();
	    if (value.length() < 4) {
		return Optional.empty();
	    }

	    return ISO8601DateTimeUtils.parseISO8601ToDate(value);

	}
	return Optional.empty();
    }

    public Dimension findDimension(String name) {
	List<Dimension> dimensions = layer.getDimensions();
	Dimension ret = null;
	if (dimensions == null || dimensions.isEmpty() || !containsDimension(dimensions, name)) {
	    for (int i = hierarchy.size() - 1; i >= 0; i--) {
		Layer tmp = hierarchy.get(i);
		dimensions = tmp.getDimensions();
		if (dimensions != null && containsDimension(dimensions, name)) {
		    for (Dimension dimension : dimensions) {
			if (dimension.getName().equals(name)) {
			    ret = dimension;
			}
		    }
		    break;
		}
	    }
	} else {
	    for (Dimension dimension : dimensions) {
		if (dimension.getName().equals(name)) {
		    ret = dimension;
		}
	    }
	}
	return ret;
    }

    private boolean containsDimension(List<Dimension> dimensions, String name) {
	for (Dimension dimension : dimensions) {
	    if (dimension.getName().equals(name)) {
		return true;
	    }
	}
	return false;
    }

    @Override
    public String getVersion() {
	return "1.3.0";
    }

    @Override
    public Integer getDefaultWidth() {
	try {

	    List<BoundingBox> boxes = layer.getBoundingBoxes();
	    for (BoundingBox box : boxes) {
		Double resx = box.getResx();
		Double resy = box.getResx();
		double minx = box.getMinx();
		double miny = box.getMiny();
		double maxx = box.getMaxx();
		double maxy = box.getMaxy();
		if (resx != null && resy != null) {
		    String crsString = box.getCRS();
		    CRS crs = CRS.fromIdentifier(crsString);
		    if (crs != null) {
			double totalWidth;
			int size;
			switch (crs.getAxisOrder()) {
			case NORTH_EAST:
			    totalWidth = maxy - miny;
			    size = (int) Math.round(totalWidth / resy);
			    return size;
			case EAST_NORTH:
			default:
			    totalWidth = maxx - minx;
			    size = (int) Math.round(totalWidth / resx);
			    return size;
			}
		    }
		}
	    }
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).warn("Can't get default width", e);
	}
	return null;

    }

    @Override
    public Integer getDefaultHeight() {
	try {

	    List<BoundingBox> boxes = layer.getBoundingBoxes();
	    for (BoundingBox box : boxes) {
		Double resx = box.getResx();
		Double resy = box.getResx();
		double minx = box.getMinx();
		double miny = box.getMiny();
		double maxx = box.getMaxx();
		double maxy = box.getMaxy();
		if (resx != null && resy != null) {
		    String crsString = box.getCRS();
		    CRS crs = CRS.fromIdentifier(crsString);
		    if (crs != null) {
			double totalHeight;
			int size;
			switch (crs.getAxisOrder()) {
			case NORTH_EAST:
			    totalHeight = maxx - minx;
			    size = (int) Math.round(totalHeight / resx);
			    return size;
			case EAST_NORTH:
			default:
			    totalHeight = maxy - miny;
			    size = (int) Math.round(totalHeight / resy);
			    return size;
			}
		    }
		}
	    }

	    Double minScaleDenominator = null;
	    Double maxScaleDenominator = null;
	    minScaleDenominator = layer.getMinScaleDenominator();
	    if (minScaleDenominator == null) {
		for (int i = hierarchy.size() - 1; i >= 0; i--) {
		    Layer tmp = hierarchy.get(i);
		    minScaleDenominator = tmp.getMinScaleDenominator();
		    if (minScaleDenominator != null) {
			break;
		    }
		}
	    }
	    maxScaleDenominator = layer.getMaxScaleDenominator();
	    if (maxScaleDenominator == null) {
		for (int i = hierarchy.size() - 1; i >= 0; i--) {
		    Layer tmp = hierarchy.get(i);
		    maxScaleDenominator = tmp.getMaxScaleDenominator();
		    if (maxScaleDenominator != null) {
			break;
		    }
		}
	    }
	    EnvelopeType envelope4326 = getEnvelope(EPSG4326);
	    if (envelope4326 != null) {

		double northExtent = envelope4326.getUpperCorner().getValue().get(0) - envelope4326.getLowerCorner().getValue().get(0);
		double linearSize = (northExtent * 6378137. * Math.PI) / 180.;
		double pixelSize = 0.00028; // by default, as defined in WMS 1.3.0
		Integer minPixels = null;
		Integer maxPixels = null;
		if (minScaleDenominator != null) {
		    // o.k. it is inverted (min scale -> max pixels)
		    maxPixels = (int) (linearSize / (minScaleDenominator * pixelSize));
		}
		if (maxScaleDenominator != null) {
		    // o.k. it is inverted (max scale -> min pixels)
		    minPixels = (int) (linearSize / (maxScaleDenominator * pixelSize));
		}
		if (maxPixels == null && minPixels != null) {
		    return (int) (minPixels * 1.);
		}
		if (minPixels == null && maxPixels != null) {
		    return (int) (maxPixels * .2);
		}
		if (minPixels != null && maxPixels != null) {
		    return ((maxPixels + minPixels) / 2);
		}
	    }
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).warn("Can't get default height", e);
	}
	return null;
    }

    @Override
    public String getGetMapURL() {
	return capabilities.getGetMapOnlineResource();
    }

    public Layer getLayer() {
	return layer;
    }

    @Override
    public List<IWMSLayer> getChildren() {
	List<IWMSLayer> ret = new ArrayList<>();
	List<Layer> children = this.layer.getLayers();
	for (Layer localLayer : children) {
	    WMS_1_3_0Layer child = new WMS_1_3_0Layer(capabilities, localLayer);
	    ret.add(child);
	}
	return ret;
    }

    @Override
    public Optional<String> getDimensionAxis(String dimensionName) {

	Dimension dimension = findDimension(dimensionName);

	if (dimension != null) {

	    return Optional.ofNullable(dimension.getValue());

	}

	return Optional.empty();
    }

    @Override
    public String getDimensionAxisDefault(String dimensionName) {
	Dimension dimension = findDimension(dimensionName);
	if (dimension != null) {
	    return dimension.getDefault();
	}
	return null;
    }

}
