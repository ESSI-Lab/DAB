package eu.essi_lab.profiler.worldcereal.handler.discover;

import java.util.ArrayList;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.QueryableBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.RuntimeInfoElementBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.SpatialBond;
import eu.essi_lab.messages.bond.SpatialExtent;
import eu.essi_lab.messages.bond.ViewBond;
import eu.essi_lab.messages.bond.parser.DiscoveryBondHandler;
import eu.essi_lab.model.resource.MetadataElement;

/**
 * @author roncella
 */
public class WorldCerealBondHandler implements DiscoveryBondHandler {

    private Logger logger = GSLoggerFactory.getLogger(WorldCerealBondHandler.class);

    private static final String WORLDCEREAL_URL = "ewoc-rdm-api.iiasa.ac.at";

    private static final String COLLECTION_ID_KEY = "collections";
    private static final String EQUAL = "=";
    private static final String WORLDCEREAL_START_KEY = "SkipCount";
    private static final String WORLDCEREAL_COUNT_KEY = "MaxResultCount";
    private static final String AGROSTAC_START_KEY = "pagenumber";
    private static final String AGROSTAC_COUNT_KEY = "pagesize";
    private static final String AND = "&";
    private static final String ENCODED_SPACE = "%20";
    private static final String COMMA = ",";
    private static final String BBOX = "Bbox";
    private static final String AGROSTAC_MIN_LON = "minlondd";
    private static final String AGROSTAC_MAX_LON = "maxlondd";
    private static final String AGROSTAC_MIN_LAT = "minlatdd";
    private static final String AGROSTAC_MAX_LAT = "maxlatdd";
    private static final String AGROSTAC_ACCESS_TOKEN = "accesstoken";
    // private static final String END_POLYGON = "))";
    // private static final String GEOM_KEY = "geometry";
    private static final String DATETIME = "datetime";
    private static final String SEPARATOR = "/";
    private static final String STARTDATE_KEY = "ValidityStartTime";
    private static final String ENDDATE_KEY = "ValidityEndTime";
    private static final String EWOC_CODES = "EwocCodes";
    private static final String CROP_TYPES = "cropTypes";
    private static final String QUANTITY_TYPES = "cropquantity";
    private static final String LAND_COVER_TYPES = "landCoverTypes";
    private static final String IRRIGATION_TYPES = "IrrigationTypes";
    private static final String INTERSECT = "intersect";
    private static final String START_CONFIDENCE_CROP = "CropTypeConfidence.Start";
    private static final String START_CONFIDENCE_LC = "LandCoverConfidence.Start";
    private static final String END_CONFIDENCE_CROP = "CropTypeConfidence.End";
    private static final String END_CONFIDENCE_LC = "LandCoverConfidence.End";
    // private static final String START_CONFIDENCE_IRR = "IrrigationConfidence.Start";
    // private static final String END_CONFIDENCE_IRR = "IrrigationConfidence.End";

    private Integer start;
    private Integer count;

    private String polygon;
    private String startDate;
    private String endDate;
    private String startConfidenceCrop;
    private String endConfidenceCrop;
    private String startConfidenceLc;
    private String endConfidenceLc;
    // private String startConfidenceIrr;
    // private String endConfidenceIrr;
    private Map<String, List<String>> textSearches;
    private String quantity;
    private boolean valid;

    private String datasetId;

    public String getOnlineId() {
	return datasetId;
    }

    public void setOnlineId(String onlineId) {
	this.datasetId = onlineId;
    }

    /**
     * @param datasetId
     */
    public WorldCerealBondHandler() {

	super();
	this.valid = true;
	this.textSearches = new HashMap<>();
    }

