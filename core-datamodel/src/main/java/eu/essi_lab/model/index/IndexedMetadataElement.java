package eu.essi_lab.model.index;

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

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.index.jaxb.BoundingBox;
import eu.essi_lab.model.resource.BNHSProperty;
import eu.essi_lab.model.resource.BNHSPropertyReader;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import net.opengis.iso19139.gco.v_20060504.CharacterStringPropertyType;
import net.opengis.iso19139.gco.v_20060504.CodeListValueType;
import net.opengis.iso19139.gmx.v_20060504.AnchorType;

/**
 * An {@link IndexedElement} whose {@link #getValues()} are defined by the {@link #defineValues(GSResource)}
 * method and {@link #getElementName()} can be set according to {@link MetadataElement#getName()}.<br>
 * All the {@link MetadataElement}s related to these indexes are <i>non-volatile</i>
 * {@link Queryable} properties.<br>
 * To create a <b>custom element</b>, use the constructors {@link #IndexedMetadataElement(String)} or
 * {@link #IndexedMetadataElement(String, String)}
 * 
 * @see #getMetadataElement()
 * @see #defineValues(GSResource)
 * @see Queryable#isVolatile()
 * @author Fabrizio
 */
public abstract class IndexedMetadataElement extends IndexedElement {

    private MetadataElement element;
    private BoundingBox boundingBox;

    /**
     * Creates a new custom <code>IndexedMetadataElement</code> with the supplied <code>elementName</code>
     * 
     * @param elementName a non <code>null</code> string containing only alphabetical characters and "_" character
     */
    public IndexedMetadataElement(String elementName) {

	super(elementName);
    }

    /**
     * Creates a new custom <code>IndexedMetadataElement</code> with the supplied <code>elementName</code> and
     * <code>value</code>
     * 
     * @param elementName a non <code>null</code> string containing only alphabetical characters and "_" character
     * @param value a non <code>null</code> string, empty string is admitted
     */
    public IndexedMetadataElement(String elementName, String value) {

	super(elementName, value);
    }

    /**
     * Creates a new indexed element related to the supplied <code>element</code>
     * 
     * @param element
     */
    public IndexedMetadataElement(MetadataElement element) {
	super(element.getName());
	this.element = element;
    }

    public void addKeywords(GSResource resource, String type) {
	addKeywords(resource, type, new String[] {});
    }

    public void addKeywords(GSResource resource, String typeToInclude, String... typesToExclude) {
	Iterator<DataIdentification> identifications = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		.getDataIdentifications();
	while (identifications.hasNext()) {
	    DataIdentification dataId = identifications.next();
	    Iterator<Keywords> keywords = dataId.getKeywords();
	    k: while (keywords.hasNext()) {
		Keywords keyword = (Keywords) keywords.next();
		String t = keyword.getTypeCode();
		if (typeToInclude != null) {
		    if (t == null || !t.equals(typeToInclude)) {
			continue;
		    }
		}
		if (typesToExclude.length > 0) {
		    for (String typeToExclude : typesToExclude) {
			if (typeToExclude == null) {
			    if (t == null) {
				continue k;
			    }
			} else {
			    if (t != null && t.equals(typeToExclude)) {
				continue k;
			    }
			}
		    }
		}
		List<CharacterStringPropertyType> ks = keyword.getElementType().getKeyword();
		for (CharacterStringPropertyType k : ks) {
		    if (k == null || k.getCharacterString() == null || k.getCharacterString().getValue() == null) {
			continue;
		    }
		    Object obj = k.getCharacterString().getValue();
		    if (obj instanceof AnchorType) {
			AnchorType anchor = (AnchorType) obj;
			String title = anchor.getValue();
			if (checkStringValue(title)) {
			    getValues().add(title);
			}
		    } else if (obj instanceof CodeListValueType) {
			CodeListValueType cvt = (CodeListValueType) ks;
			String title = cvt.getCodeListValue();
			if (checkStringValue(title)) {
			    getValues().add(title);
			}
		    } else if (obj instanceof String) {
			String title = (String) obj;
			if (checkStringValue(title)) {
			    getValues().add(title);
			}
		    }
		}
	    }
	}
    }

    public void addKeywordsURI(GSResource resource, String type) {
	addKeywordsURI(resource, type, new String[] {});
    }

