package eu.essi_lab.messages;

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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import eu.essi_lab.messages.ResourceSelector.ExtendedElementsPolicy;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.HarmonizedMetadata;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;

/**
 * Allows to select specific elements of the discovered {@link GSResource}s in order to speed up the unmarshal time
 * 
 * @author Fabrizio
 */
public class ResourceSelector implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -7516805077469988112L;
    private boolean includeOriginal;

    /**
     * Defines policies to select indexes of the {@link GSResource}. The use of a minimum set can highly improve the
     * unmarshal time
     * 
     * @see ResourceSelector#addIndex(Queryable)
     * @author Fabrizio
     */
    public enum IndexesPolicy {

	/**
	 * Select all indexes of the {@link GSResource}
	 */
	ALL,
	/**
	 * Select no indexes
	 */
	NONE,
	/**
	 * Select only indexes related to a specific set of {@link ResourceProperty}
	 */
	PROPERTIES
    }

    /**
     * Defines policies to select extended elements of the {@link GSResource}. The use of a minimum set can somewhat
     * improve the
     * response time being the payload smaller
     * 
     * @see ResourceSelector#addExtendedElement(MetadataElement)
     * @author Fabrizio
     */
    public enum ExtendedElementsPolicy {

	/**
	 * Select all extended elements of the {@link GSResource}
	 */
	ALL,
	/**
	 * Select no extended elements
	 */
	NONE
    }

    /**
     * Defines policies to select specific elements of the {@link GSResource}. The use of a minimum set can highly
     * improve the unmarshal time
     * 
     * @author Fabrizio
     */
    public enum ResourceSubset {

	/**
	 * <ul>
	 * <li>{@link GSResource#getSource()}</li>
	 * <li>{@link HarmonizedMetadata#getCoreMetadata()}</li>
	 * <li>{@link HarmonizedMetadata#getAugmentedMetadataElements()}</li>
	 * <li>{@link HarmonizedMetadata#getExtendedMetadata()}</li>
	 * </ul>
	 */
	FULL,

	/**
	 * <ul>
	 * <li>{@link GSResource#getSource()}</li>
	 * </ul>
	 */
	NO_HARMONIZED,

	/**
	 * <ul>
	 * <li>{@link GSResource#getSource()}</li>
	 * <li>{@link HarmonizedMetadata#getCoreMetadata()}</li>
	 * </ul>
	 */
	SOURCE_CORE,

	/**
	 * <ul>
	 * <li>{@link GSResource#getSource()}</li>
	 * <li>{@link HarmonizedMetadata#getAugmentedMetadataElements()}</li>
	 * <li>{@link HarmonizedMetadata#getExtendedMetadata()}</li>
	 * </ul>
	 */
	NO_CORE,

	/**
	 * <ul>
	 * <li>{@link GSResource#getSource()}</li>
	 * <li>{@link HarmonizedMetadata#getCoreMetadata()}</li>
	 * <li>{@link HarmonizedMetadata#getExtendedMetadata()}</li>
	 * </ul>
	 */
	NO_AUGMENTED,

	/**
	 * <ul>
	 * <li>{@link GSResource#getSource()}</li>
	 * <li>{@link HarmonizedMetadata#getCoreMetadata()}</li>
	 * <li>{@link HarmonizedMetadata#getAugmentedMetadataElements()}</li>
	 * </ul>
	 */
	NO_EXTENDED,

	/**
	 * <ul>
	 * <li>{@link HarmonizedMetadata#getCoreMetadata()}</li>
	 * <li>{@link HarmonizedMetadata#getAugmentedMetadataElements()}</li>
	 * <li>{@link HarmonizedMetadata#getExtendedMetadata()}</li>
	 * </ul>
	 */
	NO_SOURCE,

	/**
	 * <ul>
	 * <li>{@link GSResource#getSource()}</li>
	 * </ul>
	 */
	SOURCE,

	/**
	 * <ul>
	 * <li>{@link HarmonizedMetadata#getCoreMetadata()}</li>
	 * <li>{@link HarmonizedMetadata#getAugmentedMetadataElements()}</li>
	 * <li>{@link HarmonizedMetadata#getExtendedMetadata()}</li>
	 * </ul>
	 */
	HARMONIZED,

	/**
	 * <ul>
	 * <li>{@link HarmonizedMetadata#getCoreMetadata()}</li>
	 * AUGEMENTED
	 * </ul>
	 */
	CORE_AUGMENTED,

	/**
	 * <ul>
	 * <li>{@link HarmonizedMetadata#getCoreMetadata()}</li>
	 * <li>{@link HarmonizedMetadata#getExtendedMetadata()}</li>
	 * </ul>
	 */
	CORE_EXTENDED,

	/**
	 * <ul>
	 * <li>{@link HarmonizedMetadata#getAugmentedMetadataElements()}</li>
	 * <li>{@link HarmonizedMetadata#getExtendedMetadata()}</li>
	 * </ul>
	 */
	AUGMENTED_EXTENDED,

	/**
	 * <ul>
	 * <li>{@link HarmonizedMetadata#getCoreMetadata()}</li>
	 * </ul>
	 */
	CORE,

	/**
	 * <ul>
	 * <li>{@link HarmonizedMetadata#getAugmentedMetadataElements()}</li>
	 * </ul>
	 */
	AUGMENTED,

	/**
	 * <ul>
	 * <li>{@link HarmonizedMetadata#getExtendedMetadata()}</li>
	 * </ul>
	 */
	EXTENDED,
	/**
	 * no metadata selected by default
	 */
	NONE

    }

    private List<Queryable> quaryableList;
    private List<String> extendedElementList = new ArrayList<>();
    private ResourceSubset subset;
    private IndexesPolicy indexesPolicy;
    private ExtendedElementsPolicy extendedElementsPolicy = null;

    /**
     * Creates new instance of {@link ResourceSelector} with {@link ResourceSubset#FULL} and {@link IndexesPolicy#ALL}
     */
    public ResourceSelector() {

	quaryableList = Lists.newArrayList();

	includeOriginal = true;

	setSubset(ResourceSubset.FULL);
	setIndexesPolicy(IndexesPolicy.ALL);
    }

    /**
     * @return
     */
    public boolean isOriginalIncluded() {

	return includeOriginal;
    }

    /**
     * @param includeOriginal
     */
    public void setIncludeOriginal(boolean includeOriginal) {

	this.includeOriginal = includeOriginal;
    }

    /**
     * @param subset
     */
    public void setSubset(ResourceSubset subset) {

	this.subset = subset;
    }

    /**
     * @param policy
     */
    public void setIndexesPolicy(IndexesPolicy policy) {

	this.indexesPolicy = policy;
    }

    /**
     * @param policy
     */
    public void setExtendedElementsPolicy(ExtendedElementsPolicy policy) {

	this.extendedElementsPolicy = policy;
    }

    /**
     * Select the index related to the supplied <code>queryable</code>.<br>
     * 
     * @usageNote
     *            This method overrides any preceding or succeeding call of the {@link #setIndexesPolicy(IndexesPolicy)}
     *            method
     * @param queryable
     */
    public void addIndex(Queryable queryable) throws IllegalArgumentException {

	if (queryable.isVolatile()) {
	    throw new IllegalArgumentException("Volatile queryables are not selectable: " + queryable.getName());
	}

	quaryableList.add(queryable);
    }

    /**
     * Select the extended element related to the supplied <code>metadata element</code>.<br>
     * 
     * @usageNote
     *            This method overrides any preceding or succeeding call of the
     *            {@link #setExtendedElementPolicy(ExtendedElementsPolicy)}
     *            method
     * @param queryable
     */
    public void addExtendedElement(MetadataElement element) throws IllegalArgumentException {

	if (!element.isExtendedElement()) {
	    throw new IllegalArgumentException("Only metadata elements that are extended elements are selectable: " + element.getName());
	}

	extendedElementList.add(element.getName());
    }

    public void addExtendedElement(String extendedElementName) throws IllegalArgumentException {

	extendedElementList.add(extendedElementName);
    }

    /**
     * Returns the {@link ResourceSubset} (default: {@link ResourceSubset#FULL}
     */
    public ResourceSubset getSubset() {

	return subset;
    }

    /**
     * Returns the {@link IndexesPolicy} (default: {@link IndexesPolicy#ALL})
     */
    public IndexesPolicy getIndexesPolicy() {

	return indexesPolicy;
    }

    /**
     * Returns the {@link ExtendedElementsPolicy} (default: {@link ExtendedElementsPolicy#ALL})
     */
    public ExtendedElementsPolicy getExtendedElementsPolicy() {

	return extendedElementsPolicy;
    }

    /**
     * Returns the list of selected indexes names
     */
    public List<String> getIndexes() {

	return quaryableList.stream().map(q -> q.getName()).collect(Collectors.toList());
    }

    /**
     * Returns the list of selected indexes queryables
     */
    public List<Queryable> getIndexesQueryables() {

	return quaryableList;
    }

    /**
     * Returns the list of selected indexes names
     */
    public List<String> getExtendedElements() {

	return extendedElementList;
    }
}