    @Override
    public void simpleValueBond(SimpleValueBond bond) {

	MetadataElement element = bond.getProperty();

	switch (element) {

	case TEMP_EXTENT_BEGIN:

	    ISO8601DateTimeUtils.parseISO8601ToDate(bond.getPropertyValue())
		    .ifPresent(d -> startDate = ISO8601DateTimeUtils.getISO8601Date(d));

	    break;

	case TEMP_EXTENT_END:

	    ISO8601DateTimeUtils.parseISO8601ToDate(bond.getPropertyValue())
		    .ifPresent(d -> endDate = ISO8601DateTimeUtils.getISO8601Date(d));

	    break;

	case CONFIDENCE_CROP_TYPE:
	    BondOperator cropOp = bond.getOperator();
	    if (cropOp.equals(BondOperator.GREATER_OR_EQUAL)) {
		startConfidenceCrop = "" + Double.valueOf(bond.getPropertyValue()).intValue();
		System.out.println("" + Double.valueOf(bond.getPropertyValue()).intValue());
	    } else if (cropOp.equals(BondOperator.LESS_OR_EQUAL)) {
		endConfidenceCrop = "" + Double.valueOf(bond.getPropertyValue()).intValue();
		System.out.println("" + Double.valueOf(bond.getPropertyValue()).intValue());
	    }
	    break;
	case CONFIDENCE_LC_TYPE:
	    BondOperator lcOp = bond.getOperator();
	    if (lcOp.equals(BondOperator.GREATER_OR_EQUAL)) {
		startConfidenceLc = "" + Double.valueOf(bond.getPropertyValue()).intValue();
		System.out.println("" + Double.valueOf(bond.getPropertyValue()).intValue());
	    } else if (lcOp.equals(BondOperator.LESS_OR_EQUAL)) {
		endConfidenceLc = "" + Double.valueOf(bond.getPropertyValue()).intValue();
		System.out.println("" + Double.valueOf(bond.getPropertyValue()).intValue());
	    }
	    break;
	// case CONFIDENCE_IRR_TYPE:
	// BondOperator irrOp = bond.getOperator();
	// if (irrOp.equals(BondOperator.GREATER_OR_EQUAL)) {
	// startConfidenceIrr = "" + Double.valueOf(bond.getPropertyValue()).intValue();
	// System.out.println("" + Double.valueOf(bond.getPropertyValue()).intValue());
	// } else if (irrOp.equals(BondOperator.LESS_OR_EQUAL)) {
	// endConfidenceIrr = "" + Double.valueOf(bond.getPropertyValue()).intValue();
	// System.out.println("" + Double.valueOf(bond.getPropertyValue()).intValue());
	// }
	// break;
	case CROP_TYPES:
	    String cropValue = bond.getPropertyValue();
	    List<String> newCropList = new ArrayList<String>();
	    if (textSearches.containsKey(CROP_TYPES)) {
		newCropList = textSearches.get(CROP_TYPES);
	    }
	    newCropList.add(cropValue);
	    textSearches.put(CROP_TYPES, newCropList);
	    break;
	case LAND_COVER_TYPES:
	    String lcValue = bond.getPropertyValue();
	    List<String> newLcList = new ArrayList<String>();
	    if (textSearches.containsKey(LAND_COVER_TYPES)) {
		newLcList = textSearches.get(LAND_COVER_TYPES);
	    }
	    newLcList.add(lcValue);
	    textSearches.put(LAND_COVER_TYPES, newLcList);
	    break;
	case IRRIGATION_TYPES:
	    String irrValue = bond.getPropertyValue();
	    List<String> newIrrList = new ArrayList<String>();
	    if (textSearches.containsKey(IRRIGATION_TYPES)) {
		newIrrList = textSearches.get(IRRIGATION_TYPES);
	    }
	    newIrrList.add(irrValue);
	    textSearches.put(IRRIGATION_TYPES, newIrrList);
	    break;

	case QUANTITY_TYPES:
	    quantity = bond.getPropertyValue();
	    break;

	case TITLE:
	case ABSTRACT:
	case KEYWORD:
	case SUBJECT:
	case ANY_TEXT:
	case PARENT_IDENTIFIER:
	    break;

	default:

	    logger.warn("Invalid bond: " + element.getName());
	    valid = false;
	}
    }

    @Override
    public void spatialBond(SpatialBond bond) {

	SpatialExtent bbox = (SpatialExtent) bond.getPropertyValue();

	double east = bbox.getEast();

	double west = bbox.getWest();

	double north = bbox.getNorth();

	double south = bbox.getSouth();

	StringBuilder builder = new StringBuilder();

	builder.append(west).append(COMMA);
	builder.append(south).append(COMMA);
	builder.append(east).append(COMMA);
	builder.append(north);

	/**
	 * bbox=-10.415,36.066,3.779,44.213
	 */

	polygon = builder.toString();
    }

    /**
     * @param s
     */
    public void setStart(int s) {

	start = s - 1;
    }

    /**
     * @param c
     */
    public void setCount(int c) {

	count = c;
    }

