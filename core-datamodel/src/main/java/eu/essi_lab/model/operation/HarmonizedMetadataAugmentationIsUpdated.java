package eu.essi_lab.model.operation;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import eu.essi_lab.model.resource.AugmentedMetadataElement;
import eu.essi_lab.model.resource.HarmonizedMetadata;

public class HarmonizedMetadataAugmentationIsUpdated implements HarmonizedMetadataOperation {

    private List<AugmentedMetadataElement> augmentedMetadataElements;

    @Override
    public void perform(HarmonizedMetadata harmonizedMetadata) {
	this.augmentedMetadataElements = harmonizedMetadata.getAugmentedMetadataElements();
    }

    public boolean hasUpdatedAugmentedMetadata() {
	Map<String, Long> countReport = augmentedMetadataElements.stream()
		.collect(Collectors.groupingBy(AugmentedMetadataElement::getName, Collectors.counting()));
	for (Entry<String, Long> entry : countReport.entrySet()) {
	    if (entry.getValue() > 1)
		return true;
	}
	return false;
    }
}
