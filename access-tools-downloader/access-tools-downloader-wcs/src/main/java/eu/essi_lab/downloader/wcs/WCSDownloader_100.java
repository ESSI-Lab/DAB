package eu.essi_lab.downloader.wcs;

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

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

import eu.essi_lab.accessor.wcs.WCSConnector;
import eu.essi_lab.accessor.wcs_1_0_0.WCSConnector_100;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.data.AxisOrder;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataFormat;

public class WCSDownloader_100 extends WCSDownloader {

    @Override
    public WCSConnector createConnector() {
	return new WCSConnector_100();
    }

    public WCSConnector_100 getConnector() {
	return (WCSConnector_100) connector;
    }

    protected void setConnector(WCSConnector_100 connector) {
	this.connector = connector;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    @Override
    public boolean canDownload() {

	return NetProtocolWrapper.check(online.getProtocol(),NetProtocolWrapper.WCS_1_0_0);
    }

    public String getVersionParameter() {
	return "1.0.0";
    }

    protected Set<DataFormat> getFormats(XMLDocumentReader coverage) {
	HashSet<DataFormat> formats = new HashSet<DataFormat>();
	Node[] formatNodes;
	try {
	    formatNodes = coverage.evaluateNodes("*:CoverageDescription/*:CoverageOffering/*:supportedFormats/*:formats");
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
		formats.add(DataFormat.fromIdentifier(format));
	    }
	}

	possiblyTagGeoTiffFormat(formats);

	return formats;
    }

    /**
     * Returns the native CRSes that are also present as requestresponseCRSs. If none are present, then the requestResponseCRSs CRSes are
     * returned.
     */
    protected Set<CRS> getResponseCRSes(XMLDocumentReader coverage) {
	Set<CRS> requestResponseCrses = new HashSet<>();
	Set<CRS> responseCrses = new HashSet<>();
	Set<CRS> nativeCrses = new HashSet<>();
	Set<CRS> gridCrses = new HashSet<>();
	Node[] requestResponseNodes;
	Node[] responseNodes;
	Node[] nativeCRSNodes;
	String gridCRS = null;
	try {
	    requestResponseNodes = coverage.evaluateNodes("*:CoverageDescription/*:CoverageOffering/*:supportedCRSs/*:requestResponseCRSs");
	    nativeCRSNodes = coverage.evaluateNodes("*:CoverageDescription/*:CoverageOffering/*:supportedCRSs/*:nativeCRSs");
	    responseNodes = coverage.evaluateNodes("*:CoverageDescription/*:CoverageOffering/*:supportedCRSs/*:responseCRSs");
	    gridCRS = coverage.evaluateString(
		    "*:CoverageDescription/*:CoverageOffering/*:domainSet/*:spatialDomain/*:RectifiedGrid/@srsName");
	} catch (XPathExpressionException e) {
	    return requestResponseCrses;
	}
	for (Node crsNode : requestResponseNodes) {
	    String crs = null;
	    try {
		crs = coverage.evaluateString(crsNode, ".");
	    } catch (XPathExpressionException e) {
	    }
	    if (crs != null) {
		requestResponseCrses.add(CRS.fromIdentifier(crs));
	    }
	}
	for (Node crsNode : nativeCRSNodes) {
	    String crs = null;
	    try {
		crs = coverage.evaluateString(crsNode, ".");
	    } catch (XPathExpressionException e) {
	    }
	    if (crs != null) {
		nativeCrses.add(CRS.fromIdentifier(crs));
	    }
	}
	for (Node srsNode : responseNodes) {
	    String crs = null;
	    try {
		crs = coverage.evaluateString(srsNode, ".");
	    } catch (XPathExpressionException e) {
	    }
	    if (crs != null) {
		responseCrses.add(CRS.fromIdentifier(crs));
	    }
	}
	if (gridCRS != null) {
	    gridCrses.add(CRS.fromIdentifier(gridCRS));
	}
	Set<CRS> ret = new HashSet<CRS>(nativeCrses);
	if (!ret.isEmpty()) {
	    ret.retainAll(requestResponseCrses);
	    if (!ret.isEmpty()) {
		return ret;
	    }
	}
	ret = new HashSet<CRS>(nativeCrses);
	if (!ret.isEmpty()) {
	    ret.retainAll(responseCrses);
	    if (!ret.isEmpty()) {
		return ret;
	    }
	}
	if (!requestResponseCrses.isEmpty()) {
	    return requestResponseCrses;
	}
	if (!responseCrses.isEmpty()) {
	    return responseCrses;
	}

	// last chance... for cases such as SEDAC, where no response crs is specified
	return gridCrses;

    }

