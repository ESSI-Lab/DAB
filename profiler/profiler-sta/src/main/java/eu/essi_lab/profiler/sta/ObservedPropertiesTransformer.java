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
import java.util.Optional;
import java.util.Set;

import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResourceSelector;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.SortedFields;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.SortOrder;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.profiler.sta.STARequest.EntitySet;

/**
 * Transformer for STA ObservedProperties entity set.
 * Uses distinct UNIQUE_ATTRIBUTE_IDENTIFIER; maps ATTRIBUTE_TITLE for name.
 */
public class ObservedPropertiesTransformer extends STATransformer {

    @Override
    protected void applyEntitySpecificFilterBonds(String filter, Set<Bond> operands) {
	addEntityIdFilter(filter, "ObservedProperty", MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER, operands);
	addIdFilter(filter, MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER, operands);
    }

    @Override
    protected Optional<Queryable> getDistinctElement(WebRequest request) {
	return Optional.of(MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER);
    }

    @Override
    protected Optional<SortedFields> getSortedFields() {
	return Optional.of(new SortedFields(Arrays.asList(
		new SimpleEntry<>(MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER, SortOrder.ASCENDING))));
    }

    @Override
    protected ResourceSelector getSelector(WebRequest request) {
	ResourceSelector selector = super.getSelector(request);
	selector.setSubset(ResourceSubset.CORE_EXTENDED);
	selector.setIndexesPolicy(IndexesPolicy.ALL);
	selector.addIndex(MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER);
	selector.addIndex(MetadataElement.ATTRIBUTE_TITLE);
	selector.addIndex(MetadataElement.ATTRIBUTE_DESCRIPTION);
	selector.addIndex(MetadataElement.OBSERVED_PROPERTY_URI);
	return selector;
    }

    @Override
    protected Page getPage(WebRequest request) throws GSException {
	STARequest staRequest = new STARequest(request);
	if (staRequest.getEntitySet().orElse(null) == EntitySet.ObservedProperties
		&& staRequest.getEntityIdNormalized().map(id -> id.matches("\\d+")).orElse(false)) {
	    return new Page(1, 10000);
	}
	return super.getPage(request);
    }
}
