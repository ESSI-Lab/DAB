package eu.essi_lab.model.operation;

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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import eu.essi_lab.model.resource.AugmentedMetadataElement;
import eu.essi_lab.model.resource.HarmonizedMetadata;

/**
 * Given an AugmentedMetadataElement list and a name, the operation return all
 * the elements in list which name match with the given element name.
 * 
 * @author pezzati
 */
public class RetreiveAugmentedMetadataElements implements HarmonizedMetadataOperation {

    private List<AugmentedMetadataElement> augmentedMetadataElements;

    @Override
    public void perform(HarmonizedMetadata harmonizedMetadata) {
	this.augmentedMetadataElements = harmonizedMetadata.getAugmentedMetadataElements();
    }

    /**
     * Retreive all the augmentedElement with attribute name's value equals to
     * parameter elementName.
     * 
     * @param elementName name of the wanted elements.
     * @return a list of all {@link AugmentedMetadataElement}s that match by
     *         name.
     */
    public List<AugmentedMetadataElement> getElementsByName(String elementName) {
	List<AugmentedMetadataElement> retreivedElements = new ArrayList<>();
	retreivedElements.addAll(augmentedMetadataElements.stream()
		.filter(augmentedMetadataElement -> augmentedMetadataElement.getName().equals(elementName)).collect(Collectors.toList()));
	return retreivedElements;
    }
}
