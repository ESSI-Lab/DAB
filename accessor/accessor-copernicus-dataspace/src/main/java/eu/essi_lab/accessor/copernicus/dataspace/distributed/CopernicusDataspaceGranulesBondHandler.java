package eu.essi_lab.accessor.copernicus.dataspace.distributed;

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

import org.slf4j.Logger;

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
public class CopernicusDataspaceGranulesBondHandler implements DiscoveryBondHandler {

    private final CopernicusDataspaceGranulesTemplate template;
    private Logger logger = GSLoggerFactory.getLogger(CopernicusDataspaceGranulesBondHandler.class);

    public void setCount(int count) {

	template.setCount(count);

    }

    public void setStart(int start) {
	template.setStart(start);

    }

    public void setStartTime(String dateString) {
	template.setStartTime(dateString);

    }

    public String getQueryString() {

	return template.getRequestURL();

    }

    public void setProductType(String productType) {
	template.setProductType(productType);

    }

    public void setExactCount(boolean condition) {
	template.setExactCount(condition);
    }

    public void setSort(String sort) {
	template.setSort(sort);
    }

    public void setOrder(String sortOrder) {
	template.setOrder(sortOrder);
    }

    @Override
    public void viewBond(ViewBond bond) {
	// view not supported
    }

    @Override
    public void resourcePropertyBond(ResourcePropertyBond bond) {
	// not implemented
    }

    @Override
    public void customBond(QueryableBond<String> bond) {
	// no custom so far
    }

    @Override
    public void simpleValueBond(SimpleValueBond bond) {
	MetadataElement element = bond.getProperty();

	switch (element) {
	case TITLE:

	    template.setKeyword(bond.getPropertyValue());

	    break;
	case TEMP_EXTENT_BEGIN:

	    template.setStartTime(ISO8601DateTimeUtils.getISO8601DateTime(ISO8601DateTimeUtils.parseISO8601(bond.getPropertyValue())));

	    break;

	case TEMP_EXTENT_END:

	    template.setEndTime(ISO8601DateTimeUtils.getISO8601DateTime(ISO8601DateTimeUtils.parseISO8601(bond.getPropertyValue())));

	    break;

	case CLOUD_COVER_PERC:

	    BondOperator operator = bond.getOperator();
	    if (operator.equals(BondOperator.GREATER_OR_EQUAL)) {
		template.setMinCloudCover("" + Double.valueOf(bond.getPropertyValue()).intValue());
	    } else if (operator.equals(BondOperator.LESS_OR_EQUAL)) {
		template.setMaxCloudCover("" + Double.valueOf(bond.getPropertyValue()).intValue());
	    }
	    break;
	case SENSOR_SWATH:
	    template.setSwath(bond.getPropertyValue());
	    break;
	case PRODUCT_TYPE:
	    template.setProductType(bond.getPropertyValue());
	    break;
	case PROCESSING_LEVEL_CODE:
	    break;
	case RELATIVE_ORBIT:
	    template.setRelativeOrbit(bond.getPropertyValue());
	    break;
	// TODO: check
	case SENSOR_OP_MODE:
	    template.setSensorMode(bond.getPropertyValue());
	    break;
	case EOP_POLARIZATION_MODE:
	case SAR_POL_CH:
	    template.setPolarisation(bond.getPropertyValue());
	    break;
	case S3_TIMELINESS:
	    template.setTimeliness(bond.getPropertyValue());
	    break;
	case S3_INSTRUMENT_IDX:
	    template.setInstrument(bond.getPropertyValue());
	    break;
	case S3_PRODUCT_LEVEL:
	    template.setProcessingLevel(bond.getPropertyValue());
	    break;
	default:

	    logger.warn("Ignoring unsupported element {}", element);

	}
    }

    @Override
    public void spatialBond(SpatialBond bond) {
	SpatialExtent bbox = (SpatialExtent) bond.getPropertyValue();

	double east = bbox.getEast();

	double west = bbox.getWest();

	double north = bbox.getNorth();

	double south = bbox.getSouth();

	template.setBBox("" + south, "" + north, "" + west, "" + east);

    }

    @Override
    public void startLogicalBond(LogicalBond bond) {

	// ignoring logical, aal in AND
    }

    @Override
    public void separator() {
	// ignoring logical, aal in AND
    }

    @Override
    public void endLogicalBond(LogicalBond bond) {
	// ignoring logical, aal in AND
    }

    public CopernicusDataspaceGranulesBondHandler(String templateUrl) {
	template = new CopernicusDataspaceGranulesTemplate(templateUrl);

    }

    @Override
    public void nonLogicalBond(Bond bond) {
	// TODO Auto-generated method stub

    }

    @Override
    public void runtimeInfoElementBond(RuntimeInfoElementBond bond) {
	// TODO Auto-generated method stub

    }

}