package eu.essi_lab.augmenter.metadata;

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

import java.util.List;
import java.util.Optional;

import eu.essi_lab.augmenter.ResourceAugmenter;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.AugmentedMetadataElement;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;

/**
 * Augmenter developed in order to allow testing of harvester configuration capabilities. It allows to select the
 * {@link MetadataElement} to augment
 * ( {@link MetadataElement#TITLE} or {@link MetadataElement#ABSTRACT} ) and adds a time stamp to it
 * 
 * @author Fabrizio
 */
public class MetadataAugmenter extends ResourceAugmenter<MetadataAugmenterSetting> {

    /**
     * 
     */
    public MetadataAugmenter() {

	super();
    }

    /**
     * @param setting
     */
    public MetadataAugmenter(MetadataAugmenterSetting setting) {

	super(setting);
    }

    @SuppressWarnings("incomplete-switch")
    @Override
    public Optional<GSResource> augment(GSResource resource) throws GSException {

	GSLoggerFactory.getLogger(getClass()).warn("Metadata augmentation of current resource STARTED");

	List<MetadataElement> selectedElements = getSetting().getSelectedElements();

	for (MetadataElement el : selectedElements) {

	    String value = null;
	    String augmentedValue = null;

	    switch (el) {
	    case TITLE:
		value = resource.getHarmonizedMetadata().getCoreMetadata().getTitle();
		augmentedValue = value + " - AUGMENTED-TITLE";
		resource.getHarmonizedMetadata().getCoreMetadata().setTitle(augmentedValue);
		break;
	    case ABSTRACT:
		value = resource.getHarmonizedMetadata().getCoreMetadata().getAbstract();
		augmentedValue = value + " - AUGMENTED-ABSTRACT";
		resource.getHarmonizedMetadata().getCoreMetadata().setAbstract(augmentedValue);
		break;
	    }

	    AugmentedMetadataElement element = new AugmentedMetadataElement();
	    element.setName(el);
	    element.setOldValue(value);
	    element.setNewValue(augmentedValue);
	    element.setUpdateTimeStamp();
	    element.setIdentifier(getClass().getCanonicalName());

	    resource.getHarmonizedMetadata().getAugmentedMetadataElements().add(element);
	}

	GSLoggerFactory.getLogger(getClass()).warn("Metadata augmentation of current resource ENDED");

	return Optional.of(resource);
    }

    @Override
    public String getType() {

	return "MetadataAugmenter";
    }

    @Override
    protected String initName() {

	return "Metadata augmenter";
    }

    @Override
    protected MetadataAugmenterSetting initSetting() {

	return new MetadataAugmenterSetting();
    }
}
