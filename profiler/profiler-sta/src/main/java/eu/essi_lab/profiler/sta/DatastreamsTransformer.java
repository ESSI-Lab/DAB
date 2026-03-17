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

package eu.essi_lab.profiler.sta;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import eu.essi_lab.messages.ResourceSelector;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.SortedFields;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.SortOrder;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.profiler.sta.STARequest.EntitySet;

/**
 * Transformer for STA Datastreams entity set (timeseries records).
 * Uses retrieveStrings (full resources) like Observations.
 * When parent is ObservedProperties(id)/Datastreams, adds UNIQUE_ATTRIBUTE_IDENTIFIER constraint.
 */
public class DatastreamsTransformer extends STATransformer {

    public static final String ATTR_OBSERVED_PROPERTY_CODE = "staObservedPropertyCode";

    @Override
    protected void applyEntitySpecificFilterBonds(String filter, Set<Bond> operands) {
	addEntityIdFilter(filter, "Datastream", MetadataElement.ONLINE_ID, operands);
	addIdFilter(filter, MetadataElement.ONLINE_ID, operands);
    }

    @Override
    protected Bond getUserBond(WebRequest request) throws GSException {
	Bond base = super.getUserBond(request);
	STARequest staRequest = new STARequest(request);
	if (staRequest.getEntitySet().orElse(null) == EntitySet.ObservedProperties
		&& "Datastreams".equals(staRequest.getNavigationProperty().orElse(null))) {
	    String attributeCode = (String) request.getServletRequest().getAttribute(ATTR_OBSERVED_PROPERTY_CODE);
	    if (attributeCode == null || attributeCode.isEmpty()) {
		attributeCode = staRequest.getEntityIdNormalized().filter(id -> !id.matches("\\d+")).orElse(null);
	    }
	    if (attributeCode != null && !attributeCode.isEmpty()) {
		Set<Bond> operands = new HashSet<>();
		if (base != null) {
		    operands.add(base);
		}
		operands.add(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER,
			attributeCode));
		return operands.size() == 1 ? operands.iterator().next() : BondFactory.createAndBond(operands);
	    }
	}
	return base;
    }

    @Override
    protected Optional<eu.essi_lab.model.Queryable> getDistinctElement(WebRequest request) {
	return Optional.empty();
    }

    @Override
    protected Optional<SortedFields> getSortedFields() {
	return Optional.of(new SortedFields(Arrays.asList(
		new SimpleEntry<>(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER, SortOrder.ASCENDING),
		new SimpleEntry<>(MetadataElement.ONLINE_ID, SortOrder.ASCENDING))));
    }

    @Override
    protected ResourceSelector getSelector(WebRequest request) {
	ResourceSelector selector = super.getSelector(request);
	selector.setSubset(ResourceSubset.CORE_EXTENDED);
	selector.setIndexesPolicy(IndexesPolicy.ALL);
	selector.addIndex(MetadataElement.ONLINE_ID);
	selector.addIndex(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER);
	selector.addIndex(MetadataElement.TITLE);
	selector.addIndex(MetadataElement.PLATFORM_TITLE);
	selector.addIndex(MetadataElement.ATTRIBUTE_TITLE);
	selector.addIndex(MetadataElement.TEMP_EXTENT_BEGIN);
	selector.addIndex(MetadataElement.TEMP_EXTENT_END);
	selector.addIndex(MetadataElement.TEMP_EXTENT_END_NOW);
	selector.addIndex(MetadataElement.BOUNDING_BOX);
	selector.addIndex(MetadataElement.ATTRIBUTE_UNITS);
	selector.addIndex(MetadataElement.ATTRIBUTE_UNITS_ABBREVIATION);
	selector.addIndex(MetadataElement.UNIQUE_INSTRUMENT_IDENTIFIER);
	selector.addIndex(MetadataElement.INSTRUMENT_TITLE);
	return selector;
    }
}
