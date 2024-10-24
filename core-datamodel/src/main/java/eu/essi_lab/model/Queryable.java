package eu.essi_lab.model;

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

import java.util.Arrays;
import java.util.NoSuchElementException;

import javax.xml.bind.annotation.XmlSeeAlso;

import eu.essi_lab.model.index.IndexedElement;
import eu.essi_lab.model.ontology.OntologyObjectProperty;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;

/**
 * An interface for queryable properties with a name.
 * A queryable property can be used as parameter of a discovery query
 * <li>The volatile property</li><br>
 * Usually a queryable property has a correspondent {@link IndexedElement}, otherwise the {@link #isVolatile()}
 * property is set to <code>true</code>
 * 
 * @see MetadataElement
 * @see ResourceProperty
 * @author Fabrizio
 */
@XmlSeeAlso({ MetadataElement.class, ResourceProperty.class, OntologyObjectProperty.class, RuntimeInfoElement.class })
public interface Queryable {

    /**
     * @author Fabrizio
     */
    public enum ContentType {
	/**
	 * 
	 */
	TEXTUAL,
	/**
	 * 
	 */
	INTEGER,

	/**
	 * 
	 */
	LONG,

	/**
	 * 
	 */
	DOUBLE,
	/**
	 * 
	 */
	SPATIAL,
	/**
	 * 
	 */
	ISO8601_DATE_TIME,
	/**
	 * 
	 */
	ISO8601_DATE,
	/**
	 * 
	 */
	BOOLEAN
    }

    /**
     * @param name
     * @return
     * @throws NoSuchElementException
     */
    public static Queryable fromName(String name, Queryable[] values) throws IllegalArgumentException {

	return Arrays.asList(values).//
		stream().//
		filter(p -> p.getName().equals(name)). //
		findFirst().//
		orElseThrow(() -> new IllegalArgumentException("Unknown name: " + name));
    }

    /**
     * Returns this property name
     */
    public String getName();

    /**
     * Returns the content type of this property
     */
    public ContentType getContentType();

    /**
     * Return <code>true</code> if this element has no correspondent {@link IndexedElement}, <code>false</code>
     * otherwise
     */
    public boolean isVolatile();

    /**
     * Set the {@link #isEnabled()} property
     */
    public void setEnabled(boolean enabled);

    /**
     * Return <code>true</code> if this element is enabled to be queried, <code>false</code> otherwise
     */
    public boolean isEnabled();
}
