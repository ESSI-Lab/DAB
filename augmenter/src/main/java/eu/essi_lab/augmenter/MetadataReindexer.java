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
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.essi_lab.indexes.IndexedElementsWriter;
import eu.essi_lab.indexes.IndexedMetadataElements;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.configuration.option.GSConfOptionString;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.index.IndexedMetadataElement;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
public class MetadataReindexer extends ResourceAugmenter {

    @JsonIgnore
    private static final String METADATA_REINDEXER_TARGET_KEY = "METADATA_REINDEXER_TARGET_KEY";
    private static final String ALL = "All";

    public MetadataReindexer() {

	setLabel("Metadata reindexer");

	GSConfOptionString option = new GSConfOptionString();
	option.setLabel("Metadata to reindex");
	MetadataElement[] metadataValues = MetadataElement.values();
	String[] toReindex = new String[metadataValues.length + 1];
	toReindex[0] = ALL; 
	for (int i = 0; i < metadataValues.length; i++) {
	    MetadataElement me = metadataValues[i];
	    toReindex[i+1] = me.getName();
	}
	option.setAllowedValues(Arrays.asList(toReindex));
	option.setValue(ALL);
	option.setKey(METADATA_REINDEXER_TARGET_KEY);

	getSupportedOptions().put(METADATA_REINDEXER_TARGET_KEY, option);
    }

    @Override
    public Optional<GSResource> augment(GSResource resource) throws GSException {

	GSLoggerFactory.getLogger(getClass()).warn("Metadata reindexing of current resource STARTED");

	GSConfOption<?> option = getSupportedOptions().get(METADATA_REINDEXER_TARGET_KEY);

	List<IndexedMetadataElement> toReindex = IndexedMetadataElements.getIndexes();
	if (option != null && !option.getValue().equals(ALL)) {
	    MetadataElement targetElement = MetadataElement.fromName(option.getValue().toString());
	    Iterator<IndexedMetadataElement> toReindexIterator = toReindex.iterator();
	    while (toReindexIterator.hasNext()) {
		IndexedMetadataElement indexedMetadataElement = (IndexedMetadataElement) toReindexIterator.next();
		Optional<MetadataElement> optionalMetadataElement = indexedMetadataElement.getMetadataElement();
		if (optionalMetadataElement.isPresent()) {
		    MetadataElement tmpMetadataElement = optionalMetadataElement.get();
		    if (!tmpMetadataElement.equals(targetElement)) {
			toReindexIterator.remove();
		    }
		} else {
		    toReindexIterator.remove();
		}
	    }
	}

	GSLoggerFactory.getLogger(getClass()).info("Working with #" + toReindex.size() + " set of indexes");
	if (toReindex.size() == 1) {
	    GSLoggerFactory.getLogger(getClass()).info("Index name: " + toReindex.get(0).getElementName());
	}

	IndexedElementsWriter.indexMetadataElements(resource, toReindex);

	GSLoggerFactory.getLogger(getClass()).warn("Metadata reindexing of current resource ENDED");

	return Optional.of(resource);
    }

    @Override
    public void onOptionSet(GSConfOption<?> option) throws GSException {

    }

    @Override
    public void onFlush() throws GSException {
    }
}
