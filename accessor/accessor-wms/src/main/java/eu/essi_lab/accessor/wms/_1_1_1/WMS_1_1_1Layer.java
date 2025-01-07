package eu.essi_lab.accessor.wms._1_1_1;

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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.xml.xpath.XPathExpressionException;

import eu.essi_lab.accessor.wms.IWMSLayer;
import eu.essi_lab.jaxb.wms._1_1_1.BoundingBox;
import eu.essi_lab.jaxb.wms._1_1_1.Dimension;
import eu.essi_lab.jaxb.wms._1_1_1.Extent;
import eu.essi_lab.jaxb.wms._1_1_1.Keyword;
import eu.essi_lab.jaxb.wms._1_1_1.KeywordList;
import eu.essi_lab.jaxb.wms._1_1_1.LatLonBoundingBox;
import eu.essi_lab.jaxb.wms._1_1_1.Layer;
import eu.essi_lab.jaxb.wms._1_1_1.SRS;
import eu.essi_lab.jaxb.wms._1_1_1.Style;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.resource.data.CRS;
import net.opengis.gml.v_3_2_0.DirectPositionType;
import net.opengis.gml.v_3_2_0.EnvelopeType;

public class WMS_1_1_1Layer extends IWMSLayer {

    private WMS_1_1_1Capabilities capabilities;
    private Layer layer;
    private List<Layer> hierarchy;
    private static final String EPSG4326 = "EPSG:4326";

    public WMS_1_1_1Layer(WMS_1_1_1Capabilities capabilities, Layer layer) {
	this.capabilities = capabilities;
	Layer rootLayer = capabilities.getCapabilities().getCapability().getLayer();
	this.layer = layer;
	this.hierarchy = capabilities.getHierarchy(rootLayer, layer);
    }

