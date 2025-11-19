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
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

import eu.essi_lab.accessor.wcs.WCSConnector;
import eu.essi_lab.accessor.wcs_2_0_1.WCSConnector_201;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.data.AxisOrder;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataFormat;

public class WCSDownloader_201 extends WCSDownloader {

    @Override
    public WCSConnector createConnector() {
	return new WCSConnector_201();
    }

    public WCSConnector_201 getConnector() {
	return (WCSConnector_201) connector;
    }

    protected void setConnector(WCSConnector_201 connector) {
	this.connector = connector;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    @Override
    public boolean canDownload() {

	return NetProtocolWrapper.check(online.getProtocol(), NetProtocolWrapper.WCS_2_0_1);
    }

    public String getVersionParameter() {
	return "2.0.1";
    }

    protected Set<DataFormat> getFormats(XMLDocumentReader coverage) {
	HashSet<DataFormat> formats = new HashSet<DataFormat>();
	Node[] formatNodes;
	try {
	    formatNodes = coverage.evaluateNodes("*:CoverageDescriptions/*:CoverageDescription/*:ServiceParameters/*:nativeFormat");
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

    // protected Set<CRS> getBoundingBoxCRSes(XMLDocumentReader coverage) {
    // Set<CRS> crses = new HashSet<>();
    // Node[] crsNodes;
    // try {
    // crsNodes =
    // coverage.evaluateNodes("*:CoverageDescriptions/*:CoverageDescription/*:Domain/*:SpatialDomain/*:BoundingBox/@crs");
    //
    // } catch (XPathExpressionException e) {
    // return crses;
    // }
    // for (Node crsNode : crsNodes) {
    // String crs = null;
    // try {
    // crs = coverage.evaluateString(crsNode, ".");
    // } catch (XPathExpressionException e) {
    // }
    // if (crs != null) {
    // crses.add(CRS.fromIdentifier(crs));
    // }
    // }
    // return crses;
    // }

    protected Set<CRS> getResponseCRSes(XMLDocumentReader coverage) {
	Set<CRS> crses = new HashSet<>();
	Node[] crsNodes;
	try {
	    crsNodes = coverage.evaluateNodes(
		    "*:CoverageDescriptions/*:CoverageDescription/*:domainSet/*:RectifiedGrid/*:offsetVector/@srsName");

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
    protected SimpleEntry<Double, Double> getGridOrigin(XMLDocumentReader coverage, CRS crs) {
	String identifier = retrieveBBOXCrsIdentifier(coverage, crs);

	String origin = null;
	try {
	    origin = coverage.evaluateString(
		    "*:CoverageDescriptions/*:CoverageDescription/*:domainSet/*:RectifiedGrid/*:origin/*:Point[@srsName='" + identifier
			    + "']/*:pos");
	} catch (XPathExpressionException e) {
	}

	if (origin != null && !origin.isEmpty()) {
	    String[] split = origin.split(" ");
	    SimpleEntry<Double, Double> ret;
	    Double origin1 = Double.parseDouble(split[0]);
	    Double origin2 = Double.parseDouble(split[1]);
	    ret = new SimpleEntry<Double, Double>(origin1, origin2);
	    return ret;
	}
	return null;
    }

    @Override
    protected String getCoverageName(XMLDocumentReader coverage) {
	try {
	    return coverage.evaluateString("*:CoverageDescriptions/*:CoverageDescription/*:CoverageId");
	} catch (XPathExpressionException e) {
	    return null;
	}
    }

    private String retrieveBBOXCrsIdentifier(XMLDocumentReader coverage, CRS crs) {
	try {
	    Node[] bboxCrsNodes = coverage.evaluateNodes("*:CoverageDescriptions/*:CoverageDescription/*:boundedBy/*:Envelope/@srsName");
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
	    lowerCorner = coverage.evaluateString(
		    "*:CoverageDescriptions/*:CoverageDescription/*:boundedBy/*:Envelope[@srsName='" + identifier + "']/*:lowerCorner");
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
	    lowerCorner = coverage.evaluateString(
		    "*:CoverageDescriptions/*:CoverageDescription/*:boundedBy/*:Envelope[@srsName='" + identifier + "']/*:lowerCorner");
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
	    upperCorner = coverage.evaluateString(
		    "*:CoverageDescriptions/*:CoverageDescription/*:boundedBy/*:Envelope[@srsName='" + identifier + "']/*:upperCorner");
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
	    upperCorner = coverage.evaluateString(
		    "*:CoverageDescriptions/*:CoverageDescription/*:boundedBy/*:Envelope[@srsName='" + identifier + "']/*:upperCorner");
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

	String dim1 = "";
	String dim2 = "";
	try {
	    String spatialDimensionNames = coverage.evaluateString(
		    "*:CoverageDescriptions/*:CoverageDescription/*:boundedBy/*:Envelope[@srsName='" + crs.getIdentifier()
			    + "']/@axisLabels");
	    String[] split = spatialDimensionNames.split(" ");
	    dim1 = split[0];
	    dim2 = split[1];
	} catch (XPathExpressionException e) {
	}
	return "&subset=" + dim1 + "(" + min1 + "," + max1 + ")" + //
		"&subset=" + dim2 + "(" + min2 + "," + max2 + ")"; //
	// needed? mapserver raises an error...
	// "&subsettingCrs=" + crs.getIdentifier();
    }

    @Override
    public String getCoverageParameter() {
	return "coverageId";
    }

    @Override
    public String getCRSParameter(XMLDocumentReader coverage, CRS inputCRS, CRS outputCRS) {
	String ret = "&outputCrs=" + outputCRS.getIdentifier();

	return ret;
    }

    @Override
    public String getResolutionParameter(XMLDocumentReader coverage, SimpleEntry<Double, Double> userSpatialResolutions,
	    SimpleEntry<Long, Long> userSpatialSizes, CRS crs) {
	String dim1 = "";
	String dim2 = "";
	try {
	    String spatialDimensionNames = coverage.evaluateString(
		    "*:CoverageDescriptions/*:CoverageDescription/*:boundedBy/*:Envelope[@srsName='" + crs.getIdentifier()
			    + "']/@axisLabels");
	    String[] split = spatialDimensionNames.split(" ");
	    dim1 = split[0];
	    dim2 = split[1];
	} catch (XPathExpressionException e) {
	}
	String resolution = "";

	Long size1 = userSpatialSizes == null ? null : userSpatialSizes.getKey();
	Long size2 = userSpatialSizes == null ? null : userSpatialSizes.getValue();

	if (size1 != null && size2 != null) {
	    resolution = "&SCALESIZE=" + //
		    dim1 + "(" + size1 + ")," + //
		    dim2 + "(" + size2 + ")";
	}

	return resolution;

    }

    @Override
    protected List<Long> getSizes(XMLDocumentReader coverage, CRS specificCrs) {

	String size;
	try {
	    size = coverage.evaluateString(
		    "*:CoverageDescriptions/*:CoverageDescription/*:domainSet/*:RectifiedGrid/*:limits/*:GridEnvelope/*:high");
	} catch (XPathExpressionException e) {
	    return null;
	}

	if (size != null && size.contains(" ")) {
	    List<Long> sizes = new ArrayList<>();
	    String[] split = size.split(" ");
	    for (String s : split) {
		sizes.add(Long.parseLong(s) + 1);
	    }

	    if (specificCrs.getAxisOrder().equals(AxisOrder.NORTH_EAST)) {
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

	try {
	    String res = coverage.evaluateString(
		    "*:CoverageDescriptions/*:CoverageDescription/*:domainSet/*:RectifiedGrid/*:offsetVector[1]");
	    String[] split = res.split(" ");
	    for (String s : split) {
		double d = Double.parseDouble(s);
		if (Math.abs(d) > TOL) {
		    resolutions.add(d);
		}
	    }
	    res = coverage.evaluateString("*:CoverageDescriptions/*:CoverageDescription/*:domainSet/*:RectifiedGrid/*:offsetVector[2]");
	    split = res.split(" ");
	    for (String s : split) {
		double d = Double.parseDouble(s);
		if (Math.abs(d) > TOL) {
		    resolutions.add(d);
		}
	    }
	} catch (Exception e) {
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
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public String getTimesParameter(List<Date> times) {
	return "";
    }

}