    protected Set<CRS> getRequestCRSes(XMLDocumentReader coverage) {
	Set<CRS> requestCrses = new HashSet<>();
	Node[] requestResponseNodes;
	Node[] requestNodes;
	Set<CRS> gridCrses = new HashSet<>();
	String gridCRS = null;
	try {
	    requestResponseNodes = coverage.evaluateNodes("*:CoverageDescription/*:CoverageOffering/*:supportedCRSs/*:requestResponseCRSs");
	    requestNodes = coverage.evaluateNodes("*:CoverageDescription/*:CoverageOffering/*:supportedCRSs/*:requestCRSs");
	    gridCRS = coverage.evaluateString(
		    "*:CoverageDescription/*:CoverageOffering/*:domainSet/*:spatialDomain/*:RectifiedGrid/@srsName");
	} catch (XPathExpressionException e) {
	    return requestCrses;
	}
	for (Node crsNode : requestResponseNodes) {
	    String crs = null;
	    try {
		crs = coverage.evaluateString(crsNode, ".");
	    } catch (XPathExpressionException e) {
	    }
	    if (crs != null) {
		requestCrses.add(CRS.fromIdentifier(crs));
	    }
	}
	for (Node srsNode : requestNodes) {
	    String crs = null;
	    try {
		crs = coverage.evaluateString(srsNode, ".");
	    } catch (XPathExpressionException e) {
	    }
	    if (crs != null) {
		requestCrses.add(CRS.fromIdentifier(crs));
	    }
	}
	if (gridCRS != null) {
	    gridCrses.add(CRS.fromIdentifier(gridCRS));
	}
	if (!requestCrses.isEmpty()) {
	    return requestCrses;
	}

	// last chance... for cases such as SEDAC, where no request crs is specified

	return gridCrses;

    }

    @Override
    protected String getCoverageName(XMLDocumentReader coverage) {
	try {
	    return coverage.evaluateString("*:CoverageDescription/*:CoverageOffering/*:name");
	} catch (XPathExpressionException e) {
	    return null;
	}
    }

