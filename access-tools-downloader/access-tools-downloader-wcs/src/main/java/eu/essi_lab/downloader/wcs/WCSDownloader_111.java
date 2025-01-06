package eu.essi_lab.downloader.wcs;

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

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

import com.google.common.collect.Lists;

import eu.essi_lab.accessor.wcs.WCSConnector;
import eu.essi_lab.accessor.wcs_1_1_1.WCSConnector_111;
import eu.essi_lab.lib.net.protocols.NetProtocol;
import eu.essi_lab.lib.net.protocols.NetProtocols;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.data.AxisOrder;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataFormat;

public class WCSDownloader_111 extends WCSDownloader {

    @Override
    public WCSConnector createConnector() {
	return new WCSConnector_111();
    }

    public WCSConnector_111 getConnector() {
	return (WCSConnector_111) connector;
    }

    protected void setConnector(WCSConnector_111 connector) {
	this.connector = connector;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    @Override
    public boolean canDownload() {

	NetProtocol protocol = NetProtocols.decodeFromIdentifier(online.getProtocol());

	return NetProtocols.WCS_1_1_1.equals(protocol);
    }

    public String getVersionParameter() {
	return "1.1.1";
    }

    protected Set<DataFormat> getFormats(XMLDocumentReader coverage) {
	HashSet<DataFormat> formats = new HashSet<DataFormat>();
	Node[] formatNodes;
	try {
	    formatNodes = coverage.evaluateNodes("*:CoverageDescriptions/*:CoverageDescription/*:SupportedFormat");
	} catch (XPathExpressionException e) {
	    return formats;
	}
	for (Node formatNode : formatNodes) {
	    String format = null;
	    try {
		format = coverage.evaluateString(formatNode, ".");
	    } catch (XPathExpressionException e) {
	    }
	    if (format != null) {
		DataFormat decodedFormat = DataFormat.fromIdentifier(format);
		formats.add(decodedFormat);

	    }
	}

	possiblyTagGeoTiffFormat(formats);

	return formats;
    }

    protected Set<CRS> getBoundingBoxCRSes(XMLDocumentReader coverage) {
	Set<CRS> crses = new HashSet<>();
	Node[] crsNodes;
	try {
	    crsNodes = coverage.evaluateNodes("*:CoverageDescriptions/*:CoverageDescription/*:Domain/*:SpatialDomain/*:BoundingBox/@crs");

	} catch (XPathExpressionException e) {
	    return crses;
	}
	for (Node crsNode : crsNodes) {
	    String crs = null;
	    try {
		crs = coverage.evaluateString(crsNode, ".");
	    } catch (XPathExpressionException e) {
	    }
	    if (crs != null) {
		crses.add(CRS.fromIdentifier(crs));
	    }
	}
	return crses;
    }

    protected Set<CRS> getResponseCRSes(XMLDocumentReader coverage) {
	Set<CRS> crses = new HashSet<>();
	Node[] crsNodes;
	try {
	    crsNodes = coverage.evaluateNodes("*:CoverageDescriptions/*:CoverageDescription/*:SupportedCRS");
	    if (crsNodes.length == 0) {
		// fall back
		crsNodes = coverage
			.evaluateNodes("*:CoverageDescriptions/*:CoverageDescription/*:Domain/*:SpatialDomain/*:GridCRS/*:GridBaseCRS");
	    }
	} catch (XPathExpressionException e) {
	    return crses;
	}
	for (Node crsNode : crsNodes) {
	    String crs = null;
	    try {
		crs = coverage.evaluateString(crsNode, ".");
	    } catch (XPathExpressionException e) {
	    }
	    if (crs != null) {
		crses.add(CRS.fromIdentifier(crs));
	    }
	}
	return crses;
    }

    @Override
    protected String getCoverageName(XMLDocumentReader coverage) {
	try {
//	    return coverage.evaluateString("*:CoverageDescription/*:CoverageOffering/*:name");
	    return coverage.evaluateString("*:CoverageDescriptions/*:CoverageDescription/*:Identifier");
	} catch (XPathExpressionException e) {
	    return null;
	}
    }

    private String retrieveBBOXCrsIdentifier(XMLDocumentReader coverage, CRS crs) {
	try {
	    Node[] bboxCrsNodes = coverage
		    .evaluateNodes("*:CoverageDescriptions/*:CoverageDescription/*:Domain/*:SpatialDomain/*:BoundingBox/@crs");
	    for (Node bboxCrsNode : bboxCrsNodes) {
		String bboxCrsString = coverage.evaluateString(bboxCrsNode, ".");
		CRS tmpCRS = CRS.fromIdentifier(bboxCrsString);
		if (crs.equals(tmpCRS)) {
		    return tmpCRS.getIdentifier();
		}
	    }
	} catch (XPathExpressionException e1) {
	    return null;
	}
	return null;
    }

    @Override
    protected Double getLowerCornerFirstDimension(XMLDocumentReader coverage, CRS crs) {
	String identifier = retrieveBBOXCrsIdentifier(coverage, crs);
	if (identifier == null) {
	    return null;
	}
	String lowerCorner;
	try {
	    lowerCorner = coverage
		    .evaluateString("*:CoverageDescriptions/*:CoverageDescription/*:Domain/*:SpatialDomain/*:BoundingBox[@crs='"
			    + identifier + "']/*:LowerCorner");
	} catch (XPathExpressionException e) {
	    return null;
	}
	Double ret = Double.parseDouble(lowerCorner.split(" ")[0]);
	return ret;
    }

    @Override
    protected Double getLowerCornerSecondDimension(XMLDocumentReader coverage, CRS crs) {
	String identifier = retrieveBBOXCrsIdentifier(coverage, crs);
	if (identifier == null) {
	    return null;
	}
	String lowerCorner;
	try {
	    lowerCorner = coverage
		    .evaluateString("*:CoverageDescriptions/*:CoverageDescription/*:Domain/*:SpatialDomain/*:BoundingBox[@crs='"
			    + identifier + "']/*:LowerCorner");
	} catch (XPathExpressionException e) {
	    return null;
	}
	Double ret = Double.parseDouble(lowerCorner.split(" ")[1]);
	return ret;
    }

    @Override
    protected Double getUpperCornerFirstDimension(XMLDocumentReader coverage, CRS crs) {
	String identifier = retrieveBBOXCrsIdentifier(coverage, crs);
	if (identifier == null) {
	    return null;
	}
	String upperCorner;
	try {
	    upperCorner = coverage
		    .evaluateString("*:CoverageDescriptions/*:CoverageDescription/*:Domain/*:SpatialDomain/*:BoundingBox[@crs='"
			    + identifier + "']/*:UpperCorner");
	} catch (XPathExpressionException e) {
	    return null;
	}
	Double ret = Double.parseDouble(upperCorner.split(" ")[0]);
	return ret;
    }

    @Override
    protected Double getUpperCornerSecondDimension(XMLDocumentReader coverage, CRS crs) {
	String identifier = retrieveBBOXCrsIdentifier(coverage, crs);
	if (identifier == null) {
	    return null;
	}
	String upperCorner;
	try {
	    upperCorner = coverage
		    .evaluateString("*:CoverageDescriptions/*:CoverageDescription/*:Domain/*:SpatialDomain/*:BoundingBox[@crs='"
			    + identifier + "']/*:UpperCorner");
	} catch (XPathExpressionException e) {
	    return null;
	}
	Double ret = Double.parseDouble(upperCorner.split(" ")[1]);
	return ret;
    }

    @Override
    public String getSpatialSubsetParameter(XMLDocumentReader coverage,
	    SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> userLowerAndUpperCorners, CRS crs) {
	SimpleEntry<Double, Double> lowerCorner = userLowerAndUpperCorners.getKey();
	SimpleEntry<Double, Double> upperCorner = userLowerAndUpperCorners.getValue();
	Double min1 = lowerCorner.getKey();
	Double min2 = lowerCorner.getValue();
	Double max1 = upperCorner.getKey();
	Double max2 = upperCorner.getValue();
	return "&BoundingBox=" + //
		min1 + "," + //
		min2 + "," + //
		max1 + "," + //
		max2 + "," + //
		crs.getIdentifier();
    }

    @Override
    public String getCoverageParameter() {
	return "identifier";
    }

    @Override
    public String getCRSParameter(XMLDocumentReader coverage, CRS inputCRS, CRS outputCRS) {
	CRS crs = outputCRS;
	String ret = "&GridBaseCRS=" + crs.getIdentifier();

	Node gridCRSNode;
	try {
	    gridCRSNode = coverage
		    .evaluateNode("*:CoverageDescriptions/*:CoverageDescription/*:Domain/*:SpatialDomain/*:GridCRS[*:GridBaseCRS='"
			    + crs.getIdentifier() + "']");
	} catch (XPathExpressionException e) {
	    return "";
	}
	String gridType;
	try {
	    gridType = coverage.evaluateString(gridCRSNode, "*:GridType");
	    ret += "&GridType=" + gridType;
	} catch (XPathExpressionException e) {
	    return "";
	}
	String gridCS;
	try {
	    gridCS = coverage.evaluateString(gridCRSNode, "*:GridCS");
	    ret += "&GridCS=" + gridCS;
	} catch (XPathExpressionException e) {
	    return "";
	}
	return ret;
    }

    @Override
    protected SimpleEntry<Double, Double> getGridOrigin(XMLDocumentReader coverage, CRS crs) {
	String identifier = retrieveBBOXCrsIdentifier(coverage, crs);
	String origin = null;
	try {
	    origin = coverage
		    .evaluateString("*:CoverageDescriptions/*:CoverageDescription/*:Domain/*:SpatialDomain/*:GridCRS[*:GridBaseCRS='"
			    + identifier + "']/*:GridOrigin");
	} catch (XPathExpressionException e) {
	}

	if (origin != null && !origin.isEmpty()) {

	    String[] split = origin.split(" ");
	    SimpleEntry<Double, Double> ret;
	    Double origin1 = Double.parseDouble(split[0]);
	    Double origin2 = Double.parseDouble(split[1]);
	    // NOTE: this swapping wouldn't be necessary in WCS 1.1.1!
	    // however it is needed because of a bug in virtually all the implementations, including:
	    // 1) the EOX WCS 1.1.1 RI
	    // 2) geoserver: https://osgeo-org.atlassian.net/browse/GEOS-6635
	    // 3) mapserver: https://trac.osgeo.org/mapserver/ticket/2940
	    //
	    if (crs.getAxisOrder().equals(AxisOrder.NORTH_EAST)) {
		ret = new SimpleEntry<Double, Double>(origin2, origin1);
	    } else {
		ret = new SimpleEntry<Double, Double>(origin1, origin2);
	    }
	    return ret;
	}
	return null;
    }

    @Override
    public String getResolutionParameter(XMLDocumentReader coverage, SimpleEntry<Double, Double> userResolutions,
	    SimpleEntry<Long, Long> userSpatialSizes, CRS crs) {

	String resolution = null;

	List<Double> defaultResolution = getResolutions(coverage, crs);

	for (int i = 0; i < 2; i++) {
	    Double defaultRes = defaultResolution.get(i);
	    double tmp = i == 0 ? userResolutions.getKey() : userResolutions.getValue();
	    if (tmp * defaultRes < 0) {
		defaultResolution.set(i, -tmp);
	    } else {
		defaultResolution.set(i, tmp);
	    }
	}

	if (defaultResolution != null && !defaultResolution.isEmpty()) {
	    String gridResolution = "";
	    String gridType = null;
	    try {
		gridType = coverage
			.evaluateString("*:CoverageDescriptions/*:CoverageDescription/*:Domain/*:SpatialDomain/*:GridCRS/*:GridType");
	    } catch (XPathExpressionException e) {
	    }
	    if (gridType != null && gridType.toLowerCase().contains("2dsimplegrid")) {
		for (Double d : defaultResolution) {
		    gridResolution += d + ",";
		}
		gridResolution = gridResolution.substring(0, gridResolution.length() - 1);
	    } else {
		gridResolution += defaultResolution.get(0) + ",0.0,0.0," + defaultResolution.get(1);
	    }

	    resolution = "&GridOffsets=" + gridResolution;

	}

	return resolution;
    }

    @Override
    protected List<Long> getSizes(XMLDocumentReader coverage, CRS specificCrs) {

	Set<CRS> crses = getBoundingBoxCRSes(coverage);

	for (CRS crs : crses) {
	    if (crs.equals(CRS.OGC_IMAGE())) {
		List<Long> ret = new ArrayList<>();
		Long columns = Math.round(getUpperCornerFirstDimension(coverage, crs)) + 1;
		Long rows = Math.round(getUpperCornerSecondDimension(coverage, crs)) + 1;
		if (specificCrs.getAxisOrder().equals(AxisOrder.NORTH_EAST)) {
		    ret.add(rows);
		    ret.add(columns);
		} else {
		    ret.add(columns);
		    ret.add(rows);
		}
		return ret;
	    }
	}

	return null;
    }

    /**
     * Return a list of resolutions from the coverage description. Multiline resolutions are put on a single line.
     * 
     * @param coverage
     * @param crs
     * @return
     */
    protected List<Double> getOriginalGridResolutionsOnASingleLine(XMLDocumentReader coverage, CRS crs) {
	List<Double> resolutions = new ArrayList<>();

	String dimensionString;
	try {
	    dimensionString = coverage
		    .evaluateString("*:CoverageDescriptions/*:CoverageDescription/*:Domain/*:SpatialDomain/*:BoundingBox/@dimensions");
	} catch (XPathExpressionException e) {
	    return resolutions;
	}
	if (dimensionString == null || dimensionString.isEmpty()) {
	    return resolutions;
	}

	String offsetVectors;
	try {
	    offsetVectors = coverage
		    .evaluateString("*:CoverageDescriptions/*:CoverageDescription/*:Domain/*:SpatialDomain/*:GridCRS/*:GridOffsets");
	} catch (XPathExpressionException e) {
	    return resolutions;
	}

	offsetVectors = offsetVectors.replace("\n", " ").replace("\t", " ").replaceAll("( )+", " ");

	String[] split = offsetVectors.trim().split(" ");

	for (String s : split) {
	    resolutions.add(Double.parseDouble(s));
	}

	return resolutions;

    }

    @Override
    protected List<Double> getResolutions(XMLDocumentReader coverage, CRS crs) {
	String gridType = null;
	try {
	    gridType = coverage
		    .evaluateString("*:CoverageDescriptions/*:CoverageDescription/*:Domain/*:SpatialDomain/*:GridCRS/*:GridType");
	} catch (XPathExpressionException e) {
	}

	List<Double> originalGridResolutions = getOriginalGridResolutionsOnASingleLine(coverage, crs);

	List<Double> resolutions = decodeGridResolutions(originalGridResolutions, gridType);

	// NOTE: this swapping wouldn't be necessary in WCS 1.1.1!
	// however it is needed because of a bug in virtually all the implementations, including:
	// 1) the EOX WCS 1.1.1 RI
	// 2) geoserver: https://osgeo-org.atlassian.net/browse/GEOS-6635
	// 3) mapserver: https://trac.osgeo.org/mapserver/ticket/2940
	//
	if (crs.getAxisOrder().equals(AxisOrder.NORTH_EAST)) {
	    resolutions = Lists.reverse(resolutions);
	}

	return resolutions;

    }

    private List<Double> decodeGridResolutions(List<Double> originalGridResolutions, String gridType) {
	List<Double> resolutions = new ArrayList<>();

	if (gridType != null && gridType.toLowerCase().contains("2dsimplegrid")) {

	    for (Double res : originalGridResolutions) {
		resolutions.add(res);
	    }

	} else {
	    int dimensions = (int) Math.round(Math.sqrt(originalGridResolutions.size()));

	    int position = 0;
	    firstFor: for (int i = 0; i < originalGridResolutions.size(); i += dimensions) {
		for (int j = 0; j < dimensions; j++) {
		    Double res = originalGridResolutions.get(i + j);
		    if (j == position) {
			position++;
			resolutions.add(res);
			continue firstFor;
		    }
		}
	    }
	}
	return resolutions;
    }

    @Override
    protected List<Date> getTimes(XMLDocumentReader coverage) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public String getTimesParameter(List<Date> times) {
	return "";
    }

}