    public void addKeywordsURI(GSResource resource, String typeToInclude, String... typesToExclude) {
	Iterator<DataIdentification> identifications = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		.getDataIdentifications();
	while (identifications.hasNext()) {
	    DataIdentification dataId = identifications.next();
	    Iterator<Keywords> keywords = dataId.getKeywords();
	    k: while (keywords.hasNext()) {
		Keywords keyword = (Keywords) keywords.next();
		String t = keyword.getTypeCode();
		if (typeToInclude != null) {
		    if (t == null || !t.equals(typeToInclude)) {
			continue;
		    }
		}
		if (typesToExclude.length > 0) {
		    for (String typeToExclude : typesToExclude) {
			if (typeToExclude == null) {
			    if (t == null) {
				continue k;
			    }
			} else {
			    if (t != null && t.equals(typeToExclude)) {

				continue k;
			    }
			}
		    }
		}
		List<CharacterStringPropertyType> ks = keyword.getElementType().getKeyword();
		for (CharacterStringPropertyType k : ks) {
		    if (k == null || k.getCharacterString() == null || k.getCharacterString().getValue() == null) {
			continue;
		    }
		    Object obj = k.getCharacterString().getValue();
		    if (obj instanceof AnchorType) {
			AnchorType anchor = (AnchorType) obj;
			String href = anchor.getHref();
			if (checkStringValue(href)) {
			    getValues().add(href);
			}
		    }
		}
	    }
	}
    }

    /**
     * Creates a new indexed element related to {@link MetadataElement#BOUNDING_BOX} with
     * the supplied <code>bbox</code>
     *
     * @param bbox
     */
    public IndexedMetadataElement(BoundingBox bbox) {
	super(MetadataElement.BOUNDING_BOX.getName());
	this.boundingBox = bbox;
    }

    /**
     * Set the {@link BoundingBox} if this indexed element is related to {@link MetadataElement#BOUNDING_BOX}
     * 
     * @param bbox
     */
    public void setBoundingBox(BoundingBox bbox) {

	this.boundingBox = bbox;
    }

    /**
     * Get the {@link BoundingBox} if this indexed element is related to {@link MetadataElement#BOUNDING_BOX}
     * 
     * @return the {@link BoundingBox} if this indexed element is related to {@link MetadataElement#BOUNDING_BOX} or
     *         <code>null</code> otherwise
     */
    public BoundingBox getBoundingBox() {

	return boundingBox;
    }

    /**
     * If this element is custom, returns an empty
     * {@link Optional}, otherwise returns an {@link Optional} describing this {@link MetadataElement}
     * 
     * @return if this element is custom, returns an empty
     *         {@link Optional}, otherwise returns an {@link Optional} describing this {@link MetadataElement}
     */
    public Optional<MetadataElement> getMetadataElement() {

	return Optional.ofNullable(element);
    }

    /**
     * Defines the value/s of this indexed element by retrieving it/them from the
     * {@link CoreMetadata} and adding such value/s to the {@link #getValues()} list. If this element
     * is related to {@link MetadataElement#BOUNDING_BOX}, this method set a {@link BoundingBox} with
     * the proper values using the {@link #setBoundingBox(BoundingBox)} method
     * 
     * @implnote
     *           Following example shows the implementation for the {@link IndexedMetadataElement} related to
     *           {@link MetadataElement#IDENTIFIER}
     * 
     *           <pre>
     *  <code>@Override
     *  public void defineValues(GSResource resource){
             * 
             * 	// get the value of the identifier 
             * 	String identifier = resource.getHarmonizedMetadata().getCoreMetadata().getIdentifier();
             * 
             * 	getValues().add(identifier);
             * }
             * </pre></code>
     * @param resource the {@link GSResource} to be written
     */
    public abstract void defineValues(GSResource resource);

    protected void defineBNHSProperty(BNHSProperty property, GSResource resource) {
	Optional<String> value = BNHSPropertyReader.readProperty(resource, property);
	if (value.isPresent()) {
	    if (checkStringValue(value.get())) {
		getValues().add(value.get());
	    }
	}
    }

    /**
     * @param value
     * @return
     */
    protected static boolean checkStringValue(String value) {

	return value != null && !value.equals("");
    }
}