    public WMS_1_1_1Layer(WMS_1_1_1Capabilities capabilities, String name) {
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
	    List<Keyword> keywordList = lkl.getKeyword();
	    for (Keyword keyword : keywordList) {
		ret.add(keyword.getvalue());
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
	
	// retrieving bboxes from the current layer
	List<BoundingBox> boxes = layer.getBoundingBox();
	LatLonBoundingBox box4326 = layer.getLatLonBoundingBox();
	if (box4326 != null) {
	    BoundingBox bbox = new BoundingBox();
	    bbox.setSRS(EPSG4326);
	    bbox.setMinx(box4326.getMinx());
	    bbox.setMaxx(box4326.getMaxx());
	    bbox.setMiny(box4326.getMiny());
	    bbox.setMaxy(box4326.getMaxy());
	    if (boxes == null) {
		boxes = new ArrayList<>();
	    }
	    boxes.add(bbox);
	}
	// and also from the full hierarchy
	if (boxes == null || boxes.isEmpty()) {

	    for (int i = hierarchy.size() - 1; i >= 0; i--) {
		Layer tmp = hierarchy.get(i);
		boxes = tmp.getBoundingBox();
		box4326 = tmp.getLatLonBoundingBox();
		if (box4326 != null) {
		    BoundingBox bbox = new BoundingBox();
		    bbox.setSRS(EPSG4326);
		    bbox.setMinx(box4326.getMinx());
		    bbox.setMaxx(box4326.getMaxx());
		    bbox.setMiny(box4326.getMiny());
		    bbox.setMaxy(box4326.getMaxy());
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
	if (boxes != null && !boxes.isEmpty()) {
	    for (BoundingBox box : boxes) {
		EnvelopeType envelope = new EnvelopeType();
		DirectPositionType dpt1 = new DirectPositionType();
		DirectPositionType dpt2 = new DirectPositionType();
		envelope.setLowerCorner(dpt1);
		envelope.setUpperCorner(dpt2);
		CRS crs = CRS.fromIdentifier(box.getSRS());
		if (crs.equals(CRS.EPSG_4326())|| crs.equals(CRS.OGC_84())) {
		    dpt1.getValue().add(Double.parseDouble(box.getMiny()));
		    dpt1.getValue().add(Double.parseDouble(box.getMinx()));
		    dpt2.getValue().add(Double.parseDouble(box.getMaxy()));
		    dpt2.getValue().add(Double.parseDouble(box.getMaxx()));
		    envelope.setSrsName(CRS.EPSG_4326().getIdentifier());
		} else  {		    
		    dpt1.getValue().add(Double.parseDouble(box.getMinx()));
		    dpt1.getValue().add(Double.parseDouble(box.getMiny()));
		    dpt2.getValue().add(Double.parseDouble(box.getMaxx()));
		    dpt2.getValue().add(Double.parseDouble(box.getMaxy()));
		    envelope.setSrsName(crs.getIdentifier());
		} 
		ret.add(envelope);
	    }
	}
	return ret;
    }

    @Override
    public List<String> getStyleNames() throws XPathExpressionException {
	List<String> ret = new ArrayList<>();
	List<Style> styles = layer.getStyle();
	for (Style style : styles) {
	    ret.add(style.getName());
	}
	return ret;
    }

    @Override
    public List<String> getStyleTitles() throws XPathExpressionException {
	List<String> ret = new ArrayList<>();
	List<Style> styles = layer.getStyle();
	for (Style style : styles) {
	    ret.add(style.getTitle());
	}
	return ret;
    }

    @Override
    public List<String> getCRS() throws XPathExpressionException {
	for (int i = hierarchy.size() - 1; i >= 0; i--) {
	    Layer tmp = hierarchy.get(i);
	    List<SRS> srses = tmp.getSRS();
	    if (srses != null && !srses.isEmpty()) {
		List<String> ret = new ArrayList<>();
		for (SRS srs : srses) {
		    ret.add(srs.getvalue());
		}
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
	LatLonBoundingBox bbox = new LatLonBoundingBox();
	// EXGeographicBoundingBox bbox = new EXGeographicBoundingBox();
	bbox.setMaxy("" + north);
	bbox.setMiny("" + south);
	bbox.setMinx("" + west);
	bbox.setMaxx("" + east);
	getLayer().setLatLonBoundingBox(bbox);

    }

    @Override
    public void addBoundingBox(String srs, double minx, double miny, double maxx, double maxy) {
	BoundingBox bbox = new BoundingBox();
	bbox.setSRS(srs);
	bbox.setMinx("" + minx);
	bbox.setMaxx("" + maxx);
	bbox.setMiny("" + miny);
	bbox.setMaxy("" + maxy);
	getLayer().getBoundingBox().add(bbox);

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
	boolean found = false;
	for (SRS srs : getLayer().getSRS()) {
	    String value = srs.getvalue();
	    if (value.equals(crs)) {
		found = true;
	    }
	}
	if (!found) {
	    SRS newSrs = new SRS();
	    newSrs.setvalue(crs);
	    getLayer().getSRS().add(newSrs);
	}

    }

    @Override
    public void addTimeDimension(String begin, String end, String timeResolution) {
	Dimension timeDimension = null;
	List<Dimension> dimensions = getLayer().getDimension();

	for (Dimension dimension : dimensions) {
	    if (dimension.getName().equals("time")) {
		timeDimension = dimension;
	    }
	}
	if (timeDimension == null) {
	    timeDimension = new Dimension();
	    timeDimension.setName("time");
	    timeDimension.setUnits("ISO8601");
	    getLayer().getDimension().add(timeDimension);
	}

	List<Extent> extents = getLayer().getExtent();
	Extent timeExtent = null;
	for (Extent extent : extents) {
	    if (extent.getName().equals("time")) {
		timeExtent = extent;
	    }
	}
	if (timeExtent == null) {
	    timeExtent = new Extent();
	    timeExtent.setName("time");
	    extents.add(timeExtent);
	}
	String value = begin + "/" + end;
	if (timeResolution != null) {
	    value = value + "/" + timeResolution;
	}
	timeExtent.setvalue(value);
    }

    @Override
    public void addElevationDimension(String elevationMin, String elevationMax, String resolution, String units) {
	Dimension elevationDimension = null;
	List<Dimension> dimensions = getLayer().getDimension();

	for (Dimension dimension : dimensions) {
	    if (dimension.getName().equals("elevation")) {
		elevationDimension = dimension;
	    }
	}
	if (elevationDimension == null) {
	    elevationDimension = new Dimension();
	    elevationDimension.setName("elevation");
	    getLayer().getDimension().add(elevationDimension);
	}
	elevationDimension.setUnits(units);

	List<Extent> extents = getLayer().getExtent();
	Extent elevationExtent = null;
	for (Extent extent : extents) {
	    if (extent.getName().equals("elevation")) {
		elevationExtent = extent;
	    }
	}
	if (elevationExtent == null) {
	    elevationExtent = new Extent();
	    elevationExtent.setName("elevation");
	    extents.add(elevationExtent);
	}
	String value = elevationMin + "/" + elevationMax;
	if (resolution != null) {
	    value = value + "/" + resolution;
	}
	elevationExtent.setvalue(value);
    }

    @Override
    public boolean isSubsettable() throws XPathExpressionException {
	return layer.getNoSubsets() == null || layer.getNoSubsets().equals("0");
    }

    @Override
    public Integer getFixedWidth() throws XPathExpressionException {
	String ret = layer.getFixedWidth();
	if (ret == null) {
	    return null;
	}
	return Integer.parseInt(ret);
    }

    @Override
    public Integer getFixedHeight() throws XPathExpressionException {
	String ret = layer.getFixedHeight();
	if (ret == null) {
	    return null;
	}
	return Integer.parseInt(ret);
    }

    @Override
    public String getName() {
	return layer.getName();
    }

    @Override
    public Optional<Date> getDefaultPosition() {
	Extent timeExtent = findExtent("time");
	if (timeExtent != null) {
	    String value = timeExtent.getDefault().trim();
	    if (value.length() < 4) {
		return Optional.empty();
	    }

	    return ISO8601DateTimeUtils.parseISO8601ToDate(value);

	}
	return Optional.empty();
    }

    public Extent findExtent(String name) {
	List<Extent> extents = layer.getExtent();
	Extent ret = null;
	if (extents == null || extents.isEmpty() || !containsExtent(extents, name)) {
	    for (int i = hierarchy.size() - 1; i >= 0; i--) {
		Layer tmp = hierarchy.get(i);
		extents = tmp.getExtent();
		if (extents != null && containsExtent(extents, name)) {
		    for (Extent extent : extents) {
			if (extent.getName().equals(name)) {
			    ret = extent;
			}
		    }
		    break;
		}
	    }
	} else {
	    for (Extent extent : extents) {
		if (extent.getName().equals(name)) {
		    ret = extent;
		}
	    }
	}
	return ret;
    }

    private boolean containsExtent(List<Extent> extents, String name) {
	for (Extent extent : extents) {
	    if (extent.getName().equals(name)) {
		return true;
	    }
	}
	return false;
    }

    public Dimension findDimension(String name) {
	List<Dimension> dimensions = layer.getDimension();
	Dimension ret = null;
	if (dimensions == null || dimensions.isEmpty() || !containsDimension(dimensions, name)) {
	    for (int i = hierarchy.size() - 1; i >= 0; i--) {
		Layer tmp = hierarchy.get(i);
		dimensions = tmp.getDimension();
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

	    List<BoundingBox> boxes = layer.getBoundingBox();
	    for (BoundingBox box : boxes) {
		Double resx = box.getResx() == null ? null : Double.parseDouble(box.getResx());
		double minx = Double.parseDouble(box.getMinx());
		double maxx = Double.parseDouble(box.getMaxx());
		if (resx != null) {
		    double totalWidth = maxx - minx;
		    int size = (int) Math.round(totalWidth / resx);
		    return size;
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

	    List<BoundingBox> boxes = layer.getBoundingBox();
	    for (BoundingBox box : boxes) {
		Double resy = box.getResx() == null ? null : Double.parseDouble(box.getResx());
		double miny = Double.parseDouble(box.getMiny());
		double maxy = Double.parseDouble(box.getMaxy());
		if (resy != null) {
		    double totalHeight = maxy - miny;
		    int size = (int) Math.round(totalHeight / resy);
		    return size;
		}
	    }
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).warn("Can't get default width", e);
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
	List<Layer> children = this.layer.getLayer();
	for (Layer localLayer : children) {
	    WMS_1_1_1Layer child = new WMS_1_1_1Layer(capabilities, localLayer);
	    ret.add(child);
	}
	return ret;
    }

    @Override
    public Optional<String> getDimensionAxis(String dimensionName) {

	Extent extent = findExtent(dimensionName);

	if (extent != null) {

	    return Optional.ofNullable(extent.getvalue());

	}

	return Optional.empty();
    }

    @Override
    public String getDimensionAxisDefault(String dimensionName) {
	Extent extent = findExtent(dimensionName);
	if (extent != null) {
	    return extent.getDefault();
	}
	return null;
    }

}
