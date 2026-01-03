/**
 *
 */
package eu.essi_lab.accessor.wcs;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.xpath.XPathExpressionException;

import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.Dimension;
import eu.essi_lab.iso.datamodel.classes.Format;
import eu.essi_lab.iso.datamodel.classes.GridSpatialRepresentation;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ReferenceSystem;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent.TimeIntervalUnit;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLNodeReader;
import eu.essi_lab.model.GSPropertyHandler;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.OriginalIdentifierMapper;

/**
 * @author Fabrizio
 */
public abstract class WCSMapper extends OriginalIdentifierMapper {

    private static final String WCS_MAPPER_METADATA_PARSING_ERROR = "WCS_MAPPER_METADATA_PARSING_ERROR";
    private static final String WCS_MAPPER_INVALID_BBOX_ERROR = "WCS_MAPPER_INVALID_BBOX_ERROR";
    private static final String COLUMN = "column";

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	try {
	    XMLDocumentReader coverageDescription = new XMLDocumentReader(resource.getOriginalMetadata().getMetadata());
	    return getIdentifier(coverageDescription);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return null;
    }

    @Override
    public GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	GSPropertyHandler info = originalMD.getAdditionalInfo();
	String capId = info.get("capabilitiesId", String.class);

	Dataset dataset = new Dataset();
	dataset.setSource(source);

