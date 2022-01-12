package eu.essi_lab.augmenter;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.Arrays;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.configuration.option.GSConfOptionString;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.AugmentedMetadataElement;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
public class MetadataAugmenter extends ResourceAugmenter {

    /**
     * 
     */
    private static final long serialVersionUID = 7412866302816222900L;

    @JsonIgnore
    private static final String METADATA_AUGMENTER_TARGET_KEY = "METADATA_AUGMENTER_TARGET_KEY";

    public MetadataAugmenter() {

	setLabel("Metadata augmenter");

	GSConfOptionString option = new GSConfOptionString();
	option.setLabel("Metadata to augment");
	option.setAllowedValues(Arrays.asList(new String[] { "Title", "Abstract" }));
	option.setValue("Title");
	option.setKey(METADATA_AUGMENTER_TARGET_KEY);

	getSupportedOptions().put(METADATA_AUGMENTER_TARGET_KEY, option);
    }

    @Override
    public  Optional<GSResource> augment(GSResource resource) throws GSException {

	GSLoggerFactory.getLogger(getClass()).warn("Metadata augmentation of current resource STARTED");

	String value = null;
	MetadataElement el = null;

	if (getSupportedOptions().get(METADATA_AUGMENTER_TARGET_KEY).getValue().equals("Title")) {
	    value = resource.getHarmonizedMetadata().getCoreMetadata().getTitle();
	    el = MetadataElement.TITLE;
	} else {
	    value = resource.getHarmonizedMetadata().getCoreMetadata().getAbstract();
	    el = MetadataElement.ABSTRACT;
	}

	String augmentedValue = value + " - " + ISO8601DateTimeUtils.getISO8601DateTime();

	AugmentedMetadataElement element = new AugmentedMetadataElement();
	element.setName(el);
	element.setOldValue(value);
	element.setNewValue(augmentedValue);
	element.setUpdateTimeStamp();
	element.setIdentifier(getClass().getCanonicalName());

	resource.getHarmonizedMetadata().getAugmentedMetadataElements().add(element);

	if (getSupportedOptions().get(METADATA_AUGMENTER_TARGET_KEY).getValue().equals("Title")) {

	    resource.getHarmonizedMetadata().getCoreMetadata().setTitle(augmentedValue);
	} else {

	    resource.getHarmonizedMetadata().getCoreMetadata().setAbstract(augmentedValue);
	}

	GSLoggerFactory.getLogger(getClass()).warn("Metadata augmentation of current resource ENDED");
	
	return Optional.of(resource);
    }

    @Override
    public void onOptionSet(GSConfOption<?> option) throws GSException {

    }

    @Override
    public void onFlush() throws GSException {
    }
}
