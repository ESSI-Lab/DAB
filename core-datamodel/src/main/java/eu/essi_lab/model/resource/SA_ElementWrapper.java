/**
 * 
 */
package eu.essi_lab.model.resource;

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

import eu.essi_lab.model.Queryable.ContentType;
import eu.essi_lab.model.resource.composed.ComposedElement;
import eu.essi_lab.model.resource.composed.ComposedElementBuilder;
import eu.essi_lab.model.resource.composed.ComposedElementItem;

/**
 * @author Fabrizio
 */
public class SA_ElementWrapper {

    private ComposedElement element;

    /**
     * @param name
     * @return
     */
    public static ComposedElement of(String name) {

	return ComposedElementBuilder.get(name).//

		addItem("value", ContentType.TEXTUAL).//
		addItem("uri", ContentType.TEXTUAL).//
		addItem("uri_title", ContentType.TEXTUAL).//
		addItem("SA_uri", ContentType.TEXTUAL).//
		addItem("SA_uri_title", ContentType.TEXTUAL).//
		addItem("SA_match_type", ContentType.TEXTUAL).//
		build();
    }

    /**
     * @param element
     * @return
     */
    public static SA_ElementWrapper of(MetadataElement element) {

	return new SA_ElementWrapper(element.createComposedElement().get());
    }

    /**
     * @return the element
     */
    public ComposedElement getElement() {

	return element;
    }

    /**
     * @return
     */
    public String getValue() {

	return element.getProperty("value").get().getStringValue();
    }

    /**
     * @return
     */
    public String getUri() {

	return getUriItem().getStringValue();
    }

    /**
     * @return
     */
    public String getUriTitle() {

	return getUriTitleItem().getStringValue();
    }

    /**
     * @return
     */
    public String getSA_Uri() {

	return getSA_UriItem().getStringValue();
    }

    /**
     * @return
     */
    public String getSA_UriTitle() {

	return getSA_UriTitleItem().getStringValue();
    }

    /**
     * @return
     */
    public String getSA_MatchType() {

	return getSA_MatchTypeItem().getStringValue();
    }

    /**
     * @return
     */
    public ComposedElementItem getValueItem() {

	return element.getProperty("value").get();
    }

    /**
     * @return
     */
    public ComposedElementItem getUriItem() {

	return element.getProperty("uri").get();
    }

    /**
     * @return
     */
    public ComposedElementItem getUriTitleItem() {

	return element.getProperty("uri_title").get();
    }

    /**
     * @return
     */
    public ComposedElementItem getSA_UriItem() {

	return element.getProperty("SA_uri").get();
    }

    /**
     * @return
     */
    public ComposedElementItem getSA_UriTitleItem() {

	return element.getProperty("SA_uri_title").get();
    }

    /**
     * @return
     */
    public ComposedElementItem getSA_MatchTypeItem() {

	return element.getProperty("SA_match_type").get();
    }

    /**
     * @param value
     */
    public void setValue(String value) {

	element.getProperty("value").get().setValue(value);
    }

    /**
     * @param uri
     */
    public void setUri(String uri) {

	element.getProperty("uri").get().setValue(uri);
    }

    /**
     * @param uriTitle
     */
    public void setUriTitle(String uriTitle) {

	element.getProperty("uri_title").get().setValue(uriTitle);
    }

    /**
     * @param saUri
     */
    public void setSA_Uri(String saUri) {

	element.getProperty("SA_uri").get().setValue(saUri);
    }

    /**
     * @param uriTitle
     */
    public void setSA_UriTitle(String uriTitle) {

	element.getProperty("SA_uri_title").get().setValue(uriTitle);
    }

    /**
     * @return
     */
    public void setSA_MatchType(String matchType) {

	element.getProperty("SA_match_type").get().setValue(matchType);
    }

    /**
     * @param element
     */
    private SA_ElementWrapper(ComposedElement element) {

	this.element = element;
    }

}