	XMLDocumentReader coverageDescription = null;
	try {
	    coverageDescription = new XMLDocumentReader(originalMD.getMetadata());
	} catch (Exception e) {

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    WCS_MAPPER_METADATA_PARSING_ERROR, //
		    e);
	}

	MIMetadata miMetadata = dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();

	// --------------
	//
	// identifier
	//
	String id = getIdentifier(coverageDescription);

	// --------------
	//
	// title
	//
	dataset.getHarmonizedMetadata().getCoreMetadata().setTitle(getTitle(coverageDescription));

	// --------------
	//
	// abstract
	//
	dataset.getHarmonizedMetadata().getCoreMetadata().setAbstract(getAbstract(coverageDescription));

	// --------------
	//
	// keywords
	//
	List<String> keywords = getKeywords(coverageDescription);
	for (String kwd : keywords) {
	    miMetadata.getDataIdentification().addKeyword(kwd);
	}

	// --------------
	//
	// formats
	//
	List<String> formats = getFormats(coverageDescription);
	for (String fmrt : formats) {
	    Format format = new Format();
	    format.setName(fmrt);
	    miMetadata.getDistribution().addFormat(format);
	}

	// ---------------
	//
	// bbox
	//
	XMLDocumentReader capabilities = info.get("capabilities", XMLDocumentReader.class);

	XMLNodeReader coverageOffering = getReducedCapabilities(capabilities, capId);
	// south(0), west(1), north(2), east(3)
	List<Double> boudingBox = getBBoxFromCapabilities(coverageOffering);

	if (boudingBox.isEmpty()) {

	    boudingBox = getBboxFromDescription(coverageDescription);
	}

	if (checkBbox(boudingBox)) {
	    //
	    // in case of invalid bbox an exception is thrown and the coverage is not mapped
	    //
	    dataset.getHarmonizedMetadata().getCoreMetadata().addBoundingBox(//
		    boudingBox.get(2), //
		    boudingBox.get(1), //
		    boudingBox.get(0), //
		    boudingBox.get(3));
	}

	// ---------------
	//
	// time extent
	//
	TemporalExtent timeExtent = getTimeExtent(coverageDescription);
	if (timeExtent != null) {

	    miMetadata.getDataIdentification().addTemporalExtent(timeExtent);
	}

	// ----------------
	//
	// reference system
	//
	List<String> crsList = getSupportedCRS(coverageDescription);
	for (String crs : crsList) {

	    ReferenceSystem referenceSystem = new ReferenceSystem();
	    referenceSystem.setCode(crs);
	    miMetadata.addReferenceSystemInfo(referenceSystem);
	}

	// --------------------
	//
	// online
	//
	Online onLine = new Online();
	onLine.setLinkage(info.get("endpoint", String.class));
	onLine.setProtocol(getWCSProtocol());
	onLine.setFunctionCode("download");
	onLine.setName(id);

	miMetadata.getDistribution().addDistributionOnline(onLine);

	// ---------------------
	//
	// contact
	//
	ResponsibleParty contact = getContact(capabilities);
	if (contact != null) {
	    miMetadata.addContact(contact);
	}

	// -------------------------
	//
	// spatial representation
	//
	GridSpatialRepresentation grid = getGripSpatialRepresentation(coverageDescription);
	if (grid != null) {

	    miMetadata.addGridSpatialRepresentation(grid);
	}

	// -------------------------
	//
	// coverage description
	//
	CoverageDescription desc = getCoverageDescription(coverageDescription);
	if (desc != null) {

	    miMetadata.addCoverageDescription(desc);
	}

	return dataset;
    }

    public abstract XMLNodeReader getReducedCapabilities(XMLDocumentReader capabilities, String coverageId);

    /**
     * @param extent
     * @param timeRes
     */
    protected void setTimeInterval(TemporalExtent extent, String timeRes) {

	Duration duration = getDuration(timeRes);
	if (duration != null) {

	    int value = 0;
	    TimeIntervalUnit unit = null;

	    if (duration.getSeconds() > 0) {
		unit = TimeIntervalUnit.SECOND;
		value = duration.getSeconds();

	    } else if (duration.getMinutes() > 0) {

		unit = TimeIntervalUnit.MINUTE;
		value = duration.getMinutes();

	    } else if (duration.getHours() > 0) {
		unit = TimeIntervalUnit.HOUR;
		value = duration.getHours();

	    } else if (duration.getDays() > 0) {
		unit = TimeIntervalUnit.DAY;
		value = duration.getDays();

	    } else if (duration.getMonths() > 0) {
		unit = TimeIntervalUnit.MONTH;
		value = duration.getMonths();

	    } else if (duration.getYears() > 0) {

		unit = TimeIntervalUnit.YEAR;
		value = duration.getYears();
	    }

	    if (unit != null && value > 0) {

		extent.setTimeInterval(value);
		extent.setTimeIntervalUnit(unit);
	    }
	}
    }

    /**
     * @param lexicalRepresentation
     * @return
     */
    protected Duration getDuration(String lexicalRepresentation) {

	DatatypeFactory datatypeFactory = null;
	try {
	    datatypeFactory = DatatypeFactory.newInstance();
	} catch (javax.xml.datatype.DatatypeConfigurationException e) {
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	    return null;
	}

	return datatypeFactory.newDuration(lexicalRepresentation.trim());

    }

    /**
     * @param s
     * @return
     */
    protected boolean checkString(String s) {

	return s != null && !s.equals("");
    }

    /**
     * @param boudingBox
     * @return
     */
    protected boolean checkBbox(List<Double> boudingBox) throws GSException {

	Double south = boudingBox.get(0);
	Double west = boudingBox.get(1);
	Double north = boudingBox.get(2);
	Double east = boudingBox.get(3);

	south = BigDecimal.valueOf(south).setScale(3, RoundingMode.FLOOR).doubleValue();
	west = BigDecimal.valueOf(west).setScale(3, RoundingMode.FLOOR).doubleValue();
	north = BigDecimal.valueOf(north).setScale(3, RoundingMode.FLOOR).doubleValue();
	east = BigDecimal.valueOf(east).setScale(3, RoundingMode.FLOOR).doubleValue();

	if (east > 180) { // as TDS employs 0;359 notation
	    west = west - 180.0;
	    east = east - 180.0;
	}

	if (south < -90 || west < -180 || north > 90 || east > 180) {

	    GSLoggerFactory.getLogger(getClass()).warn("Coverage bbox not valid (s,w,n,e): {}", boudingBox);

	    throw GSException.createException(//
		    getClass(), "Coverage bbox not valid (s,w,n,e): " + boudingBox, //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    WCS_MAPPER_INVALID_BBOX_ERROR);

	}

	return true;
    }

    public static void main(String[] args) {

	Double toBeTruncated = -180.00000000000003;

	Double halfUp = BigDecimal.valueOf(toBeTruncated).setScale(3, RoundingMode.HALF_UP).doubleValue();

	Double halfDown = BigDecimal.valueOf(toBeTruncated).setScale(3, RoundingMode.HALF_DOWN).doubleValue();

	System.out.println(halfUp);
	System.out.println(halfDown);
    }

    /**
     * @param coverageDescription
     * @param boudingBox
     * @return
     */
    protected GridSpatialRepresentation getGripSpatialRepresentation(XMLDocumentReader coverageDescription) {

	try {

	    GridSpatialRepresentation grid = new GridSpatialRepresentation();

	    Integer dimCount = getDimensionsCount(coverageDescription);
	    grid.setNumberOfDimensions(dimCount);

	    List<Double> offsets = getOffsets(coverageDescription);

	    List<String> dimensionsNames = new ArrayList<>();
	    List<Double> dimensionsResolutions = new ArrayList<>();

	    if (dimCount == 1) {
		dimensionsNames.add("row");
		if (!offsets.isEmpty()) {
		    dimensionsResolutions.add(offsets.get(0));
		}
	    }

	    if (dimCount == 2) {
		dimensionsNames.add("row");
		dimensionsNames.add(COLUMN);
		if (offsets.size() > 3) {
		    dimensionsResolutions.add(offsets.get(0));
		    dimensionsResolutions.add(offsets.get(3));
		}
	    }

	    if (dimCount == 3) {
		dimensionsNames.add("row");
		dimensionsNames.add(COLUMN);
		dimensionsNames.add("vertical");
		if (offsets.size() > 8) {
		    dimensionsResolutions.add(offsets.get(0));
		    dimensionsResolutions.add(offsets.get(4));
		    dimensionsResolutions.add(offsets.get(8));
		}
	    }

	    if (dimCount == 4) {
		dimensionsNames.add("row");
		dimensionsNames.add(COLUMN);
		dimensionsNames.add("vertical");
		dimensionsNames.add("d4");
		if (offsets.size() > 13) {
		    dimensionsResolutions.add(offsets.get(0));
		    dimensionsResolutions.add(offsets.get(5));
		    dimensionsResolutions.add(offsets.get(9));
		    dimensionsResolutions.add(offsets.get(13));
		}
	    }

	    List<Integer> points = getPointsOverDimensions(offsets, coverageDescription);
	    if (points.isEmpty()) {
		GSLoggerFactory.getLogger(getClass()).warn("Unable to compute points over dimensions for current coverage");
		return null;
	    }

	    String crs = getGridBoudingBoxCRS(coverageDescription);

	    boolean degrees = crs.contains("4326") || crs.toLowerCase().contains("crs84");
	    boolean meters = crs.contains("3857") || crs.contains("3035");

	    String uom = "unknown";
	    if (degrees) {
		uom = "degrees";
	    } else if (meters) {
		uom = "meters";
	    }

	    for (int i = 0; i < dimCount; i++) {

		Dimension dimension = new Dimension();

		if (dimensionsResolutions.size() > i) {

		    Double res = dimensionsResolutions.get(i);
		    dimension.setResolution(uom, res);

		    if (points.size() > i) {
			dimension.setDimensionSize(new BigInteger(String.valueOf(points.get(i))));
		    }
		}

		dimension.setDimensionNameTypeCode(dimensionsNames.get(i));

		grid.addAxisDimension(dimension);
	    }

	    return grid;

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);
	}

	return null;
    }

    /**
     * @param offsets
     * @param coverageDescription
     * @return
     */
    protected List<Integer> getPointsOverDimensions(List<Double> offsets, XMLDocumentReader coverageDescription) {

	List<Integer> ret = new ArrayList<>();

	List<Double> resolutions = offsets.stream().//
		filter(d -> d != 0).//
		collect(Collectors.toList());

	List<Double> gridBoudingBox = getGridBoudingBox(coverageDescription);

	if (gridBoudingBox.isEmpty()) {
	    return new ArrayList<>();
	}

	double[] lower = new double[] { gridBoudingBox.get(0), gridBoudingBox.get(1) };
	double[] upper = new double[] { gridBoudingBox.get(2), gridBoudingBox.get(3) };

	for (int i = 0; i < resolutions.size(); i++) {
	    double res = resolutions.get(i);
	    double interval = upper[i] - lower[i];
	    int points = (int) (interval / res);
	    points = Math.abs(points);
	    ret.add(points);
	}

	return ret;
    }

    /**
     * @param coverageDescription
     * @return
     */
    protected abstract int getDimensionsCount(XMLDocumentReader coverageDescription) throws XPathExpressionException;

    /**
     * @param coverageDescription
     * @return
     */
    protected abstract List<Double> getOffsets(XMLDocumentReader coverageDescription) throws XPathExpressionException;

    /**
     * @param capabilities
     * @return
     */
    protected abstract ResponsibleParty getContact(XMLDocumentReader capabilities);

    /**
     * @param coverageDescription
     * @return
     */
    protected abstract String getIdentifier(XMLDocumentReader coverageDescription);

    /**
     * @param coverageDescription
     * @return
     */
    protected abstract List<String> getKeywords(XMLDocumentReader coverageDescription);

    /**
     * @param coverageDescription
     * @return
     */
    protected abstract List<String> getFormats(XMLDocumentReader coverageDescription);

    /**
     * @param coverageDescription
     * @return
     */
    protected abstract String getTitle(XMLDocumentReader coverageDescription);

    /**
     * @param coverageDescription
     * @return
     */
    protected abstract String getAbstract(XMLDocumentReader coverageDescription);

    /**
     * Returns the bounding box expressed in degrees (epsg:4326/wgs84 -> lat/lon or wgs84 lon/lat) with the coordinates
     * in the following
     * order: south, west, north, east
     *
     * @param coverageDescription
     * @param id
     * @return
     */
    protected abstract List<Double> getBboxFromDescription(XMLDocumentReader coverageDescription);

    /**
     * south, west, north, east
     *
     * @param coverage offering
     * @return
     */
    protected abstract List<Double> getBBoxFromCapabilities(XMLNodeReader coverageOffering);

    /**
     * Returns the bounding box as expressed in the coverage spatial domain
     *
     * @param coverageDescription
     * @return
     */
    protected abstract List<Double> getGridBoudingBox(XMLDocumentReader coverageDescription);

    /**
     * Returns the CRS of the bounding box as expressed in the coverage spatial domain
     *
     * @param coverageDescription
     * @return
     */
    protected abstract String getGridBoudingBoxCRS(XMLDocumentReader coverageDescription);

    /**
     * @param coverageDescription
     * @param id
     * @return
     */
    protected abstract TemporalExtent getTimeExtent(XMLDocumentReader coverageDescription);

    /**
     * @param coverageDescription
     * @return
     */
    protected abstract List<String> getSupportedCRS(XMLDocumentReader coverageDescription);

    /**
     * @param coverageDescription
     * @return
     */
    protected abstract CoverageDescription getCoverageDescription(XMLDocumentReader coverageDescription);

    /**
     * @return
     */
    protected abstract String getWCSProtocol();

    /**
     * @return
     */
    protected abstract String getVersion();

}
