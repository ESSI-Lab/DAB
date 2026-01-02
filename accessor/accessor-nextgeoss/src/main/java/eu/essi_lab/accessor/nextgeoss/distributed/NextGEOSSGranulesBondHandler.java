package eu.essi_lab.accessor.nextgeoss.distributed;

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

import org.slf4j.Logger;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.QueryableBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.RuntimeInfoElementBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.SpatialBond;
import eu.essi_lab.messages.bond.spatial.SpatialExtent;
import eu.essi_lab.messages.bond.ViewBond;
import eu.essi_lab.messages.bond.parser.DiscoveryBondHandler;
import eu.essi_lab.model.resource.MetadataElement;

/**
 * @author roncella
 */
public class NextGEOSSGranulesBondHandler implements DiscoveryBondHandler {

    private final NextGEOSSGranulesTemplate template;
    private Logger logger = GSLoggerFactory.getLogger(NextGEOSSGranulesBondHandler.class);

    public void setCount(int count) {

	template.setCount(count);

    }

    public void setStart(int start) {
	template.setStart(start);

    }

    public String getQueryString() {

	return template.getRequestURL();

    }
    
    public void setProductType(String productType) {
	template.setProductType(productType);
	
    }

    @Override
    public void viewBond(ViewBond bond) {
	//view not supported
    }

    @Override
    public void resourcePropertyBond(ResourcePropertyBond bond) {
	//not implemented
    }

    @Override
    public void customBond(QueryableBond<String> bond) {
	//no custom so far
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

	//ignoring logical, aal in AND
    }

    @Override
    public void separator() {
	//ignoring logical, aal in AND
    }

    @Override
    public void endLogicalBond(LogicalBond bond) {
	//ignoring logical, aal in AND
    }

    public NextGEOSSGranulesBondHandler(String templateUrl) {
	template = new NextGEOSSGranulesTemplate(templateUrl);

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
