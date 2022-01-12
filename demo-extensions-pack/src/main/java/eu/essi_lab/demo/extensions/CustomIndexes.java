package eu.essi_lab.demo.extensions;

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

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import eu.essi_lab.api.database.Database.DatabaseImpl;
import eu.essi_lab.indexes.CustomIndexedElements;
import eu.essi_lab.indexes.marklogic.MarkLogicIndexTypes;
import eu.essi_lab.indexes.marklogic.MarkLogicScalarType;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.Distribution;
import eu.essi_lab.lib.utils.StreamUtils;
import eu.essi_lab.model.index.IndexedElementInfo;
import eu.essi_lab.model.index.IndexedMetadataElement;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.GSResource;
public class CustomIndexes extends CustomIndexedElements {

    /**
     * 
     */
    public static final IndexedMetadataElement ONLINE_NAME = new IndexedMetadataElement(CustomQueryable.ONLINE_NAME.getName()) {

	@Override
	public void defineValues(GSResource resource) {

	    Distribution distribution = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDistribution();

	    if (distribution == null) {
		return;
	    }

	    StreamUtils.iteratorToStream(distribution.getDistributionOnlines()).//
	    map(o -> o.getName()).//
	    filter(n -> checkStringValue(n)).//
	    forEach(n -> getValues().add(n));//
	}
    };

    /**
     * 
     */
    public static final IndexedMetadataElement CONTACT_CITY = new IndexedMetadataElement(CustomQueryable.CONTACT_CITY.getName()) {

	@Override
	public void defineValues(GSResource resource) {

	    Iterator<DataIdentification> identifications = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		    .getDataIdentifications();

	    StreamUtils.iteratorToStream(identifications).//
	    map(i -> i.getPointOfContact()).//
	    filter(Objects::nonNull).//
	    map(p -> p.getContact()).//
	    filter(Objects::nonNull).//
	    map(c -> c.getAddress()).//
	    filter(Objects::nonNull).//
	    map(a -> a.getCity()).//
	    filter(c -> checkStringValue(c)).//
	    forEach(c -> getValues().add(c));
	}
    };

    @Override
    public Provider getProvider() {

	return DemoProvider.getInstance();
    }

    @Override
    public List<IndexedMetadataElement> getIndexes() {

	return getIndexes(getClass()).//
		stream().//
		map(index -> (IndexedMetadataElement) index).//
		peek(index -> index.getInfoList().add(//
			new IndexedElementInfo(//
				index.getElementName(), //
				DatabaseImpl.MARK_LOGIC.getName(), //
				MarkLogicIndexTypes.RANGE_ELEMENT_INDEX.getType(), //
				MarkLogicScalarType.STRING.getType())))
		.//
		collect(Collectors.toList());//
    }
}
