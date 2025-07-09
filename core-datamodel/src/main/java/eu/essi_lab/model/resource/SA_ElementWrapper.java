/**
 * 
 */
package eu.essi_lab.model.resource;

import eu.essi_lab.model.Queryable.ContentType;
import eu.essi_lab.model.resource.composed.ComposedElement;
import eu.essi_lab.model.resource.composed.ComposedElementBuilder;

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
		addItem("SA_matchType", ContentType.TEXTUAL).//
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

	return element.getProperty("uri").get().getStringValue();
    }

    /**
     * @return
     */
    public String getUriTitle() {

	return element.getProperty("uri_title").get().getStringValue();
    }

    /**
     * @return
     */
    public String getSA_Uri() {

	return element.getProperty("SA_uri").get().getStringValue();
    }

    /**
     * @return
     */
    public String getSA_UriTitle() {

	return element.getProperty("SA_uri_title").get().getStringValue();
    }

    /**
     * @return
     */
    public String getSA_MatchType() {

	return element.getProperty("SA_matchType").get().getStringValue();
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

	element.getProperty("SA_matchType").get().setValue(matchType);
    }

    /**
     * @param element
     */
    private SA_ElementWrapper(ComposedElement element) {

	this.element = element;
    }

}