    private String retrieveBBOXCrsIdentifier(XMLDocumentReader coverage, CRS crs) {
	try {
	    Node[] bboxCrsNodes = coverage.evaluateNodes("*:CoverageDescription/*:CoverageOffering/*:domainSet/*:spatialDomain/*/@srsName");
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
    protected SimpleEntry<Double, Double> getGridOrigin(XMLDocumentReader coverage, CRS crs) {
	String identifier = retrieveBBOXCrsIdentifier(coverage, crs);
	String origin;
	try {
	    origin = coverage.evaluateString(
		    "*:CoverageDescription/*:CoverageOffering/*:domainSet/*:spatialDomain[*[@srsName='" + identifier
			    + "']]/*:RectifiedGrid/*:origin/*:pos");
	} catch (XPathExpressionException e) {
	    return null;
	}
	if (origin != null && !origin.isEmpty()) {

	    String[] split = origin.split(" ");
	    SimpleEntry<Double, Double> ret;
	    // because for WCS 1.0.0 is always EAST/NORTH
	    Double originE = Double.parseDouble(split[0]);
	    Double originN = Double.parseDouble(split[1]);
	    if (crs.getAxisOrder().equals(AxisOrder.NORTH_EAST)) {
		ret = new SimpleEntry<Double, Double>(originN, originE);
	    } else {
		ret = new SimpleEntry<Double, Double>(originE, originN);
	    }
	    return ret;
	}
	return null;
    }

    @Override
    protected Double getLowerCornerFirstDimension(XMLDocumentReader coverage, CRS crs) {
	String identifier = retrieveBBOXCrsIdentifier(coverage, crs);
	Node envelope;
	try {
	    envelope = coverage.evaluateNode(
		    "*:CoverageDescription/*:CoverageOffering/*:domainSet/*:spatialDomain/*[@srsName='" + identifier + "']");
	} catch (XPathExpressionException e) {
	    return null;
	}
	if (envelope != null) {
	    String lowerCorner;
	    try {
		lowerCorner = coverage.evaluateString(envelope, "*:pos[1]");
	    } catch (XPathExpressionException e) {
		return null;
	    }
	    if (lowerCorner != null) {
		String[] lowerSplit = lowerCorner.split(" ");
		// because for WCS 1.0.0 is always EAST/NORTH
		int position = crs.getAxisOrder().equals(AxisOrder.NORTH_EAST) ? 1 : 0;
		Double lower1 = Double.parseDouble(lowerSplit[position]);
		return lower1;
	    }
	}
	return null;
    }

    @Override
    protected Double getLowerCornerSecondDimension(XMLDocumentReader coverage, CRS crs) {
	String identifier = retrieveBBOXCrsIdentifier(coverage, crs);
	Node envelope;
	try {
	    envelope = coverage.evaluateNode(
		    "*:CoverageDescription/*:CoverageOffering/*:domainSet/*:spatialDomain/*[@srsName='" + identifier + "']");
	} catch (XPathExpressionException e) {
	    return null;
	}
	if (envelope != null) {
	    String lowerCorner;
	    try {
		lowerCorner = coverage.evaluateString(envelope, "*:pos[1]");
	    } catch (XPathExpressionException e) {
		return null;
	    }
	    if (lowerCorner != null) {
		String[] lowerSplit = lowerCorner.split(" ");
		// because for WCS 1.0.0 is always EAST/NORTH
		int position = crs.getAxisOrder().equals(AxisOrder.NORTH_EAST) ? 0 : 1;
		Double lower1 = Double.parseDouble(lowerSplit[position]);
		return lower1;
	    }
	}
	return null;
    }

    @Override
    protected Double getUpperCornerFirstDimension(XMLDocumentReader coverage, CRS crs) {
	String identifier = retrieveBBOXCrsIdentifier(coverage, crs);
	Node envelope;
	try {
	    envelope = coverage.evaluateNode(
		    "*:CoverageDescription/*:CoverageOffering/*:domainSet/*:spatialDomain/*[@srsName='" + identifier + "']");
	} catch (XPathExpressionException e) {
	    return null;
	}
	if (envelope != null) {
	    String lowerCorner;
	    try {
		lowerCorner = coverage.evaluateString(envelope, "*:pos[2]");
	    } catch (XPathExpressionException e) {
		return null;
	    }
	    if (lowerCorner != null) {
		String[] lowerSplit = lowerCorner.split(" ");
		// because for WCS 1.0.0 is always EAST/NORTH
		int position = crs.getAxisOrder().equals(AxisOrder.NORTH_EAST) ? 1 : 0;
		Double lower1 = Double.parseDouble(lowerSplit[position]);
		return lower1;
	    }
	}
	return null;
    }

    @Override
    protected Double getUpperCornerSecondDimension(XMLDocumentReader coverage, CRS crs) {
	String identifier = retrieveBBOXCrsIdentifier(coverage, crs);
	Node envelope;
	try {
	    envelope = coverage.evaluateNode(
		    "*:CoverageDescription/*:CoverageOffering/*:domainSet/*:spatialDomain/*[@srsName='" + identifier + "']");
	} catch (XPathExpressionException e) {
	    return null;
	}
	if (envelope != null) {
	    String lowerCorner;
	    try {
		lowerCorner = coverage.evaluateString(envelope, "*:pos[2]");
	    } catch (XPathExpressionException e) {
		return null;
	    }
	    if (lowerCorner != null) {
		String[] lowerSplit = lowerCorner.split(" ");
		// because for WCS 1.0.0 is always EAST/NORTH
		int position = crs.getAxisOrder().equals(AxisOrder.NORTH_EAST) ? 0 : 1;
		Double lower1 = Double.parseDouble(lowerSplit[position]);
		return lower1;
	    }
	}
	return null;
    }

    @Override
    public String getCoverageParameter() {
	return "COVERAGE";
    }

    @Override
    public String getCRSParameter(XMLDocumentReader coverage, CRS inputCRS, CRS outputCRS) {
	String ret = "&CRS=" + urlEncode(inputCRS.getIdentifier());
	if (!inputCRS.equals(outputCRS)) {
	    ret += "&RESPONSE_CRS=" + urlEncode(outputCRS.getIdentifier());
	}
	return ret;
    }

    @Override
    public String getResolutionParameter(XMLDocumentReader coverage, SimpleEntry<Double, Double> userSpatialResolutions,
	    SimpleEntry<Long, Long> userSpatialSizes, CRS crs) {
	String resolution = null;

	Integer height = null;
	Integer width = null;
	Long firstSize = userSpatialSizes == null ? null : userSpatialSizes.getKey();
	Long secondSize = userSpatialSizes == null ? null : userSpatialSizes.getValue();
	if (firstSize != null && secondSize != null) {
	    if (crs.getAxisOrder().equals(AxisOrder.NORTH_EAST)) {
		height = firstSize.intValue();
		width = secondSize.intValue();
	    } else {
		height = secondSize.intValue();
		width = firstSize.intValue();
	    }
	}
	if (height != null && width != null) {
	    resolution = "&WIDTH=" + width + "&HEIGHT=" + height;
	} else {
	    Double resFirst = userSpatialResolutions == null ? null : userSpatialResolutions.getKey();
	    Double resSecond = userSpatialResolutions == null ? null : userSpatialResolutions.getValue();
	    if (resFirst != null && resSecond != null) {
		if (crs.getAxisOrder().equals(AxisOrder.NORTH_EAST)) {
		    resolution = "&RESX=" + resSecond;
		    resolution += "&RESY=" + resFirst;
		} else {
		    resolution = "&RESX=" + resFirst;
		    resolution += "&RESY=" + resSecond;
		}
	    }

	}

	return resolution;
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
	boolean northEastCRS = crs.getAxisOrder().equals(AxisOrder.NORTH_EAST);
	String ret = northEastCRS ? min2 + "," + min1 + "," + max2 + "," + max1 : min1 + "," + min2 + "," + max1 + "," + max2;
	return "&BBOX=" + ret;
    }

    @Override
    protected List<Long> getSizes(XMLDocumentReader coverage, CRS crs) {

	String size;
	try {
	    size = coverage.evaluateString(
		    "*:CoverageDescription/*:CoverageOffering/*:domainSet/*:spatialDomain/*:RectifiedGrid/*:limits/*:GridEnvelope/*:high");
	} catch (XPathExpressionException e) {
	    return null;
	}

	if (size != null && size.contains(" ")) {
	    List<Long> sizes = new ArrayList<>();
	    String[] split = size.split(" ");
	    for (String s : split) {
		sizes.add(Long.parseLong(s) + 1);
	    }

	    if (crs.getAxisOrder().equals(AxisOrder.NORTH_EAST)) {
		Long tmp = sizes.get(0);
		sizes.set(0, sizes.get(1));
		sizes.set(1, tmp);
	    }
	    return sizes;
	}

	return null;

    }

    @Override
    protected List<Double> getResolutions(XMLDocumentReader coverage, CRS crs) {

	List<Double> resolutions = new ArrayList<>();
	Node[] offsetVectors;
	try {
	    offsetVectors = coverage.evaluateNodes(
		    "*:CoverageDescription/*:CoverageOffering/*:domainSet/*:spatialDomain/*:RectifiedGrid/*:offsetVector");
	} catch (XPathExpressionException e) {
	    return resolutions;
	}
	if (offsetVectors != null && offsetVectors.length > 0) {
	    int dimensions = offsetVectors.length;
	    int position = 0;
	    for (Node offsetVector : offsetVectors) {
		String offsetVectorString;
		try {
		    offsetVectorString = coverage.evaluateString(offsetVector, ".");
		} catch (XPathExpressionException e) {
		    return resolutions;
		}
		if (dimensions == 1) {
		    resolutions.add(Double.parseDouble(offsetVectorString));
		} else {
		    String[] split = offsetVectorString.split(" ");
		    Double resolution = Double.parseDouble(split[position++]);
		    resolutions.add(resolution);
		}
	    }
	}
	if (crs.getAxisOrder().equals(AxisOrder.NORTH_EAST)) {
	    Double tmp = resolutions.get(0);
	    resolutions.set(0, resolutions.get(1));
	    resolutions.set(1, tmp);
	}
	return resolutions;

    }

    @Override
    protected List<Date> getTimes(XMLDocumentReader coverage) {
	List<Date> ret = new ArrayList<>();
	try {
	    Node[] nodes = coverage.evaluateNodes("*:CoverageDescription/*:CoverageOffering/*:domainSet/*:temporalDomain/*:timePosition");
	    for (Node node : nodes) {
		String time = coverage.evaluateString(node, ".");
		Optional<Date> date = ISO8601DateTimeUtils.parseISO8601ToDate(time);
		if (date.isPresent()) {
		    ret.add(date.get());
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
	ret = ret.stream().distinct().sorted().collect(Collectors.toList());
	return ret;
    }

    @Override
    public String getTimesParameter(List<Date> times) {
	if (times.isEmpty()) {
	    return "";
	}
	String ret = "&TIME=";
	for (Date time : times) {
	    ret += ISO8601DateTimeUtils.getISO8601DateTime(time) + ",";
	}
	ret = ret.substring(0, ret.length() - 1);
	return ret;
    }

    @Override
    protected SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> fixUserLowerAndUpperCorners(
	    SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> userLowerAndUpperCorners,
	    SimpleEntry<Double, Double> userSpatialResolutions) {
	if (userLowerAndUpperCorners != null && userSpatialResolutions != null) {
	    Double res1 = userSpatialResolutions.getKey();
	    Double res2 = userSpatialResolutions.getValue();
	    SimpleEntry<Double, Double> lowerCorner = userLowerAndUpperCorners.getKey();
	    SimpleEntry<Double, Double> upperCorner = userLowerAndUpperCorners.getValue();

	    Double min1 = lowerCorner.getKey() - res1 / 2.0;
	    Double min2 = lowerCorner.getValue() - res2 / 2.0;
	    Double max1 = upperCorner.getKey() + res1 / 2.0;
	    Double max2 = upperCorner.getValue() + res2 / 2.0;
	    lowerCorner = new SimpleEntry<>(min1, min2);
	    upperCorner = new SimpleEntry<>(max1, max2);
	    return new SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>>(lowerCorner, upperCorner);
	}
	return userLowerAndUpperCorners;
    }

}
