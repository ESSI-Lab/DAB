package eu.essi_lab.demo.extensions.indexes;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import eu.essi_lab.api.database.Database.DatabaseImpl;
import eu.essi_lab.demo.extensions.DemoProvider;
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

/**
 * This class provides 2 custom indexed elements related to the queryables provided by the {@link CustomQueryable}
 * enum
 * 
 * @author Fabrizio
 */
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
