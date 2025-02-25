package eu.essi_lab.indexes;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.index.IndexedElement;
import eu.essi_lab.model.index.IndexedMetadataElement;
import eu.essi_lab.model.pluggable.PluginsLoader;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.HarmonizedMetadata;
import eu.essi_lab.model.resource.ResourcePropertyHandler;

/**
 * Utility class which writes {@link IndexedElement}s in to a {@link GSResource}.<br>
 * {@link GSResource} indexing is the process of adding the value of {@link IndexedElement}s to the
 * {@link HarmonizedMetadata#getIndexesMetadata()}
 * 
 * @author Fabrizio
 */
public class IndexedElementsWriter {

    /**
     * Writes all the {@link IndexedMetadataElements} to the supplied <code>resource</code>. It also set resource time
     * stamp and metadata quality with the methods {@link ResourcePropertyHandler#setResourceTimeStamp()} and
     * {@link ResourcePropertyHandler#setMetadataQuality()}
     * 
     * @param resource the {@link GSResource} to index
     */
    public synchronized static void write(GSResource resource) {

	// ----------------------------------
	//
	// set the current time stamp
	//
	resource.getPropertyHandler().setResourceTimeStamp();

	// ----------------------------------
	//
	// add the source deployment
	//
	List<String> deployment = resource.getSource().getDeployment();
	deployment.forEach(dep -> resource.getPropertyHandler().addSourceDeployment(dep));

	// --------------------------
	//
	// metadata quality
	//
	try {
	    if (!resource.getPropertyHandler().getMetadataQuality().isPresent()) {

		resource.getPropertyHandler().setMetadataQuality();
	    }
	} catch (Exception e) {
	    // this try catch is needed because in case of an error the harvesting procedure can continue, instead of
	    // being stopped
	    GSLoggerFactory.getLogger(IndexedElementsWriter.class).error(e);
	}

	// all default indexed elements
	List<IndexedMetadataElement> indexes = IndexedMetadataElements.getIndexes();
	indexMetadataElements(resource, indexes);

	// ---------------------------------------
	//
	// custom indexes
	//

	PluginsLoader<CustomIndexedElements> loader = new PluginsLoader<>();
	List<CustomIndexedElements> plugins = loader.loadPlugins(CustomIndexedElements.class);

	for (CustomIndexedElements p : plugins) {
	    p.getIndexes().stream().//
	    // excludes indexes with invalid names
		    filter(index -> CustomIndexedElements.checkName(index.getElementName())).//
		    forEach(index -> { //
			try {
			    index.defineValues(resource);
			    resource.getIndexesMetadata().write(index);
			} catch (Exception e) {
			    GSLoggerFactory.getLogger(IndexedElementsWriter.class).error(e);
			}
		    });
	}
    }

    public synchronized static void indexMetadataElements(GSResource resource, List<IndexedMetadataElement> indexes) {
	// -------------------------------------
	//
	// indexed metadata elements
	//
	indexes.//
		stream().//
		forEach(index -> { //
		    // ----------------------------
		    //
		    // this is required since the elements are defined as static in the
		    // grouping classes, so the list is shared. this avoid to store
		    // values from a previous stored resource
		    //
		    index.getValues().clear();
		    index.setBoundingBox(null);
		    //
		    // ----------------------------

		    try {
			index.defineValues(resource);

			if (index.getBoundingBox() != null || !index.getValues().isEmpty()) {
			    String elementName = index.getElementName();
			    if (elementName != null) {
				resource.getIndexesMetadata().remove(elementName);
			    }
			    resource.getIndexesMetadata().write(index);
			}
		    } catch (Exception e) {
			// this try catch is needed because in case of an error the harvesting procedure can continue,
			// instead of being stopped
			GSLoggerFactory.getLogger(IndexedElementsWriter.class).error(e);
		    }
		});

    }
}
