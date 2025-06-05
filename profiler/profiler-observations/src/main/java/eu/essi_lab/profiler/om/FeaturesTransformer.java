package eu.essi_lab.profiler.om;

import eu.essi_lab.messages.DiscoveryMessage;

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

import eu.essi_lab.messages.ResourceSelector;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;

public class FeaturesTransformer extends OMTransformer {

    @Override
    protected ResourceSelector getSelector(WebRequest request) {

	ResourceSelector selector = new ResourceSelector();
	selector.setSubset(ResourceSubset.HARMONIZED);
	selector.setIndexesPolicy(IndexesPolicy.NONE);
	selector.addIndex(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER);
	selector.addIndex(MetadataElement.PLATFORM_TITLE);
	selector.addIndex(MetadataElement.PLATFORM_IDENTIFIER);
	selector.addIndex(ResourceProperty.SOURCE_ID);
	selector.addIndex(MetadataElement.BOUNDING_BOX);
	selector.addIndex(MetadataElement.COUNTRY);
	selector.setIncludeOriginal(false);
	return selector;
    }

    @Override
    public DiscoveryMessage transform(WebRequest request) throws GSException {
	DiscoveryMessage ret = super.transform(request);
	ret.setDistinctValuesElement(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER);
	ret.setExcludeResourceBinary(true);
	return ret;
    }

}