    public String getQueryString(String type) {

	StringBuilder builder = new StringBuilder();

	if (type.contains(WorldCerealHandler.WORLDCEREAL_BASE_URL)) {
	    // worldcereal use case
	    if (start != null) {
		builder.append(WORLDCEREAL_START_KEY).append(EQUAL).append(start).append(AND);
	    }

	    if (count != null) {
		builder.append(WORLDCEREAL_COUNT_KEY).append(EQUAL).append(count).append(AND);
	    }

	    if (polygon != null) {
		// TODO: bbox request are in this form: Bbox=minLon&Bbox=minLat&Bbox=maxLon&Bbox=maxLat
		String[] splittedBbox = polygon.split(COMMA);
		for (String box : splittedBbox) {
		    builder.append(BBOX).append(EQUAL).append(box).append(AND);
		}
	    }

	    if (startDate != null) {
		builder.append(STARTDATE_KEY).append(EQUAL).append(startDate).append(AND);
	    }

	    if (endDate != null) {
		builder.append(ENDDATE_KEY).append(EQUAL).append(endDate).append(AND);
	    }

	    for (Map.Entry<String, List<String>> entry : textSearches.entrySet()) {
		List<String> splittedTerms = entry.getValue();

		if (entry.getKey().equals(CROP_TYPES) || entry.getKey().equals(LAND_COVER_TYPES)) {
		    // split
		    for (String s : splittedTerms) {
			builder.append(EWOC_CODES).append(EQUAL).append(s).append(AND);
		    }
		} else if (entry.getKey().equals(IRRIGATION_TYPES)) {
		    // irrigationType
		    for (String s : splittedTerms) {
			builder.append(entry.getKey()).append(EQUAL).append(s).append(AND);
		    }
		}
	    }

	    if (startConfidenceCrop != null) {
		builder.append(START_CONFIDENCE_CROP).append(EQUAL).append(startConfidenceCrop).append(AND);
	    }
	    if (startConfidenceLc != null) {
		builder.append(START_CONFIDENCE_LC).append(EQUAL).append(startConfidenceLc).append(AND);
	    }
	    // if (startConfidenceIrr != null) {
	    // builder.append(START_CONFIDENCE_IRR).append(EQUAL).append(startConfidenceIrr).append(AND);
	    // }
	    // if (endConfidenceIrr != null) {
	    // builder.append(END_CONFIDENCE_IRR).append(EQUAL).append(endConfidenceIrr).append(AND);
	    // }
	    if (endConfidenceCrop != null) {
		builder.append(END_CONFIDENCE_CROP).append(EQUAL).append(endConfidenceCrop).append(AND);
	    }
	    if (endConfidenceLc != null) {
		builder.append(END_CONFIDENCE_LC).append(EQUAL).append(endConfidenceLc).append(AND);
	    }

	} else {
	    // agrostac
	    if (!textSearches.isEmpty()) {
		List<String> res = textSearches.get(CROP_TYPES);
		if (res != null && !res.isEmpty())
		    builder.append(res.get(0)).append("?");
	    }
	    
	    if(quantity != null) {
		builder.append(QUANTITY_TYPES).append(EQUAL).append(quantity).append(AND);
	    }

	    if (polygon != null) {
		// TODO: bbox request are in this form: Bbox=minLon&Bbox=minLat&Bbox=maxLon&Bbox=maxLat
		String[] splittedBbox = polygon.split(COMMA);
		builder.append(AGROSTAC_MIN_LON).append(EQUAL).append(splittedBbox[0]).append(AND);
		builder.append(AGROSTAC_MAX_LON).append(EQUAL).append(splittedBbox[2]).append(AND);
		builder.append(AGROSTAC_MIN_LAT).append(EQUAL).append(splittedBbox[1]).append(AND);
		builder.append(AGROSTAC_MAX_LAT).append(EQUAL).append(splittedBbox[4]).append(AND);
		for (String box : splittedBbox) {
		    builder.append(BBOX).append(EQUAL).append(box).append(AND);
		}
	    }
	    
	    if (start != null) {
		if(start == 0) {
		    start++;
		}
		builder.append(AGROSTAC_START_KEY).append(EQUAL).append(start).append(AND);
	    }

	    if (count != null) {
		builder.append(AGROSTAC_COUNT_KEY).append(EQUAL).append(count).append(AND);
	    }
	    
	    builder.append(AGROSTAC_ACCESS_TOKEN).append(EQUAL).append(ConfigurationWrapper.getCredentialsSetting().getAGROSTACToken());
	    
	}

	// if (startDate != null && endDate != null) {
	// builder.append(DATETIME).append(EQUAL).append(startDate).append(TIME_SEPARATOR).append(endDate).append(AND);
	// }
	//
	// if (startDate != null && endDate == null) {
	// builder.append(DATETIME).append(EQUAL).append(startDate).append(TIME_SEPARATOR).append("..").append(AND);
	// }
	//
	// if (startDate == null && endDate != null) {
	// builder.append(DATETIME).append(EQUAL).append("..").append(TIME_SEPARATOR).append(endDate).append(AND);
	// }

	return builder.toString();
    }

    @Override
    public void nonLogicalBond(Bond bond) {
    }

    @Override
    public void runtimeInfoElementBond(RuntimeInfoElementBond bond) {
    }

    @Override
    public void viewBond(ViewBond bond) {
    }

    @Override
    public void resourcePropertyBond(ResourcePropertyBond bond) {
    }

    @Override
    public void customBond(QueryableBond<String> bond) {
    }

    @Override
    public void startLogicalBond(LogicalBond bond) {
    }

    @Override
    public void separator() {
    }

    @Override
    public void endLogicalBond(LogicalBond bond) {
    }

}
