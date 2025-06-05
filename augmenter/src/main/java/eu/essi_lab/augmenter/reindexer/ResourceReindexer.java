package eu.essi_lab.augmenter.reindexer;

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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import eu.essi_lab.augmenter.ResourceAugmenter;
import eu.essi_lab.indexes.IndexedElementsWriter;
import eu.essi_lab.indexes.IndexedMetadataElements;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.index.IndexedMetadataElement;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;

/**
 * Augmenter developed in order to reindex an existing metadata. This can be useful in case of an updated procedure to
 * generate the index values (i.e. to be found in IndexedMetadataElement)
 * 
 * @author boldrini
 */
public class ResourceReindexer extends ResourceAugmenter<ResourceReindexerSetting> {

    /**
    * 
    */
    public ResourceReindexer() {
    }

    /**
     * @param setting
     */
    public ResourceReindexer(ResourceReindexerSetting setting) {

	super(setting);
    }

    @Override
    public Optional<GSResource> augment(GSResource resource) throws GSException {

	GSLoggerFactory.getLogger(getClass()).warn("Metadata reindexing of current resource STARTED");

	List<MetadataElement> selectedElements = getSetting().getSelectedElements();

	List<IndexedMetadataElement> toReindex = IndexedMetadataElements.//
		getIndexes().//
		stream().//
		filter(el -> el.getMetadataElement().isPresent()).//
		filter(el -> selectedElements.contains(el.getMetadataElement().get())).//
		collect(Collectors.toList());

	GSLoggerFactory.getLogger(getClass()).info("Working with #" + toReindex.size() + " set of indexes");

	if (toReindex.size() == 1) {

	    GSLoggerFactory.getLogger(getClass()).info("Index name: " + toReindex.get(0).getElementName());
	}

	IndexedElementsWriter.indexMetadataElements(resource, toReindex);

	GSLoggerFactory.getLogger(getClass()).warn("Metadata reindexing of current resource ENDED");

	return Optional.of(resource);
    }

    @Override
    protected String initName() {

	return "Resource reindexer";
    }

    @Override
    public String getType() {

	return "ResourceReindexer";
    }

    @Override
    protected ResourceReindexerSetting initSetting() {

	return new ResourceReindexerSetting();
    }
}
